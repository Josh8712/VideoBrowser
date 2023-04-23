package com.jcomp.browser.parser.post;

import com.jcomp.browser.parser.ParserBase;
import com.jcomp.browser.parser.post.db.Post;
import com.jcomp.browser.tools.HelperFunc;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.Iterator;
import java.util.LinkedHashMap;

public class AvplePostParser extends ParserBase<LinkedHashMap<String, Post>> {
    @Override
    public LinkedHashMap<String, Post> parse(Document html, String fullURL) {
        LinkedHashMap<String, Post> posts = new LinkedHashMap<>();
        homepage(html, posts, fullURL);
        page(html, posts, fullURL);
        return posts;
    }

    private void homepage(Document html, LinkedHashMap<String, Post> posts, String fullURL) {
        Element element = html.selectFirst("#__NEXT_DATA__");
        if (element == null)
            return;
        try {
            JSONObject json = new JSONObject(element.html());
            json = json.getJSONObject("props").getJSONObject("pageProps").getJSONObject("indexListObj");
            Iterator<String> keys = json.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                if (json.isNull(key))
                    continue;
                JSONArray arr = json.getJSONArray(key);
                for (int i = 0; i < arr.length(); i++) {
                    JSONObject cat = arr.getJSONObject(i);
                    if (!cat.has("title"))
                        continue;
                    Post post = new Post(cat.getString("title"), cat.getString("img_preview"), HelperFunc.fixURLWithUrl(fullURL, "/video/" + cat.getString("_id")));
                    posts.put(post.getKey(), post);
                }
            }
        } catch (JSONException ignore) {

        }
    }

    private void page(Document html, LinkedHashMap<String, Post> posts, String fullURL) {
        Element element = html.selectFirst("#__NEXT_DATA__");
        if (element == null)
            return;
        try {
            JSONObject json = new JSONObject(element.html());
            JSONArray arr = json.getJSONObject("props").getJSONObject("pageProps").getJSONArray("data");
            for (int i = 0; i < arr.length(); i++) {
                JSONObject cat = arr.getJSONObject(i);
                if (!cat.has("title"))
                    continue;
                Post post = new Post(cat.getString("title"), cat.getString("img_preview"), HelperFunc.fixURLWithUrl(fullURL, "/video/" + cat.getString("_id")));
                posts.put(post.getKey(), post);
            }
        } catch (JSONException ignore) {

        }
    }
}
