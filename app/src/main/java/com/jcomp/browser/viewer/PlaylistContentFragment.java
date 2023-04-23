package com.jcomp.browser.viewer;

import androidx.fragment.app.FragmentActivity;

import com.jcomp.browser.AppDatabase;
import com.jcomp.browser.parser.model.ModelCache;
import com.jcomp.browser.parser.post.db.PlaylistDoa;
import com.jcomp.browser.parser.post.db.Post;

import java.util.LinkedHashMap;
import java.util.List;

public class PlaylistContentFragment extends VideoParserFragment {
    @Override
    public void addPost(LinkedHashMap<String, Post> postList) {
        // ignore online posts
    }

    public void addLocalPost(LinkedHashMap<String, Post> postList) {
        super.addPost(postList);
    }

    @Override
    public void load(String url) {
        if (url == null)
            return;
        if (url.equals(rootURL)) {
            if (postList.isEmpty())
                refreshLayout.postDelayed(() -> {
                    new Thread(this::localLoad).start();
                }, 200);
            else
                new Thread(this::localLoad).start();
        } else
            super.load(url);
    }

    protected void localLoad() {
        if (getContext() == null)
            return;
        PlaylistDoa db = AppDatabase.getInstance(getContext()).playlistDoa();
        List<Post> data = db.getAllInPlaylist(Integer.parseInt(rootURL));
        LinkedHashMap<String, Post> posts = new LinkedHashMap<>();
        for (Post post : data) {
            post.set_model(ModelCache.getSingleton(getContext()).getModel(post.getKey()));
            posts.put(String.valueOf(post.uid), post);
        }
        FragmentActivity activity = getActivity();
        if (activity == null)
            return;
        activity.runOnUiThread(() -> {
            addLocalPost(posts);
            parseFinished();
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        loadMore();
    }

    @Override
    protected boolean addToTop() {
        return true;
    }
}
