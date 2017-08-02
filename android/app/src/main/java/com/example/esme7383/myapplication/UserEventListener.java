package com.example.esme7383.myapplication;

import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;

/**
 * Created by ESME7383 on 02/08/2017.
 */

class UserEventListener {
    private final SurfaceView surface;
    public static String TAG = "EVENT LISTENER";
    private int previousButtonState = 0;

    private int leftMask = 0x01;
    private int rightMask = 0x10;

    public UserEventListener(SurfaceView sv) {
        this.surface = sv;
        this.surface.setOnGenericMotionListener(new View.OnGenericMotionListener() {
            @Override
            public boolean onGenericMotion(View v, MotionEvent event) {
                Log.d(TAG, "GENERIC onGenericMotion : "+event.getAction()+" "+event.getActionMasked());
                if(event.getAction() == MotionEvent.ACTION_HOVER_MOVE) {
                    Log.d(TAG, "X: "+event.getX()+" Y: "+event.getY());
                }
                return false;
            }
        });

        this.surface.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Log.d(TAG, "Touch event : "+event.getAction());
                Log.d(TAG, "Action masked : "+event.getActionMasked());
                Log.d(TAG, "Touch button state : "+event.getButtonState());

                switch(event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        //send mouse button : 1 left, 2 right , 3 both, 4 middle
                        //TODO send
                    case MotionEvent.ACTION_UP:
                        //send mouse button : 1 left, 2 right , 3 both, 4 middle
                        //TODO send
                    case MotionEvent.ACTION_MOVE:
                        boolean left = (event.getButtonState() & leftMask) == leftMask;
                        boolean right =(event.getButtonState() & rightMask) == rightMask;

                    Log.d(TAG, "left : "+left+" right : "+right);
                }
/*
                if(event.getAction() == MotionEvent.ACTION_DOWN) {

                   Log.d(TAG, "BUTTON DOWN : "+event.getButtonState()+" "+event.getActionMasked());
                    return true;

                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    Log.d(TAG, "BUTTON UP : "+event.getButtonState()+" "+event.getActionMasked());
                    return true;
                } else if (event.getAction() == MotionEvent.ACTION_MOVE) {

                }
 */
                return false;
            }
        });
    }
}
