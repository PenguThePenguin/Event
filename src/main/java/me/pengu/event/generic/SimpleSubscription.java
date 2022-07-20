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

import lombok.Getter;
import me.pengu.event.EventBus;
import me.pengu.event.EventHandler;
import me.pengu.event.Subscription;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.lang.reflect.Method;

@Getter
public class SimpleSubscription<E> implements Subscription<E> {

    private final int order;
    private final EventBus<E> bus;
    private final Class<? extends E> eventClass;
    private final EventHandler<? super E> handler;

    private final boolean acceptsCancelled;

    public SimpleSubscription(int order, EventBus<E> bus, Class<? extends E> eventClass, Object target, Method method, boolean acceptsCancelled) {
        this(order, bus, eventClass, event -> method.invoke(target, event), acceptsCancelled);
    }

    public SimpleSubscription(int order, EventBus<E> bus, Class<? extends E> eventClass, EventHandler<? super E> handler, boolean acceptsCancelled) {
        this.order = order;
        this.bus = bus;
        this.eventClass = eventClass;
        this.handler = handler;
        this.acceptsCancelled = acceptsCancelled;
    }

    /**
     * Called when this event is posted.
     *
     * @param event the event ({@link E}) to be handled.
     */
    public void on(@NonNull E event) throws Throwable {
        try {
            this.handler.handle(event);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Returns if this can be cancelled.
     *
     * @return a boolean value.
     */
    @Override
    public boolean acceptsCancelled() {
        return this.acceptsCancelled;
    }


    /**
     * Unsubscribe from the event bus.
     */
    public void unsubscribe() {
        this.bus.unregister(this);
    }

}
