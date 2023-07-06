package me.pengu.event.gen;

import me.pengu.event.data.Index;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.ClassFileVersion;
import net.bytebuddy.description.NamedElement;
import net.bytebuddy.description.modifier.Visibility;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.dynamic.scaffold.subclass.ConstructorStrategy;
import net.bytebuddy.implementation.FieldAccessor;
import net.bytebuddy.implementation.FixedValue;
import net.bytebuddy.implementation.MethodCall;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Comparator;

import static net.bytebuddy.matcher.ElementMatchers.*;

public final class GeneratedEvent<G extends GeneratedEventData<I>, I> {

    private final Class<G> eventType;
    private final Class<I> instanceType;

    private final MethodHandle constructor;
    private final MethodHandle[] setters;

    public GeneratedEvent(Class<G> eventType, Class<I> instanceType) throws Throwable {
        this.eventType = eventType;
        this.instanceType = instanceType;

        TypeDescription eventClassType = new TypeDescription.ForLoadedType(eventType);

        String eventClassSuffix = eventType.getName().substring(eventType.getPackage().getName().length());
        String packageWithName = GeneratedEvent.class.getName();
        String generatedClassName = packageWithName.substring(0, packageWithName.lastIndexOf('.')) + eventClassSuffix;

        DynamicType.Builder<G> builder = new ByteBuddy(ClassFileVersion.JAVA_V8)
                .subclass(this.eventType, ConstructorStrategy.Default.IMITATE_SUPER_CLASS_OPENING)
                .name(generatedClassName)
                .implement(eventClassType)
                .method(isAnnotatedWith(Index.class))
                .intercept(FieldAccessor.of(NamedElement.WithRuntimeName::getInternalName))
                .method(named("getEventType").and(returns(Class.class)).and(takesArguments(0)))
                .intercept(FixedValue.value(eventClassType))
                .method(named("lookupMethodHandles").and(returns(MethodHandles.Lookup.class)).and(takesArguments(0)))
                .intercept(MethodCall.invoke(MethodHandles.class.getMethod("lookup")))
                .withToString();

        Method[] properties = Arrays.stream(eventType.getMethods())
                .filter(m -> m.isAnnotationPresent(Index.class))
                .sorted(Comparator.comparingInt(o -> o.getAnnotation(Index.class).value()))
                .toArray(Method[]::new);

        for (Method method : properties) {
            builder = builder.defineField(method.getName(), method.getReturnType(), Visibility.PRIVATE);
        }

        Class<?> generatedClass = builder.make().load(GeneratedEvent.class.getClassLoader()).getLoaded();
        this.constructor = MethodHandles.publicLookup().in(generatedClass)
                .findConstructor(generatedClass, MethodType.methodType(void.class, this.instanceType))
                .asType(MethodType.methodType(this.eventType, this.instanceType));

        // noinspection unchecked
        MethodHandles.Lookup lookup = ((G) this.constructor.invoke((Object) null)).lookupMethodHandles();

        this.setters = new MethodHandle[properties.length];
        for (int i = 0; i < properties.length; i++) {
            Method method = properties[i];

            this.setters[i] = lookup.findSetter(generatedClass, method.getName(), method.getReturnType())
                    .asType(MethodType.methodType(void.class, new Class[]{this.eventType, Object.class}));
        }

    }

    public <E> E newInstance(I instance, Object... properties) throws Throwable {
        if (properties.length != this.setters.length) {
            throw new IllegalStateException("Unexpected number of properties. Given: " + properties.length + ", expected: " + this.setters.length);
        }

        // noinspection unchecked
        E event = (E) this.constructor.invokeExact(instance);

        for (int i = 0; i < this.setters.length; i++) {
            MethodHandle setter = this.setters[i];
            Object value = properties[i];

            setter.invokeExact(event, value);
        }

        return event;
    }

}
