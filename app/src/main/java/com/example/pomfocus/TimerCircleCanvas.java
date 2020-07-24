package com.example.pomfocus;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;

import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

public class TimerCircleCanvas extends View {

    private static final String TAG = "TimerCircleCanvas";
    private static final float START_ANGLE = -90F;
    public static final int FULL_ANGLE = 360;
    private final Paint mCompletedPaint = new Paint();
    private final Paint mToGoPaint = new Paint();
    private final RectF mRect = new RectF();
    private int mcx, mcy, mRadius;
    private float mSweep = 360F;

    public TimerCircleCanvas(Context context) {
        super(context);
        setUp();
    }

    public TimerCircleCanvas(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setUp();
    }

    public TimerCircleCanvas(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setUp();
    }

    private void setUp() {
        mCompletedPaint.setColor(ParseApplication.getAttrColor(getContext(), R.attr.colorPrimary));
        mCompletedPaint.setStyle(Paint.Style.FILL);
        mToGoPaint.setColor(ParseApplication.getAttrColor(getContext(), R.attr.backgroundColor));
        mToGoPaint.setStyle(Paint.Style.FILL);

        addOnLayoutChangeListener(new OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View view, int i, int i1, int i2, int i3, int i4, int i5, int i6, int i7) {
                int width = getWidth();
                int height = getHeight();

                Log.i(TAG, width + " " + height);

                int size = Math.min(width, height);

                mcx = width / 2;
                mcy = height / 2;
                mRadius = size / 3;

                int left = mcx - mRadius;
                int right = mcx + mRadius;
                int top = mcy - mRadius;
                int bottom = mcy + mRadius;

                mRect.set(new RectF(left, top, right, bottom));
            }
        });
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawCircle(mcx, mcy, mRadius, mCompletedPaint);
        canvas.drawArc(mRect, START_ANGLE, mSweep, true, mToGoPaint);
    }

    public void onChangeTime(float percentLeft) {
        mSweep = percentLeft*FULL_ANGLE;
        invalidate();
    }
}
