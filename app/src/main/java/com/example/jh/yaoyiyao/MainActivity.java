package com.example.jh.yaoyiyao;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;

import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

public class MainActivity extends AppCompatActivity {
    
    @ViewInject(R.id.image1)
    private ImageView image1;
    @ViewInject(R.id.image2)
    private  ImageView image2;
    
    SensorManager sensorManager;
    Sensor accSensor;//加速度传感器
    SensorEventListener accListener;//加速度传感器监听器
    long lastTime;
    private int rawId;
    private SoundPool soundPool;
    //震动
    private Vibrator vibrator;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        x.Ext.setDebug(true);
        x.Ext.init(getApplication());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        x.view().inject(this);

        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);//获取系统震动服务

        sensorManager= (SensorManager) getSystemService(SENSOR_SERVICE);
        initSoundPool();//初始化短小音乐
        initSensor();
    }

    private void initSensor() {
        accSensor=sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        accListener=new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                //判断条件成功后，执行摇一摇的效果
                long curTime=System.currentTimeMillis();//获取当前时间
                if ((curTime-lastTime)<1000) {
                   // Toast.makeText(MainActivity.this, "间隔时间过短", Toast.LENGTH_SHORT).show();
                    return;
                }
                
                float[]values=event.values;
                float x=values[0];
                float y=values[1];
                float z=values[2];
                Log.e("自定义标签", "类名:MainActivity" + "方法名：onSensorChanged: "+x+":"+y+":"+z);
                if (x>10||y>10||z>10) {
                    //摇一摇成功
                    //1.播音乐2.执行动画
                    soundPool.play(rawId,2,1,1,0,1);//参数2 左声道 参数3 右声道 参数4 优先级 参数5 是否循环  参数6 速率0.5-2

                    playAnim();

                    //摇动手机后，再伴随震动提示~~
                    vibrator.vibrate(500);

                }
                
                lastTime=curTime;
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {

            }
        };
        //注册
        sensorManager.registerListener(accListener,accSensor,SensorManager.SENSOR_DELAY_GAME);
        
    }

    private void initSoundPool(){
        //专门用于播放短小音乐
         soundPool=new SoundPool(10, AudioManager.STREAM_MUSIC,1);
        rawId=soundPool.load(getApplicationContext(),R.raw.kakaka,1);//将资源转化为可播放对象
    }
    private void playAnim() {
        //微信摇一摇动画
        AnimatorSet animatorSet=new AnimatorSet();
        int width=image1.getWidth();
        int height=image1.getHeight();
        //属性动画，根据屏幕圆点的
        ObjectAnimator up_image1=ObjectAnimator.ofFloat(image1,"translationY",0,-height);//向上移动
        ObjectAnimator up_image2=ObjectAnimator.ofFloat(image1,"translationY",-height,0);//返回

        ObjectAnimator down_image1=ObjectAnimator.ofFloat(image2,"translationY",0,height);//向下移动
        ObjectAnimator down_image2=ObjectAnimator.ofFloat(image2,"translationY",height,0);//返回

        animatorSet.play(up_image1).with(down_image1);//先执行up1--down1
        animatorSet.play(up_image2).after(up_image1).with(down_image2);//后执行
        animatorSet.setDuration(1000);
        animatorSet.start();//执行动画3


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        sensorManager.unregisterListener(accListener);//解除注册
    }
}
