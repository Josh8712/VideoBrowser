package com.jcomp.browser.parser;

import com.jcomp.browser.parser.category.Category;
import com.jcomp.browser.parser.searcher.Searcher;
import com.jcomp.browser.parser.tag.Tag;

import java.util.List;

public interface ParserPageCallback {
    void addCategory(List<Category> categoryList);

    void addTagList(List<Tag> tagList);

    void addSearcher(Searcher searcher);
}
