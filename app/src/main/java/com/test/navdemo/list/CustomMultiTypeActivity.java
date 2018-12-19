package com.test.navdemo.list;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.litesuits.orm.db.assit.QueryBuilder;
import com.test.navdemo.R;
import com.test.navdemo.orm.MarketBean;
import com.test.navdemo.util.ORMUtil;

import java.util.ArrayList;

import cn.lemon.view.RefreshRecyclerView;
import cn.lemon.view.adapter.BaseViewHolder;
import cn.lemon.view.adapter.CustomMultiTypeAdapter;
import cn.lemon.view.adapter.IViewHolderFactory;

public class CustomMultiTypeActivity extends Activity implements IViewHolderFactory, View.OnClickListener, RecyclerView.OnLongClickListener {

    private RefreshRecyclerView mRecyclerView;
    private CustomMultiTypeAdapter mAdapter;
    private int mPage = 0;
    private Button backBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom_multi_type);

        mRecyclerView = (RefreshRecyclerView) findViewById(R.id.recycler_view);
        mRecyclerView.setSwipeRefreshColors(0xFF437845, 0xFFE44F98, 0xFF2FAC21);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new CustomMultiTypeAdapter(this);
        mAdapter.setViewHolderFactory(this);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.addRefreshAction(() -> getData(true));
        mRecyclerView.addLoadMoreAction(() -> getData(false));
        mRecyclerView.post(() -> {
            mRecyclerView.showSwipeRefresh();
            getData(true);
        });
        mRecyclerView.addLoadMoreErrorAction(() -> getData(false));

        mRecyclerView.setOnLongClickListener(this);
        backBtn = (Button) findViewById(R.id.backBtn);
        backBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.backBtn:
                finish();
                break;
            default:
                break;
        }
    }

    @Override
    public boolean onLongClick(View v) {
        return false;
    }

    public void getData(final boolean isRefresh) {
        if (isRefresh) {
            mPage = 1;
        } else {
            mPage++;
        }
        mRecyclerView.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (isRefresh) {
                    mAdapter.clear();
                    mRecyclerView.dismissSwipeRefresh();
                }
                MarketBean[] marketBeans = getRecordVirtualData();
                mAdapter.addAll(marketBeans, VIEW_TYPE_CARD);
                if (mPage >= (marketBeans.length / 10)) {
                    mAdapter.showNoMore();
                }
                if (isRefresh) {
                    mRecyclerView.getRecyclerView().scrollToPosition(0);
                }
            }
        }, 1000);
    }

    public MarketBean[] getRecordVirtualData() {
        ArrayList<MarketBean> query = ORMUtil.getLiteOrm(CustomMultiTypeActivity.this).query(new QueryBuilder<MarketBean>(MarketBean.class)
                .appendOrderDescBy("createTime"));
        if (query == null) {
            return new MarketBean[]{};
        } else {
            MarketBean[] marketBeans = new MarketBean[query.size()];
            query.toArray(marketBeans);
            return marketBeans;
        }
    }

    private final int VIEW_TYPE_TEXT = 128 << 1;
    private final int VIEW_TYPE_IAMGE = 128 << 2;
    private final int VIEW_TYPE_TEXT_IMAGE = 128 << 3;
    private final int VIEW_TYPE_CARD = 128 << 4;

    @Override
    public <V extends BaseViewHolder> V getViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case VIEW_TYPE_TEXT:
                return (V) new TextViewHolder(parent, CustomMultiTypeActivity.this);
            case VIEW_TYPE_CARD:
                return (V) new CardRecordHolder(parent, CustomMultiTypeActivity.this);
            default:
                return (V) new TextViewHolder(parent, CustomMultiTypeActivity.this);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }
}
