package com.bjx.www.http.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.NotificationCompat;
import android.view.View;

import com.bjx.www.http.IndexActivity;
import com.bjx.www.http.LoginActivity;
import com.bjx.www.http.R;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.concurrent.ExecutorService;

import static android.content.Context.NOTIFICATION_SERVICE;

/**
 * Created by jie on 2017/12/11.
 */

public class BroadcastService extends Service{
    // 线程池
    // 为了方便展示,此处直接采用线程池进行线程管理,而没有一个个开线程
    private ExecutorService mThreadPool;
    private Socket mSocket;
    // 输入流对象
    InputStream is;
    // 输入流读取器对象
    InputStreamReader isr ;
    BufferedReader br ;
    // 接收服务器发送过来的消息
    String response;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        new Thread(new Runnable() {
            @Override
            public void run() {
                // TODO Auto-generated method stub
                try {
                    sendBroadcast();
                    Thread.sleep(3000);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();

        // 利用线程池直接开启一个线程 & 执行该线程
        mThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                try {

                    // 创建Socket对象 & 指定服务端的IP 及 端口号
                    mSocket = new Socket("192.168.1.172", 8989);

                    // 判断客户端和服务器是否连接成功
                    System.out.println(mSocket.isConnected());

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        // 利用线程池直接开启一个线程 & 执行该线程
        mThreadPool.execute(new Runnable() {
            @Override
            public void run() {

                try {
                    // 步骤1：创建输入流对象InputStream
                    is = mSocket.getInputStream();

                    // 步骤2：创建输入流读取器对象 并传入输入流对象
                    // 该对象作用：获取服务器返回的数据
                    isr = new InputStreamReader(is);
                    br = new BufferedReader(isr);

                    // 步骤3：通过输入流读取器对象 接收服务器发送过来的数据
                    response = br.readLine();

                    // 步骤4:发通知
                    sendBroadcast();

                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        });

    }

    public void sendBroadcast(){
        Bitmap btm = BitmapFactory.decodeResource(getResources(), R.drawable.icon_run_man);
        //获取状态通知栏管理
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        //实例化通知栏构造器
        NotificationCompat.Builder mBuilder = (NotificationCompat.Builder) new NotificationCompat.Builder(this)
                .setContentTitle("北极熊电子商务")
                .setContentText("你有新的订单，请注意查收")
                .setSmallIcon(R.drawable.icon_run_man)
                .setTicker("New Message")
                //振动
                .setVibrate(new long[] {0,1000,1000,1000})
                //优先级
                .setPriority(Notification.PRIORITY_MAX)
                //获取默认铃声
                .setDefaults(Notification.DEFAULT_SOUND)
                //获取自定义铃声
                // setSound(Uri.parse("file:///sdcard/xx/xx.mp3"))
                //获取Android多媒体库内的铃声
                //setSound(Uri.withAppendedPath(Audio.Media.INTERNAL_CONTENT_URI, "5"))
                //.setNumber(number) //设置通知集合的数量
                .setNumber(12)
                .setAutoCancel(true) //设置这个标志当用户单击面板就可以让通知将自动取消
                .setLargeIcon(btm);
        // 构建一个Intent
        Intent intent = new Intent();
        intent.setAction("android.bjx.newOrder");
        // 封装一个PendingIntent
        PendingIntent pi = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        mBuilder.setContentIntent(pi);
        notificationManager.notify(1, mBuilder.build());
    }

}
