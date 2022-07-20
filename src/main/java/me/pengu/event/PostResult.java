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

    /**
     * Creates a post result based off no thrown exceptions.
     *
     * @param event the event that was posted.
     * @return the {@link PostResult} result.
     */
    static <E> @NonNull PostResult<E> of(@NonNull E event) {
        return new SimplePostResult<>(event, Collections.emptyMap());
    }

    /**
     * Creates a post result with thrown exceptions.
     *
     * @param event the event that was posted.
     * @param exceptions any exceptions thrown when the event was posted.
     * @return the {@link PostResult} result.
     */
    static <E> @NonNull PostResult<E> of(@NonNull E event, @NonNull Map<Subscription<? super E>, Throwable> exceptions) {
        return new SimplePostResult<>(event, exceptions);
    }

    /**
     * Gets the event that was passed when posting.
     *
     * @return The event ({@link E}) that was posted.
     */
    @NonNull E getEvent();

    /**
     * Gets a map of all the subscriptions that have failed, and the exception that caused the failure.
     *
     * @return A {@link Map} of {@link Subscription}'s and {@link Throwable}'s
     */
    @NonNull Map<Subscription<? super E>, Throwable> getExceptions();

    /**
     * Returns if this post was successful.
     *
     * @return {@code true} if there aren't any exceptions that where caught during posting.
     */
    default boolean wasSuccessful() {
        return this.getExceptions().isEmpty();
    }

    /**
     * If the post was not successful, this will throw a {@link CompositeException}
     */
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
