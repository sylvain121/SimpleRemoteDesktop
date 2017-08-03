package com.example.esme7383.myapplication;

import android.util.Log;

import com.google.common.io.LittleEndianDataInputStream;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;


/**
 * Created by user on 7/31/17.
 */


public class DataManager {
    private static DataManager instance = null;
    private LittleEndianDataInputStream dis;
    private Socket socket;
    private OutputStream outpout;


    public void connect(String hostname , int port) {
        try {
            InetAddress ip = InetAddress.getByName(hostname);
            Log.v("DataManager", "Connecting to socket" + hostname+":"+port);
            socket = new Socket(ip, port);

            outpout = socket.getOutputStream();
            dis = new LittleEndianDataInputStream(socket.getInputStream());

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
            int frameLenght = dis.readInt();
            Log.v("DataManager", "frame length : "+frameLenght);
            byte[] frame = new byte[frameLenght];


            dis.readFully(frame);
            //bytesToHex(frame, 10);
            return frame;

        } catch (IOException e) {
            e.printStackTrace();
        }

        return new byte[0];
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
}
