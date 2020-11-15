package com.czstudio.czlibrary;

import android.app.Activity;
import android.os.Environment;
import android.util.Base64;
import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


/*
图片转BASE64上传 PHP接口
public function base64_upload_image(){
		$base64_img = trim($_POST['image']);
		$up_dir = 'uploads/picture/base64/';

		if(!file_exists($up_dir)){
			mkdir($up_dir,0777);
		}

		$filePath = $up_dir.'img_'.date('YmdHis_').'.jpg';
		if(!empty($_POST["fileName"])){
			$filePath = $up_dir.$_POST["fileName"];
		}

		if(file_put_contents($filePath, base64_decode( $base64_img))){
			$this->feedBackData("1",$filePath,json_encode($_POST),"");
		}else{
			$this->feedBackData("0","","upload fail","");

		}
	}
java调用时参数代码
ByteArrayOutputStream baos = new ByteArrayOutputStream();
bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
byte[] imgByte = baos.toByteArray();
new HashMap<String, String>() {
    {
        put("image",Base64.encodeToString(imgByte,Base64.DEFAULT));
    }
},
 */


public class CzSys_HTTP {
    static final String TAG="CzSys_HTTP";

    /**
     * HTTP GET 请求
     *
     * @param address
     * @param paramsMap
     * @param httpListener
     */
    public static void requestGet(final Activity activity, final String address, final HashMap<String, String> paramsMap, final HttpListener httpListener) {
        new Thread() {
            public void run() {
                try {
                    //String address = "https://xxx.com/getUsers?";
                    StringBuilder tempParams = new StringBuilder();
                    int pos = 0;
                    for (String key : paramsMap.keySet()) {
                        if (pos > 0) {
                            tempParams.append("&");
                        }
                        tempParams.append(String.format("%s=%s", key, URLEncoder.encode(paramsMap.get(key), "utf-8")));
                        pos++;
                    }
                    String requestUrl = address + "?" + tempParams.toString();
                    // 新建一个URL对象
                    URL url = new URL(requestUrl);
                    // 打开一个HttpURLConnection连接
                    final HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();
                    // 设置连接主机超时时间
                    urlConn.setConnectTimeout(5 * 1000);
                    //设置从主机读取数据超时
                    urlConn.setReadTimeout(10 * 1000);
                    // 设置是否使用缓存  默认是true
                    urlConn.setUseCaches(true);
                    // 设置为Post请求
                    urlConn.setRequestMethod("GET");
                    //urlConn设置请求头信息
                    //设置请求中的媒体类型信息。
                    urlConn.setRequestProperty("Content-Type", "application/json");
                    //设置客户端与服务连接类型
                    urlConn.addRequestProperty("Connection", "Keep-Alive");
                    // 开始连接
                    urlConn.connect();
                    // 向Listener返回请求成败
                    final int responseCode = urlConn.getResponseCode();
                    if (200 == responseCode) {
                        // 获取返回的数据
                        final String result = streamToString(urlConn.getInputStream());
                        if (result != null) {
                            if (httpListener != null) {
                                activity.runOnUiThread(
                                        new Runnable() {
                                            @Override
                                            public void run() {
                                                httpListener.onHttpSuccess(result);
                                            }
                                        }
                                );
                            }
                            Log.e("requestGet", "GET Request SUCCESS，result--->" + result);
                        } else {
                            if (httpListener != null) {
                                activity.runOnUiThread(
                                        new Runnable() {
                                            @Override
                                            public void run() {
                                                CzLibrary.alert(activity,"服务器未返回，请稍后再试");
                                                Log.e("CzSys_Http","Fail!getResponse:Null");
                                            }
                                        }
                                );
                            }
                            Log.e("requestGet", "GET Request get Null Data");
                        }

                    } else {
                        if (httpListener != null) {
                            activity.runOnUiThread(
                                    new Runnable() {
                                        @Override
                                        public void run() {
                                            CzLibrary.alert(activity,"服务器响应错误，请稍后再试");
                                            Log.e("CzSys_Http","Fail!getResponseCode:" + responseCode);
                                        }
                                    }
                            );
                        }
                        Log.e("requestGet", "GET Request FAIL");
                    }
                    // 关闭连接
                    urlConn.disconnect();
                } catch (final Exception e) {
                    if (httpListener != null) {
                        activity.runOnUiThread(
                                new Runnable() {
                                    @Override
                                    public void run() {
                                        CzLibrary.alert(activity,"服务器连接异常，请稍后再试");
                                        Log.e("CzSys_Http","Fail!Connect Exception:" + e);
                                    }
                                }
                        );
                    }
                    Log.e("requestGet", e.toString());
                }
            }
        }.start();
    }

    /**
     * HTTP POST 请求
     *
     * @param activity 用于获取主线程载体
     * @param address 接口地址
     * @param paramsMap POST 传参
     * @param httpListener 监听
     */
    public static void requestPost(final Activity activity, final String address, final HashMap<String, String> paramsMap, final HttpListener httpListener) {
        new Thread() {
            public void run() {
                try {
                    //合成参数
                    StringBuilder tempParams = new StringBuilder();
                    int pos = 0;
                    for (String key : paramsMap.keySet()) {
                        if (pos > 0) {
                            tempParams.append("&");
                        }
                        tempParams.append(String.format("%s=%s", key, URLEncoder.encode(paramsMap.get(key), "utf-8")));
                        pos++;
                    }
                    String params = tempParams.toString();
                    // 请求的参数转换为byte数组
                    byte[] postData = params.getBytes();
                    // 新建一个URL对象
                    URL url = new URL(address);
                    // 打开一个HttpURLConnection连接
                    HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();
                    // 设置连接超时时间
                    urlConn.setConnectTimeout(5 * 1000);
                    //设置从主机读取数据超时
                    urlConn.setReadTimeout(10 * 1000);
                    // Post请求必须设置允许输出 默认false
                    urlConn.setDoOutput(true);
                    //设置请求允许输入 默认是true
                    urlConn.setDoInput(true);
                    // Post请求不能使用缓存
                    urlConn.setUseCaches(false);
                    // 设置为Post请求
                    urlConn.setRequestMethod("POST");
                    //设置本次连接是否自动处理重定向
                    urlConn.setInstanceFollowRedirects(true);
                    // 配置请求Content-Type
                    urlConn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                    // 开始连接
                    urlConn.connect();
                    // 发送请求参数
                    DataOutputStream dos = new DataOutputStream(urlConn.getOutputStream());
                    dos.write(postData);
                    dos.flush();
                    dos.close();
                    // 向Listener返回请求成败
                    final int responseCode = urlConn.getResponseCode();
                    if (200 == responseCode) {
                        // 获取返回的数据
                        final String result = streamToString(urlConn.getInputStream());
                        if (result != null) {
                            if (httpListener != null) {
                                activity.runOnUiThread(
                                        new Runnable() {
                                            @Override
                                            public void run() {
                                                httpListener.onHttpSuccess(result);
                                            }
                                        }
                                );
                            }
                            Log.e("requestPOST", "POST Request SUCCESS，result--->" + result);
                        } else {
                            if (httpListener != null) {
                                activity.runOnUiThread(
                                        new Runnable() {
                                            @Override
                                            public void run() {
                                                CzLibrary.alert(activity,"服务器未返回，请稍后再试");
                                                Log.e("CzSys_Http","Fail!getResponse:Null");
                                            }
                                        }
                                );
                            }
                            Log.e("requestPOST", "POST Request get Null Data");
                        }

                    } else {
                        if (httpListener != null) {
                            activity.runOnUiThread(
                                    new Runnable() {
                                        @Override
                                        public void run() {
                                            CzLibrary.alert(activity,"服务器响应错误，请稍后再试");
                                            Log.e("CzSys_Http","Fail!getResponseCode:" + responseCode);
                                        }
                                    }
                            );
                        }
                        Log.e("requestPOST", "POST Request FAIL，code"+responseCode);
                    }
                    // 关闭连接
                    urlConn.disconnect();
                } catch (final Exception e) {
                    if (httpListener != null) {
                        activity.runOnUiThread(
                                new Runnable() {
                                    @Override
                                    public void run() {
                                        CzLibrary.alert(activity,"服务器连接异常，请稍后再试");
                                        Log.e("CzSys_Http","Fail!Connect Exception:" + e);
                                    }
                                }
                        );
                    }
                    Log.e("requestPOST", e.toString());
                }
            }
        }.start();
    }

    /**
     * HTTP POST 请求
     *
     * @param activity 用于获取主线程载体
     * @param address 接口地址
     * @param paramsMap POST 传参
     * @param httpListener 监听
     */
    public static void requestPostCz(final Activity activity, final String address, final HashMap<String, String> paramsMap, final HttpListener httpListener) {
        new Thread() {
            public void run() {
                try {
                    //合成参数
                    StringBuilder tempParams = new StringBuilder();
                    int pos = 0;
                    for (String key : paramsMap.keySet()) {
                        if (pos > 0) {
                            tempParams.append("&");
                        }
                        tempParams.append(String.format("%s=%s", key, URLEncoder.encode(paramsMap.get(key), "utf-8")));
                        pos++;
                    }
                    String params = tempParams.toString();
                    // 请求的参数转换为byte数组
                    byte[] postData = params.getBytes();
                    // 新建一个URL对象
                    URL url = new URL(address);
                    // 打开一个HttpURLConnection连接
                    HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();
                    // 设置连接超时时间
                    urlConn.setConnectTimeout(30 * 1000);
                    //设置从主机读取数据超时
                    urlConn.setReadTimeout(30 * 1000);
                    // Post请求必须设置允许输出 默认false
                    urlConn.setDoOutput(true);
                    //设置请求允许输入 默认是true
                    urlConn.setDoInput(true);
                    // Post请求不能使用缓存
                    urlConn.setUseCaches(false);
                    // 设置为Post请求
                    urlConn.setRequestMethod("POST");
                    //设置本次连接是否自动处理重定向
                    urlConn.setInstanceFollowRedirects(true);
                    // 配置请求Content-Type
                    urlConn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                    // 开始连接
                    urlConn.connect();
                    // 发送请求参数
                    DataOutputStream dos = new DataOutputStream(urlConn.getOutputStream());
                    dos.write(postData);
                    dos.flush();
                    dos.close();
                    // 向Listener返回请求成败
                    final int responseCode = urlConn.getResponseCode();
                    if (200 == responseCode) {
                        // 获取返回的数据
                        final String result = streamToString(urlConn.getInputStream());
                        if (result != null) {
                            if (httpListener != null) {
                                activity.runOnUiThread(
                                        new Runnable() {
                                            @Override
                                            public void run() {
                                                httpListener.onHttpSuccess(result);
                                                try {
                                                    JSONObject jobj = new JSONObject(result);
                                                    if(jobj.getString("success").equals("1")){
                                                        httpListener.onFeedBackSuccess(jobj);
                                                    }else{
                                                        CzLibrary.alert(activity,jobj.getString("err_info"));
                                                    }
                                                }catch (Exception e){
                                                    CzLibrary.alert(activity,"获取数据异常："+e+","+result);
                                                    Log.e("requestPOST", "POST Request Success,but translate to JSONObject Exp"+e+" , result="+result);
                                                }

                                            }
                                        }
                                );
                            }
                            Log.e("requestPOST", "POST Request SUCCESS，url="+address+" / result--->" + result);
                        } else {
                            if (httpListener != null) {
                                activity.runOnUiThread(
                                        new Runnable() {
                                            @Override
                                            public void run() {
                                                CzLibrary.alert(activity,"服务器未返回，请稍后再试");
                                                Log.e("CzSys_Http","Fail!getResponse:Null / url="+address );
                                            }
                                        }
                                );
                            }
                            Log.e("requestPOST", "POST Request get Null Data  / url="+address);
                        }

                    } else {
                        if (httpListener != null) {
                            activity.runOnUiThread(
                                    new Runnable() {
                                        @Override
                                        public void run() {
                                            CzLibrary.alert(activity,"服务器响应错误，请稍后再试");
                                            Log.e("CzSys_Http","Fail!getResponseCode:" + responseCode+" / url="+address);
                                        }
                                    }
                            );
                        }
                        Log.e("requestPOST", "POST Request FAIL，code"+responseCode+" / url="+address);
                    }
                    // 关闭连接
                    urlConn.disconnect();
                } catch (final Exception e) {
                    Log.e("CzSys_Http","Fail!   url="+address+"Connect Exception:" + e);
                }
            }
        }.start();
    }

    /**
     * 下载文件
     * 会自动为项目建立一个包目录如 com.xxxx.xxxx
     * 然后在该目录下建立folderPath参数定义的目录，文件即下载入此目录
     * 成功回调后的
     */
    public static void downloadFile(final Activity activity,final String url,final String folderPath,final DownloadListener downloadListener) {
        //下载函数
        final String filename=url.substring(url.lastIndexOf("/") + 1);
        final String path =activity.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
                +"/"+ activity.getPackageName()
                +"/"+folderPath;
        File f=new File(path+"/"+filename);
        if(f.exists()){
            if(downloadListener!=null){
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        downloadListener.downloadInfo("文件已从缓存获取");
                        downloadListener.downloadSuccess(path+"/"+filename);
                    }
                });
            }
            return;
        }else{
            Log.e(TAG,"downloadFile /文件为下载");
        }

        new Thread(){
            public void run(){
                try{

                    final long startTime = System.currentTimeMillis();
                    if(downloadListener!=null){
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                downloadListener.downloadInfo("startTime="+startTime);
                            }
                        });
                    }

                    //获取文件名
                    URL myURL = new URL(url);
                    URLConnection conn = myURL.openConnection();
                    conn.connect();
                    InputStream is = conn.getInputStream();
                    int fileSize = conn.getContentLength();//根据响应获取文件大小
                    if (fileSize <= 0) {
                        if(downloadListener!=null){
                            activity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    downloadListener.downloadFail("无法获知文件大小 ");
                                }
                            });
                        }
                    }
                    if (is == null)  {
                        if(downloadListener!=null){
                            activity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    downloadListener.downloadFail("Stream == null ");
                                }
                            });
                        }
                    }
                    File file1 = new File(path);
                    if(!file1.exists()){
                        file1.mkdirs();
                    }
                    Log.e(TAG,"downloadFile / mkdirs done");
                    //把数据存入路径+文件名
                    FileOutputStream fos = new FileOutputStream(path+"/"+filename);
                    byte buf[] = new byte[1024];
                    long downLoadFileSize = 0;
                    do{
                        //循环读取
                        int numread = is.read(buf);
                        if (numread == -1)
                        {
                            break;
                        }
                        fos.write(buf, 0, numread);
                        downLoadFileSize += numread;

                        if(downloadListener!=null){
                            final int percent=(int)(downLoadFileSize*100/fileSize);
                            activity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    downloadListener.downloadPercent(percent);
                                }
                            });
                        }
                        //更新进度条
                    } while (true);
                    if(downloadListener!=null){
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                downloadListener.downloadSuccess(path+"/"+filename);
                                downloadListener.downloadInfo("totalTime="+ (System.currentTimeMillis() - startTime));
                            }
                        });
                    }
                    is.close();
                } catch (final Exception ex) {
                    //Log.e("DOWNLOAD", "error: " + ex.getMessage(), ex);
                    if(downloadListener!=null){
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                downloadListener.downloadFail("下载异常="+ ex);
                            }
                        });
                    }
                }
            }
        }.start();
    }


    /**
     * 将输入流转换成字符串
     *
     * @param is 从网络获取的输入流
     * @return
     */
    public static String streamToString(InputStream is) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int len = 0;
            while ((len = is.read(buffer)) != -1) {
                baos.write(buffer, 0, len);
            }
            baos.close();
            is.close();
            byte[] byteArray = baos.toByteArray();
            return new String(byteArray);
        } catch (Exception e) {
            Log.e("streamToString", e.toString());
            return null;
        }
    }

    /**
     * 可传入多张图片和参数
     *
     * @param actionUrl
     *   发送地址
     * @param params
     *   文本参数
     * @param files
     *   文件参数
     * @return
     * @throws
     */
    public static void multiUpload(final String actionUrl, final Map<String, String> params,
                                   final Map<String, File> files, final MultiUploadListener listener) throws Exception {

        new Thread(){
            public void run(){
//                String result = "";

                String BOUNDARY = java.util.UUID.randomUUID().toString();
                String PREFIX = "--", LINEND = "\r\n";
                String MULTIPART_FROM_DATA = "multipart/form-data";
                String CHARSET = "UTF-8";
                try {
                    URL uri = new URL(actionUrl);
                    HttpURLConnection conn = (HttpURLConnection) uri.openConnection();
                    conn.setReadTimeout(5 * 1000);
                    conn.setDoInput(true);// 允许输入
                    conn.setDoOutput(true);// 允许输出
                    conn.setUseCaches(false);
                    conn.setRequestMethod("POST"); // Post方式
                    conn.setRequestProperty("connection", "keep-alive");
                    conn.setRequestProperty("Charsert", "UTF-8");
                    conn.setRequestProperty("Content-Type", MULTIPART_FROM_DATA
                            + ";boundary=" + BOUNDARY);

                    // 首先组拼文本类型的参数
                    StringBuilder sb = new StringBuilder();
                    for (Map.Entry<String, String> entry : params.entrySet()) {
                        sb.append(PREFIX);
                        sb.append(BOUNDARY);
                        sb.append(LINEND);
                        sb.append("Content-Disposition: form-data; name=\""
                                + entry.getKey() + "\"" + LINEND);
                        sb.append("Content-Type: text/plain; charset=" + CHARSET + LINEND);
                        sb.append("Content-Transfer-Encoding: 8bit" + LINEND);
                        sb.append(LINEND);
                        sb.append(entry.getValue());
                        sb.append(LINEND);
                    }

                    DataOutputStream outStream = new DataOutputStream(
                            conn.getOutputStream());
                    outStream.write(sb.toString().getBytes());

                    // 发送文件数据
                    if (files != null)
                        for (Map.Entry<String, File> file : files.entrySet()) {
                            StringBuilder sb1 = new StringBuilder();
                            sb1.append(PREFIX);
                            sb1.append(BOUNDARY);
                            sb1.append(LINEND);
                            sb1.append("Content-Disposition: form-data; name=\"file\"; filename=\""
                                    + file.getKey() + "\"" + LINEND);
                            sb1.append("Content-Type: application/octet-stream; charset="
                                    + CHARSET + LINEND);
                            sb1.append(LINEND);
                            outStream.write(sb1.toString().getBytes());
                            InputStream is = new FileInputStream(file.getValue());
                            byte[] buffer = new byte[1024];
                            int len = 0;
                            while ((len = is.read(buffer)) != -1) {
                                outStream.write(buffer, 0, len);
                            }

                            is.close();
                            outStream.write(LINEND.getBytes());
                        }

                    // 请求结束标志
                    byte[] end_data = (PREFIX + BOUNDARY + PREFIX + LINEND).getBytes();
                    outStream.write(end_data);
                    outStream.flush();

                    InputStream is = conn.getInputStream();
                    InputStreamReader isr = new InputStreamReader(is, "utf-8");
                    BufferedReader br = new BufferedReader(isr);
                    String result = br.readLine();
                    outStream.close();
                    conn.disconnect();

                    if(listener!=null){
                        listener.onUploadResponse(result);
                    }else{
                        Log.e("CzSys_HTTP","multiPost / normal / HttpListener == null");
                    }
                }catch (Exception e){
                    if(listener!=null){
                        listener.onUploadFail("multiPost / Exception:"+e);
                    }else{
                        Log.e("CzSys_HTTP","multiPost / Exception / HttpListener == null");
                    }
                }
            }
        }.start();

    }

    /**
     * 服务器返回监听
     */
    public interface HttpListener {
        public void onHttpSuccess(String data);
//        public void onHttpFail(String failInfo);
//        public void onHttpNull();
        public void onFeedBackSuccess(JSONObject feedBackData);
    }

    public interface DownloadListener{
        public void downloadPercent(int percent);
        public void downloadSuccess(String localFilePath);
        public void downloadInfo(String info);
        public void downloadFail(String failInfo);
    }

    public interface MultiUploadListener{
        public void onUploadResponse(String response);
        public void onUploadFail(String failReason);
    }

    /********************************************
     * 云通讯
     */
    public static void postPush(final Activity activity, final String address, final String paramsJsonString,
                                final HttpListener httpListener,final String appId){
        new Thread() {
            public void run() {
                try {
                    SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
                    Date curDate = new Date(System.currentTimeMillis());
                    String timeString=formatter.format(curDate);

                    String params = paramsJsonString;
                    // 请求的参数转换为byte数组
                    byte[] postData = params.getBytes();
                    // 新建一个URL对象
                    URL url = new URL(address);
                    // 打开一个HttpURLConnection连接
                    HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();
                    // 设置连接超时时间
                    urlConn.setConnectTimeout(5 * 1000);
                    //设置从主机读取数据超时
                    urlConn.setReadTimeout(10 * 1000);
                    // Post请求必须设置允许输出 默认false
                    urlConn.setDoOutput(true);
                    //设置请求允许输入 默认是true
                    urlConn.setDoInput(true);
                    // Post请求不能使用缓存
                    urlConn.setUseCaches(false);
                    // 设置为Post请求
                    urlConn.setRequestMethod("POST");
                    //设置本次连接是否自动处理重定向
                    urlConn.setInstanceFollowRedirects(true);
                    // 配置请求Content-Type
                    urlConn.setRequestProperty("Accept", "application/x-www-form-urlencoded");
                    urlConn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                    urlConn.setRequestProperty("Content-Length", "application/x-www-form-urlencoded");
                    urlConn.setRequestProperty("Authorization", getYtxAuthorization(appId,timeString));
                    // 开始连接
                    urlConn.connect();
                    // 发送请求参数
                    DataOutputStream dos = new DataOutputStream(urlConn.getOutputStream());
                    dos.write(postData);
                    dos.flush();
                    dos.close();
                    // 向Listener返回请求成败
                    final int responseCode = urlConn.getResponseCode();
                    if (200 == responseCode) {
                        // 获取返回的数据
                        final String result = streamToString(urlConn.getInputStream());
                        if (result != null) {
                            if (httpListener != null) {
                                activity.runOnUiThread(
                                        new Runnable() {
                                            @Override
                                            public void run() {
                                                httpListener.onHttpSuccess(result);
                                            }
                                        }
                                );
                            }
                            Log.e("requestPOST", "POST Request SUCCESS，result--->" + result);
                        } else {
                            if (httpListener != null) {
                                activity.runOnUiThread(
                                        new Runnable() {
                                            @Override
                                            public void run() {
                                                CzLibrary.alert(activity,"服务器未返回，请稍后再试");
                                                Log.e("CzSys_Http","Fail!getResponse:Null");
                                            }
                                        }
                                );
                            }
                            Log.e("requestPOST", "POST Request get Null Data");
                        }

                    } else {
                        if (httpListener != null) {
                            activity.runOnUiThread(
                                    new Runnable() {
                                        @Override
                                        public void run() {
                                            CzLibrary.alert(activity,"服务器响应错误，请稍后再试");
                                            Log.e("CzSys_Http","Fail!getResponseCode:" + responseCode);
                                        }
                                    }
                            );
                        }
                        Log.e("requestPOST", "POST Request FAIL，code"+responseCode);
                    }
                    // 关闭连接
                    urlConn.disconnect();
                } catch (final Exception e) {
                    if (httpListener != null) {
                        activity.runOnUiThread(
                                new Runnable() {
                                    @Override
                                    public void run() {
                                        CzLibrary.alert(activity,"服务器连接异常，请稍后再试");
                                        Log.e("CzSys_Http","Fail!Connect Exception:" + e);
                                    }
                                }
                        );
                    }
                    Log.e("requestPOST", e.toString());
                }
            }
        }.start();
    }

    public static String getYtxSigParams(String appId,String token){
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        Date curDate = new Date(System.currentTimeMillis());
        String timeString=formatter.format(curDate);
        String auth= Base64.encodeToString((appId+":"+timeString).getBytes(),Base64.DEFAULT);
        return auth;
    }

    public static String getYtxAuthorization(String appId,String timeString){
        String auth= Base64.encodeToString((appId+":"+timeString).getBytes(),Base64.DEFAULT);
        return auth;
    }
}
