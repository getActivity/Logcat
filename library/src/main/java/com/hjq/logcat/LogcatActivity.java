package com.hjq.logcat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.Adapter;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *    author : Android 轮子哥
 *    github : https://github.com/getActivity/Logcat
 *    time   : 2020/01/24
 *    desc   : Logcat 显示窗口
 */
public final class LogcatActivity extends AppCompatActivity
        implements TextWatcher, View.OnLongClickListener, View.OnClickListener,
        CompoundButton.OnCheckedChangeListener, LogcatManager.Callback,
        LogcatAdapter.OnItemLongClickListener, LogcatAdapter.OnItemClickListener {

    private static final String[] ARRAY_LOG_LEVEL = {"Verbose", "Debug", "Info", "Warn", "Error"};
    private static final String[] ARRAY_LOG_LEVEL_PORTRAIT = {"V", "D", "I", "W", "E"};

    private View mRootView;
    private View mBarView;
    private CheckBox mCheckBox;
    private View mSaveView;
    private ViewGroup mLevelLayout;
    private TextView mLevelView;
    private EditText mSearchView;
    private ImageView mIconView;
    private View mClearView;
    private View mHideView;
    private RecyclerView mRecyclerView;
    private View mDownView;

    private LinearLayoutManager mLinearLayoutManager;
    private LogcatAdapter mAdapter;

    private String mLogLevel = LogLevel.VERBOSE;

    /** 暂停输出日志标记 */
    private boolean mPauseLogFlag;

    /** Tag 过滤规则 */
    private final List<String> mTagFilter = new ArrayList<>();

    /** 搜索关键字 */
    private final List<String> mSearchKeyword = new ArrayList<>();

    /** 当前是否授予了读取日志权限 */
    private boolean mGrantedReadLogPermission;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.logcat_activity_logcat);

        mRootView = findViewById(R.id.ll_log_root);
        mBarView = findViewById(R.id.ll_log_bar);
        mCheckBox = findViewById(R.id.cb_log_switch);
        mSaveView = findViewById(R.id.iv_log_save);
        mLevelLayout = findViewById(R.id.ll_log_level);
        mLevelView = findViewById(R.id.tv_log_level);
        mSearchView = findViewById(R.id.et_log_search_input);
        mIconView = findViewById(R.id.iv_log_search_icon);
        mClearView = findViewById(R.id.iv_log_logcat_clear);
        mHideView = findViewById(R.id.iv_log_logcat_hide);
        mRecyclerView = findViewById(R.id.lv_log_logcat_list);
        mDownView = findViewById(R.id.ib_log_logcat_down);

        mAdapter = new LogcatAdapter(this);
        mAdapter.setOnItemClickListener(this);
        mAdapter.setOnItemLongClickListener(this);

        mLinearLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLinearLayoutManager);
        mRecyclerView.setAnimation(null);
        mCheckBox.setOnCheckedChangeListener(this);
        mSearchView.addTextChangedListener(this);

        mSaveView.setOnClickListener(this);
        mLevelLayout.setOnClickListener(this);
        mIconView.setOnClickListener(this);
        mClearView.setOnClickListener(this);
        mHideView.setOnClickListener(this);
        mDownView.setOnClickListener(this);

        mSaveView.setOnLongClickListener(this);
        mCheckBox.setOnLongClickListener(this);
        mLevelLayout.setOnLongClickListener(this);
        mClearView.setOnLongClickListener(this);
        mHideView.setOnLongClickListener(this);

        mGrantedReadLogPermission = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                checkSelfPermission(Manifest.permission.READ_LOGS) == PackageManager.PERMISSION_GRANTED;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R ||
                (mGrantedReadLogPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)) {
            LogcatManager.setCanObtainUid(true);
        }

        LogcatManager.setCallback(this);

        // 开始捕获
        LogcatManager.start();

        initLogFilter();
        initSearchCondition();
        refreshLayout();

        // mRecyclerView.setAdapter(mAdapter);
        long delayMillis;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            delayMillis = 500;
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            delayMillis = 800;
        } else if (Build.VERSION.SDK_INT >= VERSION_CODES.M) {
            delayMillis = 1000;
        } else {
            delayMillis = 1200;
        }
        mRecyclerView.postDelayed(new Runnable() {
            @Override
            public void run() {
                // 延迟设置适配器，这样可以提升性能，因为进入这个界面的时候，会频繁添加日志，如果这个时候适配器已经绑定
                // 那么就会引发 itemView 频繁测试和绘制，这样会很卡，并且还会出现 ANR 的情况
                mRecyclerView.setAdapter(mAdapter);
            }
        }, delayMillis);

        mRecyclerView.postDelayed(new Runnable() {
            @Override
            public void run() {
                mLinearLayoutManager.scrollToPosition(mAdapter.getItemCount() - 1);
            }
        }, 1200);
    }

    private void initSearchCondition() {
        String searchKey = LogcatConfig.getSearchKeyConfig();
        if (searchKey != null && !"".equals(searchKey)) {
            mSearchView.setText(searchKey);
        }

        String logLevel = LogcatConfig.getLogLevelConfig();
        setLogLevel(logLevel);
    }

    @Override
    public void onReceiveLog(LogcatInfo info) {
        // 如果当前没有授予读取日志权限，则只打印当前应用的日志，如果有权限，则显示所有日志
        if (!mGrantedReadLogPermission) {
            try {
                String uidString = info.getUid();
                if (uidString != null && !"".equals(uidString)) {
                    int uid = Integer.parseInt(uidString);
                    if (uid != android.os.Process.myUid()) {
                        // 这个日志必须是当前应用打印的
                        return;
                    }
                }
            } catch (NumberFormatException ignore) {
                // Android 10 及以下机型获取日志打印的 uid 会返回几个空格
                // java.lang.NumberFormatException: For input string: "    "
                // 切勿在此处打印任何异常或者日志，避免造成无限递归
            }
        }

        // 这个 Tag 必须不在过滤列表中
        if (mTagFilter.contains(info.getTag())) {
            return;
        }

        Adapter<?> adapter = mRecyclerView.getAdapter();
        if (adapter == null) {
            // 避免频繁向主线程发送消息，如果日志过多会导致 ANR 出现
            mAdapter.addItem(info);
        } else {
            mRecyclerView.post(new LogRunnable(info));
        }
    }

    @Override
    public boolean onLongClick(View v) {
        if (v == mCheckBox) {
            LogcatUtils.toast(this, R.string.logcat_capture);
        } else if (v == mSaveView) {
            LogcatUtils.toast(this, R.string.logcat_save);
        } else if (v == mLevelView) {
            LogcatUtils.toast(this, R.string.logcat_level);
        } else if (v == mClearView) {
            LogcatUtils.toast(this, R.string.logcat_empty);
        } else if (v == mHideView) {
            LogcatUtils.toast(this, R.string.logcat_close);
        }
        return true;
    }

    @Override
    public void onClick(View v) {
        if (v == mSaveView) {
            try {
                File file = LogcatUtils.saveLogToFile(this, mAdapter.getShowData());
                LogcatUtils.toast(this, getResources().getString(R.string.logcat_save_succeed) + file.getPath());
            } catch (IOException e) {
                e.printStackTrace();
                LogcatUtils.toast(this, getResources().getString(R.string.logcat_save_fail));
            }
        } else if (v == mLevelLayout) {
            new ChooseWindow(this)
                    .setList(ARRAY_LOG_LEVEL)
                    .setListener(new ChooseWindow.OnListener() {
                        @Override
                        public void onSelected(int position) {
                            switch (position) {
                                case 0:
                                    setLogLevel(LogLevel.VERBOSE);
                                    break;
                                case 1:
                                    setLogLevel(LogLevel.DEBUG);
                                    break;
                                case 2:
                                    setLogLevel(LogLevel.INFO);
                                    break;
                                case 3:
                                    setLogLevel(LogLevel.WARN);
                                    break;
                                case 4:
                                    setLogLevel(LogLevel.ERROR);
                                    break;
                                default:
                                    break;
                            }
                        }
                    })
                    .show();
        } else if (v == mIconView) {
            String keyword = mSearchView.getText().toString();
            if ("".equals(keyword)) {
                showSearchKeyword();
            } else {
                mSearchView.setText("");
            }
        } else if (v == mClearView) {
            LogcatManager.clear();
            mAdapter.clearData();
        } else if (v == mHideView) {
            onBackPressed();
        } else if (v == mDownView) {
            // 滚动到列表最底部
            mLinearLayoutManager.scrollToPosition(mAdapter.getItemCount() - 1);
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (isChecked) {
            LogcatUtils.toast(this, R.string.logcat_capture_pause);
            LogcatManager.pause();
            mPauseLogFlag = true;
        } else {
            LogcatManager.resume();
            mPauseLogFlag = false;
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {}

    @Override
    public void afterTextChanged(Editable s) {
        mSearchView.removeCallbacks(mSearchRunnable);
        mSearchView.postDelayed(mSearchRunnable, 500);

        mSearchView.removeCallbacks(mSearchKeywordRunnable);
        mSearchView.postDelayed(mSearchKeywordRunnable, 3000);
    }

    @Override
    public void onItemClick(LogcatInfo info, int position) {
        mAdapter.onItemClick(position);
    }

    @Override
    public boolean onItemLongClick(LogcatInfo info, final int position) {
        new ChooseWindow(this)
                .setList(R.string.logcat_options_copy, R.string.logcat_options_share, R.string.logcat_options_delete, R.string.logcat_options_shield)
                .setListener(new ChooseWindow.OnListener() {
                    @Override
                    public void onSelected(final int location) {
                        switch (location) {
                            case 0:
                                copyLog(position);
                                break;
                            case 1:
                                shareLog(position);
                                break;
                            case 2:
                                mAdapter.removeItem(position);
                                break;
                            case 3:
                                addFilter(mAdapter.getItem(position).getTag());
                                break;
                            default:
                                break;
                        }
                    }
                })
                .show();
        return true;
    }

    private void copyLog(int position) {
        ClipboardManager manager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        if (manager != null) {
            manager.setPrimaryClip(ClipData.newPlainText("log", mAdapter.getItem(position).getContent()));
            LogcatUtils.toast(LogcatActivity.this, R.string.logcat_copy_succeed);
        } else {
            LogcatUtils.toast(LogcatActivity.this, R.string.logcat_copy_fail);
        }
    }

    private void shareLog(int position) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, mAdapter.getItem(position).getContent());
        startActivity(Intent.createChooser(intent, getResources().getString(R.string.logcat_options_share)));
    }

    private void setLogLevel(String level) {
        if (level.equalsIgnoreCase(mLogLevel)) {
            refreshLogLevelLayout();
            return;
        }

        mLogLevel = level;
        mAdapter.setLogLevel(level);
        LogcatConfig.setLogLevelConfig(level);
        afterTextChanged(mSearchView.getText());
        refreshLogLevelLayout();
    }

    private void refreshLogLevelLayout() {
        String[] arrayLogLevel;
        if (LogcatUtils.isPortrait(this)) {
            arrayLogLevel = ARRAY_LOG_LEVEL_PORTRAIT;
        } else {
            arrayLogLevel = ARRAY_LOG_LEVEL;
        }
        switch (mLogLevel) {
            case LogLevel.VERBOSE:
                mLevelView.setText(arrayLogLevel[0]);
                break;
            case LogLevel.DEBUG:
                mLevelView.setText(arrayLogLevel[1]);
                break;
            case LogLevel.INFO:
                mLevelView.setText(arrayLogLevel[2]);
                break;
            case LogLevel.WARN:
                mLevelView.setText(arrayLogLevel[3]);
                break;
            case LogLevel.ERROR:
                mLevelView.setText(arrayLogLevel[4]);
                break;
            default:
                break;
        }
    }

    private class LogRunnable implements Runnable {

        private final LogcatInfo info;

        private LogRunnable(LogcatInfo info) {
            this.info = info;
        }

        @Override
        public void run() {
            mAdapter.addItem(info);
        }
    }

    /**
     * 初始化 Tag 过滤器
     */
    private void initLogFilter() {
        try {
            mTagFilter.addAll(LogcatUtils.readTagFilter(this));
        } catch (IOException e) {
            e.printStackTrace();
            LogcatUtils.toast(this, R.string.logcat_read_config_fail);
        }

        String[] list = getResources().getStringArray(R.array.logcat_filter_list);
        for (String tag : list) {
            if (tag == null || "".equals(tag)) {
                continue;
            }
            if (mTagFilter.contains(tag)) {
                continue;
            }
            mTagFilter.add(tag);
        }
    }

    /**
     * 添加过滤的 TAG
     */
    private void addFilter(String tag) {
        if ("".equals(tag)) {
            return;
        }
        if (mTagFilter.contains(tag)) {
            return;
        }
        mTagFilter.add(tag);

        try {
            List<String> newTagFilter = new ArrayList<>(mTagFilter);
            newTagFilter.removeAll(Arrays.asList(getResources().getStringArray(R.array.logcat_filter_list)));

            File file = LogcatUtils.writeTagFilter(this, newTagFilter);
            LogcatUtils.toast(this, getResources().getString(R.string.logcat_shield_succeed) + file.getPath());

            // 从列表中删除关于这个 Tag 的日志
            List<LogcatInfo> data = mAdapter.getShowData();
            for (int i = 0; i < data.size(); i++) {
                LogcatInfo info = data.get(i);
                if (info.getTag().equals(tag)) {
                    mAdapter.removeItem(i);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
            LogcatUtils.toast(this, R.string.logcat_shield_fail);
        }
    }

    @Override
    public void onBackPressed() {
        // 清除输入焦点
        mSearchView.clearFocus();
        // 移动到上一个任务栈
        moveTaskToBack(false);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mPauseLogFlag) {
            return;
        }
        LogcatManager.resume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mPauseLogFlag) {
            return;
        }
        LogcatManager.pause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LogcatManager.destroy();
        mSearchView.removeCallbacks(mSearchRunnable);
        mSearchView.removeCallbacks(mSearchKeywordRunnable);
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        refreshLogLevelLayout();
        if (mAdapter == null) {
            return;
        }
        mAdapter.notifyDataSetChanged();
    }

    private void showSearchKeyword() {
        if (mSearchKeyword.isEmpty()) {
            return;
        }
        new ChooseWindow(this)
                .setList(mSearchKeyword)
                .setListener(new ChooseWindow.OnListener() {
                    @Override
                    public void onSelected(final int position) {
                        mSearchView.setText(mSearchKeyword.get(position));
                        mSearchView.setSelection(mSearchView.getText().toString().length());
                    }
                })
                .show();
    }

    private void refreshLayout() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            // 沉浸式状态栏只有 Android 4.4 才有的
            return;
        }

        Window window = getWindow();
        if (window == null) {
            return;
        }

        // 沉浸式状态栏
        window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

        mBarView.setPadding(0, 0, 0, 0);
        mRootView.setPadding(0, 0, 0, 0);

        if (LogcatUtils.isPortrait(this)) {
            // 在竖屏的状态下显示状态栏和导航栏
            window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                // 实现状态栏图标和文字颜色为亮色
                window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                WindowManager.LayoutParams params = window.getAttributes();
                // 会让屏幕到延伸刘海区域中
                params.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
                window.setAttributes(params);
            }

            mBarView.setPadding(0, LogcatUtils.getStatusBarHeight(this), 0, 0);
            return;
        }

        // 在横屏的状态下隐藏状态栏和导航栏
        window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        mBarView.setPadding((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, getResources().getDisplayMetrics()), 0,
            (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, getResources().getDisplayMetrics()), 0);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            WindowManager.LayoutParams params = window.getAttributes();
            // 不让屏幕内容到延伸刘海区域中，会留出一片黑色区域
            params.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_NEVER;
            window.setAttributes(params);
            return;
        }

        if (LogcatUtils.isActivityReverse(this)) {
            mRootView.setPadding(0, 0, LogcatUtils.getStatusBarHeight(this), 0);
        } else {
            mRootView.setPadding(LogcatUtils.getStatusBarHeight(this), 0, 0, 0);
        }
    }

    /**
     * 搜索关键字任务
     */
    private final Runnable mSearchRunnable = new Runnable() {
        @Override
        public void run() {
            String keyword = mSearchView.getText().toString();
            LogcatConfig.setSearchKeyConfig(keyword);
            mAdapter.setKeyword(keyword);
            mLinearLayoutManager.scrollToPosition(mAdapter.getItemCount() - 1);

            if (!"".equals(keyword)) {
                mIconView.setVisibility(View.VISIBLE);
                mIconView.setImageResource(R.drawable.logcat_ic_empty);
                return;
            }

            if (!mSearchKeyword.isEmpty()) {
                mIconView.setVisibility(View.VISIBLE);
                mIconView.setImageResource(R.drawable.logcat_ic_history);
            } else {
                mIconView.setVisibility(View.GONE);
            }
        }
    };

    /**
     * 搜索关键字记录任务
     */
    private final Runnable mSearchKeywordRunnable = new Runnable() {

        @Override
        public void run() {
            String keyword = mSearchView.getText().toString();
            if ("".equals(keyword)) {
                return;
            }
            if (mSearchKeyword.contains(keyword)) {
                return;
            }
            mSearchKeyword.add(0, keyword);
        }
    };
}