package com.jcomp.browser.viewer.adapter;

import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.jcomp.browser.R;
import com.jcomp.browser.download.DownloadManager;
import com.jcomp.browser.download.db.DownloadPost;
import com.jcomp.browser.parser.post.db.Post;
import com.jcomp.browser.tools.HelperFunc;
import com.jcomp.browser.viewer.video_loader.LocalLoader;
import com.jcomp.browser.viewer.video_loader.VideoLoader;

import java.text.DecimalFormat;

public class PostDownloadViewHolder extends PostViewHolderBase {

    final ProgressBar progress;
    final TextView progress_text, status, speed;
    final Button pause;
    final ImageButton delete;
    final LinearLayout downloadGroup;
    final View separator;

    public PostDownloadViewHolder(@NonNull View view) {
        super(view);
        progress = view.findViewById(R.id.progress);
        progress_text = view.findViewById(R.id.progress_text);
        status = view.findViewById(R.id.status);
        delete = view.findViewById(R.id.delete);
        pause = view.findViewById(R.id.pause);
        speed = view.findViewById(R.id.speed);
        downloadGroup = view.findViewById(R.id.download_group);
        separator = view.findViewById(R.id.sep);
    }

    public void bind(Post downloadItem, PostAdapter.CallBack openCallBack, PostAdapter adapter) {
        DownloadPost post = (DownloadPost) downloadItem;
        int position = getAbsoluteAdapterPosition();
        super.bind(post, openCallBack, adapter);
        itemView.setOnLongClickListener(item -> {
            HelperFunc.showToast(itemView.getContext(), post.getTitle(), Toast.LENGTH_SHORT);
            return true;
        });
        if (getAbsoluteAdapterPosition() == 0)
            separator.setVisibility(View.GONE);
        else
            separator.setVisibility(View.VISIBLE);
        status.setVisibility(View.VISIBLE);
        progress.setVisibility(View.INVISIBLE);
        progress_text.setVisibility(View.INVISIBLE);
        speed.setVisibility(View.INVISIBLE);
        delete.setEnabled(true);
        delete.setVisibility(View.VISIBLE);
        downloadGroup.setVisibility(View.VISIBLE);
        pause.setVisibility(View.VISIBLE);
        switch (post.status) {
            case DELETED:
            case REMOVING:
                delete.setVisibility(View.INVISIBLE);
                downloadGroup.setVisibility(View.GONE);
                break;
            case RUNNING:
                if (post.speed < 0) {
                    speed.setVisibility(View.INVISIBLE);
                } else {
                    speed.setVisibility(View.VISIBLE);
                    speed.setText(HelperFunc.humanReadableByteCountBin(post.speed) + "/s");
                }
            case PAUSE:
            case FAILED:
            case PENDING:
                progress.setVisibility(View.VISIBLE);
                progress_text.setVisibility(View.VISIBLE);
                if (post.progress < 0) {
                    progress.setIndeterminate(post.status == DownloadPost.Status.RUNNING);
                    progress_text.setText(null);
                } else {
                    progress.setIndeterminate(false);
                    DecimalFormat df = new DecimalFormat();
                    df.setMaximumFractionDigits(2);
                    df.setMinimumFractionDigits(2);
                    progress.setProgress((int) post.progress);
                    progress_text.setText(df.format(post.progress) + "%");
                }
                break;
            case FINISHED:
                downloadGroup.setVisibility(View.GONE);
                status.setVisibility(View.GONE);
                break;
        }
        itemView.setOnClickListener(view -> {
            new Thread(() -> {
                DownloadPost latestPost = DownloadManager.getInstance(itemView.getContext()).getRecord(post);
                if (latestPost == null)
                    post.status = DownloadPost.Status.DELETED;
                else
                    post.status = latestPost.status;
                itemView.post(() -> {
                    adapter.notifyItemChanged(position);
                });
            }).start();
            if (post.status == DownloadPost.Status.DELETED)
                openCallBack.onClick(new PostAdapter.VideoCallBackArgs(post, new VideoLoader(post), getAbsoluteAdapterPosition()));
            else
                openCallBack.onClick(new PostAdapter.VideoCallBackArgs(post, new LocalLoader(post), getAbsoluteAdapterPosition()));
        });
        // status
        switch (post.status) {
            case DELETED:
                status.setText(R.string.removed);
                break;
            case REMOVING:
                status.setText(com.google.android.exoplayer2.core.R.string.exo_download_removing);
                break;
            case FAILED:
                status.setText(R.string.wait_retry);
                break;
            case PAUSE:
                status.setText(com.google.android.exoplayer2.core.R.string.exo_download_paused);
                break;
            case RUNNING:
                status.setText(com.google.android.exoplayer2.core.R.string.exo_download_downloading);
                break;
            case PENDING:
                status.setText(R.string.pending);
                break;
            case FINISHED:
                status.setText(com.google.android.exoplayer2.core.R.string.exo_download_completed);
                break;
        }

        //delete
        delete.setOnClickListener(itemView -> {
            delete.setEnabled(false);
            post.status = DownloadPost.Status.REMOVING;
            adapter.notifyItemChanged(getAbsoluteAdapterPosition(), post);
            int pos = getAbsoluteAdapterPosition();
            new Thread(() -> {
                boolean success = DownloadManager.getInstance(itemView.getContext()).deleteJob(post);
                if (success)
                    post.status = DownloadPost.Status.DELETED;
                else
                    post.status = DownloadPost.Status.FAILED;
                delete.post(() -> {
                    adapter.notifyItemChanged(pos);
                });
            }).start();
        });
        // pause/start
        pause.setEnabled(true);
        switch (post.status) {
            case PAUSE:
                pause.setText(itemView.getContext().getString(R.string.resume));
                pause.setOnClickListener(view -> {
                    pause.setEnabled(false);
                    post.status = DownloadPost.Status.PENDING;
                    new Thread(() -> {
                        if (!DownloadManager.getInstance(itemView.getContext()).download(post))
                            post.status = DownloadPost.Status.FAILED;
                        pause.post(() -> {
                            adapter.notifyItemChanged(getAbsoluteAdapterPosition());
                        });
                    }).start();
                });
                break;
            default:
                pause.setText(itemView.getContext().getString(R.string.pause));
                pause.setOnClickListener(view -> {
                    pause.setEnabled(false);
                    post.status = DownloadPost.Status.PAUSE;
                    new Thread(() -> {
                        DownloadManager.getInstance(itemView.getContext()).pauseJob(post);
                        pause.post(() -> {
                            adapter.notifyItemChanged(getAbsoluteAdapterPosition());
                        });
                    }).start();
                });
                break;
        }
    }
}

