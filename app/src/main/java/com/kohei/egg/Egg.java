package com.kohei.egg;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.ImageView;

import java.util.Random;

public class Egg extends Activity implements SensorEventListener {
    private AudioAttributes audioAttributes;
    private SoundPool soundPool;
    private int soundKon, soundKonKon,soundEggBreak,soundWarisugi;
    //public final static String TAG = "SensorTest2";
    protected final static double RAD2DEG = 180 / Math.PI;
    SensorManager sensorManager;
    float[] rotationMatrix = new float[9];
    float[] gravity = new float[3];
    float[] geomagnetic = new float[3];
    float[] attitude = new float[3];
    private int sensorFlg = 0;
    private int knockCnt = 0;
    ImageView imageEggPic;

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_egg);
        findImages();
        initSensor();
        soundSet();
        knockSet(10);
    }

    public void onResume() {
        super.onResume();
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_GAME);
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD), SensorManager.SENSOR_DELAY_GAME);
    }

    public void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }


    protected void findViews() {
        /*
        azimuthText = (TextView) findViewById(R.id.textView1);
        pitchText = (TextView) findViewById(R.id.textView2);
        rollText = (TextView) findViewById(R.id.textView3);
        */
    }

    protected void findImages() {
        imageEggPic = (ImageView)findViewById(R.id.imageEgg);
        imageEggPic.setImageResource(R.drawable.egg);
    }

    protected void initSensor() {
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
    }
    protected void soundSet(){
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            //kitkat以下
            soundPool = new SoundPool(4, AudioManager.STREAM_MUSIC, 0);
        }else{
            //lollipop以上
            audioAttributes = new AudioAttributes.Builder()
                    // USAGE_MEDIA
                    // USAGE_GAME
                    .setUsage(AudioAttributes.USAGE_GAME)
                    // CONTENT_TYPE_MUSIC
                    // CONTENT_TYPE_SPEECH, etc.
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .build();

            soundPool = new SoundPool.Builder()
                    .setAudioAttributes(audioAttributes)
                    // ストリーム数に応じて
                    .setMaxStreams(2)
                    .build();
        }
        //サウンドをロードしておく
        soundKon = soundPool.load(this, R.raw.kon, 1);
        soundKonKon = soundPool.load(this, R.raw.konkon, 1);
        soundWarisugi = soundPool.load(this, R.raw.warisugi, 1);
        soundEggBreak = soundPool.load(this, R.raw.eggbreak, 1);
    }
    //ノックの回数
    private void knockSet(int x){
        //乱数生成
        Random r = new Random();
        //3~X回ノックさせる
        knockCnt = r.nextInt(x) + 3;
    }


    //画面タップで違うアクション
    @Override
    public boolean onTouchEvent(MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    //殻を割って、画面が上を向いているときに画面遷移
                    if (Math.abs((int) (attitude[2] * RAD2DEG)) <= 50 && knockCnt == -1) {
                        Log.v("Tap", "タップ中");
                        Intent intent = new Intent(getApplication(), Egg.class);
                        startActivity(intent);
                    }
                    break;
                case MotionEvent.ACTION_MOVE:
                    //いいタイミングで画面が下を向いているときに殻を割れる
                    if (Math.abs((int) (attitude[2] * RAD2DEG)) >= 160 && knockCnt == 0) {
                        Log.v("Tap", "むーぶ");
                        imageEggPic.setImageResource(R.drawable.egg5);
                        imageEggPic.setScaleType(ImageView.ScaleType.FIT_XY);
                        soundPool.play(soundEggBreak, 1.0f, 1.0f, 0, 0, 1);
                        knockCnt = -1;
                    }
                    break;
            }

        return true;
    }
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        switch (event.sensor.getType()) {
            case Sensor.TYPE_MAGNETIC_FIELD:
                geomagnetic = event.values.clone();
                break;
            case Sensor.TYPE_ACCELEROMETER:
                gravity = event.values.clone();
                break;
        }
        if (geomagnetic != null && gravity != null) {
            SensorManager.getRotationMatrix(rotationMatrix, null, gravity, geomagnetic);
            SensorManager.getOrientation(rotationMatrix, attitude);
            if((int)(attitude[1] * RAD2DEG) >= -15 && (int)(attitude[1] * RAD2DEG) <= 30 && Math.abs((int)(attitude[2] * RAD2DEG)) >= 150) {
                if(sensorFlg == 0) {
                    if(knockCnt == -1){
                        sensorFlg = 1;
                        knockCnt = -1;
                    }else if(knockCnt == 0){
                        imageEggPic.setImageResource(R.drawable.egg4);
                        soundPool.play(soundWarisugi, 1.0f, 1.0f, 0, 0, 1);
                        sensorFlg = 1;
                        knockCnt = -1;
                    }else if(knockCnt == 1){
                        imageEggPic.setImageResource(R.drawable.egg3);
                        soundPool.play(soundKonKon, 1.0f, 1.0f, 0, 0, 1);
                        sensorFlg = 1;
                        knockCnt --;
                    }else{
                        imageEggPic.setImageResource(R.drawable.egg2);
                        soundPool.play(soundKon, 1.0f, 1.0f, 0, 0, 1);
                        sensorFlg = 1;
                        knockCnt --;
                    }
                }
            }
            if((int)(attitude[1] * RAD2DEG) >= -90 && (int)(attitude[1] * RAD2DEG) <= -20 && Math.abs((int)(attitude[2] * RAD2DEG)) >= 150) {
                if(sensorFlg == 1){
                    sensorFlg = 0;
                }
            }
            //Log.v("sensorFlg", String.valueOf(sensorFlg));
        }
    }

}