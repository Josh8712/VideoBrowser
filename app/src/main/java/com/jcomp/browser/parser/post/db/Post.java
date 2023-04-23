package com.jcomp.browser.parser.post.db;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverter;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.jcomp.browser.parser.model.Model;

import java.lang.reflect.Type;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Entity
public class Post {
    @PrimaryKey(autoGenerate = true)
    public long uid;

    public String title;
    public String img;
    public String url;
    public int showScale = 1;
    public int viewType = -1;

    public Model[] model;
    public String streamName;

    public Post() {
    }

    @Ignore
    public Post(String title, String streamName, String img, @NonNull String url) {
        this.title = title;
        this.streamName = streamName;
        this.img = img;
        this.url = url;
    }

    @Ignore
    public Post(String title, String img, @NonNull String url) {
        this.title = title;
        this.img = img;
        this.url = url;
    }

    public String getVideoID() {
        if (title == null)
            return null;
        Pattern pattern = Pattern.compile("[a-zA-Z\\d]+[ -]*[a-zA-Z\\d]+[ -]*[a-zA-Z\\d]+", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(title);
        boolean matchFound = matcher.find();
        if (matchFound) {
            String id = matcher.group().trim();
            if (id.replaceAll(" ", "").length() < 4)
                return null;
            return id;
        }
        return null;
    }

    public String getTitle() {
        if (streamName != null)
            return title + streamName;
        return title;
    }

    public int getViewType() {
        return viewType;
    }

    public String getKey() {
        return url;
    }

    public boolean isVideo() {
        return true;
    }

    public int getShowScale() {
        return showScale;
    }

    public void set_model(Model[] modelArr) {
        if (modelArr == null)
            return;
        this.model = modelArr;
    }

    public void setStreamName(String streamName) {
        this.streamName = streamName;
    }

    public static class Converters {
        @TypeConverter
        public static Model[] fromString(String value) {
            Type listType = new TypeToken<Model[]>() {
            }.getType();
            return new Gson().fromJson(value, listType);
        }

        @TypeConverter
        public static String fromArrayList(Model[] list) {
            Gson gson = new Gson();
            String json = gson.toJson(list);
            return json;
        }
    }
}
