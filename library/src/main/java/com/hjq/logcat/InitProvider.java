package com.hjq.logcat;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.widget.Toast;

import com.hjq.permissions.Permission;
import com.hjq.permissions.XXPermissions;

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
            if (!XXPermissions.isHasPermission(context, Permission.SYSTEM_ALERT_WINDOW)) {
                int count = LogcatConfig.getPermissionsCount();
                if (count >= 3) {
                    Toast.makeText(context, "需要显示 Logcat 请先自行授予悬浮权限", Toast.LENGTH_LONG).show();
                    return true;
                }
                LogcatConfig.setPermissionsCount(++count);
            } else {
                LogcatConfig.setPermissionsCount(0);
            }
            Intent intent = new Intent(context, InitActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
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