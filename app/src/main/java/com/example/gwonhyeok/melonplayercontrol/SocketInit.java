package com.example.gwonhyeok.melonplayercontrol;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Message;
import android.util.Log;

import com.hyeok.melon.Exception.GetSongDataException;
import com.hyeok.melon.MelonSong;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.URL;

/**
 * Created by GwonHyeok on 2014. 6. 30..
 */
public class SocketInit {
    private final String IP = "192.168.0.9";
    private final int PORT = 5719;
    private boolean IS_PLAYLIST = false;
    private static Socket socket = null;
    private static BufferedReader bufferedInputStream;
    private static BufferedOutputStream bufferedOutputStream;
    private Context mContext;

    public SocketInit(String IP, int PORT, Context mContext) throws IOException {
        if (socket != null) socket.close();
        socket = new Socket(IP, PORT);
        bufferedInputStream = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        bufferedOutputStream = new BufferedOutputStream(socket.getOutputStream());
        this.mContext = mContext;
        GetCommand();

    }

    public boolean SendCommand(String command) {
        try {
            bufferedOutputStream.write(command.getBytes());
            bufferedOutputStream.write(System.getProperty("line.separator").getBytes());
            bufferedOutputStream.flush();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            CloseConnection();
            return false;
        }
    }

    public boolean CheckConnection() {
        try {
            bufferedOutputStream.write(System.getProperty("line.separator").getBytes());
            bufferedOutputStream.flush();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private void CloseConnection() {
        try {
            bufferedOutputStream.close();
        } catch (Exception e) {
        }
    }

    private void setAlbumartfromURL(final String albumartURL) {
        new Thread() {
            @Override
            public void run() {
                try {
                    HttpURLConnection connection = (HttpURLConnection) new URL(albumartURL).openConnection();
                    Bitmap bitmap = null;
                    bitmap = BitmapFactory.decodeStream(connection.getInputStream());
                    Message bitmapMessage = new Message();
                    bitmapMessage.obj = bitmap;
                    MyActivity.ImageViewUpdate.sendMessage(bitmapMessage);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    private void setAlbumartfromSID(final String SID) {
        new Thread() {
            @Override
            public void run() {
                try {
                    MelonSong melonSong = new MelonSong(MemberInfo.getInstance().getKeyCookie());
                    melonSong.getSongData(SID);
                    HttpURLConnection connection = (HttpURLConnection) new URL(melonSong.getAlbumArtURL()).openConnection();
                    Bitmap bitmap = null;
                    bitmap = BitmapFactory.decodeStream(connection.getInputStream());
                    Message bitmapMessage = new Message();
                    Message singerMessage = new Message();
                    Message songMessage = new Message();

                    bitmapMessage.obj = bitmap;
                    songMessage.obj = melonSong.getSongName();
                    singerMessage.obj = melonSong.getSingerName();
                    MyActivity.ImageViewUpdate.sendMessage(bitmapMessage);
                    MyActivity.SingerViewupdate.sendMessage(singerMessage);
                    MyActivity.SongViewupdate.sendMessage(songMessage);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (GetSongDataException e) {
                    e.printStackTrace();
                }
            }
        }.start();

    }

    public synchronized void GetCommand() {
        new Thread() {
            @Override
            public void run() {
                try {
                    while (true) {
                        final String msg = SocketInit.bufferedInputStream.readLine();
                        if (msg == null) {
                            MyActivity.SocketDie.sendEmptyMessage(0);
                            return;
                        }
                        if (!msg.isEmpty()) {
                            Log.d("TAG", msg);
                            // get AlbumArtData
                            if (msg.contains("[ALBUMART]")) {
                                String url = msg.split("]")[1];
                                setAlbumartfromURL(url);
                            } else if (msg.contains("[KEYCOOKIE]")) {
                                String keyCookie = msg.split("]")[1];
                                MemberInfo.getInstance().setKeyCookie(keyCookie);
                            } else if (msg.contains("[SID]")) {
                                String sid = msg.split("]")[1];
                                setAlbumartfromSID(sid);
                            } else if (msg.contains("[SINGER]")) {
                                String singer = msg.split("]")[1];
                                Message singermessage = new Message();
                                singermessage.obj = singer;
                                MyActivity.SingerViewupdate.sendMessage(singermessage);
                            } else if (msg.contains("[SONG]")) {
                                String song = msg.split("]")[1];
                                Message songmessage = new Message();
                                songmessage.obj = song;
                                MyActivity.SongViewupdate.sendMessage(songmessage);
                            } else if (msg.contains("[VOL]")) {
                                int volume = Integer.parseInt(msg.split("]")[1]);
                                Message volmeessage = new Message();
                                volmeessage.arg1 = volume;
                                MyActivity.VolSeekbarupdate.sendMessage(volmeessage);
                            } else if (msg.equals("[REFRESH_PLAYLIST]")) {
                                mContext.sendBroadcast(new Intent(PlaylistActivity.REFRESH_LIST_BROADCAST));
                            } else if (msg.contains("[PLAYLIST]")) {
                                Log.d("TAG", "START");
                                String listdata = msg.replace("[PLAYLIST]", "");
                                JSONParser jsonParser = new JSONParser();
                                try {
                                    JSONArray jsonArray = (JSONArray) jsonParser.parse(listdata);
                                    for (int i = 0; i < jsonArray.size(); i++) {
                                        String id = ((JSONObject) jsonArray.get(i)).get("id").toString();
                                        String sid = ((JSONObject) jsonArray.get(i)).get("sid").toString();
                                        String singer = ((JSONObject) jsonArray.get(i)).get("singer").toString();
                                        String song = ((JSONObject) jsonArray.get(i)).get("song").toString();
                                        String albumart = ((JSONObject) jsonArray.get(i)).get("albumart").toString();
                                        indexSearchData searchData = new indexSearchData(Integer.parseInt(id),
                                                song, sid, albumart, singer);
                                        PlaylistActivity.PLAY_LIST_LIST.add(searchData);
                                    }
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                                PlaylistActivity.FINISH_WORK = true;
                            }
                        }
                    }
                } catch (IOException e) {
                    interrupt();

                    e.printStackTrace();

                }

            }
        }.start();

    }
}
