package com.czstudio.familycare.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.amap.api.maps2d.AMap;
import com.amap.api.maps2d.AMapUtils;
import com.amap.api.maps2d.CameraUpdate;
import com.amap.api.maps2d.CameraUpdateFactory;
import com.amap.api.maps2d.MapView;
import com.amap.api.maps2d.model.CameraPosition;
import com.amap.api.maps2d.model.LatLng;
import com.amap.api.maps2d.model.Marker;
import com.amap.api.maps2d.model.MarkerOptions;
import com.amap.api.maps2d.model.Polyline;
import com.amap.api.maps2d.model.PolylineOptions;
import com.czstudio.czlibrary.CzLibrary;
import com.czstudio.czlibrary.CzSys_HTTP;
import com.czstudio.familycare.Constant;
import com.czstudio.familycare.R;
import com.czstudio.familycare.adapter.Adapter_CareCamera;
import com.czstudio.familycare.model.ModelUser;
import com.czstudio.familycare.widget.BasicHeadFragment;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BeCarePersonActivity extends AppCompatActivity implements BasicHeadFragment.OnFragmentInteractionListener,View.OnClickListener{
    public static final int PAGE_TYPE_CAMERA=100,
            PAGE_TYPE_LOCATION=101,
            PAGE_TYPE_ALL=102;
    String TAG=getClass().getSimpleName();
    BeCarePersonActivity instance;

    int page_type=PAGE_TYPE_LOCATION;
    String name="";
    String sex="男";
    String avatar="";
    int care_uid=0;
    String be_care_room="";

    JSONObject personInfo;

    ConstraintLayout lay_body;
    LinearLayout lay_camera_title,lay_map_title;
    ImageView iv_camera_zoom,iv_map_zoom;


    TextView tv_last_time,tv_today_distance;
    int bodyHeight,titleCameraHeight,titleMapHeight,mapViewHeight;

    JSONArray list_camera=new JSONArray();
    JSONArray list_location=new JSONArray();

    Adapter_CareCamera adapter_careCamera=null;

    WebView wv_camera;
    String url;
    Map<String, String> extraHeaders;

    String start_time,end_time;

    MapView mMapView;
    AMap aMap;
    Polyline polyline;

    ImageView iv_back_local,iv_footprint_history;

    boolean isFirstLocal=true;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Constant.debugInfo(TAG,"onCreate");
        setContentView(R.layout.activity_be_care_person);
        initData();
        initView();


        lay_body.postDelayed(new Runnable() {
            @Override
            public void run() {
                getAllViewSize();
                setPageType(savedInstanceState);
            }
        },200);
    }

    void initData(){
        instance=this;
        Bundle extra=getIntent().getExtras();
        if(extra==null){
            Constant.debugInfo(TAG,"initData / extra==null");
            return;
        }
        String infoStr=extra.getString("person_info","");
        page_type=extra.getInt("page_type",PAGE_TYPE_LOCATION);
        if(infoStr.length()==0){
            Constant.debugInfo(TAG,"initData / infoStr==null");
            return;
        }
        try {
            Constant.debugInfo(TAG,"initData / personInfo = "+infoStr);

            personInfo = new JSONObject(infoStr);
            name = personInfo.getString("name");
            sex=personInfo.getInt("sex")==1?"男":"女";
            avatar=personInfo.getString("avatar");
            care_uid=personInfo.getInt("uid");
            be_care_room=personInfo.getString("be_care_room");
        }catch (Exception e){
            Constant.debugInfo(TAG,"initData Exp:"+e);
        }

    }

    void initView(){
        getWindow().setStatusBarColor(Constant.statusBarColor);
        BasicHeadFragment headFragment=BasicHeadFragment.getInstance(name , "刷新", new BasicHeadFragment.OnClickWidgetListener() {
            @Override
            public void onClickBack(BasicHeadFragment instance, ImageView iv) {
                finish();
            }

            @Override
            public void onClickRightMenu(BasicHeadFragment instance, TextView tv) {
                refreshCameraData();
                refreshLocationData();
            }
        });
        getSupportFragmentManager().beginTransaction().add(R.id.frag_head,headFragment).commit();

        lay_camera_title=findViewById(R.id.lay_camera_title);
        lay_camera_title.setOnClickListener(this);
        lay_map_title=findViewById(R.id.lay_map_title);
        lay_map_title.setOnClickListener(this);
        iv_camera_zoom=findViewById(R.id.iv_camera_zoom);
        iv_map_zoom=findViewById(R.id.iv_map_zoom);
        tv_last_time=findViewById(R.id.tv_last_time);
        lay_body=findViewById(R.id.lay_body);
        wv_camera=findViewById(R.id.wv_camera);
        mMapView = findViewById(R.id.map);

        iv_back_local=findViewById(R.id.iv_back_local);
        iv_back_local.setOnClickListener(this);

        iv_footprint_history=findViewById(R.id.iv_footprint_history);
        iv_footprint_history.setOnClickListener(this);

        tv_today_distance=findViewById(R.id.tv_today_distance);
    }

    void setMapView(Bundle savedInstanceState){

        //在activity执行onCreate时执行mMapView.onCreate(savedInstanceState)，创建地图
        mMapView.onCreate(savedInstanceState);
        //初始化地图控制器对象

        if (aMap == null) {
            aMap = mMapView.getMap();
        }
        CameraUpdate mCameraUpdate= CameraUpdateFactory.zoomTo(17);
        aMap.moveCamera(mCameraUpdate);
    }

    void getAllViewSize(){
        bodyHeight=lay_body.getHeight();
        titleCameraHeight=lay_camera_title.getHeight();
        titleMapHeight=lay_map_title.getHeight();
        mapViewHeight=mMapView.getHeight();
        Constant.debugInfo(TAG,bodyHeight+","+titleCameraHeight+","+titleMapHeight+","+mapViewHeight);
    }

    void setWebView(){

        url=Constant.DOMAIN_API+"/monitor/room_video?token="+ ModelUser.getInstance(instance).getToken()
                +"&room="+be_care_room;
        WebSettings settings = wv_camera.getSettings();
        settings.setUseWideViewPort(true);//调整到适合webview的大小，不过尽量不要用，有些手机有问题
        // 自适应 屏幕大小界面
        settings.setLoadWithOverviewMode(true);
        settings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
        // 设置出现缩放工具
        settings.setDisplayZoomControls(false);
        settings.setJavaScriptCanOpenWindowsAutomatically(true);//支持通过JS打开新窗口
        //设置WebView属性，能够执行Javascript脚本
        settings.setJavaScriptEnabled(true);//设置js可用
        settings.setDomStorageEnabled(true);
        settings.setAllowFileAccess(true);
        settings.setAppCacheEnabled(true);
        String path = getApplicationContext().getCacheDir().getAbsolutePath();
        settings.setAppCachePath(path);
        wv_camera.setHorizontalScrollBarEnabled(false);//不能水平滑动
        wv_camera.setHorizontalScrollbarOverlay(false);
        wv_camera.setVerticalScrollBarEnabled(false);
        wv_camera.setVerticalScrollbarOverlay(false);

        extraHeaders = new HashMap<String, String>();
        extraHeaders.put("Referer", "http://home.chengzhen1971.top");

        wv_camera.setWebViewClient(new BeCarePersonActivity.MyWebClient());
        wv_camera.setWebChromeClient(new BeCarePersonActivity.MyWebChromeClient());

    }

    void setPageType(Bundle savedInstanceState){
        switch(page_type){
            case PAGE_TYPE_ALL:
                lay_camera_title.setVisibility(View.VISIBLE);
                wv_camera.setVisibility(View.VISIBLE);
                lay_map_title.setVisibility(View.VISIBLE);
                mMapView.setVisibility(View.VISIBLE);
                break;
            case PAGE_TYPE_LOCATION:
                lay_camera_title.setVisibility(View.GONE);
                wv_camera.setVisibility(View.GONE);
                lay_map_title.setVisibility(View.GONE);
                mMapView.setVisibility(View.VISIBLE);
                ViewGroup.LayoutParams lp = mMapView.getLayoutParams();
                lp.height = bodyHeight;
                mMapView.setLayoutParams(lp);
                break;
            case PAGE_TYPE_CAMERA:
                lay_camera_title.setVisibility(View.GONE);
                wv_camera.setVisibility(View.VISIBLE);
                lay_map_title.setVisibility(View.GONE);
                mMapView.setVisibility(View.GONE);
                break;
        }

        if(page_type!=PAGE_TYPE_LOCATION) {
            setWebView();
            refreshCameraData();
            iv_back_local.setVisibility(View.GONE);
            iv_footprint_history.setVisibility(View.GONE);
            tv_today_distance.setVisibility(View.GONE);
        }

        if(page_type!=PAGE_TYPE_CAMERA) {
            setMapView(savedInstanceState);
            refreshLocationData();
            iv_back_local.setVisibility(View.VISIBLE);
            iv_footprint_history.setVisibility(View.VISIBLE);
            tv_today_distance.setVisibility(View.VISIBLE);
        }
    }

    void getLocationList(){
        final String url=Constant.DOMAIN_API+"/mobile_app/get_care_location?"
                +"token="+ModelUser.getInstance(instance).getToken()
                +"&care_uid="+care_uid;
        CzSys_HTTP.requestPostCz(instance, url, new HashMap<String, String>(),
                new CzSys_HTTP.HttpListener() {
                    @Override
                    public void onHttpSuccess(String data) {

                    }

                    @Override
                    public void onFeedBackSuccess(JSONObject feedBackData) {
                        try{
                            JSONObject dataJson=feedBackData.getJSONObject("data");
                            list_location=dataJson.getJSONArray("list_location");
                            start_time=dataJson.getString("start_time");
                            end_time=dataJson.getString("end_time");
                            Constant.debugInfo(TAG,"requestUrl="+url+"/rb:"+feedBackData.toString()
                                    +" / end_time="+end_time);
                            showLocation();
                        }catch (Exception e){
                            CzLibrary.alert(instance,"解析被监护人足迹异常："+e);
                        }
                    }
                });
    }


    void showLocation(){
        try {
            aMap.clear();
            if(isFirstLocal){
                //首次定位让地图显示当前位置，后面就用点击左下角返回来回到当前位置
                isFirstLocal=false;
                backLocal();
            }
            //画折线
            List<LatLng> points = new ArrayList<LatLng>();
            int length=list_location.length();
            double lastLati=0,lastLongi=0;
            double distance=0;
            int steps=0;
            for(int i=0;i<length;i++){
                JSONObject locaJson=list_location.getJSONObject(i);
                Constant.debugInfo(TAG, locaJson.toString());
                double latitude=locaJson.getInt("lt")/10000000.0;
                double longitude=locaJson.getInt("lg")/10000000.0;
                points.add(new LatLng(latitude, longitude));

                String net_type=locaJson.getString("nt");
                if((latitude>0)&&(longitude>0)&&(net_type.equals("MOBILE"))){
                    if((lastLati>0)||(lastLongi>0)){
                        float dist= AMapUtils.calculateLineDistance(new LatLng(lastLati,lastLongi),new LatLng(latitude,longitude));
                        distance+=dist;
                    }
                    lastLati=latitude;
                    lastLongi=longitude;
                }
                steps+=locaJson.getInt("stp");
            }
            int distTemp=(int)(steps*0.65/100);
            tv_today_distance.setText("今日步数:"+steps+" 约"+distTemp/10+"."+distTemp%10+"公里");

            polyline =aMap.addPolyline(new PolylineOptions().
                    addAll(points).width(5).color(Color.argb(128, 0xE0,0x00,0x30)));

            JSONObject locaJson = list_location.getJSONObject(0);
            Constant.debugInfo(TAG, locaJson.toString());
            double latitude = locaJson.getInt("lt") / 10000000.0;
            double longitude = locaJson.getInt("lg") / 10000000.0;
            int bat=locaJson.getInt("b") ;
            String stamp = locaJson.getString("t");

            LatLng latLng = new LatLng(latitude, longitude);
            final Marker marker = aMap.addMarker(
                    new MarkerOptions()
                            .position(latLng)
                            .title(CzLibrary.getTimeStringFromTimeStamp(stamp))
                            .snippet("最近位置,电量"+bat+"%")
                            .visible(true));
            marker.showInfoWindow();



        }catch (Exception e){
            Constant.debugInfo(TAG, "showLocation Exp:"+e);
        }

    }

    @Override
    public void onClick(View v) {
        ViewGroup.LayoutParams lp = mMapView.getLayoutParams();
        switch (v.getId()){
            case R.id.lay_camera_title:
                if(page_type!=PAGE_TYPE_ALL){
                    return;
                }
                if(mMapView.getHeight()==mapViewHeight) {
                    lp.height = 0;
                }else if(mMapView.getHeight()==0) {
                    lp.height = mapViewHeight;
                }else{
                    lp.height = mapViewHeight;
                }
                break;
            case R.id.lay_map_title:
                if(page_type!=PAGE_TYPE_ALL){
                    return;
                }
                if(mMapView.getHeight()==mapViewHeight) {
                    lp.height = bodyHeight - titleCameraHeight - titleMapHeight;
                }else if(mMapView.getHeight()==0) {
                    lp.height = mapViewHeight;
                }else{
                    lp.height = mapViewHeight;
                }
                break;
            case R.id.tv_last_time:
                showTimeSelectPanel();
                break;
            case R.id.iv_back_local:
                backLocal();
                break;
            case R.id.iv_footprint_history:
                gotoFootprintHistory();
                break;
        }
        mMapView.setLayoutParams(lp);
    }

    void refreshCameraData(){
        if(page_type!=PAGE_TYPE_LOCATION) {
            wv_camera.loadUrl(url, extraHeaders);
        }
    }

    void refreshLocationData(){
        if(page_type!=PAGE_TYPE_CAMERA){
            getLocationList();
        }
        wv_camera.postDelayed(new Runnable() {
            @Override
            public void run() {
                refreshLocationData();
            }
        },10000);
    }

    void showTimeSelectPanel(){
        //
    }

    void backLocal(){
        try {
            JSONObject locaJson = list_location.getJSONObject(0);
            Constant.debugInfo(TAG, locaJson.toString());
            double latitude = locaJson.getInt("lt") / 10000000.0;
            double longitude = locaJson.getInt("lg") / 10000000.0;
            int bat=locaJson.getInt("b") ;
            String stamp = locaJson.getString("t");

            LatLng latLng = new LatLng(latitude, longitude);
            CameraUpdate mCameraUpdate = CameraUpdateFactory.newCameraPosition(
                    new CameraPosition(latLng, 18, 0, 0));
            aMap.moveCamera(mCameraUpdate);
            final Marker marker = aMap.addMarker(
                    new MarkerOptions()
                            .position(latLng)
                            .title(CzLibrary.getTimeStringFromTimeStamp(stamp))
                            .snippet("最近位置,电量"+bat+"%")
                            .visible(true));
            marker.showInfoWindow();

            //显示时间段
            tv_last_time.setText(start_time.substring(0, 16) + " - " + end_time.substring(5, 16));
        }catch(Exception e){
            Constant.debugInfo(TAG,"backLocal Exp:"+e);
        }
    }

    void gotoFootprintHistory(){
        try {
            Intent intent= new Intent(instance, HistoryFootprintActivity.class);
            intent.putExtra("person_info", personInfo.toString());
            startActivity(intent);
        }catch (Exception e){
            CzLibrary.alert(instance,"个人信息获取异常，请稍后再试，或退出并重新登录APP");
        }
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }


    class MyWebClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            Log.e("WebAboutUsFragment"," class WebAboutUsFragment/shouldOverrideUrlLoading/url="+url);
            if (url == null) {
                return false;
            }
            Constant.debugInfo(TAG,"MyWebClient / shouldOverrideUrlLoading /url="+url);

            try {
                if (!url.startsWith("http://") && !url.startsWith("https://")) {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(intent);
                    return true;
                }
            } catch (Exception e) {//防止crash (如果手机上没有安装处理某个scheme开头的url的APP, 会导致crash)
                Log.e("WebAboutUsFragment"," shouldOverrideUrlLoading（url="+url+") Exp:"+e);
                return true;//没有安装该app时，返回true，表示拦截自定义链接，但不跳转，避免弹出上面的错误页面
            }
            //返回值是true的时候控制去WebView打开，为false调用系统浏览器或第三方浏览器
            Map<String, String> extraHeaders = new HashMap<String, String>();
            extraHeaders.put("Referer", "http://home.chengzhen1971.top");
            view.loadUrl(url,extraHeaders);
            return false;
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            Constant.debugInfo(TAG,"shouldOverrideUrlLoading / url="+request.getUrl());
            return shouldOverrideUrlLoading(view, request.getUrl().toString());
        }

        @Override
        public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
            //此方法是为了处理在5.0以上Htts的问题，必须加上
            handler.proceed();
        }
        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            Constant.debugInfo(TAG,"onPageStarted / url="+url);
        }
    }

    class MyWebChromeClient extends WebChromeClient {
        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            super.onProgressChanged(view, newProgress);
//            if (load_view == null) {
//                return;
//            }
//            load_view.setVisibility(View.VISIBLE);
//            if (newProgress == 100) {
//                load_view.setVisibility(View.GONE);
//            }
        }
    }

}
