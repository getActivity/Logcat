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
            "([0-9^-]+-[0-9^ ]+ [0-9^:]+:[0-9^:]+\\.[0-9]+) +([0-9]+) +([0-9]+) ([VDIWEF]) ([^ ]*) *: (.*)");

    static final ArrayList<String> IGNORED_LOG = new ArrayList<String>() {{
        add("--------- beginning of crash");
        add("--------- beginning of main");
        add("--------- beginning of system");
    }};

    /** 时间 */
    private String mTime;
    /** 等级 */
    private String mLevel;
    /** 标记 */
    private String mTag;
    /** 内容 */
    private String mLog;
    /** 进程id */
    private String mPid;

    LogcatInfo(String line) {
        Matcher matcher = PATTERN.matcher(line);
        if (!matcher.find()) {
            throw new IllegalStateException("logcat pattern not match: " + line);
        }

        mTime = matcher.group(1);
        mPid = matcher.group(3);
        mLevel = matcher.group(4);
        mTag = matcher.group(5);
        mLog = matcher.group(6);
    }

    String getTime() {
        return mTime;
    }

    String getLevel() {
        return mLevel;
    }

    String getTag() {
        return mTag;
    }

    String getLog() {
        return mLog;
    }

    String getPid() {
        return mPid;
    }

    void addLog(String text) {
        mLog = (mLog.startsWith(LINE_SPACE) ? "" : LINE_SPACE) + mLog + LINE_SPACE + text;
    }

    @Override
    public String toString() {
        return String.format("%s   %s   %s", mTime, mTag, mLog);
    }
}