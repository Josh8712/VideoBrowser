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

public class PostWithPlayList extends Post {
    public long playlistID;
    public PostWithPlayList() {
    }
}
