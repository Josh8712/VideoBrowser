package com.jcomp.browser.viewer.video_loader;

import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.jcomp.browser.R;
import com.jcomp.browser.browser.Browser;
import com.jcomp.browser.download.DownloadManager;
import com.jcomp.browser.download.db.DownloadPost;
import com.jcomp.browser.parser.post.db.Post;
import com.jcomp.browser.tools.HelperFunc;

import java.util.List;

public class LocalListLoader extends DownloaderLoader {

    List<DownloadPost> list;

    public LocalListLoader(Post post, ImageButton downloadButton) {
        super(post, downloadButton);
    }

    @Override
    protected void startAction() {
        new Thread(() -> {
            list = DownloadManager.getInstance(getContext()).getRecord(post.url);
            String[] playerList = new String[list.size()];
            for (int i = 0; i < playerList.length; i++)
                playerList[i] = list.get(i).getTitle();
            downloadButton.post(() -> {
                loadPlayer(playerList, null);
            });
        }).start();
    }

    @Override
    void showError() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        } else
            return;
        HelperFunc.showToast(getContext(), R.string.failed_to_load_video, Toast.LENGTH_LONG);
    }

    @Override
    public void loadPlayer(String[] playerList, Browser browser) {
        if (videoOptionDialog != null && videoOptionDialog.isShowing())
            return;
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
            if (playerList.length == 0) {
                HelperFunc.showToast(getContext(), R.string.download_not_found, Toast.LENGTH_LONG);
                return;
            }
            if (playerList.length == 1) {
                LocalLoader.playDownloaded(getContext(), list.get(0));
                return;
            }
            String[] selection = new String[playerList.length];
            for (int i = 0; i < selection.length; i++)
                selection[i] = list.get(i).getTitle();
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle(R.string.choose_video);
            builder.setItems(selection, null);
            videoOptionDialog = builder.create();
            videoOptionDialog.getListView().setOnItemClickListener((adapterView, view, idx, id) -> LocalLoader.playDownloaded(getContext(), list.get(idx)));
            videoOptionDialog.show();
        }
    }

}
