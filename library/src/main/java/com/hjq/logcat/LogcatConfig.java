package com.hjq.logcat;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;

import java.util.Locale;

/**
 *    author : Android 轮子哥
 *    github : https://github.com/getActivity/Logcat
 *    time   : 2020/01/24
 *    desc   : 日志配置
 */
final class LogcatConfig {

    @SuppressLint("StaticFieldLeak")
    private static Context sApplicationContext;
    private static SharedPreferences sConfig;

    /**
     * 初始化
     */
    static void init(Context context) {
        sApplicationContext = context.getApplicationContext();
        sConfig = sApplicationContext.getSharedPreferences(
                LogcatContract.SP_FILE_NAME, Context.MODE_PRIVATE);
    }

    static String getLogLevelConfig() {
        String defaultLogLevel = LogcatUtils.getMetaStringData(sApplicationContext,
                LogcatContract.META_DATA_LOGCAT_DEFAULT_SEARCH_LEVEL);
        if (defaultLogLevel != null && !"".equals(defaultLogLevel)) {
            defaultLogLevel = defaultLogLevel.toUpperCase(Locale.ROOT);
        }
        String logLevel = sConfig.getString(LogcatContract.SP_KEY_LOGCAT_LOG_LEVEL, defaultLogLevel);
        if (logLevel == null || "".equals(logLevel)) {
            logLevel = LogLevel.VERBOSE;
        }
        return logLevel;
    }

    static void setLogLevelConfig(String logLevel) {
        sConfig.edit().putString(LogcatContract.SP_KEY_LOGCAT_LOG_LEVEL, logLevel).apply();
    }

    static String getSearchKeyConfig() {
        String defaultSearchKey = LogcatUtils.getMetaStringData(sApplicationContext,
                LogcatContract.META_DATA_LOGCAT_DEFAULT_SEARCH_KEY);
        return sConfig.getString(LogcatContract.SP_KEY_LOGCAT_SEARCH_KEY, defaultSearchKey);
    }

    static void setSearchKeyConfig(String searchKey) {
        sConfig.edit().putString(LogcatContract.SP_KEY_LOGCAT_SEARCH_KEY, searchKey).apply();
    }
}