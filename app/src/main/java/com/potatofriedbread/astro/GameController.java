package com.potatofriedbread.astro;

import android.os.Handler;
import android.view.View;

public class GameController {

    private int completeArrowUsed, whoseTurn, rollNum;
    private ConfigHelper configHelper;
    private GameHandler gameHandler;
    private Chess[] red, yellow, blue, green;
    private Chess[][] chessList;

    private static final GameController isntance = new GameController();

    public static GameController getIsntance() {
        return isntance;
    }

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
            completeArrowUsed = 0;
            whoseTurn = (int)(Math.random() * 4);
            rollNum = 0;
            gameHandler = new GameHandler();
            //TODO resources
            //TODO load chess
            chessList = new Chess[][]{red, yellow, blue, green};
            System.out.println("Initialization complete.");
        } catch (Exception e){
            e.printStackTrace();
            System.out.println("Fail to initialize game.");
        }
    }

    public void gameStart(){
        //LOCAL + ONE PLAYER
        configHelper = new ConfigHelper(Value.LOCAL, Value.RED);
        configHelper.setHost(true);
        configHelper.changePlayerType(Value.YELLOW, Value.AI);
        configHelper.changePlayerType(Value.BLUE, Value.AI);
        configHelper.changePlayerType(Value.GREEN, Value.AI);
        System.out.println("Game start.");
        turnStart();
    }

    private void turnStart(){
        resetRoll();
        System.out.println(Value.PLAYER_COLOR[whoseTurn] + "'s turn started.");
        if(configHelper.getGameType() == Value.LOCAL){
            if(configHelper.getPlayerType(whoseTurn) == Value.AI){
                gameHandler.postAIMsg();
            } // else return;
        } else if(configHelper.isHost()){
            if(configHelper.getPlayerType(whoseTurn) == Value.ONLINE){
                gameHandler.postHostRecMsg();
            } else if(configHelper.getPlayerType(whoseTurn) == Value.AI){
                gameHandler.postAIMsg();
            } // else return;
        } else { // client
            if (configHelper.getPlayerType(whoseTurn) != Value.LOCAL_HUMAN) { // ONLINE_HUMAN or AI
                gameHandler.postClientRecMsg();
            } // else return;
        }
    }

    private void turnEnd(){
        System.out.println(Value.PLAYER_COLOR[whoseTurn] + "'s turn end.");
        if(configHelper.getGameType() == Value.LOCAL){
            // No operation.
        } else if(configHelper.isHost()){
            gameHandler.postHostSendMsg();
        } else if(!configHelper.isHost()){
            gameHandler.postClientSendMsg();
        }
        if(!Value.TAKE_OFF_NUM.contains(rollNum)){
            nextTurn();
        } else{
            turnStart();
        }
    }

    private void nextTurn(){
        whoseTurn = (whoseTurn + 1) % 4;
        turnStart();
    }

    private int roll(){
        rollNum = (int)(Math.random() * 6) + 1;
        //TextView's operation
        if(configHelper.getPlayerType(whoseTurn) == Value.LOCAL_HUMAN){
            if(!canMove())turnEnd();
        }
        return rollNum;
    }

    private boolean restartGame(){
        try{
            initChessPos();
            for(int i = 0; i < chessList.length; ++i){
                for(int j = 0; j < chessList[j].length; ++j){
                    chessList[i][j].reset();
                }
            }
            System.out.println("Reset chess.");
            configHelper.reset();
            System.out.println("Reset helper.");
            //TODO complete arrow.
            whoseTurn = (int)(Math.random() * 4);
            rollNum = 0;
            gameStart();
        } catch(Exception e){
            e.printStackTrace();
            System.out.println("Fail to restart game.");
            return false;
        }
        return true;
    }

    private void initChessPos(){
        //TODO
    }

    private void resetRoll(){
        rollNum = 0;
    }

    private boolean canMove(){
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

    private void go(Chess chess){
        if(rollNum != 0){
            if(Value.TAKE_OFF_NUM.contains(rollNum)){
                if(chess == null){
                    System.out.println("You didn't select any chess.");
                } else if(!chess.isFlying() && !chess.isCompleted()){
                    chess.takeOff();
                    gameHandler.postFlyAudio();
                    System.out.println("Take off.");
                } else{
                    chess.move(rollNum);
                    gameHandler.postFlyAudio();
                    System.out.println("RollNum = " + rollNum + ", moving complete.");
                    chessStatusJudge(chess);
                }
            } else{
                chess.move(rollNum);
                gameHandler.postFlyAudio();
                System.out.println("RollNum = " + rollNum + ", moving complete.");
                chessStatusJudge(chess);
            }
            if(Value.COMBO_NUM.contains(rollNum)){
                //TODO combo
            }
        } else{
            System.out.println("Please roll first.");
        }
        turnEnd();
    }

    private void chessStatusJudge(Chess chess){
        //TODO
    }

    private void gameOverJudge(){
        //TODO
    }

    private void killJudge(Chess chess){
        //TODO
    }

    private Chess viewToChess(int player, View v){
        for(int i = 0; i < 4; ++i){
            Chess chess = chessList[player][i];
            if(chess.getX() == (int)v.getX() * 2 / Chess.getChessWidth() &&
                    chess.getY() == (int)(v.getY() - Chess.getTopLeftY()) * 2 / Chess.getChessWidth()){
                return chess;
            }
        }
        System.out.println("You didn't click any chess.");
        return null;
    }
}
