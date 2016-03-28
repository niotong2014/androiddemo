package com.niotong.tester;

public class JniTest {
	static{
		System.loadLibrary("JniTest");
	}
	private native static double add(double a,double b);
	private native static void doJNImethod(Object con);
	
	public static double Jadd(double a,double b){
		return add(a,b);
	}

	public static void doJNI(Object con){
		doJNImethod(con);
	}
}
