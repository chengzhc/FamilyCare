package com.czstudio.czlibrary;

import android.app.Activity;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.baidu.aip.asrwakeup3.AutoCheck;
import com.baidu.speech.EventListener;
import com.baidu.speech.EventManager;
import com.baidu.speech.EventManagerFactory;
import com.baidu.speech.asr.SpeechConstant;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.LinkedHashMap;
import java.util.Map;

public class BaiduVoiceRecognize {
    static String TAG="BaiduVoiceRecognize";
    public static final int LISTENER_DATA_TYPE_TEXT=100,
            LISTENER_DATA_TYPE_INFO=101,
            LISTENER_DATA_TYPE_EXCEPTION=102,
            LISTENER_DATA_TYPE_ON_START=103,
            LISTENER_DATA_TYPE_ON_END=104,
            LISTENER_DATA_TYPE_ON_EXIT=105;

    public static BaiduVoiceRecognize baiduVoiceRecognize;
    Activity activity;


    EventManager asr;
    EventListener yourListener;
    boolean enableOffline=false;

    public BaiduVoiceRecognize(){

    }

    public static BaiduVoiceRecognize getInstance(Activity activity){
        if(baiduVoiceRecognize==null){
            baiduVoiceRecognize=new BaiduVoiceRecognize();
        }
        baiduVoiceRecognize.activity=activity;
        baiduVoiceRecognize.initSystem();
        return baiduVoiceRecognize;
    }

    void initSystem(){
        Log.e(TAG,"initSystem");
        asr = EventManagerFactory.create(activity, "asr");
        yourListener = new EventListener() {
            @Override
            public void onEvent(String name, String params, byte [] data, int offset, int length) {
                Log.e(TAG,"initSystem / onEvent name=:"+name);
                if(name.equals(SpeechConstant.CALLBACK_EVENT_ASR_READY)){
                // 引擎就绪，可以说话，一般在收到此事件后通过UI通知用户可以说话了
                }
                if(name.equals(SpeechConstant.CALLBACK_EVENT_ASR_FINISH)){
                // 识别结束
                }
//                if(name.equals(SpeechConstant.CALLBACK_EVENT_ASR_BEGIN)){
//                    sendListenerData(LISTENER_DATA_TYPE_ON_START,"");
//                }
                if(name.equals(SpeechConstant.CALLBACK_EVENT_ASR_READY)){
                    sendListenerData(LISTENER_DATA_TYPE_ON_START,"");
                }
                if(name.equals(SpeechConstant.CALLBACK_EVENT_ASR_END)){
                    sendListenerData(LISTENER_DATA_TYPE_ON_END,"");
                }
                if(name.equals(SpeechConstant.CALLBACK_EVENT_ASR_EXIT)){
                    sendListenerData(LISTENER_DATA_TYPE_ON_EXIT,"");
                }

                if(name.equals(SpeechConstant.CALLBACK_EVENT_ASR_PARTIAL)){
                    try{
                        JSONObject jobj=new JSONObject(params);
                        JSONArray textArray=jobj.getJSONArray("results_recognition");
                        sendListenerData(LISTENER_DATA_TYPE_TEXT,textArray.get(0).toString());
                    }catch(Exception e){
                        sendListenerData(LISTENER_DATA_TYPE_EXCEPTION,"获取关键词成功，解析JSON 异常："+e);
                    }
                }
                // ... 支持的输出事件和事件支持的事件参数见“输入和输出参数”一节
            }
        };
        asr.registerListener(yourListener);

    }

    public void start(){
        Map<String, Object> params = new LinkedHashMap<String, Object>();
        String event = null;
        event = SpeechConstant.ASR_START; // 替换成测试的event

        if (enableOffline) {
            params.put(SpeechConstant.DECODER, 2);
        }
        // 基于SDK集成2.1 设置识别参数
        params.put(SpeechConstant.ACCEPT_AUDIO_VOLUME, false);
        // params.put(SpeechConstant.NLU, "enable");
        // params.put(SpeechConstant.VAD_ENDPOINT_TIMEOUT, 0); // 长语音
        // params.put(SpeechConstant.IN_FILE, "res:///com/baidu/android/voicedemo/16k_test.pcm");
        // params.put(SpeechConstant.VAD, SpeechConstant.VAD_DNN);
        // params.put(SpeechConstant.PID, 1537); // 中文输入法模型，有逗号

        /* 语音自训练平台特有参数 */
        // params.put(SpeechConstant.PID, 8002);
        // 语音自训练平台特殊pid，8002：搜索模型类似开放平台 1537  具体是8001还是8002，看自训练平台页面上的显示
        // params.put(SpeechConstant.LMID,1068); // 语音自训练平台已上线的模型ID，https://ai.baidu.com/smartasr/model
        // 注意模型ID必须在你的appId所在的百度账号下
        /* 语音自训练平台特有参数 */

        // 请先使用如‘在线识别’界面测试和生成识别参数。 params同ActivityRecog类中myRecognizer.start(params);
        // 复制此段可以自动检测错误
        (new AutoCheck(activity.getApplicationContext(), new Handler() {
            public void handleMessage(Message msg) {
                if (msg.what == 100) {
                    AutoCheck autoCheck = (AutoCheck) msg.obj;
                    synchronized (autoCheck) {
                        String message = autoCheck.obtainErrorMessage(); // autoCheck.obtainAllMessage();
                        sendListenerData(LISTENER_DATA_TYPE_INFO,message);
                        ; // 可以用下面一行替代，在logcat中查看代码
                        // Log.w("AutoCheckMessage", message);
                    }
                }
            }
        },enableOffline)).checkAsr(params);
        String json = null; // 可以替换成自己的json
        json = new JSONObject(params).toString(); // 这里可以替换成你需要测试的json
        asr.send(event, json, null, 0, 0);
        sendListenerData(LISTENER_DATA_TYPE_INFO,"输入参数：" + json);
    }

    public void stop(){
        asr.send(SpeechConstant.ASR_STOP, null, null, 0, 0);
    }

    void sendListenerData(final int type,final String msg){
        if(baiduVoiceRecognizeListaner==null){
            Log.e(TAG,"baiduVoiceRecognizeListaner==null");
            return;
        }
        if(activity==null){
            Log.e(TAG,"activity==null");
            return;
        }
        try{
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    switch (type){
                        case LISTENER_DATA_TYPE_TEXT:
                            baiduVoiceRecognizeListaner.onGetRecognize(baiduVoiceRecognize,msg);
                            break;
                        case LISTENER_DATA_TYPE_INFO:
                            baiduVoiceRecognizeListaner.onGetInfo(baiduVoiceRecognize,msg);
                            break;
                        case LISTENER_DATA_TYPE_EXCEPTION:
                            baiduVoiceRecognizeListaner.onGetException(baiduVoiceRecognize,msg);
                            break;
                        case LISTENER_DATA_TYPE_ON_START:
                            baiduVoiceRecognizeListaner.onStart(baiduVoiceRecognize);
                            break;
                        case LISTENER_DATA_TYPE_ON_END:
                            baiduVoiceRecognizeListaner.onEnd(baiduVoiceRecognize);
                            break;
                        case LISTENER_DATA_TYPE_ON_EXIT:
                            baiduVoiceRecognizeListaner.onExit(baiduVoiceRecognize);
                            break;
                    }
                }
            });
        }catch (Exception e){
            Log.e(TAG,"sendListenerData Exp:"+e);
        }
    }


    BaiduVoiceRecognizeListaner baiduVoiceRecognizeListaner=null;
    public interface BaiduVoiceRecognizeListaner{
        public void onGetRecognize(BaiduVoiceRecognize instance,String text);
        public void onGetInfo(BaiduVoiceRecognize instance,String info);
        public void onGetException(BaiduVoiceRecognize instance,String exception);
        //识别开始
        public void onStart(BaiduVoiceRecognize instance);
        //发现关键词结束，后面可自动开始识别
        public void onEnd(BaiduVoiceRecognize instance);
        //退出不在识别
        public void onExit(BaiduVoiceRecognize instance);

    }

    public void setBaiduVoiceRecognizeListaner(BaiduVoiceRecognizeListaner listener){
        baiduVoiceRecognizeListaner=listener;
    }

}
