package cn.upus.app.upems.util;

import android.content.Context;
import android.media.AudioManager;


/**
 * 扬声器开关
 */
public class SpeakerUtil {

    private Context context;
    private int currVolume;

    public SpeakerUtil(Context context) {
        this.context = context;
    }

    public int getCurrVolume() {
        return currVolume;
    }

    public void setCurrVolume(int currVolume) {
        this.currVolume = currVolume;
    }

    //打开扬声器
    public void openSpeaker() {
        try {
            AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
            assert audioManager != null;
            audioManager.setMode(AudioManager.MODE_IN_CALL);
            setCurrVolume(audioManager.getStreamVolume(AudioManager.STREAM_VOICE_CALL));
            if (!audioManager.isSpeakerphoneOn()) {
                audioManager.setSpeakerphoneOn(true);
                audioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL, audioManager.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL), AudioManager.STREAM_VOICE_CALL);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //关闭扬声器
    public void closeSpeaker() {
        try {
            AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
            if (audioManager != null) {
                if (audioManager.isSpeakerphoneOn()) {
                    audioManager.setSpeakerphoneOn(false);
                    audioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL, getCurrVolume(), AudioManager.STREAM_VOICE_CALL);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
