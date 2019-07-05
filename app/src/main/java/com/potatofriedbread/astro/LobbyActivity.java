package com.potatofriedbread.astro;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.TableLayout;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class LobbyActivity extends AppCompatActivity {

    private GridView gridView;
    private Toolbar toolbar;
    private Button addRoom;
    private GameController gameController;
    private ArrayList<Map<String, Object>> mData;
    private SimpleAdapter mRoomListAdapter;

    private Timer timer = new Timer();
    private TimerTask task;

    private static String[] RoomState = {"等待中", "游戏中"};

    private SharedPreferences settings;
    private AlertDialog.Builder dialogBuilder;
    private AlertDialog alertDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lobby);
        gridView = findViewById(R.id.gridview);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(false);

        mData = new ArrayList<>();

        gameController = GameController.getInstance();
        gameController.pushContext(this);

        //加载资源
        try {
            gameController.initGameController();
        } catch (Exception e){
            e.printStackTrace();
            Log.d("TEST Choreographer", "Fail to initialize game.");
        }

        //创建房间
        addRoom = findViewById(R.id.addRoom);
        addRoom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(LobbyActivity.this,"addRoom",Toast.LENGTH_SHORT).show();
                Intent intent = new Intent();
                Bundle bundle = new Bundle();
                bundle.putString("hostIP",NetUtils.getLocalHostIp());
                bundle.putString("nickname", settings.getString("player_name_key", "大猪仔"));
                bundle.putBoolean("isHost", true);
                intent.setAction("com.potatofriedbread.astro.room");
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });


        new udpReceive(handler_for_udpReceive).start();

        task = new TimerTask() {
            @Override
            public void run() {
                Message message = new Message();
                message.what = Value.msg_list_clear;
                handler_for_udpReceive.sendMessage(message);
            }
        };
        timer.schedule(task, 0, 2500);

        mRoomListAdapter = new SimpleAdapter(this, mData,
                R.layout.grid_item_list, new String[]{"roomName", "roomState", "roomPeople"},
                new int[]{R.id.roomName, R.id.roomState, R.id.roomPeople});
        gridView.setAdapter(mRoomListAdapter);

        // 监听gridView item
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent();
                Bundle bundle = new Bundle();
                bundle.putString("hostIP",mData.get(position).get("hostIP").toString());
                bundle.putString("nickname", settings.getString("player_name_key", "大猪仔"));
                bundle.putBoolean("isHost",false);
                intent.setAction("com.potatofriedbread.astro.room");
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });
        settings = getSharedPreferences("setting", 0);
    }

    @Override
    public void onRestart(){
        super.onRestart();
        Log.e("TAG","调用了onRestart");
        timer = new Timer();
        task = new TimerTask() {
            @Override
            public void run() {
                Message message = new Message();
                message.what = Value.msg_list_clear;
                handler_for_udpReceive.sendMessage(message);
            }
        };
        timer.schedule(task, 0, 2500);
    }

    @Override
    public void onPause(){
        super.onPause();
        Log.e("TAG","调用了onPause");
        task.cancel();
        timer.cancel();
    }

    // 用于接收udp广播的handler
    private final Handler handler_for_udpReceive = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch(msg.what){
                case Value.msg_list_clear:
                    mData.clear();
                    mRoomListAdapter.notifyDataSetChanged();
                    System.out.println("清空");
                    break;
                /* 到udp多播的信息 */
                case Value.msg_udp_update:
                    Bundle data = msg.getData();
                    String hostIP = data.getString("hostIP");
                    String roomName = data.getString("roomName");
                    String roomState = data.getString("roomState");
                    String roomCurNum = data.getString("roomCurNum");
                    String roomCapacity = data.getString("roomCapacity");
                    Boolean exist = false;
                    Integer index = -1;
                    for(int i = 0; i < mData.size(); i++){
                        Map<String, Object> room = mData.get(i);
                        if(room.get("hostIP").equals(hostIP)){
                            exist = true;
                            index = i;
                            break;
                        }
                    }
                    if(exist){
                        // 之前有一样的就更新
                        Map<String, Object> room = mData.get(index);
                        room.put("roomName", roomName);
                        room.put("roomCurNum", roomCurNum);
                        room.put("roomCapacity", roomCapacity);
                        room.put("roomPeople", roomCurNum + "/" + roomCapacity);
                        room.put("roomState", Boolean.valueOf(roomState) ? RoomState[1] : RoomState[0]);
                    }
                    else{
                        // 之前没有一样的就插入
                        Map<String, Object> room = new HashMap<>();
                        room.put("hostIP", hostIP);
                        room.put("roomName", roomName);
                        room.put("roomCurNum", roomCurNum);
                        room.put("roomCapacity", roomCapacity);
                        room.put("roomPeople", roomCurNum + "/" + roomCapacity);
                        room.put("roomState", Boolean.valueOf(roomState) ? RoomState[1] : RoomState[0]);
                        mData.add(room);
                    }
                    mRoomListAdapter.notifyDataSetChanged();
                    break;
            }
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_game, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            case R.id.setting:
                gameController.showToastShort("setting");
                RelativeLayout settingForm = (RelativeLayout)getLayoutInflater().inflate(R.layout.content_setting, null);
                ((EditText)settingForm.findViewById(R.id.playerName)).setText(settings.getString("player_name_key", "大猪仔"));
                dialogBuilder = new AlertDialog.Builder(this);
                alertDialog = dialogBuilder.setIcon(R.drawable.red)
                        .setTitle("修改名称")
                        .setView(settingForm)
                        .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                            }
                        })
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                SharedPreferences.Editor editor = settings.edit();
                                EditText playerNameText = alertDialog.findViewById(R.id.playerName);
                                editor.putString("player_name_key", playerNameText.getText().toString());
                                editor.commit();
                            }
                        })
                        .create();
                alertDialog.show();
                /*
                gameController.showToastShort("For debug, start the game after 3s.");
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        gameController.getAudioPlayer().pauseLobbyBGM();
                        Intent intent = new Intent(LobbyActivity.this, GameActivity.class);
                        startActivity(intent);
                    }
                }, 3000);*/
                break;
            case R.id.music:
                //if (myMediaPlayer.isPlaying()){
                if (gameController.getAudioPlayer().isPlayingLobbyBGM()){
                    //myMediaPlayer.pause();
                    gameController.getAudioPlayer().pauseLobbyBGM();
                    gameController.showToastShort("pause music");
                }
                else{/*
                    myMediaPlayer.start();
                    myMediaPlayer.setLooping(true);*/
                    gameController.getAudioPlayer().playLobbyBGM();
                    gameController.showToastShort("play music");
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        gameController.popContext();
    }
}
