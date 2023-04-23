package com.jcomp.browser.download.db;

import androidx.room.Entity;
import androidx.room.Ignore;

import com.jcomp.browser.parser.post.db.Post;
import com.jcomp.browser.player.PlayerInfo;

@Entity
public class DownloadPost extends Post {

    public String localPath;
    public String playerPath;
    public String videoPath;
    public String previewPath;


    public long jobCounter;
    public Status status;

    public float progress = -1;
    @Ignore
    public int speed = -1;

    public DownloadPost() {
        super();
    }

    @Ignore
    public DownloadPost(PlayerInfo playerInfo, String localPath) {
        super(playerInfo.post.title, playerInfo.post.streamName, playerInfo.post.img, playerInfo.post.url);
        this.localPath = localPath;
        this.playerPath = playerInfo.playerURL;
        this.videoPath = playerInfo.videoURL;
        this.previewPath = playerInfo.previewURL;
        this.status = Status.PENDING;
        viewType = 2;
    }

    public void setProgress(float progress) {
        this.progress = progress;
    }

    public void setSpeed(int speed) {
        this.speed = speed;
    }

    @Override
    public String getKey() {
        return String.valueOf(uid);
    }

    public enum Status {
        PENDING, RUNNING, PAUSE, FINISHED, REMOVING, DELETED, FAILED
    }
}
