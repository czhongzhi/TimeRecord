package com.czz.timercord.timerecord;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    private ListView listView;
    private TextView record_stauts;
    private static MyAdapter adapter;
    private Context context;
    private static List<String> data = new ArrayList<>();

    DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

    public static MyHandler myHandler = null;

    class MyHandler extends Handler{
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 10086:
                    adapter.notifyDataSetChanged();
                    break;
            }
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        myHandler = new MyHandler();
        context = MainActivity.this;
        initView();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        myHandler =  null;
    }

    private void initView() {
        findViewById(R.id.btn_stop).setOnClickListener(this);
        findViewById(R.id.btn_start).setOnClickListener(this);
        listView = (ListView) findViewById(R.id.listView);
        record_stauts = (TextView) findViewById(R.id.record_stauts);

        adapter = new MyAdapter(data);
        listView.setAdapter(adapter);

        if(RecordService.isRecoiding){
            record_stauts.setText("启动");
        }
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_stop:
                Log.e("CZZ", "btn_stop isRecoiding "+RecordService.isRecoiding);
                if(RecordService.isRecoiding){
                    Intent stop = new Intent(context,RecordService.class);
                    stopService(stop);
                    record_stauts.setText("停止");
                }

                break;
            case R.id.btn_start:
                Log.e("CZZ", "btn_start isRecoiding "+RecordService.isRecoiding);
                if(!RecordService.isRecoiding){
                    data.clear();
                    Intent start = new Intent(context,RecordService.class);
                    startService(start);
                    record_stauts.setText("启动");
                }

                break;
        }
    }

    public static void addItem(String str){
        data.add(str);
    }

    private class MyAdapter extends BaseAdapter{
        private List<String> data;

        public MyAdapter(List<String> data){
            this.data = data;
        }

        @Override
        public int getCount() {
            return data == null ? 0 : data.size();
        }

        @Override
        public Object getItem(int position) {
            return data.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View view, ViewGroup parent) {
            ViewHolder holder = null;
            if(view == null){
                view = LayoutInflater.from(context).inflate(R.layout.item_string,null);
                holder = new ViewHolder();
                holder.tv = (TextView) view.findViewById(R.id.itme_time);
                view.setTag(holder);
            }else{
                holder = (ViewHolder) view.getTag();
            }

            holder.tv.setText(data.get(position));

            return view;
        }

    }

    private class ViewHolder{
        private TextView tv;
    }
}
