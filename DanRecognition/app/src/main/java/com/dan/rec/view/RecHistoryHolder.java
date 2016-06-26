package com.dan.rec.view;

import android.content.Context;
import android.view.View;
import android.widget.ListView;

import com.dan.rec.RecognitionApplication;
import com.dan.rec.db.DbManager;
import com.dan.rec.db.LogItem;

import java.util.ArrayList;
import java.util.List;

/**
 * @功能描述:
 * @作者:Xixi
 * @创建时间: 16/5/21 11:56
 */
public class RecHistoryHolder {
    private final int EfectTime = 48 * 60 * 60 * 1000;
    private Context mContext;
    private ListView mRootView;
    private DetectedHistoryLogAdapter mAdapter;
    private ArrayList<LogItem> mLogItems;

    public RecHistoryHolder(View rootView) {
        mContext = RecognitionApplication.getContext();
        if (rootView instanceof ListView) {
            mRootView = (ListView) rootView;
        }
        DbManager.delete(getEfectStartTime());
    }

    public boolean isShowing() {
        return mRootView.getVisibility() == View.VISIBLE;
    }

    public void showOrHide() {
        if (isShowing()) {
            mRootView.setVisibility(View.GONE);
        } else {
            mRootView.setVisibility(View.VISIBLE);
            refreshView();
        }
    }

    private void refreshView() {
        if (mAdapter == null) {
            mLogItems = new ArrayList<>();
            mAdapter = new DetectedHistoryLogAdapter(mContext, mLogItems);
            mRootView.setAdapter(mAdapter);
        }
        List<LogItem> items = DbManager.queryAll(getEfectStartTime());
        if (items != null) {
            mLogItems.clear();
            mLogItems.addAll(items);
            mAdapter.notifyDataSetChanged();
        }
    }

    private long getEfectStartTime() {
        return System.currentTimeMillis() - EfectTime;
    }
}
