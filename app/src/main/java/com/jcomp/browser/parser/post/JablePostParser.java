package com.jcomp.browser.parser.post;

import com.jcomp.browser.parser.ParserBase;
import com.jcomp.browser.parser.post.db.Post;
import com.jcomp.browser.tools.HelperFunc;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.LinkedHashMap;

public class JablePostParser extends ParserBase<LinkedHashMap<String, Post>> {
    @Override
    public LinkedHashMap<String, Post> parse(Document html, String fullURL) {
        LinkedHashMap<String, Post> posts = new LinkedHashMap<>();
        Elements elements = html.select(".video-img-box");
        for (Element element : elements) {
            Elements title = element.select(".title");
            Elements img = element.select("img");
            if (title.isEmpty() || img.isEmpty())
                continue;
            Elements url = title.select("a");
            if (url.isEmpty())
                continue;
            Post post = new Post(title.get(0).text(), img.get(0).attr("data-src"), HelperFunc.fixURLWithUrl(fullURL, url.get(0).attr("href")));
            posts.put(post.getKey(), post);
        }
        return posts;
    }
}
