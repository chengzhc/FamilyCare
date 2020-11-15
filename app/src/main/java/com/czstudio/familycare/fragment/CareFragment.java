package com.czstudio.familycare.fragment;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.czstudio.czlibrary.CzLibrary;
import com.czstudio.czlibrary.CzSys_HTTP;
import com.czstudio.czlibrary.Cz_QRCodeMaker;
import com.czstudio.czlibrary.ScanQRCodeActivity;
import com.czstudio.familycare.Constant;
import com.czstudio.familycare.MainActivity;
import com.czstudio.familycare.R;
import com.czstudio.familycare.activity.BeCarePersonActivity;
import com.czstudio.familycare.activity.HistoryHeartActivity;
import com.czstudio.familycare.adapter.Adapter_CarePerson;
import com.czstudio.familycare.model.ModelUser;
import com.google.zxing.integration.android.IntentIntegrator;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;

public class CareFragment extends Fragment implements View.OnClickListener {
    final String TAG=getClass().getName();
    View root;

    ListView lv_care_person;
    Adapter_CarePerson adapter_carePerson;
    JSONArray listData=new JSONArray();

    SwipeRefreshLayout srl_care_person;

    public static CareFragment instance;
    ModelUser modelUser;

    TextView tv_addmore,tv_reload;

    long refreshStartStamp;

    LinearLayout lay_family1,lay_family2,lay_code_2d1,lay_outto_family1,lay_code_2d2,lay_outto_family2;
    TextView tv_family1_name,tv_family2_name;

    ConstraintLayout lay_float_panel;
   EditText et_family_name;
   Button btn_create_family;
   ImageView iv_code_2d;

   MainActivity mainActivity=MainActivity.getInstance();



    //每次开启页面都会检查是否已加入家庭，并提示未加入者，但在ActivityResult返回时就不要再显示提示了，该标志就是起这个作用的
    boolean isScanActivityResult=false;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        Constant.debugInfo(TAG,"onCreateView");
        root = inflater.inflate(R.layout.fragment_care, container, false);

        initData();
        //获取数据在onResume()中进行

        return root;
    }

    void initData(){
        instance=this;
        modelUser=ModelUser.getInstance(getActivity());
        modelUser.setOnLoginListener(new ModelUser.OnLoginListener() {
            @Override
            public void onLogin() {
                initView();
            }
        });
        modelUser.checkLogin();

        mainActivity.addQrCodeResultListener(new MainActivity.QRCodeResultListener() {
            @Override
            public void onGetScanResult(String str) {
                isScanActivityResult=true;
                try {
                    intoFamily(Integer.parseInt(str));
                }catch (Exception e){
                    Log.e(TAG,"addQrCodeResultListener / Exp:"+e+"org str="+str);
                    CzLibrary.alert(getContext(),"二维码解析异常");
                }
            }

            @Override
            public void onCancelScanResult(String str) {
                Toast.makeText(getContext(),"扫描已取消",Toast.LENGTH_LONG).show();
            }
        });
    }

    void initView(){
        lv_care_person=root.findViewById(R.id.lv_care_person);
        adapter_carePerson=new Adapter_CarePerson(lv_care_person.getContext(),listData);
        lv_care_person.setAdapter(adapter_carePerson);
        adapter_carePerson.addCarePersonAdapterListener(new Adapter_CarePerson.AdapterListener() {
            @Override
            public void onClickItemRootView(JSONObject personInfoJson, View view, int itemId) {

            }

            @Override
            public void onClickItemChildView(JSONObject personInfoJson, View view, int itemId) {
                Intent intent;

                switch(view.getId()){
                    case R.id.tv_btn_camera:
                        intent = new Intent(instance.getActivity(), BeCarePersonActivity.class);
                        intent.putExtra("person_info",personInfoJson.toString());
                        intent.putExtra("page_type",BeCarePersonActivity.PAGE_TYPE_CAMERA);
                        instance.getActivity().startActivity(intent);
                        break;
                    case R.id.tv_btn_location:
                        intent = new Intent(instance.getActivity(), BeCarePersonActivity.class);
                        intent.putExtra("person_info",personInfoJson.toString());
                        intent.putExtra("page_type",BeCarePersonActivity.PAGE_TYPE_LOCATION);
                        instance.getActivity().startActivity(intent);
                        break;
                    case R.id.tv_btn_helthy:
                        intent = new Intent(instance.getActivity(), HistoryHeartActivity.class);
                        intent.putExtra("person_info",personInfoJson.toString());
                        instance.getActivity().startActivity(intent);
                        break;
                    case R.id.tv_btn_alert:
                        sendOnlineNotification(personInfoJson);
                        break;
                }

            }
        });

//        lv_care_person.setOnScrollChangeListener(new View.OnScrollChangeListener() {
//            @Override
//            public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
//                Log.e(TAG,"onScrollChange");
//            }
//        });
        lv_care_person.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                if(scrollState==SCROLL_STATE_IDLE){
                    if (view.getLastVisiblePosition() == (view.getCount() - 1)) {
                        getMoreData();
                    }
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                Log.e(TAG,"onScrollStateChanged firstItem="+firstVisibleItem+", count"+visibleItemCount);
            }
        });

        srl_care_person=root.findViewById(R.id.srl_care_person);
        srl_care_person.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Log.e(TAG,"OnRefreshListener");
                refreshStartStamp=0;
                refreshData();
            }
        });

        tv_addmore=root.findViewById(R.id.tv_addmore);
        tv_reload=root.findViewById(R.id.tv_reload);



        lay_float_panel=root.findViewById(R.id.lay_float_panel);
        lay_float_panel.setOnClickListener(this);
        et_family_name=root.findViewById(R.id.et_family_name);
        btn_create_family=root.findViewById(R.id.btn_create_family);
        btn_create_family.setOnClickListener(this);


        //ActionBar
        iv_code_2d=root.findViewById(R.id.iv_code_2d);

        lay_family1=root.findViewById(R.id.lay_family1);
        lay_family1.setOnClickListener(this);
        lay_code_2d1=root.findViewById(R.id.lay_code_2d1);
        lay_code_2d1.setOnClickListener(this);
        lay_outto_family1=root.findViewById(R.id.lay_outto_family1);
        lay_outto_family1.setOnClickListener(this);
        tv_family1_name=root.findViewById(R.id.tv_family1_name);
        //
        lay_family2=root.findViewById(R.id.lay_family2);
        lay_family2.setOnClickListener(this);
        lay_code_2d2=root.findViewById(R.id.lay_code_2d2);
        lay_code_2d2.setOnClickListener(this);
        lay_outto_family2=root.findViewById(R.id.lay_outto_family2);
        lay_outto_family2.setOnClickListener(this);
        tv_family2_name=root.findViewById(R.id.tv_family2_name);

        setFamilyButton();

    }

    /**
     * 本方法只能被setHeadButton调用
     */
    void refreshData(){
        Log.e(TAG,"----------refreshData");
        int familyId=modelUser.getFamily1_id();
        if(Constant.family_num==2){
            familyId=modelUser.getFamily2_id();
        }
        if((familyId==0)&&(!isScanActivityResult)){
            showNoneFamilyAlert();
            return;
        }
        if(System.currentTimeMillis()/1000-refreshStartStamp<60){
            return;
        }
        refreshStartStamp=System.currentTimeMillis()/1000;
        tv_reload.setVisibility(View.VISIBLE);
        CzLibrary.bounceContainer(srl_care_person,0,tv_reload.getHeight(),300);

        String url= Constant.DOMAIN_API+"/mobile_app/get_care_list?family_num="+Constant.family_num+"&token="+ modelUser.getToken();
        CzSys_HTTP.requestPostCz(instance.getActivity(), url, new HashMap<String, String>(), new CzSys_HTTP.HttpListener() {
            @Override
            public void onHttpSuccess(String data) {
//                //防止family数据突然变化，而本页不断在刷新，且刷新的family数据是从旧的user里取得
//                try{
//                    JSONObject jobj=new JSONObject(data);
//                    String succ=jobj.getString("success");
//                    if(succ.equals("0")){
//                        modelUser.checkLogin();
//                    }
//                }catch (Exception e){
//                    modelUser.checkLogin();
//                }
            }

            @Override
            public void onFeedBackSuccess(JSONObject feedBackData) {
                srl_care_person.setRefreshing(false);
                tv_reload.setVisibility(View.GONE);
                Constant.debugInfo(TAG,"getData/fb:"+feedBackData.toString());
                try{
                    JSONObject dataJson=feedBackData.getJSONObject("data");
                    JSONArray care_list=dataJson.getJSONArray("care_list");
                    if(dataJson.getInt("family_num")==Constant.family_num) {
                        adapter_carePerson.refresh(care_list);
                    }
                }catch (Exception e){
                    CzLibrary.alert(instance.getActivity(),"数据解析错误，请稍后再试");
                }
            }
        });
        //这句测试过无法保活
        root.postDelayed(new Runnable() {
            @Override
            public void run() {
                refreshData();
            }
        },60000);
    }

    void getMoreData(){
        tv_addmore.setVisibility(View.VISIBLE);
        CzLibrary.bounceContainer(srl_care_person,0,-tv_addmore.getHeight(),300);
    }

    void sendOnlineNotification(JSONObject personInfoJson) {
        try {
            String name=personInfoJson.getString("name");
            final int uid=personInfoJson.getInt("uid");
            CzLibrary.alertConfirm(getActivity(), "提示", "确定要给发送上线通知吗？", new CzLibrary.AlertCallBack() {
                @Override
                public void onAlertConfirm() {
                    Toast.makeText(getActivity(), "通知发送中", Toast.LENGTH_SHORT).show();

                    Constant.sendUmengPersonalNotification(instance.getActivity(), uid,"断线通知","FamilyCare已断线","点我让APP上线",new CzSys_HTTP.HttpListener() {
                        @Override
                        public void onHttpSuccess(String data) {

                        }

                        @Override
                        public void onFeedBackSuccess(JSONObject feedBackData) {
                            Log.e(TAG,"sendOnlineNotification get:"+feedBackData.toString());
                            CzLibrary.alert(instance.getActivity(),"通知已发送");
                        }
                    });
                }

                @Override
                public void onAlertCancel() {

                }
            });
        } catch (Exception e) {
            Toast.makeText(getActivity(), "用户信息异常："+personInfoJson.toString(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        Log.e(TAG,"onAttach");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.e(TAG,"onResume");
        refreshStartStamp=0;
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.e(TAG,"onStart");
    }

    void showNoneFamilyAlert(){
        CzLibrary.alertConfirm(getActivity(), "提示", "您暂时还未有家庭，请创建一个或扫码加入现有的家庭", "创建家庭", "扫码加入", new CzLibrary.AlertCallBack() {
            @Override
            public void onAlertConfirm() {
                showCreateFamilyPanel(true);
            }

            @Override
            public void onAlertCancel() {
                scanFamilyCode();
            }
        });
    }

    void showCreateFamilyPanel(boolean isShow){
        if(isShow){
            lay_float_panel.setVisibility(View.VISIBLE);
        }else{
            lay_float_panel.setVisibility(View.GONE);
        }
        et_family_name.setText("");
    }

    void setFamilyButton(){
        if(Constant.family_num==1){
            lay_family1.setAlpha(1);
            lay_family2.setAlpha(0.3f);
            tv_family1_name.setText(modelUser.getFamily1_name().equals("")?"家庭1":modelUser.getFamily1_name());

            lay_code_2d2.setVisibility(View.GONE);
            lay_outto_family2.setVisibility(View.GONE);

            if(modelUser.getIs_family1_manager()==1){
                setManagerHead(1,modelUser.getFamily1_id());
            }else{
                if(modelUser.getFamily1_id()==0){
                    setNewUserHead(1);
                }else{
                    setMemberHead(1);
                }
            }
        }else{
            lay_family1.setAlpha(0.3f);
            lay_family2.setAlpha(1);
            tv_family2_name.setText(modelUser.getFamily2_name().equals("")?"家庭2":modelUser.getFamily2_name());
            lay_code_2d1.setVisibility(View.GONE);
            lay_outto_family1.setVisibility(View.GONE);
            if(modelUser.getIs_family2_manager()==1){
                setManagerHead(2,modelUser.getFamily2_id());
            }else{
                if(modelUser.getFamily2_id()==0){
                    setNewUserHead(2);
                }else{
                    setMemberHead(2);
                }
            }
        }
        //关闭所有浮层
        iv_code_2d.setVisibility(View.GONE);
        clearList();
        refreshStartStamp=0;
        refreshData();
    }

    void setNewUserHead(int familyNum){
        if(familyNum==1) {
            lay_code_2d1.setVisibility(View.GONE);
            lay_outto_family1.setVisibility(View.GONE);
        }else{
            lay_code_2d2.setVisibility(View.GONE);
            lay_outto_family2.setVisibility(View.GONE);
        }
    }

    void setManagerHead(int familyNum,int family_id){
        if(familyNum==1) {
            lay_code_2d1.setVisibility(View.VISIBLE);
            lay_outto_family1.setVisibility(View.GONE);
        }else{
            lay_code_2d2.setVisibility(View.VISIBLE);
            lay_outto_family2.setVisibility(View.GONE);
        }
    }

    void setMemberHead(int familyNum){
        if(familyNum==1) {
            lay_code_2d1.setVisibility(View.GONE);
            lay_outto_family1.setVisibility(View.VISIBLE);
        }else{
            lay_code_2d2.setVisibility(View.GONE);
            lay_outto_family2.setVisibility(View.VISIBLE);
        }
    }

    void clearList(){
        adapter_carePerson.refresh(new JSONArray());
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.lay_float_panel:
                showCreateFamilyPanel(false);
                break;
            case R.id.btn_create_family:
                createFamily();
                break;
            case R.id.lay_family1:
                Constant.family_num=1;
                setFamilyButton();
                break;
            case R.id.lay_code_2d1:
                Constant.family_num=1;
                showCode2D();
                break;
            case R.id.lay_outto_family1:
                Constant.family_num=1;
                CzLibrary.alertConfirm(getContext(), "提示", "确定要退出家庭码？", new CzLibrary.AlertCallBack() {
                    @Override
                    public void onAlertConfirm() {
                        outFamily();
                    }

                    @Override
                    public void onAlertCancel() {

                    }
                });
                break;
            case R.id.lay_family2:
                Constant.family_num=2;
                setFamilyButton();
                break;
            case R.id.lay_code_2d2:
                Constant.family_num=2;
                showCode2D();
                break;
            case R.id.lay_outto_family2:
                Constant.family_num=2;
                CzLibrary.alertConfirm(getContext(), "提示", "确定要退出家庭码？", new CzLibrary.AlertCallBack() {
                    @Override
                    public void onAlertConfirm() {
                        outFamily();
                    }

                    @Override
                    public void onAlertCancel() {

                    }
                });
                break;
        }
    }

    void createFamily(){
        String url=Constant.DOMAIN_API+"/mobile_app/create_family"
                +"?family_num="+Constant.family_num
                +"&name="+et_family_name.getText().toString()
                +"&token="+modelUser.getToken();
        CzSys_HTTP.requestPostCz(instance.getActivity(), url, new HashMap<String, String>(), new CzSys_HTTP.HttpListener() {
            @Override
            public void onHttpSuccess(String data) {

            }

            @Override
            public void onFeedBackSuccess(JSONObject feedBackData) {
                try{
                    showCreateFamilyPanel(false);
                    int family_id=feedBackData.getInt("data");
                    setManagerHead(Constant.family_num,family_id);
                    modelUser.checkLogin();//更新家庭信息
                    CzLibrary.alert(getActivity(),"您已创建家庭，请点击右上角显示家庭码，让其他家庭成员扫码加入");
                    clearList();
                }catch (Exception e){
                    Log.e(TAG,"createFamily success Exp:"+e);
                }


            }
        });
    }

    void showCode2D(){
        if(iv_code_2d.getVisibility()==View.GONE){
            iv_code_2d.setVisibility(View.VISIBLE);
            Bitmap bitmap = Cz_QRCodeMaker.createQRImage(
                    ""+ (Constant.family_num==1? modelUser.getFamily1_id():modelUser.getFamily2_id()),
                    400,  400);
            iv_code_2d.setImageBitmap(bitmap);
        }else{
            iv_code_2d.setVisibility(View.GONE);
        }
    }

    void scanFamilyCode(){
        /*以下是启动我们自定义的扫描活动*/
        IntentIntegrator intentIntegrator = new IntentIntegrator(getActivity());
        intentIntegrator.setBeepEnabled(true);
        intentIntegrator.addExtra("family_num",Constant.family_num);
        /*设置启动我们自定义的扫描活动，若不设置，将启动默认活动*/
        intentIntegrator.setCaptureActivity(ScanQRCodeActivity.class);
        intentIntegrator.initiateScan();
    }

    void intoFamily(final int family_id){
        String url=Constant.DOMAIN_API+"/mobile_app/into_family"
                +"?family_num="+Constant.family_num
                +"&family_id="+family_id
                +"&token="+modelUser.getToken();
        CzSys_HTTP.requestPostCz(getActivity(), url, new HashMap<String, String>(), new CzSys_HTTP.HttpListener() {
            @Override
            public void onHttpSuccess(String data) {

            }

            @Override
            public void onFeedBackSuccess(JSONObject feedBackData) {
                if(Constant.family_num==1){
                    modelUser.setFamily1_id(family_id);
                }else{
                    modelUser.setFamily2_id(family_id);
                }

                setFamilyButton();
            }
        });
    }

    void outFamily(){

        String url=Constant.DOMAIN_API+"/mobile_app/out_family"
                +"?family_num="+Constant.family_num
                +"&token="+modelUser.getToken();
        CzSys_HTTP.requestPostCz(instance.getActivity(), url, new HashMap<String, String>(), new CzSys_HTTP.HttpListener() {
            @Override
            public void onHttpSuccess(String data) {

            }

            @Override
            public void onFeedBackSuccess(JSONObject feedBackData) {
                try{
                    if(Constant.family_num==1){
                        modelUser.setFamily1_id(0);
                    }else{
                        modelUser.setFamily2_id(0);
                    }
                    isScanActivityResult=false;
                    modelUser.checkLogin();
                    clearList();
                    setFamilyButton();
                }catch (Exception e){
                    Log.e(TAG,"createFamily success Exp:"+e);
                }


            }
        });
    }

}