package com.niotong.tester;

public class JniTest {
	static{
		System.loadLibrary("JniTest");
	}
	private native static double add(double a,double b);
	
	public static double Jadd(double a,double b){
		return add(a,b);
	}

}