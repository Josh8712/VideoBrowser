package com.jcomp.browser.parser.model;

import android.os.Parcel;
import android.os.Parcelable;

public class Model implements Parcelable {
    public static final Creator<Model> CREATOR = new Creator<Model>() {
        @Override
        public Model createFromParcel(Parcel in) {
            return new Model(in);
        }

        @Override
        public Model[] newArray(int size) {
            return new Model[size];
        }
    };
    public String url;
    public String name;

    public Model(String url, String name) {
        this.url = url;
        this.name = name;
    }

    protected Model(Parcel in) {
        url = in.readString();
        name = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(url);
        parcel.writeString(name);
    }
}
