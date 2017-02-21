package com.example.leesangwook.imageupload;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

/**
 * Created by LEE SANG WOOK on 2017-02-22.
 */

public class ImageActivity extends AppCompatActivity {

    DisplayImageOptions displayImageOption = new DisplayImageOptions.Builder()
            .delayBeforeLoading(0)
            .cacheOnDisc(true)
            .build();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);

        ImageLoader.getInstance().init(ImageLoaderConfiguration.createDefault(getApplicationContext()));
        ImageLoader.getInstance().displayImage(getIntent().getStringExtra("url"), (ImageView) findViewById(R.id.load_image), displayImageOption); // 상품이미지
    }
}
