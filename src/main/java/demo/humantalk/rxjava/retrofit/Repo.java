package demo.humantalk.rxjava.retrofit;

/**
 * Created by david.wursteisen on 21/10/2014.
 */
public class Repo {
    public String name;
    public String full_name;

    @Override
    public String toString() {
        return "Repo{" +
                "name='" + name + '\'' +
                ", full_name='" + full_name + '\'' +
                '}';
    }
}
