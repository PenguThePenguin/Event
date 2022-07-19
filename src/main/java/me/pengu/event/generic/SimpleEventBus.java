/*
 * This file is part of bCore, licensed under the MIT License.
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

package me.pengu.event.generic;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import lombok.Getter;
import me.pengu.event.*;
import me.pengu.event.data.Acceptor;
import me.pengu.event.data.Subscribe;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;

@Getter
public class SimpleEventBus<E> implements EventBus<E> {

    private final Class<E> eventType;
    private final Acceptor<E> acceptor;

    private final Map<Class<? extends E>, Subscriptions<E>> subscriptions;

    public SimpleEventBus(Class<E> eventType) {
        this(eventType, Acceptor.nonCancelWhenNonAcceptingCancelled());
    }

    public SimpleEventBus(Class<E> eventType, Acceptor<E> acceptor) {
        this.eventType = eventType;
        this.acceptor = acceptor;
        this.subscriptions = Maps.newConcurrentMap();
    }

    @Override
    public void register(@NonNull Object listener) {
        List<SimpleSubscription<E>> subscriptions = new ArrayList<>();

        for (Method method : listener.getClass().getDeclaredMethods()) {
            method.setAccessible(true);

            if (method.isAnnotationPresent(Subscribe.class)) {
                Class<?>[] parameters = method.getParameterTypes();
                Preconditions.checkArgument(parameters.length == 1,
                        "Method %s has @Subscribe annotation but has %s parameters." +
                                "Subscriber methods must only have 1 parameter.", method, parameters.length
                );

                Subscribe subscribe = method.getAnnotation(Subscribe.class);
                Class<?> eventType = method.getParameterTypes()[0];

                if (!eventType.isAssignableFrom(this.eventType)) continue;
                Class<? extends E> event = eventType.asSubclass(this.eventType);

                subscriptions.add(
                        new SimpleSubscription<>(subscribe.order(), this, event, listener, method)
                );
            }
        }

        for (SimpleSubscription<E> subscription : subscriptions) {
            this.register(subscription.getEventClass(), subscription);
        }
    }

    @Override
    public @NonNull SimpleSubscription<E> register(@NonNull Class<? extends E> eventType, @NonNull EventHandler<? super E> handler, int order) {
        SimpleSubscription<E> subscription = new SimpleSubscription<>(order, this, eventType, handler);
        this.register(eventType, subscription);

        return subscription;
    }

    @Override
    public void register(@NonNull Class<? extends E> eventType, @NonNull Subscription<E> subscription) {
        Preconditions.checkState(eventType.isAssignableFrom(this.eventType),
                "Class %s doesn't implement the event type %s.", eventType, this.eventType
        );

        this.subscriptions.computeIfAbsent(eventType, clazz -> new SimpleSubscriptions<>()).register(subscription);
    }

    @Override
    public @NonNull <T extends E> CompletableFuture<PostResult<? super T>> post(@NonNull E event) {
        ImmutableMap.Builder<Subscription<? super E>, Throwable> exceptions = null;

        for (Subscription<? super E> subscription : this.getSubscriptions(event.getClass())) {
            if (subscription != null && this.acceptor.accepts(eventType, event, subscription)) {
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

    @Override
    public void unregister(@NonNull Subscription<E> subscription) {
        this.unregisterIf(sub -> sub == subscription);
    }

    @Override
    public void unregisterIf(@NonNull Predicate<? super Subscription<E>> predicate) {
        for (Subscriptions<E> subscription : this.subscriptions.values()) {
            subscription.unregisterIf(predicate);
        }
    }

    @Override
    public void unregisterAll() {
        this.subscriptions.clear();
    }

    @Override
    public @Nullable Subscription<? super E>[] getSubscriptions(@NonNull Class<?> eventType) {
        Subscriptions<E> subscriptions = this.subscriptions.get(eventType);
        if (subscriptions == null) return null;

        return subscriptions.getRegisteredSubscriptions();
    }

    @Override
    public boolean isSubscribed(@NonNull Class<?> eventType) {
        Subscription<? super E>[] subscriptions = this.getSubscriptions(eventType);
        return subscriptions != null && subscriptions.length != 0;
    }

}