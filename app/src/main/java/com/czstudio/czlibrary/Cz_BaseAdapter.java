package com.czstudio.czlibrary;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import org.json.JSONArray;

/**
 * Created by cheng on 2017/12/26.
 */

public class Cz_BaseAdapter extends BaseAdapter {


    public JSONArray dataArray;
    public LayoutInflater mInflater;

    public Cz_BaseAdapter() {

    }

    public Cz_BaseAdapter(Context parentContext, JSONArray listData) {
        dataArray = listData;
        mInflater = LayoutInflater.from(parentContext);
    }

    public void init(Context parentContext, JSONArray listData) {
        dataArray = listData;
        mInflater = LayoutInflater.from(parentContext);
    }

    @Override
    public int getCount() {
        return dataArray.length();
    }

    @Override
    public Object getItem(int i) {
        try {
            return dataArray.getJSONObject(i);
        } catch (Exception e) {
            return null;
        }
    }

    public void refresh(JSONArray array){
        this.dataArray=array;
        notifyDataSetChanged();
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        return view;
    }
}
