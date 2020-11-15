package com.czstudio.familycare.model;

import android.util.Log;
import android.widget.Toast;

import com.czstudio.czlibrary.CzLibrary;
import com.czstudio.familycare.MainActivity;

import org.json.JSONObject;

public class TcpReceivePorcessor {
    static final String TAG= "TcpCommandProcessor";

    static TcpReceivePorcessor instance;
    MainActivity mainActivity;

    public static TcpReceivePorcessor getInstance(MainActivity mainActivity){
        if(instance==null){
            instance=new TcpReceivePorcessor();
        }
        instance.mainActivity=mainActivity;
        return instance;
    }

    public void processReceive(String msg){
        try{
            JSONObject jobj=new JSONObject(msg);
            String type=jobj.getString("type");
            if(type.equals("cmd")){
                execCommand(jobj.getString("content"));
            }
            if(type.equals("info")){
                showInfo(jobj.getString("content"));
            }
        }catch (Exception e){
            Log.e(TAG,"processReceive Exp: "+e);
        }
    }

    public void execCommand(String cmd){
        toastInfo(cmd);
    }

    public void showInfo(String info){
        CzLibrary.alert(mainActivity,info);
    }

    public void toastInfo(String info){
        Toast.makeText(mainActivity.getBaseContext(),info,Toast.LENGTH_LONG).show();
    }
}
