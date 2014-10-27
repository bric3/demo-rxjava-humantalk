package demo.humantalk.rxjava;

import com.mongodb.ServerAddress;
import com.mongodb.async.client.MongoClientSettings;
import com.mongodb.async.rx.client.MongoClients;
import com.mongodb.async.rx.client.MongoCollection;
import com.mongodb.async.rx.client.MongoDatabase;
import com.mongodb.connection.ClusterSettings;
import demo.humantalk.rxjava.retrofit.Contributor;
import demo.humantalk.rxjava.retrofit.Repo;
import org.bson.Document;
import retrofit.RestAdapter;
import retrofit.http.GET;
import retrofit.http.Path;
import rx.Observable;
import rx.schedulers.Schedulers;

import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;

/**
 * Created by david on 26/10/14.
 */
public class AsyncMongoDb {

    public static void main(String[] args) throws UnknownHostException, InterruptedException {

        GitHubServiceRx serviceRx = buildService(GitHubServiceRx.class);

        MongoDatabase db = buildMongoDb();
        MongoCollection<Document> collection = db.getCollection("hello");

        collection.find(new Document("username", new Document("$exists", true)))
                .forEach()
                .observeOn(Schedulers.immediate())
                .subscribe(System.out::println);

        collection.find(new Document("username", new Document("$exists", true)))
                .forEach()
                .observeOn(Schedulers.computation())
                .doOnNext(System.out::println)
                .flatMap((d) -> serviceRx.repos(d.getString("username")))
                .flatMap(Observable::from)
                .doOnNext((r) -> db.getCollection("repo").insert(new Document("name", r.full_name)).subscribe())
                .toBlocking()
                .forEach(System.out::println);



    }

    private static <T> T buildService(final Class<T> service) {
        RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint("https://api.github.com")
                .build();

        return restAdapter.create(service);
    }

    private static MongoDatabase buildMongoDb() throws UnknownHostException {
        ClusterSettings clusterSettings = ClusterSettings.builder()
                .hosts(Arrays.asList(new ServerAddress("localhost:27017")))
                .build();

        MongoClientSettings clientSettings = MongoClientSettings.builder()
                .clusterSettings(clusterSettings)
                .build();

        return MongoClients.create(clientSettings).getDatabase("rx");
    }

    public static interface GitHubServiceRx {
        @GET("/repos/{owner}/{repo}/stats/contributors")
        Observable<List<Contributor>> contributors(@Path("owner") String owner, @Path("repo") String repo);

        @GET("/users/{username}/repos")
        Observable<List<Repo>> repos(@Path("username") String username);
    }
}
