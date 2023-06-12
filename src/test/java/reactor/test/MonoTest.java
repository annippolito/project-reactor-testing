package reactor.test;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.reactivestreams.Subscription;
import reactor.core.publisher.Mono;

@Slf4j
/**
 *
 * Reactive Streams
 * 1. Asynchronous
 * 2. Non-blocking
 * 3. Backpressure
 *
 * Publisher <- (subscribe) Subscriber
 * Subscription is created
 * Publisher (onSubscriber with the subscription) -> subscriber
 * Subscription <- (request N) Subscriber
 * Publisher -> (onNext) Subscriber
 * until:
 * 1. Subscriber sends all the objects requested.
 * 2. Publisher sends all the onjects it has. (onCompleted) subscriber and subscription will be canceled
 * 3. There is an error. (onError) -> subscriber and subscription will be canceled
 *
 */
class MonoTest {

  @Test
  void monoSubscriber() {
    String name = "Ciro";
    var mono = Mono.just(name)
            .log();
    mono.subscribe();

    StepVerifier.create(mono)
        .expectNext(name)
        .verifyComplete();

    log.info("Name {}", mono);
    log.info("Everything working as expected");
  }

  @Test
  void monoSubscriberConsumer() {
    String name = "Ciro";
    var mono = Mono.just(name) //publisher
        .log();
    mono.subscribe(s -> log.info("Value {}", s));

    StepVerifier.create(mono)
        .expectNext(name)
        .verifyComplete();
  }

  @Test
  void monoSubscriberConsumerError() {
    String name = "Ciro";
    var mono = Mono.just(name)
        .map(s -> {throw new RuntimeException("Testing mono with error");});

    mono.subscribe(
        s -> log.info("Value {}", s),
        e -> log.error("Something bad happened", e)); //handle the error

    StepVerifier.create(mono)
        .expectError(RuntimeException.class)
        .verify();
  }

  @Test
  void monoSubscriberConsumerComplete() {
    String name = "Ciro";
    var mono = Mono.just(name)
        .log()
        .map(String::toUpperCase);

    mono.subscribe(
        s -> log.info("Value {}", s),
        Throwable::printStackTrace,
        () -> log.info("FINISHED!")); //handle the error

    StepVerifier.create(mono)
        .expectNext(name.toUpperCase())
        .verifyComplete();
  }

  @Test
  void monoSubscriberConsumerSubscription() {
    String name = "Ciro";
    var mono = Mono.just(name)
        .log()
        .map(String::toUpperCase);

    mono.subscribe(
        s -> log.info("Value {}", s),
        Throwable::printStackTrace,
        () -> log.info("FINISHED!"),
        Subscription::cancel); //clean resources when disposed

    StepVerifier.create(mono)
        .expectNext(name.toUpperCase())
        .verifyComplete();
  }

  @Test
  void monoDoOnMethods() {
    String name = "Ciro";
    var mono = Mono.just(name)
        .log()
        .map(String::toUpperCase)
        .doOnSubscribe(subscription -> log.info("Subscribed"))
        .doOnRequest(s -> log.info("Request received"))
        .doOnNext(s -> log.info("Value is here. Executing doOnNext {}", s))
        .doOnSuccess(s -> log.info("doOnSuccess executed"));

    mono.subscribe(
        s -> log.info("Value {}", s),
        Throwable::printStackTrace,
        () -> log.info("FINISHED!"),
        Subscription::cancel); //clean resources when disposed

    StepVerifier.create(mono)
        .expectNext(name.toUpperCase())
        .verifyComplete();
  }

}
