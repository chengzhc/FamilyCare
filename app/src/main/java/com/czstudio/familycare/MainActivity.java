package com.czstudio.familycare;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import com.amap.api.location.AMapLocation;
import com.czstudio.czlibrary.CzAMapLocation;
import com.czstudio.czlibrary.CzLibrary;
import com.czstudio.czlibrary.CzMoveableView;
import com.czstudio.czlibrary.CzNetworkStatus;
import com.czstudio.czlibrary.service.StepCountService;
import com.czstudio.czlibrary.service.TcpService;
import com.czstudio.czlibrary.service.Tcpbind;
import com.czstudio.familycare.model.ModelUser;
import com.czstudio.familycare.model.TcpReceivePorcessor;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.squareup.picasso.Picasso;
import com.umeng.message.PushAgent;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    final String TAG = getClass().getName();

    static MainActivity instance;

    //联网状态相关
    CzNetworkStatus czNetworkStatus;

    //socket相关
    Tcpbind tcpbind;

    //定位相关
    CzAMapLocation czAMapLocation;
    int sameLocationCount = 0;

    //传感器相关
    StepCountService.StepCount stepCount; //计步

    //用户相关
    ModelUser modelUser;

    //UI
    int containerWidth,containerHeight,navViewWidth,navViewHeight;
    ConstraintLayout container;
    BottomNavigationView nav_view;
    CzMoveableView lay_net_type;
    ImageView iv_net_type;

    //debug
    ScrollView scroll_debug;
    TextView tv_debug;


    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //UMeng 必须
        PushAgent.getInstance(this).onAppStart();

        BottomNavigationView navView = findViewById(R.id.nav_view);

        initData();
        initView();
        initTcp();
        initStepSensor();

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupWithNavController(navView, navController);

        initLocation();
        initNetworkStatus();
    }

    @Override
    protected void onResume() {
        super.onResume();
        czAMapLocation.disableBackgroundLocation();
    }

    @Override
    protected void onStop() {
        super.onStop();
        czAMapLocation.enableBackgroundLocation();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }


    @Override
    public void onBackPressed() {
        moveTaskToBack(false);
    }

    public static MainActivity getInstance() {
        return instance;
    }

    void initData(){
        instance = this;
        modelUser=ModelUser.getInstance(instance);
    }

    void initView(){
        container = findViewById(R.id.container);

        nav_view=findViewById(R.id.nav_view);
        nav_view.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                if(right-left>0){
                    v.removeOnLayoutChangeListener(this);
                    navViewWidth=right-left;
                    navViewHeight=bottom-top;
                }
            }
        });
        lay_net_type=findViewById(R.id.lay_net_type);


        iv_net_type=findViewById(R.id.iv_net_type);

        scroll_debug=findViewById(R.id.scroll_debug);
        tv_debug=findViewById(R.id.tv_debug);
        scroll_debug.setVisibility(Constant.isDebug? View.VISIBLE:View.GONE);

        container.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                if(right-left>0){
                    v.removeOnLayoutChangeListener(this);
                    containerWidth=right-left;
                    containerHeight=bottom-top;
                    lay_net_type.setOffsetAndFrame(0,0,containerWidth,containerHeight-navViewHeight,0,0);
                }
            }
        });
    }

    void initTcp() {
        try {
            Intent tcpService = new Intent(MainActivity.this, TcpService.class);
            bindService(tcpService, new ServiceConnection() {
                @Override
                public void onServiceConnected(ComponentName name, IBinder service) {
                    Toast.makeText(instance, "Service Binded", Toast.LENGTH_SHORT).show();
                    tcpbind = (Tcpbind) service;
                    tcpbind.connectTcp(instance);
                    tcpbind.setTcpBinderListener(new Tcpbind.TcpBinderListener() {
                        @Override
                        public void onTcpConnectedStatus(boolean isConnected) {
                            //Toast.makeText(instance,"Service Binded/onTcpConnectedStatus",Toast.LENGTH_SHORT).show();
                            setTcpConnected(isConnected);
                        }

                        @Override
                        public void onTcpReceiver(String rxString) {
                            //Toast.makeText(instance,"Service Binded/onTcpReceiver",Toast.LENGTH_SHORT).show();
                            processTcpReceive(rxString);
                        }

                        @Override
                        public void onTcpHeartBeat(JSONObject heartBeatJson) {
                        }
                    });

                    //mybind.getString(); //获取到getString方法
                }

                @Override
                public void onServiceDisconnected(ComponentName name) {

                }
            }, BIND_AUTO_CREATE);
            startService(tcpService);
        } catch (Exception e) {
            Constant.debugInfo(TAG, "init TCP Service Exp:" + e);
        }
    }

    void initStepSensor(){
        try{
            Intent stepCountService = new Intent(this, StepCountService.class);
            bindService(stepCountService, new ServiceConnection() {
                @Override
                public void onServiceConnected(ComponentName name, IBinder service) {
                    stepCount=(StepCountService.StepCount) service;
                }

                @Override
                public void onServiceDisconnected(ComponentName name) {

                }
            }, BIND_AUTO_CREATE);
            startService(stepCountService);
        }catch (Exception e){
            Constant.debugInfo(TAG,"init Step Service Exp:"+e);
        }
    }

    void initLocation(){
        czAMapLocation=CzAMapLocation.getInstance(instance);
        czAMapLocation.addOnLocationChangeListener(new CzAMapLocation.OnLocantionChangeListener() {
            @Override
            public void onGetLocation(CzAMapLocation instance, AMapLocation location) {
                final int steps=stepCount==null?0:stepCount.getStepsFromLastCheck();
                setLocation(location.getLatitude(),location.getLongitude(),czNetworkStatus.getNetType(),steps);
            }

            @Override
            public void onGetLocationInfo(CzAMapLocation instance, String info) {

            }
        });
        container.postDelayed(new Runnable() {
            @Override
            public void run() {
                czAMapLocation.startLocation();
            }
        },1000);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    void initNetworkStatus(){
        czNetworkStatus=CzNetworkStatus.getInstance(instance);
        czNetworkStatus.addNetworkStatusListener(new CzNetworkStatus.NetworkStatusListener() {
            @Override
            public void onNetworkConnected(CzNetworkStatus czNetworkStatus, String netType) {
                if(netType.equals(CzNetworkStatus.NET_TYPE_WIFI_STR)){
                    Constant.currentSSID=czNetworkStatus.getWifiSSID();
                }else if(netType.equals(CzNetworkStatus.NET_TYPE_MOBILE_STR)){
                    Constant.currentSSID="";
                }else if(netType.equals(CzNetworkStatus.NET_TYPE_OTHER_STR)){
                    Constant.currentSSID="";
                }else{
                    Constant.currentSSID="";
                }

            }
        });
    }


    void setTcpConnected(boolean isConnect) {
        if (isConnect) {
            Constant.isTcpConnected = true;
            if(czNetworkStatus.getNetType().equals(CzNetworkStatus.NET_TYPE_WIFI_STR)){
                Picasso.with(iv_net_type.getContext()).load(R.drawable.ic_network_wifi_green_700_36dp)
                        .placeholder(R.drawable.ic_leak_add_green_700_24dp)
                        .error(R.drawable.ic_leak_add_green_700_24dp)
                        .into(iv_net_type);
            }else if(czNetworkStatus.getNetType().equals(CzNetworkStatus.NET_TYPE_MOBILE_STR)){
                Picasso.with(iv_net_type.getContext()).load(R.drawable.ic_network_cell_green_700_36dp)
                        .placeholder(R.drawable.ic_leak_add_green_700_24dp)
                        .error(R.drawable.ic_leak_add_green_700_24dp)
                        .into(iv_net_type);
            }else if(czNetworkStatus.getNetType().equals(CzNetworkStatus.NET_TYPE_OTHER_STR)){
                Picasso.with(iv_net_type.getContext()).load(R.drawable.ic_leak_add_green_700_24dp)
                        .placeholder(R.drawable.ic_leak_add_green_700_24dp)
                        .error(R.drawable.ic_leak_add_green_700_24dp)
                        .into(iv_net_type);
            }
        } else {
            Constant.MainTcpSocket = null;
            Constant.isTcpConnected = false;
            Picasso.with(iv_net_type.getContext()).load(R.drawable.ic_leak_remove_red_700_36dp)
                    .placeholder(R.drawable.ic_leak_remove_red_700_36dp)
                    .error(R.drawable.ic_leak_remove_red_700_36dp)
                    .into(iv_net_type);
        }
    }

    void processTcpReceive(final String msg) {
        debug("=== RX ==="+msg);
//        Log.e(TAG,"processTcpReceive get msg:"+msg);
        TcpReceivePorcessor.getInstance(this).processReceive(msg);

    }

    public void setLocation(double latitude, double longitude,String netType,int steps) {
        //debug(CzTcp.getCurrentTimeStr()+" get location:"+latitude+","+longitude);
        if (tcpbind == null) {
            //Constant.debugInfo(TAG,"setLocation / tcpbind == null");
            return;
        }
        if (tcpbind.tcp == null) {
            //Constant.debugInfo(TAG,"setLocation / tcpbind.tcp == null");
            return;
        }


        if(latitude==0||longitude==0){
            return;
        }

        double[] homeLocation=modelUser.getHomeLocation();
        int betteryPercent= CzLibrary.getBatteryPercent(instance);
        if(netType.equals(CzNetworkStatus.NET_TYPE_WIFI_STR)
                &&Constant.currentSSID.equals(modelUser.getHome_wifi())
                &&(homeLocation[0]!=0)
                &&(homeLocation[1]!=0)) {
            tcpbind.tcp.setLocation(homeLocation[0], homeLocation[1],betteryPercent,
                    netType,Constant.currentSSID,steps);
        }else{
            if(latitude==Constant.lastLatitude
                    && longitude==Constant.lastLongitude
                    && sameLocationCount<10){
                tcpbind.tcp.setSameLocation(betteryPercent,netType,
                        Constant.currentSSID,steps);
                sameLocationCount++;
            }else {
                tcpbind.tcp.setLocation(latitude, longitude,betteryPercent,netType,
                        Constant.currentSSID,steps);
                sameLocationCount=0;
            }
        }

        Constant.lastLatitude=latitude;
        Constant.lastLongitude=longitude;

        //Constant.debugInfo(TAG,"setLocation done");
    }

    public void debug(final String info) {
        if (!Constant.isDebug) {
            return;
        }
        tv_debug.setText(tv_debug.getText().toString()+"\n"+info);
        scroll_debug.postDelayed(new Runnable() {
            @Override
            public void run() {
                scroll_debug.scrollTo(0,tv_debug.getMeasuredHeight());
            }
        },100);


    }

    public void showDebugPanel(boolean isShow){
        if(isShow){
            scroll_debug.setVisibility(View.VISIBLE);
        }else{
            scroll_debug.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if(result != null) {
            if(result.getContents() == null) {
                //Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show();
                if(activityResultListenerArray!=null){
                    for(QRCodeResultListener listener:activityResultListenerArray){
                        if(listener!=null){
                            listener.onCancelScanResult(result.getContents());
                        }
                    }
                }
            } else {
                //Toast.makeText(this, "Scanned: " + result.getContents(), Toast.LENGTH_LONG).show();
                if(activityResultListenerArray!=null){
                    for(QRCodeResultListener listener:activityResultListenerArray){
                        if(listener!=null){
                            listener.onGetScanResult(result.getContents());
                        }
                    }
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    List<QRCodeResultListener> activityResultListenerArray=new ArrayList<QRCodeResultListener>();
    public interface QRCodeResultListener{
        public void onGetScanResult(String str);
        public void onCancelScanResult(String str);
    }
    public void addQrCodeResultListener(QRCodeResultListener listener){
        activityResultListenerArray.add(listener);
    }

}
