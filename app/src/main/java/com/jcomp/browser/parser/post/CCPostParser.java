package com.jcomp.browser.parser.post;

import com.jcomp.browser.parser.ParserBase;
import com.jcomp.browser.parser.post.db.Post;
import com.jcomp.browser.tools.HelperFunc;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.LinkedHashMap;

public class CCPostParser extends ParserBase<LinkedHashMap<String, Post>> {
    @Override
    public LinkedHashMap<String, Post> parse(Document html, String fullURL) {
        LinkedHashMap<String, Post> posts = new LinkedHashMap<>();
        Elements elements = html.select("#waterfall li");
        for (Element element : elements) {
            Elements title = element.select("a");
            Elements img = element.select("img");
            if (title.isEmpty() || img.isEmpty())
                continue;
            String url = HelperFunc.fixURLWithUrl(fullURL, title.get(0).attr("href"));
            Post post = new Post(title.get(0).attr("title"), img.get(0).attr("src"), url);
            posts.put(post.getKey(), post);
        }
        return posts;
    }
}
