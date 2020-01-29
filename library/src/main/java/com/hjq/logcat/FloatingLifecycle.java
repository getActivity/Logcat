package com.hjq.logcat;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import com.hjq.xtoast.XToast;

/**
 *    author : Android 轮子哥
 *    github : https://github.com/getActivity/Logcat
 *    time   : 2020/01/24
 *    desc   : 悬浮窗生命控制
 */
final class FloatingLifecycle implements Application.ActivityLifecycleCallbacks {

    private XToast mToast;
    private Activity mTopActivity;

    static void with(Application application, XToast toast) {
        application.registerActivityLifecycleCallbacks(new FloatingLifecycle(toast));
    }

    private FloatingLifecycle(XToast toast) {
        mToast = toast;
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {}

    @Override
    public void onActivityStarted(Activity activity) {}

    @Override
    public void onActivityResumed(Activity activity) {
        if (activity instanceof LogcatActivity) {
            return;
        }
        mTopActivity = activity;
        if (mToast != null && !mToast.isShow()) {
            mToast.show();
        }
    }

    @Override
    public void onActivityPaused(Activity activity) {}

    @Override
    public void onActivityStopped(Activity activity) {
        if (activity instanceof LogcatActivity) {
            return;
        }
        if (mTopActivity == activity) {
            mTopActivity = null;
            if (mToast != null && mToast.isShow()) {
                mToast.cancel();
            }
        }
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {}

    @Override
    public void onActivityDestroyed(Activity activity) {

    }
}