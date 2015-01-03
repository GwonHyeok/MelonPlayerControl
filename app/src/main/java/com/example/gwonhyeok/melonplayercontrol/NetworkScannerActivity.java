package com.example.gwonhyeok.melonplayercontrol;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.math.BigInteger;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteOrder;
import java.util.ArrayList;

/**
 * Created by GwonHyeok on 2014. 7. 2..
 */
public class NetworkScannerActivity extends ActionBarActivity implements ListView.OnItemClickListener {
    private ArrayList<String> IP_List;
    private TextView Scanning_tv;
    private ProgressBar progressBar;
    private ListView Scaned_listview;
    private boolean RUNNING = false;
    private Scanip scan;

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_networkscan);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        Scanning_tv = (TextView) findViewById(R.id.Scanning_tv);
        progressBar.setIndeterminate(true);
        IP_List = new ArrayList<String>();
        Scaned_listview = (ListView) findViewById(R.id.Scaned_listview);
        Scaned_listview.setAdapter(new CustomAdapter(this, android.R.layout.simple_list_item_1, IP_List));
        Scaned_listview.setOnItemClickListener(this);
        scan = new Scanip();
        scan.execute();

    }

    final Handler refreshlistview = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Scaned_listview.invalidateViews();
        }
    };

    private class CustomAdapter extends ArrayAdapter {

        public CustomAdapter(Context context, int resource, ArrayList<String> objects) {
            super(context, resource, objects);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.network_activity_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        Intent intent = new Intent(this, MyActivity.class);
        intent.putExtra("IP", IP_List.get(i));
        startActivity(intent);
        RUNNING = true;
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.action_restart:
                Log.d("MENU", "START");
                RUNNING = true;
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        scan = null;
                        scan = new Scanip();
                        scan.execute();
                    }
                }, 1000);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    class Scanip extends AsyncTask<String, Integer, ArrayList<String>> {
        Animation inanim = AnimationUtils.loadAnimation(NetworkScannerActivity.this, android.R.anim.fade_in);
        Animation inanim_infinite = AnimationUtils.loadAnimation(NetworkScannerActivity.this, android.R.anim.fade_in);
        Animation outanim = AnimationUtils.loadAnimation(NetworkScannerActivity.this, android.R.anim.fade_out);

        @Override
        public void onPreExecute() {
            IP_List.clear();
            refreshlistview.sendEmptyMessage(0);

            inanim_infinite.setRepeatCount(Animation.INFINITE);
            inanim_infinite.setDuration(1500);
            Scanning_tv.startAnimation(inanim_infinite);

            progressBar.startAnimation(inanim);
            Scanning_tv.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.VISIBLE);
            RUNNING = false;
        }

        @Override
        protected void onPostExecute(ArrayList<String> list) {
            Scanning_tv.startAnimation(outanim);
            progressBar.startAnimation(outanim);
            Scanning_tv.setVisibility(View.GONE);
            progressBar.setVisibility(View.GONE);

            for (String ip : list) {
                Log.d("FIN", ip);
            }
        }

        @Override
        protected ArrayList<String> doInBackground(String... strings) {
            ArrayList<String> list = new ArrayList<String>();

            try {
                WifiManager wm = (WifiManager) getSystemService(WIFI_SERVICE);
                int ipAddress = (wm.getConnectionInfo().getIpAddress());
                if (ByteOrder.nativeOrder().equals(ByteOrder.LITTLE_ENDIAN)) {
                    ipAddress = Integer.reverseBytes(ipAddress);
                }
                byte[] ipByteArray = BigInteger.valueOf(ipAddress).toByteArray();
                final String ipAddressString = InetAddress.getByAddress(ipByteArray).getHostAddress();
                Log.d("IP", ipAddressString);

                String[] ip_component = ipAddressString.split("\\.");
                String subnet = ip_component[0] + "." + ip_component[1] + "." + ip_component[2] + ".";

                int timeout = 500;

                for (int i = 1; i < 254; i++) {
                    if (RUNNING) {
                        return list;
                    }
                    String host = subnet + i;
                    if (InetAddress.getByName(host).isReachable(timeout)) {
                        try {
                            Socket checksocket = new Socket();
                            checksocket.connect(new InetSocketAddress(host, 5719), 1000);
                            checksocket.close();
                            IP_List.add(host);
                            refreshlistview.sendEmptyMessage(0);
                        } catch (Exception e) {
                        }
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
            return list;
        }
    }
}