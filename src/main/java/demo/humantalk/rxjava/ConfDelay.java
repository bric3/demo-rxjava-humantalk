package demo.humantalk.rxjava;

import rx.Observable;
import rx.Scheduler;
import rx.Subscriber;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

import java.util.concurrent.TimeUnit;

/**
 * Created by david.wursteisen on 24/10/2014.
 */
public class ConfDelay {

    public static void main(String[] args) {
        Observable.range(1, 1000).lift(new ConfigurableDelay(ConfDelay::delayPerItem, Schedulers.immediate()))
                .map((i) -> "|")
                .subscribe(System.out::print);
    }


    public static TimeConf delayPerItem(Object item) {
        long value = ((Integer) item).longValue();
        return new TimeConf(value * value, TimeUnit.MILLISECONDS);
    }

    private static class TimeConf {
        private final long time;
        private final TimeUnit unit;

        private TimeConf(final long time, final TimeUnit unit) {
            this.time = time;
            this.unit = unit;
        }
    }

    private static class ConfigurableDelay<T> implements Observable.Operator<T, T> {
        private final Func1<T, TimeConf> itemToTime;
        private final Scheduler scheduler;

        public ConfigurableDelay(final Func1<T, TimeConf> itemToTime) {
            this(itemToTime, Schedulers.computation());
        }

        public ConfigurableDelay(final Func1<T, TimeConf> itemToTime, final Scheduler scheulder) {
            this.itemToTime = itemToTime;
            this.scheduler = scheulder;
        }

        @Override
        public Subscriber<? super T> call(final Subscriber<? super T> subscriber) {
            return new Subscriber<T>(subscriber) {

                private TimeConf nextTime = null;

                @Override
                public void onCompleted() {
                    subscriber.onCompleted();
                }

                @Override
                public void onError(final Throwable e) {
                    subscriber.onError(e);
                }

                @Override
                public void onNext(final T t) {
                    TimeConf previousNextTime = nextTime;
                    this.nextTime = itemToTime.call(t);
                    if (previousNextTime == null) {
                        subscriber.onNext(t);
                    } else {
                        scheduler.createWorker().schedule(() -> subscriber.onNext(t), previousNextTime.time, previousNextTime.unit);
                    }
                }
            };
        }
    }
}
