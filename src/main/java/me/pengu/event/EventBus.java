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
import me.pengu.event.generic.SimpleEventBus;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;

public interface EventBus<E> extends AutoCloseable {

    static <E> @NonNull EventBus<E> of(@NonNull Class<E> eventType) {
        return new SimpleEventBus<>(eventType);
    }

    static <E> @NonNull EventBus<E> of(@NonNull Class<E> eventType, Acceptor<E> acceptor) {
        return new SimpleEventBus<>(eventType, acceptor);
    }

    @NonNull Class<E> getEventType();

    void register(@NonNull Object listener);

    default @NonNull Subscription<E> register(@NonNull Class<? extends E> eventType, @NonNull EventHandler<? super E> handler) {
        return this.register(eventType, handler, PostOrder.NORMAL);
    }

    @NonNull Subscription<E> register(@NonNull Class<? extends E> eventType, @NonNull EventHandler<? super E> handler, int order);

    void register(@NonNull Class<? extends E> eventType, @NonNull Subscription<E> subscription);

    @NonNull <T extends E> CompletableFuture<PostResult<? super T>> post(@NonNull E event);

    void unregister(@NonNull Subscription<E> subscription);

    void unregisterIf(@NonNull Predicate<? super Subscription<E>> predicate);

    void unregisterAll();

    @Nullable Subscription<? super E>[] getSubscriptions(@NonNull Class<?> eventType);

    boolean isSubscribed(@NonNull Class<?> eventType);

    @NonNull Map<Class<? extends E>, Subscriptions<E>> getSubscriptions();

    @Override
    default void close() {
        this.unregisterAll();
    }

}