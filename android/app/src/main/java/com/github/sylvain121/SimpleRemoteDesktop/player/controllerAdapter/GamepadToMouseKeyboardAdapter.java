package com.github.sylvain121.SimpleRemoteDesktop.player.controllerAdapter;

import android.graphics.Point;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;

import com.github.sylvain121.SimpleRemoteDesktop.player.video.DataManagerChannel;

/**
 * Created by sylvain on 08/12/17.
 */

public class GamepadToMouseKeyboardAdapter implements ControlAdapter {

    public static String TAG = "GAMEPAD_TO_MOUSE_KEYBOARD";

    private final static int KEY_1 = 38;
    private final static int KEY_2 = 0; //TODO
    private final static int KEY_3 = 34;
    private final static int KEY_4 = 39;
    private final static int KEY_Q = 113;
    private final static int KEY_I = 105;
    private final static int KEY_M = 109;
    private final static int KEY_T = 116;
    private final static int KEY_CTRL = 65507;
    private int width;
    private int height;
    private int middleWidth;
    private int middleHeight;
    private int runTrigger = 80;


    @Override
    public String getName() {
        return "Gamepad adapter for diablo3";
    }

    @Override
    public boolean onGenericMotion(MotionEvent event) {
        Point position = convertToPointOnScreen(event.getX(), event.getY());
        DataManagerChannel.getInstance().sendMouseMotion(position.x, position.y);
        if(thresholdTrigged(position)){
            sendMouseButtonUpdate("left", event.getAction() == KeyEvent.ACTION_DOWN);
        }
        return true;
    }

    private boolean thresholdTrigged(Point position) {
        //TODO
        return false;
    }

    private Point convertToPointOnScreen(float x, float y) {
        Log.d(TAG, "pointer event value x : "+x+" y : "+y);
        float px = x * this.middleWidth + this.middleWidth;;
        float py = y * this.middleHeight + this.middleHeight;;
        Log.d(TAG, "x : "+(int) px+" y : "+(int) py);
        return new Point((int) px, (int) py);
    }

    @Override
    public boolean onGenericKeyEvent(KeyEvent event) {
        int keycode = getKeycodeFromDpadKey(event.getKeyCode());
        if(keycode == 999) {
            sendMouseButtonUpdate("left", event.getAction() == KeyEvent.ACTION_DOWN);
            return true;
        }
        if(keycode == 1000) {
            sendMouseButtonUpdate("right", event.getAction() == KeyEvent.ACTION_DOWN);
            return true;
        }
        switch(event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                DataManagerChannel.getInstance().sendKeyDown(keycode);
                return true;
            case MotionEvent.ACTION_UP:
                DataManagerChannel.getInstance().sendKeyUp(keycode);
                return true;
        }
        return false;
    }

    private int getKeycodeFromDpadKey(int keyCode) {
        switch(keyCode){
            case KeyEvent.KEYCODE_BUTTON_L2:
                 return 999; //FIXME ugly for mouse left button
            case KeyEvent.KEYCODE_BUTTON_L1:
                return 113; // key_Q
            case KeyEvent.KEYCODE_BUTTON_R2:
                return 1000; // fixme ugly for mouse right button
            case KeyEvent.KEYCODE_BUTTON_R1:
                return 65507; // ctrl button
            case KeyEvent.KEYCODE_BUTTON_A:
                return 38; // key_1;
            case KeyEvent.KEYCODE_BUTTON_X:
                return 0; //Key_2 but crash server
            case KeyEvent.KEYCODE_BUTTON_Y:
                return 34; //key_3
            case KeyEvent.KEYCODE_BUTTON_B:
                return 39; //key_4
            case KeyEvent.KEYCODE_BUTTON_SELECT:
                break;
            case KeyEvent.KEYCODE_BUTTON_START:
                return 116; // key_T
        }
        return 0;
    }

    @Override
    public boolean onGenericTouchEvent(MotionEvent event) {
        return false;
    }

    @Override
    public void onSurfaceChange(int width, int height) {

        this.width = width;
        this.height = height;
        Log.d(TAG, "screen width = "+this.width+" screen height = "+this.height);
        setMouseData();
    }

    private void setMouseData() {
        this.middleWidth = this.width / 2;
        this.middleHeight = this.height / 2;
        Log.d(TAG, "width middle = "+this.middleWidth+" height middle = "+this.middleHeight);


    }

    private void sendMouseButtonUpdate(String buttonName, boolean isPressed) {
        Log.d(TAG, "send mouse button update " + buttonName + " isPressed ?: " + isPressed);
        DataManagerChannel.getInstance().sendMouseButton(buttonName, isPressed);
    }
}
