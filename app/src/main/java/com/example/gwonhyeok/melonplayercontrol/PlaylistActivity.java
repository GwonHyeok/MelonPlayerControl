package com.example.gwonhyeok.melonplayercontrol;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import java.util.ArrayList;

/**
 * Created by GwonHyeok on 2014. 7. 2..
 */
public class PlaylistActivity extends Activity implements AdapterView.OnItemClickListener {
    private Context mContext;
    static ArrayList<indexSearchData> PLAY_LIST_LIST;
    ListView listView;
    private ArrayAdapter adapter;
    static boolean FINISH_WORK = false;
    boolean TIME_OUT = false;

    public static final String REFRESH_LIST_BROADCAST = "com.hyeok.melonplayer.controll.refreshlist";

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.playlist_my);
        mContext = this;
        PLAY_LIST_LIST = new ArrayList<indexSearchData>();
        listView = (ListView) findViewById(R.id.playlist_listView);
        adapter = new CustomListAdapiter(mContext, R.layout.custom_listview_my, PLAY_LIST_LIST);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(this);
        updatePlayList();

        /* Register BroadeCastReceiver */
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(REFRESH_LIST_BROADCAST);
        registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                updatePlayList();
            }
        }, intentFilter);
    }

    private void updatePlayList() {
        final UpdatePlayList update = new UpdatePlayList();
        update.execute();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (update.getStatus() == AsyncTask.Status.RUNNING) {
                    TIME_OUT = true;
                    FINISH_WORK = false;
                    update.progressDialog.cancel();
                    Toast.makeText(mContext, "문제", Toast.LENGTH_SHORT).show();
                }
            }
        }, 10000);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        Log.d(getClass().getSimpleName(), "PLAYINDEX : " + PLAY_LIST_LIST.get(i).getId());
        Log.d(getClass().getSimpleName(), "SONGNAME : " + PLAY_LIST_LIST.get(i).getSongName());
        MyActivity.socketInit.SendCommand("[PLSINDEX]" + PLAY_LIST_LIST.get(i).getId());
    }

    private class CustomListAdapiter extends ArrayAdapter<indexSearchData> {
        private ArrayList<indexSearchData> objitem;

        public CustomListAdapiter(Context context, int resource, ArrayList<indexSearchData> objects) {
            super(context, resource, objects);
            objitem = objects;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (!ImageLoader.getInstance().isInited()) {
                ImageLoader.getInstance().init(ImageLoaderConfiguration.createDefault(getContext()));
            }
            if (convertView == null) {
                LayoutInflater layoutInflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = layoutInflater.inflate(R.layout.custom_listview_my, null);
            }
            TextView songTextView = (TextView) convertView.findViewById(R.id.songTextView);
            TextView artistTextView = (TextView) convertView.findViewById(R.id.singerTextView);
            ImageView albumartImageView = (ImageView) convertView.findViewById(R.id.albumartImageView);

            songTextView.setText(objitem.get(position).getSongName());
            artistTextView.setText(objitem.get(position).getSinger());
            ImageLoader.getInstance().displayImage(objitem.get(position).getAlbumart(), albumartImageView);

            return convertView;
        }
    }

    private class UpdatePlayList extends AsyncTask<String, Integer, ArrayList> {
        ProgressDialog progressDialog = new ProgressDialog(PlaylistActivity.this);

        @Override
        public void onPreExecute() {
            PLAY_LIST_LIST.clear();
            progressDialog.setIndeterminate(true);
            progressDialog.setTitle("작업중");
            progressDialog.setMessage("플레이리스트를 받아오는 중입니다");
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.show();
        }

        @Override
        protected ArrayList doInBackground(String... strings) {
            MyActivity.socketInit.SendCommand("[PLAYLIST]");
            Log.d("TAG", String.valueOf(FINISH_WORK));
            while (!FINISH_WORK) {
                if (TIME_OUT) {
                    TIME_OUT = false;
                    return null;
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(ArrayList arrayList) {
            progressDialog.cancel();
            adapter.notifyDataSetChanged();
            FINISH_WORK = false;
        }
    }

    @Override
    public void onDestroy() {
        PLAY_LIST_LIST.clear();
        FINISH_WORK = false;
        adapter.notifyDataSetChanged();
        super.onDestroy();
    }
}