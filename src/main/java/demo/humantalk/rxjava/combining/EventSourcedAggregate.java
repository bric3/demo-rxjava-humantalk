package demo.humantalk.rxjava.combining;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import rx.Observable;
import rx.observers.Subscribers;

import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.TimeUnit;

public class EventSourcedAggregate {

    private final Observable<Event> events;
    ConcurrentSkipListSet<Integer> set = new ConcurrentSkipListSet<>();

    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    private static class Event {
        public final String details;

        public static Event change(Integer tick) {
            return new Event("change : " + tick);
        }

        public static Event snapshot(Set<Integer> set) {
            return new Event("snapshot : " + set.size());
        }
    }

    public EventSourcedAggregate() {
//        set.addAll(IntStream.range(0, 1000).boxed().collect(Collectors.toSet()));
        events = Observable.range(0, 10_000_000)
                .doOnNext(set::add)
                .map(Event::change)
                .publish()
                .refCount();
        events.subscribe(Subscribers.empty());
    }

    public Observable<Event> observeChanges() {
        return events.startWith(Observable.just(Event.snapshot(set)));
    }

    public static void main(String[] args) {
        EventSourcedAggregate eventSourcedAggreate = new EventSourcedAggregate();

        Observable.timer(2, 4, TimeUnit.SECONDS)
                .limit(2)
                .flatMap(t -> eventSourcedAggreate.observeChanges())
                .subscribe(System.out::println);

    }

}
