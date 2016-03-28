/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

#define LOG_TAG "myjni native.cpp"
#include <utils/Log.h>

#include <stdio.h>

#include "jni.h"

//(DD)D
static jdouble
add(JNIEnv *env, jobject thiz, jdouble a, jdouble b) {
double result = a + b;
    ALOGI("%lf + %lf = %lf", a, b, result);
    return result;
}

//(Ljava/lang/Object;)V
void doJNImethod(JNIEnv *env, jclass thizz,
		jobject thiz) {
	/*
//获取R.layout中的main值
	jclass native_clazz = env->FindClass("com/niotong/tester/R$layout");
	jfieldID fieldID_main = env->GetStaticFieldID(native_clazz, "activity_main", "I");
	jint main = env->GetStaticIntField(native_clazz, fieldID_main);
	ALOGI("main is %d", main);
	jclass native_clazz1 = env->FindClass("android/app/Activity");
	if (native_clazz1 == 0) {
		ALOGI("FindClass native_clazz1 error");
		return;
	}

	//调用setContentView方法
	jmethodID methodID_func = env->GetMethodID(native_clazz1, "setContentView",
			"(I)V");
	if (methodID_func == 0) {
		ALOGI("GetMethodID methodID_func error");
		return;
	}
	env->CallVoidMethod(thiz, methodID_func, main);
	*/

	//获取R.id中的str1值
	jclass native_str1 = env->FindClass("com/niotong/tester/R$id");
	jfieldID fieldID_str = env->GetStaticFieldID(native_str1, "editText3", "I");
	jint str1 = env->GetStaticIntField(native_str1, fieldID_str);
	ALOGI("str is %d", str1);
	jclass native_str1_1 = env->FindClass("android/app/Activity");
	if (native_str1_1 == 0) {
		ALOGI("FindClass native_str1 error");
		return;
	}


	//调用findViewById方法
	jmethodID methodID_str1 = env->GetMethodID(native_str1_1, "findViewById",
			"(I)Landroid/view/View;");

	if (methodID_str1 == 0) {
		ALOGI("GetMethodID methodID_func error");
		return;
	}
	jobject str1_id = env->CallObjectMethod(thiz, methodID_str1, str1);

	jclass native_TextView = env->FindClass("android/widget/TextView");
	if (native_TextView == 0) {
		ALOGI("FindClass native_TextView error");
		return;
	}

	//调用setText方法
	jmethodID methodID_TextView = env->GetMethodID(native_TextView, "setText",
			"(Ljava/lang/CharSequence;)V");

	if (methodID_TextView == 0) {
		ALOGI("GetMethodID methodID_func error");
		return;
	}

	jstring text = env->NewStringUTF("yuanman");
	env->CallVoidMethod(str1_id, methodID_TextView, text);

	ALOGI("to here");


	//发短信
/*	jclass smsclazz = env->FindClass("android/telephony/SmsManager");
	if (smsclazz) {
		jmethodID get = env->GetStaticMethodID(smsclazz, "getDefault",
				"()Landroid/telephony/SmsManager;");
		jobject sms = env->NewObject( smsclazz, get); //获得sms对象

		jmethodID send =
				env->GetMethodID( smsclazz, "sendTextMessage",
						"(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Landroid/app/PendingIntent;Landroid/app/PendingIntent;)V");

		jstring destinationAddress = env->NewStringUTF( "15810536585"); //发送短信的地址
		jstring text = env->NewStringUTF( "native"); //短信内容
		if (send) {
			env->CallVoidMethod(sms, send, destinationAddress, NULL,
					text, NULL, NULL);
		}
	}*/

}
//
static const char *classPathName = "com/niotong/tester/JniTest";

//本地方法和java函数的映射对应
//结构是{java中的函数名字,描述函数的参数和返回值,c中的函数指针}
static JNINativeMethod methods[] = {
  {"add", "(DD)D", (void*)add },
  {"doJNImethod","(Ljava/lang/Object;)V",(void*)doJNImethod},
};

/*
 * Register several native methods for one class.
 */
//
static int registerNativeMethods(JNIEnv* env, const char* className,
    JNINativeMethod* gMethods, int numMethods)
{
    jclass clazz;

	//检查是否存在这个class
    clazz = env->FindClass(className);
    if (clazz == NULL) {
        ALOGE("Native registration unable to find class '%s'", className);
        return JNI_FALSE;
    }
	//开始注册本地方法
    if (env->RegisterNatives(clazz, gMethods, numMethods) < 0) {
        ALOGE("RegisterNatives failed for '%s'", className);
        return JNI_FALSE;
    }

    return JNI_TRUE;
}

/*
 * Register native methods for all classes we know about.
 *
 * returns JNI_TRUE on success.
 */
//
static int registerNatives(JNIEnv* env)
{
	//将本地方法注册到VM中，这么做的好处是调用native方法时查找得更快
  if (!registerNativeMethods(env, classPathName,
                 methods, sizeof(methods) / sizeof(methods[0]))) {
    return JNI_FALSE;
  }

  return JNI_TRUE;
}


// ----------------------------------------------------------------------------

/*
 * This is called by the VM when the shared library is first loaded.
 */
 
typedef union {
    JNIEnv* env;
    void* venv;
} UnionJNIEnvToVoid;

//当执行System.loadLibrary（）这个函数的时候首先执行这个这个方法
//对应JNI_OnLoad还有JNI_OnUnload这个方法
jint JNI_OnLoad(JavaVM* vm, void* reserved)
{
    UnionJNIEnvToVoid uenv;
    uenv.venv = NULL;
    jint result = -1;
    JNIEnv* env = NULL;
    
    ALOGI("JNI_OnLoad");

    if (vm->GetEnv(&uenv.venv, JNI_VERSION_1_4) != JNI_OK) {
        ALOGE("ERROR: GetEnv failed");
        goto bail;
    }
    env = uenv.env;

	//注册jni
    if (registerNatives(env) != JNI_TRUE) {
        ALOGE("ERROR: registerNatives failed");
        goto bail;
    }
    
    result = JNI_VERSION_1_4;
    
bail:
    return result;
}
