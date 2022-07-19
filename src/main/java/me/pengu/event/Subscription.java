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

package me.pengu.event;

import me.pengu.event.data.PostOrder;
import me.pengu.event.generic.SimpleSubscription;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.lang.reflect.Method;
import java.util.Comparator;

public interface Subscription<E> {

    Comparator<Subscription<?>> SUBSCRIPTION_COMPARATOR = Comparator.comparingInt(Subscription::getOrder);

    static <E> @NonNull Subscription<E> of(int order, @NonNull EventBus<E> bus, @NonNull Class<? extends E> eventClass, @NonNull Object target, @NonNull Method method) {
        return new SimpleSubscription<>(order, bus, eventClass, target, method);
    }

    static <E> @NonNull Subscription<E> of(int order, @NonNull EventBus<E> bus, @NonNull Class<? extends E> eventClass, @NonNull EventHandler<? super E> handler) {
        return new SimpleSubscription<>(order, bus, eventClass, handler);
    }

    void on(@NonNull E event) throws Throwable;

    default int getOrder() {
        return PostOrder.NORMAL;
    }

    default boolean acceptsCancelled() {
        return true;
    }

}
