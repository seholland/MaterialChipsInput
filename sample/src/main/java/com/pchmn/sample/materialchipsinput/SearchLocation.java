package com.pchmn.sample.materialchipsinput;

import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;
import com.pchmn.materialchips.model.ChipInterface;

public class SearchLocation implements ChipInterface, Parcelable {

    public static final Parcelable.Creator<SearchLocation> CREATOR = new Parcelable.Creator<SearchLocation>() {
        @Override
        public SearchLocation createFromParcel(Parcel source) {
            return new SearchLocation(source);
        }

        @Override
        public SearchLocation[] newArray(int size) {
            return new SearchLocation[size];
        }
    };
    @SerializedName("displayName")
    private String displayName;
    @SerializedName("id")
    private String id;
    @SerializedName("type")
    private String type;

    public SearchLocation() {
    }

    protected SearchLocation(Parcel in) {
        this.displayName = in.readString();
        this.id = in.readString();
        this.type = in.readString();
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public Uri getAvatarUri() {
        return null;
    }

    @Override
    public Drawable getAvatarDrawable() {
        return null;
    }

    @Override
    public String getLabel() {
        return displayName;
    }

    @Override
    public String getInfo() {
        return id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return
                "SearchLocation{" +
                        "displayName = '" + displayName + '\'' +
                        ",id = '" + id + '\'' +
                        ",type = '" + type + '\'' +
                        "}";
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.displayName);
        dest.writeString(this.id);
        dest.writeString(this.type);
    }
}