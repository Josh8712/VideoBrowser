package com.jcomp.browser.parser;

import org.jsoup.nodes.Document;

abstract public class ParserBase<T> {
    abstract public T parse(Document html, String fullURL);
}
