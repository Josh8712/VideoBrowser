package com.jcomp.browser.parser.category;

import com.jcomp.browser.parser.ParserBase;
import com.jcomp.browser.tools.HelperFunc;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

public class JableCategoryParser extends ParserBase<List<Category>> {
    @Override
    public List<Category> parse(Document html, String fullURL) {
        List<Category> categories = new ArrayList<>();
        Elements elements = html.select(".title-with-more");
        for (Element element : elements) {
            Elements hrefList = element.select(".more a");
            Elements title = element.select("h2");
            if (hrefList.isEmpty() || title.isEmpty())
                continue;
            if (title.get(0).text().trim().isEmpty())
                continue;
            Category category = new Category(title.get(0).text(), HelperFunc.fixURLWithUrl(fullURL, hrefList.get(0).attr("href")));
            categories.add(category);
        }

        return categories;
    }
}
