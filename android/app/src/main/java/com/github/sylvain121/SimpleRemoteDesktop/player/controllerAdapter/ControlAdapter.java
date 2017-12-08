package com.github.sylvain121.SimpleRemoteDesktop.player.controllerAdapter;

import android.view.KeyEvent;
import android.view.MotionEvent;

/**
 * Created by sylvain on 08/12/17.
 */

public interface ControlAdapter {
    String getName();

    boolean onGenericMotion(MotionEvent event);

    boolean onGenericKeyEvent(KeyEvent event);

    boolean onGenericTouchEvent(MotionEvent event);

    void onSurfaceChange(int width, int height);
}
