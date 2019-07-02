package com.potatofriedbread.astro;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class RoomActivity extends AppCompatActivity {

    private PlayerView[] playerView;
    private JSONObject mRoom;
    private String hostIP; // 房主名
    private String nickname; // 名字
    private Boolean isHost; // 是否是房主

    private ScheduledThreadPoolExecutor exec;

    private tcpServer server; // 服务器的tcp线程
    private tcpClient client; // 客户端的tcp线程

    private Toolbar toolbar; // toolbar

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        GameController.getInstance().pushContext(this);

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

        if(isHost){
            mRoom = new JSONObject();
            try{
                mRoom.put("hostIP", NetUtils.getLocalHostIp());
                mRoom.put("roomName", nickname+"的房间");
                mRoom.put("roomState", false);
                mRoom.put("roomCurNum", 0);
                mRoom.put("roomCapacity", 2);
            }
            catch(Exception e){
                e.printStackTrace();
            }
            /* 发送 udp 多播 */
            exec = new ScheduledThreadPoolExecutor(1);
            exec.scheduleAtFixedRate(new Runnable() {
                public void run() {
                    new udpBroadcast(mRoom).start();
                }
            }, 0, 2000, TimeUnit.MILLISECONDS);

            // 打开tcp服务端
            server = new tcpServer();
            server.startServer(handler_for_msgFromClient);
        }

        // 客户端与服务器连接
        client = new tcpClient(hostIP, nickname, handler_for_msgFromServer);
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
                //也许还能把玩家自动设为托管
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
                String playerName = bundle.getString("clientName");
                for(int i = 0; i < 4; i++){
                    if(playerView[i].ifUsed == 0){
                        playerView[i].setUsername(playerName);
                        playerView[i].setIfUsed(1);
                        break;
                    }
                }
                System.out.println("收到了HELLO报文"+bundle);
                break;
          }
      }
    };

    private Handler handler_for_msgFromServer = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case Value.msg_shutdown:
                    Toast.makeText(RoomActivity.this,"房间已关闭",Toast.LENGTH_SHORT).show();
                    finish();
                    break;
                case Value.msg_room_full:
                    Toast.makeText(RoomActivity.this,"房间满人",Toast.LENGTH_SHORT).show();
                    finish();
                    break;
            }
        }
    };

}
