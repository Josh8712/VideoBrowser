package com.jcomp.browser.browser;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Base64;
import android.util.Patterns;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupMenu;

import androidx.annotation.Nullable;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.gson.Gson;
import com.jcomp.browser.AppDatabase;
import com.jcomp.browser.R;
import com.jcomp.browser.history.db.History;
import com.jcomp.browser.menu.Popup;
import com.jcomp.browser.parser.Parser;
import com.jcomp.browser.parser.ParserCommonCallback;
import com.jcomp.browser.parser.ParserJumpCallback;
import com.jcomp.browser.parser.ParserPageCallback;
import com.jcomp.browser.parser.ParserPostPageCallback;
import com.jcomp.browser.parser.ParserVideoCallback;
import com.jcomp.browser.parser.ScriptHLS;
import com.jcomp.browser.parser.model.Model;
import com.jcomp.browser.parser.model.ModelCache;
import com.jcomp.browser.parser.model.ScriptModelName;
import com.jcomp.browser.parser.model.ScriptModelURL;
import com.jcomp.browser.parser.player.ScriptPlayer;
import com.jcomp.browser.parser.post.db.Post;
import com.jcomp.browser.parser.tag.ScriptTag;
import com.jcomp.browser.player.Player;
import com.jcomp.browser.player.VideoPlayerInfo;
import com.jcomp.browser.tools.HelperFunc;
import com.jcomp.browser.widget.HideEditText;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.LinkedHashMap;

import kotlin.text.Charsets;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class Browser extends LinearLayout {
    public final static String BLANK_URL = "about:blank";
    static final int CHECK_INTERVAL = 1000;
    VideoPlayerInfo playerInfo = new VideoPlayerInfo();
    String url = "";
    boolean dirty = true;
    boolean fragmentReady = false;
    long dirtyFlag = 0;
    int checkCount = 0;
    WebView webView;
    HideEditText urlEditor;
    SwipeRefreshLayout refreshLayout;
    long lastCheck = 0;
    ParserPageCallback pageCallback;
    ParserVideoCallback videoCallback;
    ParserCommonCallback commonCallback;
    ParserPostPageCallback parserPostPageCallback;
    ParserJumpCallback jumpCallback;
    boolean exit = false;
    private Button playVideo, listVideo;
    private View homeButton;
    private ImageButton bookmark;
    private Handler mHandler;

    public Browser(Context context) {
        super(context);
    }

    public Browser(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public Browser(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @SuppressLint("ClickableViewAccessibility")
    public void init(Handler handler) {
        View optionButton = findViewById(R.id.option);
        webView = findViewById(R.id.webView);
        homeButton = findViewById(R.id.home);
        urlEditor = findViewById(R.id.url);
        refreshLayout = findViewById(R.id.refresh);
        bookmark = findViewById(R.id.bookmark_button);
        playVideo = ((ViewGroup) getParent()).findViewById(R.id.video);
        listVideo = ((ViewGroup) getParent()).findViewById(R.id.list);
        setupParser(handler);

        optionButton.setOnTouchListener((view, ev) -> {
            if (ev.getAction() != MotionEvent.ACTION_DOWN)
                return false;
            PopupMenu menu = new PopupMenu(getContext(), optionButton);
            menu.getMenu().add(Menu.NONE, 1, 1, R.string.reload);
            menu.getMenu().add(Menu.NONE, Popup.browserID, 1, R.string.open_in_browser);
            menu.getMenu().add(Menu.NONE, Popup.shareID, 1, R.string.share_to);
            menu.setOnMenuItemClickListener(item -> {
                switch (item.getItemId()) {
                    case 1:
                        HelperFunc.hideKeyboard(view);
                        if (refreshLayout.isRefreshing()) {
                            webView.stopLoading();
                        } else {
                            webView.reload();
                        }
                        break;
                    case Popup.browserID:
                        Popup.browserCallback(view.getContext(), url);
                        break;
                    case Popup.shareID:
                        Popup.shareCallback(view.getContext(), getBrowser().getTitle(), url);
                        break;
                }
                return true;
            });
            menu.show();
            return true;
        });

        refreshLayout.setOnRefreshListener(() -> webView.reload());

        urlEditor.setOnFocusChangeListener((view, b) -> {
            if (!b) {
                urlEditor.setSelection(0);
            }
            urlEditor.onFocusChange(view, b);
        });

        urlEditor.setOnKeyListener((view, i, keyEvent) -> {
            if (keyEvent.getAction() == KeyEvent.ACTION_UP && keyEvent.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                if (urlEditor.getText() == null)
                    return false;
                loadUrl(urlEditor.getText().toString(), false);
                HelperFunc.hideKeyboard(view);
                view.clearFocus();
                return true;
            }
            return false;
        });

        findViewById(R.id.clear_input_button).setOnClickListener(view -> {
            urlEditor.setText("");
            urlEditor.requestFocus();
        });

        WebSettings webSettings = webView.getSettings();
        webSettings.setDisplayZoomControls(true);
        webSettings.setSupportZoom(true);
        webSettings.setJavaScriptCanOpenWindowsAutomatically(false);
        webSettings.setSupportMultipleWindows(false);
        webSettings.setJavaScriptEnabled(true);
        webView.setWebViewClient(getWebViewClient());
        webView.setWebChromeClient(getWebChromeClient());
        playVideo.setOnClickListener(item -> navigateToPlay());
        listVideo.setOnClickListener(item -> navigateToList());
    }

    public void showPlayButton() {
        playVideo.post(() -> {
            playerInfo.setPost(new Post(getBrowser().getTitle(), null, getBrowser().getUrl()));
            playVideo.setVisibility(VISIBLE);
        });
    }

    public void navigateToPlay() {
        if (playerInfo.streamable()) {
            Intent intent = new Intent(getContext(), Player.class);
            intent.putExtra(Player.PLAYER_INFO_KEY, new Gson().toJson(playerInfo));
            getContext().startActivity(intent);
        }
    }

    public void showListButton() {
        listVideo.post(() -> {
            listVideo.setVisibility(VISIBLE);
        });
    }

    public void navigateToList() {
        if (jumpCallback != null)
            jumpCallback.navigateToList(getHistory());
    }

    public void setHomeButtonCallback(View.OnClickListener listener) {
        homeButton.setOnClickListener(listener);
    }

    private WebChromeClient getWebChromeClient() {
        return new WebChromeClient() {
            @Override
            public void onReceivedIcon(WebView view, Bitmap icon) {
                super.onReceivedIcon(view, icon);
                if (parserPostPageCallback != null)
                    parserPostPageCallback.pageInfoFinished();
            }
        };
    }

    private WebViewClient getWebViewClient() {
        return new WebViewClient() {
            private final HashMap<String, byte[]> pageCache = new HashMap<>();
            private long checkFlag;

            @Override
            public void doUpdateVisitedHistory(WebView view, String url, boolean isReload) {
                super.doUpdateVisitedHistory(view, url, isReload);
                setURL(url);
                checkFlag = dirtyFlag;
            }

            private boolean handleIntent(String url) {
                if (url.startsWith("intent://")) {
                    try {
                        Context context = getContext();
                        Intent intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME);
                        if (intent != null) {
                            PackageManager packageManager = context.getPackageManager();
                            ResolveInfo info = packageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY);
                            if (info != null) {
                                context.startActivity(intent);
                            } else {
                                String fallbackUrl = intent.getStringExtra("browser_fallback_url");
                                loadUrl(fallbackUrl, false);
                            }
                            return true;
                        }
                    } catch (URISyntaxException e) {
                        e.printStackTrace();
                    }
                }
                return false;
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                if (getVisibility() != VISIBLE)
                    return true;
                try {
                    if (new URL(request.getUrl().toString()).getHost().equals(new URL(url).getHost()))
                        return false;
                    DialogInterface.OnClickListener dialogClickListener = (dialog, which) -> {
                        switch (which) {
                            case DialogInterface.BUTTON_POSITIVE:
                                //Yes button clicked
                                view.loadUrl(request.getUrl().toString());
                                break;

                            case DialogInterface.BUTTON_NEGATIVE:
                                //No button clicked
                                break;
                        }
                    };

                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    builder.setTitle(R.string.should_browse).setMessage(request.getUrl().toString())
                            .setPositiveButton(R.string.yes, dialogClickListener)
                            .setNegativeButton(R.string.no, dialogClickListener).show();
                    return true;
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                    return handleIntent(request.getUrl().toString());
                }
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                refreshLayout.setRefreshing(true);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                view.loadUrl("javascript:window.open=console.log");
                refreshLayout.setRefreshing(false);
                checkCount = 0;
                if (checkFlag == dirtyFlag)
                    dirty = false;
            }

            @Override
            public void onPageCommitVisible(WebView view, String url) {
                super.onPageCommitVisible(view, url);
                checkCount = 0;
                if (checkFlag == dirtyFlag)
                    dirty = false;
            }

            public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                String url = request.getUrl().toString();
                if (!url.equals(Browser.this.url) || !(url.contains("Player") || url.contains("vvv.php"))) {
                    return null;
                }
                if (pageCache.size() > 10)
                    pageCache.clear();
                if (pageCache.containsKey(url)) {
                    InputStream targetStream = new ByteArrayInputStream(pageCache.get(url));
                    return new WebResourceResponse("text/html", "utf-8", targetStream);
                }
                OkHttpClient.Builder okHttpClient = new OkHttpClient().newBuilder();
                WebResourceResponse response = null;
                try {
                    Response r = okHttpClient.build().newCall(
                            new Request.Builder()
                                    .url(url)
                                    .header("Referer", url)
                                    .build()).execute();

                    byte[] content = r.body().bytes();
                    InputStream targetStream = new ByteArrayInputStream(content);
                    pageCache.put(request.getUrl().toString(), content);
                    response = new WebResourceResponse("text/html", "utf-8", targetStream);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (response == null)
                    return super.shouldInterceptRequest(view, request);
                else
                    return response;
            }
        };
    }

    public void loadScript(String url) {
        webView.loadUrl(url);
    }

    private void prepareNewPage(String url) {
        webView.stopLoading();
        webView.loadUrl(BLANK_URL);
        setURL(url);
    }

    private void setURL(String url) {
        this.url = url;
        setDirty();
        urlEditor.setText(url);
        new Thread(() -> {
            History history = AppDatabase.getInstance(getContext()).historyDoa().getByPath(url);
            boolean isBookmark = history != null;
            if (isBookmark)
                bookmark.setImageResource(R.drawable.baseline_bookmark_24);
            else
                bookmark.setImageResource(R.drawable.baseline_bookmark_border_24);
        }).start();
    }

    public void loadUrl(String url, boolean cleanHistory) {
        if(url == null || url.isEmpty())
            return;
        prepareNewPage(url);
        url = url.trim();
        if (!Patterns.WEB_URL.matcher(url).matches())
            url = buildQueryURL(url);
        try {
            new URL(url);
        } catch (Exception e) {
            url = buildQueryURL(url);
        }
        if (cleanHistory)
            webView.clearHistory();
        refreshLayout.setRefreshing(true);
        webView.loadUrl(url);
    }

    String buildQueryURL(String keyword) {
        String url = "https://www.google.com/search?q=";
        try {
            url += URLEncoder.encode(keyword.trim(), Charsets.UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        return url;
    }

    public String getUrl() {
        return url;
    }

    @SuppressLint("JavascriptInterface")
    public void addJavascriptInterface(Object obj, String name) {
        webView.addJavascriptInterface(obj, name);
    }

    public WebView getBrowser() {
        return webView;
    }

    public void toggleVisibility() {
        if (getVisibility() == VISIBLE)
            setVisibility(GONE);
        else
            setVisibility(VISIBLE);
    }

    @Override
    public int getVisibility() {
        View root = (View) getParent();
        if (root == null)
            return GONE;
        return root.getVisibility();
    }

    @Override
    public void setVisibility(int visibility) {
        View root = (View) getParent();
        if (root == null)
            return;
        root.setVisibility(visibility);
    }

    public void setupParser(Handler handler) {
        homeButton.setOnClickListener(view -> setVisibility(GONE));
        addJavascriptInterface(this, "HtmlViewer");
        mHandler = handler;
        parseWebsite(true);
    }

    public void setDirty() {
        dirtyFlag += 1;
        dirty = true;
        playVideo.setVisibility(GONE);
        listVideo.setVisibility(GONE);
        playerInfo = new VideoPlayerInfo();
    }

    public void setFragmentReady(boolean fragmentReady) {
        if (!fragmentReady) {
            setDirty();
            pageCallback = null;
            videoCallback = null;
            commonCallback = null;
            parserPostPageCallback = null;
        }
        this.fragmentReady = fragmentReady;
    }

    public void registerCallback(ParserPageCallback pageCallback) {
        setDirty();
        if (pageCallback != null)
            this.pageCallback = pageCallback;
    }

    public void registerCallback(ParserVideoCallback videoCallback) {
        setDirty();
        if (videoCallback != null)
            this.videoCallback = videoCallback;
    }

    public void registerCallback(ParserCommonCallback commonCallback) {
        setDirty();
        if (commonCallback != null)
            this.commonCallback = commonCallback;
    }

    public void registerCallback(ParserPostPageCallback parserPagerCallback) {
        setDirty();
        if (parserPagerCallback != null)
            this.parserPostPageCallback = parserPagerCallback;
    }

    public void registerCallback(ParserJumpCallback jumpCallback) {
        setDirty();
        if (jumpCallback != null)
            this.jumpCallback = jumpCallback;
    }

    private void parseWebsite(boolean force) {
        if (exit)
            return;
        if (System.currentTimeMillis() - lastCheck < CHECK_INTERVAL || dirty || !fragmentReady) {
            if (force && mHandler != null) {
                mHandler.postDelayed(() -> parseWebsite(true), CHECK_INTERVAL);
            }
            return;
        }
        lastCheck = System.currentTimeMillis();
        checkCount += 1;
        if (checkCount > 10) {
            if (checkCount == 11)
                if (commonCallback != null)
                    commonCallback.parseFinished();
        } else {
            loadScript("javascript:window.HtmlViewer.getHTML(window.location.href " +
                    ", '<html>'+document.getElementsByTagName('html')[0].innerHTML+'</html>')");
            parseHLS();
            openTag();
            parseModel();
            parsePlayer();
        }

        mHandler.postDelayed(() -> parseWebsite(force), CHECK_INTERVAL);
    }

    private void parseHLS() {
        for (String s : ScriptHLS.SCRIPT_SET) {
            loadScript("javascript:window.HtmlViewer.getHLS(window.location.href, " + s + ")");
        }
    }

    private void parsePlayer() {
        loadScript("javascript:window.HtmlViewer.getPlayerList(window.location.href, " + ScriptPlayer.CC + ")");
        loadScript("javascript:window.HtmlViewer.getPlayerList(window.location.href, " + ScriptPlayer.Net + ")");
        loadScript("javascript:window.HtmlViewer.getPlayerList(window.location.href, " + ScriptPlayer.TWDVD + ")");
    }

    private void openTag() {
        loadScript("javascript:" + ScriptTag.Avple);
    }

    private void parseModel() {
        loadScript("javascript:window.HtmlViewer.getModel(window.location.href, " + ScriptModelURL.Jable + "," + ScriptModelName.Jable + ")");
    }

    @JavascriptInterface
    public void getHTML(String location, String html) {
        Parser parser = new Parser(location, html);
        ModelCache cache = ModelCache.getSingleton(getContext());
        LinkedHashMap<String, Post> posts = parser.getPost();
        posts.putAll(parser.getComic());
        for (Post post : posts.values()) {
            post.set_model(cache.getModel(post.url));
        }
        if (commonCallback != null)
            commonCallback.addPost(posts);
        if (jumpCallback != null && !posts.isEmpty())
            showListButton();
        if (parserPostPageCallback != null)
            parserPostPageCallback.addPager(parser.getPager());
        if (pageCallback != null) {
            pageCallback.addCategory(parser.getCategory());
            pageCallback.addTagList(parser.getTag());
            pageCallback.addSearcher(parser.getSearcher());
        }
    }

    @JavascriptInterface
    public void getHLS(String location, String hls, String preview) {
        if (!hls.equals(Parser.NULL_CHAR) && !hls.startsWith(Parser.BLOB_CHAR)) {
            if (preview != null && preview.equals(Parser.NULL_CHAR))
                preview = null;
            hls = org.jsoup.parser.Parser.unescapeEntities(hls, true);
            hls = HelperFunc.fixURLWithUrl(location, hls);
            if (videoCallback != null)
                videoCallback.addHLS(hls, preview);
            if (hls != null) {
                playerInfo.setStream(hls, preview);
                showPlayButton();
            }

        }
    }

    @JavascriptInterface
    public void getHLS(String location, String hls) {
        getHLS(location, hls, null);
    }

    @JavascriptInterface
    public void getModel(String location, String[] modelURLList, String[] modelNameList) {
        if (videoCallback == null)
            return;
        if (modelURLList.length == 0)
            return;
        Model[] models = new Model[modelURLList.length];
        for (int i = 0; i < modelURLList.length; i++) {
            models[i] = new Model(modelURLList[i], modelNameList[i]);
        }
        ModelCache cache = ModelCache.getSingleton(getContext());
        if (cache.getModel(location) == null)
            ModelCache.getSingleton(getContext()).putModel(location, models);
    }

    @JavascriptInterface
    public void getPlayerList(String location, String[] playerList) {
        if (playerList != null && playerList.length > 0) {
            for (int i = 0; i < playerList.length; i++)
                playerList[i] = HelperFunc.fixURLWithUrl(location, playerList[i]);
            if (videoCallback != null)
                videoCallback.addPlayerList(playerList);
        }
    }

    public void destroy() {
        exit = true;
        webView.stopLoading();
        webView.destroy();
        webView = null;
        mHandler = null;
    }

    public String getTitle() {
        return getBrowser().getTitle();
    }

    public History getHistory() {
        WebView browser = getBrowser();
        if (browser == null)
            return null;
        Bitmap favicon = browser.getFavicon();
        String faviconString = null;
        if (favicon != null) {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            favicon.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
            byte[] byteArray = byteArrayOutputStream.toByteArray();
            faviconString = Base64.encodeToString(byteArray, Base64.DEFAULT);
        }
        return new History(getTitle(), getUrl(), faviconString);
    }

}
