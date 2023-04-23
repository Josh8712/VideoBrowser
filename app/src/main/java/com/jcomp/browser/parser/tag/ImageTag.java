package com.jcomp.browser.parser.tag;

import androidx.annotation.NonNull;

public class ImageTag extends Tag {
    public ImageTag(String title, String img, @NonNull String url) {
        super(title, img, url);
        showScale = 2;
        viewType = 3;
    }
}
