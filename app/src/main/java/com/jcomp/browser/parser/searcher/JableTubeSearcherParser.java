package com.jcomp.browser.parser.searcher;

import com.jcomp.browser.parser.ParserBase;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class JableTubeSearcherParser extends ParserBase<Searcher> {
    @Override
    public Searcher parse(Document html, String fullURL) {
        Element element = html.selectFirst("#search_form");
        if (element != null && element.hasAttr("data-url")) {
            String pattern = element.attr("data-url");
            String query = "%QUERY%";
            if (pattern.contains(query)) {
                return new Searcher(pattern.replace(query, "{0}"));
            }

        }
        return null;
    }
}
