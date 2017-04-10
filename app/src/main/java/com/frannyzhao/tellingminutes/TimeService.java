package com.frannyzhao.tellingminutes;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

public class TimeService extends Service {
    private static final String TAG = "TimeService";

    private static final int NOTIFICATION_ID = 8912;
    private boolean isRunning  = false;
    private long mCurrentTime;

    private ServiceCallback serviceCallback = null;

    public ServiceCallback getServiceCallback() {
        return serviceCallback;
    }

    public void setServiceCallback(ServiceCallback serviceCallback) {
        Log.d(TAG, "setServiceCallback");
        this.serviceCallback = serviceCallback;
    }

    private void getTime() {
        mCurrentTime = System.currentTimeMillis();
//        Log.d(TAG, "mCurrentTime = " + mCurrentTime);
        if (serviceCallback != null) {
            serviceCallback.getServiceData(ServiceCallback.KEY_CURRENT_TIME, mCurrentTime);
        }
    }

    @Override
    public void onCreate() {
        Log.i(TAG, "Service onCreate");
        isRunning = true;
        //Creating new thread for my service
        //Always write your long running tasks in a separate thread, to avoid ANR
        new Thread(new Runnable() {
            @Override
            public void run() {
                //Your logic that service will perform will be placed here
                //In this example we are just looping and waits for 1000 milliseconds in each loop.
                while (isRunning) {
                    try {
//                        Log.i(TAG, "Service running");
                        getTime();
                        Thread.sleep(1000);

                    } catch (Exception e) {
                    }
                }

                //Stop service once it finishes its task
                stopSelf();
            }
        }).start();


        Intent notificationIntent = new Intent(this, MainActivity.class);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                notificationIntent, 0);

        Notification notification = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.telling_minutes_icon)
                .setLargeIcon(BitmapFactory.decodeResource(this.getResources(), R.drawable.telling_minutes_icon))
                .setContentTitle("Telling minutes")
                .setContentText("Ongoing... click here to open it")
                .setContentIntent(pendingIntent).build();

        startForeground(NOTIFICATION_ID, notification);
        Log.i(TAG, "Service onCreate end");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "Service onStartCommand");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent arg0) {
        Log.i(TAG, "Service onBind");
        return new ServiceBinder();
    }

    @Override
    public void onDestroy() {
        isRunning = false;
        stopForeground(true);
        Log.i(TAG, "Service onDestroy");
    }

    public class ServiceBinder extends android.os.Binder{
        public TimeService getService() {
            System.out.println("getService()");
            return TimeService.this;
        }
    }
}