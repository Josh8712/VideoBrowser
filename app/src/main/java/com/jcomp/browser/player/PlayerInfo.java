package com.jcomp.browser.player;

import com.jcomp.browser.parser.post.db.Post;

public class PlayerInfo {
    public Post post;
    public PlayerType type;
    public String videoURL, playerURL, previewURL;

    public PlayerInfo() {
        this.type = PlayerType.ONLINE;
    }

    public PlayerInfo(Post post, PlayerType type) {
        this.post = post;
        this.type = type;
    }

    public String getKey() {
        return post.getKey();
    }


    public enum PlayerType {
        LOCAL, ONLINE
    }
}
