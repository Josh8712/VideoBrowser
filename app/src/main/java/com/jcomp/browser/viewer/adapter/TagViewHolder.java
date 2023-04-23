package com.jcomp.browser.viewer.adapter;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.jcomp.browser.R;
import com.jcomp.browser.parser.post.db.Post;
import com.jcomp.browser.viewer.video_loader.VideoLoader;
import com.squareup.picasso.Picasso;

public class TagViewHolder extends RecyclerView.ViewHolder {
    final TextView title;
    final ImageView img;

    public TagViewHolder(@NonNull View view) {
        super(view);
        title = view.findViewById(R.id.title);
        img = view.findViewById(R.id.img);
    }

    public void bind(Post tag, PostAdapter.CallBack openCallBack) {
        title.setText(tag.getTitle());
        if (img != null)
            Picasso.get().load(tag.img).into(img);
        View.OnClickListener callback = e -> {
            openCallBack.onClick(new PostAdapter.VideoCallBackArgs(tag, new VideoLoader(tag), getAbsoluteAdapterPosition()));
        };
        itemView.setOnClickListener(callback);
    }
}

