package com.hjq.logcat;

/**
 *    author : Android 轮子哥
 *    github : https://github.com/getActivity/Logcat
 *    time   : 2022/11/27
 *    desc   : Logcat 契约类
 */
final class LogcatContract {

   /** 通知栏入口 */
   static final String META_DATA_LOGCAT_NOTIFY_ENTRANCE = "LogcatNotifyEntrance";

   /** 悬浮窗入口 */
   static final String META_DATA_LOGCAT_WINDOW_ENTRANCE = "LogcatWindowEntrance";

    /** 自动合并打印日志（默认开启） */
    static final String META_DATA_LOGCAT_AUTO_MERGE_PRINT = "LogcatAutoMergePrint";

   /** 默认搜索关键字 */
   static final String META_DATA_LOGCAT_DEFAULT_SEARCH_KEY = "LogcatDefaultSearchKey";

   /** 默认日志等级 */
   static final String META_DATA_LOGCAT_DEFAULT_SEARCH_LEVEL = "LogcatDefaultLogLevel";

   /** SP 文件名 */
   static final String SP_FILE_NAME = "logcat";

   /** SP Key：日志过滤等级 */
   static final String SP_KEY_LOGCAT_LOG_LEVEL = "logcat_log_level";

   /** SP Key：搜索关键字 */
   static final String SP_KEY_LOGCAT_SEARCH_KEY = "logcat_search_key";
}