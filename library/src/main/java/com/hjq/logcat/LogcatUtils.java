package com.hjq.logcat;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.Display;
import android.view.Gravity;
import android.view.Surface;
import android.view.WindowManager;
import com.hjq.window.EasyWindow;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 *    author : Android 轮子哥
 *    github : https://github.com/getActivity/Logcat
 *    time   : 2021/11/14
 *    desc   : Logcat 相关工具类
 */
final class LogcatUtils {

    private static final String FILE_TYPE = "Logcat";
    private static final String LOGCAT_TAG_FILTER_FILE = "logcat_tag_filter.txt";
    private static final Charset CHARSET_UTF_8 = VERSION.SDK_INT >= VERSION_CODES.KITKAT ? StandardCharsets.UTF_8 : Charset.forName("UTF-8");

    /**
     * 判断当前是否是竖屏
     */
    static boolean isPortrait(Context context) {
        return context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
    }

    /**
     * 判断 Activity 是否反方向旋转了
     */
    @SuppressWarnings("deprecation")
    static boolean isActivityReverse(Activity activity) {
        // 获取 Activity 旋转的角度
        int activityRotation = Surface.ROTATION_0;
        if (VERSION.SDK_INT >= VERSION_CODES.R) {
            Display display = activity.getDisplay();
            if (display != null) {
                activityRotation = display.getRotation();
            }
        } else {
            WindowManager windowManager = activity.getWindowManager();
            Display defaultDisplay = null;
            if (windowManager != null) {
                defaultDisplay = windowManager.getDefaultDisplay();
            }
            if (defaultDisplay != null) {
                activityRotation = defaultDisplay.getRotation();
            }
        }
        switch (activityRotation) {
            case Surface.ROTATION_180:
            case Surface.ROTATION_270:
                return true;
            case Surface.ROTATION_0:
            case Surface.ROTATION_90:
            default:
                return false;
        }
    }

    /**
     * 获取状态栏高度
     */
    @SuppressWarnings("deprecation")
    static int getStatusBarHeight(Context context) {
        Resources resources = context.getResources();
        int resourceId = resources.getIdentifier("status_bar_height", "dimen", "android");
        return resources.getDimensionPixelSize(resourceId);
    }

    /**
     * 获取清单文件中的 mete 布尔值
     */
    @Nullable
    static Boolean getMetaBooleanData(Context context, String metaKey) {
        try {
            Bundle metaData = context.getPackageManager().getApplicationInfo(
                    context.getPackageName(), PackageManager.GET_META_DATA).metaData;
            if (metaData != null && metaData.containsKey(metaKey)) {
                return Boolean.parseBoolean(String.valueOf(metaData.get(metaKey)));
            }
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取清单文件中的 mete 字符串
     */
    @Nullable
    static String getMetaStringData(Context context, String metaKey) {
        try {
            Bundle metaData = context.getPackageManager().getApplicationInfo(
                    context.getPackageName(), PackageManager.GET_META_DATA).metaData;
            if (metaData != null && metaData.containsKey(metaKey)) {
                return String.valueOf(metaData.get(metaKey));
            }
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 吐司提示
     */
    static void toast(Activity activity, int stringId) {
        toast(activity, activity.getResources().getString(stringId));
    }

    static void toast(Activity activity, CharSequence text) {
        EasyWindow.with(activity)
                .setContentView(R.layout.logcat_window_toast)
                .setWindowDuration(3000)
                .setWindowLocation(Gravity.CENTER, 0, 0)
                .setWindowAnim(android.R.style.Animation_Toast)
                .setTextByTextView(android.R.id.message, text)
                .show();
    }

    /**
     * 保存日志到本地
     */
    static File saveLogToFile(Context context, List<LogcatInfo> data) throws IOException {
        File directory = context.getExternalFilesDir(FILE_TYPE);
        if (directory == null) {
            throw new IOException("The file does not exist.");
        }
        if (!directory.isDirectory()) {
            directory.delete();
        }
        if (!directory.exists()) {
            directory.mkdirs();
        }
        File file = new File(directory, new SimpleDateFormat("yyyyMMdd_kkmmss", Locale.getDefault()).format(new Date()) + ".txt");
        if (!file.isFile()) {
            file.delete();
        }
        if (!file.exists()) {
            file.createNewFile();
        }
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, false), CHARSET_UTF_8));
        for (LogcatInfo info : data) {
            writer.write(info.toString().replace("\n", "\r\n") + "\r\n\r\n");
        }
        writer.flush();
        try {
            writer.close();
        } catch (IOException ignored) {}
        return file;
    }

    /**
     * 读取日志过滤列表
     */
    static List<String> readTagFilter(Context context) throws IOException {
        List<String> tagFilter = new ArrayList<>();
        File file = new File(context.getExternalFilesDir(FILE_TYPE), LOGCAT_TAG_FILTER_FILE);
        if (!file.exists() || !file.isFile()) {
            return tagFilter;
        }

        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), CHARSET_UTF_8));
        String tag;
        while ((tag = reader.readLine()) != null) {
            if (tag.isEmpty()) {
                continue;
            }
            if (tagFilter.contains(tag)) {
                continue;
            }
            tagFilter.add(tag);
        }
        try {
            reader.close();
        } catch (IOException ignored) {
            // default implementation ignored
        }

        return tagFilter;
    }

    static File writeTagFilter(Context context, List<String> tagFilter) throws IOException {
        File directory = context.getExternalFilesDir(FILE_TYPE);
        if (directory == null) {
            throw new IOException("The file does not exist.");
        }
        File file = new File(directory, LOGCAT_TAG_FILTER_FILE);
        if (tagFilter == null || tagFilter.isEmpty()) {
            return file;
        }
        if (!file.isFile()) {
            file.delete();
        }
        if (!file.exists()) {
            file.createNewFile();
        }
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, false), CHARSET_UTF_8));
        for (String temp : tagFilter) {
            writer.write(temp + "\r\n");
        }
        writer.flush();

        try {
            writer.close();
        } catch (IOException ignored) {}

        return file;
    }

    /**
     * 计算字符串 md5 哈希值
     */
    public static String computeMD5Hash(String string) {
        byte[] hash;

        try {
            hash = MessageDigest.getInstance("MD5").digest(string.getBytes(CHARSET_UTF_8));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }

        StringBuilder hex = new StringBuilder(hash.length * 2);
        for (byte b : hash) {
            if ((b & 0xFF) < 0x10)
                hex.append("0");
            hex.append(Integer.toHexString(b & 0xFF));
        }

        return hex.toString();
    }
}