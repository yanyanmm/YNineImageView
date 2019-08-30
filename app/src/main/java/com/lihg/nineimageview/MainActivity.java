package com.lihg.nineimageview;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.lihg.library.nineimageview.YNineAddImageView;
import com.lihg.library.nineimageview.YNineImageView;
import com.luck.picture.lib.PictureSelector;
import com.luck.picture.lib.config.PictureConfig;
import com.luck.picture.lib.config.PictureMimeType;
import com.luck.picture.lib.entity.LocalMedia;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private YNineAddImageView mAddImageView;
    private List<LocalMedia> mLocalMedias;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().setStatusBarColor(Color.TRANSPARENT);
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR | View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
        }
        setContentView(R.layout.activity_main);

        String url = "http://b-ssl.duitang.com/uploads/item/201510/21/20151021125719_2aETz.jpeg";
        final List<String> imageUrls = new ArrayList<String>();
        for (int i = 0; i < 10; i++) {
            imageUrls.add(url);
        }
        String url2 = "http://img.chemcp.com/201811/1748702018111811165785071.jpg";
        final List<String> videoUrls = new ArrayList<String>();
        for (int i = 0; i < 4; i++) {
            videoUrls.add(url2);
        }
        final List<String> imageAddUrls = new ArrayList<String>();
        imageAddUrls.add("http://b-ssl.duitang.com/uploads/item/201510/21/20151021125719_2aETz.jpeg");
        imageAddUrls.add("http://img.chemcp.com/201811/1748702018111811165785071.jpg");
        imageAddUrls.add("https://ss1.bdstatic.com/70cFuXSh_Q1YnxGkpoWK1HF6hhy/it/u=2376858034,1145412955&fm=26&gp=0.jpg");
        imageAddUrls.add("https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1566927420102&di=522b25a7b5964f351caee9124244c98c&imgtype=0&src=http%3A%2F%2Fimg007.hc360.cn%2Fg3%2FM02%2F22%2F87%2FwKhQvlITV0KEekGAAAAAAMT0OHU969.jpg");
        imageAddUrls.add("https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1567522198&di=77b2d6e34d9d643641b4f4cd2ed787b9&imgtype=jpg&er=1&src=http%3A%2F%2Fimg2.ph.126.net%2FaJr5XW21dSrFwC-D8UYvJg%3D%3D%2F1697857059618892865.jpg");
        YNineImageView nineImageView = findViewById(R.id.nineImageView);
        nineImageView.setImageLoader(mNineImageLoader);
        nineImageView.setOnItemClickListener(new YNineImageView.OnItemClickListener() {
            @Override
            public void onItemClick(YNineImageView container, int position) {
                container.setPadding(30, 0, 0, 0);
                container.setImages(imageAddUrls, 1);
            }
        });
        nineImageView.setImages(imageUrls);

        mAddImageView = findViewById(R.id.addImageView);
        mAddImageView.setImageLoader(mNineAddImageLoader);
        mAddImageView.setOnItemClickListener(new YNineImageView.OnItemClickListener() {
            @Override
            public void onItemClick(YNineImageView container, int position) {
                Log.i("YNineAddImageView", "onItemClick");
                PictureSelector.create(MainActivity.this).themeStyle(R.style.picture_default_style).openExternalPreview(position, mAddImageView.<LocalMedia>getImages());
            }
        });
        mAddImageView.getAddImageView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PictureSelector.create(MainActivity.this)
                        .openGallery(PictureMimeType.ofAll())
                        //.theme(R.style.picture_white_style)
                        .selectionMedia(mAddImageView.<LocalMedia>getImages())
                        .forResult(PictureConfig.CHOOSE_REQUEST);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case PictureConfig.CHOOSE_REQUEST:
                    // 图片、视频、音频选择结果回调
                    mLocalMedias = PictureSelector.obtainMultipleResult(data);
                    // 例如 LocalMedia 里面返回三种path
                    // 1.media.getPath(); 为原图path
                    // 2.media.getCutPath();为裁剪后path，需判断media.isCut();是否为true  注意：音视频除外
                    // 3.media.getCompressPath();为压缩后path，需判断media.isCompressed();是否为true  注意：音视频除外
                    // 如果裁剪并压缩了，以取压缩路径为准，因为是先裁剪后压缩的
                    List<String> images = new ArrayList<String>();
                    for (LocalMedia media : mLocalMedias) {
                        if (media.isCompressed()) {
                            images.add(media.getCompressPath());
                        } else {
                            images.add(media.getPath());
                        }
                    }
                    mAddImageView.setImages(mLocalMedias);
                    break;
            }
        }
    }

    private YNineImageView.NineImageLoader mNineImageLoader = new YNineImageView.NineImageLoader() {
        @Override
        public void displayImage(YNineImageView container, final YNineImageView.YImageView imageView, Object image) {
            if (container.getType() == 1) {
                imageView.setCenterImage(R.mipmap.play);
            }
            String imageUrl = (String)image;
            if (container.singleImage()) {
                Glide.with(container.getContext()).load(imageUrl).listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        return false;
                    }
                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        imageView.setSingleImage(resource.getIntrinsicWidth(), resource.getIntrinsicHeight());
                        return false;
                    }
                }).into(imageView);
            } else {
                RequestOptions options = new RequestOptions().centerCrop();
                Glide.with(container.getContext()).load(imageUrl).apply(options).into(imageView);
            }
        }
    };

    private YNineImageView.NineImageLoader mNineAddImageLoader = new YNineImageView.NineImageLoader() {
        @Override
        public void displayImage(YNineImageView container, final YNineImageView.YImageView imageView, Object image) {
            if (container.getType() == 1) {
                imageView.setCenterImage(R.mipmap.play);
            }
            LocalMedia media = (LocalMedia)image;
            String imageUrl = media.isCompressed() ? media.getCompressPath() : media.getPath();
            RequestOptions options = new RequestOptions().centerCrop();
            Glide.with(container.getContext()).load(imageUrl).apply(options).into(imageView);
        }
    };
}
