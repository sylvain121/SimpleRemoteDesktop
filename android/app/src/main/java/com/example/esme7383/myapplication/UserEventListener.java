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

    public UserEventListener(SurfaceView sv) {
        this.surface = sv;
        this.surface.setOnGenericMotionListener(new View.OnGenericMotionListener() {
            @Override
            public boolean onGenericMotion(View v, MotionEvent event) {
                Log.v("MOTION LISTENER", "X: "+event.getX()+" Y: "+event.getY());
                return false;
            }
        });
    }
}
