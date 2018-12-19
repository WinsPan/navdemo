package com.test.navdemo.orm;

import com.litesuits.orm.db.annotation.Column;
import com.litesuits.orm.db.annotation.PrimaryKey;
import com.litesuits.orm.db.annotation.Table;
import com.litesuits.orm.db.enums.AssignType;

import java.util.Date;

@Table("marketbean")
public class MarketBean extends BaseModel {
    @PrimaryKey(AssignType.AUTO_INCREMENT)
    @Column("_id")
    public long id;

    /**
     * 纬度
     */
    @Column("latitude")
    private double latitude;
    /**
     * 经度
     */
    @Column("longitude")
    private double longitude;
    /**
     * 标题
     */
    @Column("title")
    private String title;
    /**
     * 地址
     */
    @Column("address")
    private String address;
    /**
     * 内容
     */
    @Column("content")
    private String content;

    @Column("createTime")
    private Date createTime;

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

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}