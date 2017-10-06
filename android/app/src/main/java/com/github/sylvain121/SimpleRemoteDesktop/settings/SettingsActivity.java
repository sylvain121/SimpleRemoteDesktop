package com.github.sylvain121.SimpleRemoteDesktop.settings;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.github.sylvain121.SimpleRemoteDesktop.R;

import java.util.ArrayList;
import java.util.List;


public class SettingsActivity extends Activity {

    public static final String SIMPLE_REMOTE_DESKTOP_PREF_RESOLUTION = "simple.remote.desktop.pref.resolution";
    public static final String SIMPLE_REMOTE_DESKTOP_PREF_BITRATE = "simple.remote.desktop.pref.bitrate";
    public static final String SIMPLE_REMOTE_DESKTOP_PREF_FPS = "simple.remote.desktop.pref.fps";
    public static final String SIMPLE_REMOTE_DESKTOP_PREF = "simple.remote.desktop.pref";
    private SharedPreferences sharedPreference;
    private String currentResolution;
    private int currentBandwith;
    private int CurrentFps;
    private Spinner resolutiItems;
    private Spinner fpsItems;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
        setContentView(R.layout.settings);

        sharedPreference = getBaseContext().getSharedPreferences(SIMPLE_REMOTE_DESKTOP_PREF, 0);
         currentResolution = sharedPreference.getString(SIMPLE_REMOTE_DESKTOP_PREF_RESOLUTION, null);
         currentBandwith = sharedPreference.getInt(SIMPLE_REMOTE_DESKTOP_PREF_BITRATE,0);
         CurrentFps = sharedPreference.getInt(SIMPLE_REMOTE_DESKTOP_PREF_FPS,0);

        updateResolutionSpinner();
        updatefpsSpinner();
        updateBandwithInput();

    }

    private void updateBandwithInput() {
        EditText v = (EditText) findViewById(R.id.currentBandwith);
        v.setText(currentBandwith+"");
    }

    private void updateResolutionSpinner() {
        List<String> resolutionArray = new ArrayList<String>();
        resolutionArray.add("600p");
        resolutionArray.add("720p");
        resolutionArray.add("1080p");
         //resolutionArray.add("original"); FIXME code height and width passed to decoder ?

        ArrayAdapter<String> resolutionAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, resolutionArray);
        resolutionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        resolutiItems = (Spinner) findViewById(R.id.resolutionSpinner);
        resolutiItems.setAdapter(resolutionAdapter);

        if (currentResolution != null) {
            int spinnerPosition = resolutionAdapter.getPosition(currentResolution);
            resolutiItems.setSelection(spinnerPosition);
        }
    }

    private void updatefpsSpinner() {
        List<String> fpsArray = new ArrayList<String>();
        fpsArray.add("24");
        fpsArray.add("30");
        fpsArray.add("60");
        ArrayAdapter<String> fpsAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, fpsArray);
        fpsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        fpsItems = (Spinner) findViewById(R.id.sprinnerFrameRate);
        fpsItems.setAdapter(fpsAdapter);

        String fpsString = String.valueOf(CurrentFps);
        if (fpsString != null) {
            int spinnerPosition = fpsAdapter.getPosition(fpsString);
            fpsItems.setSelection(spinnerPosition);
        }
    }

    public void onCancelClick(View view) {
        finish();
    }

    public void onSaveClick(View view) {
        EditText et = (EditText) findViewById(R.id.currentBandwith);
        String bString = et.getText().toString();
        int bandwith = Integer.parseInt(bString);

        sharedPreference
                .edit()
                .putString(SIMPLE_REMOTE_DESKTOP_PREF_RESOLUTION, resolutiItems.getSelectedItem().toString())
                .putInt(SIMPLE_REMOTE_DESKTOP_PREF_BITRATE, bandwith)
                .putInt(SIMPLE_REMOTE_DESKTOP_PREF_FPS, Integer.parseInt(fpsItems.getSelectedItem().toString()))
                .apply();

        Toast.makeText(this, "saved", Toast.LENGTH_SHORT);
        finish();

    }
}
