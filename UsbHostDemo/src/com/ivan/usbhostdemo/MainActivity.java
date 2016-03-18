package com.ivan.usbhostdemo;

import java.util.HashMap;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.widget.TextView;

public class MainActivity extends Activity {
	private static final String TAG = "niotong";
	private static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";

	private UsbManager myUsbManager;
	private UsbDevice myUsbDevice;
	private UsbInterface myInterface;
	private UsbDeviceConnection myDeviceConnection;
	PendingIntent mPermissionIntent;
	IntentFilter filter;

	private final int VendorID = 5325;
	private final int ProductID = 4626;

	private TextView info;

	private UsbEndpoint epOut;
	private UsbEndpoint epIn;
	
	byte [] writebyte =  {0x00};
	byte [] readbyte =  new   byte [ 128 ];
	

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		info = (TextView) findViewById(R.id.info);

		myUsbManager = (UsbManager) getSystemService(USB_SERVICE);
		
		
		mPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);
		filter = new IntentFilter(ACTION_USB_PERMISSION);
		
		registerReceiver(mUsbReceiver, filter);
		Log.d(TAG,"writebyte=="+writebyte.toString());
		Log.d(TAG,"readbyte="+writebyte.toString());

		enumerateDevice();

		findInterface();

		openDevice();

		assignEndpoint();
		
		readDevice();
		
		writeDevice();
		
		readDevice();
		Log.d(TAG,myUsbDevice.toString());
	}

	
	private void writeDevice() {
		// TODO Auto-generated method stub
		 int  retwrite = myDeviceConnection.bulkTransfer(epOut, writebyte, writebyte.length,  3000 );  
		 Log.d(TAG,"retwrite="+retwrite);
		 Log.d(TAG,"writebyte="+writebyte.toString());
		   
	}


	private void readDevice() {
		// TODO Auto-generated method stub
		int  retread = myDeviceConnection.bulkTransfer(epIn, readbyte, readbyte.length,  3000 );
		Log.d(TAG,"retread="+retread);
		Log.d(TAG,"readbyte="+readbyte.toString());
	}


	private void assignEndpoint() {
		Log.d(TAG,"myInterface.getEndpointCount()="+myInterface.getEndpointCount());
		if (myInterface.getEndpoint(1) != null) {
			Log.d("niotong","getEndpoint(1)");
			epOut = myInterface.getEndpoint(1);
			if(epOut.getDirection() == UsbConstants.USB_DIR_IN){
				Log.d(TAG,"epOut.getDirection() == UsbConstants.USB_DIR_IN");
			}
			if(epOut.getDirection() == UsbConstants.USB_DIR_OUT){
				Log.d(TAG,"epOut.getDirection() == UsbConstants.USB_DIR_OUT");
			}
			if (epOut.getType() == UsbConstants.USB_ENDPOINT_XFER_BULK){
				Log.d(TAG,"epOut.getType() == UsbConstants.USB_ENDPOINT_XFER_BULK");
			}
		}
		if (myInterface.getEndpoint(0) != null) {
			Log.d("niotong","getEndpoint(0)");
			epIn = myInterface.getEndpoint(0);
			if(epIn.getDirection() == UsbConstants.USB_DIR_IN){
				Log.d(TAG,"epIn.getDirection() == UsbConstants.USB_DIR_IN");
			}
			if(epIn.getDirection() == UsbConstants.USB_DIR_OUT){
				Log.d(TAG,"epIn.getDirection() == UsbConstants.USB_DIR_OUT");
			}
			if (epOut.getType() == UsbConstants.USB_ENDPOINT_XFER_BULK){
				Log.d(TAG,"epIn.getType() == UsbConstants.USB_ENDPOINT_XFER_BULK");
			}
		}

		Log.d(TAG, getString(R.string.text));
	}


	private void openDevice() {
		if (myInterface != null) {
			Log.d(TAG,"mark1");
			UsbDeviceConnection conn = null;
			myUsbManager.requestPermission(myUsbDevice, mPermissionIntent);
			
			if (myUsbManager.hasPermission(myUsbDevice)) {
				Log.d(TAG,"mark2");
				conn = myUsbManager.openDevice(myUsbDevice);
			}

			if (conn == null) {
				Log.d(TAG,"mark3");
				return;
			}

			if (conn.claimInterface(myInterface, true)) {
				Log.d(TAG,"mark4");
				myDeviceConnection = conn;
				Log.d(TAG, "openDevice");
			} else {
				Log.d(TAG,"mark5");
				conn.close();
			}
		}
	}


	private void findInterface() {
		if (myUsbDevice != null) {
			Log.d(TAG, "interfaceCounts : " + myUsbDevice.getInterfaceCount());
			for (int i = 0; i < myUsbDevice.getInterfaceCount(); i++) {
				UsbInterface intf = myUsbDevice.getInterface(i);
				Log.d(TAG,"getInterfaceClass="+intf.getInterfaceClass());
				Log.d(TAG,"getInterfaceSubclass="+intf.getInterfaceSubclass());
				Log.d(TAG,"getInterfaceProtocol="+intf.getInterfaceProtocol());

				if (intf.getInterfaceClass() == 8
						&& intf.getInterfaceSubclass() == 6
						&& intf.getInterfaceProtocol() == 80) {
					myInterface = intf;
				}
				break;
			}
		}
	}


	private void enumerateDevice() {
		if (myUsbManager == null)
			return;

		HashMap<String, UsbDevice> deviceList = myUsbManager.getDeviceList();
		Log.d(TAG,"deviceList size ="+deviceList.size());
		if (!deviceList.isEmpty()) { // deviceList��Ϊ��
			StringBuffer sb = new StringBuffer();
			for (UsbDevice device : deviceList.values()) {
				sb.append(device.toString());
				sb.append("\n");
				info.setText(sb);
				
				Log.d(TAG, "DeviceInfo: " + device.getVendorId() + " , "
						+ device.getProductId());

				if (device.getVendorId() == VendorID
						&& device.getProductId() == ProductID) {
					myUsbDevice = device;
					Log.d(TAG,myUsbDevice.toString());
				}
			}
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

		private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {

		    public void onReceive(Context context, Intent intent) {
		        String action = intent.getAction();
		        if (ACTION_USB_PERMISSION.equals(action)) {
		            synchronized (this) {
		                UsbDevice device = (UsbDevice)intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);

		                if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
		                    if(device != null){
		                      //call method to set up device communication
		                   }
		                } 
		                else {
		                    Log.d(TAG, "permission denied for device " + device);
		                }
		            }
		        }
		    }
		};
}
