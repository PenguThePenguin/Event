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

import com.google.common.collect.Maps;
import io.github.penguthepenguin.event.Subscription;
import io.github.penguthepenguin.event.Subscriptions;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Predicate;

public class SimpleSubscriptions<E> implements Subscriptions<E> {

    private final Map<Integer, List<Subscription<E>>> subscriptions;
    private Subscription<E>[] subscriptionsArray;

    public SimpleSubscriptions() {
        this.subscriptions = Maps.newConcurrentMap();
        this.bake();
    }

    /**
     * Geta all subscriptions, by their priority.
     *
     * @return a {@link Map} of subscriptions.
     */
    @Override
    public @NonNull Map<Integer, List<Subscription<E>>> getSubscriptions() {
        return this.subscriptions;
    }

    /**
     * Gets all registered subscriptions in an array for quick iteration.
     *
     * @return an array of subscriptions.
     */
    @Override
    public @NonNull Subscription<E>[] getRegisteredSubscriptions() {
        return this.subscriptionsArray;
    }

    /**
     * Refresh currently registered subscriptions
     */
    @SuppressWarnings({"unchecked"})
    public synchronized void bake() {
        List<Subscription<E>> entries = new ArrayList<>();
        for (Entry<Integer, List<Subscription<E>>> entry : this.subscriptions.entrySet()) {
            entries.addAll(entry.getValue());
        }

        entries.sort(Subscription.SUBSCRIPTION_COMPARATOR);
        this.subscriptionsArray = entries.toArray(new Subscription[0]);
    }

    /**
     * Register a subscription to this event type.
     *
     * @param subscription the subscription to register.
     */
    @Override
    public synchronized void register(Subscription<E> subscription) {
        this.subscriptions.computeIfAbsent(subscription.getOrder(), integer -> new ArrayList<>()).add(subscription);
        this.bake();
    }

    /**
     * Unregister a subscription from this event type.
     *
     * @param subscription the subscription to unregister.
     */
    @Override
    public synchronized void unregister(@NonNull Subscription<E> subscription) {
        this.unregisterIf(sub -> sub == subscription);
    }

    /**
     * Unregister all subscriptions that match the given predicate
     *
     * @param predicate a predicate to check the subscription should be unregistered.
     */
    @Override
    public synchronized void unregisterIf(@NonNull Predicate<? super Subscription<E>> predicate) {
        boolean changed = false;

        for (List<Subscription<E>> subscriptions : this.subscriptions.values()) {
            for (ListIterator<Subscription<E>> iterator = subscriptions.listIterator(); iterator.hasNext(); ) {
                if (predicate.test(iterator.next())) {
                    iterator.remove();
                    changed = true;
                }
            }
        }

        if (changed) {
            this.bake();
        }
    }

}
