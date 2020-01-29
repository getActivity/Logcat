package com.hjq.logcat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 *    author : Android 轮子哥
 *    github : https://github.com/getActivity/Logcat
 *    time   : 2020/01/24
 *    desc   : 日志管理类
 *    doc    : https://developer.android.google.cn/studio/command-line/logcat
 */
final class LogcatManager {

    private static Listener sListener;
    private static boolean FLAG_WORK;

    static void setListener(Listener listener) {
        sListener = listener;
    }

    static void start() {
        if (!FLAG_WORK) {
            FLAG_WORK = true;

            new Thread(new Runnable() {
                @Override
                public void run() {
                    BufferedReader reader = null;
                    try {
                        Process process = new ProcessBuilder("logcat", "-v", "threadtime").start();
                        reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                        String line;
                        while (FLAG_WORK && (line = reader.readLine()) != null) {
                            if (LogcatInfo.IGNORED_LOG.contains(line)) {
                                continue;
                            }
                            try {
                                if (sListener != null) {
                                    sListener.onReceiveLog(line);
                                }
                            } catch (NumberFormatException | IllegalStateException e) {
                                e.printStackTrace();
                            }
                        }
                        stop();
                    } catch (IOException e) {
                        e.printStackTrace();
                        stop();
                    }
                    if (reader != null) {
                        try {
                            reader.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }).start();
        }
    }

    static void stop() {
        FLAG_WORK = false;
    }

    static void clear() {
        try {
            new ProcessBuilder("logcat", "-c").start();
            start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public interface Listener {
        void onReceiveLog(String line);
    }
}