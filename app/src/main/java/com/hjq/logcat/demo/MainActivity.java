package com.hjq.logcat.demo;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.hjq.bar.OnTitleBarListener;
import com.hjq.bar.TitleBar;
import com.hjq.toast.ToastUtils;

public class MainActivity extends AppCompatActivity implements OnTitleBarListener {

    private TitleBar mTitleBar;
    private WebView mWebView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTitleBar = findViewById(R.id.tb_main_title);
        mTitleBar.setOnTitleBarListener(this);

        mWebView = findViewById(R.id.wv_main_web);
        mWebView.setWebViewClient(new WebViewClient());
        mWebView.setWebChromeClient(new MyWebChromeClient());
        mWebView.loadUrl("https://github.com/getActivity/Logcat");

        if (NotificationManagerCompat.from(this).areNotificationsEnabled()) {
            if (!ToastUtils.isInit()) {
                ToastUtils.init(getApplication());
            }

            ToastUtils.show("请点击通知栏入口进入日志查看页面");
        }
    }

    private class MyWebChromeClient extends WebChromeClient {

        @Override
        public void onReceivedTitle(WebView view, String title) {
            mTitleBar.setTitle(title);
        }
    }

    @Override
    public void onTitleClick(TitleBar titleBar) {
        String url = mWebView.getUrl();
        if (url != null && !"".equals(url)) {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mWebView.onResume();
        mWebView.resumeTimers();
    }

    @Override
    public void onPause() {
        super.onPause();
        mWebView.onPause();
        mWebView.pauseTimers();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //清除历史记录
        mWebView.clearHistory();
        //停止加载
        mWebView.stopLoading();
        //加载一个空白页
        mWebView.loadUrl("about:blank");
        mWebView.setWebChromeClient(null);
        mWebView.setWebViewClient(null);
        //移除WebView所有的View对象
        mWebView.removeAllViews();
        //销毁此的WebView的内部状态
        mWebView.destroy();
        ((ViewGroup) mWebView.getParent()).removeView(mWebView);
    }
}