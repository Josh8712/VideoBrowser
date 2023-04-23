package com.jcomp.browser.parser.searcher;

import com.jcomp.browser.parser.ParserBase;
import com.jcomp.browser.tools.HelperFunc;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class II159SearcherParser extends ParserBase<Searcher> {
    @Override
    public Searcher parse(Document html, String fullURL) {
        Element element = html.selectFirst("#sidebar input[name=s]");
        if (element == null)
            return null;
        String pattern = "/video/search/{0}";
        pattern = HelperFunc.fixURLWithUrl(fullURL, pattern);
        return new Searcher(pattern);
    }
}
