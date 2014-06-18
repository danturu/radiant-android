package fm.radiant.android.lib.widgets;

import android.content.Context;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Message;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.animation.LinearInterpolator;
import android.widget.Scroller;
import android.widget.TextView;

import org.apache.commons.codec.binary.StringUtils;
import org.joda.time.format.FormatUtils;

import java.lang.ref.WeakReference;
import java.text.Format;

public class AutoScrollTextView extends TextView {
    private Scroller mScroller;
    private boolean  mCanScroll = false;

    private CharSequence mText;
    private CharSequence mSpacer = "          ";

    private int timePerLetter = 300;

    public AutoScrollTextView(Context context) {
        this(context, null);
    }

    public AutoScrollTextView(Context context, AttributeSet attrs) {
        this(context, attrs, android.R.attr.textViewStyle);
    }

    public AutoScrollTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        setSingleLine();

        mScroller = new Scroller(getContext(), new LinearInterpolator());
        mScroller.forceFinished(true);
        setScroller(mScroller);
    }

    public void startScroll() {
        setHorizontallyScrolling(true);

        if (!mCanScroll) {
            return;
        }

        float dx = measureText(TextUtils.concat(mText, mSpacer).toString());

        mScroller.startScroll(0, 0, (int) dx, 0, timePerLetter * getText().length());
        invalidate();
    }

    @Override
    public void setText(CharSequence charSequence, BufferType bufferType) {
        mText = charSequence;

        if (measureText(charSequence.toString()) > getMeasuredWidth()) {
            mCanScroll = true;
            setGravity(Gravity.NO_GRAVITY);
            super.setText(TextUtils.concat(mText, mSpacer, mText) , bufferType);
        } else {
            mCanScroll = false;
            scrollTo(0, 0);
            setGravity(Gravity.CENTER);
            super.setText(mText, bufferType);
        }
    }

    @Override
    public void onSizeChanged(int w, int h, int oldw, int oldh) {
        mScroller.abortAnimation();

        if (measureText(getText().toString()) > w) {
            mCanScroll = true;
            setGravity(Gravity.NO_GRAVITY);
            super.setText(TextUtils.concat(mText, mSpacer, mText));
        } else {
           // scrollTo(0, 0);
            mCanScroll = false;
            setGravity(Gravity.CENTER);
            super.setText(mText);
        }

        super.onSizeChanged(w, h, oldw, oldh);
        startScroll();
    }

    @Override
    public CharSequence getText() {
        return mText;
    }

    @Override
    public void computeScroll() {
        super.computeScroll();

        if (mScroller != null && mScroller.isFinished()) {
            startScroll();
        }
    }

    public void setTimePerLetter(int time) {
        timePerLetter = time;
    }

    private float measureText(String str) {
        return getPaint().measureText(str);
    }
}