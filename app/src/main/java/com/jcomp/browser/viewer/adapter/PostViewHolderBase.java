package com.jcomp.browser.viewer.adapter;

import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.jcomp.browser.R;
import com.jcomp.browser.parser.post.db.Post;
import com.jcomp.browser.viewer.video_loader.VideoLoader;
import com.squareup.picasso.Picasso;

public abstract class PostViewHolderBase extends RecyclerView.ViewHolder {
    final Button model;
    final ImageView pic;
    final TextView title, id;

    public PostViewHolderBase(@NonNull View view) {
        super(view);
        pic = view.findViewById(R.id.pic);
        title = view.findViewById(R.id.title);
        id = view.findViewById(R.id.id);
        model = view.findViewById(R.id.model);
    }

    public void bind(Post post, PostAdapter.CallBack openCallBack, PostAdapter adapter) {
        if (post.img == null || post.img.isEmpty()) {
            pic.setImageResource(R.drawable.gallery_placeholder);
        } else {
            Picasso.get().load(post.img).placeholder(R.drawable.gallery_placeholder).error(R.drawable.gallery_placeholder).into(pic);
        }

        if (post.getTitle() != null) {
            id.setVisibility(View.VISIBLE);
            title.setVisibility(View.VISIBLE);

            String vid = post.getVideoID();
            id.setText(vid);
            String t = post.getTitle();
            if (vid != null)
                t = t.replaceAll(vid, "").trim();
            title.setText(t);
        } else {
            id.setVisibility(View.INVISIBLE);
            title.setVisibility(View.INVISIBLE);
        }

        if (post.model != null && post.model.length > 0) {
            model.setVisibility(View.VISIBLE);
            String title = post.model[0].name;
            if (post.model.length > 1) {
                title += "(" + post.model.length + ")";
                model.setTag(getAbsoluteAdapterPosition());
                model.setOnClickListener(adapter.modelListener);
            } else
                model.setOnClickListener(view -> ((PostAdapter.ModelCallBack) openCallBack).onModelClick(new PostAdapter.ModelCallBackArgs(post.model[0])));
            model.setText(title);
        } else
            model.setVisibility(View.INVISIBLE);


        itemView.setOnClickListener(view -> {
            openCallBack.onClick(new PostAdapter.VideoCallBackArgs(post, new VideoLoader(post), getAbsoluteAdapterPosition()));
        });
    }
}

