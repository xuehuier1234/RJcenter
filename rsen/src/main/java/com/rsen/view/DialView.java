package com.rsen.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.support.annotation.ColorInt;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

/**
 * 大转盘
 * Created by angcyo on 15-12-18 018 15:09 下午.
 */
public class DialView extends View {
    float degreeStep;//每次重绘,添加的角度
    long invalidateTime = 20;//每隔多长时间,重绘一次
    int dialNum = 3;//至少需要转多少圈;
    float startAngle, endAngle, targetAngle;//开始结束,目标的角度
    private float curDegreeStep = 20;//每次重绘,添加的角度
    @ColorInt
    private int[] mColors = new int[]{Color.RED, Color.BLUE, Color.CYAN, Color.DKGRAY, Color.GREEN, Color.YELLOW};//转盘每个块区域对应的颜色
    private String[] mTexts = new String[]{"一等奖", "二等奖", "三等奖", "四等奖", "五等奖", "六等奖"};//转盘每个块区域对应的文本
    private Bitmap[] mIcons;//转盘每个块区域对应的图标
    private float mTextSize;
    private float[] mRatios = new float[]{1, 2, 3, 2, 1, 3};//转盘每个块区域对应的大小比例
    private TextPaint mTextPaint;//文本画笔
    //转换之后的角度
    private float[] mAngles;
    //可绘制区域
    private Rect mDialRect;
    //文本绘制偏移比例
    private float mTextOffset = 0.5f;
    private float mDialCurrentDegree = 0f;// 转盘当前的角度,
    private float mDialOffsetDegree = 30f;// 转盘偏移的角度,用于决定开始时角度
    private boolean mDialStart = false;//是否开始了
    private boolean mDialEnd = true;//是否结束了

    public DialView(Context context) {
        this(context, null);
    }

    public DialView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DialView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        init();
    }

    /**
     * 测量文本范围
     */
    public static void getTextBounds(Paint paint, String text, Rect bounds) {
        Rect textRound = new Rect();
        paint.getTextBounds(text, 0, text.length(), textRound);
        float width = paint.measureText(text);
        bounds.set(textRound.left, textRound.top, (int) (textRound.left + width), textRound.bottom);
    }

    /**
     * @see com.rsen.view.DialView#getTextBounds(Paint, String, Rect)
     */
    public static Rect getTextBounds(Paint paint, String text) {
        Rect textRound = new Rect();
        getTextBounds(paint, text, textRound);
        return textRound;
    }

    private void init() {
        setTextSize(22f);
        mTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
        mTextPaint.setTextAlign(Paint.Align.CENTER);
        mTextPaint.setTextSize(mTextSize);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int size = Math.min(widthSize, heightSize);

        //固定大小
        if (widthMode == MeasureSpec.AT_MOST || heightMode == MeasureSpec.AT_MOST) {
            setMeasuredDimension(size, size);
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }

    public void start(int index) {//从0开始的索引
        if (index < 0 || index >= mRatios.length) {
            throw new IllegalArgumentException("index is invalid");
        }

        if (mDialStart) {
            return;
        }

        initTargetDegree(index);
        mDialStart = true;
        invalidate();
    }

    public boolean isStart() {
        return mDialStart;
    }

    public boolean isEnd() {
        return mDialEnd;
    }

    /**
     * 控制速度,控制结束位置
     */
    private void smoothSlow() {
        float ratio = mDialCurrentDegree / targetAngle;//已经旋转了多少角度的比例
        if (ratio < 0.5) {
            degreeStep = curDegreeStep;
        } else {
            degreeStep -= 1;
        }

        if (ratio < 0.5) {
            degreeStep = Math.max(degreeStep, curDegreeStep);
        } else if (ratio < 0.8) {
            degreeStep = Math.max(degreeStep, 5);
        } else if (ratio < 0.97) {
            degreeStep = Math.max(degreeStep, 3);
        } else if (ratio < 0.99) {
            degreeStep = Math.max(degreeStep, 2);
        } else if (ratio < 2) {
            degreeStep = Math.max(degreeStep, 1);
        }

        if ((mDialCurrentDegree + degreeStep) > targetAngle) {
            degreeStep = 1;
        }

        if (mDialCurrentDegree >= targetAngle) {
            mDialStart = false;
            mDialEnd = true;
            mDialCurrentDegree %= 360;
        }
    }

    /***
     * 获取目标需要旋转的角度
     */
    private void initTargetDegree(int index) {

        float offsetAngle = 360 * dialNum + 270;//+270 是让正上方为 指针位置;
        mDialCurrentDegree = 0;

        float startAngle = 0, curAngle = 0;
        for (int i = 0; i < mAngles.length; i++) {
            curAngle = mAngles[i];
            startAngle += curAngle;

            if (index == i) {
                break;
            }
        }

        this.startAngle = offsetAngle + 360 - startAngle;
        this.endAngle = this.startAngle + curAngle;
        this.targetAngle = (float) (this.startAngle + ((1 - Math.random()) * 0.8 * curAngle) - mDialCurrentDegree - mDialOffsetDegree);

//        Log.e("tag", "startAngle:" + this.startAngle + "  endAngle:" + this.endAngle + "  targetAngle:" + this.targetAngle);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
//        testDraw(canvas);

//        mTextPaint.setColor(mColors[0]);
//        Rect mDialRect = new Rect();
//        getDialRect(mDialRect);
//        canvas.drawRect(mDialRect, mTextPaint);
        //测量一些值
        mAngles = rationsToAngle();
        mDialRect = getDialRect();

        //开始时有一个角度
        tranToCenter(canvas, mDialRect);
        mTextPaint.setColor(Color.WHITE);
        mTextPaint.setStrokeWidth(5f);
        canvas.drawLine(0, -getMeasuredHeight(), 0, 0, mTextPaint);

        canvas.rotate(mDialOffsetDegree + mDialCurrentDegree);

        drawDialArea(canvas);
        drawDialText(canvas);
        drawDialIco(canvas);

        if (mDialStart) {
            smoothSlow();
            mDialCurrentDegree += degreeStep;//闪的很快
            postInvalidateDelayed(invalidateTime);//转的很快
        }
    }

    private void drawDialIco(Canvas canvas) {
        //绘制图标

        if (mIcons == null || mIcons.length < 1) {
            return;
        }

        canvas.save();
        float angle = 0;//需要旋转的角度
        for (int i = 0; i < mIcons.length; i++) {
            Bitmap bitmap = mIcons[i];
            angle = mAngles[i] / 2;
            canvas.rotate(angle);
            canvas.drawBitmap(bitmap, getMeasuredWidth() / 6, -bitmap.getHeight() / 2, mTextPaint);
            canvas.rotate(angle);
        }

        canvas.restore();
    }

    private void drawDialText(Canvas canvas) {
        //绘制色块上对应的文本
        canvas.save();
        mTextPaint.setColor(Color.WHITE);
//        mTextPaint.setTextAlign(Paint.Align.CENTER);//居中绘制文本

        int radius = mDialRect.width() / 2;//半径
        float textCenterX = radius / 2 + radius / 2 * mTextOffset;//文本偏移之后的横向中心坐标

        float angle = 0;//需要旋转的角度
        for (int i = 0; i < mTexts.length; i++) {
            angle = mAngles[i] / 2;
            canvas.rotate(angle);
            String text = mTexts[i];
            Rect textBound = getTextBounds(mTextPaint, text);
            canvas.drawText(text, textCenterX, textBound.height() / 2, mTextPaint);
            canvas.rotate(angle);
        }
        canvas.restore();
    }

    private void drawDialArea(Canvas canvas) {
        //绘制色块区域
        canvas.save();
//        tranToCenter(canvas, mDialRect);

        RectF rectF = new RectF(-mDialRect.width() / 2, -mDialRect.height() / 2, mDialRect.width() / 2, mDialRect.height() / 2);//扇形绘制区域
        float startAngle = 0;
        for (int i = 0; i < mAngles.length; i++) {
            float endAngle = mAngles[i];
            mTextPaint.setColor(mColors[i]);
            canvas.drawArc(rectF, startAngle, endAngle, true, mTextPaint);
            startAngle += endAngle;
        }

        canvas.restore();
    }

    private Rect tranToCenter(Canvas canvas) {
        Rect dialRect = getDialRect();
        tranToCenter(canvas, dialRect);
        return dialRect;
    }

    private void tranToCenter(Canvas canvas, Rect dialRect) {
        //讲绘图坐标移至view的中心点
        canvas.translate(dialRect.centerX(), dialRect.centerY());
    }

    private float[] rationsToAngle() {
        //讲色块比例,转换成 角度
        float sum = 0;
        float avg = 0;
        for (float ratio : mRatios) {
            sum += ratio;
        }
        avg = Math.round(360f / sum);

        int len = mRatios.length;
        float[] angles = new float[len];
        for (int i = 0; i < len; i++) {
            angles[i] = mRatios[i] * avg;
        }

        return angles;
    }

    private Rect getDialRect() {
        //获取转盘可绘制rect
        int width, height;
        width = getMeasuredWidth();
        height = getMeasuredHeight();
        int paddingLeft = getPaddingLeft();
        int paddingTop = getPaddingTop();
        int paddingRight = getPaddingRight();
        int paddingBottom = getPaddingBottom();
        return new Rect(paddingLeft, paddingTop, width - paddingRight, height - paddingBottom);
    }


    /**
     * 设置每个色块的颜色
     *
     * @param colors the colors
     */
    public void setColors(@ColorInt int[] colors) {
        this.mColors = colors;
    }

    /**
     * 设置每个色块对应的文本
     *
     * @param texts the texts
     */
    public void setTexts(String[] texts) {
        this.mTexts = texts;
    }

    /**
     * 设置每个色块大小对应的比例
     *
     * @param ratios the ratios
     */
    public void setRatios(float[] ratios) {
        this.mRatios = ratios;
    }

    /**
     * 转盘显示偏移的角度
     *
     * @param offsetAngle the offset angle
     */
    public void setStartOffsetAngle(float offsetAngle) {
        this.mDialOffsetDegree = offsetAngle;
    }

    /**
     * 抽奖开始之后,旋转的圈数
     *
     * @param num the num
     */
    public void setMinRotateNum(int num) {
        if (num > 1) {
            this.dialNum = num - 1;
        }
    }

    public void setTextSize(float size) {
        mTextSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, size, getResources().getDisplayMetrics());
    }

    /**
     * 文本向外的偏移比例 0-1f
     *
     * @param offset the offset
     */
    public void setTextOffset(float offset) {
        mTextOffset = offset;
    }

    public void setIcons(Bitmap[] bitmaps) {
        this.mIcons = bitmaps;
    }

    public String getText(int index) {
        return mTexts[index];
    }

    public int getDialNum() {
        return mRatios.length;
    }
}