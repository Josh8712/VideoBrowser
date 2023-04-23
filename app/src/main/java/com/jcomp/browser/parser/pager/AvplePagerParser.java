package com.jcomp.browser.parser.pager;

import android.util.Pair;

import com.jcomp.browser.parser.ParserBase;

import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class AvplePagerParser extends ParserBase<Pager> {
    @Override
    public Pager parse(Document html, String fullURL) {
        List<Pair<String, String>> pages = new ArrayList<>();
        int curIndex = 0;
        Element element = html.selectFirst("#__NEXT_DATA__");
        if (element == null)
            return null;
        try {
            JSONObject json = new JSONObject(element.html());
            json = json.getJSONObject("props").getJSONObject("pageProps");
            if (!json.has("page") || !json.has("tag") || !json.has("totalPage") || !json.has("sort"))
                return null;
            int page = Integer.parseInt(json.getString("page"));
            int totalPage = Integer.parseInt(json.getString("totalPage"));
            String sort = json.getString("sort");
            String tag = json.getString("tag");
            int start = page - 3;
            int end = page + 3;
            if (start < 1)
                start = 1;
            if (end > totalPage)
                end = totalPage;
            if (start != 1)
                pages.add(new Pair<>("1", getURL(fullURL) + "tags/" + tag + "/1/" + sort));
            while (start <= end) {
                if (start == page)
                    curIndex = pages.size();
                pages.add(new Pair<>(String.valueOf(start), getURL(fullURL) + "tags/" + tag + "/" + start + "/" + sort));
                start += 1;
            }
            if (end != totalPage)
                pages.add(new Pair<>(String.valueOf(totalPage), getURL(fullURL) + "tags/" + totalPage + "/1/" + sort));
        } catch (JSONException ignore) {

        }
        if (pages.isEmpty())
            return null;
        return new Pager(pages, curIndex);
    }

    private String getURL(String fullURL) {
        try {
            URL urlClass = new URL(fullURL);
            return "https://" + urlClass.getHost() + "/";
        } catch (MalformedURLException ignore) {

        }
        return "/";
    }
}
