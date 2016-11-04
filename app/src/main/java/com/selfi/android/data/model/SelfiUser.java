package com.selfi.android.data.model;

/**
 * Created by Ahmed on 02/08/2016.
 */

public class SelfiUser {
    private String name;
    private String fbId;
    private String email;
    private String profileImg;
    private String coverImg;
    private int points;

    public SelfiUser(String fbId,String name, String email, String image,String coverImg, int points) {
        this.setName(name);
        this.setEmail(email);
        this.setProfileImg(image);
        this.setPoints(points);
        this.setCoverImg(coverImg);
        this.fbId = fbId;
    }

    public SelfiUser() {
    }


    public String getCoverImg() {
        return coverImg;
    }

    public void setCoverImg(String coverImg) {
        this.coverImg = coverImg;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getProfileImg() {
        return profileImg;
    }

    public void setProfileImg(String profileImg) {
        this.profileImg = profileImg;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    public String getFbId() {
        return fbId;
    }

    public void setFbId(String fbId) {
        this.fbId = fbId;
    }
}
