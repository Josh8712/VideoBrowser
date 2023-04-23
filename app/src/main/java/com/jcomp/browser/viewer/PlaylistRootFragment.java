package com.jcomp.browser.viewer;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.jcomp.browser.AppDatabase;
import com.jcomp.browser.databinding.PlaylistFragmentBinding;
import com.jcomp.browser.databinding.PostFragmentBinding;
import com.jcomp.browser.main.MainActivity;
import com.jcomp.browser.parser.model.ModelCache;
import com.jcomp.browser.parser.post.db.Playlist;
import com.jcomp.browser.parser.post.db.PlaylistDoa;
import com.jcomp.browser.parser.post.db.PlaylistWithCount;
import com.jcomp.browser.parser.post.db.Post;
import com.jcomp.browser.viewer.adapter.PlayListAdapter;
import com.jcomp.browser.viewer.adapter.PostAdapter;
import com.jcomp.browser.viewer.adapter.PostFragmentModelView;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class PlaylistRootFragment extends ListBaseFragment {
    PlayListAdapter adapter;
    PlaylistFragmentBinding binding;
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = PlaylistFragmentBinding.inflate(inflater, container, false);
        int graph_id = PostFragmentArgs.fromBundle(getArguments()).getGRAPHIDKEY();
        String rootURL = PostFragmentArgs.fromBundle(getArguments()).getROOTURLKEY();
        CoordinatorLayout root = binding.getRoot();

        MainActivity activity = (MainActivity) getActivity();
        if (activity == null)
            return root;

        List<PlaylistWithCount> playlists = new ArrayList<>();
        adapter = new PlayListAdapter(playlists, playlist -> activity.subNavigate(playlist.getName(), String.valueOf(playlist.uid), graph_id, PlaylistContentFragment.class.getCanonicalName()));
        binding.content.setAdapter(adapter);
        binding.content.setLayoutManager(new LinearLayoutManager(activity));

        reload();
        binding.refresh.setOnRefreshListener(this::reload);
        binding.add.setOnClickListener(v-> PlayListHandler.addPlayList(activity, ()->reload()));
        return root;
    }

    private void reload() {
        binding.refresh.setRefreshing(true);
        new Thread(()->{
            List<PlaylistWithCount> playlists = AppDatabase.getInstance(getContext()).playlistDoa().getPlayList();
            FragmentActivity activity = getActivity();
            if (activity == null)
                return;
            activity.runOnUiThread(()->{
                binding.refresh.setRefreshing(false);
                if(playlists.isEmpty()) {
                    binding.empty.setVisibility(View.VISIBLE);
                    binding.content.setVisibility(View.GONE);
                } else {
                    binding.empty.setVisibility(View.GONE);
                    binding.content.setVisibility(View.VISIBLE);
                }
                adapter.reset(playlists);
            });
        }).start();
    }
}
