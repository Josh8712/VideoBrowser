package com.jcomp.browser.parser.tag;

import com.jcomp.browser.parser.ParserBase;
import com.jcomp.browser.tools.HelperFunc;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

public class CCTagParser extends ParserBase<List<Tag>> {
    @Override
    public List<Tag> parse(Document html, String fullURL) {
        List<Tag> list = new ArrayList<>();
        Elements elements = html.select("#thread_types a");
        for (Element element : elements) {
            String title = element.ownText();
            String url = element.attr("href");
            url = HelperFunc.fixURLWithUrl(fullURL, url);
            list.add(new Tag(title, url));
        }
        return list;
    }
}
