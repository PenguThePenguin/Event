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

import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

public interface Subscriptions<E> {

    /**
     * Geta all subscriptions, by their priority.
     *
     * @return a {@link Map} of subscriptions.
     */
    @NonNull Map<Integer, List<Subscription<E>>> getSubscriptions();

    /**
     * Gets all registered subscriptions in an array for quick iteration.
     *
     * @return an array of subscriptions.
     */
    @NonNull Subscription<E>[] getRegisteredSubscriptions();

    /**
     * Bakes currently registered subscriptions
     */
    void bake();

    /**
     * Register a subscription to this event type.
     *
     * @param subscription the subscription to register.
     */
    void register(Subscription<E> subscription);

    /**
     * Unregister a subscription from this event type.
     *
     * @param subscription the subscription to unregister.
     */
    void unregister(Subscription<E> subscription);

    /**
     * Unregister all subscriptions that match the given predicate
     *
     * @param predicate a predicate to check the subscription should be unregistered.
     */
    void unregisterIf(@NonNull Predicate<? super Subscription<E>> predicate);

}
