package com.bjx.www.http.service;

import android.app.Service;
import android.content.Intent;
import android.nfc.Tag;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import static android.content.ContentValues.TAG;

/**
 * Created by jie on 2017/12/10.
 */

public class MyService extends Service {
    /***
    *   IBinder是一个借口，代理类（代理服务到界面传递数据）
     *
     */
    public  class MyBinder extends Binder {
            public int getData(){
                return 1;
            }
    }
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new MyBinder();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "-----启动服务----- ");
    }
}
