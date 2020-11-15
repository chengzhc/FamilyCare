package com.czstudio.czlibrary.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.PowerManager;

import com.czstudio.familycare.Constant;

/**
 * AndroidMenifest
 * <service
 *             android:name="com.czstudio.czlibrary.service.TcpService"
 *             android:enabled="true"
 *             android:exported="true" />
 *
 * //socket相关
 * Tcpbind tcpbind;
 *
 * void initTcp() {
 *         try {
 *             Intent tcpService = new Intent(MainActivity.this, TcpService.class);
 *             bindService(tcpService, new ServiceConnection() {
 *                 @Override
 *                 public void onServiceConnected(ComponentName name, IBinder service) {
 *                     Toast.makeText(instance, "Service Binded", Toast.LENGTH_SHORT).show();
 *                     tcpbind = (Tcpbind) service;
 *                     tcpbind.connectTcp(instance);
 *                     tcpbind.setTcpBinderListener(new Tcpbind.TcpBinderListener() {
 *                         @Override
 *                         public void onTcpConnectedStatus(boolean isConnected) {
 *                             //Toast.makeText(instance,"Service Binded/onTcpConnectedStatus",Toast.LENGTH_SHORT).show();
 *                             setTcpConnected(isConnected);
 *                         }
 *
 *                         @Override
 *                         public void onTcpReceiver(String rxString) {
 *                             //Toast.makeText(instance,"Service Binded/onTcpReceiver",Toast.LENGTH_SHORT).show();
 *                             showReceive(rxString);
 *                         }
 *
 *                         @Override
 *                         public void onTcpHeartBeat(JSONObject heartBeatJson) {
 *                         }
 *                     });
 *
 *                     //mybind.getString(); //获取到getString方法
 *                 }
 *
 *                 @Override
 *                 public void onServiceDisconnected(ComponentName name) {
 *
 *                 }
 *             }, BIND_AUTO_CREATE);
 *             startService(tcpService);
 *         } catch (Exception e) {
 *             Constant.debugInfo(TAG, "init TCP Service Exp:" + e);
 *         }
 *     }
 *
 *      @NonNull
 *
 *     void setTcpConnected(boolean isConnect) {
 *         if (isConnect) {
 *             Constant.isTcpConnected = true;
 *             if(czNetworkStatus.getNetType().equals(CzNetworkStatus.NET_TYPE_WIFI_STR)){
 *                 Picasso.with(iv_net_type.getContext()).load(R.drawable.ic_network_wifi_green_700_36dp)
 *                         .placeholder(R.drawable.ic_leak_add_green_700_24dp)
 *                         .error(R.drawable.ic_leak_add_green_700_24dp)
 *                         .into(iv_net_type);
 *             }else if(czNetworkStatus.getNetType().equals(CzNetworkStatus.NET_TYPE_MOBILE_STR)){
 *                 Picasso.with(iv_net_type.getContext()).load(R.drawable.ic_network_cell_green_700_36dp)
 *                         .placeholder(R.drawable.ic_leak_add_green_700_24dp)
 *                         .error(R.drawable.ic_leak_add_green_700_24dp)
 *                         .into(iv_net_type);
 *             }else if(czNetworkStatus.getNetType().equals(CzNetworkStatus.NET_TYPE_OTHER_STR)){
 *                 Picasso.with(iv_net_type.getContext()).load(R.drawable.ic_leak_add_green_700_24dp)
 *                         .placeholder(R.drawable.ic_leak_add_green_700_24dp)
 *                         .error(R.drawable.ic_leak_add_green_700_24dp)
 *                         .into(iv_net_type);
 *             }
 *         } else {
 *             Constant.MainTcpSocket = null;
 *             Constant.isTcpConnected = false;
 *             Picasso.with(iv_net_type.getContext()).load(R.drawable.ic_leak_remove_red_700_36dp)
 *                     .placeholder(R.drawable.ic_leak_remove_red_700_36dp)
 *                     .error(R.drawable.ic_leak_remove_red_700_36dp)
 *                     .into(iv_net_type);
 *         }
 *     }
 */

public class TcpService extends Service {
    static String TAG = "TcpService";
    PowerManager.WakeLock wakeLock;


    public TcpService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Constant.debugInfo("ServiceTest", "  ----->  onCreate");
//        wakeLock = ((PowerManager) getSystemService(
//                this.POWER_SERVICE)).newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);
//        if (wakeLock != null) {
//            wakeLock.acquire();//这句执行后，手机将不会休眠，直到执行wakeLock.release();方法
//        }

        PowerManager pm = (PowerManager) getSystemService(this.POWER_SERVICE);
        wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TcpService.class.getName());
        wakeLock.acquire();


    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Constant.debugInfo("ServiceTest", "  ----->  onStartCommand");
        return super.onStartCommand(intent, flags, startId);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        Constant.debugInfo("ServiceTest", "  ----->  onDestroy");
        if (wakeLock != null) {
            wakeLock.release();
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        PowerManager pm = (PowerManager) getSystemService(this.POWER_SERVICE);
        wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TcpService.class.getName());
        wakeLock.acquire();
        return new Tcpbind();
    }


}




