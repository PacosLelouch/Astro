package com.potatofriedbread.astro;


import android.content.Context;
import android.media.MediaPlayer;
import android.provider.MediaStore;

public class AudioPlayer {

    private GameController gameController;
    private MediaPlayer myMediaPlayer;

    public AudioPlayer(GameController gameController){
        this.gameController = gameController;
        //加载房间音乐
        myMediaPlayer = MediaPlayer.create(gameController.getContext(), R.raw.musicplay);
    }

    public void playLobbyBGM(){
        myMediaPlayer.start();
        myMediaPlayer.setLooping(true);
    }

    public void pauseLobbyBGM(){
        myMediaPlayer.pause();
    }

    public boolean isPlayingLobbyBGM(){
        return myMediaPlayer.isPlaying();
    }

    public void playFlyAudio(){
    }

    public void playKillAudio(){
    }
}
