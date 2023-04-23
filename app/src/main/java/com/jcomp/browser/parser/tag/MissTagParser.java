package com.jcomp.browser.parser.tag;

import com.jcomp.browser.parser.ParserBase;
import com.jcomp.browser.tools.HelperFunc;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

public class MissTagParser extends ParserBase<List<Tag>> {
    @Override
    public List<Tag> parse(Document html, String fullURL) {
        List<Tag> list = new ArrayList<>();
        Elements elements = html.select("div[x-show].z-max a");
        for (Element element : elements) {
            if (element.attr("href").startsWith("#") || !element.attr("target").isEmpty())
                continue;
            list.add(new Tag(element.text(), null, HelperFunc.fixURLWithUrl(fullURL, element.attr("href"))));
        }
        return list;
    }
}
