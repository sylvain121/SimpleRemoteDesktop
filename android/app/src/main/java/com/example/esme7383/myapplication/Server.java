package com.example.esme7383.myapplication;

import java.net.InetAddress;

public class Server {
    public String name;
    public String os;
    public InetAddress IPAddress;


    public Server(String name, String os, InetAddress IPAddress) {
        this.name = name;
        this.os = os;
        this.IPAddress = IPAddress;
    }
}
