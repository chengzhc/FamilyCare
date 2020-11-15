package com.czstudio.familycare.adapter;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.VideoView;

import com.czstudio.czlibrary.Cz_BaseAdapter;
import com.czstudio.familycare.Constant;
import com.czstudio.familycare.R;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONObject;

public class Adapter_CareCamera extends Cz_BaseAdapter {
    String TAG=getClass().getSimpleName();

    public Adapter_CareCamera(Context parentContext, JSONArray listData) {
        super.init(parentContext, listData);
        Constant.debugInfo(TAG,"init");
    }

    @Override
    public View getView(final int i, View view, ViewGroup viewGroup) {
        super.getView(i, view, viewGroup);
        view = mInflater.inflate(R.layout.item_care_camera, null);

        Adapter_CareCamera.ViewHolder holder = new Adapter_CareCamera.ViewHolder();
        holder.tv_camera_name = view.findViewById(R.id.tv_camera_name);
        holder.video_view = view.findViewById(R.id.video_view);

        try {
            final JSONObject itemJson = dataArray.getJSONObject(i);
            String tv_camera_name = itemJson.getString("monitor_desc");
            String video_path = itemJson.getString("video_path");
            int monitor_id = itemJson.getInt("monitor_id");

            holder.tv_camera_name.setText(tv_camera_name);
            holder.video_view.setVideoPath(Constant.DOMAIN+"/uploads/rtmp/"+monitor_id+"/"+video_path);
            holder.video_view.start();

        } catch (Exception e) {
            Constant.debugInfo("Adapter_CareCamera", "数据解析异常在第" + i + "个item" + e);
        }


        return view;
    }

    public static class ViewHolder {
        TextView tv_camera_name;
        VideoView video_view;

    }

    public interface AdapterListener {
        public void onClickRootView(JSONObject personInfoJson, int id);
    }

    Adapter_CarePerson.AdapterListener adapterListener = null;

    public void addCarePersonAdapterListener(Adapter_CarePerson.AdapterListener listener) {
        adapterListener = listener;
    }
}
