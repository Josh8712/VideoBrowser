package com.jcomp.browser.viewer.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.jcomp.browser.R;
import com.jcomp.browser.parser.model.Model;
import com.jcomp.browser.parser.post.db.Post;
import com.jcomp.browser.viewer.video_loader.ResourceLoader;

import java.util.LinkedHashMap;

public class PostAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    LinkedHashMap<String, Post> postList;
    CallBack openCallBack;
    View.OnClickListener modelListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
            builder.setTitle(R.string.choose_model);
            Post post = getItem((int) view.getTag());
            String[] models = new String[post.model.length];
            for (int i = 0; i < post.model.length; i++)
                models[i] = post.model[i].name;

            builder.setItems(models, (dialog, which) -> ((ModelCallBack) openCallBack).onModelClick(new ModelCallBackArgs(post.model[which])));
            AlertDialog dialog = builder.create();
            dialog.show();
        }
    };

    public PostAdapter(LinkedHashMap<String, Post> postList, CallBack openCallBack, Context context) {
        this.postList = postList;
        this.openCallBack = openCallBack;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == 1)
            return new TagViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_item_tag, parent, false));
        else if (viewType == 2)
            return new PostDownloadViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_item_download, parent, false));
        else if (viewType == 3)
            return new TagViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_item_image_tag, parent, false));
        return new PostVideoViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_item_post, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Post post = getItem(position);
        if (getItemViewType(position) == 1)
            ((TagViewHolder) holder).bind(post, openCallBack);
        else if (getItemViewType(position) == 2)
            ((PostDownloadViewHolder) holder).bind(post, openCallBack, this);
        else if (getItemViewType(position) == 3)
            ((TagViewHolder) holder).bind(post, openCallBack);
        else
            ((PostVideoViewHolder) holder).bind(post, openCallBack, this);
    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

    public Post getItem(int position) {
        if (position >= postList.size())
            return null;
        return (Post) postList.values().toArray()[position];
    }

    public int getItem(Post post) {
        Post[] data = postList.values().toArray(new Post[0]);
        for (int i = 0; i < data.length; i++)
            if (data[i].getKey().equals(post.getKey()))
                return i;
        return -1;
    }

    @Override
    public int getItemViewType(int position) {
        return getItem(position).getViewType();
    }

    public void reset() {
        int size = postList.size();
        postList.clear();
        notifyItemRangeRemoved(0, size);
    }


    interface CallBack {
        void onClick(CallBackArgs args);
    }

    public interface ModelCallBack extends CallBack {
        void onClick(CallBackArgs args);

        void onModelClick(CallBackArgs args);
        void onPlayListAdded(CallBackArgs args);
        void onPlayListRemoved(CallBackArgs args);

    }

    public static class CallBackArgs {

    }

    public static class VideoCallBackArgs extends CallBackArgs {
        public Post post;
        public ResourceLoader resourceLoader;
        public int postPos;

        public VideoCallBackArgs(Post post, ResourceLoader resourceLoader, int postPos) {
            this.post = post;
            this.resourceLoader = resourceLoader;
            this.postPos = postPos;
        }
    }

    public static class ModelCallBackArgs extends CallBackArgs {
        public Model model;

        public ModelCallBackArgs(Model model) {
            this.model = model;
        }
    }

    public static class PlayListCallBackArgs extends CallBackArgs {
        public Post post;
        public int postPos;

        public PlayListCallBackArgs(Post post, int postPos) {
            this.post = post;
            this.postPos = postPos;
        }
    }
}
