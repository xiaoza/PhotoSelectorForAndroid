package cn.xianging.photoselector;

import android.database.Cursor;
import android.os.Build;
import android.provider.MediaStore;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by xiaoz on 16/6/7.
 * Copyright © 2016年 unfae. All rights reserved.
 */
public class PhotoSelectorFragmentDelegate {
    private PhotoSelectorFragment mFragment;

    private String[] projection = {
            MediaStore.Images.ImageColumns._ID,
            MediaStore.Images.ImageColumns.DATA,
            MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME,
            MediaStore.Images.ImageColumns.WIDTH,
            MediaStore.Images.ImageColumns.HEIGHT
    };

    private String[] projection_under16 = {
            MediaStore.Images.ImageColumns._ID,
            MediaStore.Images.ImageColumns.DATA,
            MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME,
    };

    public PhotoSelectorFragmentDelegate(PhotoSelectorFragment fragment) {
        mFragment = fragment;
    }

    List<FolderInfo> getFolderList() {
        Cursor cursor = mFragment.getActivity().getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                projection_under16,
                "",
                null,
                MediaStore.MediaColumns.DATE_ADDED + " DESC"
        );
        if (cursor == null) {
            return Collections.emptyList();
        }

        Map<String, Integer> bucketNameCountMap = new HashMap<>();
        Map<String, PhotoInfo> bucketNameImageMap = new HashMap<>();
        while (cursor.moveToNext()) {
            String bucketName = cursor.getString(2);
            if (!bucketNameCountMap.containsKey(bucketName)) {
                bucketNameCountMap.put(bucketName, 1);
                PhotoInfo image = new PhotoInfo(cursor.getString(1));
                bucketNameImageMap.put(bucketName, image);
            } else {
                int newCount = bucketNameCountMap.get(bucketName) + 1;
                bucketNameCountMap.put(bucketName, newCount);
            }
        }

        List<FolderInfo> folderInfoList = new ArrayList<>();
        if (cursor.moveToFirst()) {
            PhotoInfo photoInfo = new PhotoInfo(cursor.getString(1));
            int allImageCount = cursor.getCount();
            FolderInfo folderInfo = new FolderInfo(allImageCount, "所有图片", photoInfo);
            folderInfoList.add(folderInfo);
        }
        for (String bucket : bucketNameCountMap.keySet()) {
            PhotoInfo image = bucketNameImageMap.get(bucket);
            int count = bucketNameCountMap.get(bucket);
            FolderInfo folderInfo = new FolderInfo(count, bucket, image);
            folderInfoList.add(folderInfo);
        }
        cursor.close();
        return folderInfoList;
    }

    Loader<Cursor> createLoader() {
        String where;
        if (mFragment.isShowAllPhotos()) {
            where = "";
        } else {
            FolderInfo folder = mFragment.selectedFolder();
            where = String.format("%s='%s'", MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME, folder.getFolderName());
        }
        return new CursorLoader(
                mFragment.getContext(),
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                Build.VERSION.SDK_INT >= 16 ? projection : projection_under16,
                where,
                null,
                MediaStore.MediaColumns.DATE_ADDED + " DESC"
        );
    }


}
