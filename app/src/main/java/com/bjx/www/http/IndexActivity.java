package com.bjx.www.http;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;    //Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.ab.view.pullview.AbListViewFooter;
import com.ab.view.pullview.AbListViewHeader;
import com.ab.view.pullview.AbPullToRefreshView;
import com.bjx.www.http.common.ACache;
import com.bjx.www.http.jpush.Logger;
import com.bjx.www.http.jpush.MyApplication;
import com.bjx.www.http.service.BroadcastService;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import net.tsz.afinal.FinalActivity;
import net.tsz.afinal.FinalHttp;
import net.tsz.afinal.annotation.view.ViewInject;
import net.tsz.afinal.http.AjaxCallBack;
import net.tsz.afinal.http.AjaxParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.LogRecord;

import cn.jpush.android.api.JPushInterface;

import static android.R.attr.order;
import static com.bjx.www.http.LoginActivity.LOGIN_SAVA;
import static com.bjx.www.http.jpush.MyApplication.getNewOrderIndex;


public class IndexActivity extends AppCompatActivity {

    private String TAG="IndexActivity";
    ACache mCache;                          //轻量级缓存框架
    AbPullToRefreshView mAbPullToRefreshView;
    private int timer = 0;
    private boolean isRunning = true;
    Button   overOrderBtn;


    // 初始化 JPush。如果已经初始化，但没有登录成功，则执行重新登录。
    private void init(){
        JPushInterface.init(getApplicationContext());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        init();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new);
        gotoActivity(IndexActivity.this);
        loadDate();
        pullRefresh();
    }

    //导航条页面跳转
   private   void  gotoActivity(final Context thisPage){

        TextView newOrderPage=(TextView)findViewById(R.id.nav_new);
        TextView onlineOrderPage=(TextView)findViewById(R.id.nav_over);
        TextView indexPage=(TextView)findViewById(R.id.nav_index);
        TextView minePage=(TextView)findViewById(R.id.nav_mine);

      // Log.d("IndexActivity", newOrderContainer+"");
        newOrderPage.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                setContentView(R.layout.activity_new);
                gotoActivity(IndexActivity.this);
                loadDate();
                pullRefresh();
            }
        });
       onlineOrderPage.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                setContentView(R.layout.activity_online);
                gotoActivity(IndexActivity.this);
                overLoadDate();
                overPullRefresh();
            }
        });
        indexPage.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                setContentView(R.layout.activity_index);
                gotoActivity(IndexActivity.this);
                setIndexAdapter();
            }
        });
        minePage.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                setContentView(R.layout.activity_mine);
                gotoActivity(IndexActivity.this);
                setMineAdapter();
                initMine();
                exit();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d("IndexActivity", "结果");
        if(0 == requestCode && resultCode == RESULT_OK){
            //获取数据
            String feedBack = data.getStringExtra("feedBack");
           // Toast.makeText(getApplicationContext(), feedBack, Toast.LENGTH_SHORT).show();
        }
    }

    /**
        *    新单界面操作
         *   马杰
         *   2017.12
         */
    private  SimpleAdapter newOrderSimpleAdapter;
    //新单ListView操作
    private void setNewAdapter(){
        ListView lv= ( ListView) findViewById(R.id.listView_new_order);
        SimpleAdapter sa=getAdapter();
        lv.setAdapter(sa);

        //新单订单详情监听
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent=new Intent(IndexActivity.this,NewDetailActivity.class);
                Bundle bundle=new Bundle();
                bundle.putString("orderID","1");
                intent.putExtras(bundle);
                startActivity(intent);
                //startActivityForResult(intent, 0);   //有返回的意图
            }
        });

    }
    //加载数据
    private void loadDate(){
        mCache = ACache.get(IndexActivity.this);
        JSONArray orders=mCache.getAsJSONArray("new_order_jsonArray");
        //如果缓存存在，加载缓存数据，不存在则更新网络数据
        if(orders!=null)
        {
            setNewAdapter();
        }
        else
        {
            getData();
        }
    }
    //SimpleAdapter适配器加载缓存数据
    private  SimpleAdapter getAdapter(){
        int view=R.layout.new_order_lv_item;
        List<Integer> images=new ArrayList<>();
        List<String> username=new ArrayList<>();
        List<String> telephone=new ArrayList<>();
        List<String> address=new ArrayList<>();
        List<String> orderId=new ArrayList<>();
        mCache = ACache.get(IndexActivity.this);
        JSONArray orders=mCache.getAsJSONArray("new_order_jsonArray");
        int index;
        int listSize;
        if(MyApplication.getNewOrderIndex()<orders.length()/15)
        {
            index=MyApplication.getNewOrderIndex()*15;
            listSize=(MyApplication.getNewOrderIndex()+1)*15;
        }
        else
        {
            index=MyApplication.getNewOrderIndex()*15;
            listSize=orders.length();
        }

            for (int i=index;i< listSize;i++)
            {
                JSONObject  order = orders.optJSONObject(i);
                images.add(R.drawable.icon_user);
                username.add(order.optString("username"));
                telephone.add(order.optString("telephone"));
                address.add(order.optString("address"));
                orderId.add(order.optString("order_id"));
            }
            List<Map<String,Objects>> data=new ArrayList<Map<String, Objects>>();
            for(int i=0;i<images.size();i++)
            {
                Map item=new HashMap();
                item.put("image",images.get(i));
                item.put("username",username.get(i));
                item.put("telephone",telephone.get(i));
                item.put("address", address.get(i));
                item.put("orderId",orderId.get(i));
                data.add(item);
            }

            String [] key={"image","username","telephone","address","orderId"};
            int [] items={R.id.new_order_lv_image,R.id.new_order_lv_nick,R.id.new_order_lv_goods,R.id.new_order_lv_address,R.id.new_order_lv_orderID};

        newOrderSimpleAdapter=new SimpleAdapter(IndexActivity.this,data,view,key,items){
            //重写getView方法，设置Item button监听
              @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                final int p=position;
                final View view=super.getView(position, convertView, parent);
                Button button=(Button)view.findViewById(R.id.new_order_lv_btn);
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        TextView tv= (TextView) view.findViewById(R.id.new_order_lv_orderID);
                        AjaxParams params = new AjaxParams();
                        params.put("orderID",tv.getText().toString().trim());
                        FinalHttp http=new FinalHttp();
                        http.post("http://q170278b07.imwork.net:11399/orders/over",params, new AjaxCallBack<String>() {
                            @Override
                            public void onSuccess(String jsonTextArray) {
                                if(jsonTextArray.toString().trim().equals("1"))
                                {
                                    getData();
                                }
                                else{
                                    Toast.makeText(getApplicationContext(),
                                            jsonTextArray,
                                            Toast.LENGTH_SHORT).show();
                                }

                            }
                            @Override
                            public void onFailure(Throwable t, int errorNo, String strMsg) {
                                super.onFailure(t, errorNo, strMsg);
                                Toast.makeText(getApplicationContext(),
                                        "网络连接失败",
                                        Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });

                return view;
            }
        };
        return newOrderSimpleAdapter;

    }

    //后台获取网络数据
    //下拉刷新
    private void pullRefresh(){
        mAbPullToRefreshView= (AbPullToRefreshView) findViewById(R.id.listView_new_order_container);
        mAbPullToRefreshView.setOnHeaderRefreshListener(new AbPullToRefreshView.OnHeaderRefreshListener() {
            @Override
            public void onHeaderRefresh(AbPullToRefreshView abPullToRefreshView) {
                getData();
                MyApplication.setNewOrderIndex(0);
            }
        });
        mAbPullToRefreshView.setOnFooterLoadListener(new AbPullToRefreshView.OnFooterLoadListener() {
            @Override
            public void onFooterLoad(AbPullToRefreshView abPullToRefreshView) {
                mCache = ACache.get(IndexActivity.this);
                JSONArray orders=mCache.getAsJSONArray("new_order_jsonArray");
                if(orders.length()/15>MyApplication.getNewOrderIndex())
                {
                    MyApplication.setNewOrderIndex(MyApplication.getNewOrderIndex()+1);
                }
                else
                {
                    Toast.makeText(getApplicationContext(),
                            "没有过多数据",
                            Toast.LENGTH_LONG).show();
                }

                Message message = new Message();
                message.obj = timer;
                message.what = 2;                //标识
                handler.sendMessage(message);
            }
        });

    }
    //新单下拉刷新获取数据，并更新到handler
    private void getData(){
                AjaxParams params = new AjaxParams();
                params.put("deliverID","3");
                FinalHttp http=new FinalHttp();
                http.post("http://q170278b07.imwork.net:11399/orders/new",params, new AjaxCallBack<String>() {
                    @Override
                    public void onSuccess(String jsonTextArray) {
                       if(!jsonTextArray.isEmpty())
                       {
                           // 解析json数组
                           JSONArray jsonArray = null;
                           try {
                               jsonArray = new JSONArray(jsonTextArray);
                               mCache = ACache.get(IndexActivity.this);
                               mCache.put("new_order_jsonArray",jsonArray);
                           Message message = new Message();
                           message.obj = timer;
                           message.what = 0;                //标识
                           handler.sendMessage(message);
                           }
                           catch (JSONException e) {
                               e.printStackTrace();
                           }
                       }
                       else {
                           Toast.makeText(getApplicationContext(),
                                   "没有新的订单",
                                   Toast.LENGTH_SHORT).show();
                       }
                    }
                    @Override
                    public void onFailure(Throwable t, int errorNo, String strMsg) {
                        super.onFailure(t, errorNo, strMsg);
                        Toast.makeText(getApplicationContext(),
                                "网络连接失败",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }





    /**
   *   已完成订单
    *   马杰
    *   2017.12
    */
    private  SimpleAdapter overOrderSimpleAdapter;
    //完成订单ListView操作
    private void setOnlineAdapter(){
        ListView lv= (ListView) findViewById(R.id.listView_online_order);
        SimpleAdapter sa=getOverAdapter();
        lv.setAdapter(sa);
        //完成订单详情监听
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TextView tv=(TextView)view.findViewById(R.id.online_order_lv_orderID);
                Intent intent=new Intent(IndexActivity.this,NewDetailActivity.class);
                Bundle bundle=new Bundle();
                bundle.putString("orderID",tv.getText().toString().trim());
                intent.putExtras(bundle);
                startActivity(intent);
                //startActivityForResult(intent, 0);   //有返回的意图
            }
        });

    }
    //完成订单加载数据
    private void overLoadDate(){
        mCache = ACache.get(IndexActivity.this);
        JSONArray orders=mCache.getAsJSONArray("over_order_jsonArray");
        //如果缓存存在，加载缓存数据，不存在则更新网络数据
        if(orders!=null)
        {
            setOnlineAdapter();
        }
        else
        {
            getOverData();
        }
    }
    //SimpleAdapter适配器加载缓存数据
    private  SimpleAdapter getOverAdapter(){
        int view=R.layout.online_order_lv_item;
        List<Integer> images=new ArrayList<>();
        List<String> username=new ArrayList<>();
        List<String> telephone=new ArrayList<>();
        List<String> address=new ArrayList<>();
        List<String> orderId=new ArrayList<>();
        mCache = ACache.get(IndexActivity.this);
        JSONArray orders=mCache.getAsJSONArray("over_order_jsonArray");
        int index;
        int listSize;
        if(MyApplication.getOverOrderIndex()<orders.length()/15)
        {
            index=MyApplication.getOverOrderIndex()*15;
            listSize=(MyApplication.getOverOrderIndex()+1)*15;
        }
        else
        {
            index=MyApplication.getOverOrderIndex()*15;
            listSize=orders.length();
        }
        for (int i= index;i<listSize;i++)
        {
            JSONObject  order = orders.optJSONObject(i);
            images.add(R.drawable.icon_user);
            username.add(order.optString("username"));
            telephone.add(order.optString("telephone"));
            address.add(order.optString("address"));
            orderId.add(order.optString("order_id"));
        }
        List<Map<String,Objects>> data=new ArrayList<Map<String, Objects>>();
        for(int i=0;i<images.size();i++)
        {
            Map item=new HashMap();
            item.put("image",images.get(i));
            item.put("username",username.get(i));
            item.put("telephone",telephone.get(i));
            item.put("address", address.get(i));
            item.put("orderId",orderId.get(i));
            data.add(item);
        }

        String [] key={"image","username","telephone","address","orderId"};
        int [] items={R.id.online_order_lv_image,R.id.online_order_lv_nick,R.id.online_order_lv_goods,R.id.online_order_lv_address,R.id.online_order_lv_orderID};

        overOrderSimpleAdapter=new SimpleAdapter(IndexActivity.this,data,view,key,items);
        return overOrderSimpleAdapter;

    }
    //完成订单后台获取网络数据
    //完成订单下拉上拉刷新
    private void overPullRefresh(){
        mAbPullToRefreshView= (AbPullToRefreshView) findViewById(R.id.listView_online_order_container);
        mAbPullToRefreshView.setOnHeaderRefreshListener(new AbPullToRefreshView.OnHeaderRefreshListener() {
            @Override
            public void onHeaderRefresh(AbPullToRefreshView abPullToRefreshView) {
                getOverData();
                MyApplication.setOverOrderIndex(0);

            }
        });
        mAbPullToRefreshView.setOnFooterLoadListener(new AbPullToRefreshView.OnFooterLoadListener() {
            @Override
            public void onFooterLoad(AbPullToRefreshView abPullToRefreshView) {
                mCache = ACache.get(IndexActivity.this);
                JSONArray orders=mCache.getAsJSONArray("over_order_jsonArray");
                if(orders.length()/15>MyApplication.getOverOrderIndex())
                {
                    MyApplication.setOverOrderIndex(MyApplication.getOverOrderIndex()+1);
                }
                else
                {
                    Toast.makeText(getApplicationContext(),
                            "没有过多数据",
                            Toast.LENGTH_LONG).show();
                }

                Message message = new Message();
                message.obj = timer;
                message.what = 3;                //标识
                handler.sendMessage(message);
            }
        });

    }
    //完成订单下拉刷新获取数据，并更新到handler
    private void getOverData(){
        AjaxParams params = new AjaxParams();
        params.put("deliverID","3");
        FinalHttp http=new FinalHttp();
        http.post("http://q170278b07.imwork.net:11399/orders/complete",params, new AjaxCallBack<String>() {
            @Override
            public void onSuccess(String jsonTextArray) {
                if(!jsonTextArray.isEmpty())
                {
                    // 解析json数组
                    JSONArray jsonArray = null;
                    try {
                        jsonArray = new JSONArray(jsonTextArray);
                        mCache = ACache.get(IndexActivity.this);
                        mCache.put("over_order_jsonArray",jsonArray);
                        Message message = new Message();
                        message.obj = timer;
                        message.what = 1;                //标识
                        handler.sendMessage(message);
                    }
                    catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                else {
                    Toast.makeText(getApplicationContext(),
                            "没有新的订单",
                            Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Throwable t, int errorNo, String strMsg) {
                super.onFailure(t, errorNo, strMsg);
                Toast.makeText(getApplicationContext(),
                        "网络连接失败",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }


    /**
    *   个人中心
   *   马杰
   *   2017.12
   */
    //个人中心ListView
    private void setMineAdapter(){
        //新单界面操作
        int view=R.layout.mine_lv_item;
        ListView lv= (ListView) findViewById(R.id.listView_mine);

        int [] image={R.drawable.icon_user,R.drawable.icon_user};
        String [] functions={"资料修改","关于"};

        List<Map<String,Objects>> data=new ArrayList<Map<String, Objects>>();
        for(int i=0;i<image.length;i++)
        {
            Map item=new HashMap();
            item.put("image", image[i]);
            item.put("functions", functions[i]);
            data.add(item);
        }

        String [] key={"image","functions"};
        int [] items={R.id.mine_lv_image,R.id.mine_lv_function};
        SimpleAdapter sa=new SimpleAdapter(IndexActivity.this,data,view,key,items);
        lv.setAdapter(sa);

    }
    //注销登录监听
    private  void exit(){
        Button btn=(Button)findViewById(R.id.mine_logout_btn);
        btn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                SharedPreferences sp = getSharedPreferences("login", Activity.MODE_PRIVATE);
                String userName=sp.getString("userName","没有值");
                SharedPreferences.Editor editor=sp.edit();
                editor.clear();
                editor.commit();
                Intent intent = new Intent(IndexActivity.this,LoginActivity.class);
                intent.putExtra("userName",userName);
                startActivity(intent);
            }
        });
    }


    /**
    *   主页
     *   马杰
     *   2017.12
     */
    //主页ListView
    private  void setIndexAdapter(){
        //新单界面操作
        int view=R.layout.index_lv_item;
        ListView lv= (ListView) findViewById(R.id.listView_index);

        int [] image={R.drawable.icon_user,R.drawable.icon_user,R.drawable.icon_user,R.drawable.icon_user,R.drawable.icon_user};
        String [] functions={"今日代收","今日配送","本月配送","历史明细","客户评分"};

        List<Map<String,Objects>> data=new ArrayList<Map<String, Objects>>();
        for(int i=0;i<image.length;i++)
        {
            Map item=new HashMap();
            item.put("image", image[i]);
            item.put("functions", functions[i]);
            data.add(item);
        }

        String [] key={"image","functions"};
        int [] items={R.id.index_lv_image,R.id.index_lv_function};
        SimpleAdapter sa=new SimpleAdapter(IndexActivity.this,data,view,key,items);

        lv.setAdapter(sa);

    }
    //获取派送员名称和号码显示出来
    private void initMine(){
        TextView userName= (TextView) findViewById(R.id.mine_deliver_info_name);
        TextView menDian= (TextView) findViewById(R.id.mine_deliver_info_shop);
        TextView tel= (TextView) findViewById(R.id.mine_deliver_info_tel);
        SharedPreferences share = getSharedPreferences("login", Activity.MODE_PRIVATE);
        userName.setText(share.getString("realName", ""));
        menDian.setText(share.getString("shopName", ""));
        tel.setText(share.getString("telephone", ""));
    }



    //UI界面更新
    private Handler handler = new Handler()
    {
        @Override
        public void handleMessage(Message msg)
        {
            switch (msg.what)
            {
                case 0://新单刷新
                    setNewAdapter();
                    mAbPullToRefreshView.onHeaderRefreshFinish();
                    break;
                case 1://已完成订单
                    setOnlineAdapter();
                    mAbPullToRefreshView.onHeaderRefreshFinish();
                    break;
                case 2://新单上拉刷新
                    setNewAdapter();
                    mAbPullToRefreshView.onFooterLoadFinish();
                    break;
                case 3:
                    //完成订单上拉刷新
                    setOnlineAdapter();
                    mAbPullToRefreshView.onFooterLoadFinish();
                    break;
                case 4:

                    break;
            }
        }
    };


}
