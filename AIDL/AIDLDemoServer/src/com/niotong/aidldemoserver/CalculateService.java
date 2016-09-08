package com.niotong.aidldemoserver;

import com.niotong.aidl.CalculateInterface;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

public class CalculateService extends Service{

	private static final String TAG = "niotongyuan_CalculateService";
	private void LOG(String msg){
		Log.d(TAG,msg);
	}
	
	public void onCreate(){
		LOG("Service onCreate");
	}
	
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
	
		LOG("Service onBind");
		return mBinder;
	}
	public void onStart(Intent intent,int startID){
		LOG("Service onStart" + startID);
	}
	
	public void onDestroy(){
		LOG("Service onDestroy");
		super.onDestroy();
	}
	
	public boolean onUnbind (Intent intent){
		LOG("Service onUnbind");
		return super.onUnbind(intent);
	}
	
	public void onRebind(Intent intent){
		LOG("Service onRebind");
		super.onRebind(intent);
	}

	private final CalculateInterface.Stub mBinder = new CalculateInterface.Stub() {
		
		@Override
		public double doCalculate(double a, double b) throws RemoteException {
			// TODO Auto-generated method stub
			LOG("remote call from client! current thread id =  "+ Thread.currentThread());
			return a+b;
		}
	};


}
