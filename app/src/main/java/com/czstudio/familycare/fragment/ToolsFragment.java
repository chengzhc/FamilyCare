package com.czstudio.familycare.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.czstudio.familycare.Constant;
import com.czstudio.familycare.R;
import com.czstudio.familycare.adapter.Adapter_CarePerson;
import com.czstudio.familycare.model.ModelUser;

import org.json.JSONArray;

public class ToolsFragment extends Fragment {
    final String TAG=getClass().getName();
    View root;

    ListView lv_care_person;
    Adapter_CarePerson adapter_carePerson;
    JSONArray listData=new JSONArray();

    SwipeRefreshLayout srl_care_person;

    public static CareFragment instance;
    ModelUser modelUser;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        Constant.debugInfo(TAG,"onCreateView");
        root = inflater.inflate(R.layout.fragment_tools, container, false);

//        initData();
//        modelUser.checkLogin();

        return root;
    }
}