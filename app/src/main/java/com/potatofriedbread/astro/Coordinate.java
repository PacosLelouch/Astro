package com.potatofriedbread.astro;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.support.v7.widget.Toolbar;

import java.io.InputStream;

public class Coordinate {

    private GameActivity gameActivity;
    private float leftX, topY, mapWidth, mapHeight, chessWidth;
    private static Coordinate instance = null;

    private BitmapDrawable rollImg[];
    private BitmapDrawable chessImg[][];

    public static void createInstance(GameActivity gameActivity){
        instance = new Coordinate(gameActivity);
    }

    public static Coordinate getInstance(){
        return instance;
    }

    private Coordinate(GameActivity gameActivity){
        this.gameActivity = gameActivity;
        GameController.getInstance().increaseLoadCount();
        loadResourceImg();
        setCoordinate(gameActivity);
    }

    public void setCoordinate(final GameActivity gameActivity){
        this.gameActivity = gameActivity;
        final ImageView imageView = gameActivity.getMap();
        final Toolbar toolbar = gameActivity.getToolbar();
        imageView.post(new Runnable() {
            @Override
            public void run() {
                GameController.getInstance().increaseLoadCount();
                leftX = imageView.getX();
                topY = imageView.getY();
                mapWidth = imageView.getWidth();
                mapHeight = imageView.getHeight();
                imageView.post(new Runnable() {
                    @Override
                    public void run() {
                        if(mapHeight > mapWidth) {
                            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(imageView.getLayoutParams());
                            layoutParams.width = (int)mapWidth;
                            layoutParams.height = (int)mapWidth;
                            layoutParams.topMargin = (int)topY;
                            layoutParams.leftMargin = (int)leftX;
                            imageView.setLayoutParams(layoutParams);
                            mapHeight = mapWidth;
                        } else if(mapWidth > mapHeight){
                            WindowManager windowManager = gameActivity.getWindowManager();
                            DisplayMetrics outMetrics = new DisplayMetrics();
                            windowManager.getDefaultDisplay().getMetrics(outMetrics);
                            float windowWidth = outMetrics.widthPixels;
                            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(imageView.getLayoutParams());
                            layoutParams.width = (int)mapHeight;
                            layoutParams.height = (int)mapHeight;
                            layoutParams.topMargin = (int)topY;
                            layoutParams.leftMargin = (int)((windowWidth - mapHeight) / 2);
                            imageView.setLayoutParams(layoutParams);
                            mapWidth = mapHeight;
                            leftX = (windowWidth - mapHeight) / 2;
                        }
                        float toolbarHeight = toolbar.getHeight();
                        topY += toolbarHeight;
                        chessWidth = mapWidth / 18;
                        GameController.getInstance().increaseLoadCount();
                        final ImageView roll = gameActivity.getRoll();
                        roll.post(new Runnable() {
                            @Override
                            public void run() {
                                RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(roll.getLayoutParams());
                                layoutParams.width = (int)chessWidth;
                                layoutParams.height = (int)chessWidth;
                                roll.setLayoutParams(layoutParams);
                                roll.setTranslationX(leftX + mapWidth / 2 - chessWidth / 2);
                                roll.setTranslationY(topY + mapHeight / 2 - chessWidth / 2);
                                GameController.getInstance().decreaseLoadCount();
                                if(GameController.getInstance().noLoadingLeft()){
                                    gameActivity.gameStart();
                                }
                            }
                        });
                        Log.d("TEST Choreographer", "Measured: " + leftX + " " + topY + " " + mapWidth + " " + mapHeight);
                        Log.d("TEST Choreographer", "ChessWidth: " + chessWidth);
                        /*
                        Log.d("TEST Choreographer", "Image: " + imageView.getWidth() + " " + imageView.getHeight());*/
                        GameController.getInstance().loadChess();
                        GameController.getInstance().decreaseLoadCount();
                        if(GameController.getInstance().noLoadingLeft()){
                            gameActivity.gameStart();
                        }
                    }
                });
                GameController.getInstance().decreaseLoadCount();
                if(GameController.getInstance().noLoadingLeft()){
                    gameActivity.gameStart();
                }
            }
        });
    }

    private void loadResourceImg(){
        int[][] chessImgSrc = {
                {R.drawable.red, R.drawable.red_light, R.drawable.complete},
                {R.drawable.yellow, R.drawable.yellow_light, R.drawable.complete},
                {R.drawable.blue, R.drawable.blue_light, R.drawable.complete},
                {R.drawable.green, R.drawable.green_light, R.drawable.complete},
        };
        int[] rollImgSrc = new int[]{
                R.drawable.roll0,
                R.drawable.roll1,
                R.drawable.roll2,
                R.drawable.roll3,
                R.drawable.roll4,
                R.drawable.roll5,
                R.drawable.roll6,
        };
        chessImg = new BitmapDrawable[4][3];
        rollImg = new BitmapDrawable[7];
        BitmapFactory.Options opt = new BitmapFactory.Options();
        opt.inPreferredConfig = Bitmap.Config.RGB_565;
        //opt.inPurgeable = true;
        //opt.inInputShareable = true;
        for(int i = 0; i < chessImgSrc.length; ++i) {
            for(int j = 0; j < chessImgSrc[i].length; ++j){
                InputStream is = gameActivity.getResources().openRawResource(chessImgSrc[i][j]);
                Bitmap bm = BitmapFactory.decodeStream(is, null, opt);
                chessImg[i][j] = new BitmapDrawable(gameActivity.getResources(), bm);
            }
        }
        for(int i = 0; i < rollImgSrc.length; ++i){
            InputStream is = gameActivity.getResources().openRawResource(rollImgSrc[i]);
            Bitmap bm = BitmapFactory.decodeStream(is, null, opt);
            rollImg[i] = new BitmapDrawable(gameActivity.getResources(), bm);
        }
    }

    public float getChessWidth(){
        return chessWidth;
    }

    public int screenToMapX(float screenX){
        return (int)((screenX - leftX) / (chessWidth / 2));
    }

    public int screenToMapY(float screenY){
        return (int)((screenY - topY) / (chessWidth / 2));
    }

    public float mapToScreenX(int mapX){
        return mapX * chessWidth / 2 + leftX;
    }

    public float mapToScreenY(int mapY){
        return mapY * chessWidth / 2 + topY;
    }

    public boolean clickTheChess(Chess chess, View view){
        int viewX = screenToMapX(view.getX()), viewY = screenToMapY(view.getY());
        return chess.getX() == viewX &&
                chess.getY() == viewY;
    }

    public BitmapDrawable getChessImg(int player, int index){
        return chessImg[player][index];
    }

    public BitmapDrawable getRollImg(int index) {
        return rollImg[index];
    }
}
