package com.frannyzhao.tellingminutes;

import android.content.Context;
import android.media.AudioManager;

/**
 * Created by zhaofengyi on 4/6/17.
 */

public class VolumeHandler {
    // 调音量
    private AudioManager am = null;
    private int MUSIC_VOLUME;

    public VolumeHandler(Context context) {
        am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
    }

    public void turnVolumeUpToMax() {
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

    public void restoreVolume() {
        // 音量恢复到原本的值
//        am.setStreamVolume(AudioManager.STREAM_SYSTEM, SYSTEM_VOLUME, AudioManager.FLAG_PLAY_SOUND);
        am.setStreamVolume(AudioManager.STREAM_MUSIC, MUSIC_VOLUME, AudioManager.FLAG_PLAY_SOUND);
//        am.setStreamVolume(AudioManager.STREAM_RING, RING_VOLUME, AudioManager.FLAG_PLAY_SOUND);
//        am.setStreamVolume(AudioManager.STREAM_NOTIFICATION, NOTIFICATION_VOLUME, AudioManager.FLAG_PLAY_SOUND);
    }

}
