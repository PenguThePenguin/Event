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

package io.github.penguthepenguin.event;

import io.github.penguthepenguin.event.data.Subscribe;
import io.github.penguthepenguin.event.generic.AbstractCancellable;
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

    @Test
    void testMethodSubscriber() {
        EventBus<TestEvent> bus = EventBus.of(TestEvent.class);
        assertFalse(bus.isSubscribed(TestEvent.class));

        bus.register(new TestSubscriber());
        assertTrue(bus.isSubscribed(TestEvent.class));

        TestEvent testEvent = new TestEvent();
        bus.post(testEvent).thenAccept(result -> assertTrue(result.wasSuccessful()));
        assertEquals(2, testEvent.count);

        bus.register(TestStaticSubscriber.class);
        bus.post(testEvent).thenAccept(result -> assertTrue(result.wasSuccessful()));
        assertEquals(4, testEvent.count);
    }

    @Test
    void testCancellableEvents() {
        EventBus<TestEvent> bus = EventBus.of(TestEvent.class);
        assertNull(bus.getSubscriptions(TestEvent.class));

        bus.register(TestEvent.class, (Subscription<TestEvent>) event -> event.count++);
        assertTrue(bus.isSubscribed(TestEvent.class));

        TestEvent testEvent = new TestEvent();
        bus.post(testEvent).thenAccept(result -> assertTrue(result.wasSuccessful()));
        assertEquals(1, testEvent.count);

        testEvent.setCancelled(true);

        bus.post(testEvent).thenAccept(result -> assertTrue(result.wasSuccessful()));
        assertEquals(1, testEvent.count);
    }

    public static class TestEvent extends AbstractCancellable {

        public int count;

        @Override
        public @NonNull AtomicBoolean getCancellationState() {
            return cancellationState;
        }

    }

    public static class TestSubscriber {

        @Subscribe(order = 1)
        private void onTestEvent(TestEvent event) {
            event.count++;
        }

        @Subscribe(order = 2, ignoreCancelled = true)
        private void onTestEventNonCancellable(TestEvent event) {
            event.count++;
        }

    }

    public static class TestStaticSubscriber {

        @Subscribe
        private static void onTestStatic(TestEvent event) {
            event.count++;
        }

    }

}
