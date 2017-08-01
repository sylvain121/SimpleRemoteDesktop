package com.example.SimpleRemoteDesktop;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.media.MediaCodec;
import android.media.MediaFormat;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class MainActivity extends Activity implements SurfaceHolder.Callback, View.OnGenericMotionListener {
    private DisplayThread displayThread = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SurfaceView sv = new SurfaceView(this);
        sv.getHolder().addCallback(this);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setContentView(sv);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        if (displayThread == null) {
            displayThread = new DisplayThread(holder.getSurface());
            displayThread.start();
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }

    @Override
    public boolean onGenericMotion(View v, MotionEvent event) {
        Log.v("ACTIVITY", "EVENT EVENT");
        return true;
    }

    private class DisplayThread extends Thread {
        private MediaCodec codec;
        private Surface surface;
        private Receiver m_renderSock;


        public DisplayThread(Surface surface) {
            this.surface = surface;
        }

        @Override
        public void run() {
            m_renderSock = new Receiver();

            //Configuring Media Decoder
            try {
                codec = MediaCodec.createDecoderByType("video/avc");
            } catch (IOException e) {
                throw new RuntimeException(e.getMessage());
            }

            MediaFormat format = MediaFormat.createVideoFormat("video/avc", 800, 600);

            codec.configure(format, surface, null, 0);
            codec.start();


            while (!Thread.interrupted()) {
                int frameSize = 0;
                byte[] frameData = m_renderSock.receive();

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

                int inIndex = codec.dequeueInputBuffer(10000);
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
    }
}