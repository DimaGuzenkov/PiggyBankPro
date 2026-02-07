package com.example.piggybankpro.presentation.views.costomViews;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

import com.example.piggybankpro.R;

public class RingProgressBar extends View {
    public static int progressLow = Color.parseColor("#F44336");
    public static int progressMedium = Color.parseColor("#FF9800");
    public static int progressHigh = Color.parseColor("#4CAF50");

    private Paint backgroundPaint;
    private Paint progressPaint;
    private Paint textPaint;

    private double progress = 0;
    private int max = 100;

    private int backgroundColor = Color.LTGRAY;
    private int progressColor = Color.BLUE;
    private int textColor = Color.BLACK;

    private float strokeWidth = 20;
    private float textSize = 40;

    private boolean showText = true;
    private boolean halfCircle = false;
    private boolean showCheckOnComplete = true;

    // Для анимации
    private float checkScale = 0f;
    private boolean isCheckVisible = false;

    // Drawable галочки
    private Drawable checkDrawable;
    private int checkDrawableResId = -1;

    private RectF rectF;

    public RingProgressBar(Context context) {
        super(context);
        init(null);
    }

    public RingProgressBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public RingProgressBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        if (attrs != null) {
            TypedArray ta = getContext().obtainStyledAttributes(attrs, R.styleable.RingProgressBar);
            backgroundColor = ta.getColor(R.styleable.RingProgressBar_backgroundColor, backgroundColor);
            progressColor = ta.getColor(R.styleable.RingProgressBar_progressColor, progressColor);
            textColor = ta.getColor(R.styleable.RingProgressBar_textColor, textColor);
            strokeWidth = ta.getDimension(R.styleable.RingProgressBar_strokeWidth, strokeWidth);
            textSize = ta.getDimension(R.styleable.RingProgressBar_textSize, textSize);
            showText = ta.getBoolean(R.styleable.RingProgressBar_showText, showText);
            halfCircle = ta.getBoolean(R.styleable.RingProgressBar_halfCircle, halfCircle);

            checkDrawableResId = ta.getResourceId(R.styleable.RingProgressBar_checkDrawable, checkDrawableResId);
            ta.recycle();
        }

        // Загружаем drawable галочки
        if (checkDrawableResId != -1) {
            checkDrawable = getContext().getDrawable(checkDrawableResId);
            if (checkDrawable != null) {
                checkDrawable.setAlpha(0); // Начально невидима
            }
        }

        // Настройка Paint для фона
        backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        backgroundPaint.setColor(backgroundColor);
        backgroundPaint.setStyle(Paint.Style.STROKE);
        backgroundPaint.setStrokeWidth(strokeWidth);
        backgroundPaint.setStrokeCap(Paint.Cap.ROUND);

        // Настройка Paint для прогресса
        progressPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        progressPaint.setColor(progressColor);
        progressPaint.setStyle(Paint.Style.STROKE);
        progressPaint.setStrokeWidth(strokeWidth);
        progressPaint.setStrokeCap(Paint.Cap.ROUND);

        // Настройка Paint для текста
        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(textColor);
        textPaint.setTextSize(textSize);
        textPaint.setTextAlign(Paint.Align.CENTER);

        rectF = new RectF();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float width = getWidth();
        float height = getHeight();
        float radius = width / 2 - strokeWidth;

        float centerX = width / 2;
        float centerY = 3 * height / 4;

        rectF.set(centerX - radius, centerY - radius,
                centerX + radius, centerY + radius);

        boolean isComplete = progress >= max;

        if (isComplete && showCheckOnComplete && checkDrawable != null) {
            drawCheckMark(canvas, centerX, height / 2, radius);
        } else {
            drawProgressBar(canvas);

            if (showText) {
                drawProgressText(canvas, centerX, centerY);
            }

            // Если была видна галочка, скрываем ее
            if (isCheckVisible) {
                hideCheckMark();
            }
        }
    }

    private void drawProgressBar(Canvas canvas) {
        float startAngle, sweepAngle;

        if (halfCircle) {
            startAngle = 180;
            sweepAngle = 180;
        } else {
            startAngle = -90;
            sweepAngle = 360;
        }

        canvas.drawArc(rectF, startAngle, sweepAngle, false, backgroundPaint);

        double progressSweep = progress / max * sweepAngle;
        canvas.drawArc(rectF, startAngle, (float) progressSweep, false, progressPaint);
    }

    private void drawProgressText(Canvas canvas, float centerX, float centerY) {
        @SuppressLint("DefaultLocale")
        String progressText = String.format("%.1f%%", progress);
        canvas.drawText(progressText, centerX, centerY, textPaint);
    }

    private void drawCheckMark(Canvas canvas, float centerX, float centerY, float radius) {
        if (checkDrawable == null) {
            return;
        }

        // Размер галочки (60% от радиуса прогресс-бара)
        int checkSize = (int) (radius * 2);
        int left = (int) (centerX - (float) checkSize / 2);
        int top = (int) (centerY - (float) checkSize / 2);
        int right = left + checkSize;
        int bottom = top + checkSize;

        // Устанавливаем границы для drawable
        checkDrawable.setBounds(left, top, right, bottom);

        // Применяем анимацию масштаба
        if (checkScale > 0) {
            canvas.save();
            canvas.scale(checkScale, checkScale, centerX, centerY);
            checkDrawable.draw(canvas);
            canvas.restore();
        } else {
            checkDrawable.draw(canvas);
        }

        // Если достигли 100% и нужно показать галочку, запускаем анимацию
        if (progress >= max && showCheckOnComplete && !isCheckVisible) {
            showCheckMark();
        }
    }

    private void showCheckMark() {
        if (checkDrawable == null || isCheckVisible) {
            return;
        }

        isCheckVisible = true;

        // Анимация появления
        AnimatorSet animatorSet = new AnimatorSet();

        ObjectAnimator scaleAnimator = ObjectAnimator.ofFloat(this, "checkScale", 0f, 1f);
        scaleAnimator.setDuration(400);
        scaleAnimator.setInterpolator(new AccelerateDecelerateInterpolator());

        ObjectAnimator alphaAnimator = ObjectAnimator.ofInt(checkDrawable, "alpha", 0, 255);
        alphaAnimator.setDuration(300);

        animatorSet.playTogether(scaleAnimator, alphaAnimator);
        animatorSet.start();
    }

    private void hideCheckMark() {
        if (checkDrawable == null || !isCheckVisible) {
            return;
        }

        isCheckVisible = false;

        // Анимация исчезновения
        ObjectAnimator alphaAnimator = ObjectAnimator.ofInt(checkDrawable, "alpha", 255, 0);
        alphaAnimator.setDuration(200);
        alphaAnimator.start();

        checkScale = 0f;
    }

    // Метод для анимации масштаба
    public void setCheckScale(float scale) {
        this.checkScale = scale;
        invalidate();
    }

    public float getCheckScale() {
        return checkScale;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int desiredSize = 200; // минимальный размер в пикселях

        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        int width;
        int height;

        if (widthMode == MeasureSpec.EXACTLY) {
            width = widthSize;
        } else if (widthMode == MeasureSpec.AT_MOST) {
            width = Math.min(desiredSize, widthSize);
        } else {
            width = desiredSize;
        }

        if (heightMode == MeasureSpec.EXACTLY) {
            height = heightSize;
        } else if (heightMode == MeasureSpec.AT_MOST) {
            height = Math.min(desiredSize, heightSize);
        } else {
            height = desiredSize;
        }

        setMeasuredDimension(width, height);
    }

    public void setProgress(double progress) {
        var oldProgress = this.progress;
        this.progress = Math.min(max, Math.max(0, progress));

        if (progress < 30) {
            setProgressColor(progressLow);
        } else if (progress < 70) {
            setProgressColor(progressMedium);
        } else {
            setProgressColor(progressHigh);
        }

        if (oldProgress < max && this.progress >= max && showCheckOnComplete) {
            isCheckVisible = false;
        } else if (oldProgress >= max && this.progress < max) {
            hideCheckMark();
        }

        invalidate();
    }

    public double getProgress() {
        return progress;
    }

    public void setProgressInPercent(int percent) {
        setProgress(percent / 100f);
    }

    public void setMax(int max) {
        this.max = max;
        invalidate();
    }

    public int getMax() {
        return max;
    }

    public void setBackgroundColor(int color) {
        backgroundColor = color;
        backgroundPaint.setColor(color);
        invalidate();
    }

    public void setProgressColor(int color) {
        progressColor = color;
        progressPaint.setColor(color);
        invalidate();
    }

    public void setTextColor(int color) {
        textColor = color;
        textPaint.setColor(color);
        invalidate();
    }

    public void setStrokeWidth(float width) {
        strokeWidth = width;
        backgroundPaint.setStrokeWidth(width);
        progressPaint.setStrokeWidth(width);
        invalidate();
    }

    public void setHalfCircle(boolean halfCircle) {
        this.halfCircle = halfCircle;
        invalidate();
    }

    public void setShowText(boolean showText) {
        this.showText = showText;
        invalidate();
    }

    public void setShowCheckOnComplete(boolean showCheckOnComplete) {
        this.showCheckOnComplete = showCheckOnComplete;
        invalidate();
    }

    public void setCheckDrawable(int drawableResId) {
        this.checkDrawableResId = drawableResId;
        checkDrawable = getContext().getDrawable(drawableResId);
        if (checkDrawable != null) {
            checkDrawable.setAlpha(0);
        }
        invalidate();
    }
}