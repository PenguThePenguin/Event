package io.github.penguthepenguin.event.generic;

import io.github.penguthepenguin.event.data.Cancellable;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.concurrent.atomic.AtomicBoolean;

public abstract class AbstractCancellable implements Cancellable {

    protected final AtomicBoolean cancellationState = new AtomicBoolean(false);

    /**
     * Returns the events current cancellation state
     *
     * @return an atomic boolean of it to maintain concurrency.
     */
    @Override
    public @NonNull AtomicBoolean getCancellationState() {
        return this.cancellationState;
    }

}
