package com.czstudio.familycare.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import com.amap.api.location.AMapLocation;
import com.amap.api.maps2d.AMap;
import com.amap.api.maps2d.CameraUpdate;
import com.amap.api.maps2d.CameraUpdateFactory;
import com.amap.api.maps2d.MapView;
import com.amap.api.maps2d.model.LatLng;
import com.amap.api.maps2d.model.MyLocationStyle;
import com.czstudio.czlibrary.BaiduVoiceRecognize;
import com.czstudio.czlibrary.CzAMapLocation;
import com.czstudio.czlibrary.CzLibrary;
import com.czstudio.czlibrary.CzSys_HTTP;
import com.czstudio.familycare.Constant;
import com.czstudio.familycare.R;
import com.czstudio.familycare.activity.HistoryFeelActivity;
import com.czstudio.familycare.activity.HistoryFootprintActivity;
import com.czstudio.familycare.activity.HistoryHeartActivity;
import com.czstudio.familycare.model.ModelUser;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;

public class HomeFragment extends Fragment implements View.OnClickListener , Spinner.OnItemSelectedListener {
    final String TAG = getClass().getName();
    View root;
    public static HomeFragment instance;

    MapView mMapView = null;
    CzAMapLocation.OnLocantionChangeListener onLocationChangeListener;
    AMap aMap;
    Bundle savedInstanceState;
    ImageView iv_back_local;

    AMapLocation currentLocaltion;

    LinearLayout lay_btn_heart,lay_btn_sport,lay_btn_eat,lay_btn_feeling,lay_heart_content,lay_feel_content;
    ConstraintLayout lay_root, lay_float_panel,lay_btn_rec;
    ImageView iv_btn_rec,iv_btn_stop_rec;
    TextView tv_btn_heart_confirm,tv_btn_heart_cancel,tv_btn_heart_history,tv_heart_status,
            tv_heart_text,tv_today_steps,
            tv_btn_feel_confirm,tv_btn_feel_cancel,tv_btn_feel_history;
    EditText et_hi_presure,et_lo_presure,et_heart_rate;
    ModelUser modelUser;
    BaiduVoiceRecognize baiduVoiceRecognize;
    int hi_pressure,lo_pressure,heart_rate;

    JSONArray list_location;

    Spinner spinner_eat,spinner_tired,spinner_sweat,spinner_sleep;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        Constant.debugInfo(TAG,"onCreateView");
        root = inflater.inflate(R.layout.fragment_home, container, false);
        this.savedInstanceState=savedInstanceState;
        initData();
        initView();
        initMap(savedInstanceState);
        initLocationListener();
        refreshLocationData();
        return root;
    }

    void initData() {
        instance = this;
        modelUser=ModelUser.getInstance(instance.getActivity());
        modelUser.setOnLoginListener(new ModelUser.OnLoginListener() {
            @Override
            public void onLogin() {
                //do nothing
            }
        });
        modelUser.checkLogin();

        onLocationChangeListener= new CzAMapLocation.OnLocantionChangeListener() {
            @Override
            public void onGetLocation(CzAMapLocation instance, AMapLocation location) {
                currentLocaltion=location;
            }

            @Override
            public void onGetLocationInfo(CzAMapLocation instance, String info) {

            }
        };
    }

    void initView() {
        lay_root=root.findViewById(R.id.lay_root);
        lay_btn_heart=root.findViewById(R.id.lay_btn_heart);
        lay_btn_heart.setOnClickListener(this);
        lay_btn_sport=root.findViewById(R.id.lay_btn_sport);
        lay_btn_sport.setOnClickListener(this);
        lay_btn_eat=root.findViewById(R.id.lay_btn_eat);
        lay_btn_eat.setOnClickListener(this);
        lay_btn_eat.setAlpha(0.3f);
        lay_btn_feeling=root.findViewById(R.id.lay_btn_feeling);
        lay_btn_feeling.setOnClickListener(this);

        lay_float_panel=root.findViewById(R.id.lay_float_panel);
        lay_float_panel.setOnClickListener(this);
        lay_heart_content=root.findViewById(R.id.lay_heart_content);
        lay_heart_content.setOnClickListener(this);
        lay_btn_rec=root.findViewById(R.id.lay_btn_rec);
        lay_btn_rec.setOnClickListener(this);
        iv_btn_rec=root.findViewById(R.id.iv_btn_rec);
        iv_btn_stop_rec=root.findViewById(R.id.iv_btn_stop_rec);
        tv_heart_status=root.findViewById(R.id.tv_heart_status);
        tv_heart_text=root.findViewById(R.id.tv_heart_text);
        et_hi_presure=root.findViewById(R.id.et_hi_presure);
        et_lo_presure=root.findViewById(R.id.et_lo_presure);
        et_heart_rate=root.findViewById(R.id.et_heart_rate);
        tv_btn_heart_confirm=root.findViewById(R.id.tv_btn_heart_confirm);
        tv_btn_heart_confirm.setOnClickListener(this);
        tv_btn_heart_cancel=root.findViewById(R.id.tv_btn_heart_cancel);
        tv_btn_heart_cancel.setOnClickListener(this);
        tv_btn_heart_history=root.findViewById(R.id.tv_btn_heart_history);
        tv_btn_heart_history.setOnClickListener(this);

        tv_today_steps=root.findViewById(R.id.tv_today_steps);
        tv_today_steps.setOnClickListener(this);

        lay_feel_content=root.findViewById(R.id.lay_feel_content);
        lay_feel_content.setOnClickListener(this);
        tv_btn_feel_confirm=root.findViewById(R.id.tv_btn_feel_confirm);
        tv_btn_feel_confirm.setOnClickListener(this);
        tv_btn_feel_cancel=root.findViewById(R.id.tv_btn_feel_cancel);
        tv_btn_feel_cancel.setOnClickListener(this);
        tv_btn_feel_history=root.findViewById(R.id.tv_btn_feel_history);
        tv_btn_feel_history.setOnClickListener(this);

        spinner_eat=root.findViewById(R.id.spinner_eat);
        spinner_eat.setOnItemSelectedListener(this);
        spinner_eat.setSelection(2);
        spinner_tired=root.findViewById(R.id.spinner_tired);
        spinner_tired.setOnItemSelectedListener(this);
        spinner_tired.setSelection(2);
        spinner_sweat=root.findViewById(R.id.spinner_sweat);
        spinner_sweat.setOnItemSelectedListener(this);
        spinner_sweat.setSelection(2);
        spinner_sleep=root.findViewById(R.id.spinner_sleep);
        spinner_sleep.setOnItemSelectedListener(this);
        spinner_sleep.setSelection(2);

    }

    void initMap(Bundle savedInstanceState) {
        //获取地图控件引用
        mMapView = root.findViewById(R.id.map);
        //在activity执行onCreate时执行mMapView.onCreate(savedInstanceState)，创建地图
        mMapView.onCreate(savedInstanceState);
        //初始化地图控制器对象

        if (aMap == null) {
            aMap = mMapView.getMap();
        }

        MyLocationStyle myLocationStyle;
        myLocationStyle = new MyLocationStyle();//初始化定位蓝点样式类myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE);//连续定位、且将视角移动到地图中心点，定位点依照设备方向旋转，并且会跟随设备移动。（1秒1次定位）如果不设置myLocationType，默认也会执行此种模式。
        myLocationStyle.interval(2000); //设置连续定位模式下的定位间隔，只在连续定位模式下生效，单次定位模式下不会生效。单位为毫秒。
        aMap.setMyLocationStyle(myLocationStyle);//设置定位蓝点的Style
//aMap.getUiSettings().setMyLocationButtonEnabled(true);设置默认定位按钮是否显示，非必需设置。
        aMap.setMyLocationEnabled(true);// 设置为true表示启动显示定位蓝点，false表示隐藏定位蓝点并不进行定位，默认是false。
        CameraUpdate mCameraUpdate= CameraUpdateFactory.zoomTo(17);
        aMap.moveCamera(mCameraUpdate);

        iv_back_local=root.findViewById(R.id.iv_back_local);
        iv_back_local.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                backLocal();
            }
        });
    }

    void initLocationListener(){
        CzAMapLocation.getInstance(getActivity()).addOnLocationChangeListener(onLocationChangeListener);
    }

    void initBaiduVoiceRecognize(){
        baiduVoiceRecognize= BaiduVoiceRecognize.getInstance(instance.getActivity());
        baiduVoiceRecognize.setBaiduVoiceRecognizeListaner(new BaiduVoiceRecognize.BaiduVoiceRecognizeListaner() {
            @Override
            public void onGetRecognize(BaiduVoiceRecognize instance, String text) {
                tv_heart_text.setText(text);
            }

            @Override
            public void onGetInfo(BaiduVoiceRecognize instance, String info) {
                tv_heart_status.setText("状态信息："+info);
            }

            @Override
            public void onGetException(BaiduVoiceRecognize instance, String exception) {
                tv_heart_status.setText("识别异常："+exception);
            }

            @Override
            public void onStart(BaiduVoiceRecognize instance) {
                tv_heart_status.setText("语音识别中....");
                iv_btn_rec.setVisibility(View.GONE);
                iv_btn_stop_rec.setVisibility(View.VISIBLE);
            }

            @Override
            public void onEnd(BaiduVoiceRecognize instance) {
                tv_heart_status.setText("识别等待中....");
                iv_btn_rec.setVisibility(View.GONE);
                iv_btn_stop_rec.setVisibility(View.VISIBLE);
            }

            @Override
            public void onExit(BaiduVoiceRecognize instance) {
                tv_heart_status.setText("语音识别已结束");
                iv_btn_rec.setVisibility(View.VISIBLE);
                iv_btn_stop_rec.setVisibility(View.GONE);

                showHeartResult();
            }
        });
    }
    void releaseBaiduVoiceRecognize(){

    }

    void backLocal(){
        CameraUpdate mCameraUpdate= CameraUpdateFactory.changeLatLng(
                new LatLng(currentLocaltion.getLatitude(),currentLocaltion.getLongitude()));
        aMap.moveCamera(mCameraUpdate);
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.lay_btn_heart:
                showHearPanel(true);
                break;
            case R.id.lay_btn_sport:
                clickBtnSport();
                break;
            case R.id.lay_btn_eat:
                clickBtnEat();
                break;
            case R.id.lay_btn_feeling:
                showFeelPanel(true);
                break;
            case R.id.lay_float_panel:
                showHearPanel(false);
                showFeelPanel(false);
                break;
            case R.id.lay_heart_content:
                break;
            case R.id.lay_btn_rec:
                clickLayBtnRec();
                break;
            case R.id.tv_btn_heart_confirm:
                clickBtnHeartConfirm();
                break;
            case R.id.tv_btn_heart_cancel:
                showHearPanel(false);
                break;
            case R.id.tv_btn_heart_history:
                clickBtnHeartHistory();
                break;
            case R.id.lay_feel_content:
                break;
            case R.id.tv_btn_feel_confirm:
                clickBtnFeelConfirm();
                break;
            case R.id.tv_btn_feel_cancel:
                showFeelPanel(false);
                break;
            case R.id.tv_btn_feel_history:
                clickBtnFeelHistory();
                break;
            case R.id.tv_today_steps:
                updateSteps();
                break;
        }
    }

    void toast(String info){
        Toast.makeText(instance.getActivity(),info,Toast.LENGTH_LONG).show();
    }


    void clickBtnSport(){
        modelUser.loadUserInfo();
        try {
            JSONObject userInfoJSON = modelUser.getUserInfoJSON();
            Intent intent= new Intent(instance.getActivity(), HistoryFootprintActivity.class);
            intent.putExtra("person_info", userInfoJSON.toString());
            getActivity().startActivity(intent);
        }catch (Exception e){
            CzLibrary.alert(instance.getActivity(),"您的个人信息获取异常，请稍后再试，或退出并重新登录APP."+e);
        }

    }
    void clickBtnEat(){
        toast("饮食情况，建设中...");
    }

    void showHearPanel(boolean isShow){
        if(isShow){
            initBaiduVoiceRecognize();
            lay_float_panel.setVisibility(View.VISIBLE);
            lay_heart_content.setVisibility(View.VISIBLE);
        }else{
            releaseBaiduVoiceRecognize();
            lay_float_panel.setVisibility(View.GONE);
            lay_heart_content.setVisibility(View.GONE);
            tv_heart_status.setText("准备语音识别");
            tv_heart_text.setText("");
            et_hi_presure.setText("");
            et_lo_presure.setText("");
            et_heart_rate.setText("");
        }
    }

    void showFeelPanel(boolean isShow){
        if(isShow){
            lay_float_panel.setVisibility(View.VISIBLE);
            lay_feel_content.setVisibility(View.VISIBLE);
        }else{
            lay_float_panel.setVisibility(View.GONE);
            lay_feel_content.setVisibility(View.GONE);
            spinner_eat.setSelection(2);
            spinner_tired.setSelection(2);
            spinner_sweat.setSelection(2);
            spinner_sleep.setSelection(2);
        }
    }

    void clickLayBtnRec(){
        if(iv_btn_rec.getVisibility()==View.GONE){
            stopVoiceRecognize();
        }else{
            startVoiceRecognize();
        }
    }

    void startVoiceRecognize(){
        toast("startVoiceRecognize");

        if(baiduVoiceRecognize!=null) {
            baiduVoiceRecognize.start();
        }
        tv_heart_text.setText("");
        et_hi_presure.setText("");
        et_lo_presure.setText("");
        et_heart_rate.setText("");
    }

    void stopVoiceRecognize(){
        toast("stopVoiceRecognize");

        if(baiduVoiceRecognize!=null) {
            baiduVoiceRecognize.stop();
        }
    }

    void clickBtnHeartConfirm(){
            String url = Constant.DOMAIN_API + "/mobile_app/set_blood_pressure?"
                    + "hi_pressure=" + getEditTextNumber(et_hi_presure)
                    + "&lo_pressure=" + getEditTextNumber(et_lo_presure)
                    + "&heart_rate=" + getEditTextNumber(et_heart_rate)
                    + "&token=" + modelUser.getToken();
            CzSys_HTTP.requestPostCz(getActivity(), url, new HashMap<String, String>(), new CzSys_HTTP.HttpListener() {
                @Override
                public void onHttpSuccess(String data) {

                }

                @Override
                public void onFeedBackSuccess(JSONObject feedBackData) {
                    CzLibrary.alertConfirm(getActivity(), "提示", "血压心率数据保存成功，要关闭对话框吗？", new CzLibrary.AlertCallBack() {
                        @Override
                        public void onAlertConfirm() {
                            showHearPanel(false);
                        }

                        @Override
                        public void onAlertCancel() {

                        }
                    });
                }
            });

    }

    void clickBtnHeartHistory(){
        modelUser.loadUserInfo();
        try {
            JSONObject userInfoJSON = modelUser.getUserInfoJSON();
            Intent intent= new Intent(instance.getActivity(), HistoryHeartActivity.class);
            intent.putExtra("person_info", userInfoJSON.toString());
            getActivity().startActivity(intent);
        }catch (Exception e){
            CzLibrary.alert(instance.getActivity(),"您的个人信息获取异常，请稍后再试，或退出并重新登录APP");
        }
    }

    void clickBtnFeelConfirm(){
        String url = Constant.DOMAIN_API + "/mobile_app/set_feel?"
                + "eat=" + (spinner_eat.getSelectedItemPosition()+1)
                + "&tired=" + (spinner_tired.getSelectedItemPosition()+1)
                + "&sweat=" + (spinner_sweat.getSelectedItemPosition()+1)
                + "&sleep=" + (spinner_sleep.getSelectedItemPosition()+1)
                + "&token=" + modelUser.getToken();
        CzSys_HTTP.requestPostCz(getActivity(), url, new HashMap<String, String>(), new CzSys_HTTP.HttpListener() {
            @Override
            public void onHttpSuccess(String data) {

            }

            @Override
            public void onFeedBackSuccess(JSONObject feedBackData) {
                CzLibrary.alertConfirm(getActivity(), "提示", "个人体感数据保存成功，要关闭对话框吗？", new CzLibrary.AlertCallBack() {
                    @Override
                    public void onAlertConfirm() {
                        showFeelPanel(false);
                    }

                    @Override
                    public void onAlertCancel() {

                    }
                });
            }
        });

    }

    void clickBtnFeelHistory(){
        showFeelPanel(true);
        modelUser.loadUserInfo();
        try {
            JSONObject userInfoJSON = modelUser.getUserInfoJSON();
            Intent intent= new Intent(instance.getActivity(), HistoryFeelActivity.class);
            intent.putExtra("person_info", userInfoJSON.toString());
            getActivity().startActivity(intent);
        }catch (Exception e){
            CzLibrary.alert(instance.getActivity(),"您的个人信息获取异常，请稍后再试，或退出并重新登录APP");
        }
    }

    void showHeartResult(){
        String text=tv_heart_text.getText().toString();
        int posHi=text.indexOf("高压");
        int posLo=text.indexOf("低压");
        int posRate=text.indexOf("心率");
        try {
            String strHiP=text.substring(posHi+2,posLo);
            hi_pressure=getNumber(strHiP);
            et_hi_presure.setText(""+hi_pressure);
            String strLoP=text.substring(posLo+2,posRate);
            lo_pressure=getNumber(strLoP);
            et_lo_presure.setText(""+lo_pressure);
            String strHR=text.substring(posRate+2);
            heart_rate=getNumber(strHR);
            et_heart_rate.setText(""+heart_rate);
        }catch (Exception e){
            Log.e(TAG,"showHeartResult Exp:"+e);
        }

    }

    int getNumber(String text){
        int result=0;

        try {
            result=Integer.parseInt(text);
        }catch(Exception e){
//            try {
                int length = text.length();
                String numStr = "";
                for (int i = 0; i < length; i++) {
                    if (text.substring(i, i+1).equals("一")) {
                        numStr += "1";
                    } else if (text.substring(i, i+1).equals("二")) {
                        numStr += "2";
                    } else if (text.substring(i, i+1).equals("三")) {
                        numStr += "3";
                    } else if (text.substring(i, i+1).equals("四")) {
                        numStr += "4";
                    } else if (text.substring(i, i+1).equals("五")) {
                        numStr += "5";
                    } else if (text.substring(i, i+1).equals("六")) {
                        numStr += "6";
                    } else if (text.substring(i, i+1).equals("七")) {
                        numStr += "7";
                    } else if (text.substring(i, i+1).equals("八")) {
                        numStr += "8";
                    } else if (text.substring(i, i+1).equals("九")) {
                        numStr += "9";
                    } else if (text.substring(i, i+1).equals("零")) {
                        numStr += "0";
                    }
                }
                result = Integer.parseInt(numStr);
                Log.e(TAG, "getNumber() , " + text + " , " + numStr);
                String lastChar = text.substring(length - 1);
                if (lastChar.equals("万")) {
                    result = result * 10000;
                } else if (lastChar.equals("千")) {
                    result = result * 1000;
                } else if (lastChar.equals("百")) {
                    result = result * 100;
                } else if (lastChar.equals("十")) {
                    result = result * 10;
                }
//            }catch (Exception e1){
//                e1.printStackTrace();
//            }
        }
        return result;
    }

    int getEditTextNumber(EditText et){
        int num=0;
        try {
            num=Integer.parseInt(et.getText().toString());
        }catch (Exception e){
            num=0;
        }
        return num;
    }

    void updateSteps(){

    }

    void refreshLocationData(){
        getLocationList();
        lay_root.postDelayed(new Runnable() {
            @Override
            public void run() {
                refreshLocationData();
            }
        },10000);
    }

    void getLocationList(){
        final String url=Constant.DOMAIN_API+"/mobile_app/get_care_location?"
                +"token="+ModelUser.getInstance(instance.getActivity()).getToken();
        CzSys_HTTP.requestPostCz(instance.getActivity(), url, new HashMap<String, String>(),
                new CzSys_HTTP.HttpListener() {
                    @Override
                    public void onHttpSuccess(String data) {

                    }

                    @Override
                    public void onFeedBackSuccess(JSONObject feedBackData) {
                        try{
                            JSONObject dataJson=feedBackData.getJSONObject("data");
                            list_location=dataJson.getJSONArray("list_location");
                            showLocation();
                        }catch (Exception e){
                            CzLibrary.alert(instance.getActivity(),"解析被监护人足迹异常："+e);
                        }
                    }
                });
    }


    void showLocation(){
        try {
            int length=list_location.length();
            int steps=0;
            for(int i=0;i<length;i++){
                JSONObject locaJson=list_location.getJSONObject(i);
                Constant.debugInfo(TAG, locaJson.toString());
                steps+=locaJson.getInt("stp");
            }
            int distTemp=(int)(steps*0.65/100);
            tv_today_steps.setText("今日步数:"+steps+" 约"+distTemp/10+"."+distTemp%10+"公里");

        }catch (Exception e){
            Constant.debugInfo(TAG, "showLocation Exp:"+e);
        }

    }


    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        //Log.e(TAG,"onItemSelected / view："+parent.getId()+" , pos="+position);
        switch(parent.getId()){
            case R.id.spinner_eat:
                //Toast.makeText(this.getContext(),"spinner_eat:"+position,Toast.LENGTH_LONG).show();
                break;
            case R.id.spinner_tired:
                //Toast.makeText(this.getContext(),"spinner_tired:"+position,Toast.LENGTH_LONG).show();
                break;
            case R.id.spinner_sweat:
                //Toast.makeText(this.getContext(),"spinner_sweat:"+position,Toast.LENGTH_LONG).show();
                break;
            case R.id.spinner_sleep:
                //Toast.makeText(this.getContext(),"spinner_sleep:"+position,Toast.LENGTH_LONG).show();
                break;
        }
        //Log.e(TAG,"onItemSelected "+R.id.spinner_eat+","+R.id.spinner_tired+","+R.id.spinner_sweat+","+R.id.spinner_sleep);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}