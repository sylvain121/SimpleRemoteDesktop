package com.example.esme7383.myapplication.player.video;

import android.content.SharedPreferences;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.util.Log;
import android.view.Surface;

import com.example.esme7383.myapplication.settings.SettingsActivity;

import java.io.IOException;
import java.lang.reflect.Array;
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
            case "original":
                codec_width = 0;
                codec_height = 0;
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
        format.setInteger(MediaFormat.KEY_MAX_WIDTH, codec_width);
        format.setInteger(MediaFormat.KEY_MAX_HEIGHT, codec_height);
        //format.setInteger(MediaFormat.KEY_OPERATING_RATE, Short.MAX_VALUE);

        codec.configure(format, surface, null, 0);
        codec.setVideoScalingMode(MediaCodec.VIDEO_SCALING_MODE_SCALE_TO_FIT);

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
            //Later on this will be serversided, but for now...
            //Separate the SPSPPS from the Data



/*
            if (SPSPPS.length != 0) {
                Log.d(TAG, "sps detected : profile : "+SPSPPS[5]+" level : "+SPSPPS[7]);
                int inIndex = codec.dequeueInputBuffer(100000);

                if (inIndex >= 0) {
                    ByteBuffer input = codec.getInputBuffer(inIndex);
                    input.clear();
                    SPSPPS[5] = 66;
                    SPSPPS[7] = 10;
                    input.put(SPSPPS);
                    codec.queueInputBuffer(inIndex, 0, SPSPPS.length, 16, MediaCodec.BUFFER_FLAG_CODEC_CONFIG);
                }
            }*/

            int codecFlags = 0;
            int dataoffset = 0;
            byte[] SPSPPS = null;

            if(isSPS(frameData)) {
                //hack SPS to reduce decoder buffer
                Log.d(TAG, "sps detected : profile : "+frameData[5]+" level : "+frameData[7]);
                for (int i = 4; i < frameData.length - 4; i++) {
                    if(frameData[i] == 0 && frameData[i+1] == 0 && frameData[i+2] == 1 ) {
                        if(frameData[i+3] != 103 && frameData[i+3] != 104) {
                            dataoffset = i;
                            break;
                        }
                    }
                }

                SPSPPS = Arrays.copyOfRange(frameData, 0, dataoffset);
                frameData = Arrays.copyOfRange(frameData, dataoffset, frameData.length);

                Log.d(TAG, "sps length : "+SPSPPS.length);
                Log.d(TAG, "data length : "+frameData.length);

                SPSPPS[5] = 66; //baseline
                SPSPPS[7] = 32; // level 3.2
                Log.d(TAG, "sps new : profile : "+SPSPPS[5]+" level : "+SPSPPS[7]);

            }

            /*Edit end*/
            if(!decoderStoped) {
                if(SPSPPS != null) {
                    Log.d(TAG, "Update CONFIG");
                    sendToInputBuffer(SPSPPS, MediaCodec.BUFFER_FLAG_CODEC_CONFIG);
                }

                sendToInputBuffer(frameData, 0);

                MediaCodec.BufferInfo buffInfo = new MediaCodec.BufferInfo();
                int outIndex = codec.dequeueOutputBuffer(buffInfo, 10000);

                switch (outIndex) {
                    case MediaCodec.INFO_OUTPUT_FORMAT_CHANGED:
                        Log.d(TAG, "output format changed");
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

    private void sendToInputBuffer(byte[] frameData, int codecFlags) {
        long timestampUs = System.nanoTime() / 1000;
        Log.d(TAG, "buffer number : "+codec.getInputBuffers().length);
        int inIndex = codec.dequeueInputBuffer(100000); // TODO crash on  finish activity
        if (inIndex >= 0) {
            ByteBuffer inputBuffer = codec.getInputBuffer(inIndex);
            ;
            inputBuffer.clear();
            //inputBuffer.put(data);
            inputBuffer.put(frameData);
            //codec.queueInputBuffer(inIndex, 0, data.length, 16, 0);
            codec.queueInputBuffer(inIndex, 0, frameData.length, timestampUs, codecFlags);
        }
    }

    private boolean isSPS(byte[] frameData) {
        return frameData[4] == 0x67;

    }

    public void close() {
        Log.d(TAG, "Closing display thread");
        this.decoderStoped = true;
        codec.release();
        m_renderSock.closeChannel();
        interrupt();
    }
}