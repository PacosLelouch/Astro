package com.potatofriedbread.astro;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.Stack;

public class GameController {

    private tcpClient client;
    private tcpServer server;
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
    //private int[] rollsId;
    private Toast toast;
    private Stack<Context> contextStack;
    private boolean playing;

    private static final GameController instance = new GameController();

    private GameController(){
        /*
        try {
            initGameController();
        } catch (Exception e){
            e.printStackTrace();
            Log.d("TEST Choreographer", "Fail to initialize game.");
        }*/
        this.contextStack = new Stack<Context>();
    }

    //可能在开头画面新起一个线程加载资源？
    public void initGameController() throws Exception{
        try{
            //completeArrowUsed = 0;
            setState(Value.STATE_CANNOT_MOVE);
            animationLeft = 0;
            loadLeft = 0;
            rollNum = 0;
            playing = false;
            loadResources();
            //Do not load chess.
            controlHandler = new ControlHandler(this);
            lanHandler = new LANHandler(this);
            serverHandler = new ServerHandler(this);
            Log.d("TEST Choreographer", "Initialization complete.");
        } catch (Exception e){
            e.printStackTrace();
            Log.d("TEST Choreographer", "Fail to initialize game.");
        }
    }

    public void loadResources(){
        audioPlayer = new AudioPlayer(this);
        animationPlayer = new AnimationPlayer(this);
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
                increaseLoadCount();
            }
        }
        for(int i = 0; i < images.length; ++i){
            for(int j = 0; j < images[i].length; ++j){
                final ImageView image = images[i][j];
                final int startY = Value.STARTS_Y[i][j];
                final int startX = Value.STARTS_X[i][j];
                final int player = i, num = j;
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
                        decreaseLoadCount();
                        if(noLoadingLeft()){
                            initChessPosAll();
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

    public void gameStart(int gameType, int hostPlayer, int localPlayer, ArrayList<Integer> aiList){
        //LOCAL + ONE PLAYER
        //if(configHelper == null) {
            configHelper = new ConfigHelper(gameType, hostPlayer, localPlayer);
        /*}  else{
            configHelper.reset();
        }*/
        if(gameType == Value.LOCAL) {
            configHelper.changePlayerType(Value.RED, Value.AI);
            configHelper.changePlayerType(Value.YELLOW, Value.AI);
            configHelper.changePlayerType(Value.BLUE, Value.AI);
            configHelper.changePlayerType(Value.GREEN, Value.AI);
            configHelper.changePlayerType(localPlayer, Value.LOCAL_HUMAN);
        } else if(gameType == Value.ONLINE_LAN || gameType == Value.ONLINE_SERVER){
            configHelper.changePlayerType(Value.RED, Value.ONLINE_HUMAN);
            configHelper.changePlayerType(Value.YELLOW, Value.ONLINE_HUMAN);
            configHelper.changePlayerType(Value.BLUE, Value.ONLINE_HUMAN);
            configHelper.changePlayerType(Value.GREEN, Value.ONLINE_HUMAN);
            for(int i = 0; i < aiList.size(); ++i){
                configHelper.changePlayerType(aiList.get(i), Value.AI);
            }
            configHelper.changePlayerType(localPlayer, Value.LOCAL_HUMAN);
        }
        /*
        if(gameType == Value.ONLINE_LAN){
            client.changeHandler(lanHandler);
            if(configHelper.isHost()){
                server.changeHandler(new Handler(){
                    @Override
                    public void handleMessage(Message msg){
                        super.handleMessage(msg);
                        if(msg == null){
                            return;
                        }
                        Bundle bundle = msg.getData();
                        HashMap<String, Object> hashMap = new HashMap<>();
                        Set<String> keySet = bundle.keySet();
                        for(String key: keySet){
                            hashMap.put(key, bundle.get(key));
                        }
                        server.sendMessageToAll(hashMap.toString());
                    }
                });
            }
        }*/
        setPlaying(true);
        Log.d("TEST Choreographer", "Game start.");
        initWhoseTurn();
        //turnStart async.
    }

    public void turnStart(){
        if(!isPlaying()){
            return;
        }
        showToastShort(Value.PLAYER_COLOR[whoseTurn] + "'s turn.");
        gameActivity.displayPlayerPrompt(whoseTurn);
        setState(Value.STATE_ROLL);
        resetRoll();
        Log.d("TEST Choreographer", Value.PLAYER_COLOR[whoseTurn] + "'s turn started.");
        checkRollOperation();
    }

    private void checkRollOperation(){
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
                    lanHandler.getOnlineRollLAN();
                } else{
                    lanHandler.getOnlineRollLAN();
                }
            } else { // client
                if (configHelper.getPlayerType(whoseTurn) != Value.LOCAL_HUMAN) { // ONLINE_HUMAN or AI
                    lanHandler.getOnlineRollLAN();
                } else{
                    lanHandler.getOnlineRollLAN();
                }
            }
        } else{ // ONLINE_SERVER
            if (configHelper.getPlayerType(whoseTurn) != Value.LOCAL_HUMAN) { // ONLINE_HUMAN or AI
                serverHandler.getOnlineRollServer();
            } else{
                serverHandler.getOnlineRollServer();
            }
        }
    }

    public void turnEnd(){
        Log.d("TEST Choreographer", Value.PLAYER_COLOR[whoseTurn] + "'s turn end.");
        /*
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
        }*/
        if(!Value.COMBO_NUM.contains(rollNum)){
            nextTurn();
        } else{
            Log.d("TEST Choreographer", "Another chance.");
            turnStart();
        }
    }

    public void nextTurn(){
        setWhoseTurn((whoseTurn + 1) % chessList.length);
        //turnStart();
    }

    public void rollByLocalPlayer(){/*
        if(configHelper.getGameType() != Value.LOCAL && configHelper.getLocalPlayer() != whoseTurn){
            Log.d("TEST Choreographer", "Not your turn.");
            showToastShort("Not your turn.");
            return;
        }*/
        if(configHelper.getGameType() == Value.LOCAL) {
            rollNum = (int) (Math.random() * 6) + 1;
            setState(Value.STATE_CANNOT_MOVE);
            animationPlayer.playRollAnimation(rollNum);
            Log.d("TEST Choreographer", "rollNum = " + rollNum);
        } else if(configHelper.getGameType() == Value.ONLINE_LAN){
            lanHandler.postOnlineRollLAN();
        } else if(configHelper.getGameType() == Value.ONLINE_SERVER){
            //TODO: Not implemented,
        }
        //TextView's operation
        //下面的代码已经放animation的onAnimationEnd
    }

    public void onlineRoll(int rollNum){
        this.rollNum = rollNum;
        setState(Value.STATE_CANNOT_MOVE);
        animationPlayer.playRollAnimation(rollNum);
    }

    public boolean resetGame(){
        try{
            initChessPosAll();

            Log.d("TEST Choreographer", "Reset chess.");
            configHelper.reset();
            Log.d("TEST Choreographer", "Reset helper.");
            rollNum = 0;
            initWhoseTurn();
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
                Log.d("TEST", "I am host.");
                int whoseTurnTmp = (int)(Math.random() * chessList.length);
                //setWhoseTurn((int)(Math.random() * chessList.length));
                lanHandler.postWhoseTurnLAN(whoseTurnTmp);
                lanHandler.getWhoseTurnLAN();
            } else{
                lanHandler.getWhoseTurnLAN();
            }
        } else if(configHelper.getGameType() == Value.ONLINE_SERVER){
            //serverHandler.getWhostTurnServer(); TODO: Not implemented.
        }
    }

    public void setWhoseTurn(int value){
        whoseTurn = value;
        controlHandler.postTurnStart();
    }

    public void setPlayerTypeWhilePlaying(int player, int type){
        configHelper.changePlayerType(player, type);
        gameActivity.setPlayerChargeText(player, type == Value.AI ? "(托管中)" : "");
        if(configHelper.getGameType() == Value.LOCAL) {
            if(type == Value.AI && whoseTurn == player) {
                if (state == Value.STATE_ROLL) {
                    controlHandler.getAIRoll();
                } else if (state == Value.STATE_MOVE_CHESS) {
                    controlHandler.getAIMove(rollNum);
                }
            }
        } else if(configHelper.getGameType() == Value.ONLINE_LAN){
            if(type == Value.AI && whoseTurn == player) {
                if(configHelper.isHost()) {
                    if (state == Value.STATE_ROLL) {
                        lanHandler.postAIRollLAN();
                        //lanHandler.getOnlineRollLAN(); No need to get more.
                    } else if (state == Value.STATE_MOVE_CHESS) {
                        lanHandler.postAIMoveLAN(rollNum);
                        //lanHandler.getOnlineMoveLAN(); No need to get more.
                    }
                } else {
                    if (state == Value.STATE_ROLL) {
                        //lanHandler.getOnlineRollLAN(); No need to get more.
                    } else if (state == Value.STATE_MOVE_CHESS) {
                        //lanHandler.getOnlineMoveLAN(); No need to get more.
                    }
                }
            }
        } else if(configHelper.getGameType() == Value.ONLINE_SERVER) {
            //TODO: Not implemented.
        }
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
        gameActivity.getRoll().setImageDrawable(Coordinate.getInstance().getRollImg(rollNum));
    }

    public boolean canMove(){
        if(Value.TAKE_OFF_NUM.contains(rollNum)){
            //TextView
            Log.d("TEST Choreographer", Value.PLAYER_COLOR[whoseTurn] + " can take off.");
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

    public void goByLocalPlayer(Chess chess){
        if(whoseTurn != chess.getPlayer() || configHelper.getPlayerType(whoseTurn) != Value.LOCAL_HUMAN){
            showToastShort("Not your turn.");
            return;
        } else if(state != Value.STATE_MOVE_CHESS){
            showToastShort("You cannot move now.");
            return;
        } else if(chess.isCompleted()) {
            showToastShort("It's completed.");
            return;
        } else if(!Value.TAKE_OFF_NUM.contains(rollNum) && !chess.isFlying()){
            showToastShort("Not the number to take off.");
            return;
        }
        if(configHelper.getGameType() == Value.LOCAL){
            go(chess); // judge inside go.
        } else if(configHelper.getGameType() == Value.ONLINE_LAN){
            int nowPos = chess.getNowPos();
            //go(chess);
            lanHandler.postOnlineMoveLAN(chess.getPlayer(), chess.getChessNum(), nowPos);
            //lanHandler.getOnlineMoveLAN(); // No need to get more.
        } else if(configHelper.getGameType() == Value.ONLINE_SERVER){
            //TODO: Not implemented.
        }
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
                if(chess.isFlying() && !chess.isCompleted()) {
                    int from = chess.getNowPos();
                    chess.move(rollNum);
                    audioPlayer.playFlyAudio();
                    animationPlayer.playMoveAnimation(chess, from, rollNum);
                    Log.d("TEST Choreographer", "RollNum = " + rollNum + ", moving complete.");
                } else if(chess.isCompleted()){
                    Log.d("TEST Choreographer", "It's completed.");
                    showToastShort("It's completed.");
                    return;
                } else{
                    Log.d("TEST Choreographer", "Not the number to take off.");
                    showToastShort("Not the number to take off.");
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
            gameOver();
            return true;
        }
        return false;
    }

    private void gameOver(){
        try{
            setPlaying(false);
            if(audioPlayer.isPlayingGamePlayingBGM()){
                audioPlayer.pauseGamePlayingBGM();
                audioPlayer.playGameOverBGM();
            }
            //Thread.sleep(3000);
            //resetGame();
        } catch(Exception e){
            e.printStackTrace();
        }
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

    public void showToastShort(String text){
        if(toast != null){
            toast.cancel();
        }
        toast = Toast.makeText(getContext(), text, Toast.LENGTH_SHORT);
        toast.show();
    }

    public void charge(int player){
        if(configHelper.getGameType() == Value.LOCAL) {
            setPlayerTypeWhilePlaying(player, Value.AI);
        } else if(configHelper.getGameType() == Value.ONLINE_LAN){
            lanHandler.postChangeTypeLAN(player, Value.AI);
        }
    }

    public void discharge(int player){
        if(configHelper.getGameType() == Value.LOCAL) {
            setPlayerTypeWhilePlaying(player, Value.LOCAL_HUMAN);
        } else if(configHelper.getGameType() == Value.ONLINE_LAN){
            lanHandler.postChangeTypeLAN(player, Value.LOCAL_HUMAN);
        }
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

    public void increaseAnimationCount(){
        setState(Value.STATE_CANNOT_MOVE);
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
        pushContext(value);
        gameActivity = value;
        Log.d("TEST Choreographer","gameActivity: " + gameActivity.toString());
    }

    public void pushContext(Context context){
        contextStack.push(context);
    }

    public void popContext(){
        contextStack.pop();
    }

    public Context getContext(){
        return contextStack.peek();
    }

    public GameActivity getGameActivity() {
        return gameActivity;
    }

    public static GameController getInstance() {
        return instance;
    }

    public boolean isPlaying(){
        return playing;
    }

    public void setPlaying(boolean value){
        playing = value;
        audioPlayer.pauseGameOverBGM();
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

    public void setClient(tcpClient client){
        this.client = client;
    }

    public tcpClient getClient(){
        return client;
    }

    public void setServer(tcpServer server){
        this.server = server;
    }

    public tcpServer getServer(){
        return server;
    }
}
