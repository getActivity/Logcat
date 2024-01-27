package com.hjq.logcat;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.view.View;

/**
 *    author : Android 轮子哥
 *    github : https://github.com/getActivity/Logcat
 *    time   : 2020/01/24
 *    desc   : Logcat 悬浮窗全局显示派发
 */
final class LogcatGlobalDispatcher implements Application.ActivityLifecycleCallbacks {

    static void launch(Application application) {
        LogcatWindow logcatWindow = new LogcatWindow(application);
        logcatWindow.show();

        application.registerActivityLifecycleCallbacks(new LogcatGlobalDispatcher(logcatWindow));
    }

    private final LogcatWindow mLogcatWindow;

    private LogcatGlobalDispatcher(LogcatWindow logcatWindow) {
        mLogcatWindow = logcatWindow;
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {}

    @Override
    public void onActivityStarted(Activity activity) {}

    @Override
    public void onActivityResumed(Activity activity) {
        if (!(activity instanceof LogcatActivity)) {
            return;
        }
        mLogcatWindow.setWindowVisibility(View.GONE);
    }

    @Override
    public void onActivityPaused(Activity activity) {}

    @Override
    public void onActivityStopped(Activity activity) {
        if (!(activity instanceof LogcatActivity)) {
            return;
        }
        mLogcatWindow.setWindowVisibility(View.VISIBLE);
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {}

    @Override
    public void onActivityDestroyed(Activity activity) {}
}