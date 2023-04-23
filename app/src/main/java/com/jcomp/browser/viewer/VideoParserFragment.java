package com.jcomp.browser.viewer;

import com.jcomp.browser.parser.ParserVideoCallback;
import com.jcomp.browser.parser.model.ModelCache;
import com.jcomp.browser.parser.post.db.Post;

import java.util.LinkedHashMap;

abstract public class VideoParserFragment extends ViewerFragmentBase implements ParserVideoCallback {
    @Override
    protected void setupCallback() {
        super.setupCallback();
        browser.registerCallback((ParserVideoCallback) this);
    }

    @Override
    public void load(String url) {
        browser.loadUrl(url, true);
    }

    @Override
    public void onResume() {
        super.onResume();
        updateSelectedModel();
    }

    private void updateSelectedModel() {
        if (selectedPosition < 0)
            return;
        if (selectedPosition < adapter.getItemCount()) {
            Post post = adapter.getItem(selectedPosition);
            if (post.model == null) {
                post.model = ModelCache.getSingleton(getContext()).getModel(post.getKey());
                adapter.notifyItemChanged(selectedPosition);
            }
        }
        selectedPosition = -1;
    }

    @Override
    public void addPost(LinkedHashMap<String, Post> postList) {
        if (postList.isEmpty())
            return;
        LinkedHashMap<String, Post> newPostList = new LinkedHashMap<>(this.postList);
        newPostList.putAll(postList);
        refreshLayout.post(() -> {
            refreshLayout.setRefreshing(false);
            postFragmentModelView.getmPost().setValue(newPostList);
        });
    }

    @Override
    public void addHLS(String hls, String preview) {
        if (resourceLoader != null && resourceLoader.isActive()) {
            refreshLayout.post(() -> resourceLoader.loadHLS(hls, preview));
        }
    }

    @Override
    public void addPlayerList(String[] playerList) {
        if (resourceLoader != null && resourceLoader.isActive()) {
            refreshLayout.post(() -> resourceLoader.loadPlayer(playerList, browser));
        }
    }
}
