package fm.radiant.android.lib.widgets;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.Button;

import fm.radiant.android.R;

public class ImageSquareButton extends Button {
    private Drawable mDrawableCenter;

    public ImageSquareButton(Context context) {
        super(context);
    }

    public ImageSquareButton(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.imageSquareButtonStyle);
    }

    public ImageSquareButton(Context context, AttributeSet attrs, final int defStyle) {
        super(context, attrs, defStyle);

        final TypedArray attributes = context.obtainStyledAttributes(attrs, R.styleable.ImageSquareButton, defStyle, 0);

        if (attributes.hasValue(R.styleable.ImageSquareButton_drawableCenter)) {
           setDrawableCenter(attributes.getDrawable(R.styleable.ImageSquareButton_drawableCenter));
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, widthMeasureSpec);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (mDrawableCenter != null)  {
            canvas.save();
            canvas.translate((((float) getWidth()) / 2) - ((float) mDrawableCenter.getIntrinsicWidth()) / 2, (((float)getHeight()) / 2) - ((float) mDrawableCenter.getIntrinsicHeight()) / 2);
            mDrawableCenter.draw(canvas);
            canvas.restore();
        }
    }

    public void setDrawableCenter(int imageResource) {
        setDrawableCenter(getResources().getDrawable(imageResource));
    }

    public void setDrawableTop(int imageResource) {
        setDrawableCenter(null);
        setDrawableTop(getResources().getDrawable(imageResource));
    }

    public void setDrawableTop(Drawable image) {
        setDrawableCenter(null);
        setCompoundDrawablesWithIntrinsicBounds(null, image, null, null);
    }

    public void setDrawableCenter(Drawable image) {
        if (mDrawableCenter == image) {
            return;
        }

        mDrawableCenter = image;

        updateCenterDrawable();
    }

    public void mimicImageButton() {
        setText("");
        setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
    }

    public void mimicTextButton() {
        setDrawableCenter(null);
    }

    @Override
    protected void drawableStateChanged() {
        super.drawableStateChanged();

        if (mDrawableCenter != null && mDrawableCenter.isStateful()) {
            mDrawableCenter.setState(getDrawableState());
        }
    }

    @Override
    protected boolean setFrame(int l, int t, int r, int b) {
        boolean changed = super.setFrame(l, t, r, b);
        configureCenterDrawableBounds();
        return changed;
    }

    private void updateCenterDrawable() {
        configureCenterDrawableBounds();
        invalidate();
    }

    private void configureCenterDrawableBounds() {
        if (mDrawableCenter == null) {
            return;
        }

        mDrawableCenter.setBounds(new Rect(0, 0, mDrawableCenter.getIntrinsicWidth(), mDrawableCenter.getIntrinsicHeight()));
    }
}