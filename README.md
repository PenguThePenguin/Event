# Event
Event is a publishing / subscribing event api based off the [publish-subscribe pattern](https://en.wikipedia.org/wiki/Publish%E2%80%93subscribe_pattern).

## Why event?

Event is fully optimized for any implementation, with support for events to be fired on different threads.

## Examples

### Setting up a bus
```java
EventBus<ExampleEvent> bus = EventBus.of(ExampleEvent.class);
```


### Create an event

This is a base event you can extend for different actions
###### You can also intercept event posting by implementing Cancellable or using a custom bus Acceptor

```java
public static class ExampleEvent {
    
}
```

### Register a handler

```java
// Using java 8's lambdas
bus.register(ExampleEvent.class, (EventHandler<ExampleEvent>) event -> {
    System.out.println("Hey!");
});

// Using regular java
bus.register(ExampleEvent.class, new EventHandler<ExampleEvent>() {
    @Override
    public void handle(ExampleEvent event) throws Throwable {
        System.out.println("Hey!");
    }
});
```

### Create a subscriber

You can also make a subscriber from a class with methods annotated with @Subscribe 

```java
bus.register(new ExampleSubscriber());

public static class ExampleSubscriber {

	@Subscribe(order = 1)
	private void onExampleEvent(ExampleEvent event) {
		System.out.println("Hey!");
	}
}
```

