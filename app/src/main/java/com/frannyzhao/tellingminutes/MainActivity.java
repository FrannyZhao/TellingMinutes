package com.frannyzhao.tellingminutes;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, TextToSpeech.OnInitListener, ServiceConnection{
    private static final String TAG = "MainActivity";
    private TextView dateTextView;
    private TextView timeTextView;
    // 时间相关
    private final Calendar mCalendar = Calendar.getInstance();
    int mHour = -1;
    int mMinuts = -1;
    private SimpleDateFormat formatterDate = new SimpleDateFormat("yyyy/MM/dd", Locale.CHINESE);
    private SimpleDateFormat formatterTime = new SimpleDateFormat("HH:mm:ss", Locale.CHINESE); // todo 多语言支持
    // 语音相关
    private String minutesStr = "";
    private TextToSpeech mTts;
//    private boolean isFirstTimeSpeak = true;
    private boolean needTellMinutes = false;
    // 调音量
    private VolumeHandler mVolumeHandler;

    TimeService.ServiceBinder binder;
    Intent intent = new Intent();
    private TimeHandler timeHandler = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        dateTextView = (TextView) findViewById(R.id.showDate);
        timeTextView = (TextView) findViewById(R.id.showTime);
        timeHandler = new TimeHandler();

        mVolumeHandler = new VolumeHandler(this);
        mVolumeHandler.turnVolumeUpToMax();

        intent.setClass(MainActivity.this, TimeService.class);
        bindService(intent, MainActivity.this, BIND_AUTO_CREATE);

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
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {/*
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
            */
        } else {
            Log.d(TAG, "onInit status = " + status);
        }
    }

    @Override
    protected void onDestroy() {
        //Close the Text to Speech Library
        if(mTts != null) {
            mTts.stop();
            mTts.shutdown();
            Log.d(TAG, "TTS Destroyed");
        }
        mVolumeHandler.restoreVolume();
        unbindService(this);
        super.onDestroy();
    }

    private void getTime(long currentTime) {
//        mCurrentTime = System.currentTimeMillis();
        mCalendar.setTimeInMillis(currentTime);
        mHour = mCalendar.get(Calendar.HOUR);
        if (mCalendar.get(Calendar.MINUTE) > mMinuts) {
            needTellMinutes = true;
            Log.d(TAG, "set needTellMinutes true");
        }
        mMinuts = mCalendar.get(Calendar.MINUTE);

        Date curDate =  new Date(currentTime);
        dateTextView.setText(formatterDate.format(curDate));
        timeTextView.setText(formatterTime.format(curDate));
        minutesStr = mHour + "点" + mMinuts + "分"; // todo 多语言支持
        if (needTellMinutes) {
            mTts.speak(minutesStr, TextToSpeech.QUEUE_ADD, null);
//            if (!isFirstTimeSpeak) {
                needTellMinutes = false;
//            }

        }
//        isFirstTimeSpeak = false;
    }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            System.out.println("onServiceConnected");
            Log.d(TAG, "onServiceConnected");
            binder = (TimeService.ServiceBinder) service;
            binder.getService().setServiceCallback(new ServiceCallback() {
                @Override
                public void getServiceData(int key, Object value) {
                    switch (key) {
                        case ServiceCallback.KEY_CURRENT_TIME:
                            Message msg = new Message();
                            msg.what = TimeHandler.MSG_UPDATE_TIME;
                            Bundle bundle = new Bundle();
                            bundle.putLong(TimeHandler.KEY_TIME, (long) value);
                            msg.setData(bundle);
                            timeHandler.sendMessage(msg);
                            break;
                        default:
                            break;
                    }
                }
            });

        }

    @Override
    public void onClick(View v) {
        System.out.println("onClick");
        int id = v.getId();

    }

    private class TimeHandler extends Handler {
        static final int MSG_UPDATE_TIME = 1;
        static final String KEY_TIME = "key_time";
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_UPDATE_TIME:
                    getTime(msg.getData().getLong(KEY_TIME));
                    break;
                default:
                    break;
            }
        }
    }

}
