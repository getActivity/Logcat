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

    private static final String LINE_SPACE = "\n    ";

    private static final Pattern PATTERN = Pattern.compile(
            "([0-9^-]+-[0-9^ ]+\\s[0-9^:]+:[0-9^:]+\\.[0-9]+)\\s+([0-9]+)\\s+([0-9]+)\\s([VDIWEF])\\s([^\\s]*)\\s*:\\s(.*)");

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
    /** 进程id */
    private String pid;

    static LogcatInfo create(String line) {
        Matcher matcher = PATTERN.matcher(line);
        // 判断日志格式是否合法（目前发现华为手机有在日志的 TAG 中加空格导致识别不出来，这种无法做兼容）
        if (!matcher.find()) {
            return null;
        }

        LogcatInfo info = new LogcatInfo();
        info.time = matcher.group(1);
        info.pid = matcher.group(2);
        info.level = matcher.group(4);
        info.tag = matcher.group(5);
        info.log = matcher.group(6);
        return info;
    }

    private LogcatInfo() {}

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

    String getPid() {
        return pid;
    }

    void addLog(String text) {
        log = (log.startsWith(LINE_SPACE) ? "" : LINE_SPACE) + log + LINE_SPACE + text;
    }

    @Override
    public String toString() {
        return String.format("%s   %s   %s", time, tag, log);
    }
}