package com.jcomp.browser.parser.tag;

import com.jcomp.browser.parser.ParserBase;
import com.jcomp.browser.parser.post.db.Post;
import com.jcomp.browser.tools.HelperFunc;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.LinkedHashMap;

public class TubeImageTagParser extends ParserBase<LinkedHashMap<String, Post>> {
    @Override
    public LinkedHashMap<String, Post> parse(Document html, String fullURL) {
        LinkedHashMap<String, Post> list = new LinkedHashMap<>();
        member(html, fullURL, list);
        categories(html, fullURL, list);
        return list;
    }

    private void member(Document html, String fullURL, LinkedHashMap<String, Post> list) {
        Elements elements = html.select(".list-members a");
        for (Element element : elements) {
            Tag post = new Tag(element.attr("title"), HelperFunc.fixURLWithUrl(fullURL, element.attr("href")));
            list.put(post.getKey(), post);
        }
    }

    private void categories(Document html, String fullURL, LinkedHashMap<String, Post> list) {
        Elements elements = html.select(".list-categories a");
        for (Element element : elements) {
            Element img = element.selectFirst("img");
            if (img == null)
                continue;
            ImageTag post = new ImageTag(element.attr("title"), img.attr("src"), HelperFunc.fixURLWithUrl(fullURL, element.attr("href")));
            list.put(post.getKey(), post);
        }
    }
}
