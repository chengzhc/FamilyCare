package com.czstudio.familycare.custom_view;


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.Log;
import android.view.View;

import com.czstudio.czlibrary.CzLibrary;

import org.json.JSONArray;
import org.json.JSONObject;

public class HistoryFeelView extends View {
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
    String[] coodinateYValueArray={"很差","不良","一般","不错","极好"};

    JSONArray dataArray=new JSONArray();

    int colorEat=0xF0EE3333,colorTired=0xF03300FF,colorSweat=0xF033DD33,colorSleep=0xF0333333,colorTime=0xFF808080;

    public HistoryFeelView(Context context) {
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

    void checkCoodinate(){
        coordinateYMaxValue=5;
        coordinateYMinValue=0;
        coordinateYIntervalValue =1;
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

    void drawCoordinate(Canvas canvas){
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
        for(int i=1;i<markSizeY+1;i++){
            float startX=coordinateStartX;
            float startY=coordinateStartY-i*coordinateIntervalY;
            canvas.drawLine(startX,startY,startX+markLineBaseLength,startY,paint);
                paint.setColor(0x40606060);
                canvas.drawLine(startX,startY,mainWidth,startY,paint);
                paint.setColor(0xFF606060);


                canvas.drawText(
                        coodinateYValueArray[i-1],
                        leftWidth-5,startY+paintCoodinateMarkTextSize/2,paint);
            //
        }
        paint.setTextAlign(Paint.Align.CENTER);
    }

    void drawSign(){
        //右上角颜色标记的偏移量
        int offsetTop=10;
        int offsetRight=10;
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setColor(colorEat);
        canvas.drawRoundRect(new RectF(mainWidth-150-offsetRight,offsetTop,mainWidth-offsetRight,paintCoodinateMarkTextSize+20+offsetTop), 50, 50, paint);
        paint.setColor(0xFFFFFFFF);
        canvas.drawText("胃口",mainWidth-75-offsetRight,paintCoodinateMarkTextSize+5+offsetTop,paint);

        paint.setColor(colorTired);
        canvas.drawRoundRect(new RectF(mainWidth-300-offsetRight,offsetTop,mainWidth-150-offsetRight,paintCoodinateMarkTextSize+20+offsetTop), 50, 50, paint);
        paint.setColor(0xFFFFFFFF);
        canvas.drawText("体力",mainWidth-225-offsetRight,paintCoodinateMarkTextSize+5+offsetTop,paint);

        paint.setColor(colorSweat);
        canvas.drawRoundRect(new RectF(mainWidth-450-offsetRight,offsetTop,mainWidth-300-offsetRight,paintCoodinateMarkTextSize+20+offsetTop), 50, 50, paint);
        paint.setColor(0xFFFFFFFF);
        canvas.drawText("虚汗",mainWidth-375-offsetRight,paintCoodinateMarkTextSize+5+offsetTop,paint);

        paint.setColor(colorSleep);
        canvas.drawRoundRect(new RectF(mainWidth-600-offsetRight,offsetTop,mainWidth-450-offsetRight,paintCoodinateMarkTextSize+20+offsetTop), 50, 50, paint);
        paint.setColor(0xFFFFFFFF);
        canvas.drawText("睡眠",mainWidth-525-offsetRight,paintCoodinateMarkTextSize+5+offsetTop,paint);
    }

    void drawData(Canvas canvas){
        int length=dataArray.length();
        Path pathEat=new Path();
        Path pathTired=new Path();
        Path pathSweat=new Path();
        Path pathSleep=new Path();
        float x=leftWidth;
        float textOffsetY=-10;
        float yEat=0,yTired=0,ySweat=0,ySleep=0;
        int stamp=0,eat=0,tired=0,sweat=0,sleep=0;
        for(int i=0;i<length;i++){
            try{
                JSONObject item=dataArray.getJSONObject(i);
                stamp=item.getInt("create_time");
                eat=item.getInt("eat");
                tired=item.getInt("tired");
                sweat=item.getInt("sweat");
                sleep=item.getInt("sleep");

                x= coordinateStartX+(coordinateIntervalX*(float)(stamp-startStamp)/(86400/spliteInDay));
                yEat= coordinateStartY-10-coordinateHeight* (eat-coordinateYMinValue)/coordinateYD_Value;
                yTired= coordinateStartY-coordinateHeight *(tired-coordinateYMinValue)/coordinateYD_Value;
                ySweat= coordinateStartY+10-coordinateHeight* (sweat-coordinateYMinValue)/coordinateYD_Value;
                ySleep= coordinateStartY+20-coordinateHeight* (sleep-coordinateYMinValue)/coordinateYD_Value;



                if(i==0){
                    paint.setStyle(Paint.Style.FILL);
                    pathEat.moveTo(x,yEat);
                    paint.setColor(colorEat);
                    canvas.drawText(""+eat,x,yEat+textOffsetY,paint);
                    paint.setStyle(Paint.Style.STROKE);
                    canvas.drawCircle(x,yEat,5,paint);

                    paint.setStyle(Paint.Style.FILL);
                    pathTired.moveTo(x,yTired);
                    paint.setColor(colorTired);
                    canvas.drawText(""+tired,x,yTired+textOffsetY,paint);
                    paint.setStyle(Paint.Style.STROKE);
                    canvas.drawCircle(x,yTired,5,paint);

                    paint.setStyle(Paint.Style.FILL);
                    pathSweat.moveTo(x,ySweat);
                    paint.setColor(colorSweat);
                    canvas.drawText(""+sweat,x,ySweat+textOffsetY,paint);
                    paint.setStyle(Paint.Style.STROKE);
                    canvas.drawCircle(x,ySweat,5,paint);

                    paint.setStyle(Paint.Style.FILL);
                    pathSleep.moveTo(x,ySleep);
                    paint.setColor(colorSleep);
                    canvas.drawText(""+sweat,x,ySweat+textOffsetY,paint);
                    paint.setStyle(Paint.Style.STROKE);
                    canvas.drawCircle(x,ySleep,5,paint);
                }else{
                    if(eat>0) {
                        paint.setStyle(Paint.Style.FILL);
                        pathEat.lineTo(x,  yEat);
                        paint.setColor(colorEat);
                        canvas.drawText(""+eat,x,yEat+textOffsetY,paint);
                        paint.setStyle(Paint.Style.STROKE);
                        canvas.drawCircle(x,yEat,5,paint);
                    }
                    if(tired>0) {
                        paint.setStyle(Paint.Style.FILL);
                        pathTired.lineTo(x, yTired);
                        paint.setColor(colorTired);
                        canvas.drawText(""+tired,x,yTired+textOffsetY,paint);
                        paint.setStyle(Paint.Style.STROKE);
                        canvas.drawCircle(x,yTired,5,paint);
                    }
                    if(sweat>0) {
                        paint.setStyle(Paint.Style.FILL);
                        pathSweat.lineTo(x,ySweat);
                        paint.setColor(colorSweat);
                        canvas.drawText(""+sweat,x,ySweat+textOffsetY,paint);
                        paint.setStyle(Paint.Style.STROKE);
                        canvas.drawCircle(x,ySweat,5,paint);
                    }
                    if(sleep>0) {
                        paint.setStyle(Paint.Style.FILL);
                        pathSleep.lineTo(x,ySleep);
                        paint.setColor(colorSleep);
                        canvas.drawText(""+sleep,x,ySleep+textOffsetY,paint);
                        paint.setStyle(Paint.Style.STROKE);
                        canvas.drawCircle(x,ySleep,5,paint);
                    }
                }

                paint.setStyle(Paint.Style.FILL);
                paint.setColor(colorTime);
                if(eat>0||tired>0||sweat>0||sleep>0) {
                    canvas.drawText(CzLibrary.getTimeStringFromTimeStamp("" + stamp).substring(11, 16)
                            , x, coordinateStartY-paintCoodinateMarkTextSize, paint);
                }

            }catch (Exception e){
                e.printStackTrace();
                Log.e(TAG,"drawData / Exp at "+i+":"+e);
            }

        }

        paint.setStyle(Paint.Style.STROKE);
//        paint.setStrokeWidth(8);
        paint.setColor(colorEat);
        canvas.drawPath(pathEat,paint);
        paint.setColor(colorTired);
        canvas.drawPath(pathTired,paint);
        paint.setColor(colorSweat);
        canvas.drawPath(pathSweat,paint);
        paint.setColor(colorSleep);
        canvas.drawPath(pathSleep,paint);
    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        this.canvas=canvas;
        if(dataArray.length()<1){
            return;
        }
        checkCoodinate();//如果为传入坐标系设置，则自动设置
        countSize(canvas);
        initPaint();
        drawCoordinate(canvas);
        drawSign();
        Log.e(TAG,"drawCoordinate Done");
        drawData(canvas);

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
