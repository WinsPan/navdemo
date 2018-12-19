package com.test.navdemo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.BounceInterpolator;
import android.view.animation.Interpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.Projection;
import com.amap.api.maps.UiSettings;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.MyLocationStyle;
import com.amap.api.navi.model.NaviLatLng;
import com.litesuits.orm.db.assit.QueryBuilder;
import com.litesuits.orm.db.model.ColumnsValue;
import com.litesuits.orm.db.model.ConflictAlgorithm;
import com.test.navdemo.alertview.AlertView;
import com.test.navdemo.alertview.OnItemClickListener;
import com.test.navdemo.eventbus.MessageEvent;
import com.test.navdemo.list.ListViewDelActivity;
import com.test.navdemo.nav.WalkRouteCalculateActivity;
import com.test.navdemo.orm.MarketBean;
import com.test.navdemo.util.ORMUtil;
import com.test.navdemo.util.ToastUtil;
import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.Permission;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Date;

public class MainActivity extends Activity implements View.OnClickListener, AMap.OnMarkerClickListener,
        AMap.OnInfoWindowClickListener, OnItemClickListener {
    MapView mMapView = null;
    AMap aMap = null;
    Button flagBtn = null;
    Button listBtn = null;
    private static final String TAG = "WindowAdapter";
    private AlertView mAlertView;//避免创建重复View，先创建View，然后需要的时候show出来，推荐这个做法
    private AlertView mAlertViewExt;//窗口拓展例子
    private EditText etName;//拓展View内容
    private EditText etContent;//拓展View内容
    private InputMethodManager imm;
    private UiSettings mUiSettings;
    private TextView nameMain;
    private TextView typeMain;
    private TextView consumeNumMain;
    private TextView lanLatMain;
    private LinearLayout bottomMain;
    private LinearLayout bottomNav;
    private MyLocationStyle myLocationStyle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //取消标题
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        //取消状态栏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);
        //获取地图控件引用
        mMapView = (MapView) findViewById(R.id.map);
        //在activity执行onCreate时执行mMapView.onCreate(savedInstanceState)，创建地图
        mMapView.onCreate(savedInstanceState);
        requestPermission();
        flagBtn = (Button) findViewById(R.id.button);
        flagBtn.setOnClickListener(this);
        listBtn = (Button) findViewById(R.id.signlist);
        listBtn.setOnClickListener(this);

        bottomMain = (LinearLayout) findViewById(R.id.bottomMain);
        bottomNav = (LinearLayout) findViewById(R.id.bottomNav);
        nameMain = (TextView) findViewById(R.id.nameMain);
        typeMain = (TextView) findViewById(R.id.typeMain);
        consumeNumMain = (TextView) findViewById(R.id.contentMain);
        lanLatMain = (TextView) findViewById(R.id.lanlatMain);
        bottomNav.setOnClickListener(this);
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    public void requestPermission() {
        AndPermission.with(this)
                .runtime()
                .permission(new String[]{Permission.ACCESS_COARSE_LOCATION, Permission.ACCESS_FINE_LOCATION, Permission.WRITE_EXTERNAL_STORAGE})
                .onGranted(permissions -> {
                    initMap();
                    // Storage permission are allowed.
                })
                .onDenied(permissions -> {
                    // Storage permission are not allowed.
                    Toast.makeText(MainActivity.this, "请先开启定位权限", Toast.LENGTH_LONG);
                })
                .start();
    }

    public void initMap() {
        if (aMap == null) {
            aMap = mMapView.getMap();
            mUiSettings = aMap.getUiSettings();
        }
        aMap.moveCamera(CameraUpdateFactory.zoomTo(17));
        myLocationStyle = new MyLocationStyle();//初始化定位蓝点样式类myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE);//连续定位、且将视角移动到地图中心点，定位点依照设备方向旋转，并且会跟随设备移动。（1秒1次定位）如果不设置myLocationType，默认也会执行此种模式。
        myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE);
        myLocationStyle.interval(2000); //设置连续定位模式下的定位间隔，只在连续定位模式下生效，单次定位模式下不会生效。单位为毫秒。
        myLocationStyle.strokeColor(Color.TRANSPARENT);//设置定位蓝点精度圆圈的边框颜色
        myLocationStyle.radiusFillColor(Color.TRANSPARENT);//设置定位蓝点精度圆圈的填充颜色
        aMap.setMyLocationStyle(myLocationStyle);//设置定位蓝点的Style
        aMap.getUiSettings().setMyLocationButtonEnabled(true);//设置默认定位按钮是否显示，非必需设置。
        aMap.setMyLocationEnabled(true);// 设置为true表示启动显示定位蓝点，false表示隐藏定位蓝点并不进行定位，默认是false。
        // 绑定marker拖拽事件
        aMap.setOnMarkerDragListener(markerDragListener);
        mUiSettings.setScaleControlsEnabled(true);
        mUiSettings.setCompassEnabled(true);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.button:
                ArrayList<MarketBean> query = ORMUtil.getLiteOrm(MainActivity.this).query(MarketBean.class);
                if (query.size() > 9) {
                    ToastUtil.show(MainActivity.this, "标记最多10条");
                    return;
                }
                imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                //拓展窗口
                mAlertViewExt = new AlertView("提示", "请输入标记信息！", "取消", null, new String[]{"完成"}, this, AlertView.Style.Alert, this);
                ViewGroup extView = (ViewGroup) LayoutInflater.from(this).inflate(R.layout.alertext_form, null);
                etName = (EditText) extView.findViewById(R.id.etName);
                etName.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                    @Override
                    public void onFocusChange(View view, boolean focus) {
                        //输入框出来则往上移动
                        boolean isOpen = imm.isActive();
                        mAlertViewExt.setMarginBottom(isOpen && focus ? 120 : 0);
                    }
                });
                etContent = (EditText) extView.findViewById(R.id.etContent);
                etContent.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                    @Override
                    public void onFocusChange(View view, boolean focus) {
                        //输入框出来则往上移动
                        boolean isOpen = imm.isActive();
                        mAlertViewExt.setMarginBottom(isOpen && focus ? 120 : 0);
                    }
                });
                mAlertViewExt.addExtView(extView);
                mAlertViewExt.show();
                break;
            case R.id.signlist:
                startActivity(new Intent(MainActivity.this, ListViewDelActivity.class));
                break;
            case R.id.bottomNav:
                String[] re = lanLatMain.getText().toString().split(":");
                String[] strings = re[1].split(",");
                NaviLatLng startNav = new NaviLatLng(aMap.getMyLocation().getLatitude(), aMap.getMyLocation().getLongitude());
                NaviLatLng endNav = new NaviLatLng(Double.valueOf(strings[1]), Double.valueOf(strings[0]));
                Intent intent = new Intent(MainActivity.this, WalkRouteCalculateActivity.class);
                intent.putExtra("startNav", startNav);
                intent.putExtra("endNav", endNav);
                startActivity(intent);
                break;
            default:
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //在activity执行onDestroy时执行mMapView.onDestroy()，销毁地图
        mMapView.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        //在activity执行onResume时执行mMapView.onResume ()，重新绘制加载地图
        if (aMap != null) {
            aMap.clear();
        }
        mMapView.onResume();
        initMark();
        if (bottomMain.getVisibility() == View.VISIBLE) {
            bottomMain.setVisibility(View.GONE);
            bottomNav.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        //在activity执行onPause时执行mMapView.onPause ()，暂停地图的绘制
        mMapView.onPause();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //在activity执行onSaveInstanceState时执行mMapView.onSaveInstanceState (outState)，保存地图当前的状态
        mMapView.onSaveInstanceState(outState);
    }

    public LatLng getLocation() {
        LatLng latLng = new LatLng(aMap.getMyLocation().getLatitude(), aMap.getMyLocation().getLongitude());
        return latLng;
    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        Log.e(TAG, "InfoWindow被点击了");

//添加监听回调，用于处理算路成功
//        Poi start = new Poi(aMap.getMyLocation().getProvider(), new LatLng(aMap.getMyLocation().getLatitude(), aMap.getMyLocation().getLongitude()), "");
//        Poi end = new Poi(marker.getSnippet(), new LatLng(marker.getPosition().latitude, marker.getPosition().longitude), "B000A83M61");
//        AmapNaviPage.getInstance().showRouteActivity(this, new AmapNaviParams(null, null, end, AmapNaviType.WALK), null);
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE_NO_CENTER);
        aMap.setMyLocationStyle(myLocationStyle);
        String name = marker.getTitle();
        if (name == null || "".equals(name)) {
            return false;
        }
        jumpPoint(marker);
        ArrayList<MarketBean> query = ORMUtil.getLiteOrm(MainActivity.this).query(new QueryBuilder<MarketBean>(MarketBean.class)
                .whereEquals("address", name).whereAppendOr().whereEquals("address", marker.getSnippet())
        );
        if (query == null || query.size() < 1) {
            return false;
        }
        if (bottomMain.getVisibility() == View.GONE) {
            bottomMain.setVisibility(View.VISIBLE);
            bottomNav.setVisibility(View.VISIBLE);
        }
        nameMain.setText("变压器信息");
        typeMain.setText("位置名称:" + query.get(0).getAddress());
        lanLatMain.setText("经纬度信息:" + query.get(0).getLongitude() + "," + query.get(0).getLatitude());
        consumeNumMain.setText("位置描述:" + query.get(0).getContent());
        aMap.moveCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition(new LatLng(query.get(0).getLatitude(), query.get(0).getLongitude()), 17, 0, 0)));
        return false;
    }

    @Override
    public void onItemClick(Object o, int position) {
        //判断是否是拓展窗口View，而且点击的是非取消按钮
        if (o == mAlertViewExt && position != AlertView.CANCELPOSITION) {
            String name = etName.getText().toString();
            String content = etContent.getText().toString();
            if (name.isEmpty()) {
                Toast.makeText(this, "标记名称不能为空", Toast.LENGTH_SHORT).show();
            } else if (content.isEmpty()) {
                Toast.makeText(this, "标记描述不能为空", Toast.LENGTH_SHORT).show();
            } else {
                MarketBean marketBean = new MarketBean();
                marketBean.setLatitude(aMap.getMyLocation().getLatitude());
                marketBean.setLongitude(aMap.getMyLocation().getLongitude());
                marketBean.setTitle(name);
                marketBean.setAddress(name);
                marketBean.setContent(content);
                marketBean.setCreateTime(new Date());
                aMap.addMarker(new MarkerOptions()
                        .position(new LatLng(marketBean.getLatitude(),//设置纬度
                                marketBean.getLongitude()))//设置经度
                        .title(marketBean.getTitle())//设置标题
                        .snippet(marketBean.getContent())//设置内容
                        .setFlat(true) // 将Marker设置为贴地显示，可以双指下拉地图查看效果
                        .draggable(true) //设置Marker可拖动
                        .icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory
                                .decodeResource(getResources(), R.drawable.flag))));
                //设置自定义弹窗
                aMap.setInfoWindowAdapter(new WindowAdapter(this));
                //绑定信息窗点击事件
                aMap.setOnInfoWindowClickListener(this);
                aMap.setOnMarkerClickListener(this);
                ORMUtil.getLiteOrm(MainActivity.this).save(marketBean);
                closeKeyboard();
                mAlertViewExt.dismiss();
            }
        } else {
            Toast.makeText(this, "啥都没填呢", Toast.LENGTH_SHORT).show();
            closeKeyboard();
            mAlertViewExt.dismiss();
        }
    }

    private void closeKeyboard() {
        //关闭软键盘
        imm.hideSoftInputFromWindow(etName.getWindowToken(), 0);
        //恢复位置
        mAlertViewExt.setMarginBottom(0);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MessageEvent event) {
        if (event != null) {
            MarketBean marketBean = ORMUtil.getLiteOrm(MainActivity.this).queryById(event.getMessage(), MarketBean.class);
            myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE_NO_CENTER);
            aMap.setMyLocationStyle(myLocationStyle);
            aMap.moveCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition(new LatLng(marketBean.getLatitude(), marketBean.getLongitude()), 17, 0, 0)));
        }
    }

    // 定义 Marker拖拽的监听
    AMap.OnMarkerDragListener markerDragListener = new AMap.OnMarkerDragListener() {

        // 当marker开始被拖动时回调此方法, 这个marker的位置可以通过getPosition()方法返回。
        // 这个位置可能与拖动的之前的marker位置不一样。
        // marker 被拖动的marker对象。
        @Override
        public void onMarkerDragStart(Marker arg0) {
            // TODO Auto-generated method stub

        }

        // 在marker拖动完成后回调此方法, 这个marker的位置可以通过getPosition()方法返回。
        // 这个位置可能与拖动的之前的marker位置不一样。
        // marker 被拖动的marker对象。
        @Override
        public void onMarkerDragEnd(Marker arg0) {
            String name = arg0.getTitle();
            ArrayList<MarketBean> query = ORMUtil.getLiteOrm(MainActivity.this).query(new QueryBuilder<MarketBean>(MarketBean.class)
                    .whereEquals("address", name)
            );
            ColumnsValue cv = new ColumnsValue(new String[]{"latitude", "longitude"}, new Object[]{arg0.getPosition().latitude, arg0.getPosition().longitude});
            ORMUtil.getLiteOrm(MainActivity.this).update(query.get(0), cv, ConflictAlgorithm.None);
        }

        // 在marker拖动过程中回调此方法, 这个marker的位置可以通过getPosition()方法返回。
        // 这个位置可能与拖动的之前的marker位置不一样。
        // marker 被拖动的marker对象。
        @Override
        public void onMarkerDrag(Marker arg0) {
            // TODO Auto-generated method stub

        }
    };

    public void initMark() {
        ArrayList<MarketBean> marketBeans = ORMUtil.getLiteOrm(MainActivity.this).query(new QueryBuilder<MarketBean>(MarketBean.class)
                .appendOrderDescBy("createTime"));
        for (MarketBean marketBean : marketBeans) {
            aMap.addMarker(new MarkerOptions()
                    .position(new LatLng(marketBean.getLatitude(),//设置纬度
                            marketBean.getLongitude()))//设置经度
                    .title(marketBean.getAddress())//设置标题
                    .snippet(marketBean.getContent())//设置内容
                    .setFlat(true) // 将Marker设置为贴地显示，可以双指下拉地图查看效果
                    .draggable(true) //设置Marker可拖动
                    .icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory
                            .decodeResource(getResources(), R.drawable.flag))));
            //设置自定义弹窗
            aMap.setInfoWindowAdapter(new WindowAdapter(this));
            //绑定信息窗点击事件
            aMap.setOnInfoWindowClickListener(this);
            aMap.setOnMarkerClickListener(this);
        }
    }

    /**
     * marker点击时跳动一下
     */
    public void jumpPoint(final Marker marker) {
        final Handler handler = new Handler();
        final long start = SystemClock.uptimeMillis();
        Projection proj = aMap.getProjection();
        final LatLng markerLatlng = marker.getPosition();
        Point markerPoint = proj.toScreenLocation(markerLatlng);
        markerPoint.offset(0, -100);
        final LatLng startLatLng = proj.fromScreenLocation(markerPoint);
        final long duration = 1500;

        final Interpolator interpolator = new BounceInterpolator();
        handler.post(new Runnable() {
            @Override
            public void run() {
                long elapsed = SystemClock.uptimeMillis() - start;
                float t = interpolator.getInterpolation((float) elapsed
                        / duration);
                double lng = t * markerLatlng.longitude + (1 - t)
                        * startLatLng.longitude;
                double lat = t * markerLatlng.latitude + (1 - t)
                        * startLatLng.latitude;
                marker.setPosition(new LatLng(lat, lng));
                if (t < 1.0) {
                    handler.postDelayed(this, 16);
                }
            }
        });
    }
}
