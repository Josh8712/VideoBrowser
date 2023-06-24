package com.jcomp.browser.player;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.LoadControl;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.PlaybackException;
import com.google.android.exoplayer2.database.StandaloneDatabaseProvider;
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory;
import com.google.android.exoplayer2.ui.DefaultTimeBar;
import com.google.android.exoplayer2.ui.StyledPlayerView;
import com.google.android.exoplayer2.ui.TimeBar;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DataSpec;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource;
import com.google.android.exoplayer2.upstream.FileDataSource;
import com.google.android.exoplayer2.upstream.TransferListener;
import com.google.android.exoplayer2.upstream.cache.CacheDataSource;
import com.google.android.exoplayer2.upstream.cache.LeastRecentlyUsedCacheEvictor;
import com.google.android.exoplayer2.upstream.cache.SimpleCache;
import com.google.gson.Gson;
import com.jcomp.browser.R;
import com.jcomp.browser.databinding.ActivityPlayerBinding;
import com.jcomp.browser.download.DownloadManager;
import com.jcomp.browser.download.db.DownloadPost;
import com.jcomp.browser.tools.HelperFunc;
import com.jcomp.browser.viewer.PlayListHandler;
import com.jcomp.browser.widget.BreathingAnim;

import java.io.File;
import java.util.HashMap;

public class Player extends AppCompatActivity {
    public static final String PLAYER_INFO_KEY = "PLAYER_INFO_KEY";
    private static final String PLAYER_SHARED_PREF_KEY = "PLAYER_SHARED_PREF_KEY";
    public static final String LAST_VIEW_POSITION = "LAST_VIEW_POSITION";
    private static final String VOLUME_KEY = "VOLUME_KEY";
    private static SimpleCache simpleCache;
    VideoPlayerInfo playerInfo;
    PreviewHandler.Cue mCue = null;
    long bytesRead = 0;
    long startTime = System.currentTimeMillis();
    Runnable updateFrame;
    GestureTask currentTask = GestureTask.NONE;
    private ExoPlayer player;
    private TextView textInfo;
    private ImageView previewImageView;
    private DefaultTimeBar timeBar;
    private ImageButton downloadButton;
    private PreviewHandler previewer;

    public static SimpleCache getInstance(AppCompatActivity appCompatActivity) {
        if (simpleCache == null) {
            File downloadContentDirectory = new File(appCompatActivity.getExternalCacheDir(), "cache");
            simpleCache = new SimpleCache(downloadContentDirectory, new LeastRecentlyUsedCacheEvictor(128 * 1024 * 1024), new StandaloneDatabaseProvider(appCompatActivity));
        }
        return simpleCache;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowManager.LayoutParams attrs = getWindow().getAttributes();
        attrs.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
        getWindow().setAttributes(attrs);

        if (getIntent() == null || getIntent().getStringExtra(PLAYER_INFO_KEY) == null) {
            finish();
            return;
        }
        final String rawInfo = getIntent().getStringExtra(PLAYER_INFO_KEY);
        playerInfo = new Gson().fromJson(rawInfo, VideoPlayerInfo.class);
        if(playerInfo.type == VideoPlayerInfo.PlayerType.ONLINE)
            new Thread(() -> {
                PlayListHandler.insertToDefaultHistoryList(playerInfo.post, this);
            }).start();
        previewer = playerInfo.getPreviewer();

        ActivityPlayerBinding binding = ActivityPlayerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        textInfo = binding.textInfo;
        previewImageView = findViewById(R.id.preview);
        timeBar = findViewById(com.google.android.exoplayer2.ui.R.id.exo_progress);
        downloadButton = findViewById(R.id.exo_download);

        setupPlayer();
        setupDownload();
        long lastPosition = getSharedPreferences(LAST_VIEW_POSITION, Context.MODE_PRIVATE)
                .getLong(playerInfo.getKey(), 0);
        if (lastPosition > 0) {
            askResume(lastPosition);
        }

        ((TextView) findViewById(R.id.title)).setText(playerInfo.post.getTitle());
        findViewById(R.id.back).setOnClickListener(item -> finish());
        binding.playerView.setPlayer(player);
        binding.playerView.showController();
        setupView(binding.playerView, player);
        setupDownloadProgressUpdater();
    }

    private void askResume(long lastPosition) {
        android.app.AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.continue_playing)
                .setMessage( HelperFunc.convertTime(lastPosition / 1000) )
                .setPositiveButton(R.string.yes, (dialog, which) -> player.seekTo(lastPosition))
                .setNegativeButton(R.string.no, null).show();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (player == null)
            return;
        player.stop();
        SharedPreferences sharedPref = getSharedPreferences(PLAYER_SHARED_PREF_KEY, Context.MODE_PRIVATE);
        sharedPref.edit().putFloat(VOLUME_KEY, player.getVolume()).apply();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (player == null)
            return;

        if (player.getContentPosition() > 0) {
            getSharedPreferences(LAST_VIEW_POSITION, Context.MODE_PRIVATE).edit()
                    .putLong(playerInfo.getKey(), player.getContentPosition()).apply();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (player == null)
            return;
        player.prepare();
    }

    private void setupPlayer() {
        String videoURL = playerInfo.videoURL;
        LoadControl loadControl = new DefaultLoadControl.Builder()
                .setBufferDurationsMs(120000,
                        360000,
                        1,
                        1)
                .setTargetBufferBytes(-1)
                .setBackBuffer(120000, true)
                .setPrioritizeTimeOverSizeThresholds(true).build();
        TransferListener transferListener = new TransferListener() {
            @Override
            public void onTransferInitializing(DataSource source, DataSpec dataSpec, boolean isNetwork) {
                System.out.println("listener " + "init" + dataSpec.uri);
            }

            @Override
            public void onTransferStart(DataSource source, DataSpec dataSpec, boolean isNetwork) {
                System.out.println("listener " + "start " + dataSpec.uri);
            }

            @Override
            public void onBytesTransferred(DataSource source, DataSpec dataSpec, boolean isNetwork, int bytesTransferred) {
                bytesRead += bytesTransferred;
                if (bytesRead == bytesTransferred) {
                    startTime = System.currentTimeMillis();
                }
            }

            @Override
            public void onTransferEnd(DataSource source, DataSpec dataSpec, boolean isNetwork) {
                System.out.println("listener " + " end " + dataSpec.uri);
            }
        };
        DataSource.Factory factory;
        if (isLocal())
            factory = new FileDataSource.Factory().setListener(transferListener);
        else {
            HashMap<String, String> requestProperty = new HashMap<>();
            requestProperty.put("referer", playerInfo.playerURL);
            factory = new CacheDataSource.Factory()
                    .setCache(getInstance(this))
                    .setUpstreamDataSourceFactory(new DefaultHttpDataSource.Factory().setTransferListener(transferListener).setDefaultRequestProperties(requestProperty));
        }

        player = new ExoPlayer.Builder(this)
                .setMediaSourceFactory(new DefaultMediaSourceFactory(this).setDataSourceFactory(factory))
                .setLoadControl(loadControl).build();
        player.setMediaItem(MediaItem.fromUri(Uri.parse(videoURL)));
        player.setPlayWhenReady(true);
        SharedPreferences sharedPref = getSharedPreferences(PLAYER_SHARED_PREF_KEY, Context.MODE_PRIVATE);
        player.setVolume(sharedPref.getFloat(VOLUME_KEY, 0.5f));
        player.prepare();
        player.addListener(new com.google.android.exoplayer2.Player.Listener() {
            @Override
            public void onPlayerError(PlaybackException error) {
                com.google.android.exoplayer2.Player.Listener.super.onPlayerError(error);
                if (error.errorCode == PlaybackException.ERROR_CODE_IO_FILE_NOT_FOUND || error.errorCode == PlaybackException.ERROR_CODE_IO_READ_POSITION_OUT_OF_RANGE) {
                    HelperFunc.showToast(Player.this, R.string.failed_to_play_undone_video, Toast.LENGTH_SHORT);
                } else {
                    HelperFunc.showToast(Player.this, R.string.failed_to_play_video, Toast.LENGTH_SHORT);
                    finish();
                }
            }

            @Override
            public void onPlaybackStateChanged(int playbackState) {
                if (playbackState == ExoPlayer.STATE_BUFFERING) {
                    findViewById(R.id.progress_bar).setVisibility(View.VISIBLE);
                    bytesRead = 0;
                    startTime = System.currentTimeMillis();
                    new Thread(updateFrame).start();
                } else {
                    downloadButton.setVisibility(View.VISIBLE);
                    findViewById(R.id.progress_bar).setVisibility(View.INVISIBLE);
                }
            }
        });
    }

    private void setupDownload() {
        if (isLocal()) {
            downloadButton.setImageResource(R.drawable.baseline_delete_outline_24);
            downloadButton.setOnClickListener(view -> {
                BreathingAnim.breath(downloadButton, 0, 1, 1000);
                new Thread(() -> {
                    DownloadManager manager = DownloadManager.getInstance(getApplicationContext());
                    DownloadPost downloadPost = manager.getRecord(playerInfo);
                    boolean success = manager.deleteJob(downloadPost);
                    downloadButton.post(() -> {
                        if (success)
                            finish();
                        else {
                            ObjectAnimator anim = (ObjectAnimator) downloadButton.getTag(R.id.breath_tag);
                            if (anim != null) {
                                anim.cancel();
                                downloadButton.setAlpha(1f);
                            }
                            HelperFunc.showToast(Player.this, R.string.failed_to_delete_video);
                        }
                    });
                }).start();
            });
        } else {
            new Thread(() -> {
                DownloadManager manager = DownloadManager.getInstance(getApplicationContext());
                DownloadPost downloadPost = manager.getRecord(playerInfo);
                if (downloadPost != null && downloadPost.status == DownloadPost.Status.FINISHED) {
                    downloadButton.post(() -> {
                        downloadButton.setImageResource(R.drawable.baseline_check_24);
                    });
                    downloadButton.setOnClickListener((view) -> {
                        HelperFunc.showToast(getApplicationContext(), getText(com.google.android.exoplayer2.core.R.string.exo_download_completed), Toast.LENGTH_SHORT);
                    });
                } else if (downloadPost == null) {
                    downloadButton.setOnClickListener((view) -> {
                        HelperFunc.showToast(getApplicationContext(), getText(com.google.android.exoplayer2.core.R.string.exo_download_downloading), Toast.LENGTH_SHORT);
                        manager.download(playerInfo);
                        setDownloadRunningButtonClick();
                    });
                } else {
                    setDownloadRunningButtonClick();
                }
            }).start();
        }
    }

    private void setDownloadRunningButtonClick() {
        downloadButton.post(() -> {
            BreathingAnim.breath(downloadButton, 0, 1, 1000);
        });
        downloadButton.setOnClickListener((view) -> {
            HelperFunc.showToast(getApplicationContext(), getText(com.google.android.exoplayer2.core.R.string.exo_download_downloading), Toast.LENGTH_SHORT);
        });
    }

    private void setupDownloadProgressUpdater() {
        updateFrame = () -> {
            View progressBar = findViewById(R.id.progress_bar);
            TextView progress = findViewById(R.id.progress_text);
            if (progress == null || player == null)
                return;
            while (!isDestroyed() && progressBar.getVisibility() == View.VISIBLE) {
                long diff = System.currentTimeMillis();
                diff -= startTime;
                diff /= 1000;
                final float speed;
                if (diff > 0)
                    speed = (float) bytesRead / diff;
                else
                    speed = 0;
                runOnUiThread(() -> {
                    progress.setVisibility(View.VISIBLE);
                    progress.setText(HelperFunc.humanReadableByteCountBin(bytesRead) + " ( " + HelperFunc.humanReadableByteCountBin((int) Math.ceil(speed)) + "/sec )");
                });
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
        new Thread(updateFrame).start();
    }

    private boolean isLocal() {
        return playerInfo.type == VideoPlayerInfo.PlayerType.LOCAL;
    }

    private void setupView(StyledPlayerView root, ExoPlayer player) {
        root.setControllerAutoShow(false);
        View rewind = findViewById(R.id.rewind_text);
        ObjectAnimator rewindAnimator = ObjectAnimator.ofFloat(rewind, View.ALPHA, 0.2f, 1f);
        rewindAnimator.setDuration(400);
        rewindAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        rewindAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                rewind.setVisibility(View.INVISIBLE);
            }
        });
        View ffw = findViewById(R.id.ffw_text);
        ObjectAnimator ffwAnimator = ObjectAnimator.ofFloat(ffw, View.ALPHA, 0.2f, 1f);
        ffwAnimator.setDuration(400);
        ffwAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        ffwAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                ffw.setVisibility(View.INVISIBLE);
            }
        });
        GestureDetector gesture = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onDoubleTap(@NonNull MotionEvent e) {
                if (e.getX() > root.getWidth() * 0.6) {
                    player.seekForward();
                    ffw.setVisibility(View.VISIBLE);
                    ffwAnimator.cancel();
                    ffwAnimator.start();

                } else if (e.getX() < root.getWidth() * 0.4) {
                    player.seekBack();
                    rewind.setVisibility(View.VISIBLE);
                    rewindAnimator.cancel();
                    rewindAnimator.start();
                } else if (player.isPlaying()) {
                    player.pause();
                    root.showController();
                } else {
                    player.play();
                    root.hideController();
                }
                return super.onDoubleTap(e);
            }

            @Override
            public void onLongPress(@NonNull MotionEvent e) {
                super.onLongPress(e);
                if (currentTask != GestureTask.NONE)
                    return;

                long pos = (long) (player.getContentPosition() + (e.getX() - e.getX()) * (float) player.getDuration() / root.getWidth());
                e.setLocation(pos / (float) player.getDuration() * root.getWidth(), 0);
                if (!timeBar.onTouchEvent(e)) {
                    root.showController();
                    return;
                }
                currentTask = GestureTask.SEEK;
                Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    v.vibrate(VibrationEffect.createOneShot(30, VibrationEffect.DEFAULT_AMPLITUDE));
                } else {
                    v.vibrate(30);
                }
            }

            @Override
            public boolean onSingleTapConfirmed(@NonNull MotionEvent e) {
                if (currentTask == GestureTask.NONE) {
                    if (root.isControllerFullyVisible())
                        root.hideController();
                    else
                        root.showController();
                }
                return super.onSingleTapConfirmed(e);
            }
        });
        root.setOnTouchListener(new View.OnTouchListener() {
            float startX, startY, startProgress;
            float startVolume;
            boolean isShowing = false;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    startProgress = player.getContentPosition();
                    startVolume = player.getVolume();
                    startX = event.getX();
                    startY = event.getY();
                    isShowing = root.isControllerFullyVisible();
                }
                gesture.onTouchEvent(event);
                if (event.getAction() == MotionEvent.ACTION_MOVE) {
                    if (currentTask == GestureTask.NONE) {
                        if (startX > root.getWidth() * 0.6)
                            currentTask = GestureTask.VOLUME;
                    } else if (currentTask == GestureTask.SEEK) {
                        long pos = (long) (startProgress + (event.getX() - startX) * (float) player.getDuration() / root.getWidth());
                        event.setLocation(pos / (float) player.getDuration() * root.getWidth(), 0);
                        timeBar.onTouchEvent(event);
                        root.showController();
                        if (!isShowing)
                            findViewById(com.google.android.exoplayer2.ui.R.id.exo_center_controls).setVisibility(View.GONE);
                    } else if (currentTask == GestureTask.VOLUME) {
                        textInfo.setVisibility(View.VISIBLE);
                        float target = startVolume + (startY - event.getY()) / root.getHeight() / 0.5f;
                        target = Math.min(target, 1);
                        target = Math.max(target, 0);
                        textInfo.setText(Math.round(target * 100) + "%");
                        player.setVolume(target);
                    }
                }
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    textInfo.setVisibility(View.GONE);
                    previewImageView.setVisibility(View.GONE);
                    findViewById(com.google.android.exoplayer2.ui.R.id.exo_center_controls).setVisibility(View.VISIBLE);
                    if (currentTask == GestureTask.SEEK) {
                        long pos = (long) (startProgress + (event.getX() - startX) * (float) player.getDuration() / root.getWidth());
                        event.setLocation(pos / (float) player.getDuration() * root.getWidth(), 0);
                        timeBar.onTouchEvent(event);
                        //player.seekTo(pos);
                        if (isShowing)
                            root.showController();
                        else
                            root.hideController();
                    }
                }
                if (event.getAction() != MotionEvent.ACTION_MOVE)
                    currentTask = GestureTask.NONE;
                return true;
            }
        });
        timeBar.addListener(new TimeBar.OnScrubListener() {
            @Override
            public void onScrubStart(TimeBar timeBar, long position) {

            }

            @Override
            public void onScrubMove(TimeBar timeBar, long position) {
                setPreviewImage(position);
            }

            @Override
            public void onScrubStop(TimeBar timeBar, long position, boolean canceled) {
                previewImageView.setVisibility(View.GONE);
            }
        });
    }

    public void setPreviewImage(long pos) {
        if (previewer == null)
            return;
        PreviewHandler.Cue cue = previewer.getCue(pos, player.getDuration());
        previewImageView.setVisibility(View.VISIBLE);
        if (cue != mCue) {
            mCue = cue;
            previewer.setBitmap(mCue, previewImageView);
        }
    }

    enum GestureTask {
        NONE, SEEK, VOLUME
    }

}
