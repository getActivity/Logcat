package com.hjq.logcat.demo;

import android.app.Application;

import com.hjq.toast.ToastUtils;

public final class AppApplication extends Application {

   @Override
   public void onCreate() {
      super.onCreate();
      ToastUtils.init(this);
   }
}