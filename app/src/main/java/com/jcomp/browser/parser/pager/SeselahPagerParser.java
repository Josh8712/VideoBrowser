package com.jcomp.browser.parser.pager;

import android.util.Pair;

import com.jcomp.browser.parser.ParserBase;
import com.jcomp.browser.tools.HelperFunc;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

public class SeselahPagerParser extends ParserBase<Pager> {
    @Override
    public Pager parse(Document html, String fullURL) {
        List<Pair<String, String>> pages = new ArrayList<>();
        int curIndex = -1;
        Elements elements = html.select(".pagination, .page-navigator");
        if (elements.isEmpty())
            return null;
        elements = elements.select("li a:last-child");
        if (elements.isEmpty())
            return null;
        for (Element element : elements) {
            String url = null;
            String pageName = element.text();
            if (pageName.isEmpty())
                pageName = "--";
            if (element.attr("href").equals("") || element.parent() != null && element.parent().attr("class").contains("current")) {
                curIndex = pages.size();
            } else {
                url = element.attr("href");
                url = HelperFunc.fixURLWithUrl(fullURL, url);
            }
            pages.add(new Pair<>(pageName, url));
        }
        if (pages.isEmpty() || curIndex < 0)
            return null;
        return new Pager(pages, curIndex);
    }
}
