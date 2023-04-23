package com.jcomp.browser.parser;

import com.jcomp.browser.parser.category.AvpleCategoryParser;
import com.jcomp.browser.parser.category.Category;
import com.jcomp.browser.parser.category.JableCategoryParser;
import com.jcomp.browser.parser.category.MissCategoryParser;
import com.jcomp.browser.parser.category.NetCategoryParser;
import com.jcomp.browser.parser.category.Seselah159iCategoryParser;
import com.jcomp.browser.parser.category.TWDVDCategoryParser;
import com.jcomp.browser.parser.category.TubeCategoryParser;
import com.jcomp.browser.parser.pager.AvplePagerParser;
import com.jcomp.browser.parser.pager.CCPagerParser;
import com.jcomp.browser.parser.pager.JablePagerParser;
import com.jcomp.browser.parser.pager.MissPagerParser;
import com.jcomp.browser.parser.pager.NetPagerParser;
import com.jcomp.browser.parser.pager.Pager;
import com.jcomp.browser.parser.pager.SeselahPagerParser;
import com.jcomp.browser.parser.pager.TWDVDPagerParser;
import com.jcomp.browser.parser.pager.TubePagerParser;
import com.jcomp.browser.parser.post.AvplePostParser;
import com.jcomp.browser.parser.post.CCPostParser;
import com.jcomp.browser.parser.post.II159PostParser;
import com.jcomp.browser.parser.post.JablePostParser;
import com.jcomp.browser.parser.post.MissPostParser;
import com.jcomp.browser.parser.post.NetPostParser;
import com.jcomp.browser.parser.post.SeselahPostParser;
import com.jcomp.browser.parser.post.TWDVDPostParser;
import com.jcomp.browser.parser.post.TubePostParser;
import com.jcomp.browser.parser.post.db.Post;
import com.jcomp.browser.parser.searcher.AvpleSearcherParser;
import com.jcomp.browser.parser.searcher.II159SearcherParser;
import com.jcomp.browser.parser.searcher.JableTubeSearcherParser;
import com.jcomp.browser.parser.searcher.MissSearcherParser;
import com.jcomp.browser.parser.searcher.Searcher;
import com.jcomp.browser.parser.searcher.SeselahSearcherParser;
import com.jcomp.browser.parser.tag.AvpleTagParser;
import com.jcomp.browser.parser.tag.CCTagParser;
import com.jcomp.browser.parser.tag.JableImageTagParser;
import com.jcomp.browser.parser.tag.JableTagParser;
import com.jcomp.browser.parser.tag.MissTagParser;
import com.jcomp.browser.parser.tag.NetImageTagParser;
import com.jcomp.browser.parser.tag.SeselahImageTagParser;
import com.jcomp.browser.parser.tag.SeselahTagParser;
import com.jcomp.browser.parser.tag.Tag;
import com.jcomp.browser.parser.tag.TubeImageTagParser;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class Parser {
    public static final String NULL_CHAR = "undefined";
    public static final String BLOB_CHAR = "blob";
    private static final Class[] POST_PARSERS = {
            JablePostParser.class, JablePostParser.class, JableImageTagParser.class, AvplePostParser.class, TubePostParser.class, TubeImageTagParser.class, CCPostParser.class, NetPostParser.class, NetImageTagParser.class, SeselahPostParser.class, SeselahImageTagParser.class, II159PostParser.class, TWDVDPostParser.class, MissPostParser.class
    };
    private static final Class[] Category_PARSERS = {
            JableCategoryParser.class, AvpleCategoryParser.class, TubeCategoryParser.class, NetCategoryParser.class, Seselah159iCategoryParser.class, TWDVDCategoryParser.class, MissCategoryParser.class
    };
    private static final Class[] Tag_PARSERS = {
            JableTagParser.class, AvpleTagParser.class, CCTagParser.class, SeselahTagParser.class, MissTagParser.class
    };
    private static final Class[] Pager_PARSERS = {
            JablePagerParser.class, AvplePagerParser.class, TubePagerParser.class, CCPagerParser.class, NetPagerParser.class, SeselahPagerParser.class, TWDVDPagerParser.class, MissPagerParser.class
    };
    private static final Class[] Search_PARSERS = {
            JableTubeSearcherParser.class, AvpleSearcherParser.class, SeselahSearcherParser.class, II159SearcherParser.class, MissSearcherParser.class
    };
    Document html;
    String fullURL;

    public Parser(String fullURL, String html) {
        Document root = Jsoup.parse(html);
        this.html = root;
        this.fullURL = fullURL;
    }

    public void isTarget(TargetCallbackInterface callback) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                LinkedHashMap<String, Post> posts = getPost();
                if (posts.isEmpty())
                    callback.onFinished();
                else
                    callback.onTarget();
            }
        }).start();
    }


    public LinkedHashMap<String, Post> getPost() {
        LinkedHashMap<String, Post> posts = new LinkedHashMap<>();
        for (Class<ParserBase<LinkedHashMap<String, Post>>> parser : POST_PARSERS) {
            try {
                posts.putAll(parser.newInstance().parse(html, fullURL));
            } catch (Exception ignore) {
            }
        }
        return posts;
    }

    public List<Category> getCategory() {
        List<Category> categories = new ArrayList<>();
        for (Class<ParserBase<List<Category>>> parser : Category_PARSERS) {
            try {
                categories.addAll(parser.newInstance().parse(html, fullURL));
            } catch (Exception ignore) {
            }
        }
        return categories;
    }

    public List<Tag> getTag() {
        List<Tag> list = new ArrayList<>();
        for (Class<ParserBase<List<Tag>>> parser : Tag_PARSERS) {
            try {
                list.addAll(parser.newInstance().parse(html, fullURL));
            } catch (Exception ignore) {
            }
        }
        return list;
    }

    public Pager getPager() {
        for (Class<ParserBase<Pager>> parser : Pager_PARSERS) {
            try {
                Pager pager = parser.newInstance().parse(html, fullURL);
                if (pager != null)
                    return pager;
            } catch (Exception ignore) {
            }
        }
        return null;
    }

    public Searcher getSearcher() {
        for (Class<ParserBase<Searcher>> parser : Search_PARSERS) {
            try {
                Searcher searcher = parser.newInstance().parse(html, fullURL);
                if (searcher != null)
                    return searcher;
            } catch (Exception ignore) {
            }
        }
        return null;
    }

    public interface TargetCallbackInterface {
        void onFinished();

        void onTarget();
    }
}
