package com.czstudio.familycare.custom_view;


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.Log;
import android.view.View;

import com.czstudio.czlibrary.CzLibrary;

import org.json.JSONArray;
import org.json.JSONObject;

public class HistoryHeartView extends View {
    String TAG="HealthyView";
    Paint paint;
    boolean isDrawBloodPressure=false,
            isDrawHeartRate=false,
            isDrawWeather=false,
            isDrawSport=false,
            isDrawEat=false,
            isDrawFeeling=false,
            isDrawEmotion=false,
            isDrawMedicine=false;

    Path pathBloodPressure,pathHeartRate,pathWeather,pathSport,pathEat,pathFeeling,pathEmotion,pathMedicine;
    JSONArray jArrBloodPressure,jArrHeartRate,jArrWeather,jArrSport,jArrEat,jArrFeeling,jArrEmotion,jArrMedicine;

    long startStamp,stopStamp;//时间段，在setDuration()内会做24小时取整处理
    int days;//计算出多少天
    int spliteInDay=12;//一天分几段

    float mainWidth,mainHeight;//主界面尺寸
    float leftWidth,bottomHeight;
    float coordinateStartX,coordinateStartY,coordinateWidth,coordinateHeight,//坐标轴尺寸
            coordinateIntervalX,coordinateIntervalY,//坐标轴刻度间隔尺寸
            coordinateYMinValue,coordinateYMaxValue,coordinateYD_Value,coordinateYIntervalValue,//Y坐标轴最大值，最小值，刻度长度，最小刻度
            markSizeX,markSizeY,//坐标刻度总数
            markLineBaseLength,//刻度线基本长度
            testEnd;
    int paintCoodinateMarkTextSize=30;

    JSONArray dataArray=new JSONArray();

    int colorHiPressure=0xFFEE3333,colorLoPressure=0xFF3333dd,colorHeartRate=0xFF33DD33,colorTime=0xFF808080;

    public HistoryHeartView(Context context) {
        super(context);
        paint = new Paint();
        initPaint();
        initPath();
    }

    void initPaint() {
        paint.setAntiAlias(true);
        paint.setStrokeWidth(10);
        paint.setARGB(200, 0, 0x99, 0xcc);
        paint.setTextSize(paintCoodinateMarkTextSize);
        paint.setStyle(Paint.Style.FILL);
        paint.setTextAlign(Paint.Align.CENTER);
    }

    void initPath(){
        pathBloodPressure=new Path();
        pathHeartRate=new Path();
        pathWeather=new Path();
        pathSport=new Path();
        pathEat=new Path();
        pathFeeling=new Path();
        pathEmotion=new Path();
        pathMedicine=new Path();
    }

    public void showData(JSONArray dataArray){
        this.dataArray=dataArray;
        invalidate();
    }

    public void setCoodinate(long startStamp,long stopStamp,int coordinateYMinValue,int coordinateYMaxValue,int coordinateYIntervalValue){
        this.startStamp=86400*(startStamp/86400)-28800;
        this.stopStamp=86400*(stopStamp/86400)-28800;
        if(stopStamp%86400>0){
            this.stopStamp+=86400;
        }
        days=(int) (this.stopStamp-this.startStamp)/86400;
        Log.e(TAG,"days="+days);
        this.coordinateYMinValue=coordinateYMinValue;
        this.coordinateYMaxValue=coordinateYMaxValue;
        this.coordinateYIntervalValue=coordinateYIntervalValue;
    }

    void checkCoodinate(){
        //如果没有设置，就定义为x轴7天，y轴0-100
        if(days<1){
            long currentStamp=System.currentTimeMillis()/1000;
            setCoodinate(currentStamp-7*86400,currentStamp,0,200,10);
        }
    }



    void countSize(Canvas canvas){
        leftWidth=100;
        bottomHeight=40;
        mainWidth=canvas.getWidth();
        mainHeight=canvas.getHeight();

        //canvas.drawText(""+mainWidth+"x"+mainHeight,mainWidth/2,mainHeight/2,paint);
        coordinateStartX=leftWidth;
        coordinateStartY=mainHeight-bottomHeight;
        coordinateWidth=mainWidth-leftWidth;
        coordinateHeight=mainHeight-bottomHeight;

        markSizeX=days*spliteInDay;
        coordinateIntervalX=coordinateWidth/markSizeX;

        coordinateYD_Value=coordinateYMaxValue-coordinateYMinValue;
        markSizeY=coordinateYD_Value/coordinateYIntervalValue;
        coordinateIntervalY=coordinateHeight/markSizeY;

        markLineBaseLength=10;
    }

    void drawCoordinate(Canvas canvas){
        paint.setStrokeWidth(4);
        paint.setColor(0xFF606060);
        //横坐标
        canvas.drawLine(coordinateStartX,coordinateStartY,mainWidth,coordinateStartY,paint);
        //纵坐标
        canvas.drawLine(coordinateStartX,coordinateStartY,coordinateStartX,0,paint);

        //横坐标点及标记
        markSizeX=days*spliteInDay;
        paint.setStrokeWidth(2);
        for(int i=0;i<markSizeX;i++){
            float startX=coordinateStartX+i*coordinateIntervalX;
            float startY=coordinateStartY;
            canvas.drawLine(startX,startY,startX,startY-markLineBaseLength,paint);
            if(i%spliteInDay==0){
                paint.setColor(0x40606060);
                canvas.drawLine(startX,startY,startX,0,paint);
                paint.setColor(0xFF606060);
                canvas.drawText(
                        CzLibrary.getTimeStringFromTimeStamp(""+(startStamp+86400*i/spliteInDay)).substring(5,10),
                        startX,startY+35,paint);
            }
            //canvas.drawText(""+i,startX,startY,paint);
        }

        //纵坐标点及标记
        markSizeY=coordinateYMaxValue/coordinateYIntervalValue;
        paint.setStrokeWidth(2);
        paint.setColor(0xFF606060);
        for(int i=1;i<markSizeY;i++){
            float startX=coordinateStartX;
            float startY=coordinateStartY-i*coordinateIntervalY;
            canvas.drawLine(startX,startY,startX+markLineBaseLength,startY,paint);
            if(i%2==0){
                paint.setColor(0x40606060);
                canvas.drawLine(startX,startY,mainWidth,startY,paint);
                paint.setColor(0xFF606060);
                canvas.drawText(
                        ""+i*coordinateYIntervalValue,
                        leftWidth/2,startY+paintCoodinateMarkTextSize/2,paint);
            }
            //canvas.drawText(""+i,startX,startY,paint);
        }
    }

    public void drawLimitLine(Canvas canvas){

        //上下临界线
        int hiPressureLimit=140;
        float hilimitLineY=coordinateStartY-coordinateIntervalY*hiPressureLimit/coordinateYIntervalValue;
        int loPressureLimit=90;
        float lolimitLineY=coordinateStartY-coordinateIntervalY*loPressureLimit/coordinateYIntervalValue;
        paint.setTextSize(paintCoodinateMarkTextSize);
        paint.setColor(colorHiPressure);
        paint.setPathEffect(new DashPathEffect(new float[]{8, 8}, 0));
        paint.setTextAlign(Paint.Align.LEFT);
        canvas.drawLine(coordinateStartX,hilimitLineY,mainWidth,hilimitLineY,paint);
        canvas.drawText("收缩压上限",coordinateStartX+10,hilimitLineY-10,paint);
        paint.setColor(colorLoPressure);
        canvas.drawLine(coordinateStartX,lolimitLineY,mainWidth,lolimitLineY,paint);
        canvas.drawText("舒张压上限",coordinateStartX+10,lolimitLineY-10,paint);
        paint.setTextSize(paintCoodinateMarkTextSize);
        paint.setPathEffect(null);

        //右上角颜色标记的偏移量
        int offsetTop=10;
        int offsetRight=10;
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setColor(colorHeartRate);
        canvas.drawRoundRect(new RectF(mainWidth-150-offsetRight,offsetTop,mainWidth-offsetRight,paintCoodinateMarkTextSize+20+offsetTop), 50, 50, paint);
        paint.setColor(0xFFFFFFFF);
        canvas.drawText("心率",mainWidth-75-offsetRight,paintCoodinateMarkTextSize+5+offsetTop,paint);

        paint.setColor(colorLoPressure);
        canvas.drawRoundRect(new RectF(mainWidth-300-offsetRight,offsetTop,mainWidth-150-offsetRight,paintCoodinateMarkTextSize+20+offsetTop), 50, 50, paint);
        paint.setColor(0xFFFFFFFF);
        canvas.drawText("舒张压",mainWidth-225-offsetRight,paintCoodinateMarkTextSize+5+offsetTop,paint);

        paint.setColor(colorHiPressure);
        canvas.drawRoundRect(new RectF(mainWidth-450-offsetRight,offsetTop,mainWidth-300-offsetRight,paintCoodinateMarkTextSize+20+offsetTop), 50, 50, paint);
        paint.setColor(0xFFFFFFFF);
        canvas.drawText("收缩压",mainWidth-375-offsetRight,paintCoodinateMarkTextSize+5+offsetTop,paint);
    }

    void drawData(Canvas canvas){
        int length=dataArray.length();
        Path pathHiPressure=new Path();
        Path pathLoPressure=new Path();
        Path pathHeartRate=new Path();
        float x=leftWidth;
        float textOffsetY=-10;
        float yHiPressure=0,yLoPressure=0,yHeartRate=0;
        int stamp=0,hi_pressure=0,lo_pressure=0,heart_rate=0;
        for(int i=0;i<length;i++){
            try{
                JSONObject item=dataArray.getJSONObject(i);
                stamp=item.getInt("create_time");
                hi_pressure=item.getInt("hi_pressure");
                lo_pressure=item.getInt("lo_pressure");
                heart_rate=item.getInt("heart_rate");

                x= coordinateStartX+(coordinateIntervalX*(float)(stamp-startStamp)/(86400/spliteInDay));
                yHiPressure= coordinateStartY-coordinateHeight* (hi_pressure-coordinateYMinValue)/(float)coordinateYD_Value;
                yLoPressure= coordinateStartY-coordinateHeight *(lo_pressure-coordinateYMinValue)/(float)coordinateYD_Value;
                yHeartRate= coordinateStartY-coordinateHeight* (heart_rate-coordinateYMinValue)/(float)coordinateYD_Value;



                if(i==0){
                    paint.setStyle(Paint.Style.FILL);
                    pathHiPressure.moveTo(x,yHiPressure);
                    paint.setColor(colorHiPressure);
                    canvas.drawText(""+hi_pressure,x,yHiPressure+textOffsetY,paint);
                    paint.setStyle(Paint.Style.STROKE);
                    canvas.drawCircle(x,yHiPressure,5,paint);

                    paint.setStyle(Paint.Style.FILL);
                    pathLoPressure.moveTo(x,yLoPressure);
                    paint.setColor(colorLoPressure);
                    canvas.drawText(""+lo_pressure,x,yLoPressure+textOffsetY,paint);
                    paint.setStyle(Paint.Style.STROKE);
                    canvas.drawCircle(x,yLoPressure,5,paint);

                    paint.setStyle(Paint.Style.FILL);
                    pathHeartRate.moveTo(x,yHeartRate);
                    paint.setColor(colorHeartRate);
                    canvas.drawText(""+heart_rate,x,yHeartRate+textOffsetY,paint);
                    paint.setStyle(Paint.Style.STROKE);
                    canvas.drawCircle(x,yHeartRate,5,paint);
                }else{
                    if(hi_pressure>0) {
                        paint.setStyle(Paint.Style.FILL);
                        pathHiPressure.lineTo(x,  yHiPressure);
                        paint.setColor(colorHiPressure);
                        canvas.drawText(""+hi_pressure,x,yHiPressure+textOffsetY,paint);
                        paint.setStyle(Paint.Style.STROKE);
                        canvas.drawCircle(x,yHiPressure,5,paint);
                    }
                    if(lo_pressure>0) {
                        paint.setStyle(Paint.Style.FILL);
                        pathLoPressure.lineTo(x, yLoPressure);
                        paint.setColor(colorLoPressure);
                        canvas.drawText(""+lo_pressure,x,yLoPressure+textOffsetY,paint);
                        paint.setStyle(Paint.Style.STROKE);
                        canvas.drawCircle(x,yLoPressure,5,paint);
                    }
                    if(heart_rate>0) {
                        paint.setStyle(Paint.Style.FILL);
                        pathHeartRate.lineTo(x,yHeartRate);
                        paint.setColor(colorHeartRate);
                        canvas.drawText(""+heart_rate,x,yHeartRate+textOffsetY,paint);
                        paint.setStyle(Paint.Style.STROKE);
                        canvas.drawCircle(x,yHeartRate,5,paint);
                    }
                }

                paint.setStyle(Paint.Style.FILL);
                paint.setColor(colorTime);
                if(hi_pressure>0||lo_pressure>0||heart_rate>0) {
                    canvas.drawText(CzLibrary.getTimeStringFromTimeStamp("" + stamp).substring(11, 16)
                            , x, coordinateStartY-paintCoodinateMarkTextSize, paint);
                }

            }catch (Exception e){
                e.printStackTrace();
                Log.e(TAG,"drawData / Exp at "+i+":"+e);
            }

        }

        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(colorHiPressure);
        canvas.drawPath(pathHiPressure,paint);
        paint.setColor(colorLoPressure);
        canvas.drawPath(pathLoPressure,paint);
        paint.setColor(colorHeartRate);
        canvas.drawPath(pathHeartRate,paint);
    }

    public void showBloodPressure(boolean isDraw, JSONArray dataArray){
        isDrawBloodPressure=isDraw;
        if(dataArray!=null) {
            jArrBloodPressure = dataArray;
        }
        invalidate();
    }
    public void showHeartRate(boolean isDraw, JSONArray dataArray){
        isDrawHeartRate=isDraw;
        if(dataArray!=null) {
            jArrHeartRate = dataArray;
        }
        invalidate();
    }
    public void showWeather(boolean isDraw, JSONArray dataArray){
        isDrawWeather=isDraw;
        if(dataArray!=null) {
            jArrWeather = dataArray;
        }
        invalidate();
    }
    public void showSport(boolean isDraw, JSONArray dataArray){
        isDrawSport=isDraw;
        if(dataArray!=null) {
            jArrSport = dataArray;
        }
        invalidate();
    }
    public void showEat(boolean isDraw, JSONArray dataArray){
        isDrawEat=isDraw;
        if(dataArray!=null) {
            jArrEat = dataArray;
        }
        invalidate();
    }
    public void showFeeling(boolean isDraw, JSONArray dataArray){
        isDrawFeeling=isDraw;
        if(dataArray!=null) {
            jArrFeeling = dataArray;
        }
        invalidate();
    }
    public void showEmotion(boolean isDraw, JSONArray dataArray){
        isDrawEmotion=isDraw;
        if(dataArray!=null) {
            jArrEmotion = dataArray;
        }
        invalidate();
    }
    public void showMedicin(boolean isDraw, JSONArray dataArray){
        isDrawMedicine=isDraw;
        if(dataArray!=null) {
            jArrMedicine = dataArray;
        }
        invalidate();
    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        checkCoodinate();//如果为传入坐标系设置，则自动设置
        countSize(canvas);

        initPaint();

        drawCoordinate(canvas);
        drawLimitLine(canvas);
        drawData(canvas);


    }



}
