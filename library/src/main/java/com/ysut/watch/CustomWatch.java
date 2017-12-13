package com.ysut.watch;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

import java.util.Calendar;

public class CustomWatch extends View {

    private static final String[] DIG_STR = {"6", "7", "8", "9", "10", "11", "12", "1", "2", "3", "4", "5"};
    private final Rect mTextRect = new Rect();
    private final Paint mPaint;
    private float mRadius = 0, mOuterPadding = 0, mScaleLineLen = 0;//半径、外圆间隔、刻度线长度
    private int mHourPointerLen = 0, mMinutePointerLen = 0, mSecondPointerLen = 0;//时针、分针、秒针长度
    private int mHourPointerWidth = 0, mMinutePointerWidth = 0, mSecondPointerWidth = 0;//时针、分针、秒针宽度
    private float mBigDotRadius = 0, mSmallDotRadius = 0;//表盘大圆点、小圆点半径
    private int mMode = 0;
    private final int[][] mColorMode = {{Color.WHITE, Color.BLACK, Color.DKGRAY, Color.RED},
                                            {Color.BLACK, Color.WHITE, Color.LTGRAY, Color.RED}};
    private int mTextSize = 50;//字体大小

    public CustomWatch(Context context) {
        this(context, null);
    }

    public CustomWatch(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CustomWatch(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        this.mPaint = new Paint();
        this.mPaint.setAntiAlias(true);
        this.mPaint.setDither(true);

        Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        if (hour >= 18 || hour <= 6) mMode = 1;

        if (attrs != null) {
            TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.CustomWatch);
            try {
                mTextSize = ta.getInt(R.styleable.CustomWatch_textSize, mTextSize);
            } finally {
                ta.recycle();
            }
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);

        //内部重新计算表盘大小：取外部设置的最小值作为表盘的长和宽（最小值为200dp）
        int width = Math.min(widthSize, heightSize);
        if (widthMode == MeasureSpec.UNSPECIFIED || heightMode == MeasureSpec.UNSPECIFIED) {
            width = (int) Math.max(width, MeasureSpec.getSize(200));
        }

        setMeasuredDimension(width, width);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        init(w, h);
    }

    @Override
    protected void onDraw(final Canvas canvas) {
        canvas.save();
        canvas.translate(mRadius, mRadius);

        invalidateDial(canvas);

        invalidateTime(canvas);

        canvas.restore();

        postInvalidateDelayed(1000);
    }

    final void invalidate(Canvas canvas, Paint paint, float startY, float stopY, int degree) {
        canvas.save();
        canvas.rotate(degree - 180);
        canvas.drawLine(0, startY, 0, stopY, paint);
        canvas.restore();
    }

    private void invalidateTime(Canvas canvas) {
        Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        if (hour >= 18 || hour <= 6) mMode = 1;
        else if (mMode == 1) mMode = 0;

        mPaint.setAntiAlias(true);
        mPaint.setColor(mColorMode[mMode][3]);
        mPaint.setStrokeWidth(mSecondPointerWidth);
        invalidate(canvas, mPaint, 0, mSecondPointerLen, c.get(Calendar.SECOND) * 6);

        mPaint.setColor(mColorMode[mMode][1]);
        mPaint.setStrokeWidth(mMinutePointerWidth);
        invalidate(canvas, mPaint, mSmallDotRadius, mMinutePointerLen, c.get(Calendar.MINUTE) * 6);

        mPaint.setStrokeWidth(mHourPointerWidth);
        invalidate(canvas, mPaint, mSmallDotRadius, mHourPointerLen, c.get(Calendar.HOUR) * 30);
    }

    private void init(final int width, final int height) {
        this.mRadius = width / 2f - 2;
        this.mOuterPadding = mRadius / 20;
        this.mScaleLineLen = mOuterPadding * 2;
        this.mHourPointerLen = (int) (mRadius * 0.6);
        this.mMinutePointerLen = (int) (mRadius * 0.70);
        this.mSecondPointerLen = (int) (mRadius * 0.75);

        this.mBigDotRadius = mOuterPadding;
        this.mSmallDotRadius = mBigDotRadius / 2;

        this.mHourPointerWidth = (int) (mOuterPadding / 2.3);
        this.mMinutePointerWidth = (int) (mHourPointerWidth * 0.8f);
        this.mSecondPointerWidth = (int) (mHourPointerWidth * 0.6f);
    }

    private void invalidateDial(Canvas canvas) {
        paintCircle(canvas);
        paintScaleAndText(canvas);
        paintDot(canvas);
    }

    private void paintDot(Canvas canvas) {
        mPaint.setColor(mColorMode[mMode][1]);
        mPaint.setStyle(Paint.Style.FILL);
        canvas.drawCircle(0, 0, mBigDotRadius, mPaint);
        mPaint.setColor(mColorMode[mMode][3]);
        canvas.drawCircle(0, 0, mSmallDotRadius, mPaint);
    }

    private void paintCircle(Canvas canvas) {
        mPaint.setColor(mColorMode[mMode][0]);
        mPaint.setStyle(Paint.Style.FILL);
        canvas.drawCircle(0, 0, mRadius, mPaint);
    }

    private void paintScaleAndText(Canvas canvas) {
        float lineWidth = mScaleLineLen;
        float addLine = lineWidth * 5 / 4;
        canvas.save();
        for (int i = 0; i < 60; i++) {
            if (i % 5 == 0) {
                mPaint.setColor(mColorMode[mMode][1]);
                mPaint.setStrokeWidth(3);
                lineWidth = addLine;

                paintText(canvas, i, lineWidth);
            } else {
                mPaint.setColor(mColorMode[mMode][2]);
                mPaint.setStrokeWidth(2);
                lineWidth = mScaleLineLen;
            }
            canvas.drawLine(0, -mRadius + mOuterPadding, 0, -mRadius + mOuterPadding + lineWidth, mPaint);
            canvas.rotate(6);
        }
        canvas.restore();
    }

    private void paintText(Canvas canvas, int i, float lineWidth) {
        canvas.save();
        final String num = DIG_STR[i/5];
        mPaint.setTextSize(mTextSize);
        mPaint.getTextBounds(num, 0, num.length(), mTextRect);
        canvas.translate(0, mRadius - mOuterPadding - lineWidth - mTextRect.height());
        canvas.rotate(-6 * i);
        canvas.drawText(num, -(mTextRect.right + mTextRect.left) / 2,  -(mTextRect.bottom + mTextRect.top) / 2, mPaint);
        canvas.restore();
    }
}