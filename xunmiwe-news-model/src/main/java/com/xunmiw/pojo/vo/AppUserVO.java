package com.xunmiw.pojo.vo;

public class AppUserVO {

    private String id;
    private String nickname;
    private String face;
    private Integer activeStatus;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getFace() {
        return face;
    }

    public void setFace(String face) {
        this.face = face;
    }

    public Integer getActiveStatus() {
        return activeStatus;
    }

    public void setActiveStatus(Integer activeStatus) {
        this.activeStatus = activeStatus;
    }

    @Override
    public String toString() {
        return "AppUserVO{" +
                "id='" + id + '\'' +
                ", nickname='" + nickname + '\'' +
                ", face='" + face + '\'' +
                ", activeStatus=" + activeStatus +
                '}';
    }
}