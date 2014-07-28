package demo.humantalk.rxjava;

public class NewsStories {
    public final String heureStr;
    public final String text;
    public final String link;

    private NewsStories(String hourStr, String title, String link) {
        this.heureStr = hourStr;
        this.text = title;
        this.link = link;
    }

    public static NewsStories from(String heureStr, String text, String link) {
        return new NewsStories(heureStr, text, link);
    }

    @Override
    public String toString() {
        return "{" + heureStr + " : '" + text + "'}";
    }
}
