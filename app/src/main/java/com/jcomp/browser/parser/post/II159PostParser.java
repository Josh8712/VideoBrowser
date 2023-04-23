package com.jcomp.browser.parser.post;

import android.net.Uri;

import com.jcomp.browser.parser.ParserBase;
import com.jcomp.browser.parser.post.db.Post;
import com.jcomp.browser.tools.HelperFunc;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.LinkedHashMap;

public class II159PostParser extends ParserBase<LinkedHashMap<String, Post>> {
    @Override
    public LinkedHashMap<String, Post> parse(Document html, String fullURL) {
        LinkedHashMap<String, Post> posts = new LinkedHashMap<>();
        Elements elements = html.select(".post");
        for (Element element : elements) {
            Elements title = element.select("h2");
            Elements img = element.select("iframe");
            if (title.isEmpty() || img.isEmpty())
                continue;
            String url = HelperFunc.fixURLWithUrl(fullURL, img.attr("src"));
            String imgURL = Uri.parse(url).getQueryParameter("im");
            imgURL = HelperFunc.fixURLWithUrl(fullURL, imgURL);
            Post post = new Post(title.get(0).text(), imgURL, url);
            posts.put(post.getKey(), post);
        }
        return posts;
    }
}
