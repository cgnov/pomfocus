package com.example.pomfocus;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;

public class CustomCanvas extends View {
    private Paint mCompletedPaint = new Paint();
    private Paint mToGoPaint = new Paint();
    private RectF mRect;
    private int mcx, mcy, mRadius;
    private float mSweep = 360F;
    private static final float START_ANGLE = -90F;
    private static final String TAG = "CustomCanvas";

    public CustomCanvas(Context context) {
        super(context);
        setup();
    }

    public CustomCanvas(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setup();
    }

    public CustomCanvas(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setup();
    }

    private void setup() {
        mCompletedPaint.setColor(getResources().getColor(R.color.red7));
        mCompletedPaint.setStyle(Paint.Style.FILL);
        mToGoPaint.setColor(getResources().getColor(R.color.grey5));
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

                mRect = new RectF(left, top, right, bottom);
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
        mSweep = percentLeft;
        invalidate();
    }
}
