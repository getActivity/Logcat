package com.hjq.logcat;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import com.hjq.window.EasyWindow;
import com.hjq.window.OnWindowViewClickListener;
import com.hjq.window.draggable.SpringBackWindowDraggableRule;

/**
 *    author : Android 轮子哥
 *    github : https://github.com/getActivity/Logcat
 *    time   : 2020/01/24
 *    desc   : 悬浮窗口
 */
final class LogcatWindow extends EasyWindow<LogcatWindow> implements OnWindowViewClickListener<View> {

    public LogcatWindow(Application application) {
        super(application);
        init(application);
    }

    public LogcatWindow(Activity activity) {
        super(activity);
        init(activity);
    }

    private void init(Context context) {
        ImageView imageView = new ImageView(context.getApplicationContext());
        imageView.setId(android.R.id.icon);
        imageView.setImageResource(R.drawable.logcat_selector_floating);
        setContentView(imageView);

        int size = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 45, context.getResources().getDisplayMetrics());
        setWindowSize(size, size);

        setWindowAnim(android.R.style.Animation_Toast);
        setWindowDraggableRule(new SpringBackWindowDraggableRule());
        setWindowLocation(Gravity.END | Gravity.CENTER_VERTICAL, 0, 0);
        setOnClickListenerByView(android.R.id.icon, this);
    }

    @Override
    public void onClick(@NonNull EasyWindow<?> easyWindow, @NonNull View view) {
        Context context = getContext();
        Intent intent = new Intent(context, LogcatActivity.class);
        if (!(context instanceof Activity)) {
            // 如果当前的上下文不是 Activity，调用 startActivity 必须加入新任务栈的标记，否则会报错：android.util.AndroidRuntimeException
            // Calling startActivity() from outside of an Activity context requires the FLAG_ACTIVITY_NEW_TASK flag. Is this really what you want?
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        startActivity(intent);
    }
}