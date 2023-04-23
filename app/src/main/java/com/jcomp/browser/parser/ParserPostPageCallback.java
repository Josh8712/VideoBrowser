package com.jcomp.browser.parser;

import com.jcomp.browser.parser.pager.Pager;

public interface ParserPostPageCallback {
    void addPager(Pager pager);

    void pageInfoFinished();
}
