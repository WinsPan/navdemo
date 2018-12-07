package com.test.navdemo;

public class MarketBean {
    /**
     * 纬度
     */
    private double latitude;
    /**
     * 经度
     */
    private double longitude;
    /**
     * 标题
     */
    private String title;
    /**
     * 内容
     */
    private String content;

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public MarketBean(double latitude, double longitude, String title, String content) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.title = title;
        this.content = content;
    }
}