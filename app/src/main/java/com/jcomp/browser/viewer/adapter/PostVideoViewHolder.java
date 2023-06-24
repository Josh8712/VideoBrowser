package com.jcomp.browser.viewer.adapter;

import android.view.Gravity;
import android.view.Menu;
import android.view.View;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.jcomp.browser.AppDatabase;
import com.jcomp.browser.R;
import com.jcomp.browser.download.db.DownloadPost;
import com.jcomp.browser.download.db.DownloadPostDoa;
import com.jcomp.browser.menu.Popup;
import com.jcomp.browser.parser.post.db.PlaylistDoa;
import com.jcomp.browser.parser.post.db.Post;
import com.jcomp.browser.parser.post.db.PostWithPlayList;
import com.jcomp.browser.tools.HelperFunc;
import com.jcomp.browser.viewer.PlayListHandler;
import com.jcomp.browser.viewer.video_loader.ComicDownloaderLoader;
import com.jcomp.browser.viewer.video_loader.DownloaderLoader;
import com.jcomp.browser.viewer.video_loader.LocalListLoader;
import com.jcomp.browser.widget.BreathingAnim;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PostVideoViewHolder extends PostViewHolderBase {

    final ImageButton playlistButton, downloadButton;
    private final ExecutorService mFixedThreadPool;

    public PostVideoViewHolder(@NonNull View view) {
        super(view);
        playlistButton = view.findViewById(R.id.playlist_button);
        downloadButton = view.findViewById(R.id.download_button);
        id.setSelected(true);
        title.setSelected(true);
        id.setOnClickListener(view1 -> {
            HelperFunc.showToast(id.getContext(), id.getText(), Toast.LENGTH_SHORT);
        });
        title.setOnClickListener(view12 -> {
            HelperFunc.showToast(title.getContext(), title.getText(), Toast.LENGTH_SHORT);
        });
        mFixedThreadPool = Executors.newFixedThreadPool(2);
    }

    private void openPopup(View view, Post post, int position) {
        PopupMenu menu = new PopupMenu(view.getContext(), view, Gravity.END);
        menu.getMenu().add(Menu.NONE, 1, 1, R.string.open);
        menu.getMenu().add(Menu.NONE, Popup.browserID, 1, R.string.open_in_browser);
        menu.getMenu().add(Menu.NONE, Popup.copyID, 1, R.string.copy_url);
        menu.getMenu().add(Menu.NONE, Popup.shareID, 1, R.string.share_to);
        String url = post.url;
        menu.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case 1:
                    view.performClick();
                    break;
                case Popup.browserID:
                    Popup.browserCallback(view.getContext(), url);
                    break;
                case Popup.copyID:
                    Popup.copyToClipboard(view.getContext(), url);
                    break;
                case Popup.shareID:
                    Popup.shareCallback(view.getContext(), post.getTitle(), url);
                    break;
            }
            return true;
        });
        menu.show();
    }


    public void bind(Post post, PostAdapter.CallBack openCallBack, PostAdapter adapter) {
        super.bind(post, openCallBack, adapter);
        if (model.getVisibility() == View.INVISIBLE)
            model.setVisibility(View.GONE);
        playlistButton.setTag(post.getKey());
        playlistButton.setVisibility(View.INVISIBLE);
        downloadButton.setVisibility(View.INVISIBLE);
        mFixedThreadPool.execute(() -> {
            DownloadPostDoa db = AppDatabase.getInstance(itemView.getContext()).downloadPostDao();
            DownloadPost downloadPost = db.getByURL(post.url);
            itemView.post(() -> {
                if (playlistButton.getTag() != post.getKey())
                    return;
                updateDownloadButton(downloadPost);
            });
        });
        mFixedThreadPool.execute(() -> {
            List<PostWithPlayList> result = PlayListHandler.checkRecordByURL(post.url, itemView.getContext());
            itemView.post(() -> {
                if (playlistButton.getTag() != post.getKey())
                    return;
                playlistButton.setVisibility(View.VISIBLE);
                if (result.isEmpty()) {
                    playlistButton.setImageResource(R.drawable.baseline_playlist_add_24);
                    playlistButton.setColorFilter(itemView.getContext().getColor(R.color.gray));
                    playlistButton.setOnClickListener(e -> {
                        mFixedThreadPool.execute(() -> {
                            PlayListHandler.insertToDefaultPlayList(post, itemView.getContext());
                            itemView.post(() -> {
                                ((PostAdapter.ModelCallBack)openCallBack).onPlayListAdded(new PostAdapter.PlayListCallBackArgs(post, getAbsoluteAdapterPosition()));
                            });
                        });
                    });
                } else {
                    playlistButton.setImageResource(R.drawable.baseline_playlist_add_check_24);
                    playlistButton.setColorFilter(itemView.getContext().getColor(R.color.colorPrimary));
                    playlistButton.setOnClickListener(e -> {
                        mFixedThreadPool.execute(() -> {
                            if(result.size() == 1) {
                                PlayListHandler.remove(result.get(0), itemView.getContext());
                                itemView.post(() -> {
                                    ((PostAdapter.ModelCallBack)openCallBack).onPlayListRemoved(new PostAdapter.PlayListCallBackArgs(post, getAbsoluteAdapterPosition()));
                                });
                            } else {
                                itemView.post(() -> {
                                    PlayListHandler.managePlayList(itemView.getContext(), result.get(0), () -> {
                                        ((PostAdapter.ModelCallBack)openCallBack).onPlayListAdded(new PostAdapter.PlayListCallBackArgs(post, getAbsoluteAdapterPosition()));
                                    });
                                });
                            }
                        });
                    });
                }
            });
        });
        downloadButton.setOnClickListener(view -> {
            mFixedThreadPool.execute(() -> {
                DownloadPostDoa db = AppDatabase.getInstance(itemView.getContext()).downloadPostDao();
                DownloadPost showCheckPost = db.getByURL(post.url);
                itemView.post(() -> {
                    updateDownloadButton(showCheckPost);
                    downloadButton.setTag(post);
                    if((post.getViewType() & Post.TYPE_COMIC) == Post.TYPE_COMIC)
                        openCallBack.onClick(new PostAdapter.VideoCallBackArgs(post, new ComicDownloaderLoader(post, downloadButton), getAbsoluteAdapterPosition()));
                    else
                        openCallBack.onClick(new PostAdapter.VideoCallBackArgs(post, new DownloaderLoader(post, downloadButton), getAbsoluteAdapterPosition()));
                });
            });
        });
        playlistButton.setOnLongClickListener(item -> {
            PlayListHandler.managePlayList(itemView.getContext(), post, () -> {
                ((PostAdapter.ModelCallBack)openCallBack).onPlayListAdded(new PostAdapter.PlayListCallBackArgs(post, getAbsoluteAdapterPosition()));
            });
            return true;
        });
        downloadButton.setOnLongClickListener(item -> {
            openCallBack.onClick(new PostAdapter.VideoCallBackArgs(post, new LocalListLoader(post, downloadButton), getAbsoluteAdapterPosition()));
            return true;
        });
        itemView.setOnLongClickListener(view -> {
            openPopup(view, post, getAbsoluteAdapterPosition());
            return true;
        });
    }

    public void updateDownloadButton(DownloadPost downloadPost) {
        BreathingAnim.clear(downloadButton);
        downloadButton.setVisibility(View.VISIBLE);
        downloadButton.setColorFilter(itemView.getContext().getColor(R.color.colorPrimary));
        if (downloadPost == null) {
            downloadButton.setImageResource(R.drawable.ic_baseline_download_24);
            downloadButton.setColorFilter(itemView.getContext().getColor(R.color.gray));
        } else if (downloadPost.status == DownloadPost.Status.FINISHED) {
            downloadButton.setImageResource(R.drawable.baseline_check_24);
        } else {
            if (downloadPost.status != DownloadPost.Status.PAUSE)
                BreathingAnim.breath(downloadButton, 0.2f, 1f, 1000);
            downloadButton.setImageResource(R.drawable.ic_baseline_download_24);
        }
    }
}

