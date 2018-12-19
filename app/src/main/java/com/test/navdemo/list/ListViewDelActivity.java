package com.test.navdemo.list;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import com.litesuits.orm.db.assit.QueryBuilder;
import com.mcxtzhang.commonadapter.lvgv.CommonAdapter;
import com.mcxtzhang.commonadapter.lvgv.ViewHolder;
import com.mcxtzhang.swipemenulib.SwipeMenuLayout;
import com.test.navdemo.R;
import com.test.navdemo.alertview.AlertView;
import com.test.navdemo.alertview.OnItemClickListener;
import com.test.navdemo.eventbus.MessageEvent;
import com.test.navdemo.orm.MarketBean;
import com.test.navdemo.util.ORMUtil;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;


public class ListViewDelActivity extends Activity implements View.OnClickListener {
    private static final String TAG = "zxt";
    private ListView mLv;
    private List<MarketBean> mDatas;
    private Button backBtn;
    private AlertView mAlertView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom_multi_type);
        mLv = (ListView) findViewById(R.id.listviewmarks);
        backBtn = (Button) findViewById(R.id.backBtn);
        backBtn.setOnClickListener(this);
        mDatas = initDatas();
        mLv.setAdapter(new CommonAdapter<MarketBean>(this, mDatas, R.layout./*item_swipe_menu*/item_cst_swipe) {
            @Override
            public void convert(final ViewHolder holder, MarketBean swipeBean, final int position) {
                //((SwipeMenuLayout)holder.getConvertView()).setIos(false);//这句话关掉IOS阻塞式交互效果
                holder.setText(R.id.name, "变压器信息");
                holder.setText(R.id.type, "位置名称：" + swipeBean.getAddress());
                holder.setText(R.id.content, "位置描述：" + swipeBean.getContent());
                holder.setText(R.id.lanlat, "经纬度信息：" + swipeBean.getLongitude() + "," + swipeBean.getLatitude());
//                name = findViewById(R.id.name);
//                type = findViewById(R.id.type);
//                consumeNum = findViewById(R.id.content);
//                lanLat = findViewById(R.id.lanlat);
//                name.setText("变压器信息");
//                type.setText("位置名称：" + object.getAddress());
//                lanLat.setText("经纬度信息：" + object.getLongitude() + "," + object.getLatitude());
//                consumeNum.setText("位置描述：" + object.getContent());

                holder.setOnClickListener(R.id.clickinfo, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        MessageEvent messageEvent = new MessageEvent();
                        messageEvent.setMessage(String.valueOf(getItem(position).getId()));
                        EventBus.getDefault().postSticky(messageEvent);
                        finish();
                    }
                });

                holder.setOnClickListener(R.id.btnDelete, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        mAlertView = new AlertView("警告", "是否确认删除？", "取消", new String[]{"确定"}, null, ListViewDelActivity.this, AlertView.Style.Alert, new OnItemClickListener() {
                            @Override
                            public void onItemClick(Object o, int position) {
                                if (position == -1) {
                                    if (mAlertView != null) {
                                        mAlertView.dismiss();
                                    }
                                } else {
                                    //在ListView里，点击侧滑菜单上的选项时，如果想让擦花菜单同时关闭，调用这句话
                                    ORMUtil.getLiteOrm(ListViewDelActivity.this).delete(getItem(position));
                                    ((SwipeMenuLayout) holder.getConvertView()).quickClose();
                                    mDatas.remove(position);
                                    notifyDataSetChanged();
                                    mAlertView.dismiss();
                                }
                            }
                        });
                        mAlertView.show();
                    }
                });
            }
        });
    }

    private List<MarketBean> initDatas() {
        ArrayList<MarketBean> query = ORMUtil.getLiteOrm(ListViewDelActivity.this).query(new QueryBuilder<MarketBean>(MarketBean.class)
                .appendOrderDescBy("createTime"));
        return query;
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

    OnItemClickListener onItemClickListener = new OnItemClickListener() {
        @Override
        public void onItemClick(Object o, int position) {
            if (position == 0) {
                mAlertView.dismiss();
            } else {

            }
        }
    };
}
