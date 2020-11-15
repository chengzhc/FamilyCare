package com.czstudio.czlibrary;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

public class CzMoveableView extends FrameLayout implements View.OnTouchListener {
    String TAG="CzMoveableView";
    boolean isMove=false;
    float offsetX=0,offsetY=0;
    int pxWidth,pxHeight;
    Rect limitRect;
    int statusBarHeight;

    public CzMoveableView(Context context){
        super(context);
        statusBarHeight=UtilsDisplay.getStatusBarHeight(context);
        limitRect=new Rect();
        setOnTouchListener(this);
        Log.e(TAG,"CzMoveableView init....");
    }

    public CzMoveableView(Context context, AttributeSet attrs,
                           int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        statusBarHeight=UtilsDisplay.getStatusBarHeight(context);
        limitRect=new Rect();
        setOnTouchListener(this);
    }

    public CzMoveableView(Context context, AttributeSet attrs) {
        super(context, attrs);
        statusBarHeight=UtilsDisplay.getStatusBarHeight(context);
        limitRect=new Rect();
        setOnTouchListener(this);
    }

    public void setOffsetAndFrame(int left,int top,int right,int bottom,int offsetX,int offsetY){
        limitRect.left=left;
        limitRect.top=top;
        limitRect.right=right;
        limitRect.bottom=bottom;
        this.offsetX=offsetX;
        this.offsetY=offsetY;
    }

//    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        pxWidth=(int) offsetX+w;
        pxHeight=(int) offsetY+h;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                isMove=true;
                break;
            case MotionEvent.ACTION_MOVE:
                isMove=true;
                float x=event.getRawX()-pxWidth/2;
                float y=event.getRawY()-pxHeight/2-statusBarHeight;
                if(x<limitRect.left){
                    x=limitRect.left;
                }
                if(x>(limitRect.right-pxWidth)){
                    x=limitRect.right-pxWidth;
                }
                if(y<limitRect.top){
                    y=limitRect.top;
                }
                if(y>limitRect.bottom-pxHeight){
                    y=limitRect.bottom-pxHeight;
                }
                v.setX(x);
                v.setY(y);
                break;
            case MotionEvent.ACTION_UP:
                isMove=false;
                break;
        }
        Log.e(TAG,"onTouch:"+event.getAction()+",x="+event.getRawX()+" / y="+event.getRawY());
        return true;
    }
}
