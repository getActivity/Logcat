package com.hjq.logcat;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.hjq.xtoast.XToast;

import java.util.ArrayList;
import java.util.List;

/**
 * author : Android 轮子哥
 * github : https://github.com/getActivity/Logcat
 * time   : 2020/01/24
 * desc   : Logcat 显示窗口
 */
public final class LogcatActivity extends Activity
        implements View.OnLongClickListener, View.OnClickListener,
        CompoundButton.OnCheckedChangeListener, LogcatManager.Listener,
        AdapterView.OnItemLongClickListener, AdapterView.OnItemClickListener, TextWatcher {

    private final static String[] ARRAY_LOG_LEVEL = {"Verbose", "Debug", "Info", "Warn", "Error"};

    private List<LogcatInfo> mLogData = new ArrayList<>();

    private CheckBox mSwitchView;
    private TextView mLevelView;
    private EditText mSearchView;
    private View mCleanView;
    private View mCloseView;
    private ListView mListView;

    private LogcatAdapter mAdapter;

    private String mLogLevel = "V";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 设置全屏显示
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.logcat_window_logcat);

        mSwitchView = findViewById(R.id.iv_log_switch);
        mLevelView = findViewById(R.id.tv_log_level);
        mSearchView = findViewById(R.id.et_log_search);
        mCleanView = findViewById(R.id.iv_log_clean);
        mCloseView = findViewById(R.id.iv_log_close);
        mListView = findViewById(R.id.lv_log_list);

        mAdapter = new LogcatAdapter();
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(this);
        mListView.setOnItemLongClickListener(this);
        mSwitchView.setOnCheckedChangeListener(this);
        mSearchView.addTextChangedListener(this);

        mSearchView.setText(LogcatConfig.getLogcatText());
        setLogLevel(LogcatConfig.getLogcatLevel());

        mLevelView.setOnClickListener(this);
        mCleanView.setOnClickListener(this);
        mCloseView.setOnClickListener(this);

        mSwitchView.setOnLongClickListener(this);
        mLevelView.setOnLongClickListener(this);
        mCleanView.setOnLongClickListener(this);
        mCloseView.setOnLongClickListener(this);

        // 开始捕获
        LogcatManager.setListener(this);
        LogcatManager.start();

        mListView.postDelayed(new Runnable() {
            @Override
            public void run() {
                mListView.setSelection(mAdapter.getCount() - 1);
            }
        }, 1000);
    }

    @Override
    public void onReceiveLog(String line) {
        mListView.post(new LogRunnable(new LogcatInfo(line)));
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        mAdapter.onItemClick(position);
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, final int location, long id) {
        new ChooseWindow(this)
                .setList("复制日志", "分享日志")
                .setListener(new ChooseWindow.OnListener() {
                    @Override
                    public void onSelected(int position) {
                        switch (position) {
                            case 0:
                                ClipboardManager manager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                                if (manager != null) {
                                    manager.setPrimaryClip(ClipData.newPlainText("log", mAdapter.getItem(location).getLog()));
                                    toast("日志复制成功");
                                }
                                break;
                            case 1:
                                Intent intent = new Intent(Intent.ACTION_SEND);
                                intent.setType("text/plain");
                                intent.putExtra(Intent.EXTRA_TEXT, mAdapter.getItem(location).getLog());
                                startActivity(Intent.createChooser(intent, "分享文本"));
                                break;
                            default:
                                break;
                        }
                    }
                })
                .show();
        return true;
    }

    @Override
    public boolean onLongClick(View v) {
        if (v == mSwitchView) {
            toast("日志捕获开关");
        } else if (v == mLevelView) {
            toast("日志等级过滤");
        } else if (v == mCleanView) {
            toast("清空日志");
        } else if (v == mCloseView) {
            toast("关闭显示");
        }
        return true;
    }

    @Override
    public void onClick(View view) {
        if (view == mLevelView) {
            new ChooseWindow(this)
                    .setList(ARRAY_LOG_LEVEL)
                    .setListener(new ChooseWindow.OnListener() {
                        @Override
                        public void onSelected(int position) {
                            switch (position) {
                                case 0:
                                    setLogLevel("V");
                                    break;
                                case 1:
                                    setLogLevel("D");
                                    break;
                                case 2:
                                    setLogLevel("I");
                                    break;
                                case 3:
                                    setLogLevel("W");
                                    break;
                                case 4:
                                    setLogLevel("E");
                                    break;
                                default:
                                    break;
                            }
                        }
                    })
                    .show();
        } else if (view == mCleanView) {
            LogcatManager.clear();
            mAdapter.clearData();
        } else if (view == mCloseView) {
            onBackPressed();
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (isChecked) {
            toast("日志捕捉已暂停");
            LogcatManager.stop();
        } else {
            LogcatManager.start();
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        String keyword = s.toString().trim();
        LogcatConfig.setLogcatText(keyword);
        mAdapter.setKeyword(keyword);
        mAdapter.clearData();
        for (LogcatInfo info : mLogData) {
            if ("V".equals(mLogLevel) || info.getLevel().equals(mLogLevel)) {
                if (!"".equals(keyword)) {
                    if (info.getLog().contains(keyword) || info.getTag().contains(keyword)) {
                        mAdapter.addItem(info);
                    }
                } else {
                    mAdapter.addItem(info);
                }
            }
        }
        mListView.setSelection(mAdapter.getCount() - 1);
    }

    private void setLogLevel(String level) {
        if (!level.equals(mLogLevel)) {
            mLogLevel = level;
            LogcatConfig.setLogcatLevel(level);
            afterTextChanged(mSearchView.getText());
            switch (level) {
                case "V":
                    mLevelView.setText(ARRAY_LOG_LEVEL[0]);
                    break;
                case "D":
                    mLevelView.setText(ARRAY_LOG_LEVEL[1]);
                    break;
                case "I":
                    mLevelView.setText(ARRAY_LOG_LEVEL[2]);
                    break;
                case "W":
                    mLevelView.setText(ARRAY_LOG_LEVEL[3]);
                    break;
                case "E":
                    mLevelView.setText(ARRAY_LOG_LEVEL[4]);
                    break;
                default:
                    break;
            }
        }
    }

    private class LogRunnable implements Runnable {

        private LogcatInfo info;

        private LogRunnable(LogcatInfo info) {
            this.info = info;
        }

        @Override
        public void run() {
            if (mLogData.size() > 0) {
                LogcatInfo lastInfo = mLogData.get(mLogData.size() - 1);
                if (info.getLevel().equals(lastInfo.getLevel()) &&
                        info.getTag().equals(lastInfo.getTag())) {

                    lastInfo.addLog(info.getLog());
                    mAdapter.notifyDataSetChanged();
                    return;
                }
            }

            mLogData.add(info);

            String content = mSearchView.getText().toString();
            if ("".equals(content) && "V".equals(mLogLevel)) {
                mAdapter.addItem(info);
            } else {
                if (info.getLevel().equals(mLogLevel)) {
                    if (info.getLog().contains(content) || info.getTag().contains(content)) {
                        mAdapter.addItem(info);
                    }
                }
            }
        }
    }

    /**
     * 吐司提示
     */
    private void toast(CharSequence text) {
        new XToast(this)
                .setView(R.layout.logcat_window_toast)
                .setDuration(3000)
                .setGravity(Gravity.CENTER)
                .setAnimStyle(android.R.style.Animation_Toast)
                .setText(android.R.id.message, text)
                .show();
    }

    @Override
    public void onBackPressed() {
        // 移动到上一个任务栈
        moveTaskToBack(false);
    }
}