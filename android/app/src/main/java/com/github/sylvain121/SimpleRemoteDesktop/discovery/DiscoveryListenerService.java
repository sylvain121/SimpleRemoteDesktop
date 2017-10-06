package com.github.sylvain121.SimpleRemoteDesktop.discovery;

import android.util.Log;

import com.github.sylvain121.SimpleRemoteDesktop.MainActivity;

import java.io.IOException;
import java.net.*;
import java.util.Iterator;

public class DiscoveryListenerService implements Runnable {

    private final int port;
    private final MainActivity activity;
    private DatagramSocket serverSocket;

    public DiscoveryListenerService(MainActivity mainActivity, int port) {
        this.port = port;
        this.activity = mainActivity;
    }

    @Override
    public void run() {
        try {
            serverSocket = new DatagramSocket(port);
            byte[] receiveData = new byte[20]; //TOdO how to fix length ? same on server ?

            Log.d("DISCOVERY", "Listening on udp : "+InetAddress.getLocalHost().getHostAddress()+":port");
            DatagramPacket receivePacket = new DatagramPacket(receiveData,
                    receiveData.length);

            while(!Thread.interrupted())
            {
                serverSocket.receive(receivePacket);
                String sentence = new String( receivePacket.getData(), 0,
                        receivePacket.getLength() );
                Log.d("DISCOVERY SERVICE", "RECEIVED: " + sentence);
                // now send acknowledgement packet back to sender
                InetAddress IPAddress = receivePacket.getAddress();
                Log.d("DISCOVERY SERVICE", "RECEIVED: " + IPAddress.getHostName());
                String sendString = "polo";
                byte[] sendData = sendString.getBytes("UTF-8");
                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length,
                        IPAddress, receivePacket.getPort());
                serverSocket.send(sendPacket);
                String[] d = sentence.split(";");
                String name = d[0];
                String os = d[1];

                boolean duplicate = false;
                Iterator<Server> i = this.activity.getServerList().iterator();
                while(i.hasNext()) {
                    Server s = i.next();
                    if(s.IPAddress.toString().equals(IPAddress.toString())) {
                        duplicate = true;
                    }
                }
                if(!duplicate) {
                    this.activity.getServerList().add(new Server(name, os, IPAddress));
                    this.activity.getHandler().sendEmptyMessage(1);
                }

            }
        } catch (IOException e) {
            Log.d("SERVICE", String.valueOf(e));
        }
        // should close serverSocket in finally block
    }


    public void close() {
        serverSocket.close();
    }
}
