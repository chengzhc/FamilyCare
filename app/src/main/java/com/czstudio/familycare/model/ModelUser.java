package com.czstudio.familycare.model;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.provider.Settings;
import android.util.Log;

import com.czstudio.czlibrary.CzLibrary;
import com.czstudio.czlibrary.CzSys_HTTP;
import com.czstudio.familycare.Constant;
import com.czstudio.familycare.activity.LoginActivity;

import org.json.JSONObject;

import java.util.HashMap;

public class ModelUser {
    String TAG = getClass().getSimpleName();
    static ModelUser instance;
    public static String UMengDevToken="";
    Activity activity;
    public static final int DEFAULT_BGM_REST_MIN=1;//背景音间隔默认3分钟

    public static ModelUser getInstance(Activity activity) {
        if (instance == null) {
            instance = new ModelUser();
        }
        instance.activity=activity;
        instance.loadUserInfo();
        return instance;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getHw_serial() {
        return hw_serial;
    }

    public void setHw_serial(String hw_serial) {
        this.hw_serial=hw_serial;
    }



    public String getRooms() {
        return rooms;
    }

    public void setRooms(String rooms) {
        this.rooms = rooms;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getBirth_y() {
        return birth_y;
    }

    public void setBirth_y(int birth_y) {
        this.birth_y = birth_y;
    }

    public int getBirth_m() {
        return birth_m;
    }

    public void setBirth_m(int birth_m) {
        this.birth_m = birth_m;
    }

    public int getBirth_d() {
        return birth_d;
    }

    public void setBirth_d(int birth_d) {
        this.birth_d = birth_d;
    }

    public String getBlood() {
        return blood;
    }

    public void setBlood(String blood) {
        this.blood = blood;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public Boolean getAccept_care() {
        return accept_care;
    }

    public void setAccept_care(Boolean accept_care) {
        this.accept_care = accept_care;
    }

    public String getSharedPrefNameForSetting() {
        return sharedPrefNameForSetting;
    }

    public void setSharedPrefNameForSetting(String sharedPrefNameForSetting) {
        this.sharedPrefNameForSetting = sharedPrefNameForSetting;
    }

    double home_lati_int,home_longi_int;

    public double[] getHomeLocation(){
        double[] homeLocal={home_lati_int/10000000.0,home_longi_int/10000000.0};
        return homeLocal;
    }

    public void setHomeLocation(double lati,double longi){
        home_lati_int=lati*10000000;
        home_longi_int=longi*10000000;
    }

    public String getHome_wifi() {
        return home_wifi;
    }

    public void setHome_wifi(String home_wifi) {
        this.home_wifi = home_wifi;
    }

    String home_wifi;



    //用户信息
    @SuppressLint("MissingPermission")
    String token = "";// Build.getSerial();
    String mobile = "";
    String hw_serial = "";
    String rooms = "";
    String username = "";
    String name="";
    int birth_y=1970;
    int birth_m=1;
    int birth_d=1;
    String blood="";
    int level = 1;
    String avatar = "";
    Boolean accept_care = false;
    int family1_id=0;
    int is_family1_manager=0;
    String family1_name="";
    int family2_id=0;
    int is_family2_manager=0;
    String family2_name="";

    public int getFamily1_id() {
        return family1_id;
    }

    public void setFamily1_id(int family1_id) {
        this.family1_id = family1_id;
    }

    public int getIs_family1_manager() {
        return is_family1_manager;
    }

    public void setIs_family1_manager(int is_family1_manager) {
        this.is_family1_manager = is_family1_manager;
    }
    public String getFamily1_name() {
        return family1_name;
    }

    public void setFamily1_name(String family1_name) {
        this.family1_name = family1_name;
    }



    public int getFamily2_id() {
        return family2_id;
    }

    public void setFamily2_id(int family2_id) {
        this.family2_id = family2_id;
    }

    public int getIs_family2_manager() {
        return is_family2_manager;
    }

    public void setIs_family2_manager(int is_family2_manager) {
        this.is_family2_manager = is_family2_manager;
    }
    public String getFamily2_name() {
        return family2_name;
    }

    public void setFamily2_name(String family2_name) {
        this.family2_name = family2_name;
    }
    String sharedPrefNameForSetting = "setting";

    public int getBgm_rest_dur_min() {
        return  getSettingSharedPref().getInt("bgm_rest_dur_min",DEFAULT_BGM_REST_MIN);
    }

    public void setBgm_rest_dur_min(int bgm_rest_dur_min) {
        getSettingSharedPref().edit().putInt("bgm_rest_dur_min",bgm_rest_dur_min).commit();
    }

    public void loadUserInfo() {
        if(activity==null){
            Log.e(TAG,"loadUserInfo / activity==null");
            return ;
        }
        token = getSettingSharedPref().getString("token", "");
        mobile = getSettingSharedPref().getString("mobile", "");
        hw_serial = getSettingSharedPref().getString("hw_serial", "");
        rooms = getSettingSharedPref().getString("rooms", "");
        username = getSettingSharedPref().getString("username", "");
        name = getSettingSharedPref().getString("name", "");
        birth_y = getSettingSharedPref().getInt("birth_y", 1970);
        birth_m = getSettingSharedPref().getInt("birth_m", 1);
        birth_d = getSettingSharedPref().getInt("birth_d", 1);
        blood = getSettingSharedPref().getString("blood", "");
        level = getSettingSharedPref().getInt("level", 1);
        avatar = getSettingSharedPref().getString("avatar", "");
        accept_care = getSettingSharedPref().getBoolean("accept_care", false);
        home_lati_int=getSettingSharedPref().getInt("home_lati_int", 0);
        home_longi_int=getSettingSharedPref().getInt("home_longi_int", 0);
        home_wifi=getSettingSharedPref().getString("home_wifi", "OTHER");
        family1_id=getSettingSharedPref().getInt("family1_id", 0);
        family1_name=getSettingSharedPref().getString("family1_name", "");
        is_family1_manager=getSettingSharedPref().getInt("is_family1_manager", 0);
        family2_id=getSettingSharedPref().getInt("family2_id", 0);
        family2_name=getSettingSharedPref().getString("family2_name", "");
        is_family2_manager=getSettingSharedPref().getInt("is_family2_manager", 0);
    }

    public void saveUserInfo( JSONObject userJson) {
        if(activity==null){
            Log.e(TAG,"saveUserInfo / activity==null");
            return ;
        }
        try {
            getSettingSharedPref().edit().putString("token", userJson.getString("token"))
                    .putString("mobile", userJson.getString("mobile"))
                    .putString("hw_serial", userJson.getString("hw_serial"))
                    .putString("rooms",userJson.getString("rooms"))
                    .putString("username", userJson.getString("username"))
                    .putString("name", userJson.getString("name"))
                    .putInt("birth_y", userJson.getInt("birth_y"))
                    .putInt("birth_m", userJson.getInt("birth_m"))
                    .putInt("birth_d", userJson.getInt("birth_d"))
                    .putString("blood",userJson.getString("blood"))
                    .putInt("level", userJson.getInt("level"))
                    .putString("avatar",userJson.getString("avatar"))
                    .putBoolean("accept_care", (userJson.getInt("accept_care")==1))
                    .putInt("home_lati_int",userJson.getInt("home_lati_int"))
                    .putInt("home_longi_int",userJson.getInt("home_longi_int"))
                    .putString("home_wifi",userJson.getString("home_wifi"))
                    .putInt("family1_id",userJson.getInt("family1_id"))
                    .putString("family1_name",userJson.getString("family1_name"))
                    .putInt("is_family1_manager",userJson.getInt("is_family1_manager"))
                    .putInt("family2_id",userJson.getInt("family2_id"))
                    .putString("family2_name",userJson.getString("family2_name"))
                    .putInt("is_family2_manager",userJson.getInt("is_family2_manager"))

                    .commit();
        } catch (Exception e) {
            Constant.debugInfo(TAG,"saveUserInfo Exp:"+e);
        }
        Constant.debugInfo(TAG,"saveUserInfo Done:");
    }

    public JSONObject getUserInfoJSON() throws Exception{
        JSONObject jobj=new JSONObject();
        SharedPreferences sp=getSettingSharedPref();
        jobj.put("token", sp.getString("token",""));
        jobj.put("mobile", sp.getString("mobile",""));
        jobj.put("hw_serial", sp.getString("hw_serial",""));
        jobj.put("rooms",sp.getString("rooms",""));
        jobj.put("username", sp.getString("username",""));
        jobj.put("name", sp.getString("name",""));
        jobj.put("birth_y", sp.getInt("birth_y",1970));
        jobj.put("birth_m", sp.getInt("birth_m",1));
        jobj.put("birth_d", sp.getInt("birth_d",1));
        jobj.put("blood",sp.getString("blood",""));
        jobj.put("level", sp.getInt("level",0));
        jobj.put("avatar",sp.getString("avatar",""));
        jobj.put("accept_care", (sp.getBoolean("accept_care",false)?1:0));
        jobj.put("home_lati_int",sp.getInt("home_lati_int",0));
        jobj.put("home_longi_int",sp.getInt("home_longi_int",0));
        jobj.put("home_wifi",sp.getString("home_wifi",""));
        jobj.put("family1_id",sp.getInt("family1_id",0));
        jobj.put("family1_name",sp.getString("family1_name",""));
        jobj.put("is_family1_manager",sp.getInt("is_family1_manager",0));
        jobj.put("family2_id",sp.getInt("family2_id",0));
        jobj.put("family2_name",sp.getString("family2_name",""));
        jobj.put("is_family2_manager",sp.getInt("is_family2_manager",0));
        return jobj;
    }

    public SharedPreferences getSettingSharedPref() {
        if(activity==null){
            Log.e(TAG,"getSettingSharedPref / activity==null");
            return null;
        }
        return activity.getSharedPreferences(sharedPrefNameForSetting, Context.MODE_PRIVATE);
    }

    ;

    public void saveToken( String tkn) {
        if(activity==null){
            Log.e(TAG,"saveToken / activity==null");
            return;
        }
        token = tkn;
        getSettingSharedPref().edit().putString("token", token).commit();
    }


    public void checkLogin() {
        if(activity==null){
            Log.e(TAG,"checkLogin / activity==null");
            return;
        }
        String url = Constant.DOMAIN_API + "/mobile_app/check_login?token=" + token+"&um_dev_token="+UMengDevToken;
        CzSys_HTTP.requestPostCz(activity, url, new HashMap<String, String>(), new CzSys_HTTP.HttpListener() {
            @Override
            public void onHttpSuccess(String data) {

            }

            @Override
            public void onFeedBackSuccess(JSONObject feedBackData) {
                boolean isLogin = false;
                try {
                    JSONObject dataJson = feedBackData.getJSONObject("data");
                    String is_loginStr = dataJson.getString("is_login");
                    if (is_loginStr.equals("1")) {
                        isLogin = true;
                        saveUserInfo(dataJson.getJSONObject("info"));
                        loadUserInfo();
                        if(onLoginListener!=null){
                            onLoginListener.onLogin();
                        }
                    }
                } catch (Exception e) {
                    Log.e(instance.TAG, "checkLogin Exp:" + e);
                    isLogin = false;
                }

                if (!isLogin) {
                    Constant.debugInfo(TAG, "checkLogin / goto LoginActivity");
                    activity.startActivity(new Intent(activity, LoginActivity.class));
                    activity.finish();
                }
            }
        });
    }

    public void logout() {
        if(activity==null){
            Log.e(TAG,"logout / activity==null");
            return;
        }
        token = "";
        getSettingSharedPref().edit().putString("token", "").commit();
        Constant.debugInfo(TAG, "checkLogin / goto LoginActivity");
        activity.startActivity(new Intent(activity, LoginActivity.class));
        activity.finish();
    }

    public void rebindDevice(CzSys_HTTP.HttpListener listener){
        if(activity==null){
            Log.e(TAG,"rebindDevice / activity==null");
            return;
        }

        String hw_serial=""+ Settings.System.getString(activity.getContentResolver(),
                Settings.Secure.ANDROID_ID);
        String sign=CzLibrary.md5Encode( Constant.SIGN_KEY+hw_serial);
        String url=Constant.DOMAIN_API+"/mobile_app/bind_device?"
                +"token="+token
                +"&hw_serial="+hw_serial
                +"&sign="+sign;
        CzSys_HTTP.requestPostCz(activity, url, new HashMap<String, String>(),
                listener);
    }

    public interface OnLoginListener{
        public void onLogin();
    }

    OnLoginListener onLoginListener=null;
    public void setOnLoginListener(OnLoginListener listener){
        onLoginListener=listener;
    }

}
