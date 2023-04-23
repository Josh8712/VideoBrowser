package com.jcomp.browser.widget;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

import com.jcomp.browser.R;

public class BreathingAnim {
    public static void breath(View v, float fromRange, float toRange,
                              long duration) {
        BreathingAnim.clear(v);
        ObjectAnimator animator = ObjectAnimator.ofFloat(v, View.ALPHA,
                fromRange, toRange);
        animator.setDuration(duration);
        animator.setRepeatMode(ValueAnimator.REVERSE);
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.start();
        v.setTag(R.id.breath_tag, animator);
    }

    public static void clear(View v) {
        Object tag = v.getTag(R.id.breath_tag);
        if (tag instanceof ObjectAnimator) {
            ((ObjectAnimator) tag).cancel();
            v.setAlpha(1f);
        }
    }
}
