package com.hjq.logcat;

import android.graphics.Color;
import android.os.Build;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 *    author : Android 轮子哥
 *    github : https://github.com/getActivity/Logcat
 *    time   : 2020/01/24
 *    desc   : 日志列表适配器
 */
final class LogcatAdapter extends BaseAdapter {

    private static final int MAX_LINE = 4;
    private final SparseBooleanArray mExpandSet = new SparseBooleanArray();

    private final List<LogcatInfo> mDataSet = new ArrayList<>();
    private String mKeyword = "";

    @Override
    public int getCount() {
        return mDataSet.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.logcat_item_logcat, parent, false);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.onBindView(getItem(position), position);
        return convertView;
    }

    @Override
    public LogcatInfo getItem(int position) {
        return mDataSet.get(position);
    }

    List<LogcatInfo> getData() {
        return mDataSet;
    }

    /**
     * 添加单条数据
     */
    void addItem(LogcatInfo item) {
        mDataSet.add(item);
        notifyDataSetChanged();
    }

    /**
     * 删除某条数据
     */
    void removeItem(int position) {
        mDataSet.remove(position);
        notifyDataSetChanged();
    }

    void removeItem(LogcatInfo item) {
        mDataSet.remove(item);
        notifyDataSetChanged();
    }

    /**
     * 清空当前数据
     */
    void clearData() {
        mExpandSet.clear();
        mDataSet.clear();
        notifyDataSetChanged();
    }

    void onItemClick(int position) {
        boolean expand = mExpandSet.get(position);
        mExpandSet.put(position, !expand);
        notifyDataSetChanged();
    }

    void setKeyword(String keyword) {
        mKeyword = keyword;
    }

    private class ViewHolder {

        private final TextView mContentView;
        private final TextView mIndexView;
        private final ImageView mExpandView;
        private final View mLineView;

        private ViewHolder(View view) {
            mContentView = view.findViewById(R.id.tv_log_content);
            mIndexView = view.findViewById(R.id.tv_log_index);
            mExpandView = view.findViewById(R.id.iv_log_expand);
            mLineView = view.findViewById(R.id.v_log_line);
        }

        private void onBindView(LogcatInfo info, int position) {
            String content = info.toString();
            CharSequence text = content;
            if (mKeyword != null && mKeyword.length() > 0) {
                int index = content.indexOf(mKeyword);
                if (index == -1) {
                    index = content.toLowerCase().indexOf(mKeyword.toLowerCase());
                }
                SpannableString spannable = new SpannableString(content);
                while (index > -1) {
                    int start = index;
                    int end = index + mKeyword.length();
                    spannable.setSpan(new BackgroundColorSpan(Color.WHITE), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    spannable.setSpan(new ForegroundColorSpan(Color.BLACK), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    index = content.indexOf(mKeyword, end);
                    if (index == -1) {
                        index = content.toLowerCase().indexOf(mKeyword.toLowerCase(), end);
                    }
                    text = spannable;
                }
            }
            mContentView.setText(text);

            final int resourceId;
            switch (info.getLevel()) {
                case "D":
                    resourceId = R.color.logcat_level_debug_color;
                    break;
                case "I":
                    resourceId = R.color.logcat_level_info_color;
                    break;
                case "W":
                    resourceId = R.color.logcat_level_warn_color;
                    break;
                case "E":
                    resourceId = R.color.logcat_level_error_color;
                    break;
                case "V":
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
            mLineView.setVisibility((position == getCount() - 1) ? View.GONE : View.VISIBLE);

            if (mContentView.getLineCount() - MAX_LINE > 1) {
                if (mExpandSet.get(position)) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        if (mContentView.getMaxLines() != Integer.MAX_VALUE) {
                            mContentView.setMaxLines(Integer.MAX_VALUE);
                            mExpandView.setImageResource(R.drawable.logcat_ic_arrows_up);
                        }
                    } else {
                        mContentView.setMaxLines(Integer.MAX_VALUE);
                        mExpandView.setImageResource(R.drawable.logcat_ic_arrows_up);
                    }
                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        if (mContentView.getMaxLines() != MAX_LINE) {
                            mContentView.setMaxLines(MAX_LINE);
                            mExpandView.setImageResource(R.drawable.logcat_ic_arrows_down);
                        }
                    } else {
                        mContentView.setMaxLines(MAX_LINE);
                        mExpandView.setImageResource(R.drawable.logcat_ic_arrows_down);
                    }
                }

                mExpandView.setVisibility(View.VISIBLE);
                mIndexView.setVisibility(View.GONE);
            } else {
                mContentView.setMaxLines(Integer.MAX_VALUE);
                mIndexView.setText(String.valueOf(position + 1));

                mExpandView.setVisibility(View.GONE);
                mIndexView.setVisibility(View.VISIBLE);
            }
        }
    }
}