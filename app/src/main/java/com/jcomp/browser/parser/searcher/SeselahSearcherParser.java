package com.jcomp.browser.parser.searcher;

import com.jcomp.browser.parser.ParserBase;
import com.jcomp.browser.tools.HelperFunc;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class SeselahSearcherParser extends ParserBase<Searcher> {
    @Override
    public Searcher parse(Document html, String fullURL) {
        Element element = html.selectFirst(".search-outer input");
        if (element != null) {
            String pattern = "/?q={0}";
            return new Searcher(HelperFunc.fixURLWithUrl(fullURL, pattern));
        }
        return null;
    }
}
