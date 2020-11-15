package com.czstudio.familycare.fragment;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import com.czstudio.czlibrary.CzLibrary;
import com.czstudio.czlibrary.CzSys_HTTP;
import com.czstudio.czlibrary.CzTimeSelector;
import com.czstudio.familycare.BuildConfig;
import com.czstudio.familycare.Constant;
import com.czstudio.familycare.R;
import com.czstudio.familycare.model.ModelUser;
import com.squareup.picasso.Picasso;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.HashMap;

public class MeFragment extends Fragment implements Switch.OnCheckedChangeListener, View.OnClickListener {


    final String TAG = getClass().getName();
    View root;
    public static MeFragment instance;


    TextView tv_name, tv_mobile,  tv_hw_serial, tv_version, tv_logout,
        tv_home_location,tv_home_wifi,tv_rebind_dev,tv_birthday;
    Switch switch_accept_care;
    ImageView iv_avatar,iv_name_edit,iv_avatar_preview,iv_change_avatar_cancel;
    EditText et_name_edit,et_blood;
    Button btn_name_confirm,btn_avatar_camera,btn_avatar_galley,btn_blood_confirm;

    ConstraintLayout lay_name_edit,lay_avatar_edit;
    LinearLayout  lay_name_edit_container,lay_birthday,lay_blood,lay_home_location,lay_home_wifi,lay_avatar_edit_container;

    boolean initDone = false;

    ModelUser modelUser;
    Bitmap photo;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        Constant.debugInfo(TAG, "onCreateView");
        root = inflater.inflate(R.layout.fragment_me, container, false);

        initData();
        modelUser.checkLogin();

        return root;
    }

    void initData() {
        instance = this;
        modelUser = ModelUser.getInstance(getActivity());
        modelUser.setOnLoginListener(new ModelUser.OnLoginListener() {
            @Override
            public void onLogin() {
                initView();
                switch_accept_care.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        initDone = true;
                    }
                }, 1000);
            }
        });
    }

    void initView() {
        switch_accept_care = root.findViewById(R.id.switch_accept_care);
        switch_accept_care.setOnCheckedChangeListener(this);
        switch_accept_care.setChecked(modelUser.getAccept_care());


        tv_logout = root.findViewById(R.id.tv_logout);
        tv_logout.setOnClickListener(this);

        tv_name = root.findViewById(R.id.tv_name);
        tv_name.setOnClickListener(this);
        tv_mobile = root.findViewById(R.id.tv_mobile);
        iv_avatar = root.findViewById(R.id.iv_avatar);
        iv_avatar.setOnClickListener(this);
        tv_rebind_dev=root.findViewById(R.id.tv_rebind_dev);
        tv_rebind_dev.setOnClickListener(this);
        tv_hw_serial = root.findViewById(R.id.tv_hw_serial);
        tv_version = root.findViewById(R.id.tv_version);

        lay_birthday= root.findViewById(R.id.lay_birthday);
        lay_birthday.setOnClickListener(this);
        tv_birthday= root.findViewById(R.id.tv_birthday);
        lay_blood= root.findViewById(R.id.lay_blood);
        lay_blood.setOnClickListener(this);
        et_blood= root.findViewById(R.id.et_blood);
        btn_blood_confirm=root.findViewById(R.id.btn_blood_confirm);
        btn_blood_confirm.setOnClickListener(this);

        lay_home_location=root.findViewById(R.id.lay_home_location);
        lay_home_location.setOnClickListener(this);
        tv_home_location=root.findViewById(R.id.tv_home_location);
        lay_home_wifi=root.findViewById(R.id.lay_home_wifi);
        lay_home_wifi.setOnClickListener(this);
        tv_home_wifi=root.findViewById(R.id.tv_home_wifi);

        iv_name_edit=root.findViewById(R.id.iv_name_edit);
        iv_name_edit.setOnClickListener(this);
        lay_name_edit=root.findViewById(R.id.lay_name_edit);
        lay_name_edit.setOnClickListener(this);
        lay_name_edit_container=root.findViewById(R.id.lay_name_edit_container);
        lay_name_edit_container.setOnClickListener(this);
        et_name_edit=root.findViewById(R.id.et_name_edit);
        btn_name_confirm=root.findViewById(R.id.btn_name_confirm);
        btn_name_confirm.setOnClickListener(this);

        lay_avatar_edit=root.findViewById(R.id.lay_avatar_edit);
        lay_avatar_edit.setOnClickListener(this);
        lay_avatar_edit_container=root.findViewById(R.id.lay_avatar_edit_container);
        iv_avatar_preview=root.findViewById(R.id.iv_avatar_preview);
        iv_avatar_preview.setOnClickListener(this);
        btn_avatar_camera=root.findViewById(R.id.btn_avatar_camera);
        btn_avatar_camera.setOnClickListener(this);
        btn_avatar_galley=root.findViewById(R.id.btn_avatar_gallery);
        btn_avatar_galley.setOnClickListener(this);
        iv_change_avatar_cancel=root.findViewById(R.id.iv_change_avatar_cancel);
        iv_change_avatar_cancel.setOnClickListener(this);

        //设置数据
        ModelUser user = modelUser;
        tv_name.setText(user.getName());
        tv_mobile.setText(CzLibrary.getSecretString(user.getMobile()));
        tv_hw_serial.setText(CzLibrary.getSecretString(user.getHw_serial()));
        tv_version.setText(BuildConfig.VERSION_NAME);

        double[] home_location=modelUser.getHomeLocation();
        tv_home_location.setText(home_location[0]+"\n"+home_location[1]);
        tv_home_wifi.setText(modelUser.getHome_wifi());

        tv_birthday.setText(modelUser.getBirth_y()+"年"+modelUser.getBirth_m()+"月"+modelUser.getBirth_d()+"日");
        et_blood.setText(modelUser.getBlood().toUpperCase());
        et_blood.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                btn_blood_confirm.setVisibility(View.VISIBLE);
                String blood=et_blood.getText().toString().toUpperCase();
                if(!blood.equals("")&&!blood.equals("A")
                        &&!blood.equals("B") &&!blood.equals("AB") &&!blood.equals("O")){
                    CzLibrary.alertConfirm(getContext(), "错误", "请输入常规血型", new CzLibrary.AlertCallBack() {
                        @Override
                        public void onAlertConfirm() {
                            et_blood.setText(modelUser.getBlood());
                        }

                        @Override
                        public void onAlertCancel() {
                            et_blood.setText(modelUser.getBlood());
                        }
                    });

                }
            }
        });

        showAvatar(Constant.DOMAIN + user.getAvatar());
    }

    void showAvatar(String fullPath){
        Picasso.with(root.getContext()).load(fullPath)
                .placeholder(R.drawable.icon_user)
                .error(R.drawable.icon_user)
//                    .centerCrop()
                .into(iv_avatar);

        Picasso.with(root.getContext()).load(fullPath)
                .placeholder(R.drawable.icon_user)
                .error(R.drawable.icon_user)
//                    .centerCrop()
                .into(iv_avatar_preview);
    }


    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (!initDone) {
            return;
        }
        switch (buttonView.getId()) {
            case R.id.switch_accept_care:
                setAcceptCareStatus(isChecked);
                break;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_logout:
                CzLibrary.alertConfirm(getActivity(), "提示", "确定要退出登录吗？", new CzLibrary.AlertCallBack() {
                    @Override
                    public void onAlertConfirm() {
                        modelUser.logout();
                    }

                    @Override
                    public void onAlertCancel() {

                    }
                });

                break;
            case R.id.tv_rebind_dev:
                CzLibrary.alertConfirm(instance.getContext(), "警告", "重新绑定设备，将清空当前所有足迹数据，确定要这样做吗？", new CzLibrary.AlertCallBack() {
                    @Override
                    public void onAlertConfirm() {
                        modelUser.rebindDevice(new CzSys_HTTP.HttpListener() {
                            @Override
                            public void onHttpSuccess(String data) {

                            }

                            @Override
                            public void onFeedBackSuccess(JSONObject feedBackData) {
                                try {
                                    tv_hw_serial.setText(CzLibrary.getSecretString(feedBackData.getJSONObject("data").getString("hw_serial")));
                                } catch (Exception e) {
                                    Constant.debugInfo(TAG, "onClick/onFeedBackSuccess Exp:" + e);
                                }
                            }
                        });
                    }

                    @Override
                    public void onAlertCancel() {

                    }
                });
                break;
            case R.id.lay_birthday:
                CzTimeSelector timeSelector = new CzTimeSelector(getActivity(), new CzTimeSelector.ResultHandler() {
                    @Override
                    public void handle(final String time) {
                        CzLibrary.alertConfirm(getContext(), "提示", "确定要更新生日吗？", new CzLibrary.AlertCallBack() {
                            @Override
                            public void onAlertConfirm() {
                                try {
                                    String year = time.substring(0, 4);
                                    String month = time.substring(5, 7);
                                    String day = time.substring(8, 10);

                                    changeBirth(Integer.parseInt(year), Integer.parseInt(month), Integer.parseInt(day));
                                }catch (Exception e){
                                    Toast.makeText(getContext(),"CzTimeSelector/handle Exp:"+e,Toast.LENGTH_LONG).show();
                                    Log.e(TAG,"CzTimeSelector/handle Exp:"+e);
                                }
                            }

                            @Override
                            public void onAlertCancel() {

                            }
                        });

                    }
                }, "1900-01-01 00:00:00", "2050-01-01 00:00:00");
                timeSelector.setMode(CzTimeSelector.MODE.YMD);
                timeSelector.show(System.currentTimeMillis()/1000);
                break;
            case R.id.lay_blood:
                break;
            case R.id.lay_home_location:
                CzLibrary.alertConfirm(instance.getActivity(), "提示", "更新回家定位可提高在家时定位的扰动，防止家人误判您的当前位置，确定要更新吗?", new CzLibrary.AlertCallBack() {
                    @Override
                    public void onAlertConfirm() {
                        confirmHomeLocation();
                    }

                    @Override
                    public void onAlertCancel() {

                    }
                });
                break;
            case R.id.lay_home_wifi:
                if(Constant.currentSSID.length()==0){
                    CzLibrary.alert(instance.getActivity(),"您还未连接WIFI");
                    return;
                }
                CzLibrary.alertConfirm(instance.getActivity(), "提示", "当前SSID为"+Constant.currentSSID+"，确定要更新吗?", new CzLibrary.AlertCallBack() {
                    @Override
                    public void onAlertConfirm() {
                        confirmHomeWifi();
                    }

                    @Override
                    public void onAlertCancel() {

                    }
                });
                break;

            case R.id.iv_name_edit:
                showNameEditLay(true);
                break;
            case R.id.lay_name_edit:
                showNameEditLay(false);
                break;
            case R.id.lay_name_edit_container:
                //防穿透
                break;
            case R.id.btn_name_confirm:
                CzLibrary.alertConfirm(getContext(), "提示", "确定要修改名字吗？", new CzLibrary.AlertCallBack() {
                    @Override
                    public void onAlertConfirm() {
                        changeName();
                    }

                    @Override
                    public void onAlertCancel() {

                    }
                });
                break;

            case R.id.iv_avatar:
                showAvatarEditLay(true);
                break;
            case R.id.lay_avatar_edit:
                showAvatarEditLay(false);
                break;
            case R.id.lay_avatar_edit_container:
                //防穿透
                break;
            case R.id.btn_avatar_camera:
                takePhoto();
                break;
            case R.id.btn_avatar_gallery:
                selectGallery();
                break;
            case R.id.iv_change_avatar_cancel:
                showAvatarEditLay(false);
                break;
            case R.id.btn_blood_confirm:
                CzLibrary.alertConfirm(getContext(), "提示", "确定要修改血型吗", new CzLibrary.AlertCallBack() {
                    @Override
                    public void onAlertConfirm() {
                        changeBlood();
                    }

                    @Override
                    public void onAlertCancel() {
                        et_blood.setText(modelUser.getBlood().toUpperCase());
                        btn_blood_confirm.setVisibility(View.GONE);
                    }
                });

                break;
        }
    }

    void setAcceptCareStatus(final boolean isChecked) {
        String url = Constant.DOMAIN_API + "/mobile_app/accept_care?"
                + "accept_care=" + (isChecked ? 1 : 0)
                + "&token=" + modelUser.getToken();
        CzSys_HTTP.requestPostCz(instance.getActivity(), url, new HashMap<String, String>(),
                new CzSys_HTTP.HttpListener() {
                    @Override
                    public void onHttpSuccess(String data) {

                    }

                    @Override
                    public void onFeedBackSuccess(JSONObject feedBackData) {
                        setAcceptCare(isChecked);
                        Toast.makeText(instance.getContext(), "接受监护状态设置成功", Toast.LENGTH_SHORT).show();
                    }
                });
    }


    void confirmHomeLocation(){
        //以后要跳到单独页进行设置
        String url=Constant.DOMAIN_API+"/mobile_app/set_home_location?"
                +"home_lati_int="+Constant.lastLatitude*10000000
                +"&home_longi_int="+Constant.lastLongitude*10000000
                +"&token="+modelUser.getToken();
        CzSys_HTTP.requestPostCz(getActivity(), url, new HashMap<String, String>(), new CzSys_HTTP.HttpListener() {
            @Override
            public void onHttpSuccess(String data) {

            }

            @Override
            public void onFeedBackSuccess(JSONObject feedBackData) {
                Toast.makeText(instance.getActivity(),"更新成功",Toast.LENGTH_LONG).show();
                modelUser.setHomeLocation(Constant.lastLatitude,Constant.lastLongitude);
                tv_home_location.setText(""+Constant.lastLatitude+"\n"+Constant.lastLongitude);
            }
        });

    }

    void confirmHomeWifi(){
        //以后要跳到单独页进行设置
        String url=Constant.DOMAIN_API+"/mobile_app/set_home_wifi?"
                +"home_wifi="+Constant.currentSSID
                +"&token="+modelUser.getToken();
        CzSys_HTTP.requestPostCz(getActivity(), url, new HashMap<String, String>(), new CzSys_HTTP.HttpListener() {
            @Override
            public void onHttpSuccess(String data) {

            }

            @Override
            public void onFeedBackSuccess(JSONObject feedBackData) {
                Toast.makeText(instance.getActivity(),"更新成功",Toast.LENGTH_LONG).show();
                modelUser.setHome_wifi(Constant.currentSSID);
                tv_home_wifi.setText(""+Constant.currentSSID);
            }
        });

    }

    public static void setAcceptCare(boolean isChecked) {
        ModelUser.getInstance(instance.getActivity()).setAccept_care(isChecked);
        ModelUser.getInstance(instance.getActivity()).getSettingSharedPref().edit().putBoolean("accept_care", isChecked).commit();
    }

    void changeBirth(final int year,final int month,final int day){
        String url=Constant.DOMAIN_API+"/mobile_app/change_birth?y="+year+"&m="+month+"&d="+day
                +"&token="+modelUser.getToken();
        CzSys_HTTP.requestPostCz(getActivity(), url, new HashMap<String, String>(), new CzSys_HTTP.HttpListener() {
            @Override
            public void onHttpSuccess(String data) {

            }

            @Override
            public void onFeedBackSuccess(JSONObject feedBackData) {
                tv_birthday.setText(year + "年" + month + "月" + day + "日");
            }
        });
    }

    void changeBlood(){
        String url=Constant.DOMAIN_API+"/mobile_app/change_blood?blood="+et_blood.getText().toString()
                +"&token="+modelUser.getToken();
        CzSys_HTTP.requestPostCz(getActivity(), url, new HashMap<String, String>(), new CzSys_HTTP.HttpListener() {
            @Override
            public void onHttpSuccess(String data) {

            }

            @Override
            public void onFeedBackSuccess(JSONObject feedBackData) {
                et_blood.setText(et_blood.getText().toString().toUpperCase());
                btn_blood_confirm.setVisibility(View.GONE);
            }
        });
    }

    void showAvatarEditLay(boolean isShow){
        if(isShow){
            lay_avatar_edit.setVisibility(View.VISIBLE);
        }else{
            lay_avatar_edit.setVisibility(View.GONE);
        }
    }

    void showNameEditLay(boolean isShow){
        if(isShow){
            lay_name_edit.setVisibility(View.VISIBLE);
        }else{
            lay_name_edit.setVisibility(View.GONE);
        }
    }

    void changeName(){
        String url=Constant.DOMAIN_API+"/mobile_app/change_name?name="+et_name_edit.getText().toString()
                    +"&token="+modelUser.getToken();
        CzSys_HTTP.requestPostCz(getActivity(), url, new HashMap<String, String>(), new CzSys_HTTP.HttpListener() {
            @Override
            public void onHttpSuccess(String data) {

            }

            @Override
            public void onFeedBackSuccess(JSONObject feedBackData) {
                String newName=et_name_edit.getText().toString();
                tv_name.setText(newName);
                modelUser.setName(newName);
                showNameEditLay(false);
            }
        });
    }

    void changeAvatar(){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        photo.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] imgByte = baos.toByteArray();

        String url=Constant.DOMAIN_API+"/mobile_app/change_avatar";
        HashMap<String, String> params=new HashMap<String, String>() ;
        params.put("image", Base64.encodeToString(imgByte,Base64.DEFAULT));
        params.put("token",modelUser.getToken());

        CzSys_HTTP.requestPostCz(getActivity(), url, params, new CzSys_HTTP.HttpListener() {
            @Override
            public void onHttpSuccess(String data) {

            }

            @Override
            public void onFeedBackSuccess(JSONObject feedBackData) {
                try{
                    JSONObject jobj=feedBackData.getJSONObject("data");
                    String avatar=jobj.getString("avatar");
                    modelUser.setAvatar(avatar);
                    showAvatar(Constant.DOMAIN+ avatar);
                    showAvatarEditLay(false);
                    Toast.makeText(getContext(),"头像更新成功",Toast.LENGTH_LONG).show();
                }catch (Exception e){
                    Log.e(TAG,"解析图片异常："+e);
                }
            }
        });

    }

    void takePhoto(){
        //用于保存调用相机拍照后所生成的文件
        tempFile = new File(Environment.getExternalStorageDirectory().getPath(), System.currentTimeMillis() + ".png");
        //跳转到调用系统相机
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        //判断版本
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {   //如果在Android7.0以上,使用FileProvider获取Uri
            intent.setFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            Uri contentUri = FileProvider.getUriForFile(getContext(), ".fileprovider", tempFile);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, contentUri);
            Log.e("getPicFromCamera", contentUri.toString());
        } else {    //否则使用Uri.fromFile(file)方法获取Uri
            intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(tempFile));
        }
        startActivityForResult(intent, Constant.GET_PICTURE_BY_CAMERA);
    }

    void selectGallery(){
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        photoPickerIntent.setType("image/*");
        startActivityForResult(photoPickerIntent, Constant.GET_PICTURE_BY_GALLERY);
    }

    File tempFile;
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        switch (requestCode) {
            case Constant.GET_PICTURE_BY_CAMERA:   //调用相机后返回
                if (resultCode == Activity.RESULT_OK) {
                    //用相机返回的照片去调用剪裁也需要对Uri进行处理
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        Uri contentUri = FileProvider.getUriForFile(getContext(), ".fileprovider", tempFile);
                        startPhotoZoom(contentUri);//开始对图片进行裁剪处理
                    } else {
                        startPhotoZoom(Uri.fromFile(tempFile));//开始对图片进行裁剪处理
                    }
                }
                break;
            case Constant.GET_PICTURE_BY_GALLERY:    //调用相册后返回
                if (resultCode == Activity.RESULT_OK) {
                    Uri uri = intent.getData();
                    startPhotoZoom(uri); // 开始对图片进行裁剪处理
                }
                break;
            case Constant.CROP_SMALL_PICTURE:  //调用剪裁后返回
                if (intent != null) {
                    // 让刚才选择裁剪得到的图片显示在界面上
                    photo =BitmapFactory.decodeFile(mFile);
                    iv_avatar_preview.setImageBitmap(photo);
                    CzLibrary.alertConfirm(getContext(), "提示", "确定要修改头像吗？", new CzLibrary.AlertCallBack() {
                        @Override
                        public void onAlertConfirm() {
                            changeAvatar();
                        }

                        @Override
                        public void onAlertCancel() {

                        }
                    });

                } else {
                    Log.e("data","data为空");
                }
                break;
        }
    }

    /**
     * 裁剪图片方法实现
     *
     * @param uri
     */
    protected void startPhotoZoom(Uri uri) {

        if (uri == null) {
            Log.e("tag", "The uri is not exist.");
        }
//        tempUri = uri;
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        intent.setDataAndType(uri, "image/*");
        // 设置裁剪
        intent.putExtra("crop", "true");
        // aspectX aspectY 是宽高的比例
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        // outputX outputY 是裁剪图片宽高
        intent.putExtra("outputX", 100);
        intent.putExtra("outputY", 100);
        intent.putExtra("return-data", false);
        File out = new File(getPath());
        if (!out.getParentFile().exists()) {
            out.getParentFile().mkdirs();
        }
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(out));
        startActivityForResult(intent, Constant.CROP_SMALL_PICTURE);
    }

    //裁剪后的地址
    String  mFile;
    public  String getPath() {
        //resize image to thumb
        if (mFile == null) {
            mFile = Environment.getExternalStorageDirectory() + "/" +"wode/"+ "outtemp.png";
        }
        return mFile;
    }
}