package com.niotong.jnitest;

public class getJniString {
	private native static String getStringCPP();
	private native static String getStringJave();
	public static String getScpp(){
		return getStringCPP();
	}
	public static String getSj(){
		return getStringJave();
	}
}
