package com.jcomp.browser.parser.post;

import com.jcomp.browser.parser.ParserBase;
import com.jcomp.browser.parser.model.Model;
import com.jcomp.browser.parser.post.db.Post;
import com.jcomp.browser.tools.HelperFunc;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;

public class NetPostParser extends ParserBase<LinkedHashMap<String, Post>> {
    @Override
    public LinkedHashMap<String, Post> parse(Document html, String fullURL) {
        LinkedHashMap<String, Post> posts = new LinkedHashMap<>();
        Element element = html.selectFirst("#__NEXT_DATA__");
        if (element == null)
            return posts;
        try {
            JSONObject json = new JSONObject(element.html());
            json = json.getJSONObject("props").getJSONObject("initialState");
            Iterator<String> keys = json.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                if (json.isNull(key))
                    continue;
                JSONObject cat = json.getJSONObject(key);
                if (!cat.has("docs") || cat.isNull("docs"))
                    continue;
                JSONArray arr = cat.getJSONArray("docs");
                for (int i = 0; i < arr.length(); i++) {
                    cat = arr.getJSONObject(i);
                    if (!cat.has("title"))
                        continue;
                    Post post = new Post(cat.getString("title"), cat.getString("preview"), HelperFunc.fixURLWithUrl(fullURL, "/video?id=" + cat.getString("videoId")));
                    if (cat.has("actors") && !cat.isNull("actors")) {
                        JSONArray models = cat.getJSONArray("actors");
                        ArrayList<Model> modelList = new ArrayList<>();
                        if (models.length() > 0) {
                            String model;
                            for (int j = 0; j < models.length(); j++) {
                                model = models.getString(j);
                                if (model.contains(":") && model.split(":")[0].equals("zh")) {
                                    model = model.split(":")[1];
                                    modelList.add(new Model(HelperFunc.fixURLWithUrl(fullURL, "/all?actress=" + URLEncoder.encode(model, "UTF-8")), model));
                                }
                            }
                            Model[] modelArr = new Model[modelList.size()];
                            modelArr = modelList.toArray(modelArr);
                            post.set_model(modelArr);
                        }
                    }
                    posts.put(post.getKey(), post);
                }
            }
        } catch (JSONException | UnsupportedEncodingException ignore) {

        }
        return posts;
    }
}
