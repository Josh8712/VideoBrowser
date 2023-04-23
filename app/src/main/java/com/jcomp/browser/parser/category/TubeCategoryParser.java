package com.jcomp.browser.parser.category;

import com.jcomp.browser.parser.ParserBase;
import com.jcomp.browser.tools.HelperFunc;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

public class TubeCategoryParser extends ParserBase<List<Category>> {
    @Override
    public List<Category> parse(Document html, String fullURL) {
        List<Category> categories = new ArrayList<>();
        Elements hrefs = html.select("ul.primary li a[href]");
        for (Element href : hrefs) {
            if (href == hrefs.get(0))
                continue;
            String title = href.text();
            if (title.isEmpty())
                continue;
            Category category = new Category(title, HelperFunc.fixURLWithUrl(fullURL, href.attr("href")));
            categories.add(category);
        }

        return categories;
    }
}
