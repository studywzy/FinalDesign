package com.example.oushinu.smartmap;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.amap.api.maps.AMap;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.*;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.route.BusRouteResult;
import com.amap.api.services.route.DrivePath;
import com.amap.api.services.route.DriveRouteResult;
import com.amap.api.services.route.RideRouteResult;
import com.amap.api.services.route.RouteSearch;
import com.amap.api.services.route.WalkRouteResult;
import com.example.oushinu.smartmap.util.AMapUtil;
import com.example.oushinu.smartmap.util.ToastUtil;

public class MainActivity extends AppCompatActivity implements AMap.OnMyLocationChangeListener,AMap.OnMapClickListener,
        AMap.OnMarkerClickListener, RouteSearch.OnRouteSearchListener {

    private AMap aMap;
    private MapView mapView;
    private MyLocationStyle myLocationStyle;
    private static final int STROKE_COLOR = Color.argb(180, 3, 145, 255);
    private static final int FILL_COLOR = Color.argb(10, 0, 0, 180);
    private Context mContext;
    private RouteSearch mRouteSearch;
    private DriveRouteResult mDriveRouteResult;
    //起点待修改为当前定位点，终点待修改为搜索指定点
    //第一个参数为经度，第二个参数为纬度
    private LatLonPoint mStartPoint = new LatLonPoint(39.942295, 116.335891);
    private LatLonPoint mEndPoint = new LatLonPoint(39.995576, 116.481288);
    private final int ROUTE_TYPE_DRIVE = 2;//出行为驾车出行，标识为2
    private RelativeLayout mBottomLayout;
    private TextView mRouteTimeDes, mRouteDetailDes;
    private ProgressDialog progDialog = null;// 搜索时进度条

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);// 不显示程序的标题栏

        setContentView(R.layout.activity_main);

        mContext = this.getApplicationContext();

        mapView = (MapView) findViewById(R.id.map);

        mapView.onCreate(savedInstanceState);// Activity被唤醒时调用地图唤醒

        init();//调用init()方法

        setfromandtoMarker();//调用setfromandtoMarker()方法

        myLocationStyle = new MyLocationStyle();
        myLocationStyle.strokeColor(STROKE_COLOR);// 设置圆形的边框颜色
        myLocationStyle.radiusFillColor(FILL_COLOR);// 设置圆形的填充颜色
        myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE_NO_CENTER);
        myLocationStyle.interval(2000); //设置连续定位模式下的定位间隔，只在连续定位模式下生效，单次定位模式下不会生效。单位为毫秒。
        aMap.setMyLocationStyle(myLocationStyle);//设置定位蓝点的Style
        aMap.getUiSettings().setMyLocationButtonEnabled(true);//设置默认定位按钮是否显示，非必需设置。
        aMap.setMyLocationEnabled(true);// 设置为true表示启动显w示定位蓝点，false表示隐藏定位蓝点并不进行定位，默认是false
    }

    //设置起点终点位置和图标
    private void setfromandtoMarker() {
        aMap.addMarker(new MarkerOptions()
                .position(AMapUtil.convertToLatLng(mStartPoint))
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.start)));
        aMap.addMarker(new MarkerOptions()
                .position(AMapUtil.convertToLatLng(mEndPoint))
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.end)));
    }

    /**
     * 初始化
     */
    private void init() {
        if (aMap == null) {
            aMap = mapView.getMap();//获得地图控制器AMap对象
        }
        registerListener();//注册监听
        mRouteSearch = new RouteSearch(this);
        mRouteSearch.setRouteSearchListener(this);

        mBottomLayout = (RelativeLayout) findViewById(R.id.bottom_layout);
        mRouteTimeDes = (TextView) findViewById(R.id.firstline);
        mRouteDetailDes = (TextView) findViewById(R.id.secondline);
    }

    //获取经纬度信息
    public void onMyLocationChange(android.location.Location location){
    //从location对象中获取经纬度信息，地址描述信息，建议拿到位置之后调用逆地理编码接口获取（获取地址描述数据章节有介绍）
    }


    /**
     * 注册监听
     */
    private void registerListener() {
        aMap.setOnMapClickListener(this);
        aMap.setOnMarkerClickListener(this);

    }

    public void onDriveClick(View view) {
        searchRouteResult(ROUTE_TYPE_DRIVE, RouteSearch.DRIVING_SINGLE_DEFAULT);//调用searchRouteResult(int routeType, int mode)方法
        mapView.setVisibility(View.VISIBLE);//使view可见
    }

    /**
     * 开始搜索路径规划方案
     */
    public void searchRouteResult(int routeType, int mode) {
        if (mStartPoint == null) {
            ToastUtil.show(mContext, "起点未设置");
            return;
        }
        if (mEndPoint == null) {
            ToastUtil.show(mContext, "终点未设置");
        }
        showProgressDialog();
        final RouteSearch.FromAndTo fromAndTo = new RouteSearch.FromAndTo(
                mStartPoint, mEndPoint);
        if (routeType == ROUTE_TYPE_DRIVE) {// 驾车路径规划
            RouteSearch.DriveRouteQuery query = new RouteSearch.DriveRouteQuery(fromAndTo, mode, null,
                    null, "");// 第一个参数表示路径规划的起点和终点，第二个参数表示驾车模式，第三个参数表示途经点，第四个参数表示避让区域，第五个参数表示避让道路
            mRouteSearch.calculateDriveRouteAsyn(query);// 异步路径规划驾车模式查询
        }
    }

    @Override
    public void onMapClick(LatLng latLng) {

    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        return false;
    }

    @Override
    public void onBusRouteSearched(BusRouteResult busRouteResult, int i) {

    }

    @Override
    public void onDriveRouteSearched(DriveRouteResult driveRouteResult, int errorCode) {
        dissmissProgressDialog();
        aMap.clear();// 清理地图上的所有覆盖物
        if (errorCode == 1000) {
            if (driveRouteResult != null && driveRouteResult.getPaths() != null) {
                if (driveRouteResult.getPaths().size() > 0) {
                    mDriveRouteResult = driveRouteResult;
                    final DrivePath drivePath = mDriveRouteResult.getPaths()
                            .get(0);
                    DrivingRouteOverlay drivingRouteOverlay = new DrivingRouteOverlay(
                            mContext, aMap, drivePath,
                            mDriveRouteResult.getStartPos(),
                            mDriveRouteResult.getTargetPos(), null);
                    drivingRouteOverlay.setNodeIconVisibility(false);//设置节点marker是否显示
                    drivingRouteOverlay.setIsColorfulline(true);//是否用颜色展示交通拥堵情况，默认true
                    drivingRouteOverlay.removeFromMap();
                    drivingRouteOverlay.addToMap();
                    drivingRouteOverlay.zoomToSpan();
                    mBottomLayout.setVisibility(View.VISIBLE);
                    int dis = (int) drivePath.getDistance();
                    int dur = (int) drivePath.getDuration();
                    String des = AMapUtil.getFriendlyTime(dur)+"("+AMapUtil.getFriendlyLength(dis)+")";
                    mRouteTimeDes.setText(des);
                    mRouteDetailDes.setVisibility(View.VISIBLE);
                    int taxiCost = (int) mDriveRouteResult.getTaxiCost();
                    mRouteDetailDes.setText("打车约"+taxiCost+"元");
                    mBottomLayout.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(mContext,
                                    DriveRouteDetailActivity.class);
                            intent.putExtra("drive_path", drivePath);
                            intent.putExtra("drive_result",
                                    mDriveRouteResult);
                            startActivity(intent);
                        }
                    });
                } else if (driveRouteResult != null && driveRouteResult.getPaths() == null) {
                    ToastUtil.show(mContext, R.string.no_result);
                }

            } else {
                ToastUtil.show(mContext, R.string.no_result);
            }
        } else {
            ToastUtil.showerror(this.getApplicationContext(), errorCode);
        }

    }

    @Override
    public void onWalkRouteSearched(WalkRouteResult walkRouteResult, int i) {

    }

    @Override
    public void onRideRouteSearched(RideRouteResult rideRouteResult, int i) {

    }

    /**
     * 显示进度框
     */
    private void showProgressDialog() {
        if (progDialog == null)
            progDialog = new ProgressDialog(this);
        progDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progDialog.setIndeterminate(false);
        progDialog.setCancelable(true);
        progDialog.setMessage("正在搜索...");
        progDialog.show();
    }

    /**
     * 隐藏进度框
     */
    private void dissmissProgressDialog() {
        if (progDialog != null) {
            progDialog.dismiss();
        }
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

}
