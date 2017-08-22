package com.example.esme7383.myapplication;

import android.provider.ContactsContract;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;

/**
 * Created by ESME7383 on 02/08/2017.
 */

class UserEventManager {

    public static String TAG = "EVENT LISTENER";
    private int previousButtonState = 0;

    private int leftMask = 1;
    private int rightMask = 2;
    private Boolean preLeft = false;
    private Boolean prevRight = false;


    public boolean genericMouseHandler(MotionEvent event) {
        boolean left = (event.getButtonState() & leftMask) == leftMask;
        boolean right = (event.getButtonState() & rightMask) == rightMask;

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_HOVER_MOVE:
            case MotionEvent.ACTION_MOVE:
                Log.d(TAG, "left : " + left + " right : " + right);

                if (isMouseButtonStateChange(left, preLeft)) {
                    preLeft = left;
                    sendMouseButtonUpdate("left", left);
                } else if (isMouseButtonStateChange(right, prevRight)) {
                    prevRight = right;
                    sendMouseButtonUpdate("right", right);
                } else {
                    sendMousePosition(event.getX(), event.getY());
                }
                break;
        }

        return true;
    }

    private void sendMousePosition(float fx, float fy) {
        int x = Math.round(fx);
        int y = Math.round(fy);

        Log.d(TAG, "X : " + x + " Y : " + y);
        DataManagerCHannel.getInstance().sendMouseMotion(x, y);
    }

    private void sendMouseButtonUpdate(String buttonName, boolean isPressed) {
        Log.d(TAG, "send mouse button update " + buttonName + ": " + isPressed);
        DataManagerCHannel.getInstance().sendMouseButton(buttonName, isPressed);
    }

    private boolean isMouseButtonStateChange(boolean mouseButton, Boolean previousMouseButtonState) {
        if (mouseButton != previousMouseButtonState) return true;
        return false;
    }

    public boolean onTouchHandler(MotionEvent event) {
        sendMousePosition(event.getX(), event.getY());
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                sendMouseButtonUpdate("left", true);
                break;
            case MotionEvent.ACTION_UP:
                sendMouseButtonUpdate("left", false);
                break;
        }
        return true;
    }
}
