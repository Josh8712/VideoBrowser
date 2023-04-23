package com.jcomp.browser.parser.pager;

import android.util.Pair;

import com.jcomp.browser.parser.ParserBase;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

public class JablePagerParser extends ParserBase<Pager> {
    @Override
    public Pager parse(Document html, String fullURL) {
        Elements elements = html.select(".pagination .page-item");
        List<Pair<String, String>> pages = new ArrayList<>();
        int curIndex = 0;
        for (Element element : elements) {
            String pageName = element.text();
            String url = null;
            Elements a = element.select("a");
            if (a.isEmpty())
                curIndex = pages.size();
            else {
                String params = a.get(0).attr("data-parameters");
                String blockID = a.get(0).attr("data-block-id");
                url = fullURL + "?" + "mode=async&function=get_block&block_id=" + blockID;
                for (String s : params.split(";")) {
                    String[] tag = s.split(":");
                    if (tag.length < 2)
                        continue;
                    String[] queries = tag[0].split("\\+");
                    for (String q : queries) {
                        url += "&" + q + "=" + tag[1];
                    }
                }
            }
            pages.add(new Pair<>(pageName, url));
        }
        if (pages.isEmpty())
            return null;
        return new Pager(pages, curIndex);
    }
}
