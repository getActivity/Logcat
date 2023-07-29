package com.hjq.logcat;

import android.content.Context;
import android.support.annotation.Nullable;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *    author : Android 轮子哥
 *    github : https://github.com/getActivity/Logcat
 *    time   : 2020/01/24
 *    desc   : 日志信息
 */
final class LogcatInfo {

    static final String SPACE = "  ";

    static final String LINE_SPACE = "\n" + SPACE;

    /**
     * 日志分隔符匹配正则（空格数量不定，范围在 1 个到 4 个，应该是用了制表符）
     *
     * 04-23 14:45:30.003  1000   628   635 I QISL    : QSEE Interrupt Service Listener Thread is started
     * 04-23 14:45:30.086  root   651   651 W vold    : Failed to LOOP_GET_STATUS64 /dev/block/loop15: No such device or address
     * 11-25 11:17:58.233 10157  6666  6734 D EGL_emulation: app_time_stats: avg=100.62ms min=2.53ms max=693.42ms count=11
     * 11-30 22:41:51.454        8914  8914 I hjq.logcat.dem: Late-enabling -Xcheck:jni
     * 11-30 22:43:05.809 10157  8023  8232 D libEGL  : loaded /vendor/lib/egl/libEGL_emulation.so
     * 11-30 22:45:55.382 10173 12798 12798 W hjq.logcat.demo: Unexpected CPU variant for x86: x86_64.
     */
    private static final String LOG_SEPARATOR = "\\s{1,4}";

    /**
     * 日志时间匹配正则
     *
     * 案例：05-19 23:59:18.383
     */
    private static final String LOG_TIME = "([0-9^-]+-[0-9^ ]+\\s[0-9^:]+:[0-9^:]+\\.[0-9]+)";

    /**
     * 应用 id 匹配正则
     *
     * 案例：(10468/root/空格)
     */
    private static final String LOG_UID = "(.*)";

    /**
     * 进程 id 匹配正则
     *
     * 案例：3177
     */
    private static final String LOG_PID = "([0-9]+)";

    /**
     * 线程 id 匹配正则
     *
     * 案例：3258
     */
    private static final String LOG_TID = "([0-9]+)";

    /**
     * 日志等级匹配正则
     *
     * 案例：V/D/I/W/E/F
     */
    private static final String LOG_LEVEL = "([VDIWEF])";

    /**
     * 日志 TAG 匹配正则
     */
    private static final String LOG_TAG = "([^:]*):";

    /**
     * 日志内容匹配正则
     */
    private static final String LOG_CONTENT = "(.*)";

    /** 日志文本匹配正则表达式（默认格式） */
    private static final Pattern LOG_PATTERN_DEFAULT = Pattern.compile(LOG_TIME +
            LOG_SEPARATOR + LOG_PID + LOG_SEPARATOR + LOG_TID + LOG_SEPARATOR +
            LOG_LEVEL + LOG_SEPARATOR + LOG_TAG + LOG_SEPARATOR + LOG_CONTENT);

    /** 日志文本匹配正则表达式（附加程序 id） */
    private static final Pattern LOG_PATTERN_ADD_UID = Pattern.compile(LOG_TIME + LOG_SEPARATOR + LOG_UID +
            LOG_SEPARATOR + LOG_PID + LOG_SEPARATOR + LOG_TID + LOG_SEPARATOR +
            LOG_LEVEL + LOG_SEPARATOR + LOG_TAG + LOG_SEPARATOR + LOG_CONTENT);

    static final ArrayList<String> IGNORED_LOG = new ArrayList<String>() {{
        add("--------- beginning of crash");
        add("--------- beginning of main");
        add("--------- beginning of system");
    }};

    /** 时间 */
    private String time;
    /**
     * 应用 id
     *
     * Android 11 获取的才能获取得到 uid
     * Android 10 及以下机型会获取一堆空格
     */
    @Nullable
    private String uid;
    /** 线程 id */
    private String tid;
    /** 进程 id */
    private String pid;
    /** 等级 */
    private String level;
    /** 标记 */
    private String tag;
    /** 内容 */
    private String content;

    static LogcatInfo create(String line, boolean obtainUid) {
        if (line == null) {
            return null;
        }

        Matcher matcher;
        if (obtainUid) {
            matcher = LOG_PATTERN_ADD_UID.matcher(line);
        } else {
            matcher = LOG_PATTERN_DEFAULT.matcher(line);
        }
        if (!matcher.find()) {
            return null;
        }

        LogcatInfo info = new LogcatInfo();
        info.time = matcher.group(1);
        if (obtainUid) {
            info.uid = matcher.group(2);
            info.pid = matcher.group(3);
            info.tid = matcher.group(4);
            info.level = matcher.group(5);
            info.tag = matcher.group(6);
            info.content = matcher.group(7);
        } else {
            info.pid = matcher.group(2);
            info.tid = matcher.group(3);
            info.level = matcher.group(4);
            info.tag = matcher.group(5);
            info.content = matcher.group(6);
        }
        return info;
    }

    private LogcatInfo() {}

    String getTime() {
        return time;
    }

    @Nullable
    String getUid() {
        return uid;
    }

    String getPid() {
        return pid;
    }

    String getTid() {
        return tid;
    }

    String getLevel() {
        return level;
    }

    String getTag() {
        return tag;
    }

    String getContent() {
        return content;
    }

    /**
     * 追加日志内容
     */
    void addLogContent(String text) {
        content = (content.startsWith(LINE_SPACE) ? "" : LINE_SPACE) + content + LINE_SPACE + text;
    }

    @Override
    public String toString() {
        return String.format("%s" + SPACE + "%s" + SPACE + "%s", time, tag, content);
    }

    public String toString(Context context) {
        if (!LogcatUtils.isPortrait(context)) {
            return toString();
        }

        String log = getContent();
        return String.format("%s" + LogcatInfo.SPACE + "%s" +
                (log.startsWith("\n") ? LogcatInfo.SPACE : "\n")
                + "%s", time, tag, log);
    }
}