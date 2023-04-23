package com.jcomp.browser.parser.pager;

import android.util.Pair;

import com.jcomp.browser.parser.ParserBase;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

public class TubePagerParser extends ParserBase<Pager> {
    @Override
    public Pager parse(Document html, String fullURL) {
        List<Pair<String, String>> pages = new ArrayList<>();
        int curIndex = -1;
        Elements elements = html.select(".pagination li");
        for (Element element : elements) {
            String pageName = element.text();
            if (pageName.trim().isEmpty())
                continue;
            Elements a = element.select("a");
            String url = null;
            if (element.attr("class").equals("page-current"))
                curIndex = pages.size();
            else if (a.isEmpty())
                continue;
            else {
                String params = a.get(0).attr("data-parameters");
                String blockID = a.get(0).attr("data-block-id");
                url = fullURL.split("\\?")[0] + "?mode=async&function=get_block&block_id=" + blockID;
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
        if (pages.isEmpty() || curIndex < 0)
            return null;
        return new Pager(pages, curIndex);
    }
}
