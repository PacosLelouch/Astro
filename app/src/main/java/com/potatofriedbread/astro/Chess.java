package com.potatofriedbread.astro;

import android.widget.ImageView;

import java.io.Serializable;

public class Chess implements Serializable {
    private static final long serialVersionUID = 1L;
    private static int chessWidth, topLeftY;
    private boolean flying, completed;
    private int nowPos, player, chessNum;
    private ImageView img;

    public static void loadMap(int chessWidth, int topLeftY){
        Chess.chessWidth = chessWidth;
        Chess.topLeftY = topLeftY;
    }

    // Load chess image.
    public Chess(int player, int chessNum, ImageView img){
        reset();
        this.player = player;
        this.chessNum = chessNum;
        this.img = img;
    }

    public void reset(){
        completed = false;
        flying = false;
        nowPos = 0;
    }

    public void move(int posX, int posY){
        img.setX(posX * chessWidth);
        img.setY(posY * chessWidth + topLeftY);
    }

    public void move(int step){
        //max: 57
        int stepLeft = nowPos + step - Value.TERMINAL;
        if(stepLeft < 0){
            stepLeft = 0;
        }
        if(isFlying()){
            move(Value.PATHS_X[player][nowPos + step - stepLeft], Value.PATHS_Y[player][nowPos + step - stepLeft]);
            /*switch(player){
                case Value.RED:
                    move(Value.RED_PATH_X[nowPos + step - stepLeft], Value.RED_PATH_Y[nowPos + step - stepLeft]);
                    break;
                case Value.YELLOW:
                    move(Value.YELLOW_PATH_X[nowPos + step - stepLeft], Value.YELLOW_PATH_Y[nowPos + step - stepLeft]);
                    break;
                case Value.BLUE:
                    move(Value.BLUE_PATH_X[nowPos + step - stepLeft], Value.BLUE_PATH_Y[nowPos + step - stepLeft]);
                    break;
                case Value.GREEN:
                    move(Value.GREEN_PATH_X[nowPos + step - stepLeft], Value.GREEN_PATH_Y[nowPos + step - stepLeft]);
                    break;
            }*/
        }
        nowPos += step;
    }

    public int getX(){
        return (int)img.getX() / chessWidth;
    }

    public int getY(){
        return (int)(img.getY() - topLeftY) / chessWidth;
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
    }

    public void takeOff(){
        nowPos = 0;
        move(Value.PATHS_X[player][nowPos], Value.PATHS_Y[player][nowPos]);
        /*switch(player) {
            case Value.RED:
                move(Value.RED_PATH_X[nowPos], Value.RED_PATH_Y[nowPos]);
                break;
            case Value.YELLOW:
                move(Value.YELLOW_PATH_X[nowPos], Value.YELLOW_PATH_Y[nowPos]);
                break;
            case Value.BLUE:
                move(Value.BLUE_PATH_X[nowPos], Value.BLUE_PATH_Y[nowPos]);
                break;
            case Value.GREEN:
                move(Value.GREEN_PATH_X[nowPos], Value.GREEN_PATH_Y[nowPos]);
                break;
            default:
                System.out.println("Fail to take off.");
                return;
        }*/
        setFlying(true);
    }

    public void killed(){
        nowPos = 0;
        setFlying(false);
        System.out.println("Chess are killed.");
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

    public static int getChessWidth(){
        return chessWidth;
    }

    public static int getTopLeftY(){
        return topLeftY;
    }
}
