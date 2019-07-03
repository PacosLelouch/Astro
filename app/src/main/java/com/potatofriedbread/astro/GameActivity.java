package com.potatofriedbread.astro;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;

public class GameActivity extends AppCompatActivity {

    private int localPlayer, hostPlayer;
    private ArrayList<Integer> aiList;
    private String[] nameList;
    private GameController gameController;
    private ImageButton play;
    private ImageButton charge;
    private ImageView map;
    private Toolbar toolbar;
    private PlayerView[] playerView;
    private Coordinate coordinate;
    private ImageView roll;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //加上其他Activity之后，这个函数会放在其他地方
        /*
        try {
            GameController.getInstance().initGameController();
        } catch (Exception e){
            e.printStackTrace();
            Log.d("TEST Choreographer", "Fail to initialize game.");
        }*/
        //localPlayer = Value.RED; // 以后是在房间里选
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        getRoomInfo(getIntent().getExtras());
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        play = findViewById(R.id.play);
        charge = findViewById(R.id.charge);
        map = findViewById(R.id.map);
        roll = findViewById(R.id.roll);
        playerView = new PlayerView[]{
                new PlayerView(R.id.user2, R.id.icon2, R.id.username2, R.id.charge2, Value.RED),
                new PlayerView(R.id.user4, R.id.icon4, R.id.username4, R.id.charge4, Value.YELLOW),
                new PlayerView(R.id.user3, R.id.icon3, R.id.username3, R.id.charge3, Value.BLUE),
                new PlayerView(R.id.user1, R.id.icon1, R.id.username1, R.id.charge1, Value.GREEN)
        };
        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onePlay();
            }
        });
        roll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gameController.showToastShort("What's up? Please roll by ROLL button.\nOkay, I will help you roll.");
                onePlay();
            }
        });
        charge.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setCharge();
            }
        });
        gameController = GameController.getInstance();
        gameController.setGameActivity(this);
        //if(Coordinate.getInstance() == null){
            Coordinate.createInstance(this);
        //}
        coordinate = Coordinate.getInstance();
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
                gameController.showToastShort("setting");
                break;
            case R.id.music:
                gameController.showToastShort("music");
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        if(gameController.getState() == Value.ONLINE_LAN){
            gameController.getLANHandler().postChangeTypeLAN(localPlayer, Value.AI);
        }
        gameController.setPlaying(false);
        gameController.popContext();
        coordinate = null;
    }

    private void getRoomInfo(Bundle bundle){
        hostPlayer = bundle.getInt("hostPlayer");
        localPlayer = bundle.getInt("localPlayer");
        aiList = bundle.getIntegerArrayList("aiList");
        nameList = bundle.getStringArray("nameList");
    }

    public ImageView getMap(){
        return map;
    }
    public ImageView getRoll() {
        return roll;
    }
    public Toolbar getToolbar() {
        return toolbar;
    }

    private void onePlay(){
        if(gameController.getWhoseTurn() != localPlayer) {
            Log.d("TEST Choreographer", "Not your turn to roll.");
        } else if(gameController.getState() != Value.STATE_ROLL){
            Log.d("TEST Choreographer", "Not the state to roll.");
        } else{
            gameController.roll();
            Log.i("TEST Choreographer", "Roll.");
        }
    }

    private void setCharge(){
        Log.i("TEST Choreographer", "Charge.");
        int playerType = gameController.getConfigHelper().getPlayerType(localPlayer);
        if(playerType == Value.AI){
            gameController.discharge(localPlayer);
        } else if(playerType == Value.LOCAL_HUMAN){
            gameController.charge(localPlayer);
        }
    }

    public void setPlayerChargeText(int player, String text){
        playerView[player].charge.setText(text);
    }

    public void gameStart(){
        if(GameController.getInstance().isPlaying()){
            gameController.showToastShort("Game resume.");
            return;
        }
        gameController.showToastShort("Game start.");
        if(aiList.size() == 3) {
            gameController.gameStart(Value.LOCAL, localPlayer, localPlayer, aiList);
        } else{
            gameController.gameStart(Value.ONLINE_LAN, hostPlayer, localPlayer, aiList);
        }
        for (int i = 0; i < playerView.length; ++i) {
            int playerType = gameController.getConfigHelper().getPlayerType(i);
            if (playerType == Value.AI) {
                playerView[i].charge.setText("(托管中)");
            }
        }
    }

    public void displayPlayerPrompt(int player){
        for(int i = 0; i < playerView.length; ++i){
            playerView[i].changeImage(0);
        }
        playerView[player].changeImage(1);
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
            username.setText(nameList[player]);
        }

        public void changeImage(int index){
            icon.setImageDrawable(Coordinate.getInstance().getChessImg(player, index));
        }
    }
}
