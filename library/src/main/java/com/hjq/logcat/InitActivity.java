package com.hjq.logcat;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Toast;

import com.hjq.permissions.OnPermission;
import com.hjq.permissions.Permission;
import com.hjq.permissions.XXPermissions;

import java.util.List;

/**
 * author : Android 轮子哥
 * github : https://github.com/getActivity/Logcat
 * time   : 2020/01/24
 * desc   : 权限申请和开启 Logcat
 */
public final class InitActivity extends Activity
        implements OnPermission {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        XXPermissions.with(this)
                .permission(Permission.SYSTEM_ALERT_WINDOW)
                .request(this);
    }

    @Override
    public void hasPermission(List<String> granted, boolean all) {
        FloatingLifecycle.with(getApplication(), new FloatingWindow(getApplication()).show());
        finish();
    }

    @Override
    public void noPermission(List<String> denied, boolean quick) {
        Toast.makeText(getApplicationContext(), "权限申请失败，无法显示 Logcat", Toast.LENGTH_LONG).show();
        finish();
    }
}