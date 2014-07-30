package demo.humantalk.rxjava;

import rx.Observable;
import rx.Subscriber;
import rx.functions.Func1;

public class TransformingObservableOperator<R, T> implements Observable.Operator<R, T> {

    private final Func1<T, Observable<R>> transformingObservable;

    public TransformingObservableOperator(Func1<T, Observable<R>> transformingObservable) {
        this.transformingObservable = transformingObservable;
    }

    @Override
    public Subscriber<? super T> call(Subscriber<? super R> subscriber) {
        return new Subscriber<T>() {
            @Override
            public void onCompleted() {
                if (!subscriber.isUnsubscribed()) {
                    subscriber.onCompleted();
                }
            }

            @Override
            public void onError(Throwable e) {
                if (!subscriber.isUnsubscribed()) {
                    subscriber.onError(e);
                }
            }

            @Override
            public void onNext(T s) {
                if (!subscriber.isUnsubscribed()) {
                    transformingObservable.call(s).subscribe(subscriber::onNext, subscriber::onError);
                }
            }
        };
    }
}
