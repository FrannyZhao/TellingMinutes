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
import android.widget.Button;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, ServiceConnection{
    private static final String TAG = "MainActivity";
    private TextView dateTextView;
    private TextView timeTextView;
    private TextView openTTSTextView;
    private Button openTTSButton;
    // 时间相关
    private final Calendar mCalendar = Calendar.getInstance();
    int mHour = -1;
    int mMinuts = -1;
    private SimpleDateFormat formatterDate = new SimpleDateFormat("yyyy/MM/dd", Locale.CHINESE);
    private SimpleDateFormat formatterTime = new SimpleDateFormat("HH:mm:ss", Locale.CHINESE);
    // 语音相关
    private String minutesStr = "";
    private TextToSpeech mTts;
    private boolean needTellMinutes = false;
    // 调音量
    private VolumeHandler mVolumeHandler;

    TimeService.ServiceBinder binder;
    Intent intent = new Intent();
    private TimeHandler timeHandler = null;
    private Object obj = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        dateTextView = (TextView) findViewById(R.id.showDate);
        timeTextView = (TextView) findViewById(R.id.showTime);
        openTTSTextView = (TextView) findViewById(R.id.tv_tts_error);
        openTTSButton = (Button) findViewById(R.id.btn_tts_error);
        openTTSTextView.setOnClickListener(this);
        openTTSButton.setOnClickListener(this);

        CrashHandler.getInstance().init(this);

        timeHandler = new TimeHandler();

        mVolumeHandler = new VolumeHandler(this);
        mVolumeHandler.turnVolumeUpToMax();

        intent.setClass(MainActivity.this, TimeService.class);
        bindService(intent, MainActivity.this, BIND_AUTO_CREATE);

        CrashHandler.getInstance().uploadCrashLogFiles();
    }
    @Override
    protected void onResume() {
        super.onResume();
        openTTSTextView.setVisibility(View.GONE);
        openTTSButton.setVisibility(View.GONE);
        disableAndEnableTTS();
    }

    private void disableAndEnableTTS() {
        if(mTts != null) {
            mTts.stop();
            mTts.shutdown();
            Log.d(TAG, "TTS Destroyed");
        }
        final Locale defaultLocal = Locale.getDefault();
        Log.e(TAG, "defaultLocal = " + defaultLocal);
        mTts = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                Log.d(TAG, "onInit status = " + status);
                if (status == TextToSpeech.SUCCESS) {
                    if (null != mTts) {
                        mTts.setSpeechRate(0.75f);
                        needTellMinutes = true;
                        Log.d(TAG, "mTts.isLanguageAvailable = " + mTts.isLanguageAvailable(defaultLocal));
                        if (mTts.isLanguageAvailable(defaultLocal) >= 0) {
                            mTts.setLanguage(defaultLocal);
                        } else if (mTts.isLanguageAvailable(Locale.ENGLISH) >= 0) {
                            mTts.setLanguage(Locale.ENGLISH);
                        } else {
                            // 打开设置tts页面
                            Log.d(TAG, "I will open tts settings");
                            openTTSTextView.setVisibility(View.VISIBLE);
                            openTTSButton.setVisibility(View.VISIBLE);
                        }
                    } else {
                        Log.e(TAG, "Cann't create TextToSpeech object");
                    }
                }
            }
        });
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
        mCalendar.setTimeInMillis(currentTime);
        mHour = mCalendar.get(Calendar.HOUR);
        if (mMinuts != -1 && mCalendar.get(Calendar.MINUTE) > mMinuts) {
            needTellMinutes = true;
            Log.d(TAG, "set needTellMinutes true");
//        obj.equals(""); //hack test crash handler
        }
        mMinuts = mCalendar.get(Calendar.MINUTE);

        Date curDate =  new Date(currentTime);
        dateTextView.setText(formatterDate.format(curDate));
        timeTextView.setText(formatterTime.format(curDate));
        minutesStr = mHour + "点" + mMinuts + "分";
        if (needTellMinutes) {
            mTts.speak(minutesStr, TextToSpeech.QUEUE_ADD, null);
            needTellMinutes = false;
        }
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
        if (id == R.id.btn_tts_error || id == R.id.tv_tts_error) {
            Intent intent = new Intent();
            intent.setAction("com.android.settings.TTS_SETTINGS");
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            MainActivity.this.startActivity(intent);
        }
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
