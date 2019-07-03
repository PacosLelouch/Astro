package com.potatofriedbread.astro;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class RoomActivity extends AppCompatActivity {

    private PlayerView[] playerView;
    private JSONObject mRoom;
    private String hostIP; // 房主ip
    private String nickname; // 名字
    private Boolean isHost; // 是否是房主
    private Integer curNum = 0;
    private Integer capacity = 4;
    private Integer localPlayer;

    private ScheduledThreadPoolExecutor exec;

    private tcpServer server; // 服务器的tcp线程
    private tcpClient client; // 客户端的tcp线程

    private Toolbar toolbar; // toolbar
    private ImageButton startGame;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        GameController.getInstance().pushContext(this);
        localPlayer = new Integer(-1);

        final Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        hostIP = bundle.getString("hostIP");
        nickname = bundle.getString("nickname");
        isHost = bundle.getBoolean("isHost");

        playerView = new PlayerView[]{
                new PlayerView(R.id.user2, R.id.icon2, R.id.username2, R.id.charge2, Value.RED),
                new PlayerView(R.id.user4, R.id.icon4, R.id.username4, R.id.charge4, Value.YELLOW),
                new PlayerView(R.id.user3, R.id.icon3, R.id.username3, R.id.charge3, Value.BLUE),
                new PlayerView(R.id.user1, R.id.icon1, R.id.username1, R.id.charge1, Value.GREEN)
        };

        startGame = findViewById(R.id.startGame);
        startGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startGameActivityHost();
            }
        });

        if(isHost){
            /* 发送 udp 多播 */
            exec = new ScheduledThreadPoolExecutor(1);
            exec.scheduleAtFixedRate(new Runnable() {
                public void run() {
                    mRoom = new JSONObject();
                    try{
                        mRoom.put("hostIP", NetUtils.getLocalHostIp());
                        mRoom.put("roomName", nickname+"的房间");
                        mRoom.put("roomState", false);
                        mRoom.put("roomCurNum", curNum);
                        mRoom.put("roomCapacity", capacity);
                    }
                    catch(Exception e){
                        e.printStackTrace();
                    }
                    new udpBroadcast(mRoom).start();
                }
            }, 0, 2000, TimeUnit.MILLISECONDS);

            // 打开tcp服务端
            server = new tcpServer();
            GameController.getInstance().setServer(server);
            server.startServer(handler_for_msgFromClient);
        } else {
            startGame.setVisibility(View.INVISIBLE);
        }

        // 客户端与服务器连接
        client = new tcpClient(hostIP, nickname, handler_for_msgFromServer);
        GameController.getInstance().setClient(client);
        client.startConnect();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_game, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case android.R.id.home:
                finish();
            case R.id.setting:
                // TODO
                break;
            case R.id.music:
                // TODO
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        GameController.getInstance().popContext();
        if(isHost){
            exec.shutdownNow();
            server.shutdown();
        }
    }

    public class PlayerView{
        public RelativeLayout user;
        public ImageView icon;
        public TextView username;
        public TextView charge;
        public int player;
        private Integer ifUsed;

        public PlayerView(int idUser, int idIcon, int idUsername, int idCharge, int player){
            user = findViewById(idUser);
            icon = findViewById(idIcon);
            username = findViewById(idUsername);
            charge = findViewById(idCharge);
            this.player = player;
            ifUsed = 0;
            username.setText("[空闲位置]");
        }

        public void changeImage(int index){
            icon.setImageDrawable(Coordinate.getInstance().getChessImg(player, index));
        }

        public void setIfUsed(Integer x){
            ifUsed = x;
        }

        public void setUsername(String name){
            username.setText(name);
        }
    }

    private Handler handler_for_msgFromClient = new Handler(){
        @Override
        public void handleMessage(Message msg){
            super.handleMessage(msg);
            switch(msg.what){
              case Value.type_hello:
                Bundle bundle = msg.getData();
                String clientIP = bundle.getString("clientIP");
                String clientName = bundle.getString("clientName");
                if(tcpServer.findPosition(clientIP, clientName)) // 为新来的玩家找位置
                    curNum += 1;
                tcpServer.broadcastPosition(); // 广播新的座位表
                System.out.println("收到了HELLO报文"+clientIP);
                break;
            }
        }
    };

    private Handler handler_for_msgFromServer = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Bundle bundle = msg.getData();
            switch (msg.what) {
                case Value.msg_shutdown:
                    Toast.makeText(RoomActivity.this,"房间已关闭",Toast.LENGTH_SHORT).show();
                    finish();
                    break;
                case Value.msg_room_full:
                    Toast.makeText(RoomActivity.this,"房间满人",Toast.LENGTH_SHORT).show();
                    finish();
                    break;
                case Value.msg_position:
                    try {
                        JSONArray clientIPs = new JSONArray(bundle.getString("clientIPs"));
                        JSONArray clientNames = new JSONArray(bundle.getString("clientNames"));
                        for(int i = 0; i < 4; i++){
                            if(clientIPs.get(i) == null || clientIPs.get(i).toString() == "null"){
                                playerView[i].setUsername("[空位]");
                                playerView[i].setIfUsed(0);
                                continue;
                            }
                            else {
                                if(clientIPs.get(i).toString().equals(NetUtils.getLocalHostIp())){
                                    localPlayer = i;
                                }
                                playerView[i].setUsername(clientNames.get(i).toString());
                                playerView[i].setIfUsed(1);
                            }

                        }
                    }
                    catch(Exception e){
                        e.printStackTrace();
                    }

                    break;
                case Value.msg_start_game:
                    startGameActivityClient(Integer.parseInt(bundle.get("hostPlayer").toString()));
                    break;
            }
        }
    };


    private void startGameActivityHost(){
        //Intent intent = new Intent("com.potatofriedbread.astro.game");
        //Bundle bundle = new Bundle();
        int hostPlayer = tcpServer.getClientMap().get(NetUtils.getLocalHostIp()).clientPosition;
        localPlayer = hostPlayer;
        ArrayList<Integer> aiList = new ArrayList<>();
        for(int i = 0; i < tcpServer.getIfUsedList().length; ++i){
            if(tcpServer.getIfUsedList()[i] == false){
                aiList.add(i);
            }
        }
        Log.d("TEST AI H", aiList.toString());
        String[] nameList = new String[playerView.length];
        for(int i = 0; i < playerView.length; ++i) {
            nameList[i] = playerView[i].username.getText().toString();
        }
        /*bundle.putInt("hostPlayer", hostPlayer);
        bundle.putInt("localPlayer", localPlayer);
        bundle.putIntegerArrayList("aiList", aiList);
        bundle.putStringArray("nameList", nameList);
        intent.putExtras(bundle);*/
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("type", Value.msg_start_game);
        hashMap.put("hostPlayer", hostPlayer);
        Log.d("TEST", hashMap.toString());
        server.sendMessageToAll(hashMap.toString());
        //startActivity(intent);
    }

    private void startGameActivityClient(int hostPlayer){
        Intent intent = new Intent("com.potatofriedbread.astro.game");
        Bundle bundle = new Bundle();
        Log.d("TEST", "hostPlayer" + hostPlayer + " localPlayer" + localPlayer);
        bundle.putInt("hostPlayer", hostPlayer);
        bundle.putInt("localPlayer", localPlayer);
        ArrayList<Integer> aiList = new ArrayList<>();
        for(int i = 0; i < playerView.length; ++i){
            if(playerView[i].username.getText().toString().equals("[空位]")){
                aiList.add(i);
            }
        }
        Log.d("TEST AI C", aiList.toString());
        String[] nameList = new String[playerView.length];
        for(int i = 0; i < playerView.length; ++i) {
            nameList[i] = playerView[i].username.getText().toString();
        }
        bundle.putIntegerArrayList("aiList", aiList);
        bundle.putStringArray("nameList", nameList);
        intent.putExtras(bundle);
        startActivity(intent);
    }
}
