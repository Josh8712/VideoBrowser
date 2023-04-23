package com.jcomp.browser.parser.tag;

import com.jcomp.browser.parser.ParserBase;
import com.jcomp.browser.parser.post.db.Post;
import com.jcomp.browser.tools.HelperFunc;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.LinkedHashMap;

public class SeselahImageTagParser extends ParserBase<LinkedHashMap<String, Post>> {
    @Override
    public LinkedHashMap<String, Post> parse(Document html, String fullURL) {
        LinkedHashMap<String, Post> list = new LinkedHashMap<>();
        Elements elements = html.select(".tag-list a");

        for (Element element : elements) {
            String title = element.text();
            Elements img = element.select(".thumb");
            if (img.isEmpty())
                continue;
            String imgURL = img.attr("style").split("url\\(")[1].split("\\)")[0];
            ImageTag post = new ImageTag(title, imgURL, HelperFunc.fixURLWithUrl(fullURL, element.attr("href")));
            list.put(post.getKey(), post);
        }
        return list;
    }
}
