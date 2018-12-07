package com.test.navdemo;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.MyLocationStyle;
import com.amap.api.navi.AMapNavi;
import com.amap.api.navi.AMapNaviListener;
import com.amap.api.navi.AMapNaviView;
import com.amap.api.navi.enums.NaviType;
import com.amap.api.navi.model.AMapCalcRouteResult;
import com.amap.api.navi.model.AMapLaneInfo;
import com.amap.api.navi.model.AMapModelCross;
import com.amap.api.navi.model.AMapNaviCameraInfo;
import com.amap.api.navi.model.AMapNaviCross;
import com.amap.api.navi.model.AMapNaviInfo;
import com.amap.api.navi.model.AMapNaviLocation;
import com.amap.api.navi.model.AMapNaviRouteNotifyData;
import com.amap.api.navi.model.AMapNaviTrafficFacilityInfo;
import com.amap.api.navi.model.AMapServiceAreaInfo;
import com.amap.api.navi.model.AimLessModeCongestionInfo;
import com.amap.api.navi.model.AimLessModeStat;
import com.amap.api.navi.model.NaviInfo;
import com.amap.api.navi.model.NaviLatLng;
import com.autonavi.tbt.TrafficFacilityInfo;
import com.bigkoo.alertview.AlertView;
import com.bigkoo.alertview.OnItemClickListener;
import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.Permission;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, AMap.InfoWindowAdapter, AMap.OnMarkerClickListener,
        AMap.OnInfoWindowClickListener, AMapNaviListener, OnItemClickListener {
    AMapNaviView mMapView = null;
    AMap aMap = null;
    Button flagBtn = null;
    private static final String TAG = "WindowAdapter";
    private AMapNavi mAMapNavi = null;
    private AlertView mAlertView;//避免创建重复View，先创建View，然后需要的时候show出来，推荐这个做法
    private AlertView mAlertViewExt;//窗口拓展例子
    private EditText etName;//拓展View内容
    private InputMethodManager imm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //获取地图控件引用
        mMapView = (AMapNaviView) findViewById(R.id.map);
        //在activity执行onCreate时执行mMapView.onCreate(savedInstanceState)，创建地图
        mMapView.onCreate(savedInstanceState);
        requestPermission();
        flagBtn = (Button) findViewById(R.id.button);
        flagBtn.setOnClickListener(this);
    }

    public void requestPermission() {
        AndPermission.with(this)
                .runtime()
                .permission(Permission.Group.LOCATION)
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
        }
        aMap.moveCamera(CameraUpdateFactory.zoomTo(17));
        MyLocationStyle myLocationStyle;
        myLocationStyle = new MyLocationStyle();//初始化定位蓝点样式类myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE);//连续定位、且将视角移动到地图中心点，定位点依照设备方向旋转，并且会跟随设备移动。（1秒1次定位）如果不设置myLocationType，默认也会执行此种模式。
        myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE);
        myLocationStyle.interval(2000); //设置连续定位模式下的定位间隔，只在连续定位模式下生效，单次定位模式下不会生效。单位为毫秒。
        myLocationStyle.strokeColor(Color.TRANSPARENT);//设置定位蓝点精度圆圈的边框颜色
        myLocationStyle.radiusFillColor(Color.TRANSPARENT);//设置定位蓝点精度圆圈的填充颜色
        aMap.setMyLocationStyle(myLocationStyle);//设置定位蓝点的Style
        aMap.getUiSettings().setMyLocationButtonEnabled(true);//设置默认定位按钮是否显示，非必需设置。
        aMap.setMyLocationEnabled(true);// 设置为true表示启动显示定位蓝点，false表示隐藏定位蓝点并不进行定位，默认是false。
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.button:
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
                        System.out.println(isOpen);
                    }
                });
                mAlertViewExt.addExtView(extView);
                mAlertViewExt.show();
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
        mAMapNavi.stopNavi();
        mAMapNavi.destroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //在activity执行onResume时执行mMapView.onResume ()，重新绘制加载地图
        mMapView.onResume();
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
    public View getInfoWindow(Marker marker) {
        //关联布局
        View view = LayoutInflater.from(this).inflate(R.layout.layout_info_item, null);
        //标题
        TextView title = (TextView) view.findViewById(R.id.info_title);
        //地址信息
        TextView address = (TextView) view.findViewById(R.id.info_address);
        //纬度
        TextView latitude = (TextView) view.findViewById(R.id.info_latitude);
        //经度
        TextView longitude = (TextView) view.findViewById(R.id.info_longitude);

        title.setText(marker.getTitle());
        address.setText(marker.getSnippet());
        latitude.setText("纬度：" + marker.getPosition().latitude + "");
        longitude.setText("经度" + marker.getPosition().longitude + "");
        Log.e(TAG, "getInfoWindow1: " + marker.getTitle());
        Log.e(TAG, "getInfoWindow: " + marker.getSnippet());
        Log.e(TAG, "getInfoWindow: " + marker.getPosition().latitude);
        Log.e(TAG, "getInfoWindow: " + marker.getPosition().longitude);
        return view;
    }

    @Override
    public View getInfoContents(Marker marker) {
        return null;
    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        Log.e(TAG, "InfoWindow被点击了");

        //获取AMapNavi实例
        mAMapNavi = AMapNavi.getInstance(this);
//添加监听回调，用于处理算路成功
        mAMapNavi.addAMapNaviListener(this);
        mAMapNavi.calculateWalkRoute(new NaviLatLng(marker.getPosition().latitude, marker.getPosition().longitude));

//        Poi start = new Poi(aMap.getMyLocation().getProvider(), new LatLng(aMap.getMyLocation().getLatitude(), aMap.getMyLocation().getLongitude()), "");
//        Poi end = new Poi(marker.getSnippet(), new LatLng(marker.getPosition().latitude, marker.getPosition().longitude), "B000A83M61");
//        AmapNaviPage.getInstance().showRouteActivity(this, new AmapNaviParams(null, null, end, AmapNaviType.WALK), null);
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        return false;
    }

    @Override
    public void onInitNaviSuccess() {

    }

    @Override
    public void onInitNaviFailure() {

    }

    @Override
    public void onStartNavi(int i) {

    }

    @Override
    public void onTrafficStatusUpdate() {

    }

    @Override
    public void onLocationChange(AMapNaviLocation aMapNaviLocation) {

    }

    @Override
    public void onGetNavigationText(int i, String s) {

    }

    @Override
    public void onGetNavigationText(String s) {

    }

    @Override
    public void onEndEmulatorNavi() {

    }

    @Override
    public void onArriveDestination() {

    }

    @Override
    public void onCalculateRouteFailure(int i) {

    }

    @Override
    public void onReCalculateRouteForYaw() {

    }

    @Override
    public void onReCalculateRouteForTrafficJam() {

    }

    @Override
    public void onArrivedWayPoint(int i) {

    }

    @Override
    public void onGpsOpenStatus(boolean b) {

    }

    @Override
    public void onNaviInfoUpdate(NaviInfo naviInfo) {

    }

    @Override
    public void onNaviInfoUpdated(AMapNaviInfo aMapNaviInfo) {

    }

    @Override
    public void updateCameraInfo(AMapNaviCameraInfo[] aMapNaviCameraInfos) {

    }

    @Override
    public void updateIntervalCameraInfo(AMapNaviCameraInfo aMapNaviCameraInfo, AMapNaviCameraInfo aMapNaviCameraInfo1, int i) {

    }

    @Override
    public void onServiceAreaUpdate(AMapServiceAreaInfo[] aMapServiceAreaInfos) {

    }

    @Override
    public void showCross(AMapNaviCross aMapNaviCross) {

    }

    @Override
    public void hideCross() {

    }

    @Override
    public void showModeCross(AMapModelCross aMapModelCross) {

    }

    @Override
    public void hideModeCross() {

    }

    @Override
    public void showLaneInfo(AMapLaneInfo[] aMapLaneInfos, byte[] bytes, byte[] bytes1) {

    }

    @Override
    public void showLaneInfo(AMapLaneInfo aMapLaneInfo) {

    }

    @Override
    public void hideLaneInfo() {

    }

    @Override
    public void onCalculateRouteSuccess(int[] ints) {

    }

    @Override
    public void notifyParallelRoad(int i) {

    }

    @Override
    public void OnUpdateTrafficFacility(AMapNaviTrafficFacilityInfo aMapNaviTrafficFacilityInfo) {

    }

    @Override
    public void OnUpdateTrafficFacility(AMapNaviTrafficFacilityInfo[] aMapNaviTrafficFacilityInfos) {

    }

    @Override
    public void OnUpdateTrafficFacility(TrafficFacilityInfo trafficFacilityInfo) {

    }

    @Override
    public void updateAimlessModeStatistics(AimLessModeStat aimLessModeStat) {

    }

    @Override
    public void updateAimlessModeCongestionInfo(AimLessModeCongestionInfo aimLessModeCongestionInfo) {

    }

    @Override
    public void onPlayRing(int i) {

    }

    @Override
    public void onCalculateRouteSuccess(AMapCalcRouteResult aMapCalcRouteResult) {
        mAMapNavi.startNavi(NaviType.GPS);
    }

    @Override
    public void onCalculateRouteFailure(AMapCalcRouteResult aMapCalcRouteResult) {

    }

    @Override
    public void onNaviRouteNotify(AMapNaviRouteNotifyData aMapNaviRouteNotifyData) {

    }

    @Override
    public void onItemClick(Object o, int position) {
        closeKeyboard();
        //判断是否是拓展窗口View，而且点击的是非取消按钮
        if (o == mAlertViewExt && position != AlertView.CANCELPOSITION) {
            String name = etName.getText().toString();
            if (name.isEmpty()) {
                Toast.makeText(this, "啥都没填呢", Toast.LENGTH_SHORT).show();
            } else {
                MarketBean marketBean = new MarketBean(aMap.getMyLocation().getLatitude(), aMap.getMyLocation().getLongitude(), "标记", name);
                aMap.addMarker(new MarkerOptions()
                        .position(new LatLng(marketBean.getLatitude(),//设置纬度
                                marketBean.getLongitude()))//设置经度
                        .title(marketBean.getTitle())//设置标题
                        .snippet(marketBean.getContent())//设置内容
                        .setFlat(true) // 将Marker设置为贴地显示，可以双指下拉地图查看效果
                        .draggable(true) //设置Marker可拖动
                        .icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_launcher)));
                //设置自定义弹窗
                aMap.setInfoWindowAdapter(new WindowAdapter(this));
                //绑定信息窗点击事件
                aMap.setOnInfoWindowClickListener(this);
                aMap.setOnMarkerClickListener(this);
            }
            return;
        }
        Toast.makeText(this, "啥都没填呢", Toast.LENGTH_SHORT).show();
    }

    private void closeKeyboard() {
        //关闭软键盘
        imm.hideSoftInputFromWindow(etName.getWindowToken(), 0);
        //恢复位置
        mAlertViewExt.setMarginBottom(0);
    }
}
