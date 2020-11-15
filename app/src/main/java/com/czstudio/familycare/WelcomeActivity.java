package com.czstudio.familycare;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.czstudio.czlibrary.CzLibrary;
import com.czstudio.czlibrary.CzSys_HTTP;
import com.czstudio.familycare.model.ModelUser;

import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class WelcomeActivity extends AppCompatActivity implements View.OnClickListener{
    String TAG=getClass().getSimpleName();
    static String URL_CHECK_ANDROID_UPDATE=
            Constant.DOMAIN_API + "/mobile_app/check_android_update";

    WelcomeActivity instance;
    RelativeLayout lay_root;

    ProgressBar progress_download;
    List<TextView> tvList;

    ConstraintLayout lay_down_version;
    TextView tv_version_info,tv_btn_down_confirm,tv_btn_down_cancel;

    String newVersionUrl;
    boolean isDownloadNewVersion=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //推入后台再启动，则跳过
        if ((getIntent().getFlags() & Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT) != 0) {
            finish();
            return;
        }

        setContentView(R.layout.activity_welcome);
        initData();
        initView();
        lay_root.postDelayed(new Runnable() {
            @Override
            public void run() {
                initPermission();
            }
        },1000);
    }

    void initData(){
        instance=this;
    }

    void initView(){
        progress_download=findViewById(R.id.progress_download);
        lay_root=findViewById(R.id.lay_root);
        //自己绘制首页
        lay_root.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                if((right-left)>0){
                    v.removeOnLayoutChangeListener(this);
                    setBackground();
                }
            }
        });
        lay_down_version=findViewById(R.id.lay_download_version);
        tv_version_info=findViewById(R.id.tv_version_info);
        tv_btn_down_confirm=findViewById(R.id.tv_btn_down_confirm);
        tv_btn_down_confirm.setOnClickListener(this);
        tv_btn_down_cancel=findViewById(R.id.tv_btn_down_cancel);
        tv_btn_down_cancel.setOnClickListener(this);
    }

    void setBackground(){
        int rectSize=200;
        int randomRange=rectSize/3;
        int row=1+lay_root.getHeight()/rectSize;
        int column=1+lay_root.getWidth()/rectSize;
        Constant.debugInfo(TAG,"setBackground row,column:"+row+" / "+column);

        tvList=new ArrayList<TextView>();

        for(int i=0;i<row;i++){
            for(int j=0;j<column;j++){
                TextView view=new TextView(lay_root.getContext());
                lay_root.addView(view);
                tvList.add(view);
                ViewGroup.LayoutParams lp=view.getLayoutParams();
                lp.height=(int)(rectSize+CzLibrary.getRandomBetween(0,randomRange));
                lp.width=(int)(rectSize+CzLibrary.getRandomBetween(0,randomRange));
                view.setLayoutParams(lp);
                view.setX(rectSize*j+CzLibrary.getRandomBetween(-randomRange,randomRange));
                view.setY(rectSize*i+CzLibrary.getRandomBetween(-randomRange,randomRange));
//                view.setText(""+i+"/"+j);

                view.setBackgroundColor(0x800099CC+CzLibrary.getRandomBetween(-randomRange,randomRange)*0x1000);
            }
        }
    }

    @NonNull
    private void initPermission() {
        String permissions[] = {android.Manifest.permission.RECORD_AUDIO,
                android.Manifest.permission.ACCESS_NETWORK_STATE,
                android.Manifest.permission.INTERNET,
                android.Manifest.permission.READ_PHONE_STATE,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                android.Manifest.permission.MODIFY_AUDIO_SETTINGS,
                android.Manifest.permission.WRITE_SETTINGS,
                android.Manifest.permission.ACCESS_WIFI_STATE,
                android.Manifest.permission.CHANGE_WIFI_STATE,
                android.Manifest.permission.CAMERA,

                android.Manifest.permission.ACCESS_COARSE_LOCATION,
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.MOUNT_UNMOUNT_FILESYSTEMS,
                android.Manifest.permission.RECORD_AUDIO,

//                android.Manifest.permission.ACCESS_WIFI_STATE,
                //android.Manifest.permission.ACCESS_NETWORK_STATE,
                //android.Manifest.permission.CHANGE_WIFI_STATE,
                //android.Manifest.permission.READ_PHONE_STATE,
                //android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                //android.Manifest.permission.INTERNET,

        };
        ArrayList<String> toApplyList = new ArrayList<String>();

        for (String perm : permissions) {
            if (PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(this, perm)) {
                toApplyList.add(perm);
                //进入到这里代表没有权限.
            }
        }
        String tmpList[] = new String[toApplyList.size()];
        if (!toApplyList.isEmpty()) {
            ActivityCompat.requestPermissions(this, toApplyList.toArray(tmpList), 123);
        }
    }

    /**
     * 权限回调
     *
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @NonNull
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        // 此处为android 6.0以上动态授权的回调，用户自行实现。
        Constant.debugInfo(TAG,"onRequestPermissionsResult / requestCode="+requestCode
                +" , permissions length="+permissions.length+" , grantResults.length="+grantResults.length);
        int length=permissions.length;
        if(grantResults.length>length){
            Constant.debugInfo(TAG,"grantResults Length > permissionlength");
        }else if(grantResults.length<length){
            Constant.debugInfo(TAG,"grantResults Length < permissionlength");
            length=grantResults.length;
        }else{
            Constant.debugInfo(TAG,"grantResults Length = permissionlength");
        }
        for(int i=0;i<length;i++){
            Constant.debugInfo(TAG,permissions[i]+"--->"+grantResults[i]);
        }

        initSystem();
    }

    void initSystem(){
        loadStorage();
        checkNewVersion();
    }

    void loadStorage(){
        ModelUser.getInstance(instance).loadUserInfo();
    }

    @NonNull
    void checkNewVersion() {
        CzSys_HTTP.requestPostCz(instance,URL_CHECK_ANDROID_UPDATE,
                new HashMap<String, String>(),
                new CzSys_HTTP.HttpListener() {
                    @Override
                    public void onHttpSuccess(String data) {

                    }

                    @Override
                    public void onFeedBackSuccess(JSONObject feedBackData) {
                        try {
                            JSONObject data = feedBackData.getJSONObject("data");
                            int version_code = Integer.parseInt(data.getString("version_code"));

                            if (version_code > BuildConfig.VERSION_CODE) {
                                JSONObject version_info=new JSONObject(data.getString("version_info"));
                                Log.e(TAG,"version_info="+version_info.toString());
                                newVersionUrl =version_info .getString("url")+version_info .getString("fn");
                                if (newVersionUrl.length() > 10) {
                                    showDownloadPad(true);
                                    tv_version_info.setText("版本号："+version_info .getString("v_str"));
                                }
                            } else {
                                Constant.debugInfo(TAG, "已是最新版本");
                                startActivity(new Intent(instance,MainActivity.class));
                                finish();

                            }

                        } catch (Exception e) {
                            CzLibrary.alert(instance, "版本信息获取异常"+e);
                        }
                    }
                });
    }

    void startDownloadNewVersion(String downloadUrl, final ProgressBar progress_download
    ) {
        startDownloadNewVersion(downloadUrl, progress_download
                , true);
    }

    @NonNull
    void startDownloadNewVersion(String downloadUrl, final ProgressBar progressBar
            , final boolean isFinishHideProgress) {
        if(isDownloadNewVersion){
            CzLibrary.alert(instance,"下载中，请勿重复点击");
            return;
        }
        isDownloadNewVersion=true;
        CzLibrary.verifyStoragePermissions(instance);

        CzSys_HTTP.downloadFile(instance, downloadUrl, "/download", new CzSys_HTTP.DownloadListener() {
            @Override
            public void downloadPercent(int percent) {
                //Log.e(TAG,"getDownload percent="+percent);
                if(progressBar==null){
                    return;
                }
                progressBar.setVisibility(View.VISIBLE);
                progressBar.setProgress(percent);
            }

            @Override
            public void downloadSuccess(final String localFilePath) {

                if (isFinishHideProgress) {
                    if(progressBar!=null) {
                        progressBar.setVisibility(View.GONE);
                    }
                }
                showDownloadPad(false);
                CzLibrary.alertConfirm(instance, "提示", "新版本下载完成，点击确定更新", new CzLibrary.AlertCallBack() {
                    @Override
                    public void onAlertConfirm() {
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        File file = new File(localFilePath);
                        Log.e(TAG,"downloaded newVersion path="+localFilePath);
                        if (Build.VERSION.SDK_INT >= 24) {
                            try {
                                Uri apkUri = FileProvider.getUriForFile(instance,
                                        ".fileprovider", file);
                                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
                            }catch (Exception e){
                                Log.e(TAG," startDownloadNewVersion apkUri Exp:"+e);
                            }
                        } else {
                            intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
                        }
                        startActivityForResult(intent, 0);
                    }

                    @Override
                    public void onAlertCancel() {

                    }
                });
                isDownloadNewVersion=false;
            }

            @Override
            public void downloadInfo(String info) {

            }

            @Override
            public void downloadFail(String failInfo) {
                isDownloadNewVersion=false;
                Toast.makeText(instance, "新版本下载失败:" + failInfo, Toast.LENGTH_LONG).show();
                Log.e(TAG,"新版本下载失败:" + failInfo);
                startActivity(new Intent(instance,MainActivity.class));
                finish();
            }
        });
    }

    void showDownloadPad(boolean isShow){
        lay_down_version.setVisibility(isShow?View.VISIBLE:View.GONE);
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.tv_btn_down_confirm:
                startDownloadNewVersion(newVersionUrl, progress_download);
                break;
            case R.id.tv_btn_down_cancel:
                startActivity(new Intent(instance,MainActivity.class));
                finish();
                break;
        }
    }
}
