package com.jcomp.browser.parser.category;

import com.jcomp.browser.parser.ParserBase;
import com.jcomp.browser.tools.HelperFunc;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

public class TWDVDCategoryParser extends ParserBase<List<Category>> {
    @Override
    public List<Category> parse(Document html, String fullURL) {
        List<Category> categories = new ArrayList<>();
        Elements elements = html.select("tr td[bgcolor] > a:not([target]):not([onclick])");
        for (Element element : elements) {
            String title = element.text();
            if (element.attr("href").equals("#") || element.attr("href").startsWith("javascript:"))
                continue;
            Category category = new Category(title, HelperFunc.fixURLWithUrl(fullURL, element.attr("href")));
            categories.add(category);
        }

        return categories;
    }
}
