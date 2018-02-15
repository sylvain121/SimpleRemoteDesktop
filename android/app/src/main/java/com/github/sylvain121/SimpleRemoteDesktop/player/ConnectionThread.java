package com.github.sylvain121.SimpleRemoteDesktop.player;

import android.content.SharedPreferences;
import android.util.Log;

import com.github.sylvain121.SimpleRemoteDesktop.player.video.DataManagerChannel;
import com.github.sylvain121.SimpleRemoteDesktop.player.video.MediaCodecDecoderRenderer;
import com.github.sylvain121.SimpleRemoteDesktop.settings.SettingsActivity;

import java.util.Arrays;

/**
 * Created by ESME7383 on 06/10/2017.
 */

class ConnectionThread extends Thread {
    private final int width;
    private final int height;
    private final String ipAddress;
    private final int bandwidth;
    private final int fps;
    private int codec_height;
    private int codec_width;
    private DataManagerChannel m_renderSock;
    private MediaCodecDecoderRenderer mediaCodec;
    private int FrameNumber = 0;

    private final static String TAG = "CONNEXION_THREAD";

    public ConnectionThread(int width, int height, String ipAddress, SharedPreferences sharedPreference) {

        this.width = width;
        this.height = height;
        this.ipAddress = ipAddress;

        String currentResolution = sharedPreference.getString(SettingsActivity.SIMPLE_REMOTE_DESKTOP_PREF_RESOLUTION, null);

        switch (currentResolution) {
            case "600p":
                codec_width = 800;
                codec_height = 600;
                break;
            case "720p":
                codec_width = 1280;
                codec_height = 720;
                break;
            case "1080p":
                codec_width = 1920;
                codec_height = 1080;
                break;
            case "original":
                codec_width = 0;
                codec_height = 0;
        }


        bandwidth = sharedPreference.getInt(SettingsActivity.SIMPLE_REMOTE_DESKTOP_PREF_BITRATE,0);
        fps = sharedPreference.getInt(SettingsActivity.SIMPLE_REMOTE_DESKTOP_PREF_FPS,0);

    }

    @Override
    public void run() {
        Log.d(TAG, "getting network instance");
        m_renderSock = DataManagerChannel.getInstance();
        Log.d(TAG, "connecting to serveur "+ipAddress+ " port "+ 8001);
        m_renderSock.connect(ipAddress, 8001);
        Log.d(TAG, "Send start message");
        m_renderSock.sendStartStream(this.width, this.height, fps,codec_width, codec_height, bandwidth);


        while (!Thread.interrupted()) {
            byte[] frameData = m_renderSock.receive();
            Log.d(TAG, "frame type : "+ frameData[4]);
            if(frameData[4] == 0x67) {
                int ppsOffset = 0;
                int dataOffset = 0;
                for(int i = 4; i < frameData.length; i++) {
                    int NAL_START = frameData[i] << 16 | frameData[i + 1] << 8 | frameData[i + 2];
                    if(NAL_START == 0x01 && frameData[i + 3] == 0x68 ) {
                        ppsOffset = i - 1;
                        i = i + 4;
                    } else if(ppsOffset > 0 && NAL_START == 0x01) {
                        dataOffset = i;
                        break;
                    }


                }

                byte[] sps = Arrays.copyOfRange(frameData, 0, ppsOffset);
                byte[] pps = Arrays.copyOfRange(frameData, ppsOffset, dataOffset);
                byte[] data = Arrays.copyOfRange(frameData, dataOffset, frameData.length);

                sendFrame(sps);
                sendFrame(pps);
                sendFrame(data);
            } else {
                    sendFrame(frameData);
            }
        }
    }

    private void sendFrame(byte[] data) {
        Log.d(TAG, "sending new frame");
        this.mediaCodec.submitDecodeUnit(data, data.length, this.FrameNumber++,System.currentTimeMillis());
    }

    public void setDecoderHandler(MediaCodecDecoderRenderer mediaCodec) {
        this.mediaCodec = mediaCodec;
    }

    public void close() {
        m_renderSock.closeChannel();
    }
}
