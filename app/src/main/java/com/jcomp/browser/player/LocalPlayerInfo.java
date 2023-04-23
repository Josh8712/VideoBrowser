package com.jcomp.browser.player;

import android.net.Uri;

import com.jcomp.browser.download.PreviewDownloadHandler;
import com.jcomp.browser.download.db.DownloadPost;

import java.io.File;

public class LocalPlayerInfo extends PlayerInfo {
    public LocalPlayerInfo(DownloadPost post, String indexName) {
        super(Uri.fromFile(new File(post.localPath + indexName)).getPath(), post.playerPath,
                post.localPath + PreviewDownloadHandler.PREVIEW_FILENAME, post, PlayerInfo.PlayerType.LOCAL);
    }
}
