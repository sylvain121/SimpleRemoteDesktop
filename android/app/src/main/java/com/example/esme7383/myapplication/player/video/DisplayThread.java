package com.example.esme7383.myapplication.player.video;

import android.content.SharedPreferences;
import android.media.MediaCodec;
import android.media.MediaFormat;
import android.util.Log;
import android.view.Surface;

import com.example.esme7383.myapplication.settings.SettingsActivity;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class DisplayThread extends Thread {
    private static String TAG = "MEDIACODEC THREAD";
    private final String ipAddress;
    private final int bandwidth;
    private final int fps;
    private MediaCodec codec;
    private Surface surface;
    private int width;
    private int height;
    private DataManagerChannel m_renderSock;
    private boolean threadIsRunning = true;

    private int codec_width = 1280;
    private int codec_height = 720;
    private boolean decoderStoped = false;


    public DisplayThread(Surface surface, int width, int height, String ipAddress, SharedPreferences sharedPreference) {
        this.surface = surface;
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
                codec_width = 720;
                break;
            case "1080p":
                codec_width = 1920;
                codec_height = 1080;
                break;
        }


        bandwidth = sharedPreference.getInt(SettingsActivity.SIMPLE_REMOTE_DESKTOP_PREF_BITRATE,0);
        fps = sharedPreference.getInt(SettingsActivity.SIMPLE_REMOTE_DESKTOP_PREF_FPS,0);


        Log.d(TAG, "Configuring mediacodec");
        try {
            codec = MediaCodec.createDecoderByType("video/avc");
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }

        MediaFormat format = MediaFormat.createVideoFormat("video/avc", codec_width, codec_height);

        codec.configure(format, surface, null, 0);
        Log.d(TAG, "Start media codec");
        codec.start();
    }

    @Override
    public void run() {


        m_renderSock = DataManagerChannel.getInstance();
        m_renderSock.connect(ipAddress, 8001);
        m_renderSock.sendStartStream(this.width, this.height, fps,codec_width, codec_height, bandwidth);


        while (!Thread.interrupted()) {
            int frameSize = 0;
            long startTime = System.currentTimeMillis();
            byte[] frameData = m_renderSock.receive();
            //Log.d("VIDEO DECODER THREAD", "get frame time : "+(System.currentTimeMillis() - startTime));

            //if (frameData.length == 1) // Just for the moment, to cope with the first pakets get lost because of missing ARP, see http://stackoverflow.com/questions/11812731/first-udp-message-to-a-specific-remote-ip-gets-lost
            //  continue;

            /*Edit: This part may be left out*/
            int NAL_START = 1;
            //103, 104 -> SPS, PPS  | 101 -> Data
            int id = 0;
            int dataOffset = 0;

            //Later on this will be serversided, but for now...
            //Separate the SPSPPS from the Data
            for (int i = 0; i < frameData.length - 4; i++) {
                id = frameData[i] << 24 | frameData[i + 1] << 16 | frameData[i + 2] << 8
                        | frameData[i + 3];

                if (id == NAL_START) {
                    if (frameData[i + 4] == 101) {
                        dataOffset = i;
                    }
                }
            }


            byte[] SPSPPS = Arrays.copyOfRange(frameData, 0, dataOffset);
            byte[] data = Arrays.copyOfRange(frameData, dataOffset, frameData.length);

            if (SPSPPS.length != 0) {
                int inIndex = codec.dequeueInputBuffer(100000);

                if (inIndex >= 0) {
                    ByteBuffer input = codec.getInputBuffer(inIndex);
                    input.clear();
                    input.put(SPSPPS);
                    codec.queueInputBuffer(inIndex, 0, SPSPPS.length, 16, MediaCodec.BUFFER_FLAG_CODEC_CONFIG);
                }
            }
            /*Edit end*/
            if(!decoderStoped) {

                int inIndex = codec.dequeueInputBuffer(10000); // TODO crash on  finish activity
                if (inIndex >= 0) {
                    ByteBuffer inputBuffer = codec.getInputBuffer(inIndex);
                    inputBuffer.clear();
                    //inputBuffer.put(data);
                    inputBuffer.put(frameData);
                    //codec.queueInputBuffer(inIndex, 0, data.length, 16, 0);
                    codec.queueInputBuffer(inIndex, 0, frameData.length, 16, 0);
                }

                MediaCodec.BufferInfo buffInfo = new MediaCodec.BufferInfo();
                int outIndex = codec.dequeueOutputBuffer(buffInfo, 10000);

                switch (outIndex) {
                    case MediaCodec.INFO_OUTPUT_FORMAT_CHANGED:
                        break;
                    case MediaCodec.INFO_TRY_AGAIN_LATER:
                        break;
                    case -3: //This solves it
                        break;
                    default:
                        ByteBuffer buffer = codec.getOutputBuffer(outIndex);
                        codec.releaseOutputBuffer(outIndex, true);
                }
            }


        }

        codec.release();
    }

    public void close() {
        Log.d(TAG, "Closing display thread");
        this.decoderStoped = true;
        codec.release();
        m_renderSock.closeChannel();
        interrupt();
    }
}