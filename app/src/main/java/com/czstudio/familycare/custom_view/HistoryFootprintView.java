package com.czstudio.familycare.custom_view;


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.Log;
import android.view.View;

import com.amap.api.maps2d.AMapUtils;
import com.amap.api.maps2d.model.LatLng;
import com.czstudio.czlibrary.CzLibrary;

import org.json.JSONArray;
import org.json.JSONObject;

public class HistoryFootprintView extends View {
    public static final int SHOW_TYPE_DISTANCE=1,SHOW_TYPE_STEPS=2;
    int showType=SHOW_TYPE_DISTANCE;
    String TAG="HistoryFootprintView";
    Paint paint;
    Canvas canvas;


    long startStamp,stopStamp;//时间段，在setDuration()内会做24小时取整处理
    int days;//计算出多少天
    int spliteInDay=1;//一天分几段

    float mainWidth,mainHeight;//主界面尺寸
    float leftWidth,bottomHeight;
    float coordinateStartX,coordinateStartY,coordinateWidth,coordinateHeight,//坐标轴尺寸
            coordinateIntervalX,coordinateIntervalY,//坐标轴刻度间隔尺寸
            coordinateYMinValue,coordinateYMaxValue,coordinateYD_Value,coordinateYIntervalValue,//Y坐标轴最大值，最小值，刻度长度，最小刻度
            markSizeX,markSizeY,//坐标刻度总数
            markLineBaseLength,//刻度线基本长度
            topTextHeight,//柱状图顶部文字高度
            testEnd;
    int paintCoodinateMarkTextSize=30;

    JSONArray dataArray=new JSONArray();

    float[] distanceArray;
    int[] stepsArray;

    public HistoryFootprintView(Context context) {
        super(context);
        paint = new Paint();
        initPaint();
    }

    void initPaint() {
        paint.setAntiAlias(true);
        paint.setStrokeWidth(10);
        paint.setARGB(200, 0, 0x99, 0xcc);
        paint.setTextSize(paintCoodinateMarkTextSize);
        paint.setStyle(Paint.Style.FILL);
        paint.setTextAlign(Paint.Align.CENTER);
    }

    public void showData(JSONArray dataArray){
        this.dataArray=dataArray;
        invalidate();
    }

    public void setTimeRange(long startStamp,long stopStamp){
        this.startStamp=86400*(startStamp/86400)-28800;
        this.stopStamp=86400*(stopStamp/86400)-28800;
        if(stopStamp%86400>0){
            this.stopStamp+=86400;
        }
        days=(int) (this.stopStamp-this.startStamp)/86400;
        Log.e(TAG,"days="+days);
    }

    void checkDistanceCoodinate(){
        float max=0;
        for(int i=0;i<distanceArray.length;i++){
            if(max<distanceArray[i]){
                max=distanceArray[i];
            }
        }
        coordinateYMaxValue=(1+(int)max/1000)*1000;
        coordinateYMinValue=0;

        int rawInteravl=(int)coordinateYMaxValue/10;
        if(rawInteravl<1000){
            rawInteravl=1000;
        }
        coordinateYIntervalValue = (rawInteravl/1000)*1000;
    }

    void checkStepsCoodinate(){
        float max=0;
        for(int i=0;i<stepsArray.length;i++){
            if(max<stepsArray[i]){
                max=stepsArray[i];
            }
        }
        coordinateYMaxValue=(1+(int)max/1000)*1000;
        coordinateYMinValue=0;

        int rawInteravl=(int)coordinateYMaxValue/10;
        if(rawInteravl<1000){
            rawInteravl=1000;
        }
        coordinateYIntervalValue = (rawInteravl/1000)*1000;
    }



    void countSize(Canvas canvas){
        leftWidth=100;
        bottomHeight=40;
        mainWidth=canvas.getWidth();
        mainHeight=canvas.getHeight();
        topTextHeight=paintCoodinateMarkTextSize+10;

        //canvas.drawText(""+mainWidth+"x"+mainHeight,mainWidth/2,mainHeight/2,paint);
        coordinateStartX=leftWidth;
        coordinateStartY=mainHeight-bottomHeight;
        coordinateWidth=mainWidth-leftWidth;
        coordinateHeight=mainHeight-bottomHeight-topTextHeight;

        markSizeX=days*spliteInDay;
        coordinateIntervalX=coordinateWidth/markSizeX;

        coordinateYD_Value=coordinateYMaxValue-coordinateYMinValue;
        markSizeY=coordinateYD_Value/coordinateYIntervalValue;
        coordinateIntervalY=coordinateHeight/markSizeY;

        markLineBaseLength=10;
    }

    void drawDistanceCoordinate(Canvas canvas){
        paint.setStrokeWidth(4);
        paint.setColor(0xFF606060);
        //横坐标
        canvas.drawLine(coordinateStartX,coordinateStartY,mainWidth,coordinateStartY,paint);
        //纵坐标
        canvas.drawLine(coordinateStartX,coordinateStartY,coordinateStartX,0,paint);

        //横坐标点及标记
        markSizeX=days*spliteInDay;
        Log.e(TAG,"drawCoordinate markSizeX="+markSizeX);
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
        Log.e(TAG,"drawCoordinate markSizeY="+markSizeY);
        paint.setStrokeWidth(2);
        paint.setColor(0xFF606060);
        paint.setTextAlign(Paint.Align.RIGHT);
        for(int i=1;i<markSizeY;i++){
            float startX=coordinateStartX;
            float startY=coordinateStartY-i*coordinateIntervalY;
            canvas.drawLine(startX,startY,startX+markLineBaseLength,startY,paint);

            if(i%2==0){
                paint.setColor(0x40606060);
                canvas.drawLine(startX,startY,mainWidth,startY,paint);
                paint.setColor(0xFF606060);
                canvas.drawText(
                        ""+(int)(i*coordinateYIntervalValue)/1000,
                        leftWidth-5,startY+paintCoodinateMarkTextSize/2,paint);
            }
            //
        }
        paint.setTextAlign(Paint.Align.CENTER);
    }

    void drawStepsCoordinate(Canvas canvas){
        paint.setStrokeWidth(4);
        paint.setColor(0xFF606060);
        //横坐标
        canvas.drawLine(coordinateStartX,coordinateStartY,mainWidth,coordinateStartY,paint);
        //纵坐标
        canvas.drawLine(coordinateStartX,coordinateStartY,coordinateStartX,0,paint);

        //横坐标点及标记
        markSizeX=days*spliteInDay;
        Log.e(TAG,"drawCoordinate markSizeX="+markSizeX);
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
        Log.e(TAG,"drawCoordinate markSizeY="+markSizeY);
        paint.setStrokeWidth(2);
        paint.setColor(0xFF606060);
        paint.setTextAlign(Paint.Align.RIGHT);
        for(int i=1;i<markSizeY;i++){
            float startX=coordinateStartX;
            float startY=coordinateStartY-i*coordinateIntervalY;
            canvas.drawLine(startX,startY,startX+markLineBaseLength,startY,paint);

            if(i%2==0){
                paint.setColor(0x40606060);
                canvas.drawLine(startX,startY,mainWidth,startY,paint);
                paint.setColor(0xFF606060);
                canvas.drawText(
                        ""+(int)(i*coordinateYIntervalValue)/1000,
                        leftWidth-5,startY+paintCoodinateMarkTextSize/2,paint);
            }
            //
        }
        paint.setTextAlign(Paint.Align.CENTER);
    }

    void ArrangeDistanceData(Canvas canvas){
        distanceArray=new float[days];
        int length=dataArray.length();
        double lastLati=0;
        double lastLongi=0;
        for(int i=0;i<length;i++){
            try{
                JSONObject jobj=dataArray.getJSONObject(i);
                int stamp=jobj.getInt("t");
                int index=(int)((stamp-startStamp)/86400);
                double lati=jobj.getInt("lt")/10000000.0;
                double longi=jobj.getInt("lg")/10000000.0;
                String net_type=jobj.getString("nt");
                if((lati>0)&&(longi>0)&&(net_type.equals("MOBILE"))){
                    if((lastLati>0)||(lastLongi>0)){
                        float dist= AMapUtils.calculateLineDistance(new LatLng(lastLati,lastLongi),new LatLng(lati,longi));
                        distanceArray[index]+=dist;
                    }
                    lastLati=lati;
                    lastLongi=longi;
                }

            }catch (Exception e){
                Log.e(TAG,"ArrangeData Exp at "+i+":"+e);
            }
        }

    }
    void ArrangeStepsData(Canvas canvas){
        stepsArray=new int[days];
        int length=dataArray.length();

        for(int i=0;i<length;i++){
            try{
                JSONObject jobj=dataArray.getJSONObject(i);
                int stamp=jobj.getInt("t");
                int index=(int)((stamp-startStamp)/86400);
                stepsArray[index]+=jobj.getInt("stp");

            }catch (Exception e){
                Log.e(TAG,"ArrangeData Exp at "+i+":"+e);
            }
        }

    }

    void drawDistanceData(Canvas canvas){
        paint.setTextAlign(Paint.Align.CENTER);
        int length=distanceArray.length;
        float rectWidth=coordinateIntervalX/2;
        for(int i=0;i<length;i++){
            float x=leftWidth+i*coordinateIntervalX*spliteInDay;
            float y=coordinateHeight+topTextHeight- coordinateHeight *distanceArray[i]/coordinateYMaxValue;
            int distStrInt=(int)(distanceArray[i]/100);
            String distStr=distStrInt/10+"."+distStrInt%10+"km";
            int color=0xFFCC99CC+(int)( 0x10000* y/coordinateHeight);
            paint.setColor(color);
            canvas.drawRect(new RectF(x,y,x+rectWidth,coordinateHeight+topTextHeight),paint);

            canvas.drawText(distStr,x+rectWidth/2,y-5,paint);
        }
    }
    void drawStepsData(Canvas canvas){
        paint.setTextAlign(Paint.Align.CENTER);
        int length=stepsArray.length;
        float rectWidth=coordinateIntervalX/2;
        for(int i=0;i<length;i++){
            float x=leftWidth+i*coordinateIntervalX*spliteInDay;
            float y=coordinateHeight+topTextHeight- coordinateHeight *stepsArray[i]/coordinateYMaxValue;

            int color=0xFFCC99CC+(int)( 0x10000* y/coordinateHeight);
            paint.setColor(color);
            canvas.drawRect(new RectF(x,y,x+rectWidth,coordinateHeight+topTextHeight),paint);

            canvas.drawText(""+stepsArray[i],x+rectWidth/2,y-5,paint);
        }
    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        this.canvas=canvas;
        if(dataArray.length()<1){
            return;
        }
        if(showType==SHOW_TYPE_STEPS) {
            ArrangeStepsData(canvas);
            checkStepsCoodinate();//如果为传入坐标系设置，则自动设置
            countSize(canvas);
            initPaint();
            drawStepsCoordinate(canvas);
            Log.e(TAG,"drawCoordinate Done");
            drawStepsData(canvas);
        }else if(showType==SHOW_TYPE_DISTANCE){
            ArrangeDistanceData(canvas);
            checkDistanceCoodinate();//如果为传入坐标系设置，则自动设置
            countSize(canvas);
            initPaint();
            drawDistanceCoordinate(canvas);
            Log.e(TAG,"drawCoordinate Done");
            drawDistanceData(canvas);
        }else{
            Log.e(TAG,"显示类型未知");
            return;
        }

    }

    public void scaleCanvas(float scale){
        this.canvas.scale(scale, scale);
    }

    public int getShowType() {
        return showType;
    }

    public void setShowType(int showType) {
        this.showType = showType;
    }
}
