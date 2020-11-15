package com.czstudio.czlibrary;

import android.app.Activity;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

public class CzRefreshListView {

    TextView tv_loadMore;
    SwipeRefreshLayout swipeRefreshLayout;
    ListView listView;

    public CzRefreshListView(Activity activity,FrameLayout parentFrameLayout){
        swipeRefreshLayout=new SwipeRefreshLayout(activity.getApplicationContext());
        parentFrameLayout.addView(swipeRefreshLayout);
        listView=new ListView(activity.getApplicationContext());
        swipeRefreshLayout.addView(listView);
        tv_loadMore=new TextView(activity.getApplicationContext());
        tv_loadMore.setText("LoadMore");
    }
}
