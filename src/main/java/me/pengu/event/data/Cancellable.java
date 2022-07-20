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

package me.pengu.event.data;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.concurrent.atomic.AtomicBoolean;

public interface Cancellable {

    /**
     * Returns the events current cancellation state
     *
     * @return an atomic boolean to maintain concurrency.
     */
    @NonNull AtomicBoolean getCancellationState();

    /**
     * Returns if the event was cancelled
     *
     * @return {@code true} if the event was cancelled, {@code false} otherwise.
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    default boolean isCancelled() {
        return this.getCancellationState().get();
    }

   /**
     * Returns if the event is not cancelled.
     *
     * @return {@code true} if the event wasn't cancelled, {@code false} otherwise.
     */
     default boolean isNotCancelled() {
        return !this.isCancelled();
    }

    /**
     * Sets the cancellation state
     *
     * @param cancelled The new cancellation state.
     * @return The previous value of the cancellation state.
     */
    default boolean setCancelled(boolean cancelled) {
        return this.getCancellationState().getAndSet(cancelled);
    }

}
