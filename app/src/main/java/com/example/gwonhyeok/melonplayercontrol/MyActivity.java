package com.example.gwonhyeok.melonplayercontrol;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

public class MyActivity extends ActionBarActivity implements View.OnClickListener, SeekBar.OnSeekBarChangeListener {

    public static SocketInit socketInit;
    private Button next, prev, toggle_btn, playlist_btn;
    private ImageView albumartview;
    private TextView mSingerName, mSongName;
    public static Handler ImageViewUpdate, SingerViewupdate, SongViewupdate, VolSeekbarupdate, SocketDie;
    private SeekBar seekBar;
    private RelativeLayout mainLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my);
        getSupportActionBar().hide();
        ViewInit();
        HandlerInit();
        final String ip = getIntent().getStringExtra("IP");
        new Thread() {
            @Override
            public void run() {
                try {
                    socketInit = new SocketInit(ip, 5719, MyActivity.this);
                    socketInit.SendCommand("[INIT]");
                } catch (Exception e) {
                    finish();
                    startActivity(new Intent(getApplicationContext(), NetworkScannerActivity.class));
                    interrupt();
                }
            }
        }.start();

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.my, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == next.getId()) {
            socketInit.SendCommand("next");
        } else if (view.getId() == prev.getId()) {
            socketInit.SendCommand("prev");
        } else if (view.getId() == toggle_btn.getId()) {
            socketInit.SendCommand("toggle");
        } else if (view.getId() == playlist_btn.getId()) {
            startActivity(new Intent(this, PlaylistActivity.class));
        }
    }

    private void HandlerInit() {
        ImageViewUpdate = new Handler() {
            @Override
            public void handleMessage(Message message) {
                albumartview.setImageBitmap((Bitmap) message.obj);

                // set MainBackground color
                Bitmap bitmap = ((Bitmap) message.obj).copy(((Bitmap) message.obj).getConfig(), true);
                int width = 100;
                int height = 200;
                int[] nPixels = new int[width * height];
                bitmap.getPixels(nPixels, 0, width, 250, 250, width, height);
                Bitmap resizedbitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                resizedbitmap.setPixels(nPixels, 0, width, 0, 0, width, height);

                final RenderScript rs = RenderScript.create(MyActivity.this);
                final Allocation input = Allocation.createFromBitmap(rs, resizedbitmap, Allocation.MipmapControl.MIPMAP_NONE,
                        Allocation.USAGE_SCRIPT);
                final Allocation output = Allocation.createTyped(rs, input.getType());
                final ScriptIntrinsicBlur script = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs));
                script.setRadius(25.0f); //0.0f ~ 25.0f
                script.setInput(input);
                script.forEach(output);
                output.copyTo(resizedbitmap);
                Drawable drawable = new BitmapDrawable(resizedbitmap);
                mainLayout.setBackground(drawable);
            }
        };


        SingerViewupdate = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                mSingerName.setText(msg.obj.toString());
            }
        };

        SongViewupdate = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                mSongName.setText(msg.obj.toString());
            }
        };

        VolSeekbarupdate = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                seekBar.setProgress(msg.arg1);
            }
        };

        SocketDie = new Handler() {
            @Override
            public void handleMessage(Message message) {
                Toast.makeText(MyActivity.this, "서버와의 연결이 종료되었습니다.", Toast.LENGTH_SHORT).show();
                finish();
                startActivity(new Intent(MyActivity.this, NetworkScannerActivity.class));
            }
        };
    }

    private void ViewInit() {
        albumartview = (ImageView) findViewById(R.id.Albumart_View);
        next = (Button) findViewById(R.id.next);
        prev = (Button) findViewById(R.id.prev);
        toggle_btn = (Button) findViewById(R.id.toggle_btn);
        mSingerName = (TextView) findViewById(R.id.SingerName_tv);
        mSongName = (TextView) findViewById(R.id.SongName_tv);
        mainLayout = (RelativeLayout) findViewById(R.id.MainRelativeLayout);
        seekBar = (SeekBar) findViewById(R.id.Volume_Seekbar);
        playlist_btn = (Button) findViewById(R.id.playlist_btn);
        seekBar.setOnSeekBarChangeListener(this);
        next.setOnClickListener(this);
        prev.setOnClickListener(this);
        toggle_btn.setOnClickListener(this);
        playlist_btn.setOnClickListener(this);
    }

    /*
        Implement Volume SeekBar
     */
    @Override
    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
        socketInit.SendCommand("[VOL]" + i);
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }
}
