package com.frannyzhao.tellingminutes;

import android.content.Context;
import android.media.AudioManager;
import android.os.Handler;
import android.os.Message;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private TextView dateTextView;
    private TextView timeTextView;
    // 时间相关
    private final Calendar mCalendar = Calendar.getInstance();
    int mHour = -1;
    int mMinuts = -1;
    private long mCurrentTime;
    private SimpleDateFormat formatterDate = new SimpleDateFormat("yyyy/MM/dd", Locale.CHINESE);
    private SimpleDateFormat formatterTime = new SimpleDateFormat("HH:mm:ss", Locale.CHINESE); // todo 多语言支持
    // 语音相关
    private String minutesStr = "";
    private TextToSpeech mTts;
    private boolean isFirstTimeSpeak = true;
    private boolean needTellMinutes = false;
    // 调音量
    private static AudioManager am;
    private int MUSIC_VOLUME;
//    private int SYSTEM_VOLUME, RING_VOLUME, NOTIFICATION_VOLUME;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        dateTextView = (TextView) findViewById(R.id.showDate);
        timeTextView = (TextView) findViewById(R.id.showTime);

        am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        turnVolumeUpToMax();

        mTts = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    if (null != mTts) {
                        mTts.setSpeechRate(1.0f);
                        mTts.setLanguage(Locale.CHINESE); // todo 多语言支持
                    } else {
                        Log.e(TAG, "Cann't create TextToSpeech object");
                    }
                }
            }
        });
        updateHandler.sendEmptyMessage(0);
    }

    private void turnVolumeUpToMax(){
        // 保存原先的音量值
//        SYSTEM_VOLUME = am.getStreamVolume(AudioManager.STREAM_SYSTEM);
        MUSIC_VOLUME = am.getStreamVolume(AudioManager.STREAM_MUSIC);
//        RING_VOLUME = am.getStreamVolume(AudioManager.STREAM_RING);
//        NOTIFICATION_VOLUME = am.getStreamVolume(AudioManager.STREAM_NOTIFICATION);
        // 音量调到最大值
//        am.setStreamVolume(AudioManager.STREAM_SYSTEM,
//                am.getStreamMaxVolume(AudioManager.STREAM_SYSTEM), AudioManager.FLAG_PLAY_SOUND);
        am.setStreamVolume(AudioManager.STREAM_MUSIC,
                am.getStreamMaxVolume(AudioManager.STREAM_MUSIC), AudioManager.FLAG_PLAY_SOUND);
//        am.setStreamVolume(AudioManager.STREAM_RING,
//                am.getStreamMaxVolume(AudioManager.STREAM_RING), AudioManager.FLAG_PLAY_SOUND);
//        am.setStreamVolume(AudioManager.STREAM_NOTIFICATION,
//                am.getStreamMaxVolume(AudioManager.STREAM_NOTIFICATION), AudioManager.FLAG_PLAY_SOUND);
    }

    private void restoreVolume(){
        // 音量恢复到原本的值
//        am.setStreamVolume(AudioManager.STREAM_SYSTEM, SYSTEM_VOLUME, AudioManager.FLAG_PLAY_SOUND);
        am.setStreamVolume(AudioManager.STREAM_MUSIC, MUSIC_VOLUME, AudioManager.FLAG_PLAY_SOUND);
//        am.setStreamVolume(AudioManager.STREAM_RING, RING_VOLUME, AudioManager.FLAG_PLAY_SOUND);
//        am.setStreamVolume(AudioManager.STREAM_NOTIFICATION, NOTIFICATION_VOLUME, AudioManager.FLAG_PLAY_SOUND);
    }

    @Override
    protected void onDestroy() {
        //Close the Text to Speech Library
        if(mTts != null) {
            mTts.stop();
            mTts.shutdown();
            Log.d(TAG, "TTS Destroyed");
        }
        restoreVolume();
        super.onDestroy();
    }

    private void getTime() {
        mCurrentTime = System.currentTimeMillis();
        mCalendar.setTimeInMillis(mCurrentTime);
        mHour = mCalendar.get(Calendar.HOUR);
        if (mCalendar.get(Calendar.MINUTE) > mMinuts) {
            needTellMinutes = true;
        }
        mMinuts = mCalendar.get(Calendar.MINUTE);

        Date curDate =  new Date(mCurrentTime);
        dateTextView.setText(formatterDate.format(curDate));
        timeTextView.setText(formatterTime.format(curDate));
        minutesStr = mHour + "点" + mMinuts + "分"; // todo 多语言支持
        if (needTellMinutes) {
            mTts.speak(minutesStr, TextToSpeech.QUEUE_ADD, null);
            if (!isFirstTimeSpeak) {
                needTellMinutes = false;
            }

        }
        isFirstTimeSpeak = false;
    }

    private Handler updateHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            getTime();
            updateHandler.sendEmptyMessageDelayed(0, 1000);
        }
    };

}
