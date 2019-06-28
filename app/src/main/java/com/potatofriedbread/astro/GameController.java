package com.potatofriedbread.astro;

import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

public class GameController {

    private GameActivity gameActivity;
    private int whoseTurn, rollNum, state, animationLeft, loadLeft/*, completeArrowUsed*/;
    private ConfigHelper configHelper;
    private ControlHandler controlHandler;
    private LANHandler lanHandler;
    private ServerHandler serverHandler;
    private AnimationPlayer animationPlayer;
    private AudioPlayer audioPlayer;
    private Chess[] red, yellow, blue, green;
    private Chess[][] chessList;
    private int[] rollsId;
    private Toast toast;

    private static final GameController instance = new GameController();

    private GameController(){
        /*
        try {
            initGame();
        } catch (Exception e){
            e.printStackTrace();
            Log.d("TEST Choreographer", "Fail to initialize game.");
        }*/
    }

    //可能在开头画面新起一个线程加载资源？
    public void initGame() throws Exception{
        try{
            //completeArrowUsed = 0;
            setState(Value.STATE_CANNOT_MOVE);
            animationLeft = 0;
            loadLeft = 0;
            rollNum = 0;
            loadResources();
            //Do not load chess.
            controlHandler = new ControlHandler(this);
            audioPlayer = new AudioPlayer(this);
            lanHandler = new LANHandler(this);
            serverHandler = new ServerHandler(this);
            animationPlayer = new AnimationPlayer(this);
            Log.d("TEST Choreographer", "Initialization complete.");
        } catch (Exception e){
            e.printStackTrace();
            Log.d("TEST Choreographer", "Fail to initialize game.");
        }
    }

    public void loadResources(){
        // 1张未投骰子图片id+6张骰子图片id的数组
        increaseLoadCount();
        rollsId = new int[]{
                R.drawable.roll0,
                R.drawable.roll1,
                R.drawable.roll2,
                R.drawable.roll3,
                R.drawable.roll4,
                R.drawable.roll5,
                R.drawable.roll6,
        };
        //TODO other resources
    }

    public void loadChess(){
        // 棋子
        final ImageView[][] images = {{
                gameActivity.findViewById(R.id.red0),
                gameActivity.findViewById(R.id.red1),
                gameActivity.findViewById(R.id.red2),
                gameActivity.findViewById(R.id.red3)
        },{
                gameActivity.findViewById(R.id.yellow0),
                gameActivity.findViewById(R.id.yellow1),
                gameActivity.findViewById(R.id.yellow2),
                gameActivity.findViewById(R.id.yellow3)
        },{
                gameActivity.findViewById(R.id.blue0),
                gameActivity.findViewById(R.id.blue1),
                gameActivity.findViewById(R.id.blue2),
                gameActivity.findViewById(R.id.blue3)
        },{
                gameActivity.findViewById(R.id.green0),
                gameActivity.findViewById(R.id.green1),
                gameActivity.findViewById(R.id.green2),
                gameActivity.findViewById(R.id.green3)
        }};

        for(int i = 0; i < images.length; ++i){
            for(int j = 0; j < images[i].length; ++j){
                final ImageView image = images[i][j];
                final int startY = Value.STARTS_Y[i][j];
                final int startX = Value.STARTS_X[i][j];
                final int player = i;
                image.post(new Runnable() {
                    @Override
                    public void run() {
                        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(image.getLayoutParams());
                        layoutParams.height = (int)Coordinate.getInstance().getChessWidth();
                        layoutParams.width = (int)Coordinate.getInstance().getChessWidth();/*
                        layoutParams.topMargin = Coordinate.getInstance().mapToScreenY(startY);
                        layoutParams.leftMargin = Coordinate.getInstance().mapToScreenX(startX);*/
                        image.setLayoutParams(layoutParams);
                        image.setTranslationY(Coordinate.getInstance().mapToScreenY(startY));
                        image.setTranslationX(Coordinate.getInstance().mapToScreenX(startX));
                        /*Log.d("TEST Choreographer", "chess: " + image.getX() + " " + image.getY() + " " +
                                image.getWidth() + " " + image.getHeight());*/
                        image.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Chess chess = viewToChess(player, view);
                                go(chess);
                            }
                        });
                        decreaseLoadCount();
                        initChessPosAll();
                        if(noLoadingLeft()){
                            gameActivity.gameStart();
                        }
                    }
                });
            }
        }

        red = new Chess[]{
                new Chess(Value.RED, 0, images[Value.RED][0]),
                new Chess(Value.RED, 1, images[Value.RED][1]),
                new Chess(Value.RED, 2, images[Value.RED][2]),
                new Chess(Value.RED, 3, images[Value.RED][3]),
        };
        yellow = new Chess[]{
                new Chess(Value.YELLOW, 0, images[Value.YELLOW][0]),
                new Chess(Value.YELLOW, 1, images[Value.YELLOW][1]),
                new Chess(Value.YELLOW, 2, images[Value.YELLOW][2]),
                new Chess(Value.YELLOW, 3, images[Value.YELLOW][3]),
        };
        blue = new Chess[]{
                new Chess(Value.BLUE, 0, images[Value.BLUE][0]),
                new Chess(Value.BLUE, 1, images[Value.BLUE][1]),
                new Chess(Value.BLUE, 2, images[Value.BLUE][2]),
                new Chess(Value.BLUE, 3, images[Value.BLUE][3]),
        };
        green = new Chess[]{
                new Chess(Value.GREEN, 0, images[Value.GREEN][0]),
                new Chess(Value.GREEN, 1, images[Value.GREEN][1]),
                new Chess(Value.GREEN, 2, images[Value.GREEN][2]),
                new Chess(Value.GREEN, 3, images[Value.GREEN][3]),
        };
        chessList = new Chess[][]{red, yellow, blue, green};
    }

    public void gameStart(int gameType, int player){
        //LOCAL + ONE PLAYER
        if(configHelper == null) {
            configHelper = new ConfigHelper(gameType, player);
        }  else{
            configHelper.reset();
        }
        if(gameType == Value.LOCAL) {
            configHelper.changePlayerType(Value.RED, Value.AI);
            configHelper.changePlayerType(Value.YELLOW, Value.AI);
            configHelper.changePlayerType(Value.BLUE, Value.AI);
            configHelper.changePlayerType(Value.GREEN, Value.AI);
            configHelper.changePlayerType(player, Value.LOCAL_HUMAN);
        } else if(gameType == Value.ONLINE_LAN || gameType == Value.ONLINE_SERVER){
            configHelper.changePlayerType(Value.RED, Value.ONLINE_HUMAN);
            configHelper.changePlayerType(Value.YELLOW, Value.ONLINE_HUMAN);
            configHelper.changePlayerType(Value.BLUE, Value.ONLINE_HUMAN);
            configHelper.changePlayerType(Value.GREEN, Value.ONLINE_HUMAN);
            configHelper.changePlayerType(player, Value.LOCAL_HUMAN);
        }
        initWhoseTurn();
        Log.d("TEST Choreographer", "Game start.");
        turnStart();
    }

    public void turnStart(){
        showToastShort(Value.PLAYER_COLOR[whoseTurn] + "'s turn.");
        gameActivity.displayPlayerPrompt(whoseTurn);
        setState(Value.STATE_ROLL);
        resetRoll();
        Log.d("TEST Choreographer", Value.PLAYER_COLOR[whoseTurn] + "'s turn started.");
        if(configHelper.getGameType() == Value.LOCAL){
            if(configHelper.getPlayerType(whoseTurn) == Value.AI){
                controlHandler.getAIRoll();
            } // else return;
        } else if(configHelper.getGameType() == Value.ONLINE_LAN) {
            if (configHelper.isHost()) {
                if (configHelper.getPlayerType(whoseTurn) == Value.ONLINE_HUMAN) {
                    lanHandler.getOnlineRollLAN();
                } else if (configHelper.getPlayerType(whoseTurn) == Value.AI) {
                    lanHandler.postAIRollLAN();
                } // else return;
            } else { // client
                if (configHelper.getPlayerType(whoseTurn) != Value.LOCAL_HUMAN) { // ONLINE_HUMAN or AI
                    lanHandler.getOnlineRollLAN();
                } // else return;
            }
        } else{ // ONLINE_SERVER
            if (configHelper.getPlayerType(whoseTurn) != Value.LOCAL_HUMAN) { // ONLINE_HUMAN or AI
                serverHandler.getOnlineRollServer();
            } // else return;
        }
    }

    public void turnEnd(){
        Log.d("TEST Choreographer", Value.PLAYER_COLOR[whoseTurn] + "'s turn end.");
        if(configHelper.getGameType() == Value.LOCAL){
            // No operation.
        } else if(configHelper.getGameType() == Value.ONLINE_LAN) {
            if (configHelper.isHost()) {
                lanHandler.postHostTurnEndLAN();
            } else if (!configHelper.isHost()) {
                lanHandler.postClientTurnEndLAN();
            }
        } else if(configHelper.getGameType() == Value.ONLINE_SERVER){
            serverHandler.postOnlineTurnEndServer();
        }
        if(!Value.COMBO_NUM.contains(rollNum)){
            nextTurn();
        } else{
            Log.d("TEST Choreographer", "Another chance.");
            turnStart();
        }
    }

    public void nextTurn(){
        setWhoseTurn((whoseTurn + 1) % chessList.length);
        turnStart();
    }

    public void roll(){
        rollNum = (int)(Math.random() * 6) + 1;
        setState(Value.STATE_CANNOT_MOVE);
        animationPlayer.playRollAnimation(rollNum);
        //TextView's operation
        //下面的代码已经放animation的onAnimationEnd
    }

    public void getOnlineRoll(int rollNum){
        this.rollNum = rollNum;
        setState(Value.STATE_CANNOT_MOVE);
        animationPlayer.playRollAnimation(rollNum);
    }

    public boolean restartGame(){
        try{
            initChessPosAll();
            Log.d("TEST Choreographer", "Reset chess.");
            configHelper.reset();
            Log.d("TEST Choreographer", "Reset helper.");
            //TODO complete arrow.
            rollNum = 0;
            //gameStart(configHelper.getGameType(), configHelper.getHostPlayer());
        } catch(Exception e){
            e.printStackTrace();
            Log.d("TEST Choreographer", "Fail to restart game.");
            return false;
        }
        return true;
    }

    public void initWhoseTurn(){
        if(configHelper.getGameType() == Value.LOCAL) {
            //setWhoseTurn(Value.RED); // Just for test.
            setWhoseTurn((int)(Math.random() * chessList.length));
        } else if(configHelper.getGameType() == Value.ONLINE_LAN){
            if(configHelper.isHost()){
                setWhoseTurn((int)(Math.random() * chessList.length));
                lanHandler.postWhoseTurnLan(whoseTurn);
            } else{
                lanHandler.getWhoseTurnLAN();
                //TODO
            }
        } else if(configHelper.getGameType() == Value.ONLINE_SERVER){
            serverHandler.getWhostTurnServer();
            //TODO
        }
    }

    public void setWhoseTurn(int value){
        whoseTurn = value;
    }

    public void initChessPosAll(){
        for(int i = 0; i < chessList.length; ++i){
            for(int j = 0; j < chessList[i].length; ++j){
                initChessPos(chessList[i][j]);
            }
        }
    }

    public void initChessPos(Chess chess){
        chess.reset();
        chess.moveImg(
                Coordinate.getInstance().mapToScreenX(Value.STARTS_X[chess.getPlayer()][chess.getChessNum()]),
                Coordinate.getInstance().mapToScreenY(Value.STARTS_Y[chess.getPlayer()][chess.getChessNum()])
        );
    }

    public void resetRoll(){
        rollNum = 0;
        //TODO: ImageView
    }

    public boolean canMove(){
        if(Value.TAKE_OFF_NUM.contains(rollNum)){
            //TextView
            Log.d("TEST Choreographer", Value.PLAYER_COLOR[whoseTurn] + " takes off.");
            return true;
        }
        for(int i = 0; i < 4; ++i){
            Chess chess = chessList[whoseTurn][i];
            if(chess.isFlying()){
                Log.d("TEST Choreographer", Value.PLAYER_COLOR[whoseTurn] + " can move planes.");
                return true;
            }
        }
        Log.d("TEST Choreographer", Value.PLAYER_COLOR[whoseTurn] + " cannot move anything.");
        return false;
    }

    public void go(Chess chess){
        if(chess == null){
            return;
        }
        if(whoseTurn != chess.getPlayer()){
            Log.d("TEST Choreographer", "Not your turn.");
            showToastShort("Not your turn.");
            return;
        }
        if(state != Value.STATE_MOVE_CHESS){
            Log.d("TEST Choreographer", "You cannot move now.");
            showToastShort("You cannot move now.");
            return;
        }
        if(rollNum != 0){
            if (Value.TAKE_OFF_NUM.contains(rollNum)) {
                if (chess == null) {
                    Log.d("TEST Choreographer", "You didn't select any chess.");
                    return;
                } else if (!chess.isFlying() && !chess.isCompleted()) {
                    chess.takeOff();
                    audioPlayer.playFlyAudio();
                    animationPlayer.playTakeOffAnimator(chess);
                    Log.d("TEST Choreographer", "Take off.");
                } else if(!chess.isCompleted()) {
                    int from = chess.getNowPos();
                    chess.move(rollNum);
                    audioPlayer.playFlyAudio();
                    animationPlayer.playMoveAnimation(chess, from, rollNum);
                    Log.d("TEST Choreographer", "RollNum = " + rollNum + ", moving complete.");
                } else{
                    Log.d("TEST Choreographer", "It's completed.");
                    showToastShort("It's completed.");
                    return;
                }
            } else {
                if(!chess.isCompleted()) {
                    int from = chess.getNowPos();
                    chess.move(rollNum);
                    audioPlayer.playFlyAudio();
                    animationPlayer.playMoveAnimation(chess, from, rollNum);
                    Log.d("TEST Choreographer", "RollNum = " + rollNum + ", moving complete.");
                } else{
                    Log.d("TEST Choreographer", "It's completed.");
                    showToastShort("It's completed.");
                    return;
                }
            }
            displayChessNormal();
        } else{
            Log.d("TEST Choreographer", "Please roll first.");
        }
        //turnEnd(); // To async.
    }

    public void chessStatusJudge(Chess chess){
        int pos = chess.getNowPos();
        killJudge(chess);
        if(pos == Value.TERMINAL){
            int from = chess.getNowPos();
            chess.completeTour();
            animationPlayer.playTerminateAnimation(chess, from);
            //initChessPos(chess);
            //gameOverJudge(); // To async.
        } else if(Value.FLY_POINT == pos){
            chessFly(chess, 2);
        } else if(Value.JUMP_POINT.contains(pos)){
            chessJump(chess, 2);
        }
    }

    public void chessFly(Chess chess, int jumpNum){
        int from = chess.getNowPos();
        chess.move(12);
        Log.d("TEST Choreographer", "fly");
        int to = chess.getNowPos();
        audioPlayer.playFlyAudio();
        animationPlayer.playJumpAnimation(chess, from, to, jumpNum);
        //flyKillJudge(chess);
    }

    public void chessJump(Chess chess, int jumpNum){
        int from = chess.getNowPos();
        chess.move(4);
        Log.d("TEST Choreographer", "jump");
        int to = chess.getNowPos();
        audioPlayer.playFlyAudio();
        animationPlayer.playJumpAnimation(chess, from, to, jumpNum);
    }

    public boolean gameOverJudge(){
        boolean flag = true;
        for(int i = 0; i < 4; ++i){
            Chess chess = chessList[whoseTurn][i];
            if(!chess.isCompleted()){
                flag = false;
                break;
            }
        }
        if(flag){
            Log.d("TEST Choreographer", Value.PLAYER_COLOR[whoseTurn] + " wins the game.");
            //TextView
            try{
                Thread.sleep(3000);
                restartGame();
                return true;
            } catch(Exception e){
                e.printStackTrace();
            }
        }
        return false;
    }

    public void killJudge(Chess chess){
        for(int i = 0; i < chessList.length; ++i){
            if(i == chess.getPlayer()){
                continue;
            }
            for(int j = 0; j < chessList[i].length; ++j){
                Chess targetChess = chessList[i][j];
                if(chess.getX() == targetChess.getX() && chess.getY() == targetChess.getY()){
                    Log.d("TEST Choreographer", "A chess of player " + Value.PLAYER_COLOR[targetChess.getPlayer()] + " is killed.");
                    int from = targetChess.getNowPos();
                    targetChess.killed();
                    audioPlayer.playKillAudio();
                    animationPlayer.playKillAnimation(targetChess, from);
                    //initChessPos(targetChess);
                }
            }
        }
    }

    public void flyKillJudge(Chess chess){
        int target = chess.rival();
        for(int i = 0; i < 4; ++i){ // 这个版本大跳跃还能撞上终点前的飞机吗
            Chess targetChess = chessList[target][i];
            if(targetChess.getNowPos() == Value.CONFLICT){
                targetChess.killed();
                audioPlayer.playKillAudio();
                animationPlayer.playKillAnimation(targetChess, Value.CONFLICT);
                //initChessPos(targetChess);
            }
        }
    }

    public Chess viewToChess(int player, View view){
        for(int i = 0; i < chessList[player].length; ++i){
            Chess chess = chessList[player][i];
            if(Coordinate.getInstance().clickTheChess(chess, view)){
                return chess;
            }
        }
        Log.d("TEST Choreographer", "You didn't click any chess.");
        return null;
    }

    public void showToastShort(String text){
        if(toast != null){
            toast.cancel();
        }
        toast = Toast.makeText(gameActivity, text, Toast.LENGTH_SHORT);
        toast.show();
    }

    public void setState(int value){
        state = value;
        if(value == Value.STATE_ROLL){
            gameActivity.getRoll().setImageResource(R.drawable.roll0);
        } else if(value == Value.STATE_MOVE_CHESS){
            displayMovable();
        }
    }

    public int getState(){
        return state;
    }

    public ControlHandler getControlHandler(){
        return controlHandler;
    }

    public LANHandler getLANHandler(){
        return lanHandler;
    }

    public ServerHandler getServerHandler(){
        return serverHandler;
    }

    public AnimationPlayer getAnimationPlayer(){
        return animationPlayer;
    }

    public AudioPlayer getAudioPlayer(){
        return audioPlayer;
    }

    public ConfigHelper getConfigHelper(){
        return configHelper;
    }

    public int getWhoseTurn(){
        return whoseTurn;
    }

    public Chess[][] getChessList(){
        return chessList;
    }

    public int[] getRollsId(){
        return rollsId;
    }

    public void increaseAnimationCount(){
        animationLeft++;
        Log.d("TEST_ANIMATION", animationLeft + ", increase.");
    }

    public void decreaseAnimationCount(){
        animationLeft--;
        Log.d("TEST_ANIMATION", animationLeft + ", decrease.");
    }

    public boolean noAnimationLeft(){
        Log.d("TEST_ANIMATION", animationLeft + " left.");
        return animationLeft == 0;
    }

    public void increaseLoadCount(){
        loadLeft++;
    }

    public void decreaseLoadCount(){
        loadLeft--;
    }

    public boolean noLoadingLeft(){
        return loadLeft == 0;
    }

    public void setGameActivity(GameActivity value){
        gameActivity = value;
        Log.d("TEST Choreographer","gameActivity: " + gameActivity.toString());
    }

    public GameActivity getGameActivity() {
        return gameActivity;
    }

    public static GameController getInstance() {
        return instance;
    }

    private void displayMovable(){
        for(int i = 0; i < chessList[whoseTurn].length; ++i){
            Chess chess = chessList[whoseTurn][i];
            if (!chess.isFlying() && !chess.isCompleted()) {
                chess.changeImage(Value.TAKE_OFF_NUM.contains(rollNum) ? 1 : 0);
            } else if(!chess.isCompleted()) {
                chess.changeImage(1);
            } else {
                chess.changeImage(2);
            }
        }
    }

    private void displayChessNormal(){
        for(int i = 0; i < chessList[whoseTurn].length; ++i){
            Chess chess = chessList[whoseTurn][i];
            if(!chess.isCompleted()) {
                chess.changeImage(0);
            } else {
                chess.changeImage(2);
            }
        }
    }
}
