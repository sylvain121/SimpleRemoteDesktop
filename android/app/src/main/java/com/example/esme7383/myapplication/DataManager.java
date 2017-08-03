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


    public void connect(String hostname , int port) {
        try {
            InetAddress ip = InetAddress.getByName(hostname);
            Log.v("DataManager", "Connecting to socket" + hostname+":"+port);
            socket = new Socket(ip, port);

            OutputStream outpout = socket.getOutputStream();
            dis = new LittleEndianDataInputStream(socket.getInputStream());
//            outpout.write(Message.startStream(1280, 720, 60,1280, 720,4000000).toBytes());
//            outpout.flush();

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
            return frame;

        } catch (IOException e) {
            e.printStackTrace();
        }
        return new byte[0];
    }

}
