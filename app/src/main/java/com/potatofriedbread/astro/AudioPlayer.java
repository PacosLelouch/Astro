package com.potatofriedbread.astro;


import android.media.MediaPlayer;

public class AudioPlayer {

    private GameController gameController;
    private MediaPlayer lobbyMediaPlayer, gameOverMediaPlayer;

    public AudioPlayer(GameController gameController){
        this.gameController = gameController;
        //加载房间音乐
        lobbyMediaPlayer = MediaPlayer.create(gameController.getContext(), R.raw.musicplay);
        //lobbyMediaPlayer.prepareAsync();
        //加载游戏结束音乐
        gameOverMediaPlayer = MediaPlayer.create(gameController.getContext(), R.raw.musicgameover);
        //gameOverMediaPlayer.prepareAsync();
    }

    public void playLobbyBGM(){
        lobbyMediaPlayer.start();
        lobbyMediaPlayer.setLooping(true);
    }

    public void pauseLobbyBGM(){
        lobbyMediaPlayer.pause();
    }

    public void playGameOverBGM(){
        gameOverMediaPlayer.start();
        gameOverMediaPlayer.setLooping(true);
    }

    public void pauseGameOverBGM(){
        gameOverMediaPlayer.pause();
    }

    public boolean isPlayingLobbyBGM(){
        return lobbyMediaPlayer.isPlaying();
    }

    public void playFlyAudio(){
    }

    public void playKillAudio(){
    }
}
