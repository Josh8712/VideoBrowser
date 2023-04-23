package com.jcomp.browser.parser.tag;

import com.jcomp.browser.parser.ParserBase;
import com.jcomp.browser.parser.post.db.Post;
import com.jcomp.browser.tools.HelperFunc;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.LinkedHashMap;

public class NetImageTagParser extends ParserBase<LinkedHashMap<String, Post>> {
    @Override
    public LinkedHashMap<String, Post> parse(Document html, String fullURL) {
        LinkedHashMap<String, Post> list = new LinkedHashMap<>();
        Elements elements = html.select("a:has( div.actress_grid_title_block )");
        for (Element element : elements) {
            Elements title = element.select(".actress_grid_title_block");
            Elements img = element.select("img");
            if (title.isEmpty() || img.isEmpty())
                continue;
            ImageTag tag = new ImageTag(title.get(0).text(), img.get(0).attr("src"), HelperFunc.fixURLWithUrl(fullURL, element.attr("href")));
            list.put(tag.getKey(), tag);
        }
        return list;
    }
}
