package com.czz.timercord.timerecord;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Environment;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.czz.timercord.timerecord.util.FileUtils;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class RecordService extends Service {

    public static boolean isRecoiding = false;
    private Timer timer;
    private RecordTask recordTask;

    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private String filename = null;
    private File recordFile;
    NotificationManager mNotificationManager;
    //Notification notification;

    public RecordService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();

        Log.e("CZZ", "onCreate");

        //注册电量广播
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_BATTERY_CHANGED);
        registerReceiver(mBatInfoReceiver, filter);

        acquireWakeLock(this);
        showNotification();
        //startForeground(10101,notification);

        isRecoiding = true;
        if(timer == null){
            timer = new Timer();
            recordTask = new RecordTask();
            timer.schedule(recordTask,1000,1000 * 60 * 3);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e("CZZ", "onDestroy");
        unregisterReceiver(mBatInfoReceiver);

        releaseWakeLock();
        //stopForeground(true);

        isRecoiding = false;
        cancelNotification();
        filename = null;
        if(timer != null){
            timer.cancel();
            timer = null;
            recordTask.cancel();
            recordTask = null;
        }
    }

    private class RecordTask extends TimerTask{

        @Override
        public void run() {
            String str_time = dateFormat.format(new Date());

            if(filename == null){
                File dir = new File(getSDPath()+File.separator+"recordTime");
                if(!dir.exists()){
                    dir.mkdir();
                }
                filename = str_time + ".txt";
                recordFile = new File(dir,filename);
                if(!recordFile.exists()){
                    try {
                        recordFile.createNewFile();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                Log.e("CZZ", "recordFile -- " + recordFile.getAbsolutePath());
            }
            String record = str_time + " 电量："+BatteryC;
            boolean iswrite =  FileUtils.writeFileFromString(recordFile,record+"\r\n",true);
            Log.e("CZZ","iswrite -- "+iswrite);

            Log.e("CZZ", record);
            if(MainActivity.myHandler != null){
                MainActivity.addItem(record);
                MainActivity.myHandler.sendEmptyMessage(10086);
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public static int BatteryC,BatteryV,BatteryT;
    public static String BatteryStatus;

    /* 创建广播接收器 */
    private BroadcastReceiver mBatInfoReceiver = new BroadcastReceiver()   //anjb add
    {
        public void onReceive(Context context, Intent intent)
        {
            String action = intent.getAction();
            /*
             * 如果捕捉到的action是ACTION_BATTERY_CHANGED， 就运行onBatteryInfoReceiver()
             */
            if (Intent.ACTION_BATTERY_CHANGED.equals(action))
            {
                BatteryC = intent.getIntExtra("level", 100);    //目前电量
                BatteryV = intent.getIntExtra("voltage", 4350);  //电池电压
                BatteryT = intent.getIntExtra("temperature", 340);  //电池温度
                switch (intent.getIntExtra("status", BatteryManager.BATTERY_STATUS_UNKNOWN))
                {
                    case BatteryManager.BATTERY_STATUS_CHARGING:
                        BatteryStatus = "充电状态";
                        break;
                    case BatteryManager.BATTERY_STATUS_DISCHARGING:
                        BatteryStatus = "放电状态";
                        break;
                    case BatteryManager.BATTERY_STATUS_UNKNOWN:
                        BatteryStatus = "未知道状态";
                        break;
                }

            }
        }
    };


    static  WakeLock mWakeLock;

    //申请设备电源锁
    public static void acquireWakeLock(Context context)
    {
        if (null == mWakeLock)
        {
            PowerManager pm = (PowerManager)context.getSystemService(Context.POWER_SERVICE);
            mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK|PowerManager.ON_AFTER_RELEASE, "WakeLock");
            if (null != mWakeLock)
            {
                mWakeLock.acquire();
            }
        }
    }
    //释放设备电源锁
    public static void releaseWakeLock()
    {
        if (null != mWakeLock)
        {
            mWakeLock.release();
            mWakeLock = null;
        }
    }

    /**
     * 获取SD path
     *
     * @return
     */
    public String getSDPath() {
        File sdDir = null;
        boolean sdCardExist = Environment.getExternalStorageState()
                .equals(Environment.MEDIA_MOUNTED); // 判断sd卡是否存在
        if (sdCardExist) {
            sdDir = Environment.getExternalStorageDirectory();// 获取跟目录
            return sdDir.toString();
        }

        return null;
    }

    private void showNotification(){
        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this);
        mBuilder.setContentTitle("TimeRecord")//设置通知栏标题
                .setContentText("TimeRecord running...")
                .setTicker("TimeRecord running") //通知首次出现在通知栏，带上升动画效果的
                .setWhen(System.currentTimeMillis())//通知产生的时间，会在通知信息里显示，一般是系统获取到的时间
                .setAutoCancel(false)//设置这个标志当用户单击面板就可以让通知将自动取消
                .setOngoing(false)//ture，设置他为一个正在进行的通知。他们通常是用来表示一个后台任务,用户积极参与(如播放音乐)或以某种方式正在等待,因此占用设备(如一个文件下载,同步操作,主动网络连接)
                .setSmallIcon(R.mipmap.ic_launcher);//设置通知小ICON
        mNotificationManager.notify(10101,mBuilder.build());
        //notification = mBuilder.build();
    }

    private void cancelNotification(){
        mNotificationManager.cancel(10101);
    }
}
