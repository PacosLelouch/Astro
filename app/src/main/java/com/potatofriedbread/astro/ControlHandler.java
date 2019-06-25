package com.potatofriedbread.astro;

import android.os.Handler;
import android.os.Message;

public class ControlHandler extends Handler {

    private GameController gameController;

    public ControlHandler(GameController gameController){
        super();
        this.gameController = gameController;
    }

    public void getAIRoll(){
        super.postDelayed(new Runnable() {
            @Override
            public void run() {
                gameController.roll();

            }
        }, 1000);
    }

    public void getAIMove(final int rollNum){
        super.postDelayed(new Runnable() {
            @Override
            public void run() {
                //TODO
            }
        }, 1000);
    }
}
