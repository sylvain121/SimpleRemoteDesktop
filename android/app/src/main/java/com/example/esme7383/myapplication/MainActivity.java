package com.example.esme7383.myapplication;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends ListActivity {

    private ArrayList<Server> serverList = new ArrayList<>();
    private ServerAdapter serverAdapter;
    private Handler handler;
    public static final String IP_ADDRESS = "com.simpleremotedesktop.IPAddress";


    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
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

        new Thread(new DiscoveryListenerService(this, 8002)).start();



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
}

