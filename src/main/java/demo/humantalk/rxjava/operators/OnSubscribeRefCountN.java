package demo.humantalk.rxjava.operators;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.functions.Action0;
import rx.observables.ConnectableObservable;
import rx.subscriptions.Subscriptions;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

public final class OnSubscribeRefCountN<T> implements Observable.OnSubscribe<T> {
    /**
     * Occupied indicator.
     */
    private static final Object OCCUPIED = new Object();
    final ConnectableObservable<? extends T> source;
    final Object guard;
    /**
     * Manipulated while in the serialized section.
     */
    final Map<Token, Object> connectionStatus;
    private final int numberOfSub;
    /**
     * Guarded by guard.
     */
    int index;
    /**
     * Guarded by guard.
     */
    boolean emitting;
    /**
     * Guarded by guard. If true, indicates a connection request, false indicates a disconnect request.
     */
    List<Token> queue;
    /**
     * Manipulated while in the serialized section.
     */
    int count;
    /**
     * Manipulated while in the serialized section.
     */
    Subscription connection;

    public OnSubscribeRefCountN(ConnectableObservable<? extends T> source, int numberOfSub) {
        this.source = source;
        this.numberOfSub = numberOfSub;
        this.guard = new Object();
        this.connectionStatus = new WeakHashMap<Token, Object>();
    }

    @Override
    public void call(Subscriber<? super T> t1) {
        int id;
        synchronized (guard) {
            id = ++index;
        }
        final Token t = new Token(id);
        t1.add(Subscriptions.create(new Action0() {
            @Override
            public void call() {
                disconnect(t);
            }
        }));
        source.unsafeSubscribe(t1);
        if (id >= numberOfSub) {
            connect(t);
        }
    }

    private void connect(Token id) {
        List<Token> localQueue;
        synchronized (guard) {
            if (emitting) {
                if (queue == null) {
                    queue = new ArrayList<Token>();
                }
                queue.add(id);
                return;
            }

            localQueue = queue;
            queue = null;
            emitting = true;
        }
        boolean once = true;
        do {
            drain(localQueue);
            if (once) {
                once = false;
                doConnect(id);
            }
            synchronized (guard) {
                localQueue = queue;
                queue = null;
                if (localQueue == null) {
                    emitting = false;
                    return;
                }
            }
        } while (true);
    }

    private void disconnect(Token id) {
        List<Token> localQueue;
        synchronized (guard) {
            if (emitting) {
                if (queue == null) {
                    queue = new ArrayList<Token>();
                }
                queue.add(id.toDisconnect()); // negative value indicates disconnect
                return;
            }

            localQueue = queue;
            queue = null;
            emitting = true;
        }
        boolean once = true;
        do {
            drain(localQueue);
            if (once) {
                once = false;
                doDisconnect(id);
            }
            synchronized (guard) {
                localQueue = queue;
                queue = null;
                if (localQueue == null) {
                    emitting = false;
                    return;
                }
            }
        } while (true);
    }

    private void drain(List<Token> localQueue) {
        if (localQueue == null) {
            return;
        }
        int n = localQueue.size();
        for (int i = 0; i < n; i++) {
            Token id = localQueue.get(i);
            if (id.isDisconnect()) {
                doDisconnect(id);
            } else {
                doConnect(id);
            }
        }
    }

    private void doConnect(Token id) {
        // this method is called only once per id
        // if add succeeds, id was not yet disconnected
        if (connectionStatus.put(id, OCCUPIED) == null) {
            if (count++ == 0) {
                connection = source.connect();
            }
        } else {
            // connection exists due to disconnect, just remove
            connectionStatus.remove(id);
        }
    }

    private void doDisconnect(Token id) {
        // this method is called only once per id
        // if remove succeeds, id was connected
        if (connectionStatus.remove(id) != null) {
            if (--count == 0) {
                connection.unsubscribe();
                connection = null;
            }
        } else {
            // mark id as if connected
            connectionStatus.put(id, OCCUPIED);
        }
    }

    /**
     * Token that represens a connection request or a disconnection request.
     */
    private static final class Token {
        final int id;

        public Token(int id) {
            this.id = id;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (obj.getClass() != getClass()) {
                return false;
            }
            int other = ((Token) obj).id;
            return id == other || -id == other;
        }

        @Override
        public int hashCode() {
            return id < 0 ? -id : id;
        }

        public boolean isDisconnect() {
            return id < 0;
        }

        public Token toDisconnect() {
            if (id < 0) {
                return this;
            }
            return new Token(-id);
        }
    }
}
