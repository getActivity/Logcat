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
final class LogcatManager {

    private static volatile Listener sListener;
    /** 日志捕捉标记 */
    private static volatile boolean FLAG_WORK;
    /** 备用存放集合 */
    private static final List<LogcatInfo> LOG_BACKUP = new ArrayList<>();

    /**
     * 开始捕捉
     */
    static void start(Listener listener) {
        if (sListener == null) {
            FLAG_WORK = true;
            new Thread(new LogRunnable()).start();
        }
        sListener = listener;
    }

    /**
     * 继续捕捉
     */
    static void resume() {
        FLAG_WORK = true;
        if (sListener != null && !LOG_BACKUP.isEmpty()) {
            for (LogcatInfo info : LOG_BACKUP) {
                sListener.onReceiveLog(info);
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
                    if (LogcatInfo.IGNORED_LOG.contains(line)) {
                        continue;
                    }
                    if (FLAG_WORK) {
                        if (sListener != null) {
                            sListener.onReceiveLog(new LogcatInfo(line));
                        }
                    } else {
                        LOG_BACKUP.add(new LogcatInfo(line));
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

    public interface Listener {
        void onReceiveLog(LogcatInfo line);
    }
}