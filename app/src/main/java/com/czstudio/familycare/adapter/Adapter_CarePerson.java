package com.czstudio.familycare.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.czstudio.czlibrary.CzNetworkStatus;
import com.czstudio.czlibrary.Cz_BaseAdapter;
import com.czstudio.familycare.Constant;
import com.czstudio.familycare.R;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;


public class Adapter_CarePerson extends Cz_BaseAdapter  {
    String TAG=getClass().getSimpleName();
    public Adapter_CarePerson(Context parentContext, JSONArray listData) {
        super.init(parentContext, listData);
        Constant.debugInfo(TAG,"onCreate");
    }

    @Override
    public View getView(final int i, View view, ViewGroup viewGroup) {
        super.getView(i, view, viewGroup);

        view = mInflater.inflate(R.layout.item_care_person, null);

        ViewHolder holder = new ViewHolder();

        holder.iv_avatar = view.findViewById(R.id.iv_avatar);
        holder.tv_name = view.findViewById(R.id.tv_name);
        holder.tv_birth = view.findViewById(R.id.tv_birth);
        holder.tv_blood = view.findViewById(R.id.tv_blood);
        holder.tv_btn_camera = view.findViewById(R.id.tv_btn_camera);
        holder.tv_btn_location = view.findViewById(R.id.tv_btn_location);
        holder.tv_btn_all = view.findViewById(R.id.tv_btn_helthy);
        holder.tv_is_home = view.findViewById(R.id.tv_is_home);
        holder.iv_is_home = view.findViewById(R.id.iv_is_home);
        holder.iv_linked= view.findViewById(R.id.iv_linked);
        holder.tv_offline= view.findViewById(R.id.tv_offline);
        holder.tv_btn_alert=view.findViewById(R.id.tv_btn_alert);
        holder.tv_bat=view.findViewById(R.id.tv_bat);
        holder.iv_bat=view.findViewById(R.id.iv_bat);

        try {
            final JSONObject itemJson = dataArray.getJSONObject(i);
            String mobile=itemJson.getString("mobile");
            String avatar = itemJson.getString("avatar");
            String name = itemJson.getString("name");
            if(name.length()==0){
                name=mobile;
            }
            String birth = ""+itemJson.getInt("birth_y")
                    +"-"+itemJson.getInt("birth_m")
                    +"-"+itemJson.getInt("birth_d");

            String blood = itemJson.getString("blood");
            String net_type = itemJson.getString("net_type");
            String curr_ssid = itemJson.getString("curr_ssid");
            String home_wifi = itemJson.getString("home_wifi");
            int last_stamp =itemJson.getInt("last_stamp");
            int bat=itemJson.getInt("bat");

            holder.tv_name.setText(name);
            holder.tv_birth.setText(birth);
            holder.tv_blood.setText(blood);
            Picasso.with(holder.iv_avatar.getContext()).load(Constant.DOMAIN + avatar)
                    .placeholder(R.drawable.icon)
                    .error(R.drawable.icon)
//                    .centerCrop()
                    .into(holder.iv_avatar);


            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (adapterListener != null) {
                        adapterListener.onClickItemRootView(itemJson,view, i);
                    }
                }
            });

            ArrayList<View> clickViewArray= new ArrayList<View>();
            clickViewArray.add(holder.tv_btn_camera);
            clickViewArray.add(holder.tv_btn_location);
            clickViewArray.add(holder.tv_btn_all);
            clickViewArray.add(holder.tv_btn_alert);

            for(View v:clickViewArray){
                if(v!=null){
                    v.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (adapterListener != null) adapterListener.onClickItemChildView(itemJson,v,i);
                        }
                    });
                }
            }

            if(net_type.length()==0){
                holder.tv_is_home.setText("已离线");
                Picasso.with(holder.iv_is_home.getContext()).load(R.drawable.ic_location_off_blue_700_36dp)
                        .into(holder.iv_is_home);
            }else{
                if(net_type.equals(CzNetworkStatus.NET_TYPE_WIFI_STR)&&
                        (curr_ssid.equals(home_wifi)||curr_ssid.equals(home_wifi+"-5G"))){
                    holder.tv_is_home.setText("在家中");
                    Picasso.with(holder.iv_is_home.getContext()).load(R.drawable.ic_home_blue_700_36dp)
                            .into(holder.iv_is_home);
                }else{
                    holder.tv_is_home.setText("已外出");
                    Picasso.with(holder.iv_is_home.getContext()).load(R.drawable.ic_nature_people_blue_700_36dp)
                            .into(holder.iv_is_home);
                }
            }

            long interval=System.currentTimeMillis()/1000-last_stamp;

            if(interval>60){
                Picasso.with(holder.iv_linked.getContext()).load(R.drawable.ic_leak_remove_red_700_36dp)
                        .into(holder.iv_linked);
                holder.tv_offline.setText(getOfflineTimeStr((int)interval));
                holder.tv_is_home.setText("未知位置");
                Picasso.with(holder.iv_is_home.getContext()).load(R.drawable.ic_location_off_blue_700_36dp)
                        .into(holder.iv_is_home);
                holder.tv_btn_alert.setVisibility(View.VISIBLE);
                Picasso.with(holder.iv_bat.getContext()).load(R.drawable.ic_battery_charging_full_grey_700_36dp)
                        .into(holder.iv_bat);
                holder.tv_bat.setVisibility(View.GONE);
                holder.iv_bat.setVisibility(View.GONE);
            }else{
                if(net_type.equals("WIFI")) {
                    Picasso.with(holder.iv_linked.getContext()).load(R.drawable.ic_network_wifi_green_700_36dp)
                            .into(holder.iv_linked);
                }else if(net_type.equals("MOBILE")){
                    Picasso.with(holder.iv_linked.getContext()).load(R.drawable.ic_network_cell_green_700_36dp)
                            .into(holder.iv_linked);
                }else{
                    Picasso.with(holder.iv_linked.getContext()).load(R.drawable.ic_leak_add_green_700_24dp)
                            .into(holder.iv_linked);
                }
                holder.tv_btn_alert.setVisibility(View.GONE);

                if(bat>0) {
                    holder.tv_bat.setVisibility(View.VISIBLE);
                    holder.iv_bat.setVisibility(View.VISIBLE);
                    holder.tv_bat.setText(bat + "%");
                    if (bat < 20) {
                        Picasso.with(holder.iv_bat.getContext()).load(R.drawable.ic_battery_charging_20_red_900_36dp)
                                .into(holder.iv_bat);
                    } else if (bat < 30) {
                        Picasso.with(holder.iv_bat.getContext()).load(R.drawable.ic_battery_charging_30_red_500_36dp)
                                .into(holder.iv_bat);
                    } else if (bat < 50) {
                        Picasso.with(holder.iv_bat.getContext()).load(R.drawable.ic_battery_charging_50_amber_800_36dp)
                                .into(holder.iv_bat);
                    } else if (bat < 80) {
                        Picasso.with(holder.iv_bat.getContext()).load(R.drawable.ic_battery_charging_80_lime_600_36dp)
                                .into(holder.iv_bat);
                    } else if (bat < 90) {
                        Picasso.with(holder.iv_bat.getContext()).load(R.drawable.ic_battery_charging_90_green_700_36dp)
                                .into(holder.iv_bat);
                    } else {
                        Picasso.with(holder.iv_bat.getContext()).load(R.drawable.ic_battery_charging_full_green_800_36dp)
                                .into(holder.iv_bat);
                    }
                }else{
                    holder.tv_bat.setVisibility(View.GONE);
                    holder.iv_bat.setVisibility(View.GONE);
                }
            }



        } catch (Exception e) {
            Constant.debugInfo("Adapter_CarePerson", "数据解析异常在第" + i + "个item" + e);
        }


        return view;
    }



    public static class ViewHolder {
        TextView tv_name, tv_birth, tv_blood,tv_btn_location,tv_btn_camera,tv_btn_all,tv_is_home,tv_offline,tv_btn_alert,tv_bat;
        ImageView iv_avatar,iv_is_home,iv_linked,iv_bat;


    }

    public interface AdapterListener {
        public void onClickItemRootView(JSONObject personInfoJson , View view , int itemId);
        public void onClickItemChildView(JSONObject personInfoJson , View view , int itemId);
    }

    AdapterListener adapterListener = null;

    public void addCarePersonAdapterListener(AdapterListener listener) {
        adapterListener = listener;
    }

    String getOfflineTimeStr(int sec){
        String str="离线";
        if(sec>1400000000){
            str="从未在线";
        }else if(sec>86400){
            str+=(sec/86400)+"天";
        }else if(sec>3600){
            str+=(sec/3600)+"小时";
        }else if(sec>60){
            str+=(sec/60)+"分钟";
        }else{
            str+=(sec)+"秒";
        }

        return str;
    }
}