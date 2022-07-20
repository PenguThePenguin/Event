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

import me.pengu.event.data.PostOrder;
import me.pengu.event.generic.SimpleSubscription;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.lang.reflect.Method;
import java.util.Comparator;

public interface Subscription<E> {

    Comparator<Subscription<?>> SUBSCRIPTION_COMPARATOR = Comparator.comparingInt(Subscription::getOrder);

    /**
     * Creates a subscription based off a method
     *
     * @param order the order in which the subscription should be handled.
     * @param bus the {@link EventBus} instance that the {@link Subscription} will be registered to.
     * @param eventClass the class of the event that this subscription is subscribing to.
     * @param target the object that contains the method to be called.
     * @param method the method to be invoked when the event is posted.
     * @return a subscription based off provided data.
     */
    static <E> @NonNull Subscription<E> of(int order, @NonNull EventBus<E> bus, @NonNull Class<? extends E> eventClass, @NonNull Object target, @NonNull Method method, boolean acceptsCancelled) {
        return new SimpleSubscription<>(order, bus, eventClass, target, method, acceptsCancelled);
    }

    /**
     * Create a subscription based off an event handler.
     *
     * @param order the order in which the subscription should be handled.
     * @param bus the {@link EventBus} instance that the {@link Subscription} will be registered to.
     * @param eventClass the class of the event that this subscription is subscribing to.
     * @param handler the event handler that will be called when the event is fired.
     * @return a subscription based off provided data.
     */
    static <E> @NonNull Subscription<E> of(int order, @NonNull EventBus<E> bus, @NonNull Class<? extends E> eventClass, @NonNull EventHandler<? super E> handler, boolean acceptsCancelled) {
        return new SimpleSubscription<>(order, bus, eventClass, handler, acceptsCancelled);
    }

    /**
     * Called when this event is posted.
     *
     * @param event the event ({@link E}) to be handled.
     */
    void on(@NonNull E event) throws Throwable;

    /**
     * The order in which this will be posted
     *
     * @return the order as an integer.
     */
    default int getOrder() {
        return PostOrder.NORMAL;
    }

    /**
     * Returns if this can be cancelled.
     *
     * @return a boolean value.
     */
    default boolean acceptsCancelled() {
        return true;
    }

}
