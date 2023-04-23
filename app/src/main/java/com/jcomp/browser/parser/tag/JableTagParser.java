package com.jcomp.browser.parser.tag;

import com.jcomp.browser.parser.ParserBase;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

public class JableTagParser extends ParserBase<List<Tag>> {
    @Override
    public List<Tag> parse(Document html, String fullURL) {
        List<Tag> list = new ArrayList<>();
        Elements elements = html.select(".tag");
        for (Element element : elements) {
            String title = element.text();
            String url = element.attr("href");
            Tag tag = new Tag(title, null, url);
            list.add(tag);
        }
        return list;
    }
}
