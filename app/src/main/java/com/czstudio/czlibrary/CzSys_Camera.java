package com.czstudio.czlibrary;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.os.Environment;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.ViewGroup;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;


public class CzSys_Camera
{
	CzSys_Camera instance;
	Camera mCamera;
	SurfaceView mSurfaceView;
	SurfaceHolder mSurfaceHolder;

	YuvImage yuvImage;
	ByteArrayOutputStream outstream;
	public byte[] rawDataArray;
	Bitmap pictureBitmap;
	
	public boolean isPreview=false;

	Context mContext;
	
	public CzSys_Camera(Context context, SurfaceView surfaceView){
		Log.e("CzSys_Camera","init");
		instance=this;
		mContext=context;
		mCamera=Camera.open(Camera.CameraInfo.CAMERA_FACING_FRONT);
		if(surfaceView!=null){
			Log.e("CzSys_Camera","surface!=null");
			mSurfaceView=surfaceView;
		}else{
			Log.e("CzSys_Camera","surface==null");
			mSurfaceView=new SurfaceView(context);
		}

		mSurfaceHolder = mSurfaceView.getHolder();
		mSurfaceHolder.addCallback(new Callback() {
				@Override
				public void surfaceChanged(SurfaceHolder holder, int format,
										   int width, int height) {
					initCamera(width, height);
				}

				@Override
				public void surfaceCreated(SurfaceHolder holder) {

				}

				@Override
				public void surfaceDestroyed(SurfaceHolder holder) {
					
					if (mCamera != null) {
						if (isPreview)
							mCamera.stopPreview();
						mCamera.release();
						mCamera = null;
					}
					System.exit(0);
				}
			});
		
		mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	
	}
	
	void initCamera(int width, int height) {
		if (mCamera != null && !isPreview) {
			try {
				Camera.Parameters parameters = mCamera.getParameters();
				parameters.setPreviewSize(width, height);
				parameters.setPreviewFpsRange(20, 30);
				parameters.setPictureFormat(ImageFormat.NV21);
				parameters.setPictureSize(width, height);
				mCamera.setPreviewDisplay(mSurfaceHolder);
				mCamera.setPreviewCallback(new Camera.PreviewCallback(){
						@Override
						public void onPreviewFrame(byte[] data, Camera camera) {

							try {
								Size size = camera.getParameters().getPreviewSize();
								yuvImage = new YuvImage(data, ImageFormat.NV21, size.width,
															  size.height, null);
								if (yuvImage != null) {
									outstream = new ByteArrayOutputStream();
									yuvImage.compressToJpeg(new Rect(0, 0, size.width, size.height),
														 20, outstream);
									rawDataArray = outstream.toByteArray();
									if(mCameraListener!=null) {
										mCameraListener.didCameraGetRawData(instance,rawDataArray);
									}
									pictureBitmap = BitmapFactory.decodeByteArray(rawDataArray, 0, rawDataArray.length);
									if(mCameraListener!=null) {
										mCameraListener.didCameraGetBitmap(instance,pictureBitmap);
									}
									pictureBitmap=null;
									outstream.flush();
								}
							} catch (Exception ex) {
								Log.e("Sys", "Error:" + ex.getMessage());
							}
						}
				});
				mCamera.startPreview();
				mCamera.autoFocus(null);
			} catch (Exception e) {
				e.printStackTrace();
			}
			isPreview = true;
		}
	}

	public String savePicture(){
		File pictureFileDir = getDir();

		if (!pictureFileDir.exists() && !pictureFileDir.mkdirs()) {
			Toast.makeText(mContext, "Can't create directory to save image.",
					Toast.LENGTH_LONG).show();
			return null;
		}

		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
		String date = dateFormat.format(new Date());
		String photoFile = "Picture_" + date + ".jpg";

		final String filename = pictureFileDir.getPath() + File.separator + photoFile;

		File pictureFile = new File(filename);
		System.out.println("filename is "+ filename);

		try {
			FileOutputStream fos = new FileOutputStream(pictureFile);
			fos.write(rawDataArray);
			fos.close();

			Log.e("","New Image saved:" + filename);
			return filename;
		} catch (Exception error) {
			Log.e("","Image could not be saved."+error);
			return null;
		}
	}

	private File getDir() {
		File sdDir = Environment
				.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
		return new File(sdDir, "ServiceCamera");
	}




	public interface CameraListener{
		public void didCameraGetRawData(CzSys_Camera mCamera, byte[] data);
		public void  didCameraGetBitmap(CzSys_Camera mCamera, Bitmap bitmap);
	}

	CameraListener mCameraListener=null;

	public void addCameraListener(CameraListener listener){
		mCameraListener=listener;
	}
	
	public void release(){
		isPreview=false;
		mCamera.stopPreview();
		mCamera=null;
		mSurfaceHolder=null;
		mSurfaceView=null;

		yuvImage=null;
		outstream=null;
		rawDataArray=null;
		pictureBitmap=null;
		instance=null;
	}
}
