package com.frannyzhao.tellingminutes;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;

import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
/**
 * Created by zhaofengyi on 4/7/17.
 */

public class CrashHandler implements Thread.UncaughtExceptionHandler {
    /**
     * 系统默认UncaughtExceptionHandler
     */
    private Thread.UncaughtExceptionHandler mDefaultHandler;

    /**
     * context
     */
    private static Context mContext;

    /**
     * 存储异常和参数信息
     */
    private Map<String, String> paramsMap = new HashMap<>();

    /**
     * 格式化时间
     */
    private SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");

    private static final String TAG = "CrashHandler";

    private String mCrashLogDir;

    private static final String AWS_POOL_ID = "fake pool id";
    private static final Regions AWS_POOL_REGION = Regions.DEFAULT_REGION;
    private static final String AWS_BUCKET_NAME = "fake bucket name";
    private static final String AWS_LOG_DIR = "fake/dir/name/";

    private static CrashHandler mInstance;

    private static TransferUtility mTransferUtility;
    private CrashHandler() {

    }

    /**
     * 获取CrashHandler实例
     */
    public static synchronized CrashHandler getInstance() {
        if (null == mInstance) {
            mInstance = new CrashHandler();
        }
        return mInstance;
    }

    public void init(Context context) {
        mContext = context;
        mCrashLogDir = mContext.getFilesDir().getAbsolutePath() + File.separator + "crash" + File.separator;
        mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        //设置该CrashHandler为系统默认的
        Thread.setDefaultUncaughtExceptionHandler(this);

        CognitoCachingCredentialsProvider credentialsProvider = new CognitoCachingCredentialsProvider(
                mContext,    /* get the context for the application */
                AWS_POOL_ID,    /* Identity Pool ID */
                AWS_POOL_REGION           /* Region for your identity pool--US_EAST_1 or EU_WEST_1*/
        );
        AmazonS3 s3 = new AmazonS3Client(credentialsProvider);
        mTransferUtility = new TransferUtility(s3, mContext);
    }

    /**
     * uncaughtException 回调函数
     */
    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        if (!handleException(ex) && mDefaultHandler != null) {
            //如果自己没处理交给系统处理
            mDefaultHandler.uncaughtException(thread, ex);
        } else {
            Log.e(TAG, "will kill myself in 5 seconds");
            //自己处理
            new Thread() {
                @Override
                public void run() {
                    try {//延迟5秒杀进程
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        Log.e(TAG, "error : ", e);
                    }
                    //退出程序
                    android.os.Process.killProcess(android.os.Process.myPid());
                }
            }.start();
        }

    }

    /**
     * 收集错误信息.发送到服务器
     *
     * @return 处理了该异常返回true, 否则false
     */
    private boolean handleException(Throwable ex) {
        if (ex == null) {
            return false;
        }
        //收集设备参数信息
        collectDeviceInfo(mContext);
        //添加自定义信息
        addCustomInfo();
        //使用Toast来显示异常信息
        new Thread() {
            @Override
            public void run() {
                Looper.prepare();
                //在此处处理出现异常的情况
                Toast.makeText(mContext, mContext.getString(R.string.crash_toast), Toast.LENGTH_SHORT).show();
                Looper.loop();
            }
        }.start();
        //保存日志文件
        saveCrashInfo2File(ex);
        return true;
    }


    /**
     * 收集设备参数信息
     *
     * @param ctx
     */
    public void collectDeviceInfo(Context ctx) {
        //获取versionName,versionCode
        try {
            PackageManager pm = ctx.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(ctx.getPackageName(), PackageManager.GET_ACTIVITIES);
            if (pi != null) {
                String versionName = pi.versionName == null ? "null" : pi.versionName;
                String versionCode = pi.versionCode + "";
                paramsMap.put("versionName", versionName);
                paramsMap.put("versionCode", versionCode);
            }
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "an error occured when collect package info", e);
        }
        //获取所有系统信息
        Field[] fields = Build.class.getDeclaredFields();
        for (Field field : fields) {
            try {
                field.setAccessible(true);
                paramsMap.put(field.getName(), field.get(null).toString());
            } catch (Exception e) {
                Log.e(TAG, "an error occured when collect crash info", e);
            }
        }
    }

    /**
     * 添加自定义参数
     */
    private void addCustomInfo() {
        Log.i(TAG, "Something wrong with me...");
    }

    /**
     * 保存错误信息到文件中
     *
     * @param ex
     * @return 返回文件名称, 便于将文件传送到服务器
     */
    private String saveCrashInfo2File(Throwable ex) {

        StringBuffer sb = new StringBuffer();
        for (Map.Entry<String, String> entry : paramsMap.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            sb.append(key + "=" + value + "\n");
        }

        Writer writer = new StringWriter();
        PrintWriter printWriter = new PrintWriter(writer);
        ex.printStackTrace(printWriter);
        Throwable cause = ex.getCause();
        while (cause != null) {
            cause.printStackTrace(printWriter);
            cause = cause.getCause();
        }
        printWriter.close();
        String result = writer.toString();
        sb.append(result);
        try {
            long timestamp = System.currentTimeMillis();
            String time = format.format(new Date());
            String fileName = "crash-" + time + "-" + timestamp + ".log";
            File dir = new File(mCrashLogDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            FileOutputStream fos = new FileOutputStream(mCrashLogDir + fileName);
            fos.write(sb.toString().getBytes());
            Log.w(TAG, "saveCrashInfo2File: " + mCrashLogDir + fileName + "\n" +sb.toString());
            fos.close();
            return fileName;
        } catch (Exception e) {
            Log.e(TAG, "an error occured while writing file...", e);
        }
        return null;
    }

    public void uploadCrashLogFiles() {
        File dir = new File(mCrashLogDir);
        if (!dir.exists()) {
            return;
        }
        File[] crashLogFiles = dir.listFiles();
        for (File crashLogFile : crashLogFiles) {
            uploadData(crashLogFile.getName());
        }
    }

    /**
     * 上传数据
     * @param fileName
     */
    private void uploadData(final String fileName) {
        String path = mCrashLogDir + fileName;
        Log.w(TAG, "going to upload log file: " + path);
        final File crashLogFile = new File(path);
        TransferObserver observer = mTransferUtility.upload(
                AWS_BUCKET_NAME,     /* The bucket to upload to */
                AWS_LOG_DIR + fileName,    /* The key for the uploaded object */
                crashLogFile        /* The file where the data to upload exists */
        );
        observer.setTransferListener(new TransferListener() {
            @Override
            public void onStateChanged(int id, TransferState state) {
                Log.d(TAG, "uploadData() onStateChanged id = " + id + ", state = " + state);
                if (state == TransferState.COMPLETED) {
                    crashLogFile.delete();
                }
            }

            @Override
            public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
                Log.d(TAG, "uploadData() onProgressChanged id = " + id + ", bytesCurrent" + bytesCurrent + ", bytesTotal" + bytesTotal);
            }

            @Override
            public void onError(int id, Exception ex) {
                Log.e(TAG, "uploadData() onError id = " + id + ", ex = " + ex);
            }
        });
    }

    /**
     * 安全关闭.
     * @param closeable Closeable.
     */
    public static void closeSafely(Closeable closeable) {
        try {
            if (closeable != null) {
                closeable.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
