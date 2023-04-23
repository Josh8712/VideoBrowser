package com.jcomp.browser.main;

import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.PersistableBundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavArgument;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.NavGraph;
import androidx.navigation.NavGraphBuilder;
import androidx.navigation.NavOptions;
import androidx.navigation.fragment.FragmentNavigator;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.navigation.NavigationView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.jcomp.browser.AppDatabase;
import com.jcomp.browser.R;
import com.jcomp.browser.browser.Browser;
import com.jcomp.browser.databinding.ActivityMainBinding;
import com.jcomp.browser.history.db.History;
import com.jcomp.browser.history.db.HistoryDoa;
import com.jcomp.browser.parser.ParserPageCallback;
import com.jcomp.browser.parser.category.Category;
import com.jcomp.browser.parser.post.db.Post;
import com.jcomp.browser.parser.searcher.Searcher;
import com.jcomp.browser.parser.tag.Tag;
import com.jcomp.browser.tools.HelperFunc;
import com.jcomp.browser.viewer.PostFragment;
import com.jcomp.browser.viewer.TagFragment;
import com.jcomp.browser.viewer.ViewerFragmentBase;
import com.jcomp.browser.welcome.Welcome;
import com.jcomp.browser.widget.MyBottomDrawer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class MainActivity extends AppCompatActivity implements ParserPageCallback {
    public static final String CLASS_LIST_KEY = "CLASS_LIST_KEY";
    private static final String ROOT_URL_KEY = "ROOT_URL_KEY";
    private static final String GRAPH_ID_KEY = "GRAPH_ID_KEY";
    private static final String TAG_MENU_ID_KEY = "TAG_MENU_ID_KEY";
    History currentHistory;
    HashMap<String, Category> mCategoryList;
    private ArrayList<NavDestination> destinations;
    private NavController navController;
    private NavigationView navigationView;
    private MyBottomDrawer bottomNavigationContainer;
    private Browser browser;
    private Menu menu;
    private Searcher searcher;
    private int tagID = -1;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();

        currentHistory = new Gson().fromJson(intent.getStringExtra(Welcome.HISTORY_INTENT_KEY), History.class);
        String title = currentHistory.getDisplayName();
        restoreInstance(savedInstanceState);

        MainViewModel mainViewModel =
                new ViewModelProvider(this).get(MainViewModel.class);
        ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);

        bottomNavigationContainer = binding.bottomNavigationContainer;
        bottomNavigationContainer.init();
        browser = binding.browserHolder.browser;
        browser.init(new Handler());
        navigationView = binding.navView;
        setupNavGraph(mainViewModel, title);
        binding.toolbar.setOnClickListener(view -> binding.bottomNavigationContainer.open());
        mainViewModel.getmSearcher().observe(this, searcher -> {
            this.searcher = searcher;
            showSearch(true);
        });
    }

    public void setupNavGraph(MainViewModel mainViewModel, String title) {
        findViewById(R.id.nav_host_fragment_content_main).setPadding(0, 0, 0, bottomNavigationContainer.getBottomSheetBehavior().getPeekHeight());
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment_content_main);
        if (navHostFragment != null) {
            navController = navHostFragment.getNavController();
            NavGraph graph = navController.getNavInflater().inflate(R.navigation.main);
            destinations = mainViewModel.getmDestinations();
            mCategoryList = mainViewModel.getmCategoryList();
            if (destinations.isEmpty())
                initNavGraph(graph);
            else
                resumeNavGraph(graph);
            NavGraph home = (NavGraph) graph.findNode(R.id.home_graph);
            if (home != null) {
                home.findNode(R.id.nav_home).setLabel(title);
            }
            navController.setGraph(graph);
            AppBarConfiguration mAppBarConfiguration = new AppBarConfiguration.Builder(graph)
                    .setOpenableLayout(bottomNavigationContainer)
                    .build();
            NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
            NavigationUI.setupWithNavController(navigationView, navController);
        }
    }

    private void initNavGraph(NavGraph graph) {
        if (currentHistory != null && currentHistory.isSpecialTab())
            for (int i = graph.getNodes().size() - 1; i >= 0; i--) {
                if (graph.getNodes().valueAt(i).getId() != currentHistory.getId()) {
                    navigationView.getMenu().removeItem(graph.getNodes().valueAt(i).getId());
                    graph.getNodes().removeAt(i);
                } else
                    graph.setStartDestination(currentHistory.getId());
            }
        for (int i = 0; i < graph.getNodes().size(); i++) {
            int id = graph.getNodes().valueAt(i).getId();
            String url = currentHistory.getUrl();
            if (((NavGraph) graph.getNodes().valueAt(i)).getNodes().valueAt(0).getId() != R.id.nav_home)
                url = "";
            graph.getNodes().valueAt(i).addArgument(ROOT_URL_KEY, new NavArgument.Builder().setDefaultValue(url).build());
            graph.getNodes().valueAt(i).addArgument(GRAPH_ID_KEY, new NavArgument.Builder().setDefaultValue(id).build());
            destinations.add(graph.getNodes().valueAt(i));
        }
    }

    private void resumeNavGraph(NavGraph graph) {
        for (NavDestination d : destinations) {
            if (d.getParent() != null)
                d.getParent().remove(d);
        }
        graph.clear();
        graph.addDestinations(destinations);
        for (int j = 0; j < graph.getNodes().size(); j++) {
            int id = graph.getNodes().valueAt(j).getId();
            NavDestination target = ((NavGraph) graph.getNodes().valueAt(j)).getNodes().valueAt(0);
            if (navigationView.getMenu().findItem(id) == null) {
                navigationView.getMenu().add(R.id.menu_group, id, 0, target.getLabel());
            }
            if (target.getLabel().equals(getString(R.string.tags)))
                tagID = id;
        }

        for (int i = navigationView.getMenu().size() - 1; i >= 0; i--) {
            if (graph.findNode(navigationView.getMenu().getItem(i).getItemId()) == null)
                navigationView.getMenu().removeItem(navigationView.getMenu().getItem(i).getItemId());
        }

        if (graph.findNode(graph.getStartDestinationId()) == null)
            graph.setStartDestination(destinations.get(0).getId());
    }

    @Override
    public boolean onSupportNavigateUp() {
        return navController.navigateUp() || super.onSupportNavigateUp();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        this.menu = menu;
        if (searcher == null)
            showSearch(false);
        updateHistory(currentHistory);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        bottomNavigationContainer.close();
        if (id == R.id.action_preview_toggle) {
            browser.toggleVisibility();
            return true;
        } else if (id == R.id.action_change_root) {
            ViewerFragmentBase fragment = getCurrentFragment();
            if (fragment.canGoBack())
                fragment.goHome();
            else if (navController.getCurrentDestination().getParent().getStartDestinationId() == navController.getCurrentDestination().getId())
                finish();
            else
                navController.popBackStack(navController.getCurrentDestination().getParent().getStartDestinationId(), false);
            return true;
        } else if (id == R.id.action_search) {
            if (searcher == null) {
                showSearch(false);
                return true;
            }
            searcher.show(this, navController.getGraph().getStartDestinationId());
            return true;
        } else if (id == R.id.action_add_bookmark) {
            if (currentHistory == null)
                return super.onOptionsItemSelected(item);
            new Thread(() -> {
                HistoryDoa manager = AppDatabase.getInstance(this).historyDoa();
                History checkHistory = manager.getByPath(currentHistory.getUrl());
                runOnUiThread(() -> {
                    if (checkHistory == null) {
                        new Thread(() -> {
                            manager.insert(currentHistory);
                        }).start();
                        HelperFunc.showToast(this, getString(R.string.bookmark_added), Toast.LENGTH_LONG);
                        setBookmark(true);
                    } else {
                        new Thread(() -> {
                            manager.delete(currentHistory);
                        }).start();
                        setBookmark(false);
                        HelperFunc.showToast(this, getString(R.string.bookmark_removed), Toast.LENGTH_LONG);
                    }
                });
            }).start();
        } else if (id == android.R.id.home) {
            // traverse navController back queue and print destination names
            int counter = 0;
            for (int i = navController.getBackQueue().size() - 1; i >= 0; i--) {
                if (navController.getBackQueue().get(i).getDestination().getLabel() == null)
                    continue;
                counter++;
            }
            if (counter == 1) {
                bottomNavigationContainer.open();
                return true;
            }
        }

        return super.onOptionsItemSelected(item);
    }

    public void setBookmark(boolean isBookmark) {
        showBookmark(true);
        if (menu == null)
            return;
        MenuItem bookmarkMenu = menu.findItem(R.id.action_add_bookmark);
        if (bookmarkMenu == null)
            return;
        bookmarkMenu.setVisible(true);
        if (isBookmark) {
            bookmarkMenu.setIcon(R.drawable.baseline_bookmark_24);
            bookmarkMenu.setTitle(R.string.action_remove_bookmark);
        } else {
            bookmarkMenu.setIcon(R.drawable.baseline_bookmark_border_24);
            bookmarkMenu.setTitle(R.string.action_add_bookmark);
        }
    }

    public void showBookmark(boolean show) {
        if (menu == null)
            return;
        MenuItem bookmarkMenu = menu.findItem(R.id.action_add_bookmark);
        if (bookmarkMenu == null)
            return;
        bookmarkMenu.setVisible(show);
    }

    public void updateHistory(History newHistory) {
        currentHistory = newHistory;
        if (menu == null)
            return;
        if (newHistory == null || !newHistory.getUrl().startsWith("http") || newHistory.isSpecialTab()) {
            showBookmark(false);
            menu.findItem(R.id.action_preview_toggle).setVisible(false);
            return;
        } else if (!newHistory.isSpecialTab())
            menu.findItem(R.id.action_preview_toggle).setVisible(true);

        new Thread(() -> {
            if (currentHistory == null)
                return;
            History history = AppDatabase.getInstance(this).historyDoa().getByPath(currentHistory.getUrl());
            boolean isBookmark = history != null;
            runOnUiThread(() -> {
                if (currentHistory != null && currentHistory.getUrl().equals(newHistory.getUrl())) {
                    setBookmark(isBookmark);
                }
            });
            if (isBookmark) {
                history.update(newHistory.getFavicon(), newHistory.getTitle());
                AppDatabase.getInstance(this).historyDoa().update(history);
            }
        }).start();
    }

    private ViewerFragmentBase getCurrentFragment() {
        Fragment navHostFragment = getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment_content_main);
        return (ViewerFragmentBase) navHostFragment.getChildFragmentManager().getFragments().get(0);
    }


    @Override
    public void onBackPressed() {
        if (bottomNavigationContainer.isOpen())
            bottomNavigationContainer.close();
        else if (browser.getVisibility() == View.VISIBLE) {
            if (browser.getBrowser().canGoBack())
                browser.getBrowser().goBack();
            else
                browser.toggleVisibility();
        } else {
            ViewerFragmentBase fragment = getCurrentFragment();
            if (fragment.canGoBack()) {
                fragment.goBack();
            } else if (navController.getCurrentDestination().getParent().getStartDestinationId() != navController.getCurrentDestination().getId())
                navController.popBackStack();
            else
                super.onBackPressed();
        }
    }

    public Browser getBrowserHolder() {
        return browser;
    }

    public void popBack() {
        navController.navigate(navController.getGraph().getStartDestinationId(), null, new NavOptions.Builder().setLaunchSingleTop(true).setRestoreState(true).setPopUpTo(
                NavGraph.findStartDestination(navController.getGraph()).getId(), false, true).build());
    }

    public void subNavigate(String title, String url, int graph_id) {
        if (navController.findDestination(graph_id) == null)
            return;
        int id = View.generateViewId();
        FragmentNavigator.Destination dest = navController.getNavigatorProvider().getNavigator(FragmentNavigator.class).createDestination();
        dest.setClassName(PostFragment.class.getCanonicalName());
        dest.setId(id);
        dest.setLabel(title);
        dest.addArgument(ROOT_URL_KEY, new NavArgument.Builder().setDefaultValue(url).build());
        dest.addArgument(GRAPH_ID_KEY, new NavArgument.Builder().setDefaultValue(graph_id).build());
        ((NavGraph) navController.findDestination(graph_id)).addDestination(dest);
        navController.navigate(id);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ((ViewGroup) browser.getParent()).removeView(browser);
        browser.destroy();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            if (bottomNavigationContainer.isOpen()) {
                Rect outRect = new Rect();
                bottomNavigationContainer.getGlobalVisibleRect(outRect);
                if (!outRect.contains((int) ev.getRawX(), (int) ev.getRawY()))
                    bottomNavigationContainer.close();
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState, @NonNull PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
        outState.putInt(TAG_MENU_ID_KEY, tagID);
    }

    private void restoreInstance(Bundle savedInstanceState) {
        if (savedInstanceState == null)
            return;
        tagID = savedInstanceState.getInt(TAG_MENU_ID_KEY, -1);
    }

    @Override
    public void addCategory(List<Category> categoryList) {
        if (categoryList == null || categoryList.isEmpty())
            return;
        runOnUiThread(() -> {
            for (Category c : categoryList) {
                if (!mCategoryList.containsKey(c.getKey())) {
                    mCategoryList.put(c.getKey(), c);
                    _addCategory(c, navController.getGraph());
                }
            }
        });
    }

    private void _addCategory(Category category, NavGraph graph) {
        int id = View.generateViewId();
        category.setId(id);
        navigationView.getMenu().add(R.id.menu_group, id, navigationView.getMenu().size(), category.getTitle());
        navigationView.getMenu().setGroupCheckable(R.id.menu_group, true, true);

        int subID = View.generateViewId();
        FragmentNavigator.Destination dest = navController.getNavigatorProvider().getNavigator(FragmentNavigator.class).createDestination();
        dest.setClassName(PostFragment.class.getCanonicalName());
        dest.setId(subID);
        dest.setLabel(category.getTitle());
        dest.addArgument(ROOT_URL_KEY, new NavArgument.Builder().setDefaultValue(category.url).build());
        dest.addArgument(GRAPH_ID_KEY, new NavArgument.Builder().setDefaultValue(id).build());

        NavGraph subgraph = new NavGraphBuilder(navController.getNavigatorProvider(), id, subID).build();
        subgraph.addDestination(dest);
        graph.addDestination(subgraph);
        destinations.add(subgraph);
    }

    @Override
    public void addTagList(List<Tag> tagList) {
        if (tagList == null || tagList.isEmpty())
            return;
        runOnUiThread(() -> {
            boolean contain = tagID != -1;
            if (contain) {
                NavGraph destination = (NavGraph) navController.findDestination(tagID);
                if (destination == null)
                    return;
                List<Post> list = new Gson().fromJson((String) destination.getNodes().valueAt(0).getArguments().get(CLASS_LIST_KEY).getDefaultValue(), new TypeToken<List<Tag>>() {
                }.getType());
                HashSet<String> keys = new HashSet<>();
                for (Post _tag : list)
                    keys.add(_tag.getKey());
                for (Post _tag : tagList) {
                    if (!keys.contains(_tag)) {
                        keys.add(_tag.getKey());
                        list.add(_tag);
                    }
                }
                destination.addArgument(CLASS_LIST_KEY, new NavArgument.Builder().setDefaultValue(new Gson().toJson(list)).build());
            } else {
                tagID = View.generateViewId();

                navigationView.getMenu().add(R.id.menu_group, tagID, navigationView.getMenu().size(), getString(R.string.tags));
                navigationView.getMenu().setGroupCheckable(R.id.menu_group, true, true);
                int destID = View.generateViewId();
                FragmentNavigator.Destination dest = navController.getNavigatorProvider().getNavigator(FragmentNavigator.class).createDestination();
                dest.setClassName(TagFragment.class.getCanonicalName());
                dest.setId(destID);
                dest.setLabel(getString(R.string.tags));
                dest.addArgument(CLASS_LIST_KEY, new NavArgument.Builder().setDefaultValue(new Gson().toJson(tagList)).build());
                dest.addArgument(ROOT_URL_KEY, new NavArgument.Builder().setDefaultValue("").build());
                dest.addArgument(GRAPH_ID_KEY, new NavArgument.Builder().setDefaultValue(tagID).build());

                NavGraph subgraph = new NavGraphBuilder(navController.getNavigatorProvider(), tagID, destID).build();
                subgraph.addDestination(dest);
                navController.getGraph().addDestination(subgraph);
                destinations.add(subgraph);
            }
        });
    }

    private void showSearch(boolean show) {
        if (menu == null)
            return;
        MenuItem searchMenu = menu.findItem(R.id.action_search);
        if (searchMenu == null)
            return;
        searchMenu.setVisible(show);
    }

    @Override
    public void addSearcher(Searcher _searcher) {
        if (_searcher == null)
            return;
        runOnUiThread(() -> {
            MainViewModel mainViewModel =
                    new ViewModelProvider(this).get(MainViewModel.class);
            mainViewModel.getmSearcher().postValue(_searcher);
        });
    }
}
