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

import me.pengu.event.data.Cancellable;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

class EventBusTest {

    @Test
    void testRegisterAndUnregisterEvent() {
        EventBus<TestEvent> bus = EventBus.of(TestEvent.class);
        assertNull(bus.getSubscriptions(TestEvent.class));

        Subscription<TestEvent> subscription = bus.register(TestEvent.class, (EventHandler<TestEvent>) event -> event.count++);
        assertTrue(bus.isSubscribed(TestEvent.class));

        TestEvent testEvent = new TestEvent();
        bus.post(testEvent).thenAccept(result -> assertTrue(result.wasSuccessful()));
        assertEquals(1, testEvent.count);

        bus.unregister(subscription);
        assertFalse(bus.isSubscribed(TestEvent.class));

        bus.post(testEvent).thenAccept(result -> assertTrue(result.wasSuccessful()));
        assertEquals(1, testEvent.count);
    }

    void testCancellableEvents() {
        EventBus<TestEvent> bus = EventBus.of(TestEvent.class);
        assertEquals(0, bus.getSubscriptions(TestEvent.class).length);

        bus.register(TestEvent.class, (Subscription<TestEvent>) event -> event.count++);
        assertTrue(bus.isSubscribed(TestEvent.class));

        TestEvent testEvent = new TestEvent();
        bus.post(testEvent).thenAccept(result -> assertTrue(result.wasSuccessful()));
        assertEquals(1, testEvent.count);

        testEvent.setCancelled(true);

        bus.post(testEvent).thenAccept(result -> assertTrue(result.wasSuccessful()));
        assertEquals(1, testEvent.count);
    }

    public static class TestEvent implements Cancellable {

        public int count;
        private final AtomicBoolean cancellationState = new AtomicBoolean(false);

        @Override
        public @NonNull AtomicBoolean getCancellationState() {
            return cancellationState;
        }

    }


}
