package com.czstudio.czlibrary;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;

import androidx.annotation.NonNull;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.location.AMapLocationQualityReport;
import com.czstudio.familycare.MyApplication;
import com.czstudio.familycare.R;

import java.util.ArrayList;
import java.util.List;

/**
 * CzAMapLocation czAMapLocation;
 *
 * private void initLocation(){
 *         czAMapLocation=CzAMapLocation.getInstance(instance);
 *         czAMapLocation.addOnLocationChangeListener(new CzAMapLocation.OnLocantionChangeListener() {
 *             @Override
 *             public void onGetLocation(CzAMapLocation instance, AMapLocation location) {
 *                 //获取到定位了
 *                 //final int steps=stepCount==null?0:stepCount.getStepsFromLastCheck();
 *                 //setLocation(location.getLatitude(),location.getLongitude(),czNetworkStatus.getNetType(),steps);
 *             }
 *
 *             @Override
 *             public void onGetLocationInfo(CzAMapLocation instance, String info) {
 *
 *             }
 *         });
 *
 *         //延时启动
 *         container.postDelayed(new Runnable() {
 *             @Override
 *             public void run() {
 *                 czAMapLocation.startLocation();
 *             }
 *         },1000);
 *     }
 */

public class CzAMapLocation {
    String TAG="CzAMapLocation";

    static CzAMapLocation instance=null;
    Activity activity;

    //定位相关
    private AMapLocationClient locationClient = null;
    private AMapLocationClientOption locationOption = null;
    AMapLocationListener aMapLocationListener;

    //handler
    Handler handler;

    public static CzAMapLocation getInstance(Activity activity){
        if(instance==null){
            instance=new CzAMapLocation();
        }
        instance.activity=activity;
        instance.initHandler();
        instance.initListener();
        instance.initLocation();
        return instance;
    }

    void initHandler(){
        handler=new Handler(){
            @Override
            public void handleMessage(@NonNull Message msg) {
                super.handleMessage(msg);

            }
        };
    }

    void initListener(){
        aMapLocationListener=new AMapLocationListener() {
            @Override
            public void onLocationChanged(AMapLocation location) {
                if (null != location) {

                    StringBuffer sb = new StringBuffer();
                    //errCode等于0代表定位成功，其他的为定位失败，具体的可以参照官网定位错误码说明
                    if(location.getErrorCode() == 0){
                        sb.append("定位成功" + "\n");
                        sb.append("定位类型: " + location.getLocationType() + "\n");
                        sb.append("经    度    : " + location.getLongitude() + "\n");
                        sb.append("纬    度    : " + location.getLatitude() + "\n");
                        sb.append("精    度    : " + location.getAccuracy() + "米" + "\n");
                        sb.append("提供者    : " + location.getProvider() + "\n");

                        sb.append("速    度    : " + location.getSpeed() + "米/秒" + "\n");
                        sb.append("角    度    : " + location.getBearing() + "\n");
                        // 获取当前提供定位服务的卫星个数
                        sb.append("星    数    : " + location.getSatellites() + "\n");
                        sb.append("国    家    : " + location.getCountry() + "\n");
                        sb.append("省            : " + location.getProvince() + "\n");
                        sb.append("市            : " + location.getCity() + "\n");
                        sb.append("城市编码 : " + location.getCityCode() + "\n");
                        sb.append("区            : " + location.getDistrict() + "\n");
                        sb.append("区域 码   : " + location.getAdCode() + "\n");
                        sb.append("地    址    : " + location.getAddress() + "\n");
                        sb.append("地    址    : " + location.getDescription() + "\n");
                        sb.append("兴趣点    : " + location.getPoiName() + "\n");
                        //定位完成的时间
                        sb.append("定位时间: " + UtilsAMap.formatUTC(location.getTime(), "yyyy-MM-dd HH:mm:ss") + "\n");
                    } else {
                        //定位失败
                        sb.append("定位失败" + "\n");
                        sb.append("错误码:" + location.getErrorCode() + "\n");
                        sb.append("错误信息:" + location.getErrorInfo() + "\n");
                        sb.append("错误描述:" + location.getLocationDetail() + "\n");
                    }
                    sb.append("***定位质量报告***").append("\n");
                    sb.append("* WIFI开关：").append(location.getLocationQualityReport().isWifiAble() ? "开启":"关闭").append("\n");
                    sb.append("* GPS状态：").append(getGPSStatusString(location.getLocationQualityReport().getGPSStatus())).append("\n");
                    sb.append("* GPS星数：").append(location.getLocationQualityReport().getGPSSatellites()).append("\n");
                    sb.append("****************").append("\n");
                    //定位之后的回调时间
                    sb.append("回调时间: " + UtilsAMap.formatUTC(System.currentTimeMillis(), "yyyy-MM-dd HH:mm:ss") + "\n");

                    //解析定位结果，
                    String result = sb.toString();
                    sendLocationCallBack(location);
                    sendLocationInfo(result);
                } else {
                    sendLocationInfo("定位失败，loc is null");

                }
            }
        };
    }

    void initLocation(){
//初始化client
        locationClient = new AMapLocationClient(activity.getApplicationContext());
        locationOption = new AMapLocationClientOption();
        locationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);//可选，设置定位模式，可选的模式有高精度、仅设备、仅网络。默认为高精度模式
        locationOption.setGpsFirst(false);//可选，设置是否gps优先，只在高精度模式下有效。默认关闭
        locationOption.setHttpTimeOut(30000);//可选，设置网络请求超时时间。默认为30秒。在仅设备模式下无效
        locationOption.setInterval(5000);//可选，设置定位间隔。默认为2秒
        locationOption.setNeedAddress(true);//可选，设置是否返回逆地理地址信息。默认是true
        locationOption.setOnceLocation(false);//可选，设置是否单次定位。默认是false
        locationOption.setOnceLocationLatest(false);//可选，设置是否等待wifi刷新，默认为false.如果设置为true,会自动变为单次定位，持续定位时不要使用
        AMapLocationClientOption.setLocationProtocol(AMapLocationClientOption.AMapLocationProtocol.HTTP);//可选， 设置网络请求的协议。可选HTTP或者HTTPS。默认为HTTP
        locationOption.setSensorEnable(false);//可选，设置是否使用传感器。默认是false
        locationOption.setWifiScan(true); //可选，设置是否开启wifi扫描。默认为true，如果设置为false会同时停止主动刷新，停止以后完全依赖于系统刷新，定位位置可能存在误差
        locationOption.setLocationCacheEnable(true); //可选，设置是否使用缓存定位，默认为true
        //设置定位参数
        locationClient.setLocationOption(locationOption);
        // 设置定位监听
        locationClient.setLocationListener(aMapLocationListener);
    }

    /**
     * 开始定位
     *
     * @since 2.8.0
     * @author hongming.wang
     *
     */
    public void startLocation(){
        // 设置定位参数
        locationClient.setLocationOption(locationOption);
        // 启动定位
        locationClient.startLocation();
    }

    void startBackgroundLocation(){
        if(null == locationClient){
            locationClient = new AMapLocationClient(activity);
        }
        //启动后台定位
        locationClient.enableBackgroundLocation(2001, buildNotification());
    }

    /**
     * 停止定位
     *
     * @since 2.8.0
     * @author hongming.wang
     *
     */
    private void stopLocation(){
        // 停止定位
        locationClient.stopLocation();
    }


    /**
     * 获取GPS状态的字符串
     * @param statusCode GPS状态码
     * @return
     */
    private String getGPSStatusString(int statusCode){
        String str = "";
        switch (statusCode){
            case AMapLocationQualityReport.GPS_STATUS_OK:
                str = "GPS状态正常";
                break;
            case AMapLocationQualityReport.GPS_STATUS_NOGPSPROVIDER:
                str = "手机中没有GPS Provider，无法进行GPS定位";
                break;
            case AMapLocationQualityReport.GPS_STATUS_OFF:
                str = "GPS关闭，建议开启GPS，提高定位质量";
                break;
            case AMapLocationQualityReport.GPS_STATUS_MODE_SAVING:
                str = "选择的定位模式中不包含GPS定位，建议选择包含GPS定位的模式，提高定位质量";
                break;
            case AMapLocationQualityReport.GPS_STATUS_NOGPSPERMISSION:
                str = "没有GPS定位权限，建议开启gps定位权限";
                break;
        }
        return str;
    }


    void sendLocationCallBack(AMapLocation location){
        if(onLocantionChangeListenerList!=null){
            for (OnLocantionChangeListener listener :onLocantionChangeListenerList){
                if(listener!=null){
                    listener.onGetLocation(instance,location);
                }
            }
        }
    }

    void sendLocationInfo(String locationInfo){
        if(onLocantionChangeListenerList!=null){
            for (OnLocantionChangeListener listener :onLocantionChangeListenerList){
                if(listener!=null){
                    listener.onGetLocationInfo(instance,locationInfo);
                }
            }
        }
    }

    private static final String NOTIFICATION_CHANNEL_NAME = "BackgroundLocation";
    private NotificationManager notificationManager = null;
    boolean isCreateChannel = false;
    @SuppressLint("NewApi")
    private Notification buildNotification() {

        Notification.Builder builder = null;
        Notification notification = null;
        if(android.os.Build.VERSION.SDK_INT >= 26) {
            //Android O上对Notification进行了修改，如果设置的targetSDKVersion>=26建议使用此种方式创建通知栏
            if (null == notificationManager) {
                notificationManager = (NotificationManager) activity.getSystemService(Context.NOTIFICATION_SERVICE);
            }
            String channelId = activity.getPackageName();
            if(!isCreateChannel) {
                NotificationChannel notificationChannel = new NotificationChannel(channelId,
                        NOTIFICATION_CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT);
                notificationChannel.enableLights(true);//是否在桌面icon右上角展示小圆点
                notificationChannel.setLightColor(Color.BLUE); //小圆点颜色
                notificationChannel.setShowBadge(true); //是否在久按桌面图标时显示此渠道的通知
                notificationManager.createNotificationChannel(notificationChannel);
                isCreateChannel = true;
            }
            builder = new Notification.Builder(activity.getApplicationContext(), channelId);
        } else {
            builder = new Notification.Builder(activity.getApplicationContext());
        }
        builder.setSmallIcon(R.drawable.icon)
                .setContentTitle(UtilsAMap.getAppName(activity))
                .setContentText("正在后台运行")
                .setWhen(System.currentTimeMillis());

        if (android.os.Build.VERSION.SDK_INT >= 16) {
            notification = builder.build();
        } else {
            return builder.getNotification();
        }
        return notification;
    }

    //切入前台后关闭后台定位功能
    public void disableBackgroundLocation(){
        if (null != locationClient) {
            locationClient.disableBackgroundLocation(true);
        }
    }

    public void enableBackgroundLocation(){
        boolean isBackground = ((MyApplication)activity.getApplication()).isBackground();
        //如果app已经切入到后台，启动后台定位功能
        if(isBackground){
            if(null != locationClient) {
                locationClient.enableBackgroundLocation(2001, buildNotification());
            }
        }
    }


    /**
     * 销毁定位
     *
     * @since 2.8.0
     * @author hongming.wang
     *
     */
    public void destroyLocation(){
        if (null != locationClient) {
            /**
             * 如果AMapLocationClient是在当前Activity实例化的，
             * 在Activity的onDestroy中一定要执行AMapLocationClient的onDestroy
             */
            locationClient.onDestroy();
            locationClient = null;
            locationOption = null;
        }
    }


    static List<OnLocantionChangeListener> onLocantionChangeListenerList = new ArrayList<OnLocantionChangeListener>();

    public interface OnLocantionChangeListener {
        public void onGetLocation(CzAMapLocation instance, AMapLocation location);
        public void onGetLocationInfo(CzAMapLocation instance , String info);
    }

    public void addOnLocationChangeListener(OnLocantionChangeListener listener) {
        onLocantionChangeListenerList.add(listener);
    }

    public void removeOnLocationChangeListener(OnLocantionChangeListener listener) {
        onLocantionChangeListenerList.remove(listener);
    }


}
