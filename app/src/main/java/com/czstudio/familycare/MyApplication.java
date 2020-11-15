package com.czstudio.familycare;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.czstudio.familycare.model.ModelUser;
import com.umeng.commonsdk.UMConfigure;
import com.umeng.message.IUmengRegisterCallback;
import com.umeng.message.PushAgent;

public class MyApplication extends Application {
    static final String TAG="MyApplication";
    private int count = 0;
    private Context mContext;

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;

        initUmengSDK();

        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
            }

            @Override
            public void onActivityStarted(Activity activity) {
                count ++;
            }

            @Override
            public void onActivityResumed(Activity activity) {
            }

            @Override
            public void onActivityPaused(Activity activity) {
            }

            @Override
            public void onActivityStopped(Activity activity) {
                if(count > 0) {
                    count--;
                }
            }

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

            }

            @Override
            public void onActivityDestroyed(Activity activity) {
            }
        });
    }

    /**
     * 判断app是否在后台
     * @return
     */
    public boolean isBackground(){
        if(count <= 0){
            return true;
        } else {
            return false;
        }
    }

    private void initUmengSDK(){
        Log.e(TAG,"initUmengSDK");
        UMConfigure.setLogEnabled(true);
        UMConfigure.init(mContext, Constant.UMENG_AppKey, "umeng", UMConfigure.DEVICE_TYPE_PHONE,
                Constant.UMENG_MESSAGE_SECRET);

        PushAgent.getInstance(this).register(new IUmengRegisterCallback(){

            @Override
            public void onSuccess(String s) {
                Log.e("walle", "--->>> onSuccess, s is " + s);
                //这里的s就是DeviceToken（44位字符串，类似AiMxvJMl3mSvx9C4JBK8uQ8i-uxiyX_22FB9tuj3Zb0C）
                ModelUser.UMengDevToken=s;
            }

            @Override
            public void onFailure(String s, String s1) {
                Log.e("walle", "--->>> onFailure, s is " + s + ", s1 is " + s1);
            }
        });
    }
}