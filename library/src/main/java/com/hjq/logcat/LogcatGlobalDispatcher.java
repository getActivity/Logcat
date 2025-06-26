package com.hjq.logcat;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.support.annotation.NonNull;
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
    public void onActivityCreated(@NonNull Activity activity, Bundle savedInstanceState) {
        // default implementation ignored
    }

    @Override
    public void onActivityStarted(@NonNull Activity activity) {
        // default implementation ignored
    }

    @Override
    public void onActivityResumed(@NonNull Activity activity) {
        if (!(activity instanceof LogcatActivity)) {
            return;
        }
        mLogcatWindow.setWindowVisibility(View.GONE);
    }

    @Override
    public void onActivityPaused(@NonNull Activity activity) {
        // default implementation ignored
    }

    @Override
    public void onActivityStopped(@NonNull Activity activity) {
        if (!(activity instanceof LogcatActivity)) {
            return;
        }
        mLogcatWindow.setWindowVisibility(View.VISIBLE);
    }

    @Override
    public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {
        // default implementation ignored
    }

    @Override
    public void onActivityDestroyed(@NonNull Activity activity) {
        // default implementation ignored
    }
}