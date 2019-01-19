package com.niotong;

import java.io.File;

import com.ococci.aidl.OcocciInterface;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

//0，默认值
//1，android发送串口数据的函数的参数错误
//2，android端不能接收到第一层stm的数据
//3，android端串口节点无法读写
//4，android端从第一层stm接收到的数据有误
//5，第一层stm从android接收到的数据有误
//6，第一层stm从第二stm接收到的数据有误
//7，第二层stm从第一层stm接收到的数据有误
//8，函数set IO口成功
//9，函数set IO口失败
//10，函数get IO口状态为高
//11，函数get IO口状态为低
//12，其他错误
/*
 * *
 * return 为上面的说明
 * USBID的编号为1-100
 * TYPE  1为设置  2为获取
 * ISON 当TYPE为1的时候ISON有效，true为供电，false为不供电
 */
//usbFunc(USBID, TYPE, ISON);
public class MainActivity extends Activity{

	OcocciInterface mService;
	IntentFilter filter;
	private static final int CMD_TIME = 10000;
	private static final int LED_CMD_TIME = 500;
	Handler handler = new Handler();
	Handler ledhandler = new Handler();
	private static boolean LED_KEY = false;
	
	private static final String TAG = "niotongyuan_ethSerialClient";
	private static final String ACTION_TESTSTM = "com.niotong.teststm";
	private static final String ACTION_CMDSWITCH = "com.niotong.cmdswitch";
	private void LOG(String msg){
		Log.d(TAG,msg);
	}
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        filter = new IntentFilter();
        filter.addAction(ACTION_TESTSTM);
        filter.addAction(ACTION_CMDSWITCH);
        Bundle args = new Bundle();
		Intent intent = new Intent("com.ococci.OcocciService");
		intent.putExtras(args);
		bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
		LOG("bind OcocciService");
		
		
		registerReceiver(mTetsSTM,filter);
    }
    
    @Override
    protected void onDestroy() {
    	unbindService(mConnection);
    	super.onDestroy();
    }

	private ServiceConnection mConnection = new ServiceConnection() {
		
		@Override
		public void onServiceDisconnected(ComponentName arg0) {
			// TODO Auto-generated method stub
			LOG("onServiceDisconnected");
			mService = null;
		}
		
		@Override
		public void onServiceConnected(ComponentName arg0, IBinder arg1) {
			// TODO Auto-generated method stub
			LOG("onServiceConnected");
			boolean usbstatus = false;
			int retVal = 0;
			mService = OcocciInterface.Stub.asInterface(arg1);
			//下面这个例子是，将偶数的USB口打开，奇数的管掉，并且设置完之后读取
/*
			try {
				for(int i = 1;i<=100;i++)
				{
					retVal = mService.usbFunc(i, 1, true);
				}
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
*/
			//handler.postDelayed(runnable, CMD_TIME);
			try {
				mService.powerCTL(0,true);	//open DC1
				mService.powerCTL(1,true);	//open DC2
				mService.powerCTL(2,true);	//open DC3
				mService.powerCTL(3,true);	//open DC4
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			handler.post(runnable);
			ledhandler.post(ledrunnable);
		}
	};
	
	Runnable runnable = new Runnable(){

		@Override
		public void run() {
			// TODO Auto-generated method stub
			try {
				if(mService != null){
					mService.isAlive();
				}
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			handler.postDelayed(this, CMD_TIME);
		}
		
	};
	Runnable ledrunnable = new Runnable(){

		@Override
		public void run() {
			// TODO Auto-generated method stub
			try {
				if(mService != null){
					LED_KEY = !LED_KEY;
					mService.ledSwitch(LED_KEY);
				}
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			ledhandler.postDelayed(this, LED_CMD_TIME);
		}
		
	};
	private final BroadcastReceiver mTetsSTM = new BroadcastReceiver() {

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            boolean switchcmd = true;
            int retVal = 0;
            if (ACTION_TESTSTM.equals(action)) {
            	if(mService == null)
            		return;
                synchronized (this) {
                	/*
                	try {
        				for(int i = 1;i<=100;i++)
        				{
        					retVal = mService.usbFunc(i, 1, true);
        				}
        				Thread.sleep(15000);
        				getUSBNode();
        			} catch (RemoteException e) {
        				// TODO Auto-generated catch block
        				e.printStackTrace();
        			} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					*/
                	getUSBNode();
                }
            }
            if (ACTION_CMDSWITCH.equals(action)) {
            	if(mService == null)
            		return;
                synchronized (this) {
                	try {
                		switchcmd = intent.getBooleanExtra("switch", true);
                		mService.cmdSwitch(switchcmd);//单片机重启android的功能的功能,ture开启功能，false关闭
        			} catch (RemoteException e) {
        				// TODO Auto-generated catch block
        				e.printStackTrace();
        			}
                	
                }
            }
        }
    };
    private void getUSBNode(){	 
    	//1-1.X.Y.Z
    	///sys/bus/usb/devices/1-1.4.1.5
 	   String result = ""; 
 	   String usbBusPath;
 	   int xPath,yPath,zPath,usbNum;
 	   int okNum = 0;
 	   int failNum = 0;
 	   boolean usbStatus = false;
 	   File[] files = new File("/sys/bus/usb/devices").listFiles(); 
 	   for(int i = 1;i<=100;i++){
 		   usbNum = i;
 		   if(usbNum>=1 && usbNum<=25){
 			   xPath=4;
 		   }else if(usbNum>=26 && usbNum<=50){
 			   xPath=3;
 		   }else if(usbNum>=51 && usbNum<=75){
 			   xPath=2;
 		   }else if(usbNum>=76 && usbNum<=100){
 			   xPath=1;
 		   }else{
 			   LOG("ERROR");
 			   return;
 		   }
 		   usbNum = usbNum % 25;
 		   if(usbNum>=1 && usbNum<=4){
 			   yPath=4;
 		   }else if(usbNum>=5 && usbNum<=11){
 			   yPath=3;
 		   }else if(usbNum>=12 && usbNum<=18){
 			   yPath=2;
 		   }else if(usbNum>=19 && usbNum<=24){
 			   yPath=1;
 		   }else if(usbNum == 0){
 			   yPath=1;
 		   }else{
 			   LOG("ERROR");
 			   return;
 		   }
 		   switch(usbNum){
 		   case 0:
 			  zPath = 6;
 			   break;
 		   case 1:
 			  zPath = 4;
 			   break;
 		   case 2:
 			  zPath = 3;
 			   break;
 		   case 3:
 			  zPath = 2;
 			   break;
 		   case 4:
 			  zPath = 1;
 			   break;
 		   case 5:
 			  zPath = 4;
 			   break;
 		   case 6:
 			  zPath = 7;
 			   break;
 		   case 7:
 			  zPath = 3;
 			   break;
 		   case 8:
 			  zPath = 2;
 			   break;
 		   case 9:
 			  zPath = 1;
 			   break;
 		   case 10:
 			  zPath = 5;
 			   break;
 		   case 11:
 			  zPath = 6;
 			   break;
 		   case 12:
 			  zPath = 4;
 			   break;
 		   case 13:
 			  zPath = 7;
 			   break;
 		   case 14:
 			  zPath = 3;
 			   break;
 		   case 15:
 			  zPath = 2;
 			   break;
 		   case 16:
 			  zPath = 1;
 			   break;
 		   case 17:
 			  zPath = 5;
 			   break;
 		   case 18:
 			  zPath = 6;
 			   break;
 		   case 19:
 			  zPath = 7;
 			   break;
 		   case 20:
 			  zPath = 4;
 			   break;
 		   case 21:
 			  zPath = 3;
 			   break;
 		   case 22:
 			  zPath = 2;
 			   break;
 		   case 23:
 			  zPath = 1;
 			   break;
 		   case 24:
 			  zPath = 5;
 			   break;
 		   default:
 			   LOG("ERROR");
 			   return;
 		   }
 		  usbBusPath = new String("/sys/bus/usb/devices/"+"1-1."+xPath+"."+yPath+"."+zPath);
 		  //LOG(""+usbBusPath);
 		  for (File file : files) { 
 	 		   //LOG(""+file.getPath());
 			 usbStatus = false;
 			 if(file.getPath().equals(usbBusPath)){
 				 //LOG("USB"+i+"  is ok");
 				 usbStatus = true;
 				 break;
 			 }
 	 	   }
 		  if(usbStatus == false){
 			  LOG("USB"+i+"   is fail");
 			  failNum++;
 		  }else{
 			  okNum++;
 		  }
 	   }
 	  	LOG("USB "+okNum+" port is ok; "+failNum+" port is fail");
    	return;
    }
}
