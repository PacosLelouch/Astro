package com.potatofriedbread.astro;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class LobbyActivity extends AppCompatActivity {

    //private Button musicButton;
    //private MediaPlayer myMediaPlayer;
    private GridView gridView;
    ArrayList<Map<String, Object>> list;
    private Toolbar toolbar;
    private Button addRoom;
    private GameController gameController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room);
        gridView = findViewById(R.id.gridview);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(false);

        gameController = GameController.getInstance();
        gameController.pushContext(this);
        //加载资源
        try {
            gameController.initGameController();
        } catch (Exception e){
            e.printStackTrace();
            Log.d("TEST Choreographer", "Fail to initialize game.");
        }
        //localPlayer = Value.RED; // 以后是在房间里选，不过可能这个要改成静态变量？

        //加载房间音乐，放initGame了。
        //myMediaPlayer = MediaPlayer.create(LobbyActivity.this, R.raw.musicplay);
        // myMediaPlayer.prepareAsync();

        //创建房间
        addRoom = findViewById(R.id.addRoom);
        addRoom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(LobbyActivity.this,"addRoom",Toast.LENGTH_SHORT).show();
            }
        });

        // 这个房间总数应该是查询得到的，这里先设成7间
        // int rooms
        int rooms = 8;
        String[] RoomState = {"游戏中", "等待中"};
        String[] states = new String[rooms];        //房间状态
        int[] pics = new int[rooms];                //房间图标
        String[] names = new String[rooms];         //房间名称
        String[] persons = new String[rooms];       //房间人数

        for(int i=0;i<rooms;i++){
            states[i] = RoomState[1];
            pics[i] = R.drawable.home;
            names[i] = "测试房间" + i;
            persons[i] = "1/4";
        }
        list = new ArrayList<Map<String, Object>>();
        for (int i = 0; i < names.length; i++) {
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("HomePic", pics[i]);
            map.put("HomeName", names[i]);
            map.put("HomePeople", persons[i]);
            map.put("HomeID", states[i]);
            list.add(map);
        }
        SimpleAdapter adapter = new SimpleAdapter(this, list,
                R.layout.grid_item_list, new String[]{"HomePic", "HomeID", "HomeName", "HomePeople"},
                new int[]{R.id.HomePic, R.id.HomeID, R.id.HomeName, R.id.HomePeople});
        gridView.setAdapter(adapter);

        // 监听gridView item
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(LobbyActivity.this,"Short Click: "+list.get(position).get("HomeName").toString(),Toast.LENGTH_SHORT).show();
            }
        });
    }

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
            case R.id.setting:
                gameController.showToastShort("setting");
                gameController.showToastShort("For debug, start the game after 3s.");
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        gameController.getAudioPlayer().pauseLobbyBGM();
                        Intent intent = new Intent(LobbyActivity.this, GameActivity.class);
                        startActivity(intent);
                    }
                }, 3000);
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
