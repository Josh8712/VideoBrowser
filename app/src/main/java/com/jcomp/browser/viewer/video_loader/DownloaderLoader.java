package com.jcomp.browser.viewer.video_loader;

import android.widget.ImageButton;
import android.widget.Toast;

import com.google.gson.Gson;
import com.jcomp.browser.R;
import com.jcomp.browser.download.DownloadManager;
import com.jcomp.browser.download.db.DownloadPost;
import com.jcomp.browser.parser.post.db.Post;
import com.jcomp.browser.player.PlayerInfo;
import com.jcomp.browser.tools.HelperFunc;
import com.jcomp.browser.widget.BreathingAnim;

public class DownloaderLoader extends VideoLoader {
    ImageButton downloadButton;

    public DownloaderLoader(Post post, ImageButton downloadButton) {
        super(post);
        this.downloadButton = downloadButton;
    }

    @Override
    void showError() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        } else
            return;
        HelperFunc.showToast(getContext(), R.string.failed_to_load_video, Toast.LENGTH_SHORT);
    }

    @Override
    void callback(String videoURL, String previewURL) {
        PlayerInfo playerInfo = new PlayerInfo(videoURL, playerURL, previewURL, new Gson().fromJson(new Gson().toJson(post), Post.class), PlayerInfo.PlayerType.ONLINE);
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
}
