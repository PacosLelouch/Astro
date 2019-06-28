package com.potatofriedbread.astro;

import android.os.Handler;

public class LANHandler extends Handler {

    private GameController gameController;

    public LANHandler(GameController gameController){
        super();
        this.gameController = gameController;
    }

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
    }

    public void getOnlineRollLAN(){
        super.post(new Runnable() {
            @Override
            public void run() {
                //TODO
            }
        });
    }

    public void postAIRollLAN(){
        super.post(new Runnable() {
            @Override
            public void run() {
                //TODO
            }
        });
    }

    public void getOnlineMoveLAN(){
        super.post(new Runnable() {
            @Override
            public void run() {
                //TODO
            }
        });
    }

    public void postAIMoveLAN(final int rollNum){
        super.post(new Runnable() {
            @Override
            public void run() {
                //TODO
            }
        });
    }

    public void postHostMoveLAN(final Chess chess, final int rollNum){
        super.post(new Runnable() {
            @Override
            public void run() {
                //TODO
            }
        });
    }

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

    public void postWhoseTurnLan(int whoseTurn){
        super.post(new Runnable() {
            @Override
            public void run() {
                //TODO
            }
        });
    }

    public void getWhoseTurnLAN(){
        super.post(new Runnable() {
            @Override
            public void run() {
                //TODO
            }
        });
    }
}
