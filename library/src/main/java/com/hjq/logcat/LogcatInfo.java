package com.hjq.logcat;

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

    private static final Pattern PATTERN = Pattern.compile(
            "([0-9^-]+-[0-9^ ]+ [0-9^:]+:[0-9^:]+\\.[0-9]+) +([0-9]+) +([0-9]+) ([VDIWEF]) ([^ ]*) *: (.*)");

    static final ArrayList<String> IGNORED_LOG = new ArrayList<String>() {{
        add("--------- beginning of crash");
        add("--------- beginning of main");
        add("--------- beginning of system");
    }};

    /** 时间 */
    private String time;
    /** 等级 */
    private String level;
    /** 标记 */
    private String tag;
    /** 内容 */
    private String log;

    LogcatInfo(String line) {
        Matcher matcher = PATTERN.matcher(line);
        if (!matcher.find()) {
            throw new IllegalStateException("logcat pattern not match: " + line);
        }

        time = matcher.group(1);
        level = matcher.group(4);
        tag = matcher.group(5);
        log = matcher.group(6);
    }

    String getTime() {
        return time;
    }

    String getLevel() {
        return level;
    }

    String getTag() {
        return tag;
    }

    String getLog() {
        return log;
    }

    void addLog(String text) {
        log = (log.startsWith("\n\t\t\t\t") ? "" : "\n\t\t\t\t") + log + "\n\t\t\t\t" + text;
    }
}