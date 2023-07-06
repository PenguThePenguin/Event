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

package io.github.penguthepenguin.event.data;

import io.github.penguthepenguin.event.Subscription;
import org.checkerframework.checker.nullness.qual.NonNull;

public interface Acceptor<E> {

    /**
     * Only cancels the event if it is accepting cancelled.
     *
     * @return boolean of whether the event should be handled.
     */
    static <E> @NonNull Acceptor<E> nonCancelingWhenNotAcceptingCancelled() {
        return (eventType, event, subscription) -> !subscription.acceptsCancelled()
                || !(event instanceof Cancellable)
                || !((Cancellable) event).isCancelled();
    }

    /**
     * Returns if the event should be handled by its registered subscription.
     *
     * @param eventType the type of event that the subscription is interested in.
     * @param event the event that was published.
     * @param subscription the subscription that is being checked.
     * @return boolean of whether the event should be handled.
     */
    boolean accepts(@NonNull Class<E> eventType, @NonNull E event, @NonNull Subscription<? super E> subscription);

}
