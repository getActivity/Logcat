package com.hjq.logcat;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.URLSpan;
import android.text.style.UnderlineSpan;
import android.util.SparseBooleanArray;
import android.util.SparseIntArray;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *    author : Android 轮子哥
 *    github : https://github.com/getActivity/Logcat
 *    time   : 2020/01/24
 *    desc   : 日志列表适配器
 */
@SuppressLint("NotifyDataSetChanged")
final class LogcatAdapter extends RecyclerView.Adapter<LogcatAdapter.ViewHolder> {

    /** 最大日志数量限制（避免日志过多导致出现 OOM） */
    private static final int LOG_MAX_COUNT = 1000;

    /** 达到阈值时删除的日志数量 */
    private static final int LOG_REMOVE_COUNT = LOG_MAX_COUNT / 3;

    /** 报错代码行数正则表达式 */
    private static final Pattern CODE_REGEX = Pattern.compile("\\(\\w+\\.\\w+:\\d+\\)");

    /** 链接正则表达式 */
    private static final Pattern LINK_REGEX = Pattern.compile("https?://[^\\x{4e00}-\\x{9fa5}\\n\\r\\s]{3,}");

    private final SparseBooleanArray mExpandSet = new SparseBooleanArray();
    private final SparseIntArray mScrollXSet = new SparseIntArray();

    private final List<LogcatInfo> mAllData = new CopyOnWriteArrayList<>();
    private List<LogcatInfo> mShowData = mAllData;

    private final Context mContext;

    private String mKeyword = "";
    private String mLogLevel = LogLevel.VERBOSE;

    private RecyclerView mRecyclerView;

    /** 条目点击监听器 */
    @Nullable
    private OnItemClickListener mItemClickListener;
    /** 条目长按监听器 */
    @Nullable
    private OnItemLongClickListener mItemLongClickListener;

    public LogcatAdapter(Context context) {
        mContext = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.logcat_item_logcat, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.onBindView(getItem(position), position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return mShowData.size();
    }

    public LogcatInfo getItem(int position) {
        return mShowData.get(position);
    }

    List<LogcatInfo> getData() {
        return mShowData;
    }

    /**
     * 添加单条数据
     */
    void addItem(LogcatInfo info) {
        if (!mShowData.isEmpty()) {
            LogcatInfo lastInfo = getItem(mShowData.size() - 1);
            if (info.getLevel().equals(lastInfo.getLevel()) &&
                    info.getTag().equals(lastInfo.getTag())) {
                // 追加日志
                lastInfo.addLogContent(info.getContent());
                notifyItemChanged(mShowData.size() - 1);
                return;
            }
        }

        if (isConform(info)) {
            mShowData.add(info);

            // 如果当前显示的日志数量超过限定的最大数量
            if (mShowData.size() > LOG_MAX_COUNT) {
                mShowData.removeAll(mShowData.subList(0, LOG_REMOVE_COUNT));
                // 更新所有条目，这样日志的条目索引文案才能更新
                notifyDataSetChanged();
                if (mRecyclerView != null) {
                    // 列表滚动到最后一条位置上面
                    mRecyclerView.scrollToPosition(getItemCount() - 1);
                }
            } else {
                notifyItemInserted(mShowData.size() - 1);
            }
        }

        if (mShowData != mAllData) {
            mAllData.add(info);

            // 如果所有的日志数量超过限定的最大数量
            if (mAllData.size() > LOG_MAX_COUNT) {
                mAllData.removeAll(mAllData.subList(0, LOG_REMOVE_COUNT));
            }
        }
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        mRecyclerView = recyclerView;
    }

    @Override
    public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        mRecyclerView = null;
    }

    /**
     * 删除某条数据
     */
    void removeItem(int position) {
        LogcatInfo info = mShowData.remove(position);
        if (mAllData != mShowData && info != null) {
            mAllData.remove(info);
        }
        notifyItemRemoved(position);
    }

    /**
     * 清空当前数据
     */
    void clearData() {
        mExpandSet.clear();
        mScrollXSet.clear();
        mAllData.clear();
        if (mAllData != mShowData) {
            mShowData.clear();
        }
        notifyDataSetChanged();
    }

    void setKeyword(String keyword) {
        mKeyword = keyword;
        filterData();
        notifyDataSetChanged();
    }

    void setLogLevel(String logLevel) {
        mLogLevel = logLevel;
        filterData();
        notifyDataSetChanged();
    }

    void onItemClick(int position) {
        LogcatInfo info = getItem(position);
        boolean expand = mExpandSet.get(info.hashCode());
        mExpandSet.put(info.hashCode(), !expand);
        notifyItemChanged(position);
    }

    private void filterData() {
        if (TextUtils.isEmpty(mKeyword) && LogLevel.VERBOSE.equals(mLogLevel)) {
            mShowData = mAllData;
            return;
        }

        if (mShowData == mAllData) {
            mShowData = new ArrayList<>();
        }
        mShowData.clear();

        for (LogcatInfo info : mAllData) {
            if (isConform(info)) {
                mShowData.add(info);
            }
        }
    }

    private boolean isConform(LogcatInfo info) {
        return (TextUtils.isEmpty(mKeyword) || info.toString(mContext).contains(mKeyword)) &&
                (LogLevel.VERBOSE.equals(mLogLevel) || info.getLevel().equals(mLogLevel));
    }

    /**
     * 设置 RecyclerView 条目点击监听
     */
    void setOnItemClickListener(@Nullable OnItemClickListener listener) {
        mItemClickListener = listener;
    }

    /**
     * 设置 RecyclerView 条目长按监听
     */
    void setOnItemLongClickListener(@Nullable OnItemLongClickListener listener) {
        mItemLongClickListener = listener;
    }

    @Override
    public void onViewDetachedFromWindow(ViewHolder holder) {
        super.onViewDetachedFromWindow(holder);
        holder.onDetached();
    }

    @Override
    public void onViewRecycled(@NonNull LogcatAdapter.ViewHolder holder) {
        super.onViewRecycled(holder);
        holder.onRecycled();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements
            View.OnTouchListener, View.OnClickListener, View.OnLongClickListener {

        private final HorizontalScrollView mHorizontalScrollView;
        private final TextView mContentView;
        private final TextView mIndexView;
        private final View mLineView;

        @SuppressLint("ClickableViewAccessibility")
        public ViewHolder(View itemView) {
            super(itemView);

            mHorizontalScrollView = itemView.findViewById(R.id.hcv_log_content);
            mContentView = itemView.findViewById(R.id.tv_log_content);
            mIndexView = itemView.findViewById(R.id.tv_log_index);
            mLineView = itemView.findViewById(R.id.v_log_line);

            mHorizontalScrollView.setOnClickListener(this);
            mHorizontalScrollView.setOnLongClickListener(this);

            mHorizontalScrollView.setOnTouchListener(this);
        }

        @Override
        public void onClick(View v) {
            if (mItemClickListener == null) {
                return;
            }
            int layoutPosition = getLayoutPosition();
            mItemClickListener.onItemClick(getItem(layoutPosition), layoutPosition);
        }

        @Override
        public boolean onLongClick(View v) {
            if (mItemLongClickListener == null) {
                return false;
            }
            int layoutPosition = getLayoutPosition();
            return mItemLongClickListener.onItemLongClick(getItem(layoutPosition), layoutPosition);
        }

        /** 手指按下的坐标 */
        private float mViewDownX;
        private float mViewDownY;

        /** 手指按下的时间 */
        private long mDownTime;

        /** 触摸移动标记 */
        private boolean mMoveTouch;

        /**
         * View.OnTouchListener
         */
        @SuppressLint("ClickableViewAccessibility")
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    // 记录按下的位置（相对 View 的坐标）
                    mViewDownX = event.getX();
                    mViewDownY = event.getY();
                    mDownTime = System.currentTimeMillis();
                    mMoveTouch = false;
                    break;
                case MotionEvent.ACTION_MOVE:
                    if (!mMoveTouch && isTouchMove(mViewDownX, event.getX(), mViewDownY, event.getY())) {
                        // 如果用户移动了手指，那么就拦截本次触摸事件，从而不让点击事件生效
                        mMoveTouch = true;
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    // java.lang.IndexOutOfBoundsException: Index: 813, Size: 801
                    int layoutPosition = getLayoutPosition();
                    if (layoutPosition <= getItemCount()) {
                        LogcatInfo info = getItem(layoutPosition);
                        int scrollX = mHorizontalScrollView.getScrollX();
                        mScrollXSet.put(info.hashCode(), scrollX);
                    }
                    if (!mMoveTouch) {
                        if (System.currentTimeMillis() - mDownTime > 200) {
                            v.performLongClick();
                        } else {
                            v.performClick();
                        }
                    }
                    break;
                default:
                    break;
            }
            return false;
        }

        /**
         * 判断用户是否移动了，判断标准以下：
         * 根据手指按下和抬起时的坐标进行判断，不能根据有没有 move 事件来判断
         * 因为在有些机型上面，就算用户没有手指没有移动也会产生 move 事件
         *
         * @param downX         手指按下时的 x 坐标
         * @param upX           手指抬起时的 x 坐标
         * @param downY         手指按下时的 y 坐标
         * @param upY           手指抬起时的 y 坐标
         */
        protected boolean isTouchMove(float downX, float upX, float downY, float upY) {
            float minTouchSlop = getScaledTouchSlop();
            return Math.abs(downX - upX) >= minTouchSlop || Math.abs(downY - upY) >= minTouchSlop;
        }

        /**
         * 获取最小触摸距离
         */
        protected float getScaledTouchSlop() {
            return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 3, Resources.getSystem().getDisplayMetrics());
        }

        private void onBindView(LogcatInfo info, int position) {
            String content = info.toString(mContext);
            // suspend all histogram:	Sum: 45.480ms 99% C.I. 0.342us-1624.319us Avg: 196.883us Max: 1880us
            //DALVIK THREADS (32):
            //"main" prio=5 tid=1 Runnable
            //  | group="main" sCount=0 dsCount=0 flags=0 obj=0x73abd508 self=0x7bf15b0be0
            //  | sysTid=5900 nice=0 cgrp=default sched=0/0 handle=0x7d790bf500
            //  | state=R schedstat=( 38957797984 690538446 16911 ) utm=3558 stm=337 core=6 HZ=100
            //  | stack=0x7fe263a000-0x7fe263c000 stackSize=8192KB
            //  | held mutexes= "mutator lock"(shared held)
            //  at android.text.SpannableStringInternal.getSpans(SpannableStringInternal.java:348)
            //  at android.text.SpannedString.getSpans(SpannedString.java:25)
            //  at android.text.SpanSet.init(SpanSet.java:50)
            //  at android.text.TextLine.set(TextLine.java:192)
            //  at android.text.Layout.getLineExtent(Layout.java:1448)
            //  at android.text.Layout.getLineMax(Layout.java:1401)
            //  at android.widget.TextView.desired(TextView.java:10320)
            //  at android.widget.TextView.onMeasure(TextView.java:10392)
            //  at android.view.View.measure(View.java:27131)
            //  at android.widget.HorizontalScrollView.measureChildWithMargins(HorizontalScrollView.java:1841)
            //  at android.widget.FrameLayout.onMeasure(FrameLayout.java:194)
            //  at android.widget.HorizontalScrollView.onMeasure(HorizontalScrollView.java:586)
            //  at android.view.View.measure(View.java:27131)
            //  at android.widget.FrameLayout.onMeasure(FrameLayout.java:263)
            //  at android.view.View.measure(View.java:27131)
            //  at android.support.v7.widget.RecyclerView$LayoutManager.measureChildWithMargins(RecyclerView.java:8760)
            //  at android.support.v7.widget.LinearLayoutManager.layoutChunk(LinearLayoutManager.java:1582)
            //  at android.support.v7.widget.LinearLayoutManager.fill(LinearLayoutManager.java:1516)
            //  at android.support.v7.widget.LinearLayoutManager.scrollBy(LinearLayoutManager.java:1330)
            //  at android.support.v7.widget.LinearLayoutManager.scrollVerticallyBy(LinearLayoutManager.java:1074)
            //  at android.support.v7.widget.RecyclerView.scrollByInternal(RecyclerView.java:1759)
            //  at android.support.v7.widget.RecyclerView.onTouchEvent(RecyclerView.java:2971)
            //  at android.view.View.dispatchTouchEvent(View.java:15199)
            //  at android.view.ViewGroup.dispatchTransformedTouchEvent(ViewGroup.java:3914)
            //  at android.view.ViewGroup.dispatchTouchEvent(ViewGroup.java:3578)
            //  at android.view.ViewGroup.dispatchTransformedTouchEvent(ViewGroup.java:3920)
            //  at android.view.ViewGroup.dispatchTouchEvent(ViewGroup.java:3594)
            //  at android.view.ViewGroup.dispatchTransformedTouchEvent(ViewGroup.java:3920)
            //  at android.view.ViewGroup.dispatchTouchEvent(ViewGroup.java:3594)
            //  at android.view.ViewGroup.dispatchTransformedTouchEvent(ViewGroup.java:3920)
            //  at android.view.ViewGroup.dispatchTouchEvent(ViewGroup.java:3594)
            //  at android.view.ViewGroup.dispatchTransformedTouchEvent(ViewGroup.java:3920)
            //  at android.view.ViewGroup.dispatchTouchEvent(ViewGroup.java:3594)
            //  at android.view.ViewGroup.dispatchTransformedTouchEvent(ViewGroup.java:3920)
            //  at android.view.ViewGroup.dispatchTouchEvent(ViewGroup.java:3594)
            //  at android.view.ViewGroup.dispatchTransformedTouchEvent(ViewGroup.java:3920)
            //  at android.view.ViewGroup.dispatchTouchEvent(ViewGroup.java:3594)
            //  at com.android.internal.policy.DecorView.superDispatchTouchEvent(DecorView.java:913)
            //  at com.android.internal.policy.PhoneWindow.superDispatchTouchEvent(PhoneWindow.java:1957)
            //  at android.app.Activity.dispatchTouchEvent(Activity.java:4182)
            //  at android.support.v7.view.WindowCallbackWrapper.dispatchTouchEvent(WindowCallbackWrapper.java:68)
            //  at com.android.internal.policy.DecorView.dispatchTouchEvent(DecorView.java:871)
            //  at android.view.View.dispatchPointerEvent(View.java:15458)
            //  at android.view.ViewRootImpl$ViewPostImeInputStage.processPointerEvent(ViewRootImpl.java:7457)
            //  at android.view.ViewRootImpl$ViewPostImeInputStage.onProcess(ViewRootImpl.java:7233)
            //  at android.view.ViewRootImpl$InputStage.deliver(ViewRootImpl.java:6595)
            //  at android.view.ViewRootImpl$InputStage.onDeliverToNext(ViewRootImpl.java:6652)
            //  at android.view.ViewRootImpl$InputStage.forward(ViewRootImpl.java:6618)
            //  at android.view.ViewRootImpl$AsyncInputStage.forward(ViewRootImpl.java:6786)
            //  at android.view.ViewRootImpl$InputStage.apply(ViewRootImpl.java:6626)
            //  at android.view.ViewRootImpl$AsyncInputStage.apply(ViewRootImpl.java:6843)
            //  at android.view.ViewRootImpl$InputStage.deliver(ViewRootImpl.java:6599)
            //  at android.view.ViewRootImpl$InputStage.onDeliverToNext(ViewRootImpl.java:6652)
            //  at android.view.ViewRootImpl$InputStage.forward(ViewRootImpl.java:6618)
            //  at android.view.ViewRootImpl$InputStage.apply(ViewRootImpl.java:6626)
            //  at android.view.ViewRootImpl$InputStage.deliver(ViewRootImpl.java:6599)
            //  at android.view.ViewRootImpl.deliverInputEvent(ViewRootImpl.java:9880)
            //  at android.view.ViewRootImpl.doProcessInputEvents(ViewRootImpl.java:9718)
            //  at android.view.ViewRootImpl.enqueueInputEvent(ViewRootImpl.java:9671)
            //  at android.view.ViewRootImpl$WindowInputEventReceiver.onInputEvent(ViewRootImpl.java:10014)
            //  at android.view.InputEventReceiver.dispatchInputEvent(InputEventReceiver.java:220)
            //  at android.os.MessageQueue.nativePollOnce(Native method)
            //  at android.os.MessageQueue.next(MessageQueue.java:335)
            //  at android.os.Looper.loop(Looper.java:206)
            //  at android.app.ActivityThread.main(ActivityThread.java:8633)
            //  at java.lang.reflect.Method.invoke(Native method)
            //  at com.android.internal.os.RuntimeInit$MethodAndArgsCaller.run(RuntimeInit.java:602)
            //  at com.android.internal.os.ZygoteInit.main(ZygoteInit.java:1130)
            SpannableString spannable = new SpannableString(content);
            if (mKeyword != null && mKeyword.length() > 0) {
                int index = content.indexOf(mKeyword);
                if (index == -1) {
                    index = content.toLowerCase().indexOf(mKeyword.toLowerCase());
                }
                while (index > -1) {
                    int start = index;
                    int end = index + mKeyword.length();
                    spannable.setSpan(new BackgroundColorSpan(Color.WHITE), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    spannable.setSpan(new ForegroundColorSpan(Color.BLACK), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    index = content.indexOf(mKeyword, end);
                    if (index == -1) {
                        index = content.toLowerCase().indexOf(mKeyword.toLowerCase(), end);
                    }
                }
            } else {
                // 高亮代码行数
                Matcher matcher = CODE_REGEX.matcher(content);
                if (spannable.length() > 0) {
                    while (matcher.find()) {
                        // 不包含左括号（
                        int start = matcher.start() + "(".length();
                        // 不包含右括号 ）
                        int end = matcher.end() - ")".length();
                        // 设置前景
                        spannable.setSpan(new ForegroundColorSpan(0xFF999999), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                        // 设置下划线
                        spannable.setSpan(new UnderlineSpan(), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }
                }
            }

            // 高亮 H5 链接
            Matcher matcher = LINK_REGEX.matcher(content);
            while (matcher.find()) {
                // 不包含左括号（
                int start = matcher.start();
                // 不包含右括号 ）
                int end = matcher.end();
                URLSpan urlSpan = new URLSpan(String.valueOf(spannable.subSequence(start, end)));
                spannable.setSpan(urlSpan, start, end, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                // 为什么不设置点击事件，因为这样会导致外层 ScrollView 触摸事件和 itemView 点击事件无效
                //mContentView.setMovementMethod(LinkMovementMethod.getInstance());
            }

            mContentView.setText(spannable);

            final int resourceId;
            switch (info.getLevel()) {
                case LogLevel.DEBUG:
                    resourceId = R.color.logcat_level_debug_color;
                    break;
                case LogLevel.INFO:
                    resourceId = R.color.logcat_level_info_color;
                    break;
                case LogLevel.WARN:
                    resourceId = R.color.logcat_level_warn_color;
                    break;
                case LogLevel.ERROR:
                    resourceId = R.color.logcat_level_error_color;
                    break;
                case LogLevel.VERBOSE:
                    resourceId = R.color.logcat_level_verbose_color;
                    break;
                default:
                    resourceId = R.color.logcat_level_other_color;
                    break;
            }
            final int textColor;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                textColor = mContentView.getResources().getColor(resourceId, mContentView.getContext().getTheme());
            } else {
                textColor = mContentView.getResources().getColor(resourceId);
            }
            mContentView.setTextColor(textColor);
            mLineView.setVisibility((position == 0) ? View.INVISIBLE : View.VISIBLE);

            int maxLine;
            if (LogcatUtils.isPortrait(itemView.getContext())) {
                maxLine = 6;
            } else {
                maxLine = 4;
            }

            if (content.split("\n").length - maxLine > 1) {

                if (mExpandSet.get(info.hashCode())) {
                    if (mContentView.getMaxLines() != Integer.MAX_VALUE) {
                        mContentView.setMaxLines(Integer.MAX_VALUE);
                    }
                    mIndexView.setText(String.valueOf(position + 1));
                } else {
                    if (mContentView.getMaxLines() != maxLine) {
                        mContentView.setMaxLines(maxLine);
                    }
                    mIndexView.setText(LogLevel.VERBOSE);
                }
            } else {
                mContentView.setMaxLines(Integer.MAX_VALUE);
                mIndexView.setText(String.valueOf(position + 1));
            }

            mHorizontalScrollView.post(mScrollRunnable);
        }

        /**
         * ViewHolder 解绑时回调
         */
        public void onDetached() {
            mHorizontalScrollView.removeCallbacks(mScrollRunnable);
        }

        /**
         * ViewHolder 回收时回调
         */
        public void onRecycled() {
            mHorizontalScrollView.removeCallbacks(mScrollRunnable);
        }

        /**
         * 滚动任务
         */
        private final Runnable mScrollRunnable = new Runnable() {

            @Override
            public void run() {
                int layoutPosition = getLayoutPosition();
                if (layoutPosition < 0) {
                    // 在进行屏幕旋转时，位置索引会为 -1
                    // java.lang.ArrayIndexOutOfBoundsException: length=163; index=-1
                    return;
                }
                if (layoutPosition >= getItemCount()) {
                    // 避免数组出现越界
                    return;
                }
                LogcatInfo info = getItem(layoutPosition);
                int scrollX = mScrollXSet.get(info.hashCode());
                if (mHorizontalScrollView.getScrollX() == scrollX) {
                    return;
                }
                mHorizontalScrollView.scrollTo(scrollX, 0);
            }
        };
    }

    /**
     * RecyclerView 条目点击监听类
     */
    public interface OnItemClickListener{

        /**
         * 当 RecyclerView 某个条目被点击时回调
         */
        void onItemClick(LogcatInfo info, int position);
    }

    /**
     * RecyclerView 条目长按监听类
     */
    public interface OnItemLongClickListener {

        /**
         * 当 RecyclerView 某个条目被长按时回调
         */
        boolean onItemLongClick(LogcatInfo info, int position);
    }
}