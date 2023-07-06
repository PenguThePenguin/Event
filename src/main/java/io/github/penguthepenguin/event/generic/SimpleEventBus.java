/*
 * This file is part of Event, licensed under the MIT License.
 *
 * Copyright (c) pengu
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package io.github.penguthepenguin.event.generic;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import io.github.penguthepenguin.event.*;
import io.github.penguthepenguin.event.data.Subscribe;
import lombok.Getter;
import me.pengu.event.*;
import io.github.penguthepenguin.event.data.Acceptor;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;

@Getter
public class SimpleEventBus<E> implements EventBus<E> {

    private final Class<E> eventType;
    private final Acceptor<E> acceptor;

    private final Map<Class<? extends E>, Subscriptions<E>> subscriptions;

    public SimpleEventBus(Class<E> eventType) {
        this(eventType, Acceptor.nonCancelingWhenNotAcceptingCancelled());
    }

    public SimpleEventBus(Class<E> eventType, Acceptor<E> acceptor) {
        this.eventType = eventType;
        this.acceptor = acceptor;
        this.subscriptions = Maps.newConcurrentMap();
    }

    /**
     * Registers all of a {@link Class}'s static methods that are annotated with @{@link Subscribe}.
     *
     * @param subscriber the subscriber to register
     */
    @Override
    public void register(@NonNull Class<?> subscriber) {
        for (Method method : subscriber.getMethods()) {
            SimpleSubscription<E> subscription = this.generateSubscription(method, null);

            if (subscription != null) {
                this.register(subscription.getEventClass(), subscription);
            }
        }
    }

    /**
     * Registers all of a {@link Object}'s methods that are annotated with @{@link Subscribe}.
     *
     * @param subscriber the subscriber to register
     */
    @Override
    public void register(@NonNull Object subscriber) {
        Set<Method> methods = new HashSet<>(); // fetching all public / private methods
        methods.addAll(Arrays.asList(subscriber.getClass().getMethods()));
        methods.addAll(Arrays.asList(subscriber.getClass().getDeclaredMethods()));

        for (Method method : methods) {
            SimpleSubscription<E> subscription = this.generateSubscription(method, subscriber);

            if (subscription != null) {
                this.register(subscription.getEventClass(), subscription);
            }
        }
    }

    /**
     * Generates a subscription based off a method, and it's containing class.
     *
     * @param method the method that is annotated with @Subscribe
     * @param target the object that contains the method that will be invoked when the event is fired.
     * @return the generated {@link SimpleSubscription}.
     */
    public SimpleSubscription<E> generateSubscription(Method method, Object target) {
        method.setAccessible(true);
        if (!method.isAnnotationPresent(Subscribe.class)
                || target == null != Modifier.isStatic(method.getModifiers())) return null;

        Class<?>[] parameters = method.getParameterTypes();
        Preconditions.checkArgument(parameters.length == 1,
                "Method %s has @Subscribe annotation but has %s parameters." +
                        "Subscriber methods must only have 1 parameter.", method, parameters.length
        );

        Class<?> eventType = parameters[0];
        if (!eventType.isAssignableFrom(this.eventType)) return null;

        Class<? extends E> event = eventType.asSubclass(this.eventType);
        Subscribe subscribe = method.getAnnotation(Subscribe.class);

        return new SimpleSubscription<>(
                subscribe.order(), this, event, target, method, !subscribe.ignoreCancelled()
        );
    }

    /**
     * Registers an event handler with a given post order.
     *
     * @param eventType the type of event to subscribe to.
     * @param handler   the handler to be registered.
     * @param order     the order in which the handler should be called.
     * @param ignoreCancelled weather this handler should ignore cancelled events.
     * @return the subscription that was generated.
     */
    @Override
    public @NonNull SimpleSubscription<E> register(@NonNull Class<? extends E> eventType, @NonNull EventHandler<? super E> handler, int order, boolean ignoreCancelled) {
        SimpleSubscription<E> subscription = new SimpleSubscription<>(order, this, eventType, handler, !ignoreCancelled);
        this.register(eventType, subscription);

        return subscription;
    }

    /**
     * Register a subscription for a specific event type.
     *
     * @param eventType    the type of event that the subscription is interested in.
     * @param subscription the subscription to register.
     */
    @Override
    public void register(@NonNull Class<? extends E> eventType, @NonNull Subscription<E> subscription) {
        Preconditions.checkState(eventType.isAssignableFrom(this.eventType),
                "Class %s doesn't implement the event type %s.", eventType, this.eventType
        );

        this.subscriptions.computeIfAbsent(eventType, clazz -> new SimpleSubscriptions<>()).register(subscription);
    }

    /**
     * Post an event to all registered subscriptions.
     *
     * @param event the event to post.
     * @return a CompletableFuture encapsulating its PostResult.
     */
    @Override
    public @NonNull <T extends E> CompletableFuture<PostResult<? super T>> post(@NonNull E event) {
        ImmutableMap.Builder<Subscription<? super E>, Throwable> exceptions = null;

        Subscription<? super E>[] subscriptions = this.getSubscriptions(event.getClass());
        if (subscriptions != null) {

            for (Subscription<? super E> subscription : subscriptions) {
                if (subscription == null || !this.acceptor.accepts(eventType, event, subscription)) continue;

                try {
                    subscription.on(event);
                } catch (Throwable e) {
                    if (exceptions == null) {
                        exceptions = ImmutableMap.builder();
                    }

                    exceptions.put(subscription, e);
                }
            }

        }

        return CompletableFuture.completedFuture(exceptions == null
                ? PostResult.of(event)
                : PostResult.of(event, exceptions.build())
        );
    }

    /**
     * Unregister a subscription from the event bus.
     *
     * @param subscription the subscription to unregister.
     */
    @Override
    public void unregister(@NonNull Subscription<E> subscription) {
        this.unregisterIf(sub -> sub == subscription);
    }

    /**
     * Unregister all subscriptions that match the given {@link Predicate}
     *
     * @param predicate the predicate to test the subscription should be removed.
     */
    @Override
    public void unregisterIf(@NonNull Predicate<? super Subscription<E>> predicate) {
        for (Subscriptions<E> subscription : this.subscriptions.values()) {
            subscription.unregisterIf(predicate);
        }
    }

    /**
     * Unregister all the registered subscriptions.
     */
    @Override
    public void unregisterAll() {
        this.subscriptions.clear();
    }

    /**
     * Returns if the given event type is currently subscribed to.
     *
     * @param eventType The type of event you want to check is subscribed.
     * @return if the event is subscribed.
     */
    @Override
    public boolean isSubscribed(@NonNull Class<?> eventType) {
        Subscription<? super E>[] subscriptions = this.getSubscriptions(eventType);
        return subscriptions != null && subscriptions.length != 0;
    }

    /**
     * Gets an {@link Array} of all registered {@link Subscription}'s based on its event type.
     *
     * @param eventType the type of event.
     * @return All registered subscriptions.
     */
    @Override
    public @Nullable Subscription<? super E>[] getSubscriptions(@NonNull Class<?> eventType) {
        Subscriptions<E> subscriptions = this.subscriptions.get(eventType);
        return subscriptions == null ? null : subscriptions.getRegisteredSubscriptions();
    }

}