package demo.humantalk.rxjava;

import demo.humantalk.rxjava.retrofit.Contributor;
import demo.humantalk.rxjava.retrofit.Repo;
import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.http.GET;
import retrofit.http.Path;
import rx.Observable;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Created by david.wursteisen on 21/10/2014.
 */
public class FromFutureToCallbackToRx {


    public static void main(String[] args) throws InterruptedException {

        System.out.println("====== SYNC ======");
        GitHubServiceSync service = buildService(GitHubServiceSync.class);
        List<Contributor> contributors = service.contributors("ReactiveX", "RxJava");
        for (int i = 0; i < 5; i++) {
            List<Repo> repos = service.repos(contributors.get(i).author.login);
            for (Repo repo : repos) {
                System.out.println("[SYNC] => " + repo.full_name);

            }
        }


        System.out.println("====== ASYNC FUTURE ======");
        CompletableFuture.supplyAsync(
                () -> service.contributors("ReactiveX", "RxJava")).thenAccept(c -> {


            for (int i = 0; i < 5; i++) {
                final int index = i;

                CompletableFuture<List<Repo>> f = CompletableFuture.supplyAsync(() -> service.repos(c.get(index).author.login));
                f.thenAccept((repos) -> {
                    for (Repo repo : repos) {
                        System.out.println("[ASYNC FUTURE] => " + repo.full_name);
                    }
                });
            }

        });


        Thread.sleep(1000);
        System.out.println("====== ASYNC ======");
        GitHubServiceASync serviceAsync = buildService(GitHubServiceASync.class);
        serviceAsync.contributors("ReactiveX", "RxJava", new Callback<List<Contributor>>() {
            @Override
            public void success(final List<Contributor> contributors, final Response response) {
                for (int i = 0; i < 5; i++) {
                    serviceAsync.repos(contributors.get(i).author.login, new Callback<List<Repo>>() {
                        @Override
                        public void success(final List<Repo> repos, final Response response) {
                            for (Repo repo : repos) {
                                System.out.println("[ASYNC] => " + repo.full_name);
                            }
                        }

                        @Override
                        public void failure(final RetrofitError retrofitError) {

                        }
                    });
                }
            }

            @Override
            public void failure(final RetrofitError retrofitError) {

            }
        });

        Thread.sleep(1000);
        System.out.println("====== RXJAVA ======");
        GitHubServiceRx serviceRx = buildService(GitHubServiceRx.class);
        serviceRx.contributors("ReactiveX", "RxJava")
                .flatMap(Observable::from)
                .take(5)
                .flatMap((c) -> serviceRx.repos(c.author.login))
                .flatMap(Observable::from)
                .subscribe((r) -> System.out.println("[Rx] => " + r.full_name));
    }

    private static <T> T buildService(final Class<T> service) {
        RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint("https://api.github.com")
                .build();

        return restAdapter.create(service);
    }

    public static interface GitHubServiceSync {
        @GET("/repos/{owner}/{repo}/stats/contributors")
        List<Contributor> contributors(@Path("owner") String owner, @Path("repo") String repo);

        @GET("/users/{username}/repos")
        List<Repo> repos(@Path("username") String username);
    }

    public static interface GitHubServiceASync {
        @GET("/repos/{owner}/{repo}/stats/contributors")
        void contributors(@Path("owner") String owner, @Path("repo") String repo, Callback<List<Contributor>> callback);

        @GET("/users/{username}/repos")
        void repos(@Path("username") String username, Callback<List<Repo>> callback);
    }

    public static interface GitHubServiceRx {
        @GET("/repos/{owner}/{repo}/stats/contributors")
        Observable<List<Contributor>> contributors(@Path("owner") String owner, @Path("repo") String repo);

        @GET("/users/{username}/repos")
        Observable<List<Repo>> repos(@Path("username") String username);
    }


}
