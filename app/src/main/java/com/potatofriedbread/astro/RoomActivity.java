package com.potatofriedbread.astro;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.json.JSONObject;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class RoomActivity extends AppCompatActivity {

    private PlayerView[] playerView;
    private JSONObject mRoom;
    private String hostName; // 房主名字
    private Integer curNum; // 当前房间人数
    private Integer capacity; // 房间总容量

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room);

        final Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        hostName = bundle.getString("hostName");

        playerView = new PlayerView[]{
                new PlayerView(R.id.user2, R.id.icon2, R.id.username2, R.id.charge2, Value.RED),
                new PlayerView(R.id.user4, R.id.icon4, R.id.username4, R.id.charge4, Value.YELLOW),
                new PlayerView(R.id.user3, R.id.icon3, R.id.username3, R.id.charge3, Value.BLUE),
                new PlayerView(R.id.user1, R.id.icon1, R.id.username1, R.id.charge1, Value.GREEN)
        };

//        playerView[0].setUsername(hostName);
//        playerView[1].setUsername();


        mRoom = new JSONObject();
        try{
            mRoom.put("hostIP", NetUtils.getLocalHostIp());
            mRoom.put("roomName", hostName+"的房间");
            mRoom.put("roomState", false);
            mRoom.put("roomCurNum", 1);
            mRoom.put("roomCapacity", 4);
        }
        catch(Exception e){
            e.printStackTrace();
        }
        /* 发送 udp 多播 */
        ScheduledThreadPoolExecutor exec = new ScheduledThreadPoolExecutor(1);
        exec.scheduleAtFixedRate(new Runnable() {
            public void run() {
                new udpBroadcast(mRoom).start();
            }
        }, 0, 300, TimeUnit.MILLISECONDS);

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

    public class PlayerView{
        public RelativeLayout user;
        public ImageView icon;
        public TextView username;
        public TextView charge;
        public int player;

        public PlayerView(int idUser, int idIcon, int idUsername, int idCharge, int player){
            user = findViewById(idUser);
            icon = findViewById(idIcon);
            username = findViewById(idUsername);
            charge = findViewById(idCharge);
            this.player = player;
        }

        public void changeImage(int index){
            icon.setImageDrawable(Coordinate.getInstance().getChessImg(player, index));
        }

        public void setUsername(String name){
            username.setText(name);
        }
    }


}
