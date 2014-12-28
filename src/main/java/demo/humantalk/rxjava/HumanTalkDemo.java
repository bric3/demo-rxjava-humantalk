package demo.humantalk.rxjava;

import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.http.nio.client.methods.HttpAsyncMethods;
import org.jsoup.Jsoup;
import org.jsoup.select.Elements;
import rx.Observable;
import rx.apache.http.ObservableHttp;
import rx.functions.Action0;
import rx.functions.Func1;
import rx.plugins.RxJavaErrorHandler;
import rx.plugins.RxJavaPlugins;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class HumanTalkDemo {

    public static void main(String[] args) {
        CloseableHttpAsyncClient client = HttpAsyncClients.createDefault();
        client.start();

        RxJavaPlugins.getInstance().registerErrorHandler(new RxJavaErrorHandler() {
            @Override
            public void handleError(Throwable e) {
                e.printStackTrace();
            }
        });


        Observable.timer(0, 4, TimeUnit.SECONDS)
                .flatMap(tick -> new PageObservable(client).observe("http://www.lemonde.fr/"))
                .flatMap(toNewsStories())
//                .doOnTerminate(closeHttpClient(client))
                .subscribe(
                        System.out::println,
                        System.err::println
//                        closeHttpClient(client)
                );
    }

    private static Func1<String, Observable<? extends NewsStories>> toNewsStories() {
        return (String webPage) -> Observable.just(webPage)
                .map(Jsoup::parse)
                .flatMap(document -> {
                    Elements en_continu_items = document.select("div#body-publicite > div.global div.pages > ul.liste_horaire > li");
                    return Observable.from(en_continu_items);
                })
                .map(element -> NewsStories.from(
                        element.select("span.heure").text(),
                        element.select("a").text(),
                        "http://www.lemonde.fr/" + element.select("a").attr("href")
                ));
    }


    private static Action0 closeHttpClient(CloseableHttpAsyncClient client) {
        return () -> {
            try {
                client.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        };
    }


    private static class PageObservable {
        private CloseableHttpAsyncClient client;

        public PageObservable(CloseableHttpAsyncClient client) {
            this.client = client;
        }

        public Observable<String> observe(String requestURI) {
            return ObservableHttp.createRequest(HttpAsyncMethods.createGet(requestURI), client)
                    .toObservable()
                    .flatMap(response -> response.getContent().map(String::new))
                    .collect(StringBuilder::new, StringBuilder::append)
                    .map(StringBuilder::toString)
                    ;
        }
    }
}
