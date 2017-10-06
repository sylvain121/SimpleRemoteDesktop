package com.github.sylvain121.SimpleRemoteDesktop;

import android.app.ListActivity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.View;
import android.widget.ListView;

import com.github.sylvain121.SimpleRemoteDesktop.discovery.DiscoveryListenerService;
import com.github.sylvain121.SimpleRemoteDesktop.discovery.Server;
import com.github.sylvain121.SimpleRemoteDesktop.discovery.ServerAdapter;
import com.github.sylvain121.SimpleRemoteDesktop.player.PlayerActivity;
import com.github.sylvain121.SimpleRemoteDesktop.settings.SettingsActivity;

import java.util.ArrayList;


public class MainActivity extends ListActivity implements SurfaceHolder.Callback{

    private ArrayList<Server> serverList = new ArrayList<>();
    private ServerAdapter serverAdapter;
    private Handler handler;
    public static final String IP_ADDRESS = "com.simpleremotedesktop.IPAddress";
    public static final String TAG = "MAIN ACTIVITY";
    private Thread discoveryThread;
    private DiscoveryListenerService discoveryService;


    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "Creating");
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
        setContentView(R.layout.activity_main);
        serverAdapter = new ServerAdapter(this, R.layout.listserverview_item_row, serverList);
        setListAdapter(serverAdapter);

         handler = new Handler() {
             @Override
             public void handleMessage(android.os.Message msg) {
                 super.handleMessage(msg);
                 Log.d("HANDLER", "handle message called ");
                 serverAdapter.notifyDataSetChanged();
             }
         };
        discoveryService = new DiscoveryListenerService(this, 8002);
        discoveryThread = new Thread(discoveryService);
        discoveryThread.start();



    }

    @Override
    protected void onListItemClick (ListView l, View v, int position, long id) {
        Server server = serverList.get(position);
        Intent intent = new Intent(this, PlayerActivity.class);
        intent.putExtra(IP_ADDRESS, server.IPAddress.getHostName());
        Log.d("MAIN_ACTIVITY", "start session for address : "+server.IPAddress.getHostName());
        startActivity(intent);
    }



    public ArrayList<Server> getServerList() {
        return serverList;
    }


    public Handler getHandler() {
        return handler;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        serverAdapter.notifyDataSetChanged();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        serverAdapter.notifyDataSetChanged();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "Activity destroyed");
        discoveryThread.interrupt();
        discoveryService.close();
    }

    public void onSettingsClickListener(View view) {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }
}

