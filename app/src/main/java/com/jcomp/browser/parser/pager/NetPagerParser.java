package com.jcomp.browser.parser.pager;

import android.util.Pair;

import com.jcomp.browser.parser.ParserBase;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

public class NetPagerParser extends ParserBase<Pager> {
    @Override
    public Pager parse(Document html, String fullURL) {
        List<Pair<String, String>> pages = new ArrayList<>();
        int curIndex = -1;
        Elements elements = html.select(".pagination > a");
        for (Element element : elements) {
            String text = element.text();
            if (text.trim().isEmpty())
                continue;
            String url = null;
            if (element.attr("aria-current").equals("false")) {
                int pageVal = Integer.parseInt(element.attr("value"));
                url = fullURL.replaceAll("&?page=\\d+", "");
                if (!url.contains("?"))
                    url += "?";
                url += "&page=" + pageVal;
            } else
                curIndex = pages.size();
            pages.add(new Pair<>(text, url));
        }
        if (pages.isEmpty() || curIndex < 0)
            return null;
        return new Pager(pages, curIndex);
    }
}
