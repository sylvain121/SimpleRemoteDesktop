package com.example.esme7383.myapplication;

import android.util.Log;

import com.google.common.io.LittleEndianDataInputStream;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Arrays;


/**
 * Created by user on 7/31/17.
 */


public class DataManager {
    private static DataManager instance = null;
    private DataInputStream dis;
    private Socket socket;
    private OutputStream outpout;
    byte[] net_in = new byte[0];
    private byte[] frame =new byte[0];
    private boolean isFirstNal = true;
    public static final String TAG = "DATAMANAGER";


    public void connect(String hostname , int port) {
        try {
            InetAddress ip = InetAddress.getByName(hostname);
            Log.v("DataManager", "Connecting to socket" + hostname+":"+port);
            socket = new Socket(ip, port);

            outpout = socket.getOutputStream();
            dis = new DataInputStream(new BufferedInputStream(socket.getInputStream(),8192));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private DataManager() {

    }

    private void startStequence() {


    }

    public static DataManager getInstance() {
        if(instance == null) {
            instance = new DataManager();
        }
        return instance;
    }

    public byte[] receive() {

        try {
            long headerTime = System.currentTimeMillis();
            int frameLength = dis.readInt();
            Log.d("TCP DATA", "get header time : "+(System.currentTimeMillis() - headerTime));
            frame = new byte[frameLength];
            dis.readFully(frame);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return frame;
    }

    public void sendStartStream(int width, int height, int fps, int codec_width, int codec_height, int bandwidth) {
        try {
            outpout.write(Message.startStream(width, height, fps, codec_width, codec_height, bandwidth).toBytes());
            outpout.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void sendMouseMotion(int x, int y) {
        try {
            outpout.write(Message.mouseMove(x, y).toBytes());
            outpout.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMouseButton(String buttonName, boolean isPressed) {
        if(isPressed) {
            try {
                outpout.write(Message.mouseButtonDown(buttonName).toBytes());
                outpout.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            try {
                outpout.write(Message.mouseButtonUp(buttonName).toBytes());
                outpout.flush();
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
