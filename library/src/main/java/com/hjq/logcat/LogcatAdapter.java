package com.hjq.logcat;

import android.annotation.SuppressLint;
import android.content.Context;
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
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
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
    private static final int LOG_REMOVE_COUNT = LOG_MAX_COUNT / 5;

    /** 报错代码行数正则表达式 */
    private static final Pattern CODE_REGEX = Pattern.compile("\\(\\w+\\.\\w+:\\d+\\)");

    /** 链接正则表达式 */
    private static final Pattern LINK_REGEX = Pattern.compile("https?://[^\\x{4e00}-\\x{9fa5}\\n\\r\\s]{3,}");

    private final SparseBooleanArray mExpandSet = new SparseBooleanArray();
    private final SparseIntArray mScrollXSet = new SparseIntArray();

    private final List<LogcatInfo> mAllData = new ArrayList<>();
    private List<LogcatInfo> mShowData = mAllData;

    private Context mContext;

    private String mKeyword = "";
    private String mLogLevel = LogLevel.VERBOSE;

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

            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);

            mHorizontalScrollView.setOnTouchListener(this);
        }

        @Override
        public void onClick(View v) {
            if (mItemClickListener != null) {
                mItemClickListener.onItemClick(getItem(getLayoutPosition()), getLayoutPosition());
            }
        }

        @Override
        public boolean onLongClick(View v) {
            if (mItemLongClickListener != null) {
                mItemLongClickListener.onItemLongClick(getItem(getLayoutPosition()), getLayoutPosition());
            }
            return false;
        }

        /**
         * View.OnTouchListener
         */
        @SuppressLint("ClickableViewAccessibility")
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_MOVE:
                    // 修改动作为 ACTION_CANCEL
                    event.setAction(MotionEvent.ACTION_CANCEL);
                    // 将事件回传给父控件
                    itemView.onTouchEvent(event);
                    // 父容器响应后恢复事件原动作
                    event.setAction(MotionEvent.ACTION_MOVE);
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    LogcatInfo info = getItem(getLayoutPosition());
                    int scrollX = mHorizontalScrollView.getScrollX();
                    mScrollXSet.put(info.hashCode(), scrollX);
                case MotionEvent.ACTION_DOWN:
                default:
                    // 将事件回传给父控件
                    itemView.onTouchEvent(event);
                    break;
            }
            return false;
        }

        private void onBindView(LogcatInfo info, int position) {
            String content = info.toString(mContext);
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
        void onItemLongClick(LogcatInfo info, int position);
    }
}