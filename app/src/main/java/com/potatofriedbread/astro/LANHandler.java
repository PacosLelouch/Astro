package com.potatofriedbread.astro;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.util.Pair;

import java.util.HashMap;
import java.util.concurrent.LinkedBlockingQueue;

public class LANHandler extends Handler {

    private GameController gameController;
    private LinkedBlockingQueue<Bundle> operationQueue;

    public LANHandler(GameController gameController){
        super();
        this.gameController = gameController;
        this.operationQueue = new LinkedBlockingQueue<>();
    }
    /*
    public void newRoom(){ // 按下后建房，（线程1）发udp广播，（线程2）收tcp连接
        super.post(new Runnable() {
            @Override
            public void run() {
                //TODO
            }
        });
    }

    public void joinIn(){ // 按下后加房，（线程1）发tcp连接
        super.post(new Runnable() {
            @Override
            public void run() {
                //TODO
            }
        });
    }*/

    public void getOnlineRollLAN(){
        new Thread(){
            @Override
            public void run(){
                boolean isExit = false;
                while(!isExit) {
                    try {
                        Bundle bundle = operationQueue.take();
                        if (bundle != null && "roll".equals(bundle.getString("msgType"))) {
                            final int rollNum = Integer.parseInt(bundle.getString("rollNum"));
                            LANHandler.super.post(new Runnable() {
                                @Override
                                public void run() {
                                    gameController.onlineRoll(rollNum);
                                }
                            });
                            isExit = true;
                        }
                        operationQueue.poll();
                    } catch (Exception e) {
                        Log.e("TEST Choreographer", "Fail to get whose turn.");
                        e.printStackTrace();
                        isExit = true;
                    }
                }
            }
        }.start();
    }

    public void postOnlineRollLAN(){
        super.post(new Runnable() {
            @Override
            public void run() {
                HashMap<String, Object> hashMap = new HashMap<>();
                hashMap.put("msgType", "roll");
                int rollNum = (int)(Math.random() * 6) + 1;
                hashMap.put("rollNum", rollNum);
                gameController.getClient().sendMsgToServer(hashMap.toString());
            }
        });
    }

    public void getOnlineMoveLAN(){
        new Thread(){
            @Override
            public void run(){
                boolean isExit = false;
                while(!isExit) {
                    try {
                        Bundle bundle = operationQueue.take();
                        if (bundle != null && "move".equals(bundle.getString("msgType"))) {
                            final int player = Integer.parseInt(bundle.getString("player"));
                            final int chessNum = Integer.parseInt(bundle.getString("chessNum"));
                            int nowPos = Integer.parseInt(bundle.getString("nowPos"));
                            final Chess chess = gameController.getChessList()[player][chessNum];
                            chess.setNowPos(nowPos);
                            LANHandler.super.post(new Runnable() {
                                @Override
                                public void run() {
                                    Log.e("TEST go", "go chess " + player + " " + chessNum);
                                    gameController.go(chess);
                                }
                            });
                            isExit = true;
                        }
                        operationQueue.poll();
                    } catch (Exception e) {
                        Log.e("TEST Choreographer", "Fail to get whose turn.");
                        e.printStackTrace();
                        isExit = true;
                    }
                }
            }
        }.start();
    }

    public void postAIMoveLAN(final int rollNum){
        super.postDelayed(new Runnable() {
            @Override
            public void run() {
                Pair<Integer, Chess> pair = AgentAI.go(gameController.getChessList(), gameController.getWhoseTurn(), rollNum);
                postOnlineMoveLAN(pair.second.getPlayer(), pair.second.getChessNum(), pair.first);
            }
        }, 1000);
    }

    public void postOnlineMoveLAN(final int player, final int chessNum, final int nowPos){
        super.post(new Runnable() {
            @Override
            public void run() {
                HashMap<String, Object> hashMap = new HashMap<>();
                hashMap.put("msgType", "move");
                hashMap.put("player", player);
                hashMap.put("chessNum", chessNum);
                hashMap.put("nowPos", nowPos);
                gameController.getClient().sendMsgToServer(hashMap.toString());
            }
        });
    }
    /*
    public void postHostTurnEndLAN(){
        super.post(new Runnable() {
            @Override
            public void run() {
                //TODO
            }
        });
    }

    public void postClientTurnEndLAN(){
        super.post(new Runnable() {
            @Override
            public void run() {
                //TODO
            }
        });
    }
    */
    public void postWhoseTurnLAN(final int whoseTurn){
        super.post(new Runnable() {
            @Override
            public void run() {
                HashMap<String, Object> hashMap = new HashMap<>();
                hashMap.put("msgType", "turn");
                hashMap.put("whoseTurn", whoseTurn);
                Log.e("TEST", "whose turn send to server. " + whoseTurn);
                gameController.getClient().sendMsgToServer(hashMap.toString());
            }
        });
    }

    public void getWhoseTurnLAN(){
        new Thread(){
            @Override
            public void run(){
                boolean isExit = false;
                while(!isExit) {
                    Log.e("TEST", "get whose turn queue size " + operationQueue.size());
                    try {
                        Bundle bundle = operationQueue.take();
                        Log.e("TEST Choreographer","Get whose turn lan Bundle " + bundle.toString());
                        if (bundle != null && "turn".equals(bundle.getString("msgType"))) {
                            final int whoseTurn = Integer.parseInt(bundle.getString("whoseTurn"));
                            Log.e("TEST Choreographer","Get whose turn lan " + whoseTurn);
                            LANHandler.super.post(new Runnable() {
                                @Override
                                public void run() {
                                    gameController.setWhoseTurn(whoseTurn);
                                }
                            });
                            isExit = true;
                        }
                        operationQueue.poll();
                    } catch (Exception e) {
                        Log.e("TEST Choreographer", "Fail to get whose turn.");
                        e.printStackTrace();
                        isExit = true;
                    }
                }
            }
        }.start();
    }

    public void postChangeTypeLAN(final int player, final int targetType){
        super.post(new Runnable() {
            @Override
            public void run() {
                HashMap<String, Object> hashMap = new HashMap<>();
                hashMap.put("msgType", "charge");
                hashMap.put("player", player);
                hashMap.put("targetType", targetType);
                gameController.getClient().sendMsgToServer(hashMap.toString());
            }
        });
    }

    public void getChangeTypeLAN(final int player, final int targetType0){
        new Thread(){
            @Override
            public void run(){
                boolean isExit = false;
                while(!isExit) {
                    try {
                        //Bundle bundle = operationQueue.take();
                        //if (bundle != null && "charge".equals(bundle.getString("msgType"))) {
                            int targetTypePre = targetType0;
                            if(targetTypePre == Value.LOCAL_HUMAN && gameController.getConfigHelper().getLocalPlayer() != player){
                                targetTypePre = Value.ONLINE_HUMAN;
                            }
                            final int targetType = targetTypePre;
                            LANHandler.super.post(new Runnable() {
                                @Override
                                public void run() {
                                    gameController.setPlayerTypeWhilePlaying(player, targetType);

                                }
                            });
                            isExit = true;
                        //}
                        //operationQueue.poll();
                    } catch (Exception e) {
                        Log.e("TEST Choreographer", "Fail to get whose turn.");
                        e.printStackTrace();
                        isExit = true;
                    }
                }
            }
        }.start();
    }

    @Override
    public void handleMessage(Message msg){
        super.handleMessage(msg);
        if(msg == null){
            return;
        }
        try {
            Bundle bundle = msg.getData();
            Log.e("TEST", "handle message queue size " + operationQueue.size());
            if ("charge".equals(bundle.getString("msgType"))) {
                int player = Integer.parseInt(bundle.getString("player"));
                int targetType0 = Integer.parseInt(bundle.getString("targetType"));
                getChangeTypeLAN(player, targetType0);
            } else {
                operationQueue.offer(bundle);
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }
}
