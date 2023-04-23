package com.jcomp.browser.parser.post;

import com.jcomp.browser.parser.ParserBase;
import com.jcomp.browser.parser.post.db.Post;
import com.jcomp.browser.tools.HelperFunc;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.LinkedHashMap;

public class MissPostParser extends ParserBase<LinkedHashMap<String, Post>> {
    @Override
    public LinkedHashMap<String, Post> parse(Document html, String fullURL) {
        LinkedHashMap<String, Post> posts = new LinkedHashMap<>();
        Elements elements = html.select("div a:has(video)");
        for (Element element : elements) {
            Elements img = element.select("img");
            if (img.isEmpty())
                continue;
            String imgSrc = img.get(0).attr("data-src");
            if (imgSrc.equals(""))
                imgSrc = img.get(0).attr("src");
            String url = HelperFunc.fixURLWithUrl(fullURL, element.attr("href"));
            String title = url.substring(url.lastIndexOf("/") + 1);
            Post post = new Post(title + " " + img.attr("alt"), imgSrc, url);
            posts.put(post.getKey(), post);
        }
        return posts;
    }
}
