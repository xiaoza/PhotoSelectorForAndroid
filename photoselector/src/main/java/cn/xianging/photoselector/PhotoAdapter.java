package cn.xianging.photoselector;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CursorAdapter;
import android.widget.ImageView;

/**
 * Created by xiaoz on 16/3/28.
 * Copyright © 2016年 unfae. All rights reserved.
 */
public class PhotoAdapter extends CursorAdapter implements View.OnClickListener {

    private int itemSize;
    private LayoutInflater mInflater;
    private PhotoSelectorFragment mPhotoSelectorFragment;
    private OnPhotoItemSelectedListener mItemPickedListener;
    private ImageLoader mImageLoader;

    public void setPhotoItemPickedListener(OnPhotoItemSelectedListener itemPickedListener) {
        mItemPickedListener = itemPickedListener;
    }

    public PhotoAdapter(Context context, Cursor c, boolean autoRequery, PhotoSelectorFragment fragment) {
        super(context, c, autoRequery);

        mPhotoSelectorFragment = fragment;
        mImageLoader = fragment.getImageLoader();

        float windowWidth = context.getResources().getDisplayMetrics().widthPixels;
        int spaceInPix = context.getResources().getDimensionPixelSize(R.dimen.photo_space);
        itemSize = (int) ((windowWidth - 4 * spaceInPix) / 3);
        mInflater = LayoutInflater.from(context);
    }

    public LayoutInflater getInflater() {
        return mInflater;
    }

    public int getItemSize() {
        return itemSize;
    }

    public PhotoSelectorFragment getPhotoSelectorFragment() {
        return mPhotoSelectorFragment;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View convertView = mInflater.inflate(R.layout.photopick_gridlist_item, parent, false);
        ViewGroup.LayoutParams params = convertView.getLayoutParams();
        params.height = itemSize;
        params.width = itemSize;
        convertView.setLayoutParams(params);

        ViewHolder holder = new ViewHolder();
        holder.icon = (ImageView) convertView.findViewById(R.id.icon);
        holder.iconFore = (ImageView) convertView.findViewById(R.id.icon_fore);
        holder.checked = (CheckBox) convertView.findViewById(R.id.checked);
        PhotoCheckTag tag = new PhotoCheckTag(holder.iconFore);
        holder.checked.setTag(tag);
        holder.checked.setOnClickListener(this);
        convertView.setTag(holder);

        ViewGroup.LayoutParams iconParams = holder.icon.getLayoutParams();
        iconParams.width = itemSize;
        iconParams.height = itemSize;
        holder.icon.setLayoutParams(iconParams);

        return convertView;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ViewHolder holder = (ViewHolder) view.getTag();
        String path = cursor.getString(1);
        mImageLoader.loadImageToView(context, path, holder.icon, itemSize);

        ((PhotoCheckTag) holder.checked.getTag()).path = path;
        boolean picked = mPhotoSelectorFragment.isPhotoSelected(path);
        holder.checked.setChecked(picked);
        holder.iconFore.setVisibility(picked ? View.VISIBLE : View.INVISIBLE);
    }

    @Override
    public void onClick(View v) {
        if (mItemPickedListener == null) {
            return;
        }

        PhotoCheckTag tag = (PhotoCheckTag) v.getTag();
        CheckBox checkBox = (CheckBox) v;
        if (checkBox.isChecked()) {
            if (!mItemPickedListener.shouldSelectThisPhoto(tag.path)) {
                checkBox.setChecked(false);
                return;
            }
            tag.iconFore.setVisibility(View.VISIBLE);
            mItemPickedListener.onPhotoItemSelected(tag.path, true);
        } else {
            tag.iconFore.setVisibility(View.INVISIBLE);
            mItemPickedListener.onPhotoItemSelected(tag.path, false);
        }
        notifyDataSetChanged();
    }

    static class ViewHolder {
        ImageView icon;
        ImageView iconFore;
        CheckBox checked;
    }

    static class PhotoCheckTag {
        View iconFore;
        String path = "";

        public PhotoCheckTag(View iconFore) {
            this.iconFore = iconFore;
        }
    }

    interface OnPhotoItemSelectedListener {
        boolean shouldSelectThisPhoto(String photoPath);
        void onPhotoItemSelected(String photoPath, boolean isSelected);
    }
}
