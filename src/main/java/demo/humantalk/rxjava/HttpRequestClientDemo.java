package demo.humantalk.rxjava;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.http.util.EntityUtils;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;

import java.io.IOException;
import java.util.concurrent.Future;

public class HttpRequestClientDemo {


    private static final String README_URL = "https://raw.githubusercontent.com/Netflix/RxJava/master/README.md";
    private static final String CHANGES_URL = "https://raw.githubusercontent.com/Netflix/RxJava/master/CHANGES.md";

    public static void main(String[] args) throws IOException {
                              /*
        rawHttpClient();
        rawAsyncHttpClient();
        rxSyncHttpClient();
        rxAsyncHttpClient();
                                */
        final Observable<String> readme = Observable.create(new HttpAsyncGetSubscription(README_URL));
        final Observable<String> readmeSync = Observable.create(new HttpGetSubscription(README_URL));
        final Observable<String> changes = Observable.create(new HttpAsyncGetSubscription(CHANGES_URL));
        readme.mergeWith(changes).mergeWith(readmeSync).map(String::toUpperCase).subscribe(System.out::println);

    }

    private static void rawAsyncHttpClient() {
        CloseableHttpAsyncClient httpclient = HttpAsyncClients.createDefault();
        try {
            // Start the client
            httpclient.start();

            // Execute request
            final HttpGet request1 = new HttpGet(README_URL);
            Future<HttpResponse> future = httpclient.execute(request1, null, new FutureCallback<HttpResponse>() {
                @Override
                public void completed(HttpResponse response) {
                    try {
                        HttpEntity entity = response.getEntity();
                        String content = EntityUtils.toString(entity);
                        System.out.println(content);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }

                @Override
                public void failed(Exception e) {

                }

                @Override
                public void cancelled() {

                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                httpclient.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static void rxAsyncHttpClient() {
        Observable.create(new HttpAsyncGetSubscription(README_URL))
                .subscribe(System.out::println);
    }

    private static void rxSyncHttpClient() {
        Observable.create(new HttpGetSubscription(README_URL))
                .subscribe(System.out::println);
    }

    private static void rawHttpClient() throws IOException {
        HttpGet httpGet = new HttpGet(README_URL);
        CloseableHttpResponse response = null;
        try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
            response = httpclient.execute(httpGet);
            HttpEntity entity = response.getEntity();
            String content = EntityUtils.toString(entity);
            System.out.println(content);
        }


    }

    private static class HttpGetSubscription implements Observable.OnSubscribe<String> {

        private final String url;

        public HttpGetSubscription(String url) {
            this.url = url;
        }

        @Override
        public void call(Subscriber<? super String> subscriber) {
            CloseableHttpClient httpclient = HttpClients.createDefault();
            try {
                HttpGet httpGet = new HttpGet(url);
                CloseableHttpResponse response = httpclient.execute(httpGet);
                System.out.println("GET (sync) " + url);
                HttpEntity entity = response.getEntity();
                String content = EntityUtils.toString(entity);
                subscriber.onNext(content);
                subscriber.onCompleted();
            } catch (IOException e) {
                subscriber.onError(e);
            }

            subscriber.add(new Subscription() {
                private boolean unsubscribed = false;

                @Override
                public void unsubscribe() {
                    try {
                        httpclient.close();
                    } catch (IOException ignored) {

                    } finally {
                        unsubscribed = true;
                    }
                }

                @Override
                public boolean isUnsubscribed() {
                    return unsubscribed;
                }
            });
        }
    }

    private static class HttpAsyncGetSubscription implements Observable.OnSubscribe<String> {
        private final String url;

        public HttpAsyncGetSubscription(String url) {
            this.url = url;
        }

        @Override
        public void call(Subscriber<? super String> subscriber) {
            CloseableHttpAsyncClient httpclient = HttpAsyncClients.createDefault();
            try {
                // Start the client
                httpclient.start();

                // Execute request
                final HttpGet request1 = new HttpGet(url);
                httpclient.execute(request1, new FutureCallback<HttpResponse>() {
                    @Override
                    public void completed(HttpResponse response) {
                        HttpEntity entity = response.getEntity();
                        try {
                            final String content = EntityUtils.toString(entity);
                            subscriber.onNext(content);
                            subscriber.onCompleted();
                        } catch (IOException e) {
                            subscriber.onError(e);
                        }
                    }

                    @Override
                    public void failed(Exception e) {
                        subscriber.onError(e);
                    }

                    @Override
                    public void cancelled() {
                        subscriber.onCompleted();
                    }
                });
                System.out.println("GET " + url);

            } catch (Exception e) {
                subscriber.onError(e);
            }
            subscriber.add(new Subscription() {
                @Override
                public void unsubscribe() {
                    try {
                        httpclient.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public boolean isUnsubscribed() {
                    return httpclient.isRunning();
                }
            });
        }
    }
}
