package com.jcomp.browser.parser;

public interface ParserVideoCallback {
    void addHLS(String hls, String preview);

    void addPlayerList(String[] playerList);
}
