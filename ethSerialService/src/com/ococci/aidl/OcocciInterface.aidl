package com.ococci.aidl;

interface OcocciInterface{
	int usbFunc(int usbid,int type,boolean on);
	int isAlive();
	void ledSwitch(boolean on);
	int cmdSwitch(boolean on);
	int powerCTL(int num,boolean on);
}