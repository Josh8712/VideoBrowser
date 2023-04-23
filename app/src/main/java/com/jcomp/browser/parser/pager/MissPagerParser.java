package com.jcomp.browser.parser.pager;

import android.util.Pair;

import com.jcomp.browser.parser.ParserBase;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

public class MissPagerParser extends ParserBase<Pager> {
    @Override
    public Pager parse(Document html, String fullURL) {
        List<Pair<String, String>> pages = new ArrayList<>();
        int curIndex = -1;
        Element root = html.selectFirst("nav[x-data] > div > span");
        if (root != null) {
            Elements elements = root.children();
            for (Element element : elements) {
                String text = element.text();
                if (text.isEmpty())
                    continue;
                String url = null;
                if (!element.attr("href").isEmpty()) {
                    url = element.attr("href");
                } else if (!element.attr("aria-current").isEmpty()) {
                    curIndex = pages.size();
                } else
                    continue;
                pages.add(new Pair<>(text, url));
            }
        }
        if (pages.isEmpty() || curIndex < 0)
            return null;
        return new Pager(pages, curIndex);
    }
}
