package com.shiwei.launcher;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.usb.UsbManager;
import android.media.MediaPlayer;
import android.os.BatteryManager;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.os.SystemProperties;


public class Launcher extends Activity {
	
	public final String TAG = "niotongyuan Launcher";
	//a64
	private final String mSDPath = "/storage/extsd/";
	//a40i
	//private final String mSDPath = "/storage/card/";
	
	private final String mNotifyPath = "/system/media/audio/ping-ing-ing-ing.mp3";
	
	public static int keyDownCount = 0;
	public static boolean sdcardStatus = false;
	final static int REQUEST_WRITE=1;
	
	private static boolean firstInitFlag = false;
	
	private static ImageView noSdcardView;		//显示“INSERT CARTRIDGE"的VIEW
	private static FrameLayout app_fl;			//包含“LOADING....."的view和控制"CARTRIDGE ERROR"的view，用来控制这两个view的显示
	private static ImageView ins_uninsView;	//用来显示”LOADING......“
	private static BatteryView batteryView;	//用来显示电量图标
	private static TextView versionView;		//用来显示版本号
	private static FrameLayout usbstateView;	
	
	private static int colorStatus  = 1; //1 red ;2 bule ;3 greeen //用来控制“INSERT CARTRIDGE”的颜色
	private static int loadStatus  = 1;	//用来控制安装卸载过程中显示哪张图片的Flag
	private static int cartErrStatus  = 1;	//用来控制“CARTRIDGE ERROR“的颜色
	
	private static Handler nocardViewHandler = new Handler();
	private static Handler installViewHandler = new Handler();
	
	static PackageUtil mPackageUtil;
	Handler mPackageHandler = new PackageHandler();
	Handler mInstallHandler = null;
	Handler mUnInstallHandler = null;
		
	private MediaPlayer mMediaPlayer;
	
	//PackageHandler对应的msg
	public final static int PARSE_BAT = 0;
	public final static int INSTALL_APK = 1;
	public final static int INSTALL_APK_DONE = 2;
	public final static int UNINSTALL_APK = 3;
	public final static int UNINSTALL_APK_DONE = 4;
	public final static int Open_APK = 5;
	
	BatteryStatusReceiver mBatteryStatusReceiver = null;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG,"onCreate() in");
        setContentView(R.layout.activity_main);
        //hideBottomUIMenu();
		playNotifyVoice();
        mPackageUtil = new PackageUtil(this,mPackageHandler);
        mInstallHandler = mPackageUtil.mInstallHandler;
        mUnInstallHandler = mPackageUtil.mUnInstallHandler;
        
        noSdcardView = (ImageView) findViewById(R.id.nosdcardwarning);
        app_fl = (FrameLayout) findViewById(R.id.app_framelayout);
        ins_uninsView = (ImageView) findViewById(R.id.ins_unins_view);
        batteryView = (BatteryView) findViewById(R.id.batteryView);
        versionView = (TextView) findViewById(R.id.versionTV);
        usbstateView = (FrameLayout) findViewById(R.id.usbstateView);
        
    	initCheckSD();
    	initVersion();//更新VERSION
    	initBattery();//更新电池图标状态
    }
    @Override
    protected void onStart(){
    	Log.d(TAG,"onStart() in");
    	super.onStart();
    }
    @Override
    protected void onPause(){
    	//Log.d(TAG,"onStart() in");
    	this.unregisterReceiver(mBatteryStatusReceiver);
    	super.onPause();
    }
    @Override
    protected void onResume(){
    	Log.d(TAG,"onResume() in");
    	mBatteryStatusReceiver = new BatteryStatusReceiver();		
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_BATTERY_CHANGED);
        filter.addAction("com.shiwei.launcher.usbstate");
        filter.addAction("android.hardware.usb.action.USB_STATE");
        this.registerReceiver(mBatteryStatusReceiver, filter);
    	super.onResume();
    }
    @Override
    protected void onDestroy(){
    	Log.d(TAG,"onStart() in");
    	super.onDestroy();
    }
    @Override
    protected void onNewIntent(Intent intent){
    	Log.d(TAG,"onNewIntent() in");
    	super.onNewIntent(intent);  
		 setIntent(intent);
		 Bundle bundle = intent.getExtras();
		 if(bundle!=null){
			 //isBroadCast用来判断是否是SDcardState.java跳转过来的，sdcardState用来指示sd卡的状态
			 if(bundle.getBoolean("sdBroadCast",false)){
				 Log.d(TAG,"onNewIntent() get data");
				 setSdcardStatusChg(bundle.getBoolean("sdcardState",sdcardStatus));
			 }
		 } 
    }
    
    /**
     * 隐藏虚拟按键，并且全屏
     */
    protected void hideBottomUIMenu(){
        //隐藏虚拟按键，并且全屏
        if (Build.VERSION.SDK_INT > 11 && Build.VERSION.SDK_INT < 19) { // lower api
            View v = this.getWindow().getDecorView();
            v.setSystemUiVisibility(View.GONE);
        } else if (Build.VERSION.SDK_INT >= 19) {
            //for new api versions.
            View decorView = getWindow().getDecorView();
            int uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                    | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                    | View.SYSTEM_UI_FLAG_IMMERSIVE;
            decorView.setSystemUiVisibility(uiOptions);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        }
    }

    /**
     * 重写按键按下事件,主要目的是实现客户需要的组合按键功能
     */
    @Override
    public boolean onKeyDown(int keyCode,KeyEvent event){
    	if (keyCode == KeyEvent.KEYCODE_A) {
    		keyDownCount = 1;
			return true;
		}
    	if (keyCode == KeyEvent.KEYCODE_D) {
    		if(keyDownCount == 1){
    			keyDownCount = 2;
    		}else{
    			keyDownCount = 0;
    		}
			return true;
		}
    	if (keyCode == KeyEvent.KEYCODE_B) {
    		if(keyDownCount == 2){
    			keyDownCount = 3;
    		}else{
    			keyDownCount = 0;
    		}
			return true;
		}
    	if (keyCode == KeyEvent.KEYCODE_C) {
    		if(keyDownCount == 3){
    			keyDownCount = 4;
    		}else{
    			keyDownCount = 0;
    		}
			return true;
		}

    	if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
    		if(keyDownCount == 4){
    			keyDownCount = 5;
    		}else{
    			keyDownCount = 0;
    		}
			return true;
		}
    	if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
    		if(keyDownCount == 5){
    			keyDownCount = 6;
    			startSettingActivity();
    		}else{
    			keyDownCount = 0;
    		}
			return true;
		}
    	if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
    		if(keyDownCount == 4){
    			keyDownCount = 7;
    		}else{
    			keyDownCount = 0;
    		}
			return true;
		}
    	if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
    		if(keyDownCount == 7){
    			keyDownCount = 8;
    			startAllappActivity();
    		}else{
    			keyDownCount = 0;
    		}
			return true;
		}
    	//重写返回键，防止apk反复重启
    	if (keyCode == KeyEvent.KEYCODE_BACK) {
			return true;
		}
    	//if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
    		//Intent intent=new Intent(Launcher.this, AllApp.class); startActivity(intent);
	//		return true;
	//	}
    	return super.onKeyDown(keyCode, event);
    }
    
    //用来播放应用首次进入时的提示音
    private void playNotifyVoice(){
    	File file = new File(mNotifyPath);    	
    	if(!file.exists()){
    		return;
    	}
    	mMediaPlayer = new MediaPlayer();
		try {
			mMediaPlayer.setDataSource(mNotifyPath);
			mMediaPlayer.prepareAsync();
			mMediaPlayer.start();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    /**
     * 跳转Settings应用
     */
    private void startSettingActivity(){
    	if(sdcardStatus == false){
    		startActivity(new Intent(Settings.ACTION_SETTINGS));
    	}
    }
    
    /**
     * 跳转客户需要的抽屉，也就是显示客户的所有apk
     */
    private void startAllappActivity(){
    	if(sdcardStatus == false){
    		//对应的操作
    		Intent intent=new Intent(Launcher.this, AllApp.class); startActivity(intent);
    	}
    }
    
    /**
     * 获取软件版本号并更新TextView
     */
    public void initVersion(){
    	String version;
    	version = SystemProperties.get("ro.shiwei.version", "Ver.00.00");
    	versionView.setText(version);
    }
    
    public void initBattery(){

    }
    
	private class BatteryStatusReceiver extends BroadcastReceiver{
		int powerlevel = 0;
		int status = BatteryManager.BATTERY_STATUS_UNKNOWN;
		int plugType = 0;

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub        
	        if (TextUtils.equals(intent.getAction(), Intent.ACTION_BATTERY_CHANGED)){
	        	
	        	status = intent.getIntExtra(BatteryManager.EXTRA_STATUS,BatteryManager.BATTERY_STATUS_UNKNOWN);
	        	plugType = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0);
	        	if(status == BatteryManager.BATTERY_STATUS_CHARGING){
	        		batteryView.setCharge(true);
	        	}else{
	        		batteryView.setCharge(false);
	        	}
	        	
	        	powerlevel = (int)(intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0) * 100 / intent.getIntExtra(BatteryManager.EXTRA_SCALE, 0));
	        	Log.d(TAG, "powerlevel = " + powerlevel);
	        	batteryView.setPower(powerlevel);
	        }else if (TextUtils.equals(intent.getAction(), "android.hardware.usb.action.USB_STATE")){
            	//Log.d(TAG,"get android.hardware.usb.action.USB_STATE Broadcast");
            	boolean connected = intent.getBooleanExtra("connected", false);
                if (connected) {
                	usbstateView.setVisibility(View.VISIBLE);
                }else{
                	usbstateView.setVisibility(View.INVISIBLE);
                }
	        }
		}  

	}
    
    /**
     * 开机通过查看sd卡挂载的目录是否存在，应用启动前是否就插入了sd卡
     */
    public void initCheckSD(){
    	File file = new File(mSDPath + "apps.bat");//单单检查mSDPath的路径是否存在，会在开机的时候存在问题，所以检查apps.bat这个文件是否存在
    	sdcardStatus = file.exists();
    	Log.d(TAG,"initCheckSD file.exists() = "+file.exists());
		showNoSdcardWarning(!file.exists());
		if (file.exists()) {
			Message msg = new Message(); // 这种方法也可以获取到Message对象
			msg.what = PARSE_BAT; // 标志消息的标志
			mPackageHandler.sendMessage(msg);
		}
    }

    /**
     * 用来处理接收到sd卡插拔广播后的处理
     * @param isexit
     */
    private  void setSdcardStatusChg(boolean isexit) {
		// TODO Auto-generated method stub
    	//Log.d(TAG,"setSdcardStatusChg isexit = "+isexit);
    	sdcardStatus = isexit;
		showNoSdcardWarning(!isexit);
		if(isexit){
			Message msg = new Message(); 	//这种方法也可以获取到Message对象
	        msg.what = PARSE_BAT;   //标志消息的标志
	        mPackageHandler.sendMessage(msg);
		}
	}
    
    /**
     * 如果sd卡存在，不显示TextView的内容
     * @param on
     */
	private void showNoSdcardWarning(boolean on){
    	//Log.d(TAG,"showNoSdcardWarning on = "+on);
		if(on){
			noSdcardView.setVisibility(View.VISIBLE);
			app_fl.setVisibility(View.INVISIBLE);
			installViewHandler.removeCallbacks(cartErrRunnable);
			
			ins_uninsView.setImageResource(R.drawable.loading_1);//保证下次安装从"LOADING."开始
			loadStatus = 1;
			installViewHandler.removeCallbacks(installRunnable);
			nocardViewHandler.post(nocardRunnable);
		}else{
			noSdcardView.setVisibility(View.INVISIBLE);
			nocardViewHandler.removeCallbacks(nocardRunnable);
		}
    }
    //用来控制显示“INSERT CARTRIDGE”的线程
	static Runnable nocardRunnable = new Runnable(){

		@Override
		public void run() {
			// TODO Auto-generated method stub
			if(colorStatus == 3){
				noSdcardView.setImageResource(R.drawable.insert_cart_red);
				colorStatus = 1;
			}else if(colorStatus == 1){
				noSdcardView.setImageResource(R.drawable.insert_cart_blue);
				colorStatus = 2;
			}else if(colorStatus == 2){
				noSdcardView.setImageResource(R.drawable.insert_cart_green);
				colorStatus = 3;
			}
			nocardViewHandler.postDelayed(this, 400);
			
		}	
	};
	//用来控制显示安装卸载过程loading的线程
	static Runnable installRunnable = new Runnable(){

		@Override
		public void run() {
			// TODO Auto-generated method stub
			if(loadStatus == 6){
				ins_uninsView.setImageResource(R.drawable.loading_1);
				loadStatus = 1;
			}else if(loadStatus == 1){
				ins_uninsView.setImageResource(R.drawable.loading_2);
				loadStatus = 2;
			}else if(loadStatus == 2){
				ins_uninsView.setImageResource(R.drawable.loading_3);
				loadStatus = 3;
			}else if(loadStatus == 3){
				ins_uninsView.setImageResource(R.drawable.loading_4);
				loadStatus = 4;
			}else if(loadStatus == 4){
				ins_uninsView.setImageResource(R.drawable.loading_5);
				loadStatus = 5;
			}else if(loadStatus == 5){
				ins_uninsView.setImageResource(R.drawable.loading_6);
				loadStatus = 6;
			}
			installViewHandler.postDelayed(this, 200);
			
		}	
	};
	
	//用来控制显示打开APK失败显示“CARTRIDGE ERROR”的线程
	static Runnable cartErrRunnable = new Runnable(){
		@Override
		public void run() {
			// TODO Auto-generated method stub
			if(cartErrStatus == 3){
				ins_uninsView.setImageResource(R.drawable.cart_err_red);
				cartErrStatus = 1;
			}else if(cartErrStatus == 1){
				ins_uninsView.setImageResource(R.drawable.cart_err_blue);
				cartErrStatus = 2;
			}else if(cartErrStatus == 2){
				ins_uninsView.setImageResource(R.drawable.cart_err_green);
				cartErrStatus = 3;
			}
			installViewHandler.postDelayed(this, 400);
			
		}	
	};

	public class PackageHandler extends Handler{
		boolean retValue = false;
		public PackageHandler(){
			super();
		}
	    public PackageHandler(Looper looper) {
	            super(looper);
	    }
		@Override
		public void handleMessage(Message msg){
			super.handleMessage(msg);
			Message nextMsg = new Message();  
			switch(msg.what){
				case PARSE_BAT:
					//用来解析客户的apps.bat
					Log.d(TAG,"PackageHandler PARSE_BAT");
					app_fl.setVisibility(View.INVISIBLE);
					retValue = mPackageUtil.parseAppsBAT();
					if(retValue){ 
				        nextMsg.what = INSTALL_APK;   //标志消息的标志
				        mPackageHandler.sendMessage(nextMsg);
					}
					break;
				case INSTALL_APK:
					//开始安装apk，通知PackageUtil中的InstallHandler,同时显示apk正在安装的进度框
					Log.d(TAG,"PackageHandler INSTALL_APK");
					app_fl.setVisibility(View.VISIBLE);
					installViewHandler.post(installRunnable);
					nextMsg.what = PackageUtil.INSTALL; 
					mInstallHandler.sendMessage(nextMsg);
					break;
				case INSTALL_APK_DONE:
					//从PackageUtil的InstallHandler获取到通知，关闭apk正在安装的进度框
					Log.d(TAG,"PackageHandler INSTALL_APK_DONE");
					app_fl.setVisibility(View.INVISIBLE);
					//安装apk操作完成之后，执行卸载apk操作
				    nextMsg.what = UNINSTALL_APK;   //标志消息的标志
				    mPackageHandler.sendMessage(nextMsg);
					break;
				case UNINSTALL_APK:
					//开始卸载apk，通知PackageUtil中的UnInstallHandler,同时显示apk正在卸载的进度框
					Log.d(TAG,"PackageHandler UNINSTALL_APK");
					app_fl.setVisibility(View.VISIBLE);
					nextMsg.what = PackageUtil.UNINSTALL; 
					mUnInstallHandler.sendMessage(nextMsg);
					break;
				case UNINSTALL_APK_DONE:
					//从PackageUtil的UnInstallHandler获取到通知，关闭apk正在卸载的进度框
					Log.d(TAG,"PackageHandler UNINSTALL_APK_DONE");
					app_fl.setVisibility(View.INVISIBLE);
					installViewHandler.removeCallbacks(installRunnable);
				    nextMsg.what = Open_APK;   //标志消息的标志
				    mPackageHandler.sendMessage(nextMsg);
					break;
				case Open_APK:
					//打开apk的操作
					Log.d(TAG,"PackageHandler Open_APK");
					retValue = mPackageUtil.openApk();
					if(!retValue){
						//执行客户要求的显示“CARTRIDGE ERR”
						app_fl.setVisibility(View.VISIBLE);
						installViewHandler.post(cartErrRunnable);
					}
					break;
			}
		}
	}
}
