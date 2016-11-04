package com.selfi.android.data.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Ahmed on 31/07/2016.
 */

public class SelfiImage implements Parcelable , Comparable<SelfiImage> {

    private String image;
    private String authorId;
    private String id;
    private String tag;
    private double likes;
    private double color;


    public SelfiImage(String image,double color, String authorId, String tag) {
        this.image = image;
        this.authorId = authorId;
        this.tag = tag;
        this.color = color;
    }

    public SelfiImage() {
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getAuthorId() {
        return authorId;
    }

    public void setAuthorId(String authorId) {
        this.authorId = authorId;
    }


    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public double getLikes() {
        return likes;
    }

    public void setLikes(double likes) {
        this.likes = likes;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel pc, int flags) {
        pc.writeDouble(likes);
        pc.writeString(image);
        pc.writeString(id);
        pc.writeString(authorId);
        pc.writeString(tag);
        pc.writeDouble(color);
    }

    public SelfiImage(Parcel pc) {
        likes = pc.readDouble();
        image = pc.readString();
        id = pc.readString();
        authorId = pc.readString();
        tag = pc.readString();
        color = pc.readDouble();

    }

    public static final Parcelable.Creator<SelfiImage> CREATOR = new Parcelable.Creator<SelfiImage>() {
        public SelfiImage createFromParcel(Parcel pc) {
            return new SelfiImage(pc);
        }

        public SelfiImage[] newArray(int size) {
            return new SelfiImage[size];
        }

    };

    public double getColor() {
        return color;
    }

    public void setColor(double color) {
        this.color = color;
    }


    @Override
    public int compareTo(SelfiImage another) {
        return Double.compare(this.getLikes(),another.getLikes());
    }
}
