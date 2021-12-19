package com.hjq.logcat;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.View;

import java.util.List;

/**
 *    author : Android 轮子哥
 *    github : https://github.com/getActivity/Logcat
 *    time   : 2021/11/13
 *    desc   : 悬浮球 Behavior
 */
public final class FloatingActionBehavior extends CoordinatorLayout.Behavior<View> {

    private float mTranslationY;

    public FloatingActionBehavior(Context context, AttributeSet attrs) {
        super();
    }

    @Override
    public boolean layoutDependsOn(CoordinatorLayout parent, View child, View dependency) {
        return dependency instanceof Snackbar.SnackbarLayout;
    }

    @Override
    public boolean onDependentViewChanged(CoordinatorLayout parent, View child, View dependency) {
        this.updateTranslation(parent, child, dependency);
        return false;
    }

    @Override
    public void onDependentViewRemoved(CoordinatorLayout parent, View child, View dependency) {
        ViewCompat.animate(child).cancel();
        ViewCompat.animate(child).translationY(0);
        child.setTranslationY(0);
        this.mTranslationY = 0;
    }

    private void updateTranslation(CoordinatorLayout parent, View child, View dependency) {
        float translationY = this.getTranslationY(parent, child);
        if (translationY != this.mTranslationY) {
            ViewCompat.animate(child).cancel();
            if (Math.abs(translationY - this.mTranslationY) == (float) dependency.getHeight()) {
                ViewCompat.animate(child).translationY(translationY);
            } else {
                child.setTranslationY(translationY);
            }

            this.mTranslationY = translationY;
        }
    }

    private float getTranslationY(CoordinatorLayout parent, View child) {
        float minOffset = 0.0F;
        List<?> dependencies = parent.getDependencies(child);
        int i = 0;

        for (int z = dependencies.size(); i < z; ++i) {
            View view = (View) dependencies.get(i);
            if (view instanceof Snackbar.SnackbarLayout && parent.doViewsOverlap(child, view)) {
                minOffset = Math.min(minOffset, view.getTranslationY() - (float) view.getHeight());
            }
        }

        return minOffset;
    }

    @Override
    public boolean onStartNestedScroll(@NonNull CoordinatorLayout coordinatorLayout, @NonNull View child, @NonNull View directTargetChild, @NonNull View target, int axes, int type) {
        return axes == ViewCompat.SCROLL_AXIS_VERTICAL || super.onStartNestedScroll(coordinatorLayout, child, directTargetChild, target, axes, type);
    }

    private boolean mAnimFlag;

    @Override
    public void onNestedScroll(@NonNull CoordinatorLayout coordinatorLayout, @NonNull final View child, @NonNull View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed, int type) {
        super.onNestedScroll(coordinatorLayout, child, target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, type);
        if (mAnimFlag) {
            return;
        }
        if (dyConsumed > 0) {
            if (child.getVisibility() == View.INVISIBLE) {
                return;
            }
            mAnimFlag = true;
            ValueAnimator animator = ValueAnimator.ofFloat(1, 0);
            animator.setDuration(300);
            animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    float scale = (float) animation.getAnimatedValue();
                    child.setScaleX(scale);
                    child.setScaleY(scale);
                    if (scale != 0) {
                        return;
                    }
                    child.setVisibility(View.INVISIBLE);
                }
            });
            animator.addListener(new AnimatorListenerAdapter() {

                @Override
                public void onAnimationStart(Animator animation) {
                    mAnimFlag = true;
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    mAnimFlag = false;
                }
            });
            animator.start();

        } else if (dyConsumed < 0) {
            if (child.getVisibility() == View.VISIBLE) {
                return;
            }
            child.setVisibility(View.VISIBLE);
            ValueAnimator animator = ValueAnimator.ofFloat(0, 1);
            animator.setDuration(300);
            animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    float scale = (float) animation.getAnimatedValue();
                    child.setScaleX(scale);
                    child.setScaleY(scale);
                }
            });
            animator.addListener(new AnimatorListenerAdapter() {

                @Override
                public void onAnimationStart(Animator animation) {
                    mAnimFlag = true;
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    mAnimFlag = false;
                }
            });
            animator.start();
        }
    }
}