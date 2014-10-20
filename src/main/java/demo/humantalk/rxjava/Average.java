package demo.humantalk.rxjava;

import demo.humantalk.rxjava.operators.OnSubscribeRefCountN;
import rx.Observable;
import rx.Scheduler;
import rx.Subscriber;
import rx.observables.ConnectableObservable;
import rx.plugins.RxJavaErrorHandler;
import rx.plugins.RxJavaPlugins;
import rx.schedulers.Schedulers;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

/**
 * Created by david on 16/10/14.
 */
public class Average {

    static {
        RxJavaPlugins.getInstance().registerErrorHandler(new RxJavaErrorHandler() {
            @Override
            public void handleError(Throwable e) {
                e.printStackTrace();
            }
        });
    }

    public static void main(String[] args) throws InterruptedException {
        Observable<Double> data = evilDataGenerator2();
        Observable<Double> newThread = averageOf(data, Schedulers.newThread());
        Observable<Double> computation = averageOf(data, Schedulers.computation());
        Observable<Double> io = averageOf(data, Schedulers.io());

        Observable<Double> newThread2 = averageOf2(data, Schedulers.newThread());
        Observable<Double> computation2 = averageOf2(data, Schedulers.computation());
        Observable<Double> io2 = averageOf2(data, Schedulers.io());

        Observable<Double> all = newThread.concatWith(computation)
                .concatWith(io)
                .concatWith(newThread2)
                .concatWith(computation2)
                .concatWith(io2);

        Observable<String> allNames = Observable.from(
                Arrays.asList("newThread", "computation", "io"));

        allNames = allNames.concatWith(allNames);

        allNames.zipWith(all, Result::new).toBlocking().forEach(Average::displayResult);

    }

    private static void displayResult(Result result) {
        System.out.println("----------------------------------------------------");
        System.out.println(String.format("[%s]\t\t\t Avg \t -> \t%f", result.getName(), result.getResult()));
        System.out.println("----------------------------------------------------");
    }

    private static Observable<Double> averageOf(Observable<Double> data, Scheduler scheduler) {

        Observable<Double> bridge = data
                .share()
                        // the secret is...here !
                        // without it, stream is synchronized.
                        // so the zip first subscribe on the first observer
                        // as, it's synchronized, it consume it then subscribe
                        // on the second. As it's the same hot observable
                        // the stream is empty, and you're owned !
                        // -------------------------------------------
                        // The problem is that it will emit events as soon as
                        // the first observer is subscribe (here sum)
                        // so the second observer can miss events that are emitted
                        // between the first observer subscription, and the second observer subscription
                        // -------------------------------------------
                        // use Schedulers.computation() or Schedulers.newThread() to see the problem
                .subscribeOn(scheduler);


        Observable<Double> count = bridge.count().map(Integer::doubleValue);

        Observable<Double> sum = bridge.reduce(0.0, (seed, e) -> seed + e);

        return Observable.zip(sum, count, (s, c) -> s / c);
    }


    private static Observable<Double> averageOf2(Observable<Double> data, Scheduler scheduler) {

        ConnectableObservable<? extends Double> hot = data
                .publish();

        Observable<Double> bridge = Observable.create(new OnSubscribeRefCountN<>(hot, 2))
                // the secret is...here !
                .subscribeOn(scheduler);

        Observable<Double> count = bridge.count()
                .map(Integer::doubleValue);

        Observable<Double> sum = bridge.reduce(0.0, (seed, e) -> seed + e);

        return Observable.zip(sum, count, (s, c) -> s / c);
    }

    private static Observable<Double> evilDataGenerator() {
        return Observable.interval(1, TimeUnit.MICROSECONDS)
                .map(Long::doubleValue)
                        // emit items during 3 seconds
                .lift(new OperatorThrottle<>(3, TimeUnit.SECONDS));
    }

    private static Observable<Double> evilDataGenerator2() {
        return Observable.range(1, 10000).map(Integer::doubleValue);
    }

    private static Observable<Double> dataGenerator() {
        return Observable.create(new Observable.OnSubscribe<Double>() {
            @Override
            public void call(Subscriber<? super Double> subscriber) {
                System.out.println("SUBSCRIBE");
                subscriber.onNext(10.0);
                subscriber.onNext(15.0);
                subscriber.onNext(20.0);
                subscriber.onCompleted();
            }
        });
    }


    private static class OperatorThrottle<T> implements Observable.Operator<T, T> {
        private final long time;
        private final TimeUnit unit;

        private final Scheduler scheduler;

        public OperatorThrottle(final long time, final TimeUnit unit) {
            this(time, unit, Schedulers.computation());
        }


        public OperatorThrottle(final long time, final TimeUnit unit, Scheduler scheduler) {
            this.time = time;
            this.unit = unit;
            this.scheduler = scheduler;
        }

        @Override
        public Subscriber<? super T> call(final Subscriber<? super T> subscriber) {
            return new Subscriber<T>(subscriber) {

                private volatile boolean alreadyCompleted = false;

                @Override
                public void onStart() {
                    scheduler.createWorker().schedule(() -> {
                        if (!alreadyCompleted) {
                            alreadyCompleted = true;
                            subscriber.onCompleted();
                        }
                    }, time, unit);
                }

                @Override
                public void onCompleted() {
                    if (!alreadyCompleted) {
                        alreadyCompleted = true;
                        subscriber.onCompleted();
                    }
                }

                @Override
                public void onError(final Throwable e) {
                    subscriber.onError(e);
                }

                @Override
                public void onNext(final T t) {
                    subscriber.onNext(t);
                }
            };
        }
    }

    private static class Result {
        private final String name;
        private final Double result;


        private Result(final String name, final Double result) {
            this.name = name;
            this.result = result;
        }

        public String getName() {
            return name;
        }

        public Double getResult() {
            return result;
        }
    }
}
