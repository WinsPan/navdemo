package com.test.navdemo.list;

import android.content.Context;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.TextView;

import com.test.navdemo.R;
import com.test.navdemo.eventbus.MessageEvent;
import com.test.navdemo.orm.MarketBean;

import org.greenrobot.eventbus.EventBus;

import cn.lemon.view.adapter.BaseViewHolder;

class CardRecordHolder extends BaseViewHolder<MarketBean> {

    private TextView name;
    private TextView type;
    private TextView consumeNum;
    private TextView lanLat;
    private Context mContext;

    public CardRecordHolder(ViewGroup parent, Context context) {
        super(parent, R.layout.holder_consume);
        mContext = context;
    }

    @Override
    public void setData(final MarketBean object) {
        super.setData(object);
        name.setText("变压器信息");
        type.setText("位置名称：" + object.getAddress());
        lanLat.setText("经纬度信息：" + object.getLongitude() + "," + object.getLatitude());
        consumeNum.setText("位置描述：" + object.getContent());
    }

    @Override
    public void onInitializeView() {
        super.onInitializeView();
        name = findViewById(R.id.name);
        type = findViewById(R.id.type);
        consumeNum = findViewById(R.id.content);
        lanLat = findViewById(R.id.lanlat);
    }

    @Override
    public void onItemViewClick(MarketBean object) {
        super.onItemViewClick(object);
        //点击事件
        Log.i("CardRecordHolder", "onItemViewClick");
        MessageEvent messageEvent = new MessageEvent();
        messageEvent.setMessage(String.valueOf(object.getId()));
        EventBus.getDefault().postSticky(messageEvent);
        ((CustomMultiTypeActivity) mContext).finish();
    }
}