package com.hjq.logcat;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

/**
 *    author : Android 轮子哥
 *    github : https://github.com/getActivity/Logcat
 *    time   : 2020/01/24
 *    desc   : 列表选择适配器
 */
public final class ChooseAdapter extends BaseAdapter {

    private List<String> mDataSet;

    @Override
    public int getCount() {
        return mDataSet == null ? 0 : mDataSet.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public String getItem(int position) {
        return mDataSet.get(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.logcat_item_choose, parent, false);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.onBindView(getItem(position), position == getCount() - 1);
        return convertView;
    }

    void setData(List<String> data) {
        mDataSet = data;
    }

    private static class ViewHolder {

        private final TextView mContentView;
        private final View mLineView;

        private ViewHolder(View view) {
            mContentView = view.findViewById(R.id.tv_choose_content);
            mLineView = view.findViewById(R.id.v_choose_line);
        }

        private void onBindView(String text, boolean last) {
            mContentView.setText(text);
            mLineView.setVisibility(last ? View.GONE : View.VISIBLE);
        }
    }
}
