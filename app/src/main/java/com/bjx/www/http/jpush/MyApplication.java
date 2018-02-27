package com.bjx.www.http.jpush;

import android.app.Application;

import cn.jpush.android.api.JPushInterface;

/**
 * For developer startup JPush SDK
 * 
 * 一般建议在自定义 Application 类里初始化。也可以在主 Activity 里。
 */
public class MyApplication extends Application {
    private static final String TAG = "JIGUANG-Example";


    /*
    *   2018.2.1
     *  马杰
     *  新单和完成listview索引全局变量
     */
    private static int NEW_ORDER_INDEX=0;

    private static int OVER_ORDER_INDEX=0;

    public static int getNewOrderIndex() {
        return NEW_ORDER_INDEX;
    }

    public static void setNewOrderIndex(int newOrderIndex) {
        NEW_ORDER_INDEX = newOrderIndex;
    }

    public static int getOverOrderIndex() {
        return OVER_ORDER_INDEX;
    }

    public static void setOverOrderIndex(int overOrderIndex) {
        OVER_ORDER_INDEX = overOrderIndex;
    }


    @Override
    public void onCreate() {    	     
    	 Logger.d(TAG, "Jpush初始化");
         super.onCreate();

         JPushInterface.setDebugMode(true); 	// 设置开启日志,发布时请关闭日志
         JPushInterface.init(this);     		// 初始化 JPush
    }
}
