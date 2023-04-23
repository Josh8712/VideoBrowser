package com.jcomp.browser.parser.tag;

import com.jcomp.browser.parser.ParserBase;
import com.jcomp.browser.parser.post.db.Post;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.LinkedHashMap;

public class JableImageTagParser extends ParserBase<LinkedHashMap<String, Post>> {
    @Override
    public LinkedHashMap<String, Post> parse(Document html, String fullURL) {
        LinkedHashMap<String, Post> list = new LinkedHashMap<>();
        Elements elements = html.select(".img-box a");

        for (Element element : elements) {
            Elements title = element.select("h4");
            Elements img = element.select("img");
            if (title.isEmpty() || img.isEmpty())
                continue;
            ImageTag post = new ImageTag(title.get(0).text(), img.get(0).attr("src"), element.attr("href"));
            list.put(post.getKey(), post);
        }
        return list;
    }
}
