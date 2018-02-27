package com.bjx.www.http;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class OnlineDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_online_detail);
        getData();
        setAdapter();
        back();
    }

    //获取从主界面传过来的值
    private void getData(){
        Intent intent=getIntent();
        Bundle bundle = intent.getExtras();
        int num = bundle.getInt("Num");
        String orderSn = bundle.getString("orderSn");
        Toast.makeText(getApplicationContext(), num+"    "+orderSn, Toast.LENGTH_SHORT).show();
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
        int view=R.layout.online_order_detail_lv_item;
        ListView lv= (ListView) findViewById(R.id.online_order_detail_goodsList);

        int [] image={R.drawable.icon_goods_cup,R.drawable.icon_goods_cup,R.drawable.icon_goods_cup,R.drawable.icon_goods_cup};
        String [] functions={"北极熊水++","土鸡蛋++","东北大米++","薄荷水++"};

        List<Map<String,Objects>> data=new ArrayList<Map<String, Objects>>();
        for(int i=0;i<image.length;i++)
        {
            Map item=new HashMap();
            item.put("image", image[i]);
            item.put("goodsName", functions[i]);
            data.add(item);
        }

        String [] key={"image","goodsName"};
        int [] items={R.id.online_order_detail_goodsImage,R.id.online_order_detail_goodsName};
        SimpleAdapter sa=new SimpleAdapter(OnlineDetailActivity .this,data,view,key,items);

        lv.setAdapter(sa);

    }

    //按钮回退操作
    private void back(){
        TextView tv= (TextView) findViewById(R.id.online_detail_back);
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

}
