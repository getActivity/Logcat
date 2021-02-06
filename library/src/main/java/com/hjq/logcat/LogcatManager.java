package com.hjq.logcat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 *    author : Android 轮子哥
 *    github : https://github.com/getActivity/Logcat
 *    time   : 2020/01/24
 *    desc   : 日志管理类
 *    doc    : https://developer.android.google.cn/studio/command-line/logcat
 */
@SuppressWarnings("AlibabaAvoidManuallyCreateThread")
final class LogcatManager {

    /** 日志捕捉监听对象 */
    private static volatile Callback sCallback;
    /** 日志捕捉标记 */
    private static volatile boolean FLAG_WORK;
    /** 备用存放集合 */
    private static final List<LogcatInfo> LOG_BACKUP = new ArrayList<>();

    /**
     * 开始捕捉
     */
    static void start(Callback callback) {
        FLAG_WORK = true;
        new Thread(new LogRunnable()).start();
        sCallback = callback;
    }

    /**
     * 继续捕捉
     */
    static void resume() {
        FLAG_WORK = true;
        final Callback callback = sCallback;
        if (callback != null && !LOG_BACKUP.isEmpty()) {
            for (LogcatInfo info : LOG_BACKUP) {
                if (info == null) {
                    continue;
                }
                callback.onReceiveLog(info);
            }
        }
        LOG_BACKUP.clear();
    }

    /**
     * 暂停捕捉
     */
    static void pause() {
        FLAG_WORK = false;
    }

    /**
     * 停止捕捉
     */
    static void destroy() {
        FLAG_WORK = false;
        // 把监听对象置空，不然会导致内存泄漏
        sCallback = null;
    }

    /**
     * 清空日志
     */
    static void clear() {
        try {
            new ProcessBuilder("logcat", "-c").start();
            FLAG_WORK = true;
            new Thread(new LogRunnable()).start();
        } catch (IOException ignored) {}
    }

    private static class LogRunnable implements Runnable {

        @Override
        public void run() {
            BufferedReader reader = null;
            try {
                Process process = new ProcessBuilder("logcat", "-v", "threadtime").start();
                reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                String line;
                while ((line = reader.readLine()) != null) {
                    synchronized (LogcatManager.class) {
                        if (LogcatInfo.IGNORED_LOG.contains(line)) {
                            continue;
                        }
                        LogcatInfo info = LogcatInfo.create(line);
                        if (info == null) {
                            continue;
                        }
                        if (!FLAG_WORK) {
                            // 这里可能会出现下标异常
                            LOG_BACKUP.add(info);
                            continue;
                        }

                        final Callback callback = sCallback;
                        if (callback != null) {
                            callback.onReceiveLog(info);
                        }
                    }
                }
                pause();
            } catch (IOException ignored) {
                pause();
            } finally {
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException ignored) {}
                }
            }
        }
    }

    public interface Callback {

        /**
         * 收到日志
         */
        void onReceiveLog(LogcatInfo info);
    }
}