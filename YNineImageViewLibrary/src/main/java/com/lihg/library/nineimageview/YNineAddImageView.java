package com.lihg.library.nineimageview;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

import java.util.List;

public class YNineAddImageView extends YNineImageView {

    public interface OnRemoveClickListener{
        void onRemove(String image);
    }

    private int mMaxTop, mMaxLeft;

    private boolean isMoving = false;
    private int mLastX, mLastY;
    private int mCurrentPosition = 0;

    //添加按钮
    private ImageView mAddImageView;
    //移除按钮
    private ImageView mRemoveImageView;

    //点击事件
    private OnRemoveClickListener mOnRemoveClickListener;

    public YNineAddImageView(Context context) {
        this(context, null);
    }

    public YNineAddImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public YNineAddImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        this.setPadding(mImagePadding, mImagePadding, mImagePadding, mImagePadding);

        //设置添加按钮
        this.setupAddView();

        //设置移除按钮
        this.setupRemoveView();

        //设置空
        this.setImages(null);
    }

    private void setupAddView() {

        mAddImageView = new ImageView(getContext());
        mAddImageView.setImageResource(R.mipmap.btn_add);
        mAddImageView.setBackgroundResource(R.drawable.btn_add_bg);
        mAddImageView.setScaleType(ImageView.ScaleType.CENTER);
        LayoutParams params = new LayoutParams(-2, -2);
        mAddImageView.setLayoutParams(params);
        this.addView(mAddImageView);
    }

    private void setupRemoveView() {

        mRemoveImageView = new ImageView(getContext());
        mRemoveImageView.setImageResource(R.mipmap.btn_delete);
        int w = dp2px(24);
        LayoutParams params = new LayoutParams(w, w);
        mRemoveImageView.setLayoutParams(params);
        mRemoveImageView.setVisibility(View.GONE);
        this.addView(mRemoveImageView);

        mRemoveImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = (int)v.getTag();
                if (position >= 0 && position < mImageViews.size()) {
                    YImageView imageView = mImageViews.get(position);
                    String image = imageView.getImage();
                    removeView(imageView);
                    mImageViews.remove(imageView);
                    calculateCellSize();
                    layoutSubViews();
                    if (mOnRemoveClickListener != null) {
                        mOnRemoveClickListener.onRemove(image);
                    }
                }
                mRemoveImageView.setVisibility(View.GONE);
            }
        });
    }

    public void setOnRemoveClickListener(OnRemoveClickListener listener) {
        this.mOnRemoveClickListener = listener;
    }

    @Override
    public void setImages(List<String> images, int type) {
        super.setImages(images, type);

        //设置触摸事件和长按事件
        for (YImageView imageView : mImageViews) {
            imageView.setOnTouchListener(mImageTouchListener);
            imageView.setOnLongClickListener(mImageLongClickListener);
        }
    }

    @Override
    protected void calculateCellSize() {
        int width = getMeasuredWidth();
        if (width > 0) {
            int padding = getPaddingStart() + getPaddingEnd();
            mImageWidth = mImageHeight = (width - padding - mImagePadding * 2) / 3;
        }
        int count = mImageViews.size();
        mMaxLeft = ((count > mColumnCount ? mColumnCount : count) - 1) * (mImageWidth + mImagePadding);;
        mMaxTop = ((count - 1) / mColumnCount) * (mImageHeight + mImagePadding);
    }

    @Override
    public void layoutSubViews(boolean loadImage) {
        super.layoutSubViews(loadImage);

        int count = mImageViews.size();
        if (count < MAX_COUNT) {
            mAddImageView.setVisibility(View.VISIBLE);
            LayoutParams params = new LayoutParams(mImageWidth, mImageHeight);
            params.topMargin = (count / mColumnCount) * (mImageHeight + mImagePadding);
            params.leftMargin = (count % mColumnCount) * (mImageWidth + mImagePadding);
            mAddImageView.setLayoutParams(params);
        } else {
            mAddImageView.setVisibility(View.GONE);
        }
    }

    public ImageView getAddImageView() {
        return mAddImageView;
    }

    public ImageView getRemoveImageView() {
        return mRemoveImageView;
    }

    private int getPosition(int left, int top) {
        int centerX = left + mImageWidth / 2;
        int centerY = top + mImageHeight / 2;
        int row = centerY / (mImageHeight + mImagePadding);
        int col = centerX /  (mImageWidth + mImagePadding);
        return row * mColumnCount + col;
    }

    //触摸事件
    private View.OnTouchListener mImageTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            YImageView imageView = (YImageView)v;
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    isMoving = false;
                    mCurrentPosition = imageView.getPosition();
                    mRemoveImageView.setVisibility(View.GONE);
                    mLastX = (int) event.getRawX();
                    mLastY = (int) event.getRawY();
                    return false;
                case MotionEvent.ACTION_MOVE:
                    isMoving = true;
                    if (mRemoveImageView.getVisibility() == View.GONE) {
                        imageView.bringToFront();
                        LayoutParams param = (LayoutParams) imageView.getLayoutParams();
                        int dx = (int) event.getRawX() - mLastX;
                        int dy = (int) event.getRawY() - mLastY;
                        int left = max(min(param.leftMargin + dx, mMaxLeft),0);
                        int top = max(min(param.topMargin + dy, mMaxTop),0);
                        int position = getPosition(left, top);
                        if (position >= 0 && position != mCurrentPosition) {
                            mCurrentPosition = position;
                            mImageViews.remove(imageView);
                            if (position < mImageViews.size()) {
                                mImageViews.add(mCurrentPosition, imageView);
                            } else {
                                mImageViews.add(imageView);
                            }
                            layoutSubViews();
                        }
                        param.leftMargin = left;
                        param.topMargin = top;
                        imageView.setLayoutParams(param);
                        mLastX = (int) event.getRawX();
                        mLastY = (int) event.getRawY();
                    }
                    return true;
                case MotionEvent.ACTION_UP:
                    if (isMoving) {
                        layoutSubViews();
                    }
                    return isMoving;
            }
            return true;
        }
    };

    //长按事件
    private View.OnLongClickListener mImageLongClickListener = new View.OnLongClickListener() {
        @Override
        public boolean onLongClick(View v) {
            if (!isMoving) {
                YImageView imageView = (YImageView)v;
                mRemoveImageView.setVisibility(View.VISIBLE);
                LayoutParams imgParams = (LayoutParams)imageView.getLayoutParams();
                LayoutParams params = (LayoutParams)mRemoveImageView.getLayoutParams();
                params.topMargin = imgParams.topMargin;
                params.leftMargin = imgParams.leftMargin + imgParams.width - params.width;
                mRemoveImageView.setLayoutParams(params);
                mRemoveImageView.setTag(imageView.getPosition());
                mRemoveImageView.bringToFront();
            }
            return true;
        }
    };
}
