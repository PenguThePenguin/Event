package me.pengu.event.generic;

import me.pengu.event.data.Cancellable;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.concurrent.atomic.AtomicBoolean;

public abstract class AbstractCancellable implements Cancellable {

    protected final AtomicBoolean cancellationState = new AtomicBoolean(false);

    @Override
    public @NonNull AtomicBoolean getCancellationState() {
        return this.cancellationState;
    }

}
