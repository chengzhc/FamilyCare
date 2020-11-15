package com.czstudio.familycare;

import android.app.Activity;
import android.util.Log;

import com.czstudio.czlibrary.CzSys_HTTP;
import com.czstudio.familycare.model.ModelUser;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.HashMap;

public class Constant {
    public static final String TAG="Constant";
    public static final String UMENG_AppKey="5db418bd570df32704000877";
    public static final String UMENG_MESSAGE_SECRET="d634122853e26a611b7a306423da2862";
    public static final String UMENG_APP_MASTER_SECRET="ksuatr5lsfuopx8jlpi0oqseu81femdm";
    public static final String SIGN_KEY="czstudio";

    public static Socket MainTcpSocket=null;
    public static String TCP_IP="home.chengzhen1971.top";
    public static int TCP_PORT=20000;

    public static final int
            GET_PICTURE_BY_CAMERA=1003,
            GET_PICTURE_BY_GALLERY=1004,
            CROP_SMALL_PICTURE=1005;

    public static OutputStream MainTcpOutStream;
    public static InputStream MainTcpInStream;

    public static boolean isTcpConnected=false;
    public static boolean isStopTCP=true;//控制是否断开TCP

    public static int statusBarColor=0xff0099cc;

    //系统设置

    public static Boolean isDebug=false;

    public static Boolean isTextUi=false;

    //服务器
    public static final String DOMAIN="http://home.chengzhen1971.top:82";
//    public static final String DOMAIN="http://114.95.73.163:82";
    public static final String DOMAIN_API=DOMAIN+"/module_data";


    //全局变量
    public static boolean isRecordDistance=false;
    public static int distance=0;
    public static long distanceRecordSeconeds=0;
    public static double lastLatitude=0;
    public static double lastLongitude=0;
    public static String currentSSID="";
    public static int family_num=1;


    public static void debugInfo(String tag,String info){
        if(isDebug){
            Log.e(tag,info);
        }
    }

    public static void sendUmengPersonalNotification(Activity activity, int uid, String tiker, String title, String text, CzSys_HTTP.HttpListener listener){
        String url=Constant.DOMAIN_API+"/mobile_app/send_person_notification?"
                +"to_uid="+uid
                +"&ticker="+tiker
                +"&title="+title
                +"&text="+text
                +"&token="+ ModelUser.getInstance(activity).getToken();
        CzSys_HTTP.requestPostCz(activity,url,new HashMap<String, String>(),listener);
    }

}
