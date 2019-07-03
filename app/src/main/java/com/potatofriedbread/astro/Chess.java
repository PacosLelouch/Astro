package com.potatofriedbread.astro;

import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import java.io.Serializable;

public class Chess implements Serializable {
    private static final long serialVersionUID = 1L;

    private boolean flying, completed;
    private int nowPos, player, chessNum;
    private ImageView img;

    // Load chess image.
    public Chess(final int player, final int chessNum, ImageView img){
        this.player = player;
        this.chessNum = chessNum;
        this.img = img;
        img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("TEST Choreographer", "Click chess, " + Value.PLAYER_COLOR[player] + " " + chessNum);
                Chess chess = Chess.this;
                GameController.getInstance().goByLocalPlayer(chess);
            }
        });
        reset();
    }

    public void reset(){
        completed = false;
        flying = false;
        nowPos = 0;
    }

    public void moveImg(final float screenX, final float screenY){
        /*img.post(new Runnable() {
            @Override
            public void run() {
                img.setTranslationX(screenX);
                img.setTranslationY(screenY);
            }
        });*/
        img.setTranslationX(screenX);
        img.setTranslationY(screenY);
    }
    /*
    public void move(int posX, int posY){
        img.setX(Coordinate.getInstance().mapToScreenX(posX));
        img.setY(Coordinate.getInstance().mapToScreenY(posY));
    }*/

    public void move(int step){
        if(isFlying()){
            nowPos += step;
            if(nowPos > Value.TERMINAL){
                nowPos = 2 * Value.TERMINAL - nowPos;
            }
        }
    }

    public void changeImage(int index){
        img.setImageDrawable(Coordinate.getInstance().getChessImg(player, index));
    }

    public int getX(){
        if(isFlying()){
            return Value.PATHS_X[player][nowPos];
        }
        return Value.STARTS_X[player][chessNum];
    }

    public int getY(){
        if(isFlying()){
            return Value.PATHS_Y[player][nowPos];
        }
        return Value.STARTS_Y[player][chessNum];
    }

    public int getPlayer(){
        return player;
    }

    public int getChessNum() {
        return chessNum;
    }

    public int getNowPos(){
        return nowPos;
    }

    public void setNowPos(int value){
        nowPos = value;
    }

    public boolean isCompleted(){
        return completed;
    }

    public boolean isFlying() {
        return flying;
    }

    public void completeTour(){
        nowPos = 0;
        setFlying(false);
        setCompleted(true);
        img.setImageResource(R.drawable.complete);
    }

    public void takeOff(){
        nowPos = 0;
        //move(Value.PATHS_X[player][nowPos], Value.PATHS_Y[player][nowPos]);
        setFlying(true);
    }

    public void killed(){
        nowPos = 0;
        setFlying(false);
        Log.d("TEST Choreographer", "Chess are killed.");
    }

    public ImageView getImg(){
        return img;
    }

    public void setFlying(boolean value){
        flying = value;
    }

    public void setCompleted(boolean value){
        completed = value;
    }

    public int rival(){ // return (player + 2) % 4;
        switch(player){
            case Value.RED:
                return Value.BLUE;
            case Value.YELLOW:
                return Value.GREEN;
            case Value.BLUE:
                return Value.RED;
            case Value.GREEN:
                return Value.YELLOW;
            default:
                return -1;
        }
    }
}
