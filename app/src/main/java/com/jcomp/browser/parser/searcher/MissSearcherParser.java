package com.jcomp.browser.parser.searcher;

import com.jcomp.browser.parser.ParserBase;
import com.jcomp.browser.tools.HelperFunc;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class MissSearcherParser extends ParserBase<Searcher> {
    @Override
    public Searcher parse(Document html, String fullURL) {
        Element element = html.selectFirst("input[x-ref]");
        if (element == null)
            return null;
        String prefix = "/search/{0}";
        prefix = HelperFunc.fixURLWithUrl(fullURL, prefix);
        return new Searcher(prefix);
    }
}
