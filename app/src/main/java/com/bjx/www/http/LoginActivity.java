package com.bjx.www.http;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.bjx.www.http.jpush.TagAliasOperatorHelper;
import com.bjx.www.http.network.connect;
import com.bjx.www.http.service.BroadcastService;

import net.tsz.afinal.FinalActivity;
import net.tsz.afinal.FinalHttp;
import net.tsz.afinal.annotation.view.ViewInject;
import net.tsz.afinal.http.AjaxCallBack;
import net.tsz.afinal.http.AjaxParams;

import org.apache.http.cookie.Cookie;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import cn.jpush.android.api.JPushInterface;

import static android.content.ContentValues.TAG;
import static com.bjx.www.http.jpush.TagAliasOperatorHelper.sequence;
import static com.bjx.www.http.jpush.TagAliasOperatorHelper.TagAliasBean;

public class LoginActivity extends FinalActivity {
    // 线程池
    // 为了方便展示,此处直接采用线程池进行线程管理,而没有一个个开线程
    private ExecutorService mThreadPool= Executors.newCachedThreadPool();
    private Handler handler = new Handler()
    {
        @Override
        public void handleMessage(Message msg)
        {
            switch (msg.what)
            {
                case 0:
                    Intent intent = new Intent(LoginActivity.this,IndexActivity.class);
                    startActivity(intent);
                    break;
                case 1:
                    Toast.makeText(getApplicationContext(), "网络未连接", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };
    //cookie变量
    private SharedPreferences  share =null;
    private SharedPreferences.Editor getEditor=null;
    private ConstraintLayout main=null;                 //布局文件的ID
    public static final String LOGIN_SAVA="login";  //保存配送员密码信息的xml文件名

    //Afinal框架的标注注解功能
    @ViewInject(id=R.id.Btn_login,click="LoginReq")
    Button login;
    @ViewInject(id=R.id.UserName)
    TextView userName;
    @ViewInject(id=R.id.PassWord)
    TextView passWord;

    //登录操作
    public void LoginReq(View v)  {
                AjaxParams params = new AjaxParams();
                params.put("username", userName.getText().toString().trim());
                params.put("password", passWord.getText().toString().trim());
                FinalHttp http=new FinalHttp();

                http.post("http://q170278b07.imwork.net:11399/admins/login",
                        params, new AjaxCallBack<String>() {

                    @Override
                    public void onSuccess(String jsonText) {
                        Log.d(TAG, jsonText);
                        // 解析json对象
                        JSONObject jsonObject = null;
                        if(!jsonText.isEmpty())
                        {
                        try {
                                jsonObject = new JSONObject(jsonText);
                                String username=jsonObject.getString("username");
                                String telephone=jsonObject.getString("telephone");
                                String shopname=jsonObject.getString("shop");
                                String realname=jsonObject.getString("realname");
                                //保存配送员信息
                                share=getSharedPreferences(LOGIN_SAVA,Activity.MODE_PRIVATE);
                                getEditor=share.edit();
                                getEditor.putString("userName", username);
                                getEditor.putString("passWord","暂时为空");
                                getEditor.putString("realName", realname);
                                getEditor.putString("shopName", shopname);
                                getEditor.putString("telephone",telephone);
                                getEditor.commit();

                                //设置标签和别名
                                setAlias(telephone);
                                Set<String> tags= new HashSet<String>();
                                tags.add("delivery");
                                setTags(tags);
                                //通知 主线程登录成功
                                Message message = new Message();
                                message.what = 0;                //标识:登录成功
                                handler.sendMessage(message);
                            }
                        catch (JSONException e) {
                            e.printStackTrace();
                         }
                        }
                         else {
                            //密码或用户名不正确
                            Toast.makeText(getApplicationContext(),
                                    "密码或用户名不正确",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Throwable t, int errorNo, String strMsg) {
                        super.onFailure(t, errorNo, strMsg);
                        //通知 主线程登录失败
                        Message message = new Message();
                        message.what = 1;                //标识:登录失败
                        handler.sendMessage(message);
                    }
                });
            }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        JPushInterface.setDebugMode(true);//正式版的时候设置false，关闭调试
        JPushInterface.init(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        InitView();
        AutoLogin();
    }

    /****
          编程思路自然语言描述：：
          1.首次进入界面，判断是否保存有密码和用户名，
          2.有则直接跳到主页，没有就进入登录界面
          3.登录向后台发送请求，判断用户名密码是否正确，正确后台返回门店信息，已经配送员信息
          4.将配送员基本信息保存到SharedPreferences新建的XML文件（login.xml）
       *
       */
    private void  AutoLogin(){
        share = this.getSharedPreferences(LOGIN_SAVA, Activity.MODE_PRIVATE);
        String userName = share.getString("userName", "");
        String passWord = share.getString("passWord","");
        //自动登录
        if(!userName.isEmpty() && !passWord.isEmpty() )
        {
            setAlias(userName);
             Set<String> tags= new HashSet<String>();
            tags.add("delivery");
            setTags(tags);

            new Thread(new Runnable() {
                @Override
                public void run() {
                    // TODO Auto-generated method stub
                    try {
                        main.setVisibility(View.GONE); //隐藏登录界面
                        //Thread.sleep(1000);
                        Intent intent =new Intent(LoginActivity.this,IndexActivity.class);
                        startActivity(intent);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
    }
    //初始化界面
    private void InitView() {
       main= (ConstraintLayout) findViewById(R.id.login_view);
        Intent intent=getIntent();
        //注销后保留userName
        if(intent.hasExtra("userName"))
        {
            String uName = intent.getStringExtra("userName");
            userName.setText(uName.toString().trim());
        }

    }


    //设置别名
    private void setAlias(String alias){
        TagAliasBean tagAliasBean = new TagAliasBean();
        tagAliasBean.action=2;
        tagAliasBean.alias = alias;
        tagAliasBean.tags = new HashSet<>();
        tagAliasBean.isAliasAction = true;
        TagAliasOperatorHelper.getInstance().handleAction(getApplicationContext(),sequence,tagAliasBean);
    }
    //设置标签
    private void setTags(Set<String> tags){
        TagAliasBean tagAliasBean = new TagAliasBean();
        tagAliasBean.action=2;
        tagAliasBean.alias = "";
        tagAliasBean.tags = tags;
        tagAliasBean.isAliasAction = false;
        TagAliasOperatorHelper.getInstance().handleAction(getApplicationContext(),sequence,tagAliasBean);
    }
}
