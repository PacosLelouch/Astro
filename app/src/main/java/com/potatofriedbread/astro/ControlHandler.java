package com.potatofriedbread.astro;

import android.os.Handler;

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
                gameController.rollByLocalPlayer();
            }
        }, 1000);
    }

    public void getAIMove(final int rollNum){
        super.postDelayed(new Runnable() {
            @Override
            public void run() {
                AgentAI.go(gameController.getChessList(), gameController.getWhoseTurn(), rollNum);
            }
        }, 1000);
    }

    public void postTurnStart(){
        super.post(new Runnable() {
            @Override
            public void run() {
                gameController.turnStart();
            }
        });
    }

    public void postChangeStateToMove(final int rollNum){
        super.post(new Runnable() {
            @Override
            public void run() {
                gameController.setState(Value.STATE_MOVE_CHESS);
                if(gameController.getConfigHelper().getPlayerType(gameController.getWhoseTurn()) == Value.LOCAL_HUMAN){
                    if(gameController.getConfigHelper().getGameType() == Value.ONLINE_LAN){
                        gameController.getLANHandler().getOnlineMoveLAN();
                    } else if(gameController.getConfigHelper().getGameType() == Value.ONLINE_SERVER){
                        gameController.getServerHandler().getOnlineMoveServer();
                    }
                } else if(gameController.getConfigHelper().getPlayerType(gameController.getWhoseTurn()) == Value.AI){
                    if(gameController.getConfigHelper().getGameType() == Value.LOCAL) {
                        gameController.getControlHandler().getAIMove(rollNum);
                    } else if(gameController.getConfigHelper().getGameType() == Value.ONLINE_LAN){
                        if(gameController.getConfigHelper().isHost()) {
                            gameController.getLANHandler().postAIMoveLAN(rollNum);
                            gameController.getLANHandler().getOnlineMoveLAN();
                        } else{
                            gameController.getLANHandler().getOnlineMoveLAN();
                        }
                    } else if(gameController.getConfigHelper().getGameType() == Value.ONLINE_SERVER){
                        gameController.getServerHandler().getOnlineMoveServer();
                    }
                } else if(gameController.getConfigHelper().getPlayerType(gameController.getWhoseTurn()) == Value.ONLINE_HUMAN) {
                    if(gameController.getConfigHelper().getGameType() == Value.ONLINE_LAN){
                        gameController.getLANHandler().getOnlineMoveLAN();
                    } else if(gameController.getConfigHelper().getGameType() == Value.ONLINE_SERVER){
                        gameController.getServerHandler().getOnlineMoveServer();
                    } // else{} // Do nothing.
                }
            }
        });
    }

    public void postTurnEnd(){
        super.post(new Runnable() {
            @Override
            public void run() {
                gameController.turnEnd();
            }
        });
    }
}
