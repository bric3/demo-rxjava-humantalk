package demo.humantalk.rxjava;

import rx.Observable;
import rx.Subscriber;
import rx.plugins.RxJavaErrorHandler;
import rx.plugins.RxJavaPlugins;
import rx.schedulers.Schedulers;

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
        Observable<Integer> data = dataGenerator();
        Observable<Double> avg = averageOf(data);

        System.out.println("wait...");

        // Observable.timer(3, TimeUnit.SECONDS, Schedulers.immediate()).subscribe();

        avg.subscribe((result) -> System.out.println(String.format("Avg \t -> \t %f", result)));

        Observable.timer(5, TimeUnit.SECONDS, Schedulers.immediate()).subscribe();
    }

    private static Observable<Double> averageOf(Observable<Integer> data) {

        Observable<Integer> bridge = data
                .doOnNext((e) -> System.out.println(String.format("value \t -> \t%d", e)))
                .share()
                        // the secret is...here !
                .subscribeOn(Schedulers.computation());

        Observable<Double> count = bridge.count()
                .doOnNext((e) -> System.out.println("thread count \t" + Thread.currentThread().getName()))
                .doOnNext((e) -> System.out.println("count " + e))
                .map(Integer::doubleValue);

        Observable<Double> sum = bridge.reduce(0, (seed, e) -> seed + e)
                .doOnNext((e) -> System.out.println("thread sum \t" + Thread.currentThread().getName()))
                .doOnNext((e) -> System.out.println("reduce " + e))
                .map(Integer::doubleValue);

        return Observable.zip(sum, count, (s, c) -> s / c);
    }

    private static Observable<Integer> evilDataGenerator() {
        Observable<Long> interval = Observable.interval(10, TimeUnit.MILLISECONDS);
        return interval.window(1, TimeUnit.SECONDS)
                .take(1)
                .flatMap((obs) -> obs)
                .doOnEach(System.err::println)
                .map(Long::intValue);
    }

    private static Observable<Integer> dataGenerator() {
        return Observable.create(new Observable.OnSubscribe<Integer>() {
            @Override
            public void call(Subscriber<? super Integer> subscriber) {
                System.out.println("SUBSCRIBE");
                subscriber.onNext(10);
                subscriber.onNext(15);
                subscriber.onNext(20);
                subscriber.onCompleted();
            }
        });
    }
}
