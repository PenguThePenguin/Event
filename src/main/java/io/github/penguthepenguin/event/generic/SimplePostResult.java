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

import lombok.Getter;
import io.github.penguthepenguin.event.PostResult;
import io.github.penguthepenguin.event.Subscription;

import java.util.Map;

@Getter
public class SimplePostResult<E> implements PostResult<E> {

    public static final PostResult<?> FAIL = new SimplePostResult<>(null, null);

    private final E event;
    private final Map<Subscription<? super E>, Throwable> exceptions;

    public SimplePostResult(E event, Map<Subscription<? super E>, Throwable> exceptions) {
        this.event = event;
        this.exceptions = exceptions;
    }

}
