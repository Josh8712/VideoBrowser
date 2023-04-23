package com.jcomp.browser.viewer;

import androidx.lifecycle.LiveData;
import androidx.work.WorkInfo;

import com.jcomp.browser.AppDatabase;
import com.jcomp.browser.download.DownloadManager;
import com.jcomp.browser.download.db.DownloadPost;
import com.jcomp.browser.download.db.DownloadPostDoa;
import com.jcomp.browser.parser.model.ModelCache;
import com.jcomp.browser.parser.post.db.Post;

import java.util.LinkedHashMap;
import java.util.List;

public class DownloadFragment extends PlaylistContentFragment {

    private int observerIndicator = 0;

    @Override
    protected void localLoad() {
        if (getContext() == null)
            return;
        final int observerChecker = observerIndicator;
        DownloadPostDoa db = AppDatabase.getInstance(getContext()).downloadPostDao();
        DownloadManager manager = DownloadManager.getInstance(getContext());
        List<DownloadPost> list = db.getAll();
        if (list.isEmpty()) {
            parseFinished();
            return;
        }
        list.replaceAll(downloadPost -> DownloadManager.getInstance(getContext()).validateRecord(downloadPost));
        LinkedHashMap<String, Post> result = new LinkedHashMap<>();
        for (int i = 0; i < list.size(); i++) {
            DownloadPost post = list.get(i);
            if (postList.containsKey(post.getKey()))
                continue;
            post.model = ModelCache.getSingleton(getContext()).getModel(post.getKey());
            result.put(post.getKey(), post);
        }
        LinkedHashMap<String, Post> merge = new LinkedHashMap<>();
        merge.putAll(result);
        merge.putAll(postList);
        binding.content.post(() -> addLocalPost(result));
        for (Post value : merge.values()) {
            DownloadPost post = (DownloadPost) value;
            if (post.status == DownloadPost.Status.FINISHED)
                continue;
            LiveData<List<WorkInfo>> worker = manager.getWorker(post, getContext());
            binding.content.post(() -> {
                if (worker != null) {
                    worker.removeObservers(getViewLifecycleOwner());
                    worker.observe(getViewLifecycleOwner(), (workInfos) -> {
                        final int finalI = adapter.getItem(post);
                        if (finalI < 0)
                            return;
                        if (workInfos.isEmpty() || observerChecker != observerIndicator)
                            return;
                        boolean finished = true;
                        for (WorkInfo workInfo : workInfos) {
                            if (workInfo.getState().isFinished())
                                continue;
                            finished = false;
                            if (workInfo.getState() == WorkInfo.State.RUNNING) {
                                if (!workInfo.getProgress().getKeyValueMap().isEmpty())
                                    post.status = DownloadPost.Status.RUNNING;
                            } else if (workInfo.getState() == WorkInfo.State.ENQUEUED)
                                if (post.status != DownloadPost.Status.PENDING)
                                    post.status = DownloadPost.Status.FAILED;
                            if (workInfo.getProgress().getBoolean("failed", false)) {
                                post.status = DownloadPost.Status.FAILED;
                            } else {
                                post.setSpeed(workInfo.getProgress().getInt("speed", -1));
                                post.setProgress(workInfo.getProgress().getFloat("progress", -1));
                            }
                        }
                        if (finished && post.status != DownloadPost.Status.FINISHED) {
                            new Thread(() -> {
                                DownloadPost newPost = manager.getRecord(post);
                                if (newPost == null)
                                    post.status = DownloadPost.Status.DELETED;
                                else
                                    post.status = newPost.status;
                                binding.content.post(() -> {
                                    if (observerChecker == observerIndicator)
                                        if (adapter.getItem(finalI) == post)
                                            adapter.notifyItemChanged(finalI, post);
                                });
                            }).start();
                        } else
                            adapter.notifyItemChanged(finalI, post);
                    });
                }
            });
        }
    }

    @Override
    protected void reload() {
        super.reload();
        observerIndicator += 1;
    }
}
