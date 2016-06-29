package com.hanshan.circleprogressbar;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import java.util.ArrayList;

public class CircleProgressBar extends View {

    private Paint mPaint;

    /**
     * 圆环的颜色
     */
    private int mRoundColor;

    /**
     * 圆环进度的颜色
     */
    private int mRoundProgressColor;

    /**
     * 中间百分比进度的文字颜色
     */
    private int mTextColor;

    /**
     * 中间百分比进度文字大小
     */
    private float mTextSize;

    /**
     * 圆环的宽度
     */
    private float mRoundWidth;

    /**
     * 最大进度
     */
    private int mMaxProgress;

    /**
     * 当前进度
     */
    private int mProgress;
    /**
     * 是否显示中间的进度文字
     */
    private boolean mDisplayPercent;

    /**
     * 进度的风格，实心或者空心
     */
    private int mStyle;

    private long mDelayTime = 30;
    private boolean mIsRunning = true;
    private boolean mResetProgress = false;

    private RectF mRectF;

    public static final int STROKE = 0;
    public static final int FILL = 1;

    private ArrayList<Integer> mProgressList = new ArrayList<>();

    private OnProgressUpadtedListener mListener;

    public CircleProgressBar(Context context) {
        this(context, null);
    }

    public CircleProgressBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CircleProgressBar(final Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        mPaint = new Paint();
        mRectF = new RectF();
        mProgress = 40;

        TypedArray mTypedArray = context.obtainStyledAttributes(attrs,
                R.styleable.CircleProgressBar);

        //获取自定义属性和默认值
        mRoundColor = mTypedArray.getColor(R.styleable.CircleProgressBar_RoundColor, Color.RED);
        mRoundProgressColor = mTypedArray.getColor(R.styleable.CircleProgressBar_RoundProgressColor, Color.GREEN);
        mTextColor = mTypedArray.getColor(R.styleable.CircleProgressBar_TextColor, Color.GREEN);
        mTextSize = mTypedArray.getDimension(R.styleable.CircleProgressBar_TextSize, 30);
        mRoundWidth = mTypedArray.getDimension(R.styleable.CircleProgressBar_RoundWidth, 10);
        mMaxProgress = mTypedArray.getInteger(R.styleable.CircleProgressBar_Max, 100);
        mDisplayPercent = mTypedArray.getBoolean(R.styleable.CircleProgressBar_DisplayPercent, true);
        mStyle = mTypedArray.getInt(R.styleable.CircleProgressBar_Style, 0);

        mTypedArray.recycle();

        new Thread(new Runnable() {
            @Override
            public void run() {
                while (mIsRunning) {
                    try {
                        Thread.sleep(mDelayTime);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    if (mResetProgress) {
                        mResetProgress = false;
                        setProgress(0);
                        continue;
                    }

                    if (mProgressList.size() > 0) {
                        Integer integer = mProgressList.remove(0);
                        setProgress(integer);
                    }
                }
            }
        }).start();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mIsRunning = false;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        /**
         * 画最外层的大圆环
         */
        int center = getWidth() / 2;
        float halfRoundWidth = mRoundWidth / 2;
        int radius = (int) (center - halfRoundWidth);
        mPaint.setColor(mRoundColor);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(mRoundWidth);
        mPaint.setAntiAlias(true);
        canvas.drawCircle(center, center, radius, mPaint);

        /**
         * 画圆弧 ，画圆环的进度
         */
        mPaint.setStrokeWidth(mRoundWidth);
        mPaint.setColor(mRoundProgressColor);
        int rectValueOne = center - radius;
        int rectValueTwo = center + radius;

        switch (mStyle) {
            case STROKE: {
                mRectF.set(rectValueOne, rectValueOne, rectValueTwo, rectValueTwo);
                mPaint.setStyle(Paint.Style.STROKE);
                canvas.drawArc(mRectF, 0, 360 * mProgress / mMaxProgress, false, mPaint);  //根据进度画圆弧
                break;
            }
            case FILL: {
                mRectF.set(rectValueOne - halfRoundWidth, rectValueOne - halfRoundWidth
                        , rectValueTwo + halfRoundWidth, rectValueTwo + halfRoundWidth);
                mPaint.setStyle(Paint.Style.FILL);
                if (mProgress != 0)
                    canvas.drawArc(mRectF, 0, 360 * mProgress / mMaxProgress, true, mPaint);  //根据进度画圆弧
                break;
            }
        }

        /**
         * 画进度百分比
         */
        if (mDisplayPercent && mStyle == STROKE) {
            mPaint.setStrokeWidth(0);
            mPaint.setColor(mTextColor);
            mPaint.setTextSize(mTextSize);
            mPaint.setTypeface(Typeface.DEFAULT);
            int percent = (int) (((float) mProgress / (float) mMaxProgress) * 100);
            float textWidth = mPaint.measureText(percent + "%");

            if (percent != 0) {
                canvas.drawText(percent + "%", center - textWidth / 2, center + mTextSize / 2, mPaint);
            }
        }
    }


    public synchronized int getMax() {
        return mMaxProgress;
    }

    /**
     * 设置进度的最大值
     *
     * @param mMaxProgress
     */
    public synchronized void setMax(int mMaxProgress) {
        if (mMaxProgress < 0) {
            throw new IllegalArgumentException("mMaxProgress not less than 0");
        }
        this.mMaxProgress = mMaxProgress;
    }

    /**
     * 获取进度
     *
     * @return
     */
    public synchronized int getProgress() {
        return mProgress;
    }

    /**
     * 设置进度
     *
     * @param progress
     */
    public synchronized void setProgress(int progress) {
        if (progress < 0) {
            throw new IllegalArgumentException("mProgress not less than 0");
        }
        if (progress > mMaxProgress) {
            mProgress = mMaxProgress;
        }
        if (progress <= mMaxProgress) {
            mProgress = progress;
            postInvalidate();
        }

        if (mListener != null) {
            mListener.onUpdate(mProgress, mMaxProgress);
        }
    }

    /**
     * 延迟设置进度, 以保证所有进度能有机会绘制出来
     * 如果设置进度的速度超过UI刷新速度, 那么进度不一定会显示出来
     *
     * @param progress
     */
    public synchronized void setProgressDelayed(int progress, int delayTime) {
        if (progress < 0) {
            throw new IllegalArgumentException("mProgress not less than 0");
        }

        if (mDelayTime != delayTime) {
            mDelayTime = delayTime;
        }

        if (progress <= mMaxProgress) {
            mProgressList.add(progress);
        }
    }

    public synchronized void setProgressDelayed(int progress) {
        setProgressDelayed(progress, 30);
    }

    public int getCricleColor() {
        return mRoundColor;
    }

    public void setCricleColor(int cricleColor) {
        this.mRoundColor = cricleColor;
    }

    public int getCricleProgressColor() {
        return mRoundProgressColor;
    }

    public void setCricleProgressColor(int cricleProgressColor) {
        this.mRoundProgressColor = cricleProgressColor;
    }

    public int getTextColor() {
        return mTextColor;
    }

    public void setTextColor(int mTextColor) {
        this.mTextColor = mTextColor;
    }

    public float getTextSize() {
        return mTextSize;
    }

    public void setTextSize(float mTextSize) {
        this.mTextSize = mTextSize;
    }

    public float getRoundWidth() {
        return mRoundWidth;
    }

    public void setRoundWidth(float mRoundWidth) {
        this.mRoundWidth = mRoundWidth;
    }

    public void setProgressUpdatedListener(OnProgressUpadtedListener listener) {
        this.mListener = listener;
    }

    public void resetProgress() {
        mResetProgress = true;
        mProgress = 0;
        mProgressList.clear();
        postInvalidate();
    }

    public OnProgressUpadtedListener getProgressUpdatedListener() {
        return mListener;
    }

    public interface OnProgressUpadtedListener {
        void onUpdate(int progress, int mMaxProgress);
    }
}
