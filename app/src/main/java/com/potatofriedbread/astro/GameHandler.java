package com.potatofriedbread.astro;

import android.os.Handler;
import android.os.Message;
//TODO: 想拆开成多几个Handler，明天再说吧
public class GameHandler extends Handler {

    Thread AIDelay, hostRecMsg, clientRecMsg, hostSendMsg, clientSendMsg, flyAudio;

    public GameHandler(){
        super();
        AIDelay = new Thread(){

        };
        hostRecMsg = new Thread(){

        };
        clientRecMsg = new Thread(){

        };
        hostSendMsg = new Thread(){

        };
        clientSendMsg = new Thread(){

        };
        flyAudio = new Thread(){

        };
    }

    @Override
    public void handleMessage(Message msg) {
        //TODO
    }

    public void postAIMsg(){
        super.postDelayed(AIDelay, 2000);
    }

    public void postHostRecMsg(){
        super.post(hostRecMsg);
    }

    public void postClientRecMsg(){
        super.post(clientRecMsg);
    }

    public void postHostSendMsg(){
        super.post(hostSendMsg);
    }

    public void postClientSendMsg(){
        super.post(clientSendMsg);
    }

    public void postFlyAudio(){
        super.post(flyAudio);
    }
}
