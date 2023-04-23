package com.jcomp.browser.viewer;

import android.content.res.Configuration;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.gson.Gson;
import com.jcomp.browser.browser.Browser;
import com.jcomp.browser.databinding.PostFragmentBinding;
import com.jcomp.browser.history.db.History;
import com.jcomp.browser.main.MainActivity;
import com.jcomp.browser.parser.ParserCommonCallback;
import com.jcomp.browser.parser.model.Model;
import com.jcomp.browser.parser.post.db.Post;
import com.jcomp.browser.viewer.adapter.PostAdapter;
import com.jcomp.browser.viewer.adapter.PostFragmentModelView;
import com.jcomp.browser.viewer.video_loader.ResourceLoader;

import java.util.ArrayList;
import java.util.LinkedHashMap;

public abstract class ViewerFragmentBase extends Fragment implements ParserCommonCallback {
    private static final String LAST_POST_POSITION = "LAST_POST_POSITION";
    private static final String LAST_PLAYING_POSITION = "LAST_PLAYING_POSITION";
    private static final String LAST_HISTORY = "LAST_HISTORY";
    protected int graph_id;
    protected String rootURL;
    protected History currentHistory = null;

    protected ArrayList<String> historyURL = new ArrayList<>();
    protected PostFragmentModelView postFragmentModelView;
    protected SwipeRefreshLayout root;
    protected PostAdapter adapter;
    protected LinkedHashMap<String, Post> postList = new LinkedHashMap<>();
    protected Browser browser;
    protected ResourceLoader resourceLoader;
    protected PostFragmentBinding binding;
    private final Observer<LinkedHashMap<String, Post>> postObserver = postList -> {
        int ori_size = this.postList.size();
        if (addToTop()) {
            LinkedHashMap<String, Post> oldMap = (LinkedHashMap<String, Post>) this.postList.clone();
            LinkedHashMap<String, Post> newMap = (LinkedHashMap<String, Post>) postList.clone();
            newMap.keySet().removeAll(oldMap.keySet());
            this.postList.clear();
            this.postList.putAll(newMap);
            this.postList.putAll(oldMap);
        } else
            this.postList.putAll(postList);

        int count = this.postList.size() - ori_size;
        if (!this.postList.isEmpty()) {
            binding.empty.setVisibility(View.GONE);
            binding.content.setVisibility(View.VISIBLE);
        }
        if (count == 0)
            return;

        GridLayoutManager manager = (GridLayoutManager) binding.content.getLayoutManager();
        if (manager == null)
            return;
        if (addToTop()) {
            if (manager.findFirstVisibleItemPosition() == 0)
                root.postDelayed(() -> {
                    binding.content.smoothScrollToPosition(count - 1);
                }, 10);
            adapter.notifyItemRangeInserted(0, count);
        } else
            adapter.notifyItemRangeInserted(ori_size, count);
        manager.setSpanCount(getSpanCount() * postList.values().iterator().next().getShowScale());

    };
    protected int nowPlayingPostPos = -1;
    protected PostAdapter.ModelCallBack modelCallBack = new PostAdapter.ModelCallBack() {
        @Override
        public void onClick(PostAdapter.CallBackArgs _args) {
            MainActivity activity = (MainActivity) getActivity();
            if (activity == null)
                return;
            PostAdapter.VideoCallBackArgs args = ((PostAdapter.VideoCallBackArgs) _args);
            nowPlayingPostPos = args.postPos;
            if (args.post.isVideo())
                loadVideo(args.resourceLoader);
            else
                activity.subNavigate(args.post.getTitle(), args.post.url, graph_id);
        }

        @Override
        public void onModelClick(PostAdapter.CallBackArgs _args) {
            MainActivity activity = (MainActivity) getActivity();
            if (activity == null)
                return;
            Model model = ((PostAdapter.ModelCallBackArgs) _args).model;
            activity.subNavigate(model.name, model.url, graph_id);
        }
    };
    private int lastPosition = -1;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            lastPosition = savedInstanceState.getInt(LAST_POST_POSITION, -1);
            nowPlayingPostPos = savedInstanceState.getInt(LAST_PLAYING_POSITION, -1);
            String json = savedInstanceState.getString(LAST_HISTORY);
            if (json != null)
                currentHistory = new Gson().fromJson(json, History.class);
        }
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = PostFragmentBinding.inflate(inflater, container, false);
        graph_id = PostFragmentArgs.fromBundle(getArguments()).getGRAPHIDKEY();
        rootURL = PostFragmentArgs.fromBundle(getArguments()).getROOTURLKEY();
        root = binding.getRoot();

        postFragmentModelView =
                new ViewModelProvider(this).get(PostFragmentModelView.class);
        MainActivity activity = (MainActivity) getActivity();
        if (activity == null)
            return root;


        updateHistory(currentHistory);

        int scale = 1;
        if (!postList.isEmpty())
            scale = postList.values().iterator().next().getShowScale();

        adapter = new PostAdapter(postList, modelCallBack, getContext());
        binding.content.setAdapter(adapter);
        binding.content.setLayoutManager(new GridLayoutManager(getContext(), getSpanCount() * scale));


        browser = activity.getBrowserHolder();
        postFragmentModelView.getmPost().observe(getViewLifecycleOwner(), postObserver);
        root.setOnRefreshListener(this::reload);

        return root;
    }

    protected void updateHistory(History history) {
        currentHistory = history;
        MainActivity activity = (MainActivity) getActivity();
        if (activity == null)
            return;
        activity.updateHistory(currentHistory);
    }

    protected int getSpanCount() {
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT)
            return 1;
        else
            return 2;
    }

    protected void setupCallback() {
        browser.registerCallback(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        setupCallback();
        browser.setFragmentReady(true);
        if (postFragmentModelView.getmPost().getValue() == null || postFragmentModelView.getmPost().getValue().isEmpty()) {
            reload();
        }
        updateHistory(currentHistory);
        if (!this.postList.isEmpty() && lastPosition >= 0) {
            binding.content.smoothScrollToPosition(lastPosition);
            lastPosition = -1;
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        browser.setFragmentReady(false);
        GridLayoutManager manager = (GridLayoutManager) binding.content.getLayoutManager();
        if (manager != null)
            lastPosition = manager.findFirstCompletelyVisibleItemPosition();

    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(LAST_POST_POSITION, lastPosition);
        if (currentHistory != null) {
            Gson gson = new Gson();
            String json = gson.toJson(currentHistory);
            outState.putString(LAST_HISTORY, json);
        } else {
            outState.putString(LAST_HISTORY, null);
        }
        outState.putInt(LAST_PLAYING_POSITION, nowPlayingPostPos);

    }

    abstract protected boolean addToTop();

    protected void reload() {
        adapter.reset();
        postList.clear();
        binding.empty.setVisibility(View.GONE);
        binding.content.setVisibility(View.INVISIBLE);
        binding.pager.setVisibility(View.GONE);
        root.setRefreshing(true);
        loadMore();
    }

    protected void loadMore() {
        updateHistory(null);
        if (historyURL.isEmpty())
            load(rootURL);
        else
            load(historyURL.get(historyURL.size() - 1));
    }


    public abstract void load(String url);

    @Override
    public void parseFinished() {
        root.post(() -> {
            root.setRefreshing(false);
            if (resourceLoader != null)
                resourceLoader.parseFinished();
            if (postList.isEmpty()) {
                binding.empty.setVisibility(View.VISIBLE);
                binding.content.setVisibility(View.INVISIBLE);
            }
        });
    }

    public boolean loadVideo(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
        resourceLoader.start(this);
        return true;
    }

    public boolean canGoBack() {
        return !historyURL.isEmpty();
    }

    public void goBack() {
        if (!historyURL.isEmpty())
            historyURL.remove(historyURL.get(historyURL.size() - 1));
        reload();
    }

    public void goHome() {
        historyURL.clear();
        reload();
    }

    public String getCurrentURL() {
        if (historyURL.isEmpty())
            return rootURL;
        return historyURL.get(historyURL.size() - 1);
    }
}
