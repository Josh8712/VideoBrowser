package com.jcomp.browser.parser.post;

import android.net.Uri;

import com.jcomp.browser.parser.ParserBase;
import com.jcomp.browser.parser.post.db.Post;
import com.jcomp.browser.tools.HelperFunc;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.LinkedHashMap;

public class TWDVDPostParser extends ParserBase<LinkedHashMap<String, Post>> {
    @Override
    public LinkedHashMap<String, Post> parse(Document html, String fullURL) {
        LinkedHashMap<String, Post> posts = new LinkedHashMap<>();
        Elements elements = html.select(".blog_subject");
        for (Element element : elements) {
            Element frame = element.nextElementSibling();
            if (frame == null)
                continue;
            frame = frame.selectFirst("iframe");
            if (frame == null)
                continue;
            Elements title = element.select("b");

            String url = HelperFunc.fixURLWithUrl(fullURL, frame.attr("src"));
            url += "&play=1";
            String imgURL = Uri.parse(url).getQueryParameter("image");
            imgURL = HelperFunc.fixURLWithUrl(fullURL, imgURL);
            Post post = new Post(title.get(0).text(), imgURL, url);
            posts.put(post.getKey(), post);
        }
        return posts;
    }
}
