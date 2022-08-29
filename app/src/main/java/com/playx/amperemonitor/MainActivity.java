package com.playx.amperemonitor;

import static android.os.BatteryManager.BATTERY_PLUGGED_WIRELESS;
import static android.os.BatteryManager.EXTRA_PLUGGED;
import static android.os.BatteryManager.EXTRA_TEMPERATURE;
import static android.os.BatteryManager.EXTRA_VOLTAGE;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.graphics.drawable.IconCompat;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.text.TextPaint;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {
    String mleve="0";
    Boolean iswirelesscharge=false;
    double retemp=0;
    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final String CHANNEL_ID = "channel_id_1";
        final String CHANNEL_NAME = "channel_name_1";
        final DatagramSocket[] socket = {null};
        final InetAddress[] serverAddress = {null};

CommandExecution mexe=new CommandExecution();
mexe.get_root(this);

//...
        this.registerReceiver(this.mBroadcastReceiver,
                new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        NotificationManager mNotificationManager = (NotificationManager)
                getSystemService(Context.NOTIFICATION_SERVICE);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            //只在Android O之上需要渠道
            NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ID,
                    CHANNEL_NAME, NotificationManager.IMPORTANCE_LOW);
            //如果这里用IMPORTANCE_NOENE就需要在系统的设置里面开启渠道，
            //通知才能正常弹出
            notificationChannel.enableVibration(false);

            mNotificationManager.createNotificationChannel(notificationChannel);

        }
        NotificationCompat.Builder builder= new NotificationCompat.Builder(this,CHANNEL_ID);
        NotificationCompat.Builder builder2= new NotificationCompat.Builder(this,CHANNEL_ID);
        NotificationCompat.Builder builder3= new NotificationCompat.Builder(this,CHANNEL_ID);
        BatteryManager mBatteryManager = (BatteryManager) getSystemService(Context.BATTERY_SERVICE);
        final Boolean[] isruning = {true};
        final Thread[] t1 = {null};
        TextView mtext=findViewById(R.id.textView);
        Button Start=findViewById(R.id.start);
        Switch mswitch=(Switch)findViewById(R.id.Noti);
        EditText medit=findViewById(R.id.editTextTextPersonName);
        EditText mediv=findViewById(R.id.editTextNumber);
        TextView mtext2=findViewById(R.id.textView2);
        TextView mvot=findViewById(R.id.realtimevo);
        if(mediv.getText().length()==0){
            mediv.setText("0");
        }

        Start.setOnClickListener(new View.OnClickListener() {
            @Override

            public void onClick(View v) {
                isruning[0]=true;
                    if(t1[0]==null){
                    t1[0] = new Thread(new Runnable() {
                        public void run() {
                            while(isruning[0]){
                                    mexe.execCommand("cat /sys/class/power_supply/dc/voltage_now",mexe.is_root());
                                String mresult=CommandExecution.CommandResult.successMsg;
                                System.out.println("充电电压"+mresult);
                                String finalMresult = mresult;
                                if(CommandExecution.CommandResult.errorMsg.equals("Permission denied")){
                                    mresult="0";
                                }else{mvot.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        mvot.setText(String.valueOf(Integer.valueOf(finalMresult)/1000000.0)+"V");
                                    }
                                });
                                    String finalMresult1 = mresult;
                                    mediv.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            mediv.setText(String.valueOf(Integer.valueOf(finalMresult1)/1000000.0));
                                        }
                                    });}


                                Long avgCurrent = null, currentNow = null;
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

                                    currentNow = mBatteryManager.getLongProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW);
                                }
                                String TAG="battery";
                                Long finalCurrentNow = currentNow/1000;

                                Bitmap mbit;




                                mbit=cretex(finalCurrentNow.toString(), Color.GREEN);
                                if(mswitch.isChecked()) {
                                    builder
                                            .setSmallIcon(IconCompat.createWithBitmap(mbit))
                                            .setLargeIcon(mbit)
                                            .setContentTitle("电流")
                                            .setContentText(finalCurrentNow.toString())
                                            .setVibrate(new long[]{0});
                                    mNotificationManager.notify(1, builder.build());

                                    Bitmap mbit2=cretex(String.format("%.1f",finalCurrentNow*Float.parseFloat(mediv.getText().toString())/1000), Color.RED);
                                    builder2
                                            .setSmallIcon(IconCompat.createWithBitmap(mbit2))
                                            .setLargeIcon(mbit2)
                                            .setContentTitle("功率")
                                            .setContentText(String.valueOf(finalCurrentNow*Float.parseFloat(mediv.getText().toString())/1000))
                                            .setVibrate(new long[]{0});
                                    mNotificationManager.notify(2, builder2.build());
                                    Bitmap mbit3=cretex(String.format("%.1f",Float.parseFloat(mresult)/1000000.0), Color.YELLOW);
                                    builder3
                                            .setSmallIcon(IconCompat.createWithBitmap(mbit3))
                                            .setLargeIcon(mbit3)
                                            .setContentTitle("电压")
                                            .setContentText(String.format("%.1f",Float.parseFloat(mresult)/1000000.0))
                                            .setVibrate(new long[]{0});
                                    mNotificationManager.notify(3, builder3.build());
                                    try {
                                        Thread.sleep(3000);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                    mNotificationManager.cancel(1);
                                    mNotificationManager.cancel(2);
                                    mNotificationManager.cancel(3);
                                }
                                try {
                                    Thread.sleep(1000);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }

                                if(isValidIPAddress(medit.getText().toString())){
                                try {
                                    //自己的发送端口
                                    socket[0] = new DatagramSocket(35478);  //①
                                    //对方的ip
                                    serverAddress[0] = InetAddress.getByName(medit.getText().toString());  //②
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                String sendData = finalCurrentNow.toString()+"mA    "+String.format("%.2f",Integer.valueOf(finalMresult)/1000000.0)+"V   "+String.format("%.1f",finalCurrentNow*Float.parseFloat(mediv.getText().toString())/1000)+"W"+"   "+mBatteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)+"  "+retemp+"'c";
                                byte data[] = sendData.getBytes();
                                //这里的8888是接收方的端口号
                                DatagramPacket packet = new DatagramPacket(data, data.length, serverAddress[0], 45678);   //③
                                try {
                                    socket[0].send(packet);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                socket[0].close();}
                                Drawable drawable = new BitmapDrawable(mbit);
                                if(mediv.getText().length()!=0){
                                    mtext2.post(new Runnable() {
                                        @Override
                                        public void run() {

                                            mtext2.setText(String.valueOf(finalCurrentNow*Float.parseFloat(mediv.getText().toString())/1000)+"W");
                                        }
                                    });
                                }
                                mtext.post(new Runnable() {

                                    @Override
                                    public void run() {

                                        mtext.setText(finalCurrentNow.toString()+"mA");
                                    }
                                });
                            }
                        }});
                    t1[0].start();}
            }
        });
        Button stop=findViewById(R.id.stop);
        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isruning[0] =false;
                t1[0]=null;
            }
        });
    }
    public Bitmap cretex(String text,int mcolor){
        final TextPaint textPaint = new TextPaint();
        textPaint.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
        textPaint.setTextSize(50);
        textPaint.setSubpixelText(true);
        textPaint.setColor(mcolor);
        textPaint.setTextAlign(Paint.Align.LEFT);
        Bitmap myBitmap = Bitmap.createBitmap((int) textPaint.measureText(text), (int) 50, Bitmap.Config.ARGB_8888);
        Canvas myCanvas = new Canvas(myBitmap);
        myCanvas.drawText(text, 0, myBitmap.getHeight()-5, textPaint);
        return myBitmap;
    }
    public static boolean isValidIPAddress(String ipAddress) {
        if ((ipAddress != null) && (!ipAddress.isEmpty())) {
            return Pattern.matches("^([1-9]|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])(\\.(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])){3}$", ipAddress);
        }
        return false;
    }

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override

        public void onReceive(Context context, Intent intent) {
            if (null == intent) {
                return;
            }
            TextView mtext=findViewById(R.id.BATTERVO);
            String action = intent.getAction();

            if (Intent.ACTION_BATTERY_CHANGED.equals(action)) {
                // 电池温度
                int temperature = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1);
               // mleve=intent.getStringExtra(BatteryManager.EXTRA_LEVEL);
                int plugged = intent.getIntExtra(EXTRA_PLUGGED,0);
                double retemperature = intent.getIntExtra(EXTRA_TEMPERATURE, -1)/10.0;
                retemp=retemperature;

                if(plugged==BATTERY_PLUGGED_WIRELESS){
                    Boolean iswirelesscharge=true;
                }
                mtext.post(new Runnable() {
                    @Override
                    public void run() {
                        mtext.setText("电池电压="+temperature+"mV");
                    }
                });
                System.out.println(retemperature);
            }}};
    //sys_path 为节点映射到的实际路径



}