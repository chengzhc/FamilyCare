package com.czstudio.czlibrary.service;

import android.app.Activity;
import android.util.Log;

import com.czstudio.familycare.Constant;

import org.json.JSONObject;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class CzTcp {
    static final String TAG = "CzTCP";

    static final int MESSAGE_TYPE_ERROR = 10,
            MESSAGE_TYPE_INFO = 11,
            TCP_DATA_TYPE_HEART_BEAT = 100,
            TCP_DATA_TYPE_LOCATION=101,
            TCP_DATA_TYPE_SAME_LOCATION=102;;

    CzTcp instance;
    Activity activity;
    boolean initDone = false;

    boolean isHeartBeat = false;

    Socket socket;
    String ip;
    int port;

    String devSerial;

    JSONObject heartbeatJson,locationJson,sameLocationJson;

    ArrayList<String> sendQueue;

    String location;

    Thread threadHeartBeat;

    public CzTcp() {

    }

    public CzTcp(Activity activity, String ip, int port, String devSerial) {
        instance = this;
        this.activity = activity;
        this.ip = ip;
        if (this.ip == null) {
            callbackMessage(MESSAGE_TYPE_ERROR, "NULL IP!!!");
            initDone = false;
            return;
        }
        this.port = port;
        if (this.port < 1) {
            callbackMessage(MESSAGE_TYPE_ERROR, "Negative PORT!!!");
            initDone = false;
            return;
        }
        this.devSerial = devSerial;
        if (this.devSerial == null) {
            this.devSerial = "";
        }

        heartbeatJson = new JSONObject();
        try {
            heartbeatJson.put("type", TCP_DATA_TYPE_HEART_BEAT);
            heartbeatJson.put("serial", this.devSerial);
        } catch (Exception e) {
            callbackMessage(MESSAGE_TYPE_ERROR, "Init heart beat json Exp:" + e);
            initDone = false;
            return;
        }

        locationJson=new JSONObject();
        try {
            locationJson.put("type", TCP_DATA_TYPE_LOCATION);
            locationJson.put("serial", this.devSerial);
        } catch (Exception e) {
            callbackMessage(MESSAGE_TYPE_ERROR, "Init location json Exp:" + e);
            initDone = false;
            return;
        }

        sameLocationJson=new JSONObject();
        try {
            sameLocationJson.put("type", TCP_DATA_TYPE_SAME_LOCATION);
            sameLocationJson.put("serial", this.devSerial);
        } catch (Exception e) {
            callbackMessage(MESSAGE_TYPE_ERROR, "Init sameLocationJson Exp:" + e);
            initDone = false;
            return;
        }

        location = "360,360";
        initDone = true;
        sendQueue = new ArrayList<String>();
        initThreadHeartBeat();

        connect();
    }

    void initThreadHeartBeat(){
        threadHeartBeat=new Thread(){
            public void run(){
                while (isHeartBeat) {
                    try {
                        heartbeatJson.put("time", getCurrentTimeStr());
                        heartbeatJson.put("stamp", System.currentTimeMillis() / 1000);
                        threadSendData(heartbeatJson.toString());
                        Thread.sleep(5000);
                    } catch (Exception e) {
                        closeSocket();
                        callbackMessage(MESSAGE_TYPE_ERROR, "connect()/ Sleep Exp:" + e);
                        callbackConnectStatus(false);
                        reconnect();
                    }
                }
            }
        };
    }

    public void reconnect() {
        isHeartBeat=false;
        try {
            Thread.sleep(5000);
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    connect();
                }
            });
        } catch (Exception e1) {
            callbackMessage(MESSAGE_TYPE_ERROR, "reconnect() Exp:" + e1);
            try{
                Thread.sleep(5000);
                reconnect();
            }catch (Exception e2){
                callbackMessage(MESSAGE_TYPE_ERROR, "reconnect() Sleep Exp:" + e1);
            }
        }
    }

    void startHeartBeat(){
        Log.e(TAG,"startHeartBeat");
        Thread.State threadState=threadHeartBeat.getState();
        isHeartBeat=true;
        switch (threadState){
            case NEW:
                callbackMessage(MESSAGE_TYPE_INFO,"startHeartBeat threadState=NEW");
                threadHeartBeat.start();
                break;
            case RUNNABLE:
                callbackMessage(MESSAGE_TYPE_INFO,"startHeartBeat threadState=RUNNABLE");
                break;
            case BLOCKED:
                callbackMessage(MESSAGE_TYPE_INFO,"startHeartBeat threadState=BLOCKED");
                break;
            case WAITING:
                callbackMessage(MESSAGE_TYPE_INFO,"startHeartBeat threadState=WAITING");
                break;
            case TIMED_WAITING:
                callbackMessage(MESSAGE_TYPE_INFO,"startHeartBeat threadState=TIMED_WAITING");
                break;
            case TERMINATED:
                callbackMessage(MESSAGE_TYPE_INFO,"startHeartBeat threadState=TERMINATED");
                initThreadHeartBeat();
                threadHeartBeat.start();
                break;
        }
    }

    void stopHeartBeat(){
        isHeartBeat=false;
    }

    public void sendData(final String msg) {
        new Thread() {
            public void run() {
                threadSendData(msg);
            }
        }.start();
    }

    public void setLocation(double latitude,double longitude){
        setLocation(latitude,longitude,-1,"OTHER","",0);
    }


    public void setLocation(double latitude,double longitude,int batteryPercent,
                            String netType,String currSSID,int stepsFromLastCheck){
        try {
            locationJson.put("lati", latitude);
            locationJson.put("longi", longitude);
            locationJson.put("bat", batteryPercent);
            locationJson.put("net_type", netType);
            locationJson.put("ssid", currSSID);
            locationJson.put("steps", stepsFromLastCheck);
            sendData(locationJson.toString());
            locationJson.put("lati", 0);
            locationJson.put("longi", 0);
            locationJson.put("net_type", "");
            locationJson.put("ssid", "");
            locationJson.put("steps", stepsFromLastCheck);

            //locationJson.put("bat", -1);
        }catch (Exception e){
            Log.e(TAG,"setLocation Exp:"+e);
        }
    }

    public void setSameLocation(int batteryPercent,String netType,String currSSID,int stepsFromLastCheck){
        try {
            sameLocationJson.put("bat", batteryPercent);
            sameLocationJson.put("net_type", netType);
            sameLocationJson.put("ssid", currSSID);
            sameLocationJson.put("steps", stepsFromLastCheck);
            sendData(sameLocationJson.toString());
        }catch (Exception e ){
            Log.e(TAG,"setSameLocation(double latitude,double longitude,int batteryPercent) exp:"+e);
        }
    }

    public void setSameLocation(){
        setSameLocation(-1,"","",0);
    }

    void connect() {
        Log.e(TAG, "connect()");

        new Thread() {
            public void run() {
                try {
                    socket = new Socket(ip, port);
                    socket.setSoTimeout(5000);
                    callbackConnectStatus(true);
                    startHeartBeat();
                } catch (Exception e) {
                    callbackConnectStatus(false);
                    callbackMessage(MESSAGE_TYPE_ERROR, "connect()/ Exp:" + e);
                    reconnect();
                }
            }
        }.start();
    }

//    void threadHeartBeat() {
//        isHeartBeat=true;
//        while (isHeartBeat) {
//            //主线程提示
//            activity.runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    if (czTcpListener != null) {
//                        czTcpListener.onHeartBeat(instance, heartbeatJson);
//                    }
//                }
//            });
//            //发送数据
//
//        }
//    }

    void threadSendData(String msg) {
        if (socket == null) {
            return;
        }
        try {
            // 2.拿到客户端的socket对象的输出流发送给服务器数据
            OutputStream os = socket.getOutputStream();

            //写入要发送给服务器的数据
            os.write(msg.getBytes());
            os.flush();

            //拿到socket的输入流，这里存储的是服务器返回的数据
            InputStream is = socket.getInputStream();
            byte[] data = new byte[1024 * 1024];
            int len = is.read(data);
            if (len < 0) {
                callbackMessage(MESSAGE_TYPE_ERROR, "CzTcp/sendData() readlength<0");
                return;
            }
            final String str = new String(data, 0, len);
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    czTcpListener.onReceiveData(instance, str);
                }
            });
        } catch (Exception e) {
            callbackMessage(MESSAGE_TYPE_ERROR, "CzTcp/sendData() Exp:" + e);
            reconnect();
        }
    }

    void closeSocket() {
        callbackConnectStatus(false);
        isHeartBeat=false;
        try {
            socket.close();
        } catch (Exception e) {
            callbackMessage(MESSAGE_TYPE_ERROR, "closeSocket() / exp:" + e);
        }
    }

    public void destroy(){
        isHeartBeat=false;
    }

    void callbackMessage(final int msgType, final String msg) {
        if (czTcpListener == null) {
            return;
        }
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (msgType == MESSAGE_TYPE_ERROR) {
                    czTcpListener.onError(instance, msg);
                } else if (msgType == MESSAGE_TYPE_INFO) {
                    czTcpListener.onInfo(instance, msg);
                }
            }
        });
    }

    void callbackConnectStatus(final boolean isConnect) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                czTcpListener.onConnectStatus(instance, isConnect);
            }
        });
    }


    //=============================================================================

//
//    void sendHeartBeat() {
//        activity.runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                if(czTcpListener!=null){
//                    czTcpListener.onHeartBeat(instance,heartbeatJson);
//                }
//            }
//        });
//        try {
//            heartbeatJson.put("time", getCurrentTimeStr());
//            heartbeatJson.put("stamp",System.currentTimeMillis()/1000);
//            sendData(heartbeatJson.toString());
//            heartbeatJson.remove("lati");
//            heartbeatJson.remove("longi");
//        } catch (Exception ex) {
//            closeSocket();
//            callbackConnectStatus(false);
//            activity.runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    connect();
//                }
//            });
//        }
//    }
//

//

//


    public interface CzTcpListener {
        public void onConnectStatus(CzTcp czTcp, boolean isConnect);

        public void onReceiveData(CzTcp czTcp, String rxString);

        public void onInfo(CzTcp czTcp, String info);

        public void onError(CzTcp czTcp, String error);

        public void onHeartBeat(CzTcp czTcp, JSONObject heartBratJson);
    }

    CzTcpListener czTcpListener = null;

    public void setCzTcpListener(CzTcpListener listener) {
        czTcpListener = listener;
    }

    public static String getCurrentTimeStr() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(System.currentTimeMillis()));
    }
}
