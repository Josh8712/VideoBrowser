package com.jcomp.browser.parser.pager;

import android.util.Pair;

import com.jcomp.browser.parser.ParserBase;
import com.jcomp.browser.tools.HelperFunc;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

public class CCPagerParser extends ParserBase<Pager> {
    @Override
    public Pager parse(Document html, String fullURL) {
        List<Pair<String, String>> pages = new ArrayList<>();
        int curIndex = -1;
        Elements elements = html.select(".pg");
        if (elements.size() > 0) {
            elements = elements.get(0).children();
            for (Element element : elements) {
                String url = null;
                String pageName = element.text();
                if (element.tagName().equals("a")) {
                    if (element.text().trim().length() > 0) {
                        url = element.attr("href");
                        url = HelperFunc.fixURLWithUrl(fullURL, url);
                    } else
                        continue;
                } else if (element.tagName().equals("strong")) {
                    curIndex = pages.size();
                } else
                    continue;
                pages.add(new Pair<>(pageName, url));
            }
        }
        if (pages.isEmpty() || curIndex < 0)
            return null;
        return new Pager(pages, curIndex);
    }
}
