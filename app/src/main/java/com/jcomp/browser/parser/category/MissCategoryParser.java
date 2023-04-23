package com.jcomp.browser.parser.category;

import com.jcomp.browser.parser.ParserBase;
import com.jcomp.browser.tools.HelperFunc;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

public class MissCategoryParser extends ParserBase<List<Category>> {
    @Override
    public List<Category> parse(Document html, String fullURL) {
        List<Category> categories = new ArrayList<>();
        Elements elements = html.select("div.flex > div.flex-1:has(h2)");
        for (Element element : elements) {
            String title = element.text();
            element = element.parent();
            Elements a = element.select("a");
            if (a.isEmpty())
                continue;
            String url = null;
            for (Element e : a) {
                if (e.attr("href").startsWith("#"))
                    continue;
                url = e.attr("href");
            }
            if (url == null)
                continue;
            categories.add(new Category(title, HelperFunc.fixURLWithUrl(fullURL, url)));
        }
        return categories;
    }
}
