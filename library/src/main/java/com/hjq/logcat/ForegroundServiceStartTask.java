package com.hjq.logcat;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

/**
 *    author : Android 轮子哥
 *    github : https://github.com/getActivity/Logcat
 *    time   : 2022/10/02
 *    desc   : 开启前台 Service 任务
 */
final class ForegroundServiceStartTask implements Application.ActivityLifecycleCallbacks {

    static void with(Application application) {
        application.registerActivityLifecycleCallbacks(new ForegroundServiceStartTask());
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        Intent intent = new Intent(activity, LogcatService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Android 8.0 启动后台 service 出错 IllegalStateException: Not allowed to start service Intent
            // 解决此问题的方案是将 startService 替换成 startForegroundService
            // https://blog.csdn.net/u010784887/article/details/79675147
            // Caused by: android.app.BackgroundServiceStartNotAllowedException:
            // Not allowed to start service Intent { cmp=com.hjq.easy.demo/com.hjq.logcat.LogcatService }:
            // app is in background uid UidRecord{48aba27 u0a399 CEM  idle change:idle|cached procs:0 seq(0,0,0)}
            //    at android.app.ContextImpl.startServiceCommon(ContextImpl.java:1889)
            //    at android.app.ContextImpl.startService(ContextImpl.java:1840)
            //    at android.content.ContextWrapper.startService(ContextWrapper.java:774)
            //    at com.hjq.logcat.LogcatProvider.onCreate(LogcatProvider.java:42)
            //    at android.content.ContentProvider.attachInfo(ContentProvider.java:2476)
            //    at android.content.ContentProvider.attachInfo(ContentProvider.java:2446)
            //    at android.app.ActivityThread.installProvider(ActivityThread.java:7915)
            //    at android.app.ActivityThread.installContentProviders(ActivityThread.java:7405)
            //    at android.app.ActivityThread.handleBindApplication(ActivityThread.java:7159)
            //    at android.app.ActivityThread.access$1800(ActivityThread.java:284)
            //    at android.app.ActivityThread$H.handleMessage(ActivityThread.java:2277)
            //    at android.os.Handler.dispatchMessage(Handler.java:106)
            //    at android.os.Looper.loopOnce(Looper.java:233)
            //    at android.os.Looper.loop(Looper.java:334)
            //    at android.app.ActivityThread.main(ActivityThread.java:8344)
            //    at java.lang.reflect.Method.invoke(Native Method)
            //    at com.android.internal.os.RuntimeInit$MethodAndArgsCaller.run(RuntimeInit.java:582)
            //    at com.android.internal.os.ZygoteInit.main(ZygoteInit.java:1068)

            // Android 12 启动后台 Service 出错；ForegroundServiceStartNotAllowedException:
            // startForegroundService() not allowed due to mAllowStartForeground false: service xxx.LogcatService
            // 解决此问题的方案是将前台 Service 启动时机挪动到第一个 Activity 启动的时候
            // https://developer.android.google.cn/about/versions/12/foreground-services#cases-fgs-background-starts-allowed
            // Caused by: android.app.ForegroundServiceStartNotAllowedException: startForegroundService() not allowed due to mAllowStartForeground false: service com.hjq.easy.demo/com.hjq.logcat.LogcatService
            //    at android.app.ForegroundServiceStartNotAllowedException$1.createFromParcel(ForegroundServiceStartNotAllowedException.java:54)
            //    at android.app.ForegroundServiceStartNotAllowedException$1.createFromParcel(ForegroundServiceStartNotAllowedException.java:50)
            //    at android.os.Parcel.readParcelable(Parcel.java:3347)
            //    at android.os.Parcel.createExceptionOrNull(Parcel.java:2434)
            //    at android.os.Parcel.createException(Parcel.java:2423)
            //    at android.os.Parcel.readException(Parcel.java:2406)
            //    at android.os.Parcel.readException(Parcel.java:2348)
            //    at android.app.IActivityManager$Stub$Proxy.startService(IActivityManager.java:6891)
            //    at android.app.ContextImpl.startServiceCommon(ContextImpl.java:1875)
            //    at android.app.ContextImpl.startForegroundService(ContextImpl.java:1846)
            //    at android.content.ContextWrapper.startForegroundService(ContextWrapper.java:779)
            //    at com.hjq.logcat.LogcatProvider.onCreate(LogcatProvider.java:48)
            //    at android.content.ContentProvider.attachInfo(ContentProvider.java:2476)
            //    at android.content.ContentProvider.attachInfo(ContentProvider.java:2446)
            //    at android.app.ActivityThread.installProvider(ActivityThread.java:7915)
            //    at android.app.ActivityThread.installContentProviders(ActivityThread.java:7405)
            //    at android.app.ActivityThread.handleBindApplication(ActivityThread.java:7159)
            //    at android.app.ActivityThread.access$1800(ActivityThread.java:284)
            //    at android.app.ActivityThread$H.handleMessage(ActivityThread.java:2277)
            //    at android.os.Handler.dispatchMessage(Handler.java:106)
            //    at android.os.Looper.loopOnce(Looper.java:233)
            //    at android.os.Looper.loop(Looper.java:334)
            //    at android.app.ActivityThread.main(ActivityThread.java:8344)
            //    at java.lang.reflect.Method.invoke(Native Method)
            //    at com.android.internal.os.RuntimeInit$MethodAndArgsCaller.run(RuntimeInit.java:582)
            //    at com.android.internal.os.ZygoteInit.main(ZygoteInit.java:1068)
            activity.startForegroundService(intent);
        } else {
            activity.startService(intent);
        }

        // 任务完成，移除 Activity 生命周期监听
        activity.getApplication().unregisterActivityLifecycleCallbacks(this);
    }

    @Override
    public void onActivityStarted(Activity activity) {}

    @Override
    public void onActivityResumed(Activity activity) {}

    @Override
    public void onActivityPaused(Activity activity) {}

    @Override
    public void onActivityStopped(Activity activity) {}

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {}

    @Override
    public void onActivityDestroyed(Activity activity) {}
}