package com.jcomp.browser.parser.tag;

import com.jcomp.browser.parser.ParserBase;
import com.jcomp.browser.tools.HelperFunc;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class AnimezillaTagParser extends ParserBase<List<Tag>> {
    @Override
    public List<Tag> parse(Document html, String fullURL) {
        List<Tag> list = new ArrayList<>();
        Elements elements = html.select("#main-manga-parody li a");
        for (Element element : elements) {
            String title = element.ownText();
            String url = HelperFunc.fixURLWithUrl(fullURL, element.attr("href"));
            boolean sameSite = false;
            try {
                sameSite = new URL(url).getHost().equals(new URL(fullURL).getHost());
            } catch (MalformedURLException ignore) {}
            if(title.trim().isEmpty() || !sameSite)
                continue;
            Tag tag = new Tag(title, null, url);
            list.add(tag);
        }
        return list;
    }
}
