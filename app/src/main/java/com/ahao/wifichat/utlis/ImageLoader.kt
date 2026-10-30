package com.ahao.wifichat.utlis;

import android.widget.ImageView;

/**
 * Created by hao.ruan on 2017/11/1.
 */

public interface ImageLoader<T> {
    void loadImage(ImageView imageView,String url);
}
