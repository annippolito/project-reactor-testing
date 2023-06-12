package reactor.test;

import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.Flux;

@Slf4j
class FluxTest {

  @Test
  void fluxSubscriber() {
    var flux = Flux.just("Ciro", "Marta", "Giovanni", "Alice")
        .log();

    StepVerifier.create(flux)
        .expectNext("Ciro", "Marta", "Giovanni", "Alice")
        .verifyComplete();
  }

  @Test
  void fluxSubscriberNumbers() {
    var flux = Flux.range(1, 5)
        .log();

    flux.subscribe(i -> log.info("Subscriber1 num {}", i));
    StepVerifier.create(flux)
        .expectNext(1,2,3,4,5)
        .verifyComplete();
  }

  @Test
  void fluxSubscriberFromList() {
    var flux = Flux.fromIterable(List.of(1,2,3,4,5))
        .log();

    flux.subscribe(i -> log.info("Subscriber1 num {}", i));

    StepVerifier.create(flux)
        .expectNext(1,2,3,4,5)
        .verifyComplete();
  }

  @Test
  void fluxSubscriberNumbersError() {
    var flux = Flux.range(1, 5)
        .log()
            .map(i -> {
              if(i==4){
                throw new IndexOutOfBoundsException("index error");
              }
              return i;
            });

    flux.subscribe(i -> log.info("Subscriber1 num {}", i), Throwable::printStackTrace, () -> log.info("DONE!"));

    StepVerifier.create(flux)
        .expectNext(1,2,3)
        .expectError(IndexOutOfBoundsException.class)
        .verify();
  }

  @Test
  void fluxSubscriberNumbersErrorDoesntHappenWhenSubscriptionStopBefore() {
    var flux = Flux.range(1, 5)
        .log()
        .map(i -> {
          if(i==4){
            throw new IndexOutOfBoundsException("index error");
          }
          return i;
        });

    flux.subscribe(i -> log.info("Subscriber1 num {}", i), Throwable::printStackTrace, () -> log.info("DONE!"),
        subscription -> subscription.request(3));

    StepVerifier.create(flux)
        .expectNext(1,2,3)
        .expectError(IndexOutOfBoundsException.class)
        .verify();
  }

  @Test
  void fluxSubscriberNumbersUglyBackPressure() {
    var flux = Flux.range(1, 10)
        .log();

    flux.subscribe(new Subscriber<Integer>() {
      private int count  = 0;
      private Subscription subscription;
      private int requestCount = 2;

      @Override
      public void onSubscribe(Subscription subscription) {
        this.subscription = subscription;
        this.subscription.request(5);
      }

      @Override
      public void onNext(Integer integer) {
        count++;
        if(count >= requestCount){
          count = 0;
          subscription.request(2);
        }
      }

      @Override
      public void onError(Throwable throwable) {

      }

      @Override
      public void onComplete() {

      }
    });

    StepVerifier.create(flux)
        .expectNext(1,2,3,4,5,6,7,8,9,10)
        .verifyComplete();
  }

  @Test
  void fluxSubscriberNumbersBackPressure() {
    var flux = Flux.range(1, 10)
        .log();

    flux.subscribe(new BaseSubscriber<Integer>() {
      private int count  = 0;
      private int requestCount = 2;

      @Override
      protected void hookOnSubscribe(Subscription subscription){
        request(requestCount);
      }

      @Override
      protected void hookOnNext(Integer value) {
        count++;
        if(count >= requestCount){
          count = 0;
          request(requestCount);
        }
      }

    });

    StepVerifier.create(flux)
        .expectNext(1,2,3,4,5,6,7,8,9,10)
        .verifyComplete();
  }
}
