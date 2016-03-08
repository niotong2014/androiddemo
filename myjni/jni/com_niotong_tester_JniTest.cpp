/*************************************************************************
	> File Name: com_niotong_tester_JniTest.cpp
	> Author: regan
	> Mail: regan@thtfit.com 
	> Created Time: Tue 08 Mar 2016 02:16:28 PM CST
 ************************************************************************/

#include <jni.h>
#include <string.h>
#include "com_niotong_tester_JniTest.h"

JNIEXPORT jdouble JNICALL Java_com_niotong_tester_JniTest_add(JNIEnv * env, jclass c, jdouble a, jdouble b){
	return a+b;
}
