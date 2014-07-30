package demo.humantalk.rxjava;

import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.http.nio.client.methods.HttpAsyncMethods;
import org.jsoup.Jsoup;
import org.jsoup.select.Elements;
import rx.Observable;
import rx.apache.http.ObservableHttp;
import rx.functions.Action0;
import rx.plugins.RxJavaErrorHandler;
import rx.plugins.RxJavaPlugins;
import rx.subjects.PublishSubject;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

public class HumanTalkDemo {

    public static void main(String[] args) {
        PublishSubject<String> endNotifier = PublishSubject.create();
        AtomicInteger counter = new AtomicInteger();

        CloseableHttpAsyncClient client = HttpAsyncClients.createDefault();
        client.start();

        // Titre en continu
        // #body-publicite > div.global.une_edito > div > div.grid_6.omega.col_droite.resizable > div.bloc_base.bloc_droit.en_continu

        // En continu:
        // #body-publicite > div.global.une_edito > div > div.grid_6.omega.col_droite.resizable > div.bloc_base.bloc_droit.en_continu > div.pages > ul.liste_horaire.no_border.current > li:nth-child(1)
        // heure
        //    ... li:nth-child(1) > span.heure
        // lien et titre
        //    ... li:nth-child(1) > a.texte
        RxJavaPlugins.getInstance().registerErrorHandler(new RxJavaErrorHandler() {

            @Override
            public void handleError(Throwable e) {
                e.printStackTrace();
            }
        });

        new PageObservable(client).observe("http://www.lemonde.fr/")
                .map(Jsoup::parse)
                .flatMap(document -> {
                    Elements en_continu_items = document.select("div#body-publicite > div.global div.pages > ul.liste_horaire > li");
                    return Observable.from(en_continu_items);
                })
                .map(element -> NewsStories.from(
                        element.select("span.heure").text(),
                        element.select("a").text(),
                        "http://www.lemonde.fr/" + element.select("a").attr("href")
                ))
                .doOnTerminate(closeHttpClient(client))
                .subscribe(System.out::println);


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
                    .collect(new StringBuffer(), (accumulator, chunks) -> accumulator.append(chunks))
                    .map(StringBuffer::toString);
        }
    }
}
