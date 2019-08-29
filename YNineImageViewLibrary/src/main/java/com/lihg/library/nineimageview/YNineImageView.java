package com.lihg.library.nineimageview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * 九宫图控件
 */
public class YNineImageView extends RelativeLayout {

    public interface NineImageLoader{
        void displayImage(YNineImageView container, YImageView imageView, String image);
    }

    public interface OnItemClickListener{
        void onItemClick(YNineImageView container, int position);
    }

    public class YImageView extends AppCompatImageView {

        private Paint mPaint;
        private Bitmap mBitmap;

        private int position;

        private String image;

        public YImageView(Context context) {
            this(context, null);
        }

        public YImageView(Context context, AttributeSet attrs) {
            this(context, attrs, 0);
        }

        public YImageView(Context context, AttributeSet attrs, int defStyleAttr) {
            super(context, attrs, defStyleAttr);
            mPaint = new Paint();
            mBitmap = null;
        }

        public int getPosition() {
            return position;
        }

        public void setPosition(int position) {
            this.position = position;
        }

        public String getImage() {
            return image;
        }

        public void setImage(String image) {
            this.image = image;
        }

        public void setCenterImage(int imgResid) {
            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), imgResid);
            this.setCenterImage(bitmap);
        }

        public void setCenterImage(Bitmap bitmap) {
            this.setColorFilter(Color.GRAY, PorterDuff.Mode.MULTIPLY);
            if (mBitmap != null) {
                mBitmap.recycle();
                mBitmap = null;
            }
            mBitmap = bitmap;
            this.postInvalidate();
        }

        public void removeCenterImage() {
            this.setColorFilter(Color.TRANSPARENT);
            if (mBitmap != null) {
                mBitmap.recycle();
                mBitmap = null;
                this.postInvalidate();
            }
        }

        public void setSingleImage(int srcWidth, int srcHeight) {
            if (this.getScaleType() != ScaleType.FIT_XY) {
                this.setScaleType(ScaleType.FIT_XY);
            }
            int imgW = this.getWidth() - this.getPaddingLeft() - this.getPaddingRight();
            int imgH = this.getHeight() - this.getPaddingTop() - this.getPaddingBottom();
            float scale = srcWidth > srcHeight ? (imgW / (float)srcWidth) : (imgH / (float)srcHeight);
            int w = Math.round(srcWidth * scale);
            int h = Math.round(srcHeight * scale);
            ViewGroup.LayoutParams params = this.getLayoutParams();
            params.width = w + this.getPaddingLeft() + this.getPaddingRight();
            params.height = h + this.getPaddingTop() + this.getPaddingBottom();
            this.setLayoutParams(params);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            if (mBitmap != null) {
                float left = (this.getMeasuredWidth() - mBitmap.getWidth()) / 2;
                float top = (this.getMeasuredHeight() - mBitmap.getHeight()) / 2;
                canvas.drawBitmap(mBitmap, left, top, mPaint);
            }
        }

        @Override
        protected void onDetachedFromWindow() {
            super.onDetachedFromWindow();
            if (mBitmap != null) {
                mBitmap.recycle();
                mBitmap = null;
            }
        }
    }

    //最多控件数量
    protected static final int MAX_COUNT = 9;

    //自定义类型标记
    protected int mType = 0;

    //图片间的间距
    protected int mImagePadding;
    //图片宽度
    protected int mImageWidth;
    //图片高度
    protected int mImageHeight;
    //列数
    protected int mColumnCount = 3;

    // 列表
    protected List<YImageView> mImageViews;

    //图片加载器
    protected NineImageLoader mImageLoader;

    //点击事件
    private OnItemClickListener mOnItemClickListener;

    public YNineImageView(Context context) {
        this(context, null);
    }

    public YNineImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public YNineImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.setClipToPadding(false);

        //默认值
        mImagePadding = this.dp2px(3);
        mImageWidth = mImageHeight = this.dp2px(200);

        mImageViews = new ArrayList<YImageView>();

        //资源配置读取值
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.YNineImageView);
        mImagePadding = typedArray.getDimensionPixelSize(R.styleable.YNineImageView_nine_imagePadding, mImagePadding);
        typedArray.recycle();
    }

    public int getImagePadding() {
        return mImagePadding;
    }

    public void setImagePadding(int imagePadding) {
        this.mImagePadding = imagePadding;
    }

    public void setImageLoader(NineImageLoader imageLoader) {
        this.mImageLoader = imageLoader;
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener){
        mOnItemClickListener = onItemClickListener;
    }

    public void setImages(List<String> images) {
        this.setImages(images, 0);
    }

    public void setImages(List<String> images, int type) {
        this.clear();
        mType = type;
        if (images != null && images.size() > 0) {
            int n = min(images.size(), MAX_COUNT);
            for (int i = 0; i < n; i++) {
                YImageView imageView = new YImageView(getContext());
                imageView.setImage(images.get(i));
                imageView.setOnClickListener(mImageViewOnClickListener);
                addView(imageView);
                mImageViews.add(imageView);
            }
        }
        this.post(new Runnable() {
            @Override
            public void run() {
                calculateCellSize();
                layoutSubViews(true);
            }
        });
    }

    public boolean singleImage() {
        return mImageViews != null && mImageViews.size() == 1;
    }

    public int getType() {
        return mType;
    }

    public List<String> getImageUrls() {
        List<String> images = new ArrayList<String>();
        for (YImageView imageView : mImageViews) {
            images.add(imageView.getImage());
        }
        return images;
    }

    public List<YImageView> getImageViews() {
        return mImageViews;
    }

    protected void clear() {
        for (YImageView imageView : mImageViews) {
            this.removeView(imageView);
        }
        mImageViews.clear();
    }

    /**
     * 计算单元格大小
     */
    protected void calculateCellSize() {
        int width = getMeasuredWidth();
        if (width > 0) {
            int padding = getPaddingStart() + getPaddingEnd();
            mImageWidth = mImageHeight = (width - padding - mImagePadding * 2) / 3;
        }
        if (mImageViews.size() == 1) {
            mImageWidth = mImageWidth * 2 + mImagePadding;
            mImageHeight = mImageHeight * 2 + mImagePadding;
        }
        mColumnCount = mImageViews.size() == 4 ? 2 : 3;
    }

    /**
     * 布局图片控件位置
     */
    public void layoutSubViews() {
        this.layoutSubViews(false);
    }

    /**
     * 布局图片控件位置
     * @param loadImage 是否加载图片
     */
    public void layoutSubViews(boolean loadImage) {
        for (int i = 0; i < mImageViews.size(); i++) {
            LayoutParams params = new LayoutParams(mImageWidth, mImageHeight);
            params.topMargin = (i / mColumnCount) * (mImageHeight + mImagePadding);
            params.leftMargin = (i % mColumnCount) * (mImageWidth + mImagePadding);
            YImageView imageView = mImageViews.get(i);
            imageView.setLayoutParams(params);
            imageView.setPosition(i);
            if (loadImage && mImageLoader != null) {
                mImageLoader.displayImage(this, imageView, imageView.getImage());
            }
        }
    }

    protected int dp2px(float dp) {
        return (int)TypedValue.applyDimension(1, dp, getContext().getResources().getDisplayMetrics());
    }

    protected int min(int a, int b) {
        return a < b ? a : b;
    }

    protected int max(int a, int b) {
        return a > b ? a : b;
    }

    // 图片点击事件
    protected OnClickListener mImageViewOnClickListener = new OnClickListener() {
        @Override
        public void onClick(View view) {
            if (mOnItemClickListener != null) {
                YImageView imageView = (YImageView)view;
                mOnItemClickListener.onItemClick(YNineImageView.this, imageView.getPosition());
            }
        }
    };
}
