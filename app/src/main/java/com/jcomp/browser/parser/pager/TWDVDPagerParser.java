package com.jcomp.browser.parser.pager;

import android.util.Pair;

import com.jcomp.browser.parser.ParserBase;
import com.jcomp.browser.tools.HelperFunc;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

public class TWDVDPagerParser extends ParserBase<Pager> {
    @Override
    public Pager parse(Document html, String fullURL) {
        List<Pair<String, String>> pages = new ArrayList<>();
        int curIndex = -1;
        Elements elements = html.select("#maincontent div[align=center] a");
        for (Element element : elements) {
            String url = null;
            String pageName = element.text();
            url = element.attr("href");
            url = HelperFunc.fixURLWithUrl(fullURL, url);
            pages.add(new Pair<>(pageName, url));
        }
        if (pages.isEmpty())
            return null;
        if (pages.get(0).first.equals("2")) {
            curIndex = 0;
        } else {
            curIndex = 2;
        }
        pages.add(curIndex, new Pair<>("--", null));
        return new Pager(pages, curIndex);
    }
}
