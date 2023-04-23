package com.jcomp.browser.parser.category;

import com.jcomp.browser.parser.ParserBase;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.ArrayList;
import java.util.List;

public class AvpleCategoryParser extends ParserBase<List<Category>> {
    @Override
    public List<Category> parse(Document html, String fullURL) {
        List<Category> categories = new ArrayList<>();
        Element element = html.selectFirst("script[type=\"application/ld+json\"]");
        if (element == null)
            return categories;
        try {
            JSONObject json = new JSONObject(element.html());
            JSONArray arr = json.getJSONArray("@graph");
            JSONArray second = null;
            for (int i = 0; i < arr.length(); i++) {
                JSONObject cat = arr.getJSONObject(i);
                if (cat.getString("@type").equals("BreadcrumbList"))
                    second = cat.getJSONArray("itemListElement");
                else if (cat.getString("@type").equals("Person")) {
                    second = null;
                    break;
                }
            }
            if (second == null)
                return categories;
            arr = second;
            for (int i = 0; i < arr.length(); i++) {
                JSONObject cat = arr.getJSONObject(i);
                if (!cat.getString("@type").equals("ListItem"))
                    continue;
                categories.add(new Category(cat.getString("name"), cat.getString("item")));
            }
        } catch (JSONException ignore) {

        }
        return categories;
    }
}
