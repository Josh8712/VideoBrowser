package com.jcomp.browser.comic;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.NumberPicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.gson.Gson;
import com.jcomp.browser.R;
import com.jcomp.browser.databinding.ActivityComicViewerBinding;
import com.jcomp.browser.player.Player;
import com.jcomp.browser.player.VideoPlayerInfo;
import com.jcomp.browser.tools.HelperFunc;
import com.jcomp.browser.viewer.PlayListHandler;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ComicPlayer extends AppCompatActivity {
    public static final String COMIC_INFO_KEY = "COMIC_INFO_KEY";
    private ComicPlayerInfo playerInfo;
    private ExecutorService mFixedThreadPool;
    private ArrayList<String> pics = new ArrayList<>();
    private ActivityComicViewerBinding binding;


    static class JobQueue extends LinkedBlockingDeque<Runnable> {
        @Override
        public Runnable poll(long timeout, TimeUnit unit) throws InterruptedException {
            return super.pollLast(timeout, unit);
        }

        @Override
        public Runnable poll() {
            return super.pollLast();
        }

        @Override
        public Runnable take() throws InterruptedException {
            return super.takeLast();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityComicViewerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (getIntent() == null || getIntent().getStringExtra(COMIC_INFO_KEY) == null) {
            finish();
            return;
        }

        final String rawInfo = getIntent().getStringExtra(COMIC_INFO_KEY);
        playerInfo = new Gson().fromJson(rawInfo, ComicPlayerInfo.class);
        binding.title.setText(playerInfo.post.getTitle());
        if(playerInfo.type == VideoPlayerInfo.PlayerType.ONLINE)
            new Thread(() -> {
                PlayListHandler.insertToDefaultHistoryList(playerInfo.post, this);
            }).start();
        mFixedThreadPool = new ThreadPoolExecutor(4, 8, 0L,
                TimeUnit.MILLISECONDS,
                new JobQueue());
        ComicAdapter adapter = new ComicAdapter(pics, playerInfo.post.url,mFixedThreadPool);
        binding.pics.setAdapter(adapter);
        binding.pics.setOnScrollChangeListener((v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
            int page = ((LinearLayoutManager) binding.pics.getLayoutManager()).findFirstVisibleItemPosition();
            binding.pager.setText(String.valueOf(page + 1));
        });
        binding.pager.setOnClickListener(v -> showSelectPage());
        parse(adapter);
        int lastPosition = getSharedPreferences(Player.LAST_VIEW_POSITION, MODE_PRIVATE)
                .getInt(playerInfo.getKey(), 0);
        if(lastPosition > 0) {
            askResume(lastPosition);
        }
    }

    private void askResume(int lastPosition) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.continue_playing)
                .setMessage( getString(R.string.at_page, lastPosition ) )
                .setPositiveButton(R.string.yes, (dialog, which) -> binding.pics.getLayoutManager().scrollToPosition(lastPosition))
                .setNegativeButton(R.string.no, null).show();
    }

    void parse(ComicAdapter adapter) {
        mFixedThreadPool.execute(()->{
            try {
                Document html = Jsoup.parse(new URL(playerInfo.post.url), 5000);
                String[] segments = html.selectFirst(".last").attr("href").split("/");
                int pages = Integer.parseInt(segments[segments.length - 1]);
                for(int i = 1; i <= pages; i++)
                    pics.add(null);
                runOnUiThread(() -> {
                    binding.loading.setVisibility(View.GONE);
                    binding.pager.setVisibility(View.VISIBLE);
                    binding.pager.setText(String.valueOf(1));
                    adapter.notifyDataSetChanged();
                });
            } catch (Exception e) {
                error();
            }
        });
    }

    void showSelectPage() {
        int current = Integer.parseInt(binding.pager.getText().toString());
        AlertDialog.Builder builder = new AlertDialog.Builder(binding.pager.getContext());
        final NumberPicker input = new NumberPicker(binding.pager.getContext());
        input.setMinValue(1);
        input.setMaxValue(pics.size());
        input.setValue(current);
        FrameLayout container = new FrameLayout(binding.pager.getContext());
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.leftMargin = 40;
        params.rightMargin = 40;
        input.setLayoutParams(params);
        container.addView(input);

        builder.setView(container);
        builder.setPositiveButton(android.R.string.ok, (dialog, which) -> {
            int result = input.getValue();
            binding.pics.getLayoutManager().scrollToPosition(result - 1);
        });
        builder.setNegativeButton(android.R.string.cancel, (dialog, which) -> dialog.cancel());
        builder.show();
    }

    void error() {
        runOnUiThread(() -> {
            HelperFunc.showToast(this, R.string.failed_to_play_video, Toast.LENGTH_SHORT);
            finish();
        });
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        getSharedPreferences(Player.LAST_VIEW_POSITION, MODE_PRIVATE).edit()
                .putInt(playerInfo.getKey(), ((LinearLayoutManager)binding.pics.getLayoutManager()).findFirstVisibleItemPosition()).apply();
    }
}
