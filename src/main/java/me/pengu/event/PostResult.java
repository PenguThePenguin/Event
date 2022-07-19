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

import me.pengu.event.generic.SimplePostResult;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Collections;
import java.util.Map;

public interface PostResult<E> {

    static <E> @NonNull PostResult<E> of(@NonNull E event) {
        return new SimplePostResult<>(event, Collections.emptyMap());
    }

    static <E> @NonNull PostResult<E> of(@NonNull E event, @NonNull Map<Subscription<? super E>, Throwable> exceptions) {
        return new SimplePostResult<>(event, exceptions);
    }

    @NonNull E getEvent();

    @NonNull Map<Subscription<? super E>, Throwable> getExceptions();

    default boolean wasSuccessful() {
        return this.getExceptions().isEmpty();
    }

    default void raise() throws Exception {
        if (!this.wasSuccessful()) {
            throw new CompositeException(this);
        }
    }

    final class CompositeException extends Exception {

        private final PostResult<?> result;

        public CompositeException(PostResult<?> result) {
            super("Error occurred when posting to subscriptions");
            this.result = result;
        }

        public void printAllStackTraces() {
            super.printStackTrace();
            for (Throwable exception : this.result.getExceptions().values()) {
                exception.printStackTrace();
            }
        }
    }

}
