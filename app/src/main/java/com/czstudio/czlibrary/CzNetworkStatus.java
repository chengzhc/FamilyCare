package com.czstudio.czlibrary;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.LinkProperties;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import java.util.ArrayList;

import static android.content.Context.WIFI_SERVICE;

/**
 * //联网状态相关
 * CzNetworkStatus czNetworkStatus;
 *
 *  @RequiresApi(api = Build.VERSION_CODES.O)
 *     void initNetworkStatus(){
 *         czNetworkStatus=CzNetworkStatus.getInstance(instance);
 *         czNetworkStatus.addNetworkStatusListener(new CzNetworkStatus.NetworkStatusListener() {
 *             @Override
 *             public void onNetworkConnected(CzNetworkStatus czNetworkStatus, String netType) {
 *                 if(netType.equals(CzNetworkStatus.NET_TYPE_WIFI_STR)){
 *                     Constant.currentSSID=czNetworkStatus.getWifiSSID();
 *                 }else if(netType.equals(CzNetworkStatus.NET_TYPE_MOBILE_STR)){
 *                     Constant.currentSSID="";
 *                 }else if(netType.equals(CzNetworkStatus.NET_TYPE_OTHER_STR)){
 *                     Constant.currentSSID="";
 *                 }else{
 *                     Constant.currentSSID="";
 *                 }
 *
 *             }
 *         });
 *     }
 */

public class CzNetworkStatus {
    static String TAG="CzNetworkStatus";
    static CzNetworkStatus instance ;
    Activity activity;

    public final static String
            NET_TYPE_NONE_STR="NONE",
            NET_TYPE_WIFI_STR="WIFI",
            NET_TYPE_MOBILE_STR="MOBILE",
            NET_TYPE_OTHER_STR="OTHER";
    ConnectivityManager connectivityManager;
    String netType= CzNetworkStatus.NET_TYPE_NONE_STR;

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static CzNetworkStatus getInstance(Activity activity){
        if(instance==null){
            instance=new CzNetworkStatus();
        }
        instance.activity=activity;

        instance.init();
        return instance;
    }
    @RequiresApi(api = Build.VERSION_CODES.O)
    void init(){
        connectivityManager = (ConnectivityManager) activity
                .getSystemService(Context.CONNECTIVITY_SERVICE);

        connectivityManager.registerDefaultNetworkCallback(new ConnectivityManager.NetworkCallback(){
            @Override
            public void onAvailable(Network network) {
                super.onAvailable(network);
                Log.e(TAG, "newwork onAvailable: "+network);
                NetworkCapabilities networkCapabilities=connectivityManager.getNetworkCapabilities(network);
                if (networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)) {
                    Log.e(TAG,"已获取联网方式");
                    if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                            networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI_AWARE)) {
                        sendConnectType(NET_TYPE_WIFI_STR);
                    } else if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                            networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) ) {
                        sendConnectType(NET_TYPE_MOBILE_STR);
                    } else{
                        sendConnectType(NET_TYPE_OTHER_STR);
                    }
                }else{
                    Log.e(TAG,"为获取联网方式");
                }
            }

            @Override
            public void onLosing(Network network, int maxMsToLive) {
                super.onLosing(network, maxMsToLive);
                Log.e(TAG, "newwork onLosing: "+network);
            }

            @Override
            public void onLost(Network network) {
                super.onLost(network);
                Log.e(TAG, "newwork onLost: "+network.toString());
            }

            @Override
            public void onUnavailable() {
                super.onUnavailable();
                Log.e(TAG, "newwork onUnavailable: ");
            }

            @Override
            public void onCapabilitiesChanged(Network network, NetworkCapabilities networkCapabilities) {
                super.onCapabilitiesChanged(network, networkCapabilities);

                // 表明此网络连接成功验证
                if (networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)) {
                    Log.e(TAG,"已获取联网方式");
                    if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                            networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI_AWARE)) {
                        sendConnectType(NET_TYPE_WIFI_STR);
                    } else if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                            networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) ) {
                        sendConnectType(NET_TYPE_MOBILE_STR);
                    } else{
                        sendConnectType(NET_TYPE_OTHER_STR);
                    }
                }else{
                    Log.e(TAG,"为获取联网方式");
                }
            }

            @Override
            public void onLinkPropertiesChanged(Network network, LinkProperties linkProperties) {
                super.onLinkPropertiesChanged(network, linkProperties);
                Log.e(TAG, "newwork onLinkPropertiesChanged: "+network);
            }
        });
    }

    public String  getNetType(){
        return netType;
    }

    public String getWifiSSID(){
        String ssid="";
        WifiManager wm = (WifiManager) activity.getApplicationContext().getSystemService(WIFI_SERVICE);
        if (wm != null) {
            WifiInfo winfo = wm.getConnectionInfo();
            if (winfo != null) {
                String s = winfo.getSSID();
                if (s.length() > 2 && s.charAt(0) == '"' && s.charAt(s.length() - 1) == '"') {
                    ssid=s.substring(1, s.length() - 1);
                }
            }
        }
        return ssid;
    }

    void sendConnectType(final String netType){
        this.netType=netType;
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(listenerArray!=null){
                    for (NetworkStatusListener listener:listenerArray) {
                        if(listener!=null){
                            listener.onNetworkConnected(instance,netType);
                        }
                    }
                }
            }
        });

    }

    public interface NetworkStatusListener{
        public void onNetworkConnected(CzNetworkStatus czNetworkStatus,String netType);
    }

    ArrayList<NetworkStatusListener>listenerArray=new ArrayList<NetworkStatusListener>();

    public void addNetworkStatusListener(NetworkStatusListener listener){
        listenerArray.add(listener);
    }
    public void removeNetworkStatusListener(NetworkStatusListener listener){
        listenerArray.remove(listener);
    }
}
