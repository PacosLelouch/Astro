package com.potatofriedbread.astro;

import android.view.View;

//TODO: Handler的POST的方法都换成message好了，然后拆几个Handler，方便异步。
//TODO: 有关state的使用还没加进去。
public class GameController {

    private GameActivity gameActivity;
    private int whoseTurn, rollNum, state, animationLeft/*, completeArrowUsed*/;
    private ConfigHelper configHelper;
    private ControlHandler controlHandler;
    private LANHandler lanHandler;
    private ServerHandler serverHandler;
    private AnimationHandler animationHandler;
    private AudioHandler audioHandler;
    private Chess[] red, yellow, blue, green;
    private Chess[][] chessList;

    private static final GameController instance = new GameController();

    private GameController(){
        /*
        try {
            initGame();
        } catch (Exception e){
            e.printStackTrace();
            System.out.println("Fail to initialize game.");
        }*/
    }

    //可能在开头画面新起一个线程加载资源？
    public void initGame() throws Exception{
        try{
            //completeArrowUsed = 0;
            state = Value.STATE_ROLL;
            animationLeft = 0;
            whoseTurn = (int)(Math.random() * 4);
            rollNum = 0;
            controlHandler = new ControlHandler(this);
            audioHandler = new AudioHandler(this);
            lanHandler = new LANHandler(this);
            serverHandler = new ServerHandler(this);
            animationHandler = new AnimationHandler(this);
            //TODO resources
            //TODO load chess
            chessList = new Chess[][]{red, yellow, blue, green};
            initChessPosAll();
            System.out.println("Initialization complete.");
        } catch (Exception e){
            e.printStackTrace();
            System.out.println("Fail to initialize game.");
        }
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
        System.out.println("Game start.");
        turnStart();
    }

    public void turnStart(){
        setState(Value.STATE_ROLL);
        resetRoll();
        System.out.println(Value.PLAYER_COLOR[whoseTurn] + "'s turn started.");
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
        System.out.println(Value.PLAYER_COLOR[whoseTurn] + "'s turn end.");
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
            System.out.println("Another chance.");
            turnStart();
        }
    }

    public void nextTurn(){
        whoseTurn = (whoseTurn + 1) % chessList.length;
        turnStart();
    }

    public void roll(){
        rollNum = (int)(Math.random() * 6) + 1;
        setState(Value.STATE_CANNOT_MOVE);
        animationHandler.postRollAnimation();
        //TextView's operation
        //TODO:下面的代码可能放animation的onAnimationEnd
    }

    public boolean restartGame(){
        try{
            initChessPosAll();
            System.out.println("Reset chess.");
            configHelper.reset();
            System.out.println("Reset helper.");
            //TODO complete arrow.
            whoseTurn = (int)(Math.random() * chessList.length);
            rollNum = 0;
            gameStart(configHelper.getGameType(), configHelper.getHostPlayer());
        } catch(Exception e){
            e.printStackTrace();
            System.out.println("Fail to restart game.");
            return false;
        }
        return true;
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
        //TODO: put chess at start point. Using chess.getPlayer() and chess.getChessNum().
    }

    public void resetRoll(){
        rollNum = 0;
        //TODO: ImageView
    }

    public boolean canMove(){
        if(state != Value.STATE_MOVE_CHESS){
            return false;
        }
        if(Value.TAKE_OFF_NUM.contains(rollNum)){
            //TextView
            System.out.println(Value.PLAYER_COLOR[whoseTurn] + " takes off.");
            return true;
        }
        for(int i = 0; i < 4; ++i){
            Chess chess = chessList[whoseTurn][i];
            if(chess.isFlying()){
                System.out.println(Value.PLAYER_COLOR[whoseTurn] + " can move planes.");
                return true;
            }
        }
        System.out.println(Value.PLAYER_COLOR[whoseTurn] + " cannot move anything.");
        return false;
    }

    public void go(Chess chess){
        if(rollNum != 0){
            if (Value.TAKE_OFF_NUM.contains(rollNum)) {
                if (chess == null) {
                    System.out.println("You didn't select any chess.");
                } else if (!chess.isFlying() && !chess.isCompleted()) {
                    chess.takeOff();
                    audioHandler.playFlyAudio();
                    animationHandler.postTakeOffAnimator(chess);
                    System.out.println("Take off.");
                } else {
                    int from = chess.getNowPos();
                    chess.move(rollNum);
                    audioHandler.playFlyAudio();
                    animationHandler.postMoveAnimation(chess, from, rollNum);
                    System.out.println("RollNum = " + rollNum + ", moving complete.");
                }
            } else {
                int from = chess.getNowPos();
                chess.move(rollNum);
                audioHandler.playFlyAudio();
                animationHandler.postMoveAnimation(chess, from, rollNum);
                System.out.println("RollNum = " + rollNum + ", moving complete.");
            }
        } else{
            System.out.println("Please roll first.");
        }
        //turnEnd(); // To async.
    }

    public void chessStatusJudge(Chess chess){
        int pos = chess.getNowPos();
        killJudge(chess);
        if(pos == Value.TERMINAL){
            int from = chess.getNowPos();
            chess.completeTour();
            animationHandler.postTerminateAnimation(chess, from);
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
        System.out.println("fly");
        int to = chess.getNowPos();
        audioHandler.playFlyAudio();
        animationHandler.postJumpAnimation(chess, from, to, jumpNum);
        //flyKillJudge(chess);
    }

    public void chessJump(Chess chess, int jumpNum){
        int from = chess.getNowPos();
        chess.move(4);;
        System.out.println("jump");
        int to = chess.getNowPos();
        audioHandler.playFlyAudio();
        animationHandler.postJumpAnimation(chess, from, to, jumpNum);
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
            System.out.println(Value.PLAYER_COLOR[whoseTurn] + " wins the game.");
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
                    System.out.println("A chess of player " + Value.PLAYER_COLOR[targetChess.getPlayer()] + " is killed.");
                    int from = targetChess.getNowPos();
                    targetChess.killed();
                    audioHandler.playKillAudio();
                    animationHandler.postKillAnimation(targetChess, from);
                    //initChessPos(targetChess);
                }
            }
        }
    }

    public void flyKillJudge(Chess chess){
        int target;
        switch(chess.getPlayer()){
            case Value.RED:
                target = Value.BLUE;
                break;
            case Value.YELLOW:
                target = Value.GREEN;
                break;
            case Value.BLUE:
                target = Value.RED;
                break;
            case Value.GREEN:
                target = Value.YELLOW;
                break;
            default:
                target = -1;
        }
        for(int i = 0; i < 4; ++i){ // 这个版本大跳跃还能撞上终点前的飞机吗
            Chess targetChess = chessList[target][i];
            if(targetChess.getNowPos() == Value.CONFLICT){
                targetChess.killed();
                audioHandler.playKillAudio();
                animationHandler.postKillAnimation(targetChess, Value.CONFLICT);
                //initChessPos(targetChess);
            }
        }
    }

    public Chess viewToChess(int player, View v){
        for(int i = 0; i < chessList[player].length; ++i){
            Chess chess = chessList[player][i];
            if(chess.getX() == (int)v.getX() * 2 / Chess.getChessWidth() &&
                    chess.getY() == (int)(v.getY() - Chess.getTopLeftY()) * 2 / Chess.getChessWidth()){
                return chess;
            }
        }
        System.out.println("You didn't click any chess.");
        return null;
    }

    public void setState(int value){
        state = value;
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

    public AnimationHandler getAnimationHandler(){
        return animationHandler;
    }

    public AudioHandler getAudioHandler(){
        return audioHandler;
    }

    public ConfigHelper getConfigHelper(){
        return configHelper;
    }

    public int getWhoseTurn(){
        return whoseTurn;
    }

    public void increaseAnimationCount(){
        animationLeft++;
    }

    public void decreaseAnimationCount(){
        animationLeft--;
    }

    public boolean noAnimationLeft(){
        return animationLeft == 0;
    }

    public void setGameActivity(GameActivity value){
        gameActivity = value;
    }

    public static GameController getInstance() {
        return instance;
    }
}
