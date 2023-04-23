package com.jcomp.browser.parser.searcher;

import com.jcomp.browser.parser.ParserBase;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class AvpleSearcherParser extends ParserBase<Searcher> {
    @Override
    public Searcher parse(Document html, String fullURL) {
        Element element = html.selectFirst("script[type=\"application/ld+json\"]");
        if (element == null)
            return null;
        try {
            JSONObject json = new JSONObject(element.html());
            JSONArray arr = json.getJSONArray("@graph");
            for (int i = 0; i < arr.length(); i++) {
                JSONObject cat = arr.getJSONObject(i);
                if (!cat.getString("@type").equals("WebSite"))
                    continue;
                arr = cat.getJSONArray("potentialAction");
                break;
            }
            for (int i = 0; i < arr.length(); i++) {
                JSONObject cat = arr.getJSONObject(i);
                if (!cat.getString("@type").equals("SearchAction"))
                    continue;
                String template = cat.getJSONObject("target").getString("urlTemplate").split("=")[0];
                return new Searcher(template + "={0}");
            }
        } catch (JSONException ignore) {

        }
        return null;
    }
}
