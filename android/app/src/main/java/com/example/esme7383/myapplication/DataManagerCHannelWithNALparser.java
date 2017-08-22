package com.example.esme7383.myapplication;

import android.util.Log;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.nio.channels.SocketChannel;
import java.util.Arrays;


/**
 * Created by user on 7/31/17.
 */


public class DataManagerCHannelWithNALparser {
    private static DataManagerCHannelWithNALparser instance = null;
    private static ByteBuffer buf = ByteBuffer.allocate(512*1024);
    byte[] net_in = new byte[0];
    private boolean isFirstNal = true;
    public static final String TAG = "DATAMANAGER CHANNEL";
    private SocketChannel chan;
    private byte[] frame;
    private OutputStream output;


    public void connect(String hostname , int port) {
        try {

            Log.v("DataManager", "Connecting to socket" + hostname+":"+port);
            final InetSocketAddress socketAddr = new InetSocketAddress(hostname, port);
            chan = SocketChannel.open();
            chan.connect(socketAddr);
            output = chan.socket().getOutputStream();
            buf.limit (0);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private DataManagerCHannelWithNALparser() {

    }

    private void startStequence() {


    }

    public static DataManagerCHannelWithNALparser getInstance() {
        if(instance == null) {
            instance = new DataManagerCHannelWithNALparser();
        }
        return instance;
    }

    public byte[] receive() {

        try {
            long startTime = System.currentTimeMillis();
            ensure(4, chan);
            Log.d("VIDEO DECODER THREAD", "get body time : "+(System.currentTimeMillis() - startTime));
            int len = buf.getInt();
            ensure(len, chan);
            frame = new byte[len];
            buf.get(frame, 0, len);

        } catch (IOException e) {
            e.printStackTrace();
        }

        return frame;
    }

    private static void ensure (int len, ByteChannel chan) throws IOException
    {
        int test = buf.remaining();
        if (buf.remaining () < len) {
            buf.compact ();
            buf.flip ();
            do {
                buf.position (buf.limit ());
                buf.limit (buf.capacity ());
                chan.read (buf);
                buf.flip ();
            } while (buf.remaining() < len);
        }
    }


    public void sendStartStream(int width, int height, int fps, int codec_width, int codec_height, int bandwidth) {
        try {
            output.write(Message.startStream(width, height, fps, codec_width, codec_height, bandwidth).toBytes());
            output.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void sendMouseMotion(int x, int y) {
        try {
            output.write(Message.mouseMove(x, y).toBytes());
            output.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMouseButton(String buttonName, boolean isPressed) {
        if(isPressed) {
            try {
                output.write(Message.mouseButtonDown(buttonName).toBytes());
                output.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            try {
                output.write(Message.mouseButtonUp(buttonName).toBytes());
                output.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    private void bytesToHex(byte[] bytes, int length) {
        StringBuilder sb = new StringBuilder();
        String result = "";
        for (int i=0; i < length; i++) {
            sb.append(String.format("%02X ", bytes[i]));
            result +=" ";
            result +=sb.toString();
        }
        Log.d(this.getClass().getName(), result+"\n");
    }

    private byte[] NALparser() {
        for(int i = 0; i < net_in.length - 5; i++) {
            if(nalStartCodeDected(i)) {
                if(!isPPS(i)) {
                    if(isFirstNal) {
                        isFirstNal = false;
                    } else {
                        isFirstNal = true;

                        return detachFrameFromBuffer(i);
                    }

                }
            }
        }
        return new byte[0];
    }

    private byte[] detachFrameFromBuffer(int i) {
        frame = Arrays.copyOf(net_in, i);
        net_in = Arrays.copyOfRange(net_in, i, net_in.length);
        return frame;
    }

    private boolean isPPS(int i) {
        return net_in[i+4] == 0x68;
    }

    private boolean nalStartCodeDected(int i) {
        return net_in[i] == 0x00 && net_in[i+1] == 0x00 && net_in[i+2] == 0x00 && net_in[i+3]  == 0x01;
    }

    private void addToNetworkBuffer(byte[] dataAvaibleLength) {
        byte[] net_temp = new byte[dataAvaibleLength.length + net_in.length];
        System.arraycopy(net_in, 0, net_temp, 0, net_in.length);
        System.arraycopy(dataAvaibleLength, 0, net_temp,net_in.length, dataAvaibleLength.length);
        net_in = net_temp;
    }
}
