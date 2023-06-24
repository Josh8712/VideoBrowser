package com.jcomp.browser.comic;

import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.jcomp.browser.R;
import com.squareup.picasso.Callback;
import com.squareup.picasso.LruCache;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.OkHttp3Downloader;
import com.squareup.picasso.Picasso;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.net.URL;
import java.util.List;
import java.util.concurrent.ExecutorService;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ComicAdapter extends RecyclerView.Adapter<ComicAdapter.ComicViewHolder>{
    List<String> comicList;
    ExecutorService mFixedThreadPool;
    String rootUrl;

    public ComicAdapter(List<String> comicList, String url, ExecutorService mFixedThreadPool) {
        this.comicList = comicList;
        this.rootUrl = url;
        this.mFixedThreadPool = mFixedThreadPool;
    }

    @NonNull
    @Override
    public ComicViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ComicViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_item_comic_image, parent, false));
    }
    Picasso picasso;
    @Override
    public void onBindViewHolder(@NonNull ComicViewHolder holder, int position) {
        holder.setRecyclerView(recyclerView);
        ImageView root = (ImageView) holder.imageView;
        String image = comicList.get(position);
        root.setImageResource(R.drawable.gallery_placeholder);
        holder.progressBar.setVisibility(View.VISIBLE);
        if(image != null && !image.isEmpty()) {
            if(picasso == null) {
                OkHttpClient client = new OkHttpClient.Builder()
                        .addInterceptor(chain -> {
                            Request newRequest = chain.request().newBuilder()
                                    .addHeader("referer", rootUrl)
                                    .build();
                            int tryCount = 0;
                            Response response = null;
                            while (tryCount < 3 && (response == null || !response.isSuccessful())) {
                                tryCount++;
                                response = chain.proceed(newRequest);
                            }
                            return response;
                        }).retryOnConnectionFailure(true)
                        .build();
                picasso = new Picasso.Builder(root.getContext()).downloader(new OkHttp3Downloader(client)).build();
            }
            picasso.load(image).placeholder(R.drawable.gallery_placeholder).into(root, new Callback() {
                @Override
                public void onSuccess() {
                    holder.progressBar.setVisibility(View.GONE);
                }

                @Override
                public void onError(Exception e) {
                    if(holder.getAbsoluteAdapterPosition() < 0)
                        return;
                    comicList.set(holder.getAbsoluteAdapterPosition(), null);
                    notifyItemChanged(holder.getAbsoluteAdapterPosition());
                }
            });
        }
        else {
            download(position, 1, root);
            download(position-5, 10, root);
        }

    }

    private void download(int position, int limit, View root) {
        if(limit <= 0 || position >= comicList.size() || position < 0)
            return;
        mFixedThreadPool.execute(()->{
            synchronized (comicList) {
                if (comicList.get(position) != null) {
                    download(position + 1, limit - 1, root);
                    return;
                }
                comicList.set(position, "");
            }
            try {
                Document html = Jsoup.parse(new URL(this.rootUrl + "/" + (position + 1)), 5000);
                String url = html.selectFirst(".entry-content img").attr("src");
                comicList.set(position, url);
                root.post(() -> notifyItemChanged(position));
            } catch (Exception ignore) {
                comicList.set(position, null);
                root.postDelayed(() -> notifyItemChanged(position), 3000);
            }
            download(position + 1, limit - 1, root);
        });
    }

    ZoomableRecyclerView recyclerView;
    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        this.recyclerView = (ZoomableRecyclerView) recyclerView;
    }

    @Override
    public int getItemCount() {
        return comicList.size();
    }

    static class ComicViewHolder extends RecyclerView.ViewHolder {
        ZoomableRecyclerView recyclerView;
        ImageView imageView;
        ProgressBar progressBar;
        public ComicViewHolder(@NonNull View itemView) {
            super(itemView);
            itemView.setOnClickListener(v -> {
                if(progressBar.getVisibility() == View.GONE)
                    return;
                RecyclerView.Adapter<? extends RecyclerView.ViewHolder> adapter = getBindingAdapter();
                if (adapter == null)
                    return;
                adapter.notifyItemChanged(getAbsoluteAdapterPosition());
            });
            imageView = itemView.findViewById(R.id.image);
            progressBar = itemView.findViewById(R.id.progress);
        }

        public void setRecyclerView(ZoomableRecyclerView recyclerView) {
            this.recyclerView = recyclerView;
        }
    }
}
