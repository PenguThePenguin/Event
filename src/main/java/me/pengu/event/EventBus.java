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

package me.pengu.event;

import me.pengu.event.data.Acceptor;
import me.pengu.event.data.PostOrder;
import me.pengu.event.data.Subscribe;
import me.pengu.event.generic.SimpleEventBus;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.lang.reflect.Array;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;

public interface EventBus<E> extends AutoCloseable {

    /**
     * Creates a bus by its event type.
     *
     * @param eventType the type of event that this bus will handle.
     * @return the event bus created.
     */
    static <E> @NonNull EventBus<E> of(@NonNull Class<E> eventType) {
        return new SimpleEventBus<>(eventType);
    }

    /**
     * Creates a bus by its event type and a specified acceptor.
     *
     * @param eventType the type of event that this bus will handle.
     * @param acceptor the acceptor which will decide if a subscription's event should be handled.
     * @return the event bus created.
     */
    static <E> @NonNull EventBus<E> of(@NonNull Class<E> eventType, Acceptor<E> acceptor) {
        return new SimpleEventBus<>(eventType, acceptor);
    }

    /**
     * Gets the type of event accepted by this bus.
     *
     * @return the ({@link E}) type of the event.
     */
    @NonNull Class<E> getEventType();

    /**
     * Gets a {@link Map} of the event type and the {@link Subscriptions} registered.
     *
     * @return a map of subscriptions.
     */
    @NonNull Map<Class<? extends E>, Subscriptions<E>> getSubscriptions();

    /**
     * Registers all of a {@link Object}'s methods that are annotated with @{@link Subscribe}.
     *
     * @param subscriber the subscriber to register
     */
    void register(@NonNull Object subscriber);

    /**
     * Registers an event handler for the given event type.
     *
     * @param eventType the type of event to subscribe to.
     * @param handler the event handler to register.
     * @return the subscription that was generated.
     */
    default @NonNull Subscription<E> register(@NonNull Class<? extends E> eventType, @NonNull EventHandler<? super E> handler) {
        return this.register(eventType, handler, PostOrder.NORMAL);
    }

    /**
     * Registers an event handler with a given post order.
     *
     * @param eventType the type of event to subscribe to.
     * @param handler the handler to be registered.
     * @param order the order in which the handler should be called.
     * @return the subscription that was generated.
     */
    @NonNull Subscription<E> register(@NonNull Class<? extends E> eventType, @NonNull EventHandler<? super E> handler, int order);

    /**
     * Register a subscription for a specific event type.
     *
     * @param eventType the type of event that the subscription is interested in.
     * @param subscription the subscription to register.
     */
    void register(@NonNull Class<? extends E> eventType, @NonNull Subscription<E> subscription);

    /**
     * Post an event to all registered subscriptions.
     *
     * @param event the event to post.
     * @return a CompletableFuture encapsulating its PostResult.
     */
    @NonNull <T extends E> CompletableFuture<PostResult<? super T>> post(@NonNull E event);

    /**
     * Unregister a subscription from the event bus.
     *
     * @param subscription the subscription to unregister.
     */
    void unregister(@NonNull Subscription<E> subscription);

    /**
     * Unregister all subscriptions that match the given {@link Predicate}
     *
     * @param predicate the predicate to test the subscription should be removed.
     */
    void unregisterIf(@NonNull Predicate<? super Subscription<E>> predicate);

    /**
     * Unregister all the registered subscriptions.
     */
    void unregisterAll();

    /**
     * Gets an {@link Array} of all registered {@link Subscription}'s based on its event type.
     *
     * @param eventType the type of event.
     * @return All registered subscriptions.
     */
    @Nullable Subscription<? super E>[] getSubscriptions(@NonNull Class<?> eventType);

    /**
     * Returns if the given event type is currently subscribed to.
     *
     * @param eventType The type of event you want to check is subscribed.
     * @return if the event is subscribed.
     */
    boolean isSubscribed(@NonNull Class<?> eventType);

    /**
     * Cleans up this bus instance.
     */
    @Override
    default void close() {
        this.unregisterAll();
    }

}