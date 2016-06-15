package cn.xianging.photoselector;

/**
 * Created by xiaoz on 16/3/28.
 * Copyright © 2016年 xianging. All rights reserved.
 */
public class FolderInfo {
    static final String FOLDER_NAME_ALL = "所有图片";

    private int photoCount;
    private String folderName;
    private PhotoInfo firstShowPhoto;

    public FolderInfo(int photoCount, String folderName, PhotoInfo firstShowPhoto) {
        this.photoCount = photoCount;
        this.folderName = folderName;
        this.firstShowPhoto = firstShowPhoto;
    }

    /** setter getter */

    public int getPhotoCount() {
        return photoCount;
    }

    public void setPhotoCount(int photoCount) {
        this.photoCount = photoCount;
    }

    public String getFolderName() {
        return folderName;
    }

    public void setFolderName(String folderName) {
        this.folderName = folderName;
    }

    public PhotoInfo getFirstShowPhoto() {
        return firstShowPhoto;
    }

    public void setFirstShowPhoto(PhotoInfo firstShowPhoto) {
        this.firstShowPhoto = firstShowPhoto;
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

        FolderInfo that = (FolderInfo) o;

        if (photoCount != that.photoCount) {
            return false;
        }
        return firstShowPhoto.equals(that.firstShowPhoto);

    }

    @Override
    public int hashCode() {
        int result = firstShowPhoto.hashCode();
        result = 31 * result + photoCount;
        return result;
    }
}
