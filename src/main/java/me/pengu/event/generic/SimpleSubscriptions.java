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

package me.pengu.event.generic;

import com.google.common.collect.Maps;
import me.pengu.event.Subscription;
import me.pengu.event.Subscriptions;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

public class SimpleSubscriptions<E> implements Subscriptions<E> {

    private Subscription<E>[] subscriptions = null;
    private final Map<Integer, List<Subscription<E>>> subscriptionCache;

    public SimpleSubscriptions() {
        this.subscriptionCache = Maps.newConcurrentMap();
    }

    @Override
    public @NonNull Map<Integer, List<Subscription<E>>> getSubscriptions() {
        return this.subscriptionCache;
    }

    @Override
    public @NonNull Subscription<E>[] getRegisteredSubscriptions() {
        Subscription<E>[] subscriptions;
        while((subscriptions = this.subscriptions) == null) this.refreshSubscriptions();
        return subscriptions;
    }

    @SuppressWarnings({"unchecked"})
    public synchronized void refreshSubscriptions() {
        List<Subscription<E>> entries = new ArrayList<>();
        for (Map.Entry<Integer, List<Subscription<E>>> entry : this.subscriptionCache.entrySet()) {
            entries.addAll(entry.getValue());
        }

        entries.sort(Subscription.SUBSCRIPTION_COMPARATOR);
        this.subscriptions = entries.toArray(new Subscription[0]);
    }

    @Override
    public void register(Subscription<E> subscription) {
        this.subscriptionCache.computeIfAbsent(subscription.getOrder(), integer -> new ArrayList<>()).add(subscription);
        this.refreshSubscriptions();
    }

    @Override
    public void unregister(@NonNull Subscription<E> subscription) {
        this.unregisterIf(sub -> sub == subscription);
    }

    @Override
    public void unregisterIf(@NonNull Predicate<? super Subscription<E>> predicate) {
        this.subscriptionCache.forEach((key, value) -> value.stream().filter(predicate).forEach(subscription -> {
            this.subscriptionCache.remove(key);
            this.refreshSubscriptions();
        }));
    }

}
