package fm.radiant.android.lib.widgets;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.widget.ProgressBar;
import android.widget.TextView;

import fm.radiant.android.R;

public class CircleProgressBar extends TextView {
    private int mProgress;
    private int mMax;

    private float mCenter;
    private float mProgressBackgroundRadius;

    private Paint mProgressBackgroundPaint = new Paint();
    private Paint mProgressForegroundPaint = new Paint();

    private int mProgressBackgroundColor;
    private int mProgressForegroundColor;
    private int mProgressBackgroundWidth;
    private int mProgressForegroundWidth;

    private final RectF mProgressForegrounsBounds = new RectF();

    public CircleProgressBar(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.circleProgressBarStyle);
    }

    public CircleProgressBar(Context context, AttributeSet attrs, final int defStyle) {
        super(context, attrs, defStyle);

        final TypedArray attributes = context.obtainStyledAttributes(attrs, R.styleable.CircleProgressBar, defStyle, 0);

        setProgress(attributes.getInt(R.styleable.CircleProgressBar_progress, 0));
        setMax(attributes.getInt(R.styleable.CircleProgressBar_max, 100));

        setProgressForegroundColor(attributes.getColor(R.styleable.CircleProgressBar_progress_foregroundColor, Color.WHITE));
        setProgressBackgroundColor(attributes.getColor(R.styleable.CircleProgressBar_progress_backgroundColor, Color.BLACK));

        setProgressForegroundWidth((int) attributes.getDimension(R.styleable.CircleProgressBar_progress_foregroundWidth, 5));
        setProgressBackgroundWidth((int) attributes.getDimension(R.styleable.CircleProgressBar_progress_backgroundWidth, 5));

        setGravity(Gravity.CENTER);
    }

    @Override
    protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, widthMeasureSpec);

        final int side = getDefaultSize(getSuggestedMinimumWidth(), widthMeasureSpec);
        mCenter = side * 0.5f;

        mProgressBackgroundRadius = mCenter - mProgressBackgroundWidth / 2 - 1;

        float negativePoint = mProgressForegroundWidth / 2 - mCenter + 1;
        float positivePoint = mCenter - mProgressForegroundWidth / 2 - 1;

        mProgressForegrounsBounds.set(negativePoint, negativePoint, positivePoint, positivePoint);
    }

    @Override
    protected void onDraw(final Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawCircle(mCenter, mCenter, mProgressBackgroundRadius, mProgressBackgroundPaint);

        canvas.translate(mCenter, mCenter);
        canvas.drawArc(mProgressForegrounsBounds, -90, getAngle(), false, mProgressForegroundPaint);
    }

    public int getProgress() {
        return mProgress;
    }

    public void setProgress(int progress) {
        mProgress = progress;

        invalidate();
    }

    public int getMax() {
        return mMax;
    }

    public void setMax(int max) {
        mMax = max;

        invalidate();
    }

    public int getProgressBackgroundColor() {
        return mProgressBackgroundColor;
    }

    public void setProgressBackgroundColor(int color) {
        mProgressBackgroundColor = color;

        updateProgressBackgroundColor();
    }

    public int getProgressForegroundColor() {
        return mProgressForegroundColor;
    }

    public void setProgressForegroundColor(int color) {
        mProgressForegroundColor = color;

        updateProgressForegroundColor();
    }

    public int getProgressBackgroundWidth() {
        return mProgressBackgroundWidth;
    }

    public void setProgressBackgroundWidth(int width) {
        mProgressBackgroundWidth = width;

        updateProgressBackgroundColor();
    }

    public int getProgressForegroundWidth() {
        return mProgressForegroundWidth;
    }

    public void setProgressForegroundWidth(int width) {
        mProgressForegroundWidth = width;

        updateProgressForegroundColor();
    }

    private float getAngle() {
        return (float) mProgress / mMax * 360;
    }

    private void updateProgressBackgroundColor() {
        mProgressBackgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mProgressBackgroundPaint.setStyle(Paint.Style.STROKE);
        mProgressBackgroundPaint.setColor(mProgressBackgroundColor);
        mProgressBackgroundPaint.setStrokeWidth(mProgressBackgroundWidth);

        invalidate();
    }

    private void updateProgressForegroundColor() {
        mProgressForegroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mProgressForegroundPaint.setStyle(Paint.Style.STROKE);
        mProgressForegroundPaint.setColor(mProgressForegroundColor);
        mProgressForegroundPaint.setStrokeWidth(mProgressForegroundWidth);

        invalidate();
    }
}
