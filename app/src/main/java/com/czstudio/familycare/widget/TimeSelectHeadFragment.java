package com.czstudio.familycare.widget;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.czstudio.czlibrary.CzLibrary;
import com.czstudio.czlibrary.CzTimeSelector;
import com.czstudio.familycare.R;


/**

 */
public class TimeSelectHeadFragment extends Fragment {
    static final String TAG="TimeSelectHeadFragment";
    Activity activity;
    public TimeSelectHeadFragment instance;
    public View rootView;

    ImageView img_back,iv_start_time,iv_end_time;
    TextView tv_title,tv_add_record;
    TextView tv_start_time,tv_end_time;

    public String title;
    public String currentStartTimeStamp=""+(System.currentTimeMillis()/1000-7*86400);
    public String currentEndTimeStamp=""+System.currentTimeMillis()/1000;
    public String currentStartTimeString= CzLibrary.getTimeStringFromTimeStamp(currentStartTimeStamp);
    public String currentEndTimeString= CzLibrary.getTimeStringFromTimeStamp(currentEndTimeStamp);

    public TimeSelectHeadFragment() {
        // Required empty public constructor
    }

    public static TimeSelectHeadFragment getInstance(Activity activity, String title, OnClickWidgetListener listener) {
        // Required empty public constructor
        TimeSelectHeadFragment fragment = new TimeSelectHeadFragment();
        fragment.activity=activity;
        fragment.title=title;
        fragment.onClickWidgetListener=listener;
        fragment.instance=fragment;
        return fragment;
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView=inflater.inflate(R.layout.fragment_time_select_head, container, false);
        img_back=rootView.findViewById(R.id.img_back);

        tv_title=rootView.findViewById(R.id.tv_title);
        tv_title.setText(title);
        tv_add_record=rootView.findViewById(R.id.tv_add_record);

        tv_start_time=rootView.findViewById(R.id.tv_start_time);
        tv_start_time.setText(currentStartTimeString.substring(0,10));
        tv_end_time=rootView.findViewById(R.id.tv_end_time);
        tv_end_time.setText(currentEndTimeString.substring(0,10));

        iv_start_time=rootView.findViewById(R.id.iv_start_time);
        iv_end_time=rootView.findViewById(R.id.iv_end_time);

        if(onClickWidgetListener!=null){
            img_back.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onClickWidgetListener.onClickBack(instance,img_back);
                }
            });
            tv_start_time.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showStartTimeSelector();
                }
            });
            iv_start_time.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showStartTimeSelector();
                }
            });
            tv_end_time.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showEndTimeSelector();
                }
            });
            iv_end_time.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showEndTimeSelector();
                }
            });
        }
        return rootView;
    }

    public TextView getTv_add_record(){
        return tv_add_record;
    }

    public void showTv_add_record(){
        tv_add_record.setVisibility(View.VISIBLE);
    }

    void showStartTimeSelector(){
        CzTimeSelector timeSelector = new CzTimeSelector(activity, new CzTimeSelector.ResultHandler() {
            @Override
            public void handle(String time) {
                setStartTimeFromTimeString(time+":00");
            }
        }, "2019-01-01 00:00:00", "2050-01-01 00:00:00");
        timeSelector.setMode(CzTimeSelector.MODE.YMD);
        timeSelector.show(Long.parseLong(currentStartTimeStamp));
    }

    void showEndTimeSelector(){
        CzTimeSelector timeSelector = new CzTimeSelector(activity, new CzTimeSelector.ResultHandler() {
            @Override
            public void handle(String time) {
                setEndTimeFromTimeString(time+":00");
            }
        }, "2019-01-01 00:00:00", "2050-01-01 00:00:00");
        timeSelector.setMode(CzTimeSelector.MODE.YMD);
        timeSelector.show(Long.parseLong(currentEndTimeStamp));
    }

    public void setStartTimeFromTimeString(String timeStr ){
        Log.e(TAG,"setStartTimeFromTimeString / timeStr="+timeStr);
        String startStampStr=CzLibrary.getTimeStampFromTimeString(timeStr);
        long startStamp=Long.parseLong(startStampStr);
        long endStamp=Long.parseLong(currentEndTimeStamp);
        if(endStamp>startStamp) {
            currentStartTimeString = timeStr;
            currentStartTimeStamp = CzLibrary.getTimeStampFromTimeString(currentStartTimeString);
            tv_start_time.setText(timeStr.substring(0,10));
            if(onClickWidgetListener!=null) {
                onClickWidgetListener.onTimeChanged(instance);
            }
        }else{
            CzLibrary.alert(activity,"结束时间不能小于开始时间");
        }
    }

    public void setEndTimeFromTimeString(String timeStr ){
        long startStamp=Long.parseLong(currentStartTimeStamp);
        String endStampStr=CzLibrary.getTimeStampFromTimeString(timeStr);
        long endStamp=Long.parseLong(endStampStr);
        if(endStamp>startStamp){
            currentEndTimeString=timeStr;
            currentEndTimeStamp=endStampStr;
            tv_end_time.setText(timeStr.substring(0,10));
            if(onClickWidgetListener!=null) {
                onClickWidgetListener.onTimeChanged(instance);
            }
        }else{
            CzLibrary.alert(activity,"结束时间不能小于开始时间");
        }


    }

    public void setStartTimeFromTimeStamp(String timeStamp ){
        currentStartTimeStamp=timeStamp;
        currentStartTimeString=CzLibrary.getTimeStringFromTimeStamp(currentStartTimeStamp);
        tv_start_time.setText(currentStartTimeString);
    }

    public void setEndFromTimeTimeStamp(String timeStamp ){
        currentEndTimeStamp=timeStamp;
        currentEndTimeString=CzLibrary.getTimeStringFromTimeStamp(currentStartTimeStamp);
        tv_end_time.setText(currentEndTimeString);
    }

    /**
     * { StartString,StartStamp,EndString,EndStamp }
     * @return
     */
    public String[] getCurrentTimeArray(){
        String[] timeArray={currentStartTimeString,currentStartTimeStamp,currentEndTimeString,currentEndTimeStamp};
        return timeArray;
    }


    public interface OnClickWidgetListener{
        public void onClickBack(TimeSelectHeadFragment fragment, ImageView iv);
        public void onTimeChanged(TimeSelectHeadFragment fragment);
    }

    OnClickWidgetListener onClickWidgetListener=null;

}
