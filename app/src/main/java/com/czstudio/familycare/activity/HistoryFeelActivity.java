package com.czstudio.familycare.activity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.czstudio.czlibrary.CzLibrary;
import com.czstudio.czlibrary.CzSys_HTTP;
import com.czstudio.familycare.Constant;
import com.czstudio.familycare.R;
import com.czstudio.familycare.custom_view.HistoryFeelView;
import com.czstudio.familycare.model.ModelUser;
import com.czstudio.familycare.widget.TimeSelectHeadFragment;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;

public class HistoryFeelActivity extends AppCompatActivity {

    static final String TAG = "HistoryFeelActivity";

    HistoryFeelActivity instance;

    JSONObject personInfo;
    String name,sex,avatar;
    int care_uid;

    ModelUser modelUser;
    JSONArray dataArray;

    int currentPage=0,requestPage=0,total=0;

    TextView tv_debug;

    ConstraintLayout lay_canvas_container,lay_root;
    LinearLayout linear_canvas;
    HistoryFeelView historyFeelView;

    TimeSelectHeadFragment timeSelectHeadFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history_feel);
        Constant.debugInfo(TAG,"onCreate");
        initData();
        modelUser.checkLogin();
        initView();
        getData();
    }

    void initData(){
        instance=this;
        Bundle extra=getIntent().getExtras();
        if(extra==null){
            Constant.debugInfo(TAG,"initData / extra==null");
            return;
        }
        String infoStr=extra.getString("person_info","");

        if(infoStr.length()==0){
            Constant.debugInfo(TAG,"initData / infoStr==null");
            return;
        }
        try {
            Log.e(TAG,"initData / personInfo = "+infoStr);

            personInfo = new JSONObject(infoStr);
            name = personInfo.getString("name");
            sex=personInfo.getInt("sex")==1?"男":"女";
            avatar=personInfo.getString("avatar");
            care_uid=personInfo.getInt("uid");
        }catch (Exception e){
            Constant.debugInfo(TAG,"initData Exp:"+e);
        }

        modelUser= ModelUser.getInstance(instance);

    }

    void initView(){
        getWindow().setStatusBarColor(Constant.statusBarColor);
        timeSelectHeadFragment= TimeSelectHeadFragment.getInstance(instance,name + " 体感", new TimeSelectHeadFragment.OnClickWidgetListener() {
                    @Override
                    public void onClickBack(TimeSelectHeadFragment instance, ImageView iv) {
                        finish();
                    }

                    @Override
                    public void onTimeChanged(TimeSelectHeadFragment fragment) {
                        getData();
                    }
                }
        );
        getSupportFragmentManager().beginTransaction().add(R.id.frag_head,timeSelectHeadFragment).commit();

        lay_root=findViewById(R.id.lay_root);
        tv_debug=findViewById(R.id.tv_debug);

        lay_canvas_container=findViewById(R.id.lay_canvas_container);

        linear_canvas=findViewById(R.id.linear_canvas);
        historyFeelView=new HistoryFeelView(instance);
        linear_canvas.addView(historyFeelView);
//        linear_canvas.setBackgroundColor(0xFF00FF00);
//        historeyFootprintView.setBackgroundColor(0xFFF0F0F0);
        lay_canvas_container.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                if(right-left>0){
                    v.removeOnLayoutChangeListener(this);
                    ViewGroup.LayoutParams lp=historyFeelView.getLayoutParams();
                    lp.width=lay_canvas_container.getWidth();
                    lp.height=lay_canvas_container.getHeight();
                    historyFeelView.setLayoutParams(lp);
                }
            }
        });
    }

    void getData(){
        String url=Constant.DOMAIN_API+"/mobile_app/get_care_feel"
                +"?care_uid="+care_uid
                +"&start_stamp="+timeSelectHeadFragment.currentStartTimeStamp
                +"&end_stamp="+timeSelectHeadFragment.currentEndTimeStamp
                +"&page="+requestPage
                +"&token="+modelUser.getToken();


        CzSys_HTTP.requestPostCz(instance, url, new HashMap<String, String>(), new CzSys_HTTP.HttpListener() {
            @Override
            public void onHttpSuccess(String data) {

            }

            @Override
            public void onFeedBackSuccess(JSONObject feedBackData) {
                try{
                    JSONObject data=feedBackData.getJSONObject("data");
                    dataArray=data.getJSONArray("list");
                    showData();
                }catch (Exception e){
                    CzLibrary.alert(instance,"解析体感数据异常"+e);
                }
            }
        });
    }

    void showData(){
        historyFeelView.setTimeRange(
                Long.parseLong(timeSelectHeadFragment.currentStartTimeStamp),
                Long.parseLong(timeSelectHeadFragment.currentEndTimeStamp));
        historyFeelView.showData(dataArray);
    }

    void showAddRecordPanel(boolean isShow){

    }


}
