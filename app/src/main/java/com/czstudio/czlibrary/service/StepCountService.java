package com.czstudio.czlibrary.service;

import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.czstudio.czlibrary.CzLibrary;

/**
 * AndroidMenifest
 * <service
 *             android:name="com.czstudio.czlibrary.service.StepCountService"
 *             android:enabled="true"
 *             android:exported="true" />
 *
 * 声明：
 * StepCountService.StepCount stepCount;
 * ==============================================
 * 初始化启动：
 * void initStepSensor(){
 *         try{
 *             Intent stepCountService = new Intent(this, StepCountService.class);
 *             bindService(stepCountService, new ServiceConnection() {
 *                 @Override
 *                 public void onServiceConnected(ComponentName name, IBinder service) {
 *                     stepCount=(StepCountService.StepCount) service;
 *                 }
 *
 *                 @Override
 *                 public void onServiceDisconnected(ComponentName name) {
 *
 *                 }
 *             }, BIND_AUTO_CREATE);
 *             startService(stepCountService);
 *         }catch (Exception e){
 *             Constant.debugInfo(TAG,"init Step Service Exp:"+e);
 *         }
 *     }
 * ===============================================
 * 调用：
 * final int steps=stepCount==null?0:stepCount.getStepsFromLastCheck();
 */

public class StepCountService extends Service {
    static String TAG="StepCountService";
    static final int MESSAGE_TYPE_NO_STEPS=100,
            MESSAGE_TYPE_GET_STEPS=101,
            MESSAGE_TYPE_INFO=102;
    StepCount stepCount;

    public StepCountService() {

    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.e("StepCountService","  ----->  onBind");
        if(stepCount==null){
            stepCount=new StepCount();
        }
        return stepCount;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.e("StepCountService","  ----->  onCreate");
        stepCount=new StepCount();
    }


    @Override
    public int onStartCommand(Intent intent,int flags, int startId) {
        Log.e("StepCountService","  ----->  onStartCommand");

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e("StepCountService","  ----->  onDestroy");
        stepCount.isCount=false;
    }

    public class StepCount extends Binder implements SensorEventListener{
        Handler handler;
        Thread countThread;
        boolean isCount=false;
        int totalStepsFromLaunch=0;
        int stepsFromLastCheck=0;
        boolean hasRecord=false;
        int hasStepCount=0;
        int previousStepCount=0;
        int nowStepCount=0;

        SensorManager sensorManager;
        int stepSensorType;
        public StepCount(){
            initHandler();
            startStepDetector();
        }

        void initHandler(){
            handler=new Handler(){
                @Override
                public void handleMessage(@NonNull Message msg) {
                    super.handleMessage(msg);
                    switch(msg.what){
                        case MESSAGE_TYPE_NO_STEPS:
                            Log.e(TAG,"MESSAGE_TYPE_NO_STEPS at:"+ CzLibrary.getTimeStringFromTimeStamp(""+msg.getWhen()/1000));
                            break;
                        case MESSAGE_TYPE_GET_STEPS:
                            Toast.makeText(getApplicationContext(),"获取计步数据"+msg.arg1,Toast.LENGTH_LONG).show();
                            Log.e(TAG,"MESSAGE_TYPE_GET_STEPS:"+msg.arg1+ " at:"+ CzLibrary.getTimeStringFromTimeStamp(""+msg.getWhen()/1000));
                            stepsFromLastCheck=msg.arg1;
                            break;
                        case MESSAGE_TYPE_INFO:
                            try {
                                Bundle b = msg.getData();
                                Toast.makeText(getApplicationContext(),b.getString("info"),Toast.LENGTH_LONG).show();
                            }catch (Exception e){
                                Log.e(TAG,"MESSAGE_TYPE_INFO Exp:"+e);
                            }

                            break;
                    }
                }
            };
        }

        public int getTotalStepsFromLaunch(){
            return totalStepsFromLaunch;
        }
        public int getStepsFromLastCheck(){
            int temp=stepsFromLastCheck;
            stepsFromLastCheck=0;
            return temp;
        }

        /**
         * 选择计步数据采集的传感器
         * SDK大于等于19，开启计步传感器，小于开启加速度传感器
         */
        private void startStepDetector() {
            if (sensorManager != null) {
                sensorManager = null;
            }
            //获取传感器管理类
            sensorManager = (SensorManager) getApplicationContext().getSystemService(SENSOR_SERVICE);
            int versionCodes = Build.VERSION.SDK_INT;//取得SDK版本
            if (versionCodes >= 19) {
                //SDK版本大于等于19开启计步传感器
                addCountStepListener();
            }
//            else {        //小于就使用加速度传感器
//                addBasePedometerListener();
//            }
        }

        /**
         * 启动计步传感器计步
         */
        private void addCountStepListener() {
            Sensor countSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
            Sensor detectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);
            if (countSensor != null) {
                stepSensorType = Sensor.TYPE_STEP_COUNTER;
                Message msg=new Message();
                msg.what=MESSAGE_TYPE_INFO;
                Bundle b=new Bundle();
                b.putString("info","Sensor.TYPE_STEP_COUNTER");
                msg.setData(b);
                handler.sendMessage(msg);
                sensorManager.registerListener(this, countSensor, SensorManager.SENSOR_DELAY_NORMAL);
                Log.e("计步传感器类型", "Sensor.TYPE_STEP_COUNTER");
            }else if (detectorSensor != null) {
                stepSensorType = Sensor.TYPE_STEP_DETECTOR;
                Message msg=new Message();
                msg.what=MESSAGE_TYPE_INFO;
                Bundle b=new Bundle();
                b.putString("info","Sensor.TYPE_STEP_DETECTOR");
                msg.setData(b);
                handler.sendMessage(msg);
                sensorManager.registerListener(this, detectorSensor, SensorManager.SENSOR_DELAY_NORMAL);
                Log.e("计步传感器类型", "Sensor.TYPE_STEP_DETECTOR");
            } else {
                Message msg=new Message();
                msg.what=MESSAGE_TYPE_INFO;
                Bundle b=new Bundle();
                b.putString("info","Sensor.TYPE_STEP_NONE");
                msg.setData(b);
                handler.sendMessage(msg);
                Log.e("计步传感器类型", "Sensor.TYPE_STEP_NONE");
            }
        }

        @Override
        public void onSensorChanged(SensorEvent event) {
            //这种类型的传感器返回步骤的数量由用户自上次重新启动时激活。返回的值是作为浮动(小数部分设置为0),
            // 只在系统重启复位为0。事件的时间戳将该事件的第一步的时候。这个传感器是在硬件中实现,预计低功率。
            if (stepSensorType == Sensor.TYPE_STEP_COUNTER) {
                //获取当前传感器返回的临时步数
                int tempStep = (int) event.values[0];
                //首次如果没有获取手机系统中已有的步数则获取一次系统中APP还未开始记步的步数
                if (!hasRecord) {
                    hasRecord = true;
                    hasStepCount = tempStep;
                } else {
                    //获取APP打开到现在的总步数=本次系统回调的总步数-APP打开之前已有的步数
                    int thisStepCount = tempStep - hasStepCount;
                    //本次有效步数=（APP打开后所记录的总步数-上一次APP打开后所记录的总步数）
                    int thisStep = thisStepCount - previousStepCount;
                    //总步数=现有的步数+本次有效步数
                    stepsFromLastCheck += (thisStep);
                    //记录最后一次APP打开到现在的总步数
                    previousStepCount = thisStepCount;
                }
            } else if (stepSensorType == Sensor.TYPE_STEP_DETECTOR) {
                if (event.values[0] == 1.0) {
                    stepsFromLastCheck++;
                }
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    }
}
