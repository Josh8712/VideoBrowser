package com.jcomp.browser.viewer.video_loader;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.google.gson.Gson;
import com.jcomp.browser.R;
import com.jcomp.browser.browser.Browser;
import com.jcomp.browser.parser.post.db.Post;
import com.jcomp.browser.player.Player;
import com.jcomp.browser.player.PlayerInfo;
import com.jcomp.browser.tools.HelperFunc;
import com.jcomp.browser.viewer.ViewerFragmentBase;

public class VideoLoader extends ResourceLoader {

    AlertDialog videoOptionDialog;
    int steps = 0;
    private ViewerFragmentBase postFragment;
    private String streamName;

    public VideoLoader(Post post) {
        super(post);
    }

    Context getContext() {
        return postFragment.getContext();
    }

    @Override
    public void start(ViewerFragmentBase postFragment) {
        this.postFragment = postFragment;
        progressDialog = new ProgressDialog(postFragment.getContext());
        progressDialog.setTitle(getContext().getString(R.string.loading));
        progressDialog.setMessage(title);
        progressDialog.setCancelable(true);
        progressDialog.setOnCancelListener((view) -> {
            active = false;
        });
        progressDialog.show();
        startAction();
    }


    protected void startAction() {
        postFragment.load(url);
    }

    @Override
    void showError() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        } else
            return;
        HelperFunc.showToast(getContext(), R.string.failed_to_play_video, Toast.LENGTH_SHORT);
    }

    public void loadHLS(String hls, String preview) {
        playVideo(hls, preview);
    }

    public void loadPlayer(String[] playerList, Browser browser) {
        if (videoOptionDialog != null && videoOptionDialog.isShowing())
            return;
        if (steps > 0)
            return;
        steps = 1;
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
            if (playerList.length == 1) {
                openPlayer(playerList[0], browser);
                return;
            }
            String[] selection = new String[playerList.length];
            for (int i = 0; i < selection.length; i++)
                selection[i] = (i + 1) + " - " + HelperFunc.getDomainName(playerList[i]);
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle(R.string.choose_video);
            builder.setItems(selection, null);
            videoOptionDialog = builder.create();
            videoOptionDialog.getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int idx, long id) {
                    streamName = " - " + selection[idx];
                    openPlayer(playerList[idx], browser);
                }
            });
            videoOptionDialog.show();
        }
    }

    @Override
    public void parseFinished() {
        showError();
    }

    private void openPlayer(String url, Browser browser) {
        playerURL = url;
        progressDialog = new ProgressDialog(getContext());
        progressDialog.setTitle(getContext().getString(R.string.loading));
        progressDialog.setMessage(post.getTitle());
        progressDialog.setCancelable(true);
        progressDialog.show();
        active = true;
        browser.loadUrl(url, false);
    }

    void playVideo(String videoURL, String previewURL) {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
            post.setStreamName(streamName);
            callback(videoURL, previewURL);
            active = false;
        }
    }

    void callback(String videoURL, String previewURL) {
        PlayerInfo info = new PlayerInfo(videoURL, playerURL, previewURL, post, PlayerInfo.PlayerType.ONLINE);
        Intent intent = new Intent(getContext(), Player.class);
        intent.putExtra(Player.PLAYER_INFO_KEY, new Gson().toJson(info));
        postFragment.startActivity(intent);
    }
}
