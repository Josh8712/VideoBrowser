package com.jcomp.browser.viewer;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.lifecycle.Observer;

import com.jcomp.browser.history.db.History;
import com.jcomp.browser.main.MainActivity;
import com.jcomp.browser.parser.ParserJumpCallback;
import com.jcomp.browser.parser.ParserPageCallback;
import com.jcomp.browser.parser.ParserPostPageCallback;
import com.jcomp.browser.parser.pager.Pager;
import com.jcomp.browser.parser.post.db.Post;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedHashMap;

public class PostFragment extends VideoParserFragment implements ParserPostPageCallback, ParserJumpCallback {
    Observer<Pager> pagerObserver = pager -> {
        String[] names = new String[pager.getSize()];
        for (int i = 0; i < pager.getSize(); i++)
            names[i] = pager.getPageName(i);
        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_dropdown_item, names);
        binding.spinner.setAdapter(spinnerArrayAdapter);
        binding.spinner.setSelection(pager.getCurrentIndex());
        binding.spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (i == pager.getCurrentIndex())
                    return;
                String url = pager.getPageURL(i);
                changePage(url);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        binding.pager.setVisibility(View.VISIBLE);

        if (!pager.hasPrevious())
            binding.prev.setVisibility(View.INVISIBLE);
        else {
            binding.prev.setVisibility(View.VISIBLE);
            binding.prev.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String url = pager.getPageURL(pager.getCurrentIndex() - 1);
                    changePage(url);
                }
            });
        }

        if (!pager.hasNext())
            binding.forward.setVisibility(View.INVISIBLE);
        else {
            binding.forward.setVisibility(View.VISIBLE);
            binding.forward.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String url = pager.getPageURL(pager.getCurrentIndex() + 1);
                    changePage(url);
                }
            });
        }
    };

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View _root = super.onCreateView(inflater, container, savedInstanceState);
        postFragmentModelView.getmPager().observe(getViewLifecycleOwner(), pagerObserver);
        return _root;
    }

    @Override
    protected void setupCallback() {
        super.setupCallback();
        browser.registerCallback((ParserPostPageCallback) this);
        browser.registerCallback((ParserPageCallback) getActivity());
        browser.registerCallback((ParserJumpCallback) this);
    }

    @Override
    protected boolean addToTop() {
        return false;
    }

    @Override
    public void addPager(Pager pager) {
        if (pager == null)
            return;
        if (browser.getUrl().equals(getCurrentURL()))
            postFragmentModelView.getmPager().postValue(pager);
    }

    @Override
    public void addPost(LinkedHashMap<String, Post> postList) {
        super.addPost(postList);
        pageInfoFinished();
    }

    @Override
    public void parseFinished() {
        super.parseFinished();
        pageInfoFinished();
    }

    @Override
    public void pageInfoFinished() {
        root.post(() -> {
            if (isSameURL(rootURL, browser.getUrl())) {
                updateHistory(browser.getHistory());
            }
        });
    }

    private boolean isSameURL(String a, String b) {
        try {
            return removeURLTail(new URL(a).getPath()).equals(removeURLTail(new URL(b).getPath()));
        } catch (MalformedURLException e) {
            return false;
        }
    }


    private String removeURLTail(String s) {
        if (s.endsWith("/")) {
            s = s.substring(0, s.length() - 1);
        }
        return s;
    }

    private void changePage(String url) {
        historyURL.add(url);
        reload();
    }


    @Override
    public void navigateToList(History history) {
        MainActivity activity = (MainActivity) getActivity();
        if (activity == null)
            return;
        activity.subNavigate(history.getDisplayName(), history.getUrl(), graph_id);
        activity.getBrowserHolder().toggleVisibility();
    }
}
