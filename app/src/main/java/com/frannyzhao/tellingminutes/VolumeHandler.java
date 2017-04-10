package com.frannyzhao.tellingminutes;

import android.content.Context;
import android.media.AudioManager;
import android.util.Log;

/**
 * Created by zhaofengyi on 4/6/17.
 */

public class VolumeHandler {
    private static final String TAG = "VolumeHandler";
    // 调音量
    private AudioManager am = null;
    private static int MUSIC_VOLUME;
    private static boolean needRestoreVolume = true;

    public VolumeHandler(Context context) {
        am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
    }

    public void turnVolumeUpToMax() {
        // 保存原先的音量值
        MUSIC_VOLUME = am.getStreamVolume(AudioManager.STREAM_MUSIC);
        if (am.isWiredHeadsetOn() || am.isBluetoothA2dpOn()) {
            needRestoreVolume = false;
        } else {
            am.setStreamVolume(AudioManager.STREAM_MUSIC,
                    am.getStreamMaxVolume(AudioManager.STREAM_MUSIC), AudioManager.FLAG_PLAY_SOUND);
            needRestoreVolume = true;
        }
    }

    public void restoreVolume() {
        // 音量恢复到原本的值
        if (needRestoreVolume) {
            am.setStreamVolume(AudioManager.STREAM_MUSIC, MUSIC_VOLUME, AudioManager.FLAG_PLAY_SOUND);
        }
    }

}
