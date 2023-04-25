package com.jcomp.browser.viewer.video_loader;

import android.content.Context;
import android.content.Intent;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import com.jcomp.browser.R;
import com.jcomp.browser.browser.Browser;
import com.jcomp.browser.download.DownloadTask;
import com.jcomp.browser.download.db.DownloadPost;
import com.jcomp.browser.parser.post.db.Post;
import com.jcomp.browser.player.LocalPlayerInfo;
import com.jcomp.browser.player.Player;
import com.jcomp.browser.player.PlayerInfo;
import com.jcomp.browser.splash.Splash;
import com.jcomp.browser.tools.HelperFunc;
import com.jcomp.browser.tools.IO;
import com.jcomp.browser.viewer.ViewerFragmentBase;

public class LocalLoader extends ResourceLoader {

    public LocalLoader(Post post) {
        super(post);
    }

    public static Intent getPlayDownloadedIntent(Context context, DownloadPost post, Class targetClass) {
        try {
            String content = IO.getStringFromFile(post.localPath + DownloadTask.DOWNLOAD_INFO_FILENAME);
            DownloadTask.StorageInfo storageInfo = new Gson().fromJson(content, DownloadTask.StorageInfo.class);
            PlayerInfo info = new LocalPlayerInfo(post, storageInfo.indexName);
            Intent intent = new Intent(context, targetClass);
            intent.putExtra(Player.PLAYER_INFO_KEY, new Gson().toJson(info));
            return intent;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void playDownloaded(Context context, DownloadPost post) {
        Intent intent = getPlayDownloadedIntent(context, post, Player.class);
        if (intent == null) {
            HelperFunc.showToast(context, R.string.failed_to_play_video);
            return;
        }
        context.startActivity(intent);
    }

    @Override
    public void start(ViewerFragmentBase postFragment) {
        playDownloaded(postFragment.getContext(), (DownloadPost) post);
    }

    @Override
    void showError() {

    }

    public void loadHLS(String hls, String preview) {

    }

    public void loadPlayer(String[] playerList, Browser browser) {

    }

    @Override
    public void parseFinished() {

    }
}
