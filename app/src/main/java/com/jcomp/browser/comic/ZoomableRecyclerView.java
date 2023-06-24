package com.jcomp.browser.comic;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.VelocityTracker;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.GestureDetectorCompat;
import androidx.core.view.VelocityTrackerCompat;
import androidx.dynamicanimation.animation.FlingAnimation;
import androidx.dynamicanimation.animation.FloatValueHolder;
import androidx.recyclerview.widget.RecyclerView;

import com.jcomp.browser.R;

public class ZoomableRecyclerView extends RecyclerView {

    private ScaleGestureDetector mScaleDetector;
    private GestureDetectorCompat mGestureDetector;
    private static final int DEFAULT_ZOOM_DURATION = 200;
    private static final float DEFAULT_SCALE = 1;
    private int mViewWidth, mViewHeight;
    private float mTranX = 0, mScaleFactor = 1;
    VelocityTracker mVelocityTracker;
    private ValueAnimator animator;
    FlingAnimation flingAnimation;

    public ZoomableRecyclerView(Context context) {
        super(context);
        init(null);
    }

    public ZoomableRecyclerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public ZoomableRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs);
    }

    private void init(AttributeSet attr) {
        mScaleDetector = new ScaleGestureDetector(getContext(), new ScaleListener());
        mGestureDetector = new GestureDetectorCompat(getContext(), new GestureListener());
        mVelocityTracker = VelocityTracker.obtain();
        animator = new ValueAnimator();
        animator.setInterpolator(new DecelerateInterpolator());
        animator.setDuration(DEFAULT_ZOOM_DURATION);
        animator.addUpdateListener(animation -> {
            mScaleFactor = (float) animation.getAnimatedValue("scale");
            mTranX = (float) animation.getAnimatedValue("tranX");
            invalidate();
        });
        flingAnimation = new FlingAnimation(new FloatValueHolder());
        flingAnimation.addUpdateListener((animation, value, velocity) -> {
            mTranX = value;
            invalidate();
        });
        flingAnimation.addEndListener((animation, canceled, value, velocity) -> {
            fixPosition();
        });
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        mViewWidth = MeasureSpec.getSize(widthMeasureSpec);
        mViewHeight = MeasureSpec.getSize(heightMeasureSpec);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void dispatchDraw(@NonNull Canvas canvas) {
        canvas.save();
        canvas.translate(mTranX, 1);
        canvas.scale(mScaleFactor, mScaleFactor);
        super.dispatchDraw(canvas);
        canvas.restore();
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent e) {
        onTouchEvent(e);
        return super.onInterceptTouchEvent(e);
    }

    float initX, initY, initTranX;
    boolean isMoving, hasMoved;
    @Override
    public boolean onTouchEvent(@NonNull MotionEvent ev) {
        super.onTouchEvent(ev);
        if(ev.getPointerCount() == 2)
            mScaleDetector.onTouchEvent(ev);
        if(!hasMoved && mGestureDetector.onTouchEvent(ev))
            return true;
        if(mScaleDetector.isInProgress()) {
            isMoving = false;
            return true;
        }
        if(animator.isRunning())
            return true;
        if(flingAnimation.isRunning())
            flingAnimation.cancel();
        if(ev.getAction() == MotionEvent.ACTION_DOWN) {
            initX = ev.getX();
            initY = ev.getY();
            initTranX = mTranX;
            mVelocityTracker.clear();
            isMoving = true;
            hasMoved = false;
        } else if(ev.getAction() == MotionEvent.ACTION_MOVE) {
            if(!isMoving)
                return true;
            mVelocityTracker.addMovement(ev);
            mTranX = initTranX + ev.getX() - initX;
            mTranX = Math.max(Math.min(mTranX, 0), getMinTranX());
            hasMoved = true;
            invalidate();
        } else if(ev.getAction() == MotionEvent.ACTION_UP) {
            if(getMinTranX() < mTranX && mTranX < 0) {
                mVelocityTracker.computeCurrentVelocity(1000);
                flingAnimation.setStartValue(mTranX)
                        .setStartVelocity(mVelocityTracker.getXVelocity())
                        .setMinValue(getMinTranX())
                        .setMaxValue(0)
                        .start();
            } else {
                fixPosition();
            }
            isMoving = false;
        }
        return true;
    }
    private void fixPosition() {
        float endTanX = mTranX;
        float endFactor = mScaleFactor;
        if(endFactor < DEFAULT_SCALE) {
            endFactor = DEFAULT_SCALE;
        }
        if(endFactor == DEFAULT_SCALE) {
            endTanX = 0;
        } else if(mTranX > 0) {
            endTanX = 0;
        } else if(getMinTranX() > mTranX) {
            endTanX = getMinTranX();
        }

        PropertyValuesHolder tranXHolder = PropertyValuesHolder
                .ofFloat("tranX", mTranX, endTanX);
        PropertyValuesHolder scaleHolder = PropertyValuesHolder
                .ofFloat("scale", mScaleFactor, endFactor);
        animator.setValues(tranXHolder, scaleHolder);
        animator.start();
    }

    private float getMinTranX() {
        return -(mViewWidth * mScaleFactor - mViewWidth);
    }


    // handle scale event
    private class ScaleListener implements ScaleGestureDetector.OnScaleGestureListener {
        float initScale;
        private float initScaleCenterX;
        private float initTransX;

        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {
            initScale = mScaleFactor;
            initTransX = mTranX;
            initScaleCenterX = detector.getFocusX();
            return true;
        }

        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            mScaleFactor = initScale * detector.getScaleFactor();
            mScaleFactor = Math.max(mScaleFactor, 0);
            mTranX = -((initScaleCenterX - initTransX) / initScale * mScaleFactor - initScaleCenterX);
            invalidate();
            return false;
        }

        @Override
        public void onScaleEnd(ScaleGestureDetector detector) {
        }
    }


    private class GestureListener extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            float endFactor, endX;

            if (mScaleFactor == DEFAULT_SCALE) {
                endFactor = DEFAULT_SCALE * 2;
                endX = -(endFactor * mViewWidth - mViewWidth) / 2;
            } else {
                endFactor = DEFAULT_SCALE;
                endX = 0;
            }

            PropertyValuesHolder tranXHolder = PropertyValuesHolder
                    .ofFloat("tranX", mTranX, endX);
            PropertyValuesHolder scaleHolder = PropertyValuesHolder
                    .ofFloat("scale", mScaleFactor, endFactor);
            animator.setValues(tranXHolder, scaleHolder);
            animator.start();
            return true;
        }
    }
}
