package com.ahao.wifichat.utlis;

import android.content.Context;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import java.io.File;

/**
 * Created by hao.ruan on 2017/11/1.
 */

public class GlideLoder<T> implements ImageLoader<T>{
    Context context;
    public GlideLoder (Context context){
        this.context=context;
        Glide.with(context);
    }
    public void loadImageByFile(ImageView imageView,String file){
        //context.getExternalCacheDir(),
        File mFile=new File(file);
        Glide.with(context).load(mFile).into(imageView);
    }
    @Override
    public void loadImage(ImageView imageView, String url) {
            Glide.with(context).load(url).into(imageView);
    }

}
