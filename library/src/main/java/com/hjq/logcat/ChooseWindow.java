package com.hjq.logcat;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ListView;
import com.hjq.window.EasyWindow;
import com.hjq.window.OnWindowViewClickListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *    author : Android 轮子哥
 *    github : https://github.com/getActivity/Logcat
 *    time   : 2020/01/24
 *    desc   : 列表选择类
 */
final class ChooseWindow extends EasyWindow<ChooseWindow> implements
        AdapterView.OnItemClickListener, OnWindowViewClickListener<View> {

    private final ChooseAdapter mAdapter;
    private OnListener mListener;

    @SuppressWarnings("deprecation")
    ChooseWindow(Activity activity) {
        super(activity);
        setContentView(R.layout.logcat_window_choose);
        setWindowLocation(Gravity.CENTER, 0, 0);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // 设置沉浸式状态栏
            addWindowFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
        removeWindowFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

        mAdapter = new ChooseAdapter();
        ListView listView = findViewById(R.id.lv_choose_list);
        if (listView != null) {
            listView.setAdapter(mAdapter);
            listView.setOnItemClickListener(this);
        }

        setOnClickListenerByView(R.id.fl_choose_root, this);
    }

    @Override
    public void onClick(@NonNull EasyWindow<?> easyWindow, @NonNull View view) {
        cancel();
    }

    ChooseWindow setList(int... stringIds) {
        Context context = getContext();
        if (context == null) {
            return this;
        }
        List<String> data = new ArrayList<>();
        for (int stringId : stringIds) {
            data.add(context.getResources().getString(stringId));
        }
        return setList(data);
    }

    ChooseWindow setList(String... data) {
        return setList(Arrays.asList(data));
    }

    ChooseWindow setList(List<String> data) {
        mAdapter.setData(data);
        return this;
    }

    ChooseWindow setListener(OnListener listener) {
        mListener = listener;
        return this;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (mListener != null) {
            mListener.onSelected(position);
        }
        cancel();
    }

    public interface OnListener {

        void onSelected(int position);
    }
}