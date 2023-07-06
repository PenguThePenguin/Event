package me.pengu.event.gen;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Collection;
import java.util.concurrent.ExecutionException;

public class EventGeneratorCache<G extends GeneratedEventData<I>, I> {

    private final I instance;
    private final Class<I> instanceType;

    private final LoadingCache<Class<? extends G>, GeneratedEvent<? extends G, I>> cache;

    public EventGeneratorCache(I instance) {
        this.instance = instance;
        this.instanceType = (Class<I>) instance.getClass();

        this.cache = CacheBuilder.newBuilder().build(new CacheLoader<Class<? extends G>, GeneratedEvent<? extends G, I>>() {
            @Override
            public GeneratedEvent<? extends G, I> load(Class<? extends G> eventClass) {
                try {
                    return new GeneratedEvent<>(eventClass, instanceType);
                } catch (Throwable e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    public void preGenerate(@NonNull Collection<Class<? extends G>> events) {
        for (Class<? extends G> eventType : events) {
            generate(eventType);
        }
    }

    public GeneratedEvent<? extends G, I> generate(Class<? extends G> event) {
        try {
            return this.cache.get(event);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

}
