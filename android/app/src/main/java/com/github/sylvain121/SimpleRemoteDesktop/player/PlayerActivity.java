package com.github.sylvain121.SimpleRemoteDesktop.player;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.hardware.input.InputManager;
import android.os.Bundle;
import android.util.Log;
import android.view.InputDevice;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.github.sylvain121.SimpleRemoteDesktop.MainActivity;
import com.github.sylvain121.SimpleRemoteDesktop.player.video.MediaCodecDecoderRenderer;
import com.github.sylvain121.SimpleRemoteDesktop.settings.SettingsActivity;

public class PlayerActivity extends Activity implements SurfaceHolder.Callback, InputManager.InputDeviceListener {

    private UserEventManager userEventManager;
    private String TAG = "PLAYER ACTIVITY";
    private String IPAddress;
    private boolean MouseIsPresent = false;
    private MediaCodecDecoderRenderer mediaCodec;
    private ConnectionThread cnx;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                        | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                        | View.SYSTEM_UI_FLAG_IMMERSIVE);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        SurfaceView sv = new SurfaceView(this);
        sv.getHolder().addCallback(this);
        userEventManager = new UserEventManager();
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setContentView(sv);
        sv.setOnGenericMotionListener(new View.OnGenericMotionListener() {
            @Override
            public boolean onGenericMotion(View v, MotionEvent event) {
                Log.d(TAG, "event : "+event.getButtonState());
                return userEventManager.genericMouseHandler(event);

            }
        });

        sv.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Log.d(TAG, "touch event : "+event.getButtonState());
                if(event.getDevice().getSources() != InputDevice.SOURCE_MOUSE) {
                    return userEventManager.onTouchHandler(event);
                } else {
                    return userEventManager.genericMouseHandler(event);
                }

            }
        });

        Intent intent = getIntent();
        this.IPAddress = intent.getStringExtra(MainActivity.IP_ADDRESS);

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.d("SURFACE", "SURFACE CREATED");
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Log.d(TAG, "width : "+width+ "height : "+height);

        SharedPreferences sharedPreference = getBaseContext().getSharedPreferences(SettingsActivity.SIMPLE_REMOTE_DESKTOP_PREF, 0);

        mediaCodec = new MediaCodecDecoderRenderer();
        mediaCodec.setRenderTarget(holder);
        mediaCodec.setup(width, height);
        mediaCodec.start();

        cnx = new ConnectionThread(width, height, this.IPAddress, sharedPreference);
        cnx.setDecoderHandler(mediaCodec);
        cnx.start();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.d("SURFACE", "SURFACE DESTROYED");
    }

    @Override
    public void onInputDeviceAdded(int deviceId) {
        InputDevice device = InputDevice.getDevice(deviceId);
        if(device.getSources() == InputDevice.SOURCE_MOUSE) {
            Log.d(TAG, "Mouse plugged");
            this.MouseIsPresent = true;
        }
    }

    @Override
    public void onInputDeviceRemoved(int deviceId) {
        InputDevice device = InputDevice.getDevice(deviceId);
        if(device.getSources() == InputDevice.SOURCE_MOUSE) {
            Log.d(TAG, "Mouse Changed");

        }
    }

    @Override
    public void onInputDeviceChanged(int deviceId) {
        InputDevice device = InputDevice.getDevice(deviceId);
        if(device.getSources() == InputDevice.SOURCE_MOUSE) {
            Log.d(TAG, "Mouse Unplugged");
            this.MouseIsPresent = false;
        }
    }
}