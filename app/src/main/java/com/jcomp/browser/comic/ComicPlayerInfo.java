package com.jcomp.browser.comic;

import com.jcomp.browser.parser.post.db.Post;
import com.jcomp.browser.player.PlayerInfo;

public class ComicPlayerInfo extends PlayerInfo {
    public ComicPlayerInfo(Post post, PlayerType type) {
        super(post, type);
    }

    public String getKey() {
        return post.getKey();
    }
}
