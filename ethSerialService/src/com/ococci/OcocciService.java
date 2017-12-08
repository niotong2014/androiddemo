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
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import com.ococci.SerialPort;
import com.ococci.aidl.OcocciInterface;
import android.net.ethernet.EthernetDevInfo;
import android.net.ethernet.EthernetManager;

public class OcocciService extends Service {

	private static final String PC_SERIAL = "/dev/ttyS3";
	private static final String STM_SERIAL = "/dev/ttyS1";
	
	protected SerialPort mPCSerialPort;
	protected OutputStream mPCOutputStream;
	private InputStream mPCInputStream;
	
	protected SerialPort mSTMSerialPort;
	protected OutputStream mSTMOutputStream;
	private InputStream mSTMInputStream;
	
	private PCReadThread mPCReadThread;
	private STMReadThread mSTMReadThread;
	Map<String,String> mIPInfoMap = new HashMap<String,String>();
	
	private EthernetManager mEthManager;
	private EthernetDevInfo mInterfaceInfo;
	private List<EthernetDevInfo> mListDevices = new ArrayList<EthernetDevInfo>();
	
	SharedPreferences myPreference; 
	
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
		myPreference = getSharedPreferences("ipset",Context.MODE_PRIVATE);
		IPInit();
	
		try {
			mPCSerialPort = new SerialPort(new File(PC_SERIAL), 115200, 0);
			mPCOutputStream = mPCSerialPort.getOutputStream();
			mPCInputStream = mPCSerialPort.getInputStream();
			/* Create a receiving thread */
			mPCReadThread = new PCReadThread();
			mPCReadThread.start();
			
			mSTMSerialPort = new SerialPort(new File(STM_SERIAL), 115200, 0);
			mSTMOutputStream = mSTMSerialPort.getOutputStream();
			mSTMInputStream = mSTMSerialPort.getInputStream();
			/* Create a receiving thread */
			mSTMReadThread = new STMReadThread();
			mSTMReadThread.start();
			
			
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
		if (mPCReadThread != null)
			mPCReadThread.interrupt();
		if(mPCSerialPort != null)
			mPCSerialPort.serial_close();
		mPCSerialPort = null;
		
		if (mSTMReadThread != null)
			mSTMReadThread.interrupt();
		if(mSTMSerialPort != null)
			mSTMSerialPort.serial_close();
		mSTMSerialPort = null;
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

	private class PCReadThread extends Thread {

		@Override
		public void run() {
			super.run();
			while(!isInterrupted()) {
				int size;
				int i;
				try {
					byte[] buffer = new byte[64];
					if (mPCInputStream == null) return;
					size = mPCInputStream.read(buffer);
					if (size > 0) {
						for (i = 0;i < size;i++){
							LOG("----yuan----"+buffer[i]+"------");
						}
						onPCDataReceived(buffer, size);
					}
				} catch (IOException e) {
					e.printStackTrace();
					return;
				}
			}
		}
	}
	
	protected void onPCDataReceived(final byte[] buffer, final int size) {
		String pccmd = new String(buffer, 0, size);
		LOG("PC: " + pccmd);
		try {
			if (parsePCCMD(pccmd)) {
				if(myPreference.getString("Type", "GET").equals("GET")){
					//do something
					mPCOutputStream.write(new String("OCO;"+
					myPreference.getString("Mode", "dhcp")+";"+
					myPreference.getString("IP", "0.0.0.0")+";"+
					myPreference.getString("NetMask", "255.255.255.0")+";"+
					myPreference.getString("GateWay", "8.8.8.8")+";"+
					myPreference.getString("DnsAddr", "0.0.0.0")+
							";CCI").getBytes());
					return;
				}
				if (setSharedPerenceToIP() == true) {
					mPCOutputStream.write(new String("OCO;SUCCESS;SETIP;CCI")
							.getBytes());
				} else {
					mPCOutputStream.write(new String("OCO;FAIL;SETIP;CCI")
							.getBytes());
				}
			} else {
				// the cmd is not cmd
				mPCOutputStream.write(new String("OCO;FAIL;PARSEPCCMD;CCI")
						.getBytes());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
	
	
	
	private class STMReadThread extends Thread {

		@Override
		public void run() {
			super.run();
			while(!isInterrupted()) {
				int size;
				try {
					byte[] buffer = new byte[64];
					if (mSTMInputStream == null) return;
					size = mSTMInputStream.read(buffer);
					if (size > 0) {
						onSTMDataReceived(buffer, size);
					}
				} catch (IOException e) {
					e.printStackTrace();
					return;
				}
			}
		}
	}

	protected  void onSTMDataReceived(final byte[] buffer, final int size){
	
		LOG("STM: "+new String(buffer, 0, size));
		
	}
	
	
	
	private boolean parsePCCMD(String mCmd){
		String[] cmdArray = mCmd.split(";");
		int size = cmdArray.length;
		Editor editor = myPreference.edit();
		for(String s : cmdArray){
			LOG("cmdArray[]:"+s+"-----");
		}
		//OCO；SET；MANUAL；192.168.0.177；255.255.255.0；192.168.0.2；192.168.0.2；CCI
		//OCO；SET；DHCP；CCI
		//OCO；GET；CCI
		if(!cmdArray[0].equals("OCO")){
			return false;
		}
		if(!cmdArray[size-1].equals("CCI")){
			return false;
		}
		editor.putString("Type", cmdArray[1]);
		if(size == 8){
			editor.putString("Mode","manual");
			editor.putString("IP", cmdArray[3]);
			editor.putString("NetMask", cmdArray[4]);
			editor.putString("GateWay", cmdArray[5]);
			editor.putString("DnsAddr", cmdArray[6]);
		}else if(size == 4){
			editor.putString("Mode","dhcp");
		}else if(size == 3){
			//do something
		}else{
			return false;
		}
		return true;
	}
	
	
	private void IPInit(){
		mEthManager = EthernetManager.getInstance();
		if(mEthManager.getState() == EthernetManager.ETHERNET_STATE_ENABLED){
			LOG("ethernet is enabled");
		}else{
			LOG("ethernet is disabled");
		}
		mListDevices = mEthManager.getDeviceNameList();
		if (mListDevices != null) {
			for (EthernetDevInfo deviceinfo : mListDevices) {
				if (!deviceinfo.getIfName().equals("eth0")) {
					LOG("one IfName = " + deviceinfo.getIfName());
				} else {
					mInterfaceInfo = deviceinfo;
					LOG("sec IfName = " + deviceinfo.getIfName());
				}
				try {
					//mInterfaceInfo.setConnectMode(EthernetDevInfo.ETHERNET_CONN_MODE_DHCP);
					mEthManager.updateDevInfo(mInterfaceInfo);
					mEthManager.setEnabled(true);
					Thread.sleep(500);
				} catch (Exception e) {
					LOG("set ethernet enable fail");
				}
			}
		} else {
			LOG("there is no ethernet devices");
		}
		setSharedPerenceToIP();
	}
	
	@SuppressLint("CommitPrefEdits")
	private boolean setIPToSharedPerence(){
		LOG("----setIPToSharedPerence----");
		if(mInterfaceInfo == null){
			LOG("setIPToSharedPerence() mInterfaceInfo is null");
			return false;
		}
		Editor editor = myPreference.edit();
		editor.putString("Mode", mInterfaceInfo.getConnectMode()==EthernetDevInfo.ETHERNET_CONN_MODE_MANUAL?"manual":"dhcp");
		editor.putString("IfName", mInterfaceInfo.getIfName());
		editor.putString("IP", mInterfaceInfo.getIpAddress());
		editor.putString("NetMask",mInterfaceInfo.getNetMask());
		editor.putString("GateWay", mInterfaceInfo.getGateWay());
		editor.putString("DnsAddr", mInterfaceInfo.getDnsAddr());
		editor.putString("Hwaddr", mInterfaceInfo.getHwaddr());
		return true;
	}
	
	@SuppressLint("CommitPrefEdits")
	private boolean setSharedPerenceToIP(){
		LOG("----setSharedPerenceToIP------");
		if(mInterfaceInfo == null){
			LOG("setSharedPerenceToIP() mInterfaceInfo is null");
			return false;
		}
		if(myPreference.contains("Mode") == false){
			Editor editor = myPreference.edit();
			editor.putString("Mode", "dhcp");
		}
		mInterfaceInfo.setConnectMode(myPreference.getString("Mode","dhcp").equals("manual")?EthernetDevInfo.ETHERNET_CONN_MODE_MANUAL:EthernetDevInfo.ETHERNET_CONN_MODE_DHCP);
		
		if (myPreference.getString("Mode","dhcp").equals("manual")) {
			mInterfaceInfo.setIpAddress(myPreference.getString("IP","0.0.0.0"));
			mInterfaceInfo.setNetMask(myPreference.getString("NetMask","255.255.255.0"));
			mInterfaceInfo.setDnsAddr(myPreference.getString("DnsAddr","8.8.8.8"));
			mInterfaceInfo.setGateWay(myPreference.getString("GateWay","0.0.0.0"));
		}
		try{
			mEthManager.updateDevInfo(mInterfaceInfo);
			mEthManager.setEnabled(true);
			Thread.sleep(500);
		}catch(Exception e){
			LOG("setSharedPerenceToIP() set the saved ethernet enable fail");
			return false;
		}
		return true;
	}
	

	private boolean setIPToMap(){
		LOG("----setIPMap----");
		if(mInterfaceInfo == null){
			LOG("setIPMap() mInterfaceInfo is null");
			return false;
		}
		mIPInfoMap.put("Mode", mInterfaceInfo.getConnectMode()==EthernetDevInfo.ETHERNET_CONN_MODE_MANUAL?"manual":"dhcp");
		mIPInfoMap.put("IfName", mInterfaceInfo.getIfName());
		mIPInfoMap.put("IP", mInterfaceInfo.getIpAddress());
		mIPInfoMap.put("NetMask", mInterfaceInfo.getNetMask());
		mIPInfoMap.put("GateWay", mInterfaceInfo.getGateWay());
		mIPInfoMap.put("DnsAddr", mInterfaceInfo.getDnsAddr());
		mIPInfoMap.put("Hwaddr", mInterfaceInfo.getHwaddr());
		return true;
	}
	
	private boolean setMapToIP(){
		LOG("----doing something to set IP");
		if(mInterfaceInfo == null){
			LOG("setIP() mInterfaceInfo is null");
			return false;
		}
		mInterfaceInfo.setConnectMode(mIPInfoMap.get("Mode").equals("manual")?EthernetDevInfo.ETHERNET_CONN_MODE_MANUAL:EthernetDevInfo.ETHERNET_CONN_MODE_DHCP);
		
		if (mIPInfoMap.get("Mode").equals("manual")) {
			mInterfaceInfo.setIpAddress(mIPInfoMap.get("IP"));
			mInterfaceInfo.setNetMask(mIPInfoMap.get("NetMask"));
			mInterfaceInfo.setDnsAddr(mIPInfoMap.get("DnsAddr"));
			mInterfaceInfo.setGateWay(mIPInfoMap.get("GateWay"));
		}
		try{
			mEthManager.updateDevInfo(mInterfaceInfo);
			mEthManager.setEnabled(true);
			Thread.sleep(500);
		}catch(Exception e){
			LOG("setIP() set the saved ethernet enable fail");
			return false;
		}
		return true;
	}

	
	private boolean PrintIPInfo(){
		LOG("----doing something to PrintIPInfo");
		if(mInterfaceInfo == null){
			LOG("PrintIPInfo() mInterfaceInfo is null");
			return false;
		}
		LOG("Mode = "+ (mInterfaceInfo.getConnectMode()==EthernetDevInfo.ETHERNET_CONN_MODE_MANUAL?"manual":"dhcp"));
		LOG("IfName = "+mInterfaceInfo.getIfName());
		LOG("IP = "+mInterfaceInfo.getIpAddress());
		LOG("NetMask = "+mInterfaceInfo.getNetMask());
		LOG("GateWay = "+mInterfaceInfo.getGateWay());
		LOG("DnsAddr = "+mInterfaceInfo.getDnsAddr());
		LOG("Hwaddr = "+mInterfaceInfo.getHwaddr());
		return true;
	}
	
	private void testSetMap(){
		LOG("----testSetMap----");
		mIPInfoMap.put("Mode", "dhcp");
		mIPInfoMap.put("IP", "192.168.0.177");
		mIPInfoMap.put("NetMask", "255.255.255.0");
		mIPInfoMap.put("GateWay", "192.168.0.2");
		mIPInfoMap.put("DnsAddr", "192.168.0.2");
	}
}
