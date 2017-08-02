package com.example.esme7383.myapplication;

import java.nio.ByteBuffer;

/**
 * Created by user on 7/31/17.
 */
public class Message {


    public static int TYPE_KEY_DOWN = 1;
    public static int TYPE_KEY_UP = 2;
    public static int TYPE_MOUSE_MOTION= 3;
    public static int TYPE_MOUSE_DOWN = 4;
    public static int TYPE_MOUSE_UP= 5;
    public static int TYPE_ENCODER_START = 6;
    public static int TYPE_ENCODER_STOP = 7;

    private int type = 0;
    private int x = 0;
    private int y = 0;
    private int button = 0;
    private int keyCode = 0;
    private int width = 0;
    private int height = 0;
    private int codec_width = 0;
    private int codec_height = 0;
    private int bandwidth = 0;
    private int fps = 0;

    public void setType(int type) {
        this.type = type;
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }

    public void setButton(int button) {
        this.button = button;
    }

    public void setKeyCode(int keyCode) {
        this.keyCode = keyCode;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public void setCodec_width(int codec_width) {
        this.codec_width = codec_width;
    }

    public void setCodec_height(int codec_height) {
        this.codec_height = codec_height;
    }

    public void setBandwidth(int bandwidth) {
        this.bandwidth = bandwidth;
    }

    public void setFps(int fps) {
        this.fps = fps;
    }

    public static Message startStream(int width, int height, int fps, int codec_width, int codec_height, int bandwidth) {
        Message message = new Message();
        message.type = TYPE_ENCODER_START;
        message.width = width;
        message.height = height;
        message.fps = fps;
        message.codec_width = codec_width;
        message.codec_height = codec_height;
        message.bandwidth = bandwidth;

        return message;

    }

    public byte[] toBytes() {
        ByteBuffer bb = ByteBuffer.allocate(32);
        bb.putInt(type);
        bb.putInt(x);
        bb.putInt(y);
        bb.putInt(button);
        bb.putInt(keyCode);
        bb.putInt(width);
        bb.putInt(height);
        bb.putInt(codec_width);
        bb.putInt(codec_height);
        bb.putInt(bandwidth);
        bb.putInt(fps);

        return bb.array();
    }
}
