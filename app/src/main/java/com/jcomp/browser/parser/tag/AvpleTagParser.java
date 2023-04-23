package com.jcomp.browser.parser.tag;

import com.jcomp.browser.parser.ParserBase;
import com.jcomp.browser.tools.HelperFunc;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

public class AvpleTagParser extends ParserBase<List<Tag>> {
    @Override
    public List<Tag> parse(Document html, String fullURL) {
        List<Tag> list = new ArrayList<>();
        home(html, fullURL, list);
        tagPage(html, fullURL, list);
        return list;
    }

    private void home(Document html, String fullURL, List<Tag> list) {
        Elements elements = html.select(".box .list-tags a");
        for (Element link : elements) {
            String title = link.text();
            if (title.isEmpty())
                continue;
            list.add(new Tag(title, HelperFunc.fixURLWithUrl(fullURL, link.attr("href"))));
        }
    }

    private void tagPage(Document html, String fullURL, List<Tag> list) {
        Elements elements = html.select(".tags-cloud a");
        for (Element link : elements) {
            String title = link.text();
            if (title.isEmpty())
                continue;
            list.add(new Tag(title, HelperFunc.fixURLWithUrl(fullURL, link.attr("href"))));
        }
    }
}
