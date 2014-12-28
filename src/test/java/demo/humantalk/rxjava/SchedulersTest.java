package demo.humantalk.rxjava;

import org.junit.Test;
import rx.Observable;
import rx.schedulers.Schedulers;

public class SchedulersTest {

    //    Main TID: 1
    //    gen  : TID: 1:main, item: 1
    //    subs : TID: 1:main, item: 1
    //    gen  : TID: 1:main, item: 2
    //    subs : TID: 1:main, item: 2
    //    gen  : TID: 1:main, item: 3
    //    subs : TID: 1:main, item: 3
    //    gen  : TID: 1:main, item: 4
    //    subs : TID: 1:main, item: 4
    //    gen  : TID: 1:main, item: 5
    //    subs : TID: 1:main, item: 5
    @Test
    public void no_async() throws Exception {
        System.out.println("Main TID: " + Thread.currentThread().getId());

        Observable.range(1, 5).doOnNext(item -> debug("gen ", item))
                .subscribe(x -> debug("subs", x))
        ;
    }

    //    Main TID: 1
    //    gen  : TID: 12:RxCachedThreadScheduler-1, item: 1
    //    mult : TID: 12:RxCachedThreadScheduler-1, item: 10
    //    subs : TID: 12:RxCachedThreadScheduler-1, item: 10
    //    gen  : TID: 12:RxCachedThreadScheduler-1, item: 2
    //    mult : TID: 12:RxCachedThreadScheduler-1, item: 20
    //    subs : TID: 12:RxCachedThreadScheduler-1, item: 20
    //    gen  : TID: 12:RxCachedThreadScheduler-1, item: 3
    //    mult : TID: 12:RxCachedThreadScheduler-1, item: 30
    //    subs : TID: 12:RxCachedThreadScheduler-1, item: 30
    //    gen  : TID: 12:RxCachedThreadScheduler-1, item: 4
    //    mult : TID: 12:RxCachedThreadScheduler-1, item: 40
    //    subs : TID: 12:RxCachedThreadScheduler-1, item: 40
    //    gen  : TID: 12:RxCachedThreadScheduler-1, item: 5
    //    mult : TID: 12:RxCachedThreadScheduler-1, item: 50
    //    subs : TID: 12:RxCachedThreadScheduler-1, item: 50
    @Test
    public void async_using_subscribeOn() throws Exception {
        System.out.println("Main TID: " + Thread.currentThread().getId());
        Observable.range(1, 10).doOnNext(item -> debug("gen ", item))
                .subscribeOn(Schedulers.io())
                .map(i -> i * 10).doOnNext(item -> debug("mult", item))
                .subscribe(x -> debug("subs", x))
        ;

        Thread.sleep(1000);
    }

    //    Main TID: 1
    //    gen  : TID: 1:main, item: 1
    //    gen  : TID: 1:main, item: 2
    //    gen  : TID: 1:main, item: 3
    //    gen  : TID: 1:main, item: 4
    //    gen  : TID: 1:main, item: 5
    //    gen  : TID: 1:main, item: 6
    //    gen  : TID: 1:main, item: 7
    //    mult : TID: 14:RxCachedThreadScheduler-1, item: 10
    //    gen  : TID: 1:main, item: 8
    //    subs : TID: 14:RxCachedThreadScheduler-1, item: 10
    //    gen  : TID: 1:main, item: 9
    //    mult : TID: 14:RxCachedThreadScheduler-1, item: 20
    //    gen  : TID: 1:main, item: 10
    //    subs : TID: 14:RxCachedThreadScheduler-1, item: 20
    //    mult : TID: 14:RxCachedThreadScheduler-1, item: 30
    //    subs : TID: 14:RxCachedThreadScheduler-1, item: 30
    //    mult : TID: 14:RxCachedThreadScheduler-1, item: 40
    //    subs : TID: 14:RxCachedThreadScheduler-1, item: 40
    //    mult : TID: 14:RxCachedThreadScheduler-1, item: 50
    //    subs : TID: 14:RxCachedThreadScheduler-1, item: 50
    //    mult : TID: 14:RxCachedThreadScheduler-1, item: 60
    //    subs : TID: 14:RxCachedThreadScheduler-1, item: 60
    //    mult : TID: 14:RxCachedThreadScheduler-1, item: 70
    //    subs : TID: 14:RxCachedThreadScheduler-1, item: 70
    //    mult : TID: 14:RxCachedThreadScheduler-1, item: 80
    //    subs : TID: 14:RxCachedThreadScheduler-1, item: 80
    //    mult : TID: 14:RxCachedThreadScheduler-1, item: 90
    //    subs : TID: 14:RxCachedThreadScheduler-1, item: 90
    //    mult : TID: 14:RxCachedThreadScheduler-1, item: 100
    //    subs : TID: 14:RxCachedThreadScheduler-1, item: 100
    @Test
    public void async_using_observeOn() throws Exception {
        System.out.println("Main TID: " + Thread.currentThread().getId());
        Observable.range(1, 10).doOnNext(item -> debug("gen ", item))
                .observeOn(Schedulers.io())
                .map(i -> i * 10).doOnNext(item -> debug("mult", item))
                .subscribe(x -> debug("subs", x))
        ;

        Thread.sleep(1000);
    }

    //    Main TID: 1
    //    gen  : TID: 1:main, item: 1
    //    gen  : TID: 1:main, item: 2
    //    gen  : TID: 1:main, item: 3
    //    gen  : TID: 1:main, item: 4
    //    gen  : TID: 1:main, item: 5
    //    gen  : TID: 1:main, item: 6
    //    mult : TID: 15:RxCachedThreadScheduler-1, item: 10
    //    gen  : TID: 1:main, item: 7
    //    addc : TID: 13:RxNewThreadScheduler-1, item: 10a
    //    subs : TID: 13:RxNewThreadScheduler-1, item: 10a
    //    mult : TID: 15:RxCachedThreadScheduler-1, item: 20
    //    gen  : TID: 1:main, item: 8
    //    addc : TID: 13:RxNewThreadScheduler-1, item: 20a
    //    mult : TID: 15:RxCachedThreadScheduler-1,item:30
    //    subs : TID: 13:RxNewThreadScheduler-1, item: 20a
    //    gen  : TID: 1:main, item:9
    //    addc : TID: 13:RxNewThreadScheduler-1, item: 30a
    //    mult : TID: 15:RxCachedThreadScheduler-1, item: 40
    //    subs : TID: 13:RxNewThreadScheduler-1, item: 30a
    //    gen  : TID: 1:main, item: 10
    //    addc : TID: 13:RxNewThreadScheduler-1, item: 40a
    //    mult : TID: 15:RxCachedThreadScheduler-1, item: 50
    //    subs : TID: 13:RxNewThreadScheduler-1, item: 40a
    //    mult : TID: 15:RxCachedThreadScheduler-1, item: 60
    //    addc : TID: 13:RxNewThreadScheduler-1, item: 50a
    //    mult : TID: 15:RxCachedThreadScheduler-1, item: 70
    //    subs : TID: 13:RxNewThreadScheduler-1, item: 50a
    //    mult : TID: 15:RxCachedThreadScheduler-1, item: 80
    //    addc : TID: 13:RxNewThreadScheduler-1, item: 60a
    //    mult : TID: 15:RxCachedThreadScheduler-1, item: 90
    //    subs : TID: 13:RxNewThreadScheduler-1, item: 60a
    //    mult : TID: 15:RxCachedThreadScheduler-1, item: 100
    //    addc : TID: 13:RxNewThreadScheduler-1, item: 70a
    //    subs : TID: 13:RxNewThreadScheduler-1, item: 70a
    //    addc : TID: 13:RxNewThreadScheduler-1, item: 80a
    //    subs : TID: 13:RxNewThreadScheduler-1, item: 80a
    //    addc : TID: 13:RxNewThreadScheduler-1, item: 90a
    //    subs : TID: 13:RxNewThreadScheduler-1, item: 90a
    //    addc : TID: 13:RxNewThreadScheduler-1, item: 100a
    //    subs : TID: 13:RxNewThreadScheduler-1, item: 100a
    @Test
    public void async_using_two_observeOn() throws Exception {
        System.out.println("Main TID: " + Thread.currentThread().getId());
        Observable.range(1, 10).doOnNext(item -> debug("gen ", item))
                .observeOn(Schedulers.io())
                .map(i -> i * 10).doOnNext(item -> debug("mult", item))
                .observeOn(Schedulers.newThread())
                .map(i -> i + "a").doOnNext(item -> debug("addc", item))
                .subscribe(x -> debug("subs", x))
        ;

        Thread.sleep(1000);
    }

    //    Main TID: 1
    //    gen  : TID: 15:RxCachedThreadScheduler-1, item: 1
    //    mult : TID: 15:RxCachedThreadScheduler-1, item: 10
    //    gen  : TID: 15:RxCachedThreadScheduler-1, item: 2
    //    mult : TID: 15:RxCachedThreadScheduler-1, item: 20
    //    gen  : TID: 15:RxCachedThreadScheduler-1, item: 3
    //    mult : TID: 15:RxCachedThreadScheduler-1, item:30
    //    addc : TID: 13:RxNewThreadScheduler-1, item:10a
    //    subs : TID: 13:RxNewThreadScheduler-1, item:10a
    //    gen  : TID:15:RxCachedThreadScheduler-1, item: 4
    //    mult : TID: 15:RxCachedThreadScheduler-1, item: 40
    //    addc : TID: 13:RxNewThreadScheduler-1, item: 20a
    //    gen  : TID: 15:RxCachedThreadScheduler-1, item: 5
    //    subs : TID: 13:RxNewThreadScheduler-1, item: 20a
    //    mult : TID: 15:RxCachedThreadScheduler-1, item: 50
    //    addc : TID: 13:RxNewThreadScheduler-1, item: 30a
    //    subs : TID: 13:RxNewThreadScheduler-1, item: 30a
    //    addc : TID: 13:RxNewThreadScheduler-1, item: 40a
    //    subs : TID: 13:RxNewThreadScheduler-1, item: 40a
    //    addc : TID: 13:RxNewThreadScheduler-1, item: 50a
    //    subs : TID: 13:RxNewThreadScheduler-1, item: 50a
    @Test
    public void async_using_both_subscribeOn_and_observeOn() throws Exception {
        System.out.println("Main TID: " + Thread.currentThread().getId());
        Observable.range(1, 5).doOnNext(item -> debug("gen ", item))
                .subscribeOn(Schedulers.io())
                .map(i -> i * 10).doOnNext(item -> debug("mult", item))
                .observeOn(Schedulers.newThread())
                .map(i -> i + "a").doOnNext(item -> debug("addc", item))
                .subscribe(x -> debug("subs", x))
        ;

        Thread.sleep(1000);
    }

    private static <I> void debug(String source, I item) {
        System.out.format("%-5s : TID=️ %3d:%-30s ➡️ item: %4s%n", source, Thread.currentThread().getId(), Thread.currentThread().getName(), item);
    }


}
