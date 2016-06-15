package cn.xianging.photoselector;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by xiaoz on 16/3/28.
 * Copyright © 2016年 xianging. All rights reserved.
 */
public class FolderAdapter extends BaseAdapter {

    private List<FolderInfo> folders = new ArrayList<>();
    private int selectedIndex = -1;
    private ImageLoader mImageLoader;

    public FolderAdapter(ImageLoader imageLoader, List<FolderInfo> folders) {
        mImageLoader = imageLoader;
        if (folders != null) {
            this.folders.clear();
            this.folders.addAll(folders);
            this.selectedIndex = 0;
        }
    }

    public FolderInfo getSelectedFolder() {
        if (selectedIndex >= 0 && selectedIndex < folders.size()) {
            return folders.get(selectedIndex);
        }
        return null;
    }

    public void setSelectedIndex(int index) {
        if (index < 0 || index >= folders.size()) {
            return;
        }
        selectedIndex = index;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return folders.size();
    }

    @Override
    public Object getItem(int position) {
        return folders.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.photopick_folder_list_item, parent, false);
            holder.folderIcon = (ImageView) convertView.findViewById(R.id.folder_icon);
            holder.folderName = (TextView) convertView.findViewById(R.id.folder_name);
            holder.imageCount = (TextView) convertView.findViewById(R.id.image_count);
            holder.folderChecked = convertView.findViewById(R.id.folder_check);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        FolderInfo info = folders.get(position);
        holder.folderName.setText(info.getFolderName());
        holder.imageCount.setText(String.format("%d张", info.getPhotoCount()));

        String path = info.getFirstShowPhoto().getPath();
        float imageSize = parent.getContext().getResources().getDimension(R.dimen.foldericon_size);
        mImageLoader.loadImageToView(parent.getContext(), path, holder.folderIcon, (int) imageSize);

        if (position == selectedIndex) {
            holder.folderChecked.setVisibility(View.VISIBLE);
        } else {
            holder.folderChecked.setVisibility(View.INVISIBLE);
        }

        return convertView;
    }

    static class ViewHolder {
        ImageView folderIcon;
        TextView folderName;
        TextView imageCount;
        View folderChecked;
    }
}
