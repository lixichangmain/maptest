package com.jimro.maptest;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMap;
import com.amap.api.maps.AMapUtils;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.TextureMapView;
import com.amap.api.maps.model.BitmapDescriptor;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.services.core.AMapException;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.route.BusRouteResult;
import com.amap.api.services.route.DrivePath;
import com.amap.api.services.route.DriveRouteResult;
import com.amap.api.services.route.RideRouteResult;
import com.amap.api.services.route.RouteSearch;
import com.amap.api.services.route.WalkRouteResult;


public class MainActivity extends AppCompatActivity implements LocationSource, AMapLocationListener,
        RouteSearch.OnRouteSearchListener, AMap.OnMapClickListener, AMap.OnMarkerClickListener, AMap.OnInfoWindowClickListener, AMap.InfoWindowAdapter {
    //以驾车方式出行
    private static final int ROUTE_CAR_TYPE = 0;
    //以公交方式出行
    private static final int ROUTE_BUS_TYPE = 1;
    //以不行的方式出行
    private static final int ROUTE_WALK_TYPE = 2;
    //以火车的方式出行
    private static final int ROUTE_TRAIN_TYPE = 3;
    private DriveRouteResult mDriveRouteResult;
    private OnLocationChangedListener mListener;
    private AMapLocationClient mlocationClient;
    private AMapLocationClientOption mLocationOption;
    private TextureMapView mapView;
    private AMap aMap;
    private TextView textView;
    private RouteSearch mRouteSearch;
    private RelativeLayout relativeRouteDetail;
    private TextView timeText;
    private TextView detailText;
    private RadioGroup mRouteGroup;
    private LatLonPoint mStartPoint = null;
    private LatLonPoint mEndPoint = new LatLonPoint(39.995576, 116.481288);//终点，116.481288,39.995576
    //    //起点的经纬度，由定位获得
//    private double startLatitude = 39.942295;
//    private double startLongitude = 116.335891;
//    //终点的经纬度，暂时获取不到好友的坐标，只能临时选一个定点
//    private double endLatitude = 40.818311;
//    private double endLongitude = 111.670801;
    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView = (TextView) findViewById(R.id.textView);

        //获取地图控件引用
        mapView = (TextureMapView) findViewById(R.id.map);
        //在activity执行onCreate时执行mMapView.onCreate(savedInstanceState)，创建地图
        mapView.onCreate(savedInstanceState);
        init();
        //设置七点到终点的标记
        setfromandtoMarker();
    }

    private void setfromandtoMarker() {
        if (mStartPoint != null && mEndPoint != null) {
            aMap.addMarker(new MarkerOptions()
                    .position(AMapUtil.convertToLatLng(mStartPoint))
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.start)));
            aMap.addMarker(new MarkerOptions()
                    .position(AMapUtil.convertToLatLng(mEndPoint))
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.end)));
        }

    }

    private void init() {
        //初始化地图控制器对象
        if (aMap == null) {
            aMap = mapView.getMap();
            aMap.setTrafficEnabled(true);// 显示实时交通状况
            //地图模式可选类型：MAP_TYPE_NORMAL,MAP_TYPE_SATELLITE,MAP_TYPE_NIGHT
            aMap.setMapType(AMap.MAP_TYPE_NORMAL);// 卫星地图模式
        }
        setUpMap();
        registerListener();
        mRouteSearch = new RouteSearch(this);
        mRouteSearch.setRouteSearchListener(this);

        relativeRouteDetail = (RelativeLayout) findViewById(R.id.relative_route_detail);
        timeText = (TextView) findViewById(R.id.route_time);
        detailText = (TextView) findViewById(R.id.route_detail);
        mRouteGroup = (RadioGroup) findViewById(R.id.group_route);

    }

    private void registerListener() {
        //设置点击地图的监听
        aMap.setOnMapClickListener(this);
        //设置点击标记的监听
        aMap.setOnMarkerClickListener(this);
        aMap.setOnInfoWindowClickListener(this);
        aMap.setInfoWindowAdapter(this);
    }

    /**
     * 设置一些amap的属性
     */
    private void setUpMap() {
        aMap.setLocationSource(this);// 设置定位监听
        aMap.getUiSettings().setMyLocationButtonEnabled(true);// 设置默认定位按钮是否显示
        aMap.setMyLocationEnabled(true);// 设置为true表示显示定位层并可触发定位，false表示隐藏定位层并不可触发定位，默认是false
        // 设置定位的类型为定位模式 ，可以由定位、跟随或地图根据面向方向旋转几种
        aMap.setMyLocationType(AMap.LOCATION_TYPE_MAP_ROTATE);
    }


    public void onClick(View view) {
        RadioButton radioButton = null;
        if (view.getId() != R.id.xiangqing && view.getId() != R.id.xiangqing_more) {
            radioButton = (RadioButton) view;
        }
        relativeRouteDetail.setVisibility(View.GONE);
        switch (view.getId()) {
            case R.id.route_car:
                radioButton.setChecked(radioButton.isChecked());
                //点击驾车之后开始规划路线
                searchRouteResult(ROUTE_CAR_TYPE, RouteSearch.DrivingDefault);
                break;
            case R.id.route_bus:
                radioButton.setChecked(radioButton.isChecked());
                break;
            case R.id.route_walk:
                radioButton.setChecked(radioButton.isChecked());
                break;
            case R.id.route_train:
                radioButton.setChecked(radioButton.isChecked());
                break;
            case R.id.xiangqing:
            case R.id.xiangqing_more:
                break;
        }

        for (int i = 0; i < mRouteGroup.getChildCount(); i++) {
            if (mRouteGroup.getChildAt(i).getId() == view.getId()) {
                ((RadioButton) mRouteGroup.getChildAt(i)).setTextColor(Color.WHITE);
            } else {
                ((RadioButton) mRouteGroup.getChildAt(i)).setTextColor(Color.BLACK);
            }
        }
    }

    /**
     * 搜索路线规划路线
     *
     * @param routeType
     * @param mode
     */
    public void searchRouteResult(int routeType, int mode) {
        if (mStartPoint == null) {
            Toast.makeText(this, "获取当前定位失败", Toast.LENGTH_SHORT).show();
        }
        if (mEndPoint == null) {
            Toast.makeText(this, "获取对方位置失败", Toast.LENGTH_SHORT).show();
        }
        //开始规划路线，显示进度条
        showProgressDialog();
        RouteSearch.FromAndTo fromAndTo = new RouteSearch.FromAndTo(
                mStartPoint,
                mEndPoint);
        switch (routeType) {
            case ROUTE_BUS_TYPE:
                break;
            case ROUTE_CAR_TYPE:
                // 驾车路径规划
                RouteSearch.DriveRouteQuery query = new RouteSearch.DriveRouteQuery(fromAndTo, mode, null,
                        null, "");// 第一个参数表示路径规划的起点和终点，第二个参数表示驾车模式，第三个参数表示途经点，第四个参数表示避让区域，第五个参数表示避让道路
                mRouteSearch.calculateDriveRouteAsyn(query);

                break;
            case ROUTE_WALK_TYPE:
                break;
            case ROUTE_TRAIN_TYPE:
                break;
        }
    }

    /**
     * 显示搜索进度条
     */

    private void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            //设置进度条的样式
            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mProgressDialog.setIndeterminate(true);
            mProgressDialog.setMessage("路线正在规划中...");
            mProgressDialog.show();
        }
    }

    /**
     * 设置搜索进度条消失
     */
    private void dismissProgressDialog() {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
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
        deactivate();
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
        if (mlocationClient != null) {
            mlocationClient.onDestroy();
        }
    }


    /**
     * 激活定位
     */
    @Override
    public void activate(OnLocationChangedListener listener) {
        mListener = listener;
        if (mlocationClient == null) {
            mlocationClient = new AMapLocationClient(this);
            mLocationOption = new AMapLocationClientOption();
            //设置定位监听
            mlocationClient.setLocationListener(this);
            //设置为高精度定位模式
            mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
            mLocationOption.setInterval(2000);
            //设置定位参数
            mlocationClient.setLocationOption(mLocationOption);
            // 此方法为每隔固定时间会发起一次定位请求，为了减少电量消耗或网络流量消耗，
            // 注意设置合适的定位时间的间隔（最小间隔支持为2000ms），并且在合适时间调用stopLocation()方法来取消定位请求
            // 在定位结束后，在合适的生命周期调用onDestroy()方法
            // 在单次定位情况下，定位无论成功与否，都无需调用stopLocation()方法移除请求，定位sdk内部会移除
            mlocationClient.startLocation();
        }
    }

    /**
     * 停止定位
     */
    @Override
    public void deactivate() {
        mListener = null;
        if (mlocationClient != null) {
            mlocationClient.stopLocation();
            mlocationClient.onDestroy();
        }
        mlocationClient = null;
    }

    @Override
    public void onLocationChanged(AMapLocation aMapLocation) {
        if (mListener != null && aMapLocation != null) {
            if (aMapLocation != null
                    && aMapLocation.getErrorCode() == 0) {

                mStartPoint.setLatitude(aMapLocation.getLatitude());
                mStartPoint.setLongitude(aMapLocation.getLongitude());
                //定位成功回调信息，设置相关消息
                aMapLocation.getLocationType();//获取当前定位结果来源，如网络定位结果，详见定位类型表
                aMapLocation.getAccuracy();//获取精度信息
                textView.setText("当前位置" + aMapLocation.getAddress()
                        + "\t经度" + aMapLocation.getLongitude()
                        + "\t纬度" + aMapLocation.getLatitude());
                mListener.onLocationChanged(aMapLocation);// 显示系统小蓝点
            } else {
                String errText = "定位失败," + aMapLocation.getErrorCode() + ": " + aMapLocation.getErrorInfo();
                Log.e("AmapErr", errText);
            }
        }
    }

    @Override
    public void onBusRouteSearched(BusRouteResult busRouteResult, int i) {
        //公交车路线
    }

    @Override
    public void onDriveRouteSearched(DriveRouteResult result, int errorCode) {
        //驾车路线
        dismissProgressDialog();
        aMap.clear();// 清理地图上的所有覆盖物
        if (errorCode == AMapException.CODE_AMAP_SUCCESS) {
            if (result != null && result.getPaths() != null) {
                if (result.getPaths().size() > 0) {
                    mDriveRouteResult = result;
                    final DrivePath drivePath = mDriveRouteResult.getPaths()
                            .get(0);
                    DrivingRouteOverlay drivingRouteOverlay = new DrivingRouteOverlay(
                            this, aMap, drivePath,
                            mDriveRouteResult.getStartPos(),
                            mDriveRouteResult.getTargetPos(), null);
                    drivingRouteOverlay.setNodeIconVisibility(false);//设置节点marker是否显示
                    drivingRouteOverlay.setIsColorfulline(true);//是否用颜色展示交通拥堵情况，默认true
                    drivingRouteOverlay.removeFromMap();
                    drivingRouteOverlay.addToMap();
                    drivingRouteOverlay.zoomToSpan();
                    relativeRouteDetail.setVisibility(View.VISIBLE);
                    int dis = (int) drivePath.getDistance();
                    int dur = (int) drivePath.getDuration();
                    String des = AMapUtil.getFriendlyTime(dur) + "(" + AMapUtil.getFriendlyLength(dis) + ")";
                    timeText.setText(des);
                    detailText.setVisibility(View.VISIBLE);
                    int taxiCost = (int) mDriveRouteResult.getTaxiCost();
                    detailText.setText("打车约" + taxiCost + "元");
                    relativeRouteDetail.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
//                            Intent intent = new Intent(this,
//                                    DriveRouteDetailActivity.class);
//                            intent.putExtra("drive_path", drivePath);
//                            intent.putExtra("drive_result",
//                                    mDriveRouteResult);
//                            startActivity(intent);
                        }
                    });
                } else if (result != null && result.getPaths() == null) {
                    Toast.makeText(this, "对不起，没有搜索到相关的数据", Toast.LENGTH_SHORT).show();
                }

            } else {
                Toast.makeText(this, "对不起，没有搜索到相关的数据", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, errorCode, Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void onWalkRouteSearched(WalkRouteResult walkRouteResult, int i) {
        //步行路线
    }

    @Override
    public void onRideRouteSearched(RideRouteResult rideRouteResult, int i) {
        // TODO Auto-generated method stub
    }

    //点击地图的监听
    @Override
    public void onMapClick(LatLng latLng) {

    }

    //点击标记的监听
    @Override
    public boolean onMarkerClick(Marker marker) {
        return false;
    }

    @Override
    public void onInfoWindowClick(Marker marker) {

    }

    @Override
    public View getInfoWindow(Marker marker) {
        return null;
    }

    @Override
    public View getInfoContents(Marker marker) {
        return null;
    }
}
