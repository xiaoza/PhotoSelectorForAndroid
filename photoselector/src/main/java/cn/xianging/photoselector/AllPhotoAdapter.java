package cn.xianging.photoselector;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by xiaoz on 16/3/29.
 * Copyright © 2016年 unfae. All rights reserved.
 */
public class AllPhotoAdapter extends PhotoAdapter {

    public AllPhotoAdapter(Context context, Cursor c, boolean autoRequery, PhotoSelectorFragment fragment) {
        super(context, c, autoRequery, fragment);
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return 0;
        } else {
            return 1;
        }
    }

    @Override
    public int getCount() {
        return super.getCount() + 1;
    }

    @Override
    public Object getItem(int position) {
        if (position > 0) {
            return super.getItem(position - 1);
        }
        return null;
    }

    @Override
    public long getItemId(int position) {
        if (position > 0) {
            return super.getItemId(position - 1);
        }
        return -1;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (position > 0) {
            return super.getView(position - 1, convertView, parent);
        } else {
            if (convertView == null) {
                convertView = getInflater().inflate(R.layout.photopick_gridlist_item_camera2, parent, false);
                convertView.getLayoutParams().height = getItemSize();
                convertView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        getPhotoSelectorFragment().requestCamera();
                    }
                });
            }
            return convertView;
        }
    }
}
