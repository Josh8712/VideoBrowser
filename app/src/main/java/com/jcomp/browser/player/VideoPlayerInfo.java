package com.jcomp.browser.player;

import com.jcomp.browser.comic.ComicPlayerInfo;
import com.jcomp.browser.parser.post.db.Post;
import com.jcomp.browser.player.preview.FileHandler;

public class VideoPlayerInfo extends PlayerInfo {

    public VideoPlayerInfo() {
        this.type = PlayerType.ONLINE;
    }

    public VideoPlayerInfo(String videoURL, String playerURL, String previewURL, Post post, PlayerType type) {
        this.videoURL = videoURL;
        this.playerURL = playerURL;
        this.previewURL = previewURL;
        this.post = post;
        this.type = type;
    }

    public String getKey() {
        return playerURL;
    }

    public PreviewHandler getPreviewer() {
        if (type == PlayerType.LOCAL)
            return new FileHandler(previewURL);
        return PreviewHandler.parseRequest(previewURL);
    }

    public void setPost(Post post) {
        this.post = post;
        this.playerURL = post.url;
    }

    public void setStream(String hls, String preview) {
        this.videoURL = hls;
        if (preview != null)
            this.previewURL = preview;
    }

    public boolean streamable() {
        return videoURL != null;
    }
}
