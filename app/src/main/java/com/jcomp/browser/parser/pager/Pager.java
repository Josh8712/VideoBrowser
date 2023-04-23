package com.jcomp.browser.parser.pager;

import android.util.Pair;

import java.util.List;

public class Pager {
    public List<Pair<String, String>> pages;
    int currentIndex;

    public Pager(List<Pair<String, String>> pages, int currentIndex) {
        this.pages = pages;
        this.currentIndex = currentIndex;
    }

    public int getCurrentIndex() {
        return currentIndex;
    }

    public String getPageURL(int position) {
        return pages.get(position).second;
    }

    public String getPageName(int position) {
        return pages.get(position).first;
    }

    public boolean hasPrevious() {
        return currentIndex > 0;
    }

    public boolean hasNext() {
        return currentIndex < pages.size() - 1;
    }

    public int getSize() {
        return pages.size();
    }

}
