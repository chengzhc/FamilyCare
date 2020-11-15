package com.czstudio.czlibrary;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.KeyguardManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Environment;
import android.os.PowerManager;
import android.provider.MediaStore;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.HanyuPinyinVCharType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Random;

/**
 * Created by cheng on 2017/12/23.
 */

public class CzLibrary {
    static String TAG="CzLibrary";

    public static int getRandomBetween(int min,int max){
        Random r = new Random();
        int number = r.nextInt(max-min)+min;
        return number;
    }

    /**
     * 获取app的名称
     * @param context
     * @return
     */
    public static String getAppName(Context context) {
        String appName = "";
        try {
            PackageManager packageManager = context.getPackageManager();
            PackageInfo packageInfo = packageManager.getPackageInfo(
                    context.getPackageName(), 0);
            int labelRes = packageInfo.applicationInfo.labelRes;
            appName =  context.getResources().getString(labelRes);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return appName;
    }

    public static void alert(Context context ,String info){
        if(context==null){
            Log.e("CzLibrary/Alert","context 为空") ;
            return;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        try {
            builder.setTitle("提示")
                    .setMessage(info)
                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog,
                                            int which) {
                        }
                    })
                    .show();
        }catch (Exception e){
            Log.e(TAG,"alert Exp:"+e);
        }
    }

    public static void alert(Context context , String title, String info){
        if(context==null){
            Log.e("CzLibrary/Alert title","context 为空") ;
            return;
        }
        try{
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title)
                .setMessage(info)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog,
                                        int which) {
                    }
                })
                .show();
    }catch (Exception e){
        Log.e(TAG,"alert Exp:"+e);
    }
    }

    public static void alertConfirm(Context context , String title, String info,final AlertCallBack callBack){
        alertConfirm(context , title, info,"确定","取消", callBack);
    }

    public static void alertConfirm(Context context , String title, String info,String confirmStr,String cancelStr,final AlertCallBack callBack){
        if(context==null){
            Log.e("CzLibrary/AlertConfirm","context 为空") ;
            return;
        }
        try {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle(title)
                    .setMessage(info)
                    .setPositiveButton(confirmStr, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog,
                                            int which) {
                            if (callBack != null) {
                                callBack.onAlertConfirm();
                            }
                        }
                    })
                    .setNegativeButton(cancelStr, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog,
                                            int which) {
                            if (callBack != null) {
                                callBack.onAlertCancel();
                            }
                        }
                    })
                    .show();
        }catch (Exception e){
            Log.e(TAG,"alertConfirm Exp:"+e);
        }
    }

    public static void toast(Context context , String info){
        if(context==null){
            Log.e("CzLibrary/toast","context 为空") ;
            return;
        }
        Toast.makeText(context,info,Toast.LENGTH_LONG).show();
    }

    public interface AlertCallBack{
        public void onAlertConfirm();
        public void onAlertCancel();
    }

    /****************************************
     *               时间
     ****************************************/

    public static String getCurrentTime() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date curDate = new Date(System.currentTimeMillis());
        return formatter.format(curDate);
    }

    public static String getTimeStringFromTimeStamp(String timeStamp){
        try {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Long time = new Long(Long.parseLong(timeStamp) * 1000);
            return format.format(time);
        }catch(Exception e){
            return "1970-01-01 00:00:00";
        }
    }

    public static String getTimeStampFromTimeString(String timeString){
        SimpleDateFormat sdr = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
        Date date;
        String times = null;
        try {
            date = sdr.parse(timeString);
            long l = date.getTime()/1000;
            //String stf =
            times = String.valueOf(l);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return times;
    }

    public static String getFileTime() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        Date curDate = new Date(System.currentTimeMillis());
        return formatter.format(curDate);
    }

    /****************************************
     *               数据保存
     ****************************************/
    public static void saveUserData(Activity activity,String key, String value){
        SharedPreferences sharedPref = activity.getSharedPreferences("weather_pref", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(key,value);
        editor.commit();
    }

    public static String loadUserData(Activity activity,String key,String defaultString){
        SharedPreferences sharedPref2 = activity.getSharedPreferences("weather_pref", Context.MODE_PRIVATE);
        String loadedString = sharedPref2.getString(key,defaultString);
        return loadedString;
    }


    /****************************************
     *               WebView
     ****************************************/
    public static void initWebView(Context context,WebView webView){
        if(webView==null){
            toast(context,"WebView 初始化失败，WebView为空");
            return;
        }
        //声明WebSettings子类
        WebSettings webSettings = webView.getSettings();

        //如果访问的页面中要与Javascript交互，则webview必须设置支持Javascript
        webSettings.setJavaScriptEnabled(true);
        // 若加载的 html 里有JS 在执行动画等操作，会造成资源浪费（CPU、电量）
        // 在 onStop 和 onResume 里分别把 setJavaScriptEnabled() 给设置成 false 和 true 即可

        //设置自适应屏幕，两者合用
        webSettings.setUseWideViewPort(true); //将图片调整到适合webview的大小
        webSettings.setLoadWithOverviewMode(true); // 缩放至屏幕的大小

        //缩放操作
        webSettings.setSupportZoom(true); //支持缩放，默认为true。是下面那个的前提。
        webSettings.setBuiltInZoomControls(true); //设置内置的缩放控件。若为false，则该WebView不可缩放
        webSettings.setDisplayZoomControls(false); //隐藏原生的缩放控件

        //其他细节操作
        webSettings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK); //关闭webview中缓存
        webSettings.setAllowFileAccess(true); //设置可以访问文件
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true); //支持通过JS打开新窗口
        webSettings.setLoadsImagesAutomatically(true); //支持自动加载图片
        webSettings.setDefaultTextEncodingName("utf-8");//设置编码格式
    }

    public static String getSearchViewText(SearchView searchView){
        int id = searchView.getContext().getResources().getIdentifier("android:id/search_src_text", null, null);
        return ((EditText)searchView.findViewById(id)).getText().toString();
    }



    /**
     * 获得指定文件的byte数组
     */
    public static byte[] fileToBytes(String filePath) {
        byte[] buffer = null;
        try {
            File file = new File(filePath);
            FileInputStream fis = new FileInputStream(file);
            ByteArrayOutputStream bos = new ByteArrayOutputStream(1000);
            byte[] b = new byte[1000];
            int n;
            while ((n = fis.read(b)) != -1) {
                bos.write(b, 0, n);
            }
            fis.close();
            bos.close();
            buffer = bos.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return buffer;
    }

    /****************************************
     *               图片
     ****************************************/
    /**
     * bitmap 转 byte数组
     */
    public static byte[] bitmapToByte(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, baos);
        byte[] datas = baos.toByteArray();
        return datas;
    }

    public static Bitmap getBitmapFromFile(Context context,String path,int defaultDrawableId){
        try {
            BitmapFactory.Options options2 = new BitmapFactory.Options();
            options2.inPreferredConfig = Bitmap.Config.RGB_565;
            return BitmapFactory.decodeFile(path, options2);
        }catch(Exception e){
            return BitmapFactory.decodeResource(context.getResources(), defaultDrawableId);
        }
    }

    public static Bitmap getBitmapFromUri(Context context,Uri uri,int defaultDrawableId){
        try {
            BitmapFactory.Options options2 = new BitmapFactory.Options();
            options2.inPreferredConfig = Bitmap.Config.RGB_565;
            return BitmapFactory.decodeStream(context.getContentResolver().openInputStream(uri), new Rect(0, 0, 0, 0), options2);
        }catch(Exception e){
            CzLibrary.toast(context,""+e);
            return BitmapFactory.decodeResource(context.getResources(), defaultDrawableId);
        }
    }

    public static Bitmap scaleBitmap(Bitmap bmp,int maxWidth){
        float scale=1;
        if(bmp.getWidth()>maxWidth){
            scale =maxWidth/((float) bmp.getWidth());
        }
        Matrix matrix = new Matrix();
        matrix.setScale(scale, scale);
        return Bitmap.createBitmap(bmp, 0, 0,bmp.getWidth(),
                bmp.getHeight(), matrix, true);
    }

    public static Uri createImageUri(Context context) {
        String name = "takePhoto" + System.currentTimeMillis();
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.Images.Media.TITLE, name);
        contentValues.put(MediaStore.Images.Media.DISPLAY_NAME, name + ".jpeg");
        contentValues.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        Uri uri = context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
        return uri;
    }

    public static Bitmap getRoundBitmap(Context context,Bitmap bitmap){
        RoundedBitmapDrawable roundedDrawable = RoundedBitmapDrawableFactory.create(context.getResources(),bitmap);
        //设置圆角角度
        roundedDrawable.setCornerRadius(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,60,context.getResources().getDisplayMetrics()));
        return drawableToBitmap(roundedDrawable);
    }

    public static Bitmap getCircleBitmap(Context context,Bitmap bitmap){
        RoundedBitmapDrawable circleDrawable = RoundedBitmapDrawableFactory.create(context.getResources(),bitmap);
        //设置为圆形
        circleDrawable.setCircular(true);
        return drawableToBitmap(circleDrawable);
    }

    public static Bitmap drawableToBitmap(Drawable drawable) {
        Bitmap bitmap = Bitmap.createBitmap(
                drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight(),
                drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888:Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(bitmap);
        //canvas.setBitmap(bitmap);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    public static File saveImage(Bitmap bmp,String folderPath) {
        File appDir = new File(Environment.getExternalStorageDirectory(), folderPath);
        if (!appDir.exists()) {
            appDir.mkdir();
        }
        String fileName = System.currentTimeMillis() + ".jpg";
        File file = new File(appDir, fileName);
        try {
            FileOutputStream fos = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
            return file;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /****************************************
     *              相机
     ***************************************/
    public static Uri getUploadUri(Context context, Uri imgUri,int defaultImageResourceId )throws Exception{
       //throw new Exception("null uri");
       Bitmap bmp = CzLibrary.getBitmapFromUri(context,imgUri,defaultImageResourceId);
       bmp = CzLibrary.scaleBitmap(bmp,960);

       File f = CzLibrary.saveImage(bmp,"easyvideo");
       if(f!=null){
           Log.e("CzLibrary",f.getPath()+"/"+f.getName());
           return Uri.fromFile(f);
       }else{
           throw new Exception("null uri");
       }
    }

    /****************************************
     *               系统
     ****************************************/
    public static void closeKeyboard(Activity activity){
        InputMethodManager imm =  (InputMethodManager)activity.getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if(imm != null) {
            imm.hideSoftInputFromWindow(activity.getWindow().getDecorView().getWindowToken(),
                    0);
        }
    }

    public static void showKeyboard(Activity activity,EditText editText){
        InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.showSoftInput(editText, 0);
    }
    public static void wakeUpAndUnlock(Context context){
        KeyguardManager km= (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
        KeyguardManager.KeyguardLock kl = km.newKeyguardLock("unLock");
        //解锁
        kl.disableKeyguard();
        //获取电源管理器对象
        PowerManager pm=(PowerManager) context.getSystemService(Context.POWER_SERVICE);
        //获取PowerManager.WakeLock对象,后面的参数|表示同时传入两个值,最后的是LogCat里用的Tag
        PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.SCREEN_DIM_WAKE_LOCK,"bright");
        //点亮屏幕
        wl.acquire();
        //释放
        wl.release();
    }

    /**
     *
     * @param activity
     * @param notificationId
     * @param title
     * @param content
     * @param smallIconId
     * @param intent
     */
    public static void showLockScreenNotify(Activity activity,int notificationId,String title,String content,int smallIconId,Intent intent){
        wakeUpAndUnlock(activity);

        NotificationManager notificationManager = (NotificationManager) activity.getSystemService(activity.NOTIFICATION_SERVICE);

        NotificationCompat.Builder notifyBuilder =
                new NotificationCompat.Builder( activity ).setContentTitle(title )
                        .setContentText( content )
                        .setSmallIcon( smallIconId)
                        // 点击消失
                        .setAutoCancel( true )
                        // 设置该通知优先级
                        .setPriority( Notification.PRIORITY_MAX )
                        //.setLargeIcon( BitmapFactory.decodeResource( activity.getResources(), R.drawable.btn_add ) )
                        // 通知首次出现在通知栏，带上升动画效果的
                        //.setTicker( mTicker )
                        // 通知产生的时间，会在通知信息里显示
                        .setWhen( System.currentTimeMillis() )
                        // 向通知添加声音、闪灯和振动效果的最简单、最一致的方式是使用当前的用户默认设置，使用defaults属性，可以组合：
                        .setDefaults( Notification.DEFAULT_VIBRATE | Notification.DEFAULT_ALL | Notification.DEFAULT_SOUND );
        PendingIntent resultPendingIntent =
                PendingIntent.getActivity( activity, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT );
        notifyBuilder.setContentIntent( resultPendingIntent );
        notificationManager.notify( notificationId, notifyBuilder.build() );
    }

    public static int getBatteryPercent(Activity activity){
        IntentFilter intentFilter= new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent intentBattery = activity.registerReceiver(null, intentFilter);//注意，粘性广播不需要广播接收器

        return  100 * intentBattery.getIntExtra("level", 100)
                / intentBattery.getIntExtra("scale", 100);
    }


    /***********************************
     * 排序
     ***********************************/
    public static JSONArray sortJsonArrayByDate(JSONArray mJSONArray,String dateName){
        List<JSONObject> list = new ArrayList<JSONObject>();
        JSONObject jsonObj = null;
        for (int i = 0; i < mJSONArray.length(); i++) {
            jsonObj = mJSONArray.optJSONObject(i);
            list.add(jsonObj);
        }
        //排序操作
        JsonComparator pComparator =  new JsonComparator(dateName);
        Collections.sort(list, pComparator);

        //把数据放回去
        mJSONArray = new JSONArray();
        for (int i = 0; i < list.size(); i++) {
            jsonObj = list.get(i);
            mJSONArray.put(jsonObj);
        }

        return mJSONArray;
    }

    /**
     * 转换拼音
     * @param inputString
     * @return
     */
    public static String getPingYin(String inputString) {
        HanyuPinyinOutputFormat format = new HanyuPinyinOutputFormat();
        format.setCaseType(HanyuPinyinCaseType.LOWERCASE);
        format.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
        format.setVCharType(HanyuPinyinVCharType.WITH_V);
        char[] input = inputString.trim().toCharArray();
        String output = "";

        try {
            for (char curchar : input) {
                if (Character.toString(curchar).matches("[\\u4E00-\\u9FA5]+")) {
                    String[] temp = PinyinHelper.toHanyuPinyinStringArray(curchar, format);
                    output += temp[0];
                } else
                    output += Character.toString(curchar);
            }
        } catch (BadHanyuPinyinOutputFormatCombination e) {
            e.printStackTrace();
        }
        return output;
    }

    /***********************************************
     * 内部类
     ***********************************************/

    public static class JsonComparator implements Comparator<JSONObject> {

        String dateName = "";

        JsonComparator(String dateName) {
            this.dateName = dateName;
        }

        @Override
        public int compare(JSONObject json1, JSONObject json2) {
                String date1 = json1.optString(dateName);
                String date2 = json2.optString(dateName);
                if (date1.compareTo(date2) < 0) {
                    return -1;
                } else if (date1.compareTo(date2) > 0) {
                    return 1;
                }
                return 0;
        }
    }

    public static void uplog(Activity activity,String domain,String uid,String type, String position, String info ){
        HashMap<String ,String > data=new HashMap<String,String>();
        data.put("uid",uid);
        data.put("type",type);
        data.put("position",position);
        data.put("info",info);
        CzSys_HTTP.requestPost(activity,
                domain + "/module_data/log/log",
                data,
                new CzSys_HTTP.HttpListener() {
                    @Override
                    public void onHttpSuccess(String data) {

                    }

                    @Override
                    public void onFeedBackSuccess(JSONObject feedBackData) {

                    }
                }
        );
    }

    //MD5 加密
    public static String md5Encode(String content) {
        byte[] hash;
        try {
            hash = MessageDigest.getInstance("MD5").digest(content.getBytes("UTF-8"));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("NoSuchAlgorithmException", e);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("UnsupportedEncodingException", e);
        }
        //对生成的16字节数组进行补零操作
        StringBuilder hex = new StringBuilder(hash.length * 2);
        for (byte b : hash) {
            if ((b & 0xFF) < 0x10) {
                hex.append("0");
            }
            hex.append(Integer.toHexString(b & 0xFF));
        }
        return hex.toString();
    }

    public static String getSecretString(String str) {
        int length=str.length();
        if(length<8){
            return str;
        }

        String star="";
        for(int i=0;i<length-7;i++){
            star+="*";
        }

        return str.substring(0,3)+star+ str.substring(length-4,length);
    }

    // Storage Permissions
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE};

    /**
     * Checks if the app has permission to write to device storage
     * If the app does not has permission then the user will be prompted to
     * grant permissions
     *
     * @param activity
     */
    public static void verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(activity, PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE);
        }
    }

    /*************************
     * 动画
     */
    public static void  bounceContainer(final View view, final float offsetX, final float offsetY, final int duration){
        try {
            TranslateAnimation transAnim = new TranslateAnimation(
                    0, offsetX,
                    0, offsetY);
            transAnim.setDuration(duration);
            transAnim.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    try {
                        TranslateAnimation transAnimBk = new TranslateAnimation(
                                offsetX, 0,
                                offsetY, 0);
                        transAnimBk.setDuration(duration);
                        view.startAnimation(transAnimBk);
                    }catch (Exception e){
                        Log.e(TAG,"bounceContainer innerExp:"+e);
                    }
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            view.startAnimation(transAnim);
        }catch (Exception e){
            Log.e(TAG,"bounceContainer outerExp:"+e);
        }
    }

}
