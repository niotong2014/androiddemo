package com.niotong;

import com.ococci.aidl.OcocciInterface;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
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
	
	private static final String TAG = "niotongyuan_CalculateClient";
	private void LOG(String msg){
		Log.d(TAG,msg);
	}
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        Bundle args = new Bundle();
		Intent intent = new Intent("com.ococci.OcocciService");
		intent.putExtras(args);
		bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
		LOG("bind OcocciService");
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
			try {
				for(int i = 1;i<=25;i++)
				{
					if((i%2) == 0){
						retVal = mService.usbFunc(i, 1, true);
					
					}else{
						retVal = mService.usbFunc(i, 1, false);
					}
					LOG("set usbid = "+i+"  retVal ="+retVal);
				}
				for(int i = 1;i<=25;i++)
				{
					usbstatus =  false;
					retVal = mService.usbFunc(i, 2, usbstatus);
					LOG("get usbid = "+i+"  retVal ="+retVal);
				}
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
		}
	};
}
