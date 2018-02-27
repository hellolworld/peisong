package com.bjx.www.http;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.bjx.www.http.common.ACache;

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

import static android.R.attr.y;

public class NewDetailActivity extends AppCompatActivity {

    ACache  mCache;
    private int timer = 0;
    //订单信息
    TextView Address;
    TextView Telphone;
    TextView Username;
    TextView Createtime;
    TextView Appointtime;
    TextView Besides;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_detail);
        getData();
        back();
    }
    //回退操作
    @Override
    public void onBackPressed() {
        Intent intent2 = new Intent();
        intent2.putExtra("feedBack", "我已经收到了你的信息，这是我给你的反馈！");
        setResult(RESULT_OK, intent2); //返还数据
        finish();//销毁当前活动
    }

    //商品信息ListView
    public void setAdapter(){
        int view=R.layout.new_order_detail_lv_item;
        ListView lv= (ListView) findViewById(R.id.new_order_detail_goodsList);
        List<Integer> images=new ArrayList<>();
        List<String> goodsName=new ArrayList<>();
        List<String> number=new ArrayList<>();
        mCache = ACache.get(NewDetailActivity.this);
        JSONObject orderInfo=mCache.getAsJSONObject("new_order_detail_jsonArray");
        mCache.clear();
        if(orderInfo!=null)
        {
            try {
                JSONArray ordergoods=orderInfo.getJSONArray("orderGoods");
                Log.d("NewDetailActivity", ordergoods.toString());
                Log.d("NewDetailActivity","长度："+ ordergoods.length());
                for (int i=0;i<ordergoods.length();i++)
                {
                    JSONObject order = ordergoods.optJSONObject(i);
                    images.add(R.drawable.icon_goods_cup);
                    goodsName.add(order.optString("goods_name"));
                    number.add(order.optString("goods_num"));
                }
                List<Map<String,Objects>> data=new ArrayList<Map<String, Objects>>();
                for(int i=0;i<images.size();i++)
                {
                    Map item=new HashMap();
                    item.put("image",images.get(i));
                    item.put("goodsName",goodsName.get(i));
                    item.put("number", number.get(i));
                    data.add(item);
                }
                String [] key={"image","goodsName","number"};
                int [] items={R.id.new_order_detail_goodsImage,R.id.new_order_detail_goodsName,R.id.new_order_detail_goodsNum};
                SimpleAdapter sa=new SimpleAdapter(NewDetailActivity.this,data,view,key,items);
                lv.setAdapter(sa);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        else {
            Toast.makeText(getApplicationContext(),
                    "订单不存在",
                    Toast.LENGTH_SHORT).show();
        }



    }

    public void loadData(){
        mCache = ACache.get(NewDetailActivity.this);
        JSONObject orderInfo=mCache.getAsJSONObject("new_order_detail_jsonArray");
        //设置订单信息
        Address= (TextView) findViewById(R.id.new_order_detail_address);
        Telphone= (TextView) findViewById(R.id.new_order_detail_telphone);;
        Username= (TextView) findViewById(R.id.new_order_detail_userName);;
        Createtime= (TextView) findViewById(R.id.new_order_detail_createtime);;
        Appointtime= (TextView) findViewById(R.id.new_order_detail_appointtime);;
        Besides= (TextView) findViewById(R.id.new_order_detail_besides);;
        try {
            JSONObject order=orderInfo.getJSONObject("order");
            Address.append(order.optString("address"));
            Telphone.append(order.optString("telephone"));
            Username.append(order.optString("username"));
            Createtime.append(order.optString("order_time"));
            Appointtime.append(order.optString("order_time"));
            Besides.append(order.optString("besides"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        //商品信息
        setAdapter();

    }


    //获取订单详情
    private void getData() {
        Intent intent=getIntent();
        Bundle bundle = intent.getExtras();
        String orderID = bundle.getString("orderID");
        AjaxParams params = new AjaxParams();
        params.put("orderID", orderID);
        FinalHttp http = new FinalHttp();
        http.post("http://q170278b07.imwork.net:11399/orders/goods", params, new AjaxCallBack<String>() {
            @Override
            public void onSuccess(String jsonTextArray) {
                if (!jsonTextArray.isEmpty()) {
                    // 解析json数组
                    JSONObject jsonArray = null;
                    try {
                        JSONObject jsonObject= new JSONObject(jsonTextArray);
                        mCache = ACache.get(NewDetailActivity.this);
                        mCache.put("new_order_detail_jsonArray", jsonObject);
                        Message message = new Message();
                        message.obj = timer;
                        message.what = 0;                //标识:订单详情
                        mHandler.sendMessage(message);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    Toast.makeText(getApplicationContext(),
                            "订单不存在",
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

    //按钮回退操作
    private void back(){
        TextView tv= (TextView) findViewById(R.id.new_detail_back);
        tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent2 = new Intent();
                intent2.putExtra("feedBack", "我已经收到了你的信息，这是我给你的反馈！");
                setResult(RESULT_OK, intent2); //返还数据
                finish();//销毁当前活动
            }
        });
    }

    //UI界面更新
    private Handler mHandler = new Handler()
    {
        @Override
        public void handleMessage(Message msg)
        {
            switch (msg.what)
            {
                case 0://新单详情
                    loadData();
                    break;
                case 1:

                    break;
            }
        }
    };
}
