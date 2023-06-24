package com.jcomp.browser.parser.comic;

import com.jcomp.browser.parser.ParserBase;
import com.jcomp.browser.parser.post.db.Post;
import com.jcomp.browser.tools.HelperFunc;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.LinkedHashMap;

public class AnimezillaComicParser extends ParserBase<LinkedHashMap<String, Post>> {
    @Override
    public LinkedHashMap<String, Post> parse(Document html, String fullURL) {
        LinkedHashMap<String, Post> posts = new LinkedHashMap<>();
        LargeList(posts, html, fullURL);
        SmallPost(posts, html, fullURL);
        return posts;
    }

    private void LargeList(LinkedHashMap<String, Post> posts, Document html, String fullURL) {
        Elements elements = html.select("article #post-listing a");
        for (Element element : elements) {
            Elements title = element.select("span");
            Elements img = element.select("img");
            if (title.isEmpty() || img.isEmpty())
                continue;
            String imgURL = extractImage(img.get(0).attr("style"));
            if (imgURL == null)
                continue;
            Post post = new Post(title.get(0).text(), imgURL, HelperFunc.fixURLWithUrl(fullURL, element.attr("href")), Post.TYPE_COMIC);
            posts.put(post.getKey(), post);
        }
    }

    private void SmallPost(LinkedHashMap<String, Post> posts, Document html, String fullURL) {
        Elements elements = html.select("article .entry-content a");
        for (Element element : elements) {
            Elements title = element.select("h2 a");
            Elements img = element.select("img");
            if (title.isEmpty() || img.isEmpty())
                continue;
            String imgURL = img.get(0).attr("src");
            Post post = new Post(title.get(0).text(), imgURL, HelperFunc.fixURLWithUrl(fullURL, element.attr("href")), Post.TYPE_COMIC);
            posts.put(post.getKey(), post);
        }
    }

    private String extractImage(String style) {
        if (style == null || style.isEmpty())
            return null;
        String[] pair = style.split(";");
        for (String p : pair) {
            String[] kv = p.split(":", 2);
            if (kv.length == 2 && kv[0].trim().equals("background")) {
                String url = kv[1].trim();
                int start = url.indexOf("url(");
                int end = url.indexOf(")");
                if (start == -1 || end == -1)
                    return null;
                return url.substring(start + 5, end - 1);
            }
        }
        return null;
    }
}
