package com.hjq.logcat;

import android.content.Context;

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

    private static final Pattern PATTERN = Pattern.compile(
            //(          05-19 23:59:18.383                 )    (   3177  ) (   3258  ) (     I    ) (   XLog  )    ( 我是日志内容 )
            //(             time regex                      )    (pid regex) (tid regex) ( log level) ( log tag )    ( log content )
            "([0-9^-]+-[0-9^ ]+\\s[0-9^:]+:[0-9^:]+\\.[0-9]+)\\s+([0-9]+)\\s+([0-9]+)\\s([VDIWEF])\\s([^:&&\\s]*):\\s(.*)");

    static final ArrayList<String> IGNORED_LOG = new ArrayList<String>() {{
        add("--------- beginning of crash");
        add("--------- beginning of main");
        add("--------- beginning of system");
    }};

    /** 时间 */
    private String time;
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

    static LogcatInfo create(String line) {
        Matcher matcher = PATTERN.matcher(line);
        if (!matcher.find()) {
            return null;
        }

        LogcatInfo info = new LogcatInfo();
        info.time = matcher.group(1);
        info.pid = matcher.group(2);
        info.tid = matcher.group(3);
        info.level = matcher.group(4);
        info.tag = matcher.group(5);
        info.content = matcher.group(6);
        return info;
    }

    private LogcatInfo() {}

    String getTime() {
        return time;
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
                + "%s", getTime(), getTag(), log);
    }
}