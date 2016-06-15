package cn.xianging.photoselector;

import android.content.Context;
import android.widget.ImageView;

/**
 * Created by xiaoz on 16/6/7.
 * Copyright © 2016年 xianging. All rights reserved.
 */
public interface ImageLoader {
    void loadImageToView(Context context, String imagePath, ImageView imageView, int imageSize);
}
