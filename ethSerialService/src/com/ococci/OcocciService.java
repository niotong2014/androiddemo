/*
 * Copyright 2009 Cedric Priscal
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */

package com.ococci;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.security.InvalidParameterException;

import android.app.AlertDialog;
import android.app.Service;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.ococci.SerialPort;
import com.ococci.aidl.OcocciInterface;

import android.net.ethernet.EthernetDevInfo;
import android.net.ethernet.EthernetManager;

public class OcocciService extends Service {

	protected SerialPort mSerialPort;
	protected OutputStream mOutputStream;
	private InputStream mInputStream;
	private ReadThread mReadThread;
	Map<String,String> mIPInfoMap = new HashMap<String,String>();
	
	private EthernetManager mEthManager;
	private EthernetDevInfo mInterfaceInfo;
	private List<EthernetDevInfo> mListDevices = new ArrayList<EthernetDevInfo>();
	
	private static final String TAG = "niotongyuan_SerialPortService";
	
	private void LOG(String msg){
		Log.d(TAG,msg);
	}
	
	private void DisplayError(int resourceId) {
		AlertDialog.Builder b = new AlertDialog.Builder(this);
		b.setTitle("Error");
		b.setMessage(resourceId);
		b.setPositiveButton("OK", new OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				OcocciService.this.stopSelf();
			}
		});
		b.show();
	}
	
	public void onCreate(){
		LOG("Service onCreate");
		super.onCreate();
		mEthManager = EthernetManager.getInstance();
		mListDevices = mEthManager.getDeviceNameList();
		for(EthernetDevInfo deviceinfo : mListDevices){
			if(!deviceinfo.getIfName().equals("eth0")){
			//	tmpPreference = new EthPreference(getActivity(), deviceinfo);
			//	mSelected = tmpPreference;
			}else{
//				mInterfaceInfo.setHwaddr(deviceinfo.getHwaddr());
				mInterfaceInfo = deviceinfo;
				LOG("IfName = " + deviceinfo.getIfName());
			//	upEthdevice = new EthPreference(getActivity(), DevIfo);
			}
		}
	
		try {
			mSerialPort = new SerialPort(new File("/dev/ttyS2"), 115200, 0);
			mOutputStream = mSerialPort.getOutputStream();
			mInputStream = mSerialPort.getInputStream();

			/* Create a receiving thread */
			mReadThread = new ReadThread();
			mReadThread.start();
		} catch (SecurityException e) {
			DisplayError(R.string.error_security);
		} catch (IOException e) {
			DisplayError(R.string.error_unknown);
		} catch (InvalidParameterException e) {
			DisplayError(R.string.error_configuration);
		}
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
		if (mReadThread != null)
			mReadThread.interrupt();
		if(mSerialPort != null)
			mSerialPort.serial_close();
		mSerialPort = null;
	}
	
	public boolean onUnbind (Intent intent){
		LOG("Service onUnbind");
		return super.onUnbind(intent);
	}
	
	public void onRebind(Intent intent){
		LOG("Service onRebind");
		super.onRebind(intent);
	}

	private final OcocciInterface.Stub mBinder = new OcocciInterface.Stub() {
		
		@Override
		public void setIPDHCP() throws RemoteException {
			// TODO Auto-generated method stub
			LOG("remote call from client! current thread id =  "+ Thread.currentThread());
		}

		@Override
		public double doCalculate(double a, double c) throws RemoteException {
			// TODO Auto-generated method stub
			LOG("remote call from client! current thread id =  "+ Thread.currentThread());
			return 0;
		}
	};

	private class ReadThread extends Thread {

		@Override
		public void run() {
			super.run();
			while(!isInterrupted()) {
				int size;
				try {
					byte[] buffer = new byte[64];
					if (mInputStream == null) return;
					size = mInputStream.read(buffer);
					if (size > 0) {
						onDataReceived(buffer, size);
					}
				} catch (IOException e) {
					e.printStackTrace();
					return;
				}
			}
		}
	}

	protected  void onDataReceived(final byte[] buffer, final int size){
		//parse command if command is incorrect return error info
		//if command is setIP do setIP(set ip ,and return is success)
		//if command is getIP do getIP(get IP info and write it to uart)
		setIP();
		getIP();
	}
	
	private void setIPMap(){
		mIPInfoMap.put("Mode", mInterfaceInfo.getConnectMode()==EthernetDevInfo.ETHERNET_CONN_MODE_MANUAL?"manual":"dhcp");
		mIPInfoMap.put("IfName", mInterfaceInfo.getIfName());
		mIPInfoMap.put("IP", mInterfaceInfo.getIpAddress());
		mIPInfoMap.put("NetMask", mInterfaceInfo.getNetMask());
		mIPInfoMap.put("GateWay", mInterfaceInfo.getGateWay());
		mIPInfoMap.put("DnsAddr", mInterfaceInfo.getDnsAddr());
		mIPInfoMap.put("Hwaddr", mInterfaceInfo.getHwaddr());
	}
	
	private void setIP(){
		LOG("----doing something to set IP");
		mInterfaceInfo.setConnectMode(mIPInfoMap.get("Mode").equals("manual")?EthernetDevInfo.ETHERNET_CONN_MODE_MANUAL:EthernetDevInfo.ETHERNET_CONN_MODE_DHCP);
		mInterfaceInfo.setIpAddress(mIPInfoMap.get("IP"));
		mInterfaceInfo.setNetMask(mIPInfoMap.get("NetMask"));
		mInterfaceInfo.setDnsAddr(mIPInfoMap.get("DnsAddr"));
		mInterfaceInfo.setGateWay(mIPInfoMap.get("GateWay"));
		mEthManager.updateDevInfo(mInterfaceInfo);
	}
	
	private void getIP(){
		LOG("----doing something to get IP");
	}
}
