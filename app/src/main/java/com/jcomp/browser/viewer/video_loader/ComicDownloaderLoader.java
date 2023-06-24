package com.jcomp.browser.viewer.video_loader;

import android.content.Context;
import android.widget.ImageButton;
import android.widget.Toast;

import com.jcomp.browser.R;
import com.jcomp.browser.comic.ComicPlayerInfo;
import com.jcomp.browser.download.DownloadManager;
import com.jcomp.browser.download.db.DownloadPost;
import com.jcomp.browser.parser.post.db.Post;
import com.jcomp.browser.player.PlayerInfo;
import com.jcomp.browser.tools.HelperFunc;
import com.jcomp.browser.viewer.ViewerFragmentBase;
import com.jcomp.browser.widget.BreathingAnim;

public class ComicDownloaderLoader extends ResourceLoader {
    ImageButton downloadButton;

    public ComicDownloaderLoader(Post post, ImageButton downloadButton) {
        super(post);
        this.downloadButton = downloadButton;
    }

    @Override
    public void start(ViewerFragmentBase postFragment) {
        PlayerInfo playerInfo = new ComicPlayerInfo(post, ComicPlayerInfo.PlayerType.ONLINE);
        new Thread(() -> {
            DownloadManager manager = DownloadManager.getInstance(getContext());
            DownloadPost downloadPost = manager.getRecord(playerInfo);
            if (downloadPost == null)
                manager.download(playerInfo);
            else if (downloadPost.status == DownloadPost.Status.PAUSE) {
                manager.download(downloadPost);
            }
            downloadButton.post(() -> {
                Object tag = downloadButton.getTag();
                if (tag == post) {
                    if (downloadPost == null || downloadPost.status != DownloadPost.Status.FINISHED) {
                        downloadButton.setColorFilter(downloadButton.getContext().getColor(R.color.colorPrimary));
                        HelperFunc.showToast(getContext(), com.google.android.exoplayer2.core.R.string.exo_download_downloading, Toast.LENGTH_LONG);
                        BreathingAnim.breath(downloadButton, 0.2f, 1, 1000);
                    } else
                        HelperFunc.showToast(getContext(), R.string.already_download, Toast.LENGTH_LONG);
                }
            });
        }).start();
    }

    private Context getContext() {
        return downloadButton.getContext();
    }

    @Override
    void showError() {

    }
}
