package com.potatofriedbread.astro;

import android.os.Handler;

public class LANHandler extends Handler {

    private GameController gameController;

    public LANHandler(GameController gameController){
        super();
        this.gameController = gameController;
    }

    public void postAIRollLAN(){
        super.post(new Runnable() {
            @Override
            public void run() {
                //TODO
            }
        });
    }

    public void postOnlineRollLAN(){
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
}
