package cn.xianging.photoselector;

import java.io.Serializable;

/**
 * Created by xiaoz on 16/3/28.
 * Copyright © 2016年 xianging. All rights reserved.
 */
public class PhotoInfo implements Serializable {

    private long photoId;
    private String path;
    private int width;
    private int height;

    public PhotoInfo(String path) {
        this.path = path;
    }

    /** setter and getter */

    public long getPhotoId() {
        return photoId;
    }

    public void setPhotoId(long photoId) {
        this.photoId = photoId;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    /** override */

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        PhotoInfo photoInfo = (PhotoInfo) o;

        if (photoId != photoInfo.photoId) {
            return false;
        }
        if (width != photoInfo.width) {
            return false;
        }
        if (height != photoInfo.height) {
            return false;
        }
        return !(path != null ? !path.equals(photoInfo.path) : photoInfo.path != null);

    }

    @Override
    public int hashCode() {
        int result = path != null ? path.hashCode() : 0;
        result = 31 * result + (int) (photoId ^ (photoId >>> 32));
        result = 31 * result + width;
        result = 31 * result + height;
        return result;
    }
}
