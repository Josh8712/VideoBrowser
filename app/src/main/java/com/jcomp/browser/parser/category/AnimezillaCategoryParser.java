package com.jcomp.browser.parser.category;

import com.jcomp.browser.parser.ParserBase;
import com.jcomp.browser.tools.HelperFunc;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class AnimezillaCategoryParser extends ParserBase<List<Category>> {
    @Override
    public List<Category> parse(Document html, String fullURL) {
        List<Category> categories = new ArrayList<>();
        Elements elements = html.select(".shiftnav-nav li a");
        for (Element element : elements) {
            String url = element.attr("href");
            boolean sameSite = false;
            try {
                sameSite = new URL(url).getHost().equals(new URL(fullURL).getHost());
            } catch (MalformedURLException ignore) {}
            if(!sameSite)
                continue;
            Category category = new Category(element.text(), HelperFunc.fixURLWithUrl(fullURL, element.attr("href")));
            categories.add(category);
        }

        return categories;
    }
}
