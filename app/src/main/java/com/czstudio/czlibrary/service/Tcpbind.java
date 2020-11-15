package com.czstudio.czlibrary.service;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Binder;

import com.czstudio.czlibrary.CzLibrary;
import com.czstudio.familycare.Constant;
import com.czstudio.familycare.model.ModelUser;

import org.json.JSONObject;

public class Tcpbind extends Binder {
    public static final String TAG = "Tcpbind";
    public CzTcp tcp;

    public void connectTcp(final Activity instance) {

        if (instance.checkSelfPermission(Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            CzLibrary.alertConfirm(instance, "提示", "您没有同意权限，APP将无法正常工作", new CzLibrary.AlertCallBack() {
                @Override
                public void onAlertConfirm() {
                    //System.exit(0);
                }

                @Override
                public void onAlertCancel() {
                    //System.exit(0);
                }
            });

            return;
        }
        Constant.debugInfo(TAG, "---------------------------- Constant.devSerial=" + ModelUser.getInstance(instance).getHw_serial());

        tcp = new CzTcp(instance, Constant.TCP_IP, Constant.TCP_PORT, ModelUser.getInstance(instance).getHw_serial());

        tcp.setCzTcpListener(new CzTcp.CzTcpListener() {
            @Override
            public void onConnectStatus(final CzTcp czTcp, boolean isConnect) {
                setTcpConnected(isConnect);
            }

            @Override
            public void onReceiveData(CzTcp czTcp, String rxString) {
                showReceive(rxString);
            }

            @Override
            public void onInfo(CzTcp czTcp, String info) {
                //Toast.makeText(instance, info, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(CzTcp czTcp, String error) {
                setTcpConnected(false);
                Constant.debugInfo(TAG, "onError:" + error);
            }

            @Override
            public void onHeartBeat(CzTcp czTcp, JSONObject heartBratJson) {
                if (tcpBinderListener != null) {
                    tcpBinderListener.onTcpHeartBeat(heartBratJson);
                }
            }
        });
    }

    void setTcpConnected(boolean isConnect) {
        if (tcpBinderListener != null) {
            tcpBinderListener.onTcpConnectedStatus(isConnect);
        }
    }

    void showReceive(String rxString) {
        if (tcpBinderListener != null) {
            tcpBinderListener.onTcpReceiver(rxString);
        }
    }

//    public void sendData(String dataStr) {
//        tcp.sendData(dataStr);
//    }

    public interface TcpBinderListener {
        public void onTcpConnectedStatus(boolean isConnected);

        public void onTcpReceiver(String rxString);

        public void onTcpHeartBeat(JSONObject heartBeatJson);
    }

    TcpBinderListener tcpBinderListener = null;

    public void setTcpBinderListener(TcpBinderListener listener) {
        tcpBinderListener = listener;
    }
}