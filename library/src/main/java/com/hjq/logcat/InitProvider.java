package com.hjq.logcat;

import android.app.Application;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.widget.Toast;

/**
 *    author : Android 轮子哥
 *    github : https://github.com/getActivity/Logcat
 *    time   : 2020/01/24
 *    desc   : Logcat 初始化
 */
public final class InitProvider extends ContentProvider {

    @Override
    public boolean onCreate() {
        Context context = getContext();
        if (context != null) {
            LogcatConfig.init(context.getApplicationContext());
            if (context instanceof Application) {
                FloatingLifecycle.with((Application) context);
            } else {
                Toast.makeText(context, "参数错误，无法启动 Logcat", Toast.LENGTH_LONG).show();
            }
        }
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        return null;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        return null;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }
}