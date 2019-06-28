package com.potatofriedbread.astro;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import android.util.Log;
import android.util.Pair;
import android.view.animation.AnticipateOvershootInterpolator;
import android.widget.ImageView;

public class AnimationPlayer{

    private GameController gameController;
    private int[] rollNumDisplay;
    private PointEvaluator pointEvaluator;

    public AnimationPlayer(final GameController gameController){
        this.gameController = gameController;
        pointEvaluator = new PointEvaluator();
        rollNumDisplay = new int[15];
        for(int i = 0; i < 10; ++i){
            do {
                rollNumDisplay[i] = (int) (Math.random() * 6) + 1;
            } while(i > 0 && rollNumDisplay[i] == rollNumDisplay[i - 1]);
        }
    }

    //骰子动画
    public void playRollAnimation(final int rollNum){
        gameController.increaseAnimationCount();
        final ImageView roll = gameController.getGameActivity().getRoll();
        for(int i = 11; i < rollNumDisplay.length; ++i){
            rollNumDisplay[i] = rollNum;
        }
        final ValueAnimator rollAnimator = ValueAnimator
                .ofInt(rollNumDisplay)
                .setDuration(1500);
        rollAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                int index = (int)valueAnimator.getAnimatedValue();
                roll.setImageDrawable(Coordinate.getInstance().getRollImg(index));
            }
        });
        rollAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                gameController.decreaseAnimationCount();
                roll.setImageDrawable(Coordinate.getInstance().getRollImg(rollNum));
                ConfigHelper configHelper = gameController.getConfigHelper();
                int whoseTurn = gameController.getWhoseTurn();
                if(!gameController.canMove())gameController.getControlHandler().postTurnEnd();
                else{
                    gameController.getControlHandler().changeStateToMove(rollNum);
                }
                gameController.showToastShort("Roll number:" + rollNum);
            }
        });
        rollAnimator.start();
    }

    //起飞动画
    public void playTakeOffAnimator(final Chess chess){
        gameController.increaseAnimationCount();
        float startX = Coordinate.getInstance().mapToScreenX(Value.STARTS_X[chess.getPlayer()][chess.getChessNum()]);
        float startY = Coordinate.getInstance().mapToScreenY(Value.STARTS_Y[chess.getPlayer()][chess.getChessNum()]);
        Pair<Float, Float> startPoint = Pair.create(startX, startY);
        final float endX = Coordinate.getInstance().mapToScreenX(Value.PATHS_X[chess.getPlayer()][0]);
        final float endY = Coordinate.getInstance().mapToScreenY(Value.PATHS_Y[chess.getPlayer()][0]);
        Pair<Float, Float> endPoint = Pair.create(endX, endY);
        final ValueAnimator takeOffAnimator = ValueAnimator
                .ofObject(pointEvaluator, startPoint, endPoint)
                .setDuration(500);
        takeOffAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                Pair<Float, Float> point = (Pair<Float, Float>)valueAnimator.getAnimatedValue();
                chess.moveImg(point.first, point.second);
            }
        });
        takeOffAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                chess.moveImg(endX, endY);
                Log.d("TEST Choreographer", "Take off animation complete.");
                gameController.decreaseAnimationCount();
                gameController.getControlHandler().postTurnEnd();
            }
        });
        takeOffAnimator.start();
    }

    //沿着路线移动的动画
    public void playMoveAnimation(final Chess chess, final int from, final int step){
        gameController.increaseAnimationCount();
        final AnimatorSet moveAnimator = new AnimatorSet();
        ValueAnimator[] moveAnimatorSubs = new ValueAnimator[step];
        for(int i = 0; i < step; ++i){
            int fromSub = from + i;
            int toSub = fromSub + 1;
            if(fromSub >= Value.TERMINAL){
                fromSub = 2 * Value.TERMINAL - fromSub;
                toSub = 2 * Value.TERMINAL - toSub;
            }
            float startX = Coordinate.getInstance().mapToScreenX(Value.PATHS_X[chess.getPlayer()][fromSub]);
            float startY = Coordinate.getInstance().mapToScreenY(Value.PATHS_Y[chess.getPlayer()][fromSub]);
            Pair<Float, Float> startPoint = Pair.create(startX, startY);
            float endX = Coordinate.getInstance().mapToScreenX(Value.PATHS_X[chess.getPlayer()][toSub]);
            float endY = Coordinate.getInstance().mapToScreenY(Value.PATHS_Y[chess.getPlayer()][toSub]);
            Pair<Float, Float> endPoint = Pair.create(endX, endY);
            moveAnimatorSubs[i] = ValueAnimator
                    .ofObject(pointEvaluator, startPoint, endPoint)
                    .setDuration(300);
            moveAnimatorSubs[i].addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    Pair<Float, Float> point = (Pair<Float, Float>)valueAnimator.getAnimatedValue();
                    chess.moveImg(point.first, point.second);
                }
            });
        }
        int to = from + step;
        if(to > Value.TERMINAL){
            to = 2 * Value.TERMINAL - to;
        }
        final float endX = Coordinate.getInstance().mapToScreenX(Value.PATHS_X[chess.getPlayer()][to]);
        final float endY = Coordinate.getInstance().mapToScreenY(Value.PATHS_Y[chess.getPlayer()][to]);
        moveAnimatorSubs[step - 1].addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                gameController.decreaseAnimationCount();
                chess.moveImg(endX, endY);
                Log.d("TEST Choreographer", "Moving animation complete.");
                gameController.chessStatusJudge(chess);
                if(gameController.noAnimationLeft()){
                    gameController.getControlHandler().postTurnEnd();
                }
            }
        });
        moveAnimator.playSequentially(moveAnimatorSubs);
        moveAnimator.start();
    }

    //直线移动动画，适用于跳或飞
    public void playJumpAnimation(final Chess chess, final int from, final int to, final int jumpNum){
        gameController.increaseAnimationCount();
        float startX = Coordinate.getInstance().mapToScreenX(Value.PATHS_X[chess.getPlayer()][from]);
        float startY = Coordinate.getInstance().mapToScreenY(Value.PATHS_Y[chess.getPlayer()][from]);
        Pair<Float, Float> startPoint = Pair.create(startX, startY);
        final float endX = Coordinate.getInstance().mapToScreenX(Value.PATHS_X[chess.getPlayer()][to]);
        final float endY = Coordinate.getInstance().mapToScreenY(Value.PATHS_Y[chess.getPlayer()][to]);
        Pair<Float, Float> endPoint = Pair.create(endX, endY);
        final ValueAnimator jumpAnimator = ValueAnimator
                .ofObject(pointEvaluator, startPoint, endPoint)
                .setDuration(600);
        jumpAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                Pair<Float, Float> point = (Pair<Float, Float>)valueAnimator.getAnimatedValue();
                chess.moveImg(point.first, point.second);
            }
        });
        jumpAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                gameController.decreaseAnimationCount();
                chess.moveImg(endX, endY);
                if(from == Value.FLY_POINT){
                    gameController.flyKillJudge(chess);
                }
                final int newJumpNum = jumpNum - 1;
                gameController.killJudge(chess);
                if(newJumpNum > 0) {
                    if(from == Value.FLY_POINT){
                        gameController.chessJump(chess, newJumpNum); // 飞完再跳
                    } else if(to == Value.FLY_POINT){
                        gameController.chessFly(chess, newJumpNum); // 跳完再飞
                    } // else no new jump
                }
                if(gameController.noAnimationLeft()){
                    gameController.getControlHandler().postTurnEnd();
                }
            }
        });
        jumpAnimator.start();
    }

    //击杀动画，chess为被击杀的棋子
    public void playKillAnimation(final Chess chess, final int from){
        gameController.increaseAnimationCount();
        float startX = Coordinate.getInstance().mapToScreenX(Value.PATHS_X[chess.getPlayer()][from]);
        float startY = Coordinate.getInstance().mapToScreenY(Value.PATHS_Y[chess.getPlayer()][from]);
        Pair<Float, Float> startPoint = Pair.create(startX, startY);
        final float endX = Coordinate.getInstance().mapToScreenX(Value.STARTS_X[chess.getPlayer()][chess.getChessNum()]);
        final float endY = Coordinate.getInstance().mapToScreenY(Value.STARTS_Y[chess.getPlayer()][chess.getChessNum()]);
        Pair<Float, Float> endPoint = Pair.create(endX, endY);
        final ValueAnimator killAnimator = ValueAnimator
                .ofObject(pointEvaluator, startPoint, endPoint)
                .setDuration(600);
        killAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                Pair<Float, Float> point = (Pair<Float, Float>)valueAnimator.getAnimatedValue();
                chess.moveImg(point.first, point.second);
            }
        });
        killAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                chess.moveImg(endX, endY);
                gameController.decreaseAnimationCount();
                if(gameController.noAnimationLeft()){
                    gameController.getControlHandler().postTurnEnd();
                }
            }
        });
        killAnimator.start();
    }

    //到达终点动画
    public void playTerminateAnimation(final Chess chess, int from){
        gameController.increaseAnimationCount();
        float startX = Coordinate.getInstance().mapToScreenX(Value.PATHS_X[chess.getPlayer()][from]);
        float startY = Coordinate.getInstance().mapToScreenY(Value.PATHS_Y[chess.getPlayer()][from]);
        Pair<Float, Float> startPoint = Pair.create(startX, startY);
        final float endX = Coordinate.getInstance().mapToScreenX(Value.STARTS_X[chess.getPlayer()][chess.getChessNum()]);
        final float endY = Coordinate.getInstance().mapToScreenY(Value.STARTS_Y[chess.getPlayer()][chess.getChessNum()]);
        Pair<Float, Float> endPoint = Pair.create(endX, endY);
        final ValueAnimator terminateAnimator = ValueAnimator
                .ofObject(pointEvaluator, startPoint, endPoint)
                .setDuration(600);
        terminateAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                Pair<Float, Float> point = (Pair<Float, Float>)valueAnimator.getAnimatedValue();
                chess.moveImg(point.first, point.second);
            }
        });
        terminateAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                chess.moveImg(endX, endY);
                gameController.decreaseAnimationCount();
                //if(gameController.noAnimationLeft()){
                if(!gameController.gameOverJudge()){
                    gameController.getControlHandler().postTurnEnd();
                }
                //}
            }
        });
        terminateAnimator.start();
    }

    public class PointEvaluator implements TypeEvaluator {
        @Override
        public Object evaluate(float fraction, Object startValue, Object endValue) {
            Pair<Float, Float> startPoint = (Pair<Float, Float>) startValue;
            Pair<Float, Float> endPoint = (Pair<Float, Float>) endValue;
            float x = startPoint.first + fraction * (endPoint.first - startPoint.first);
            float y = startPoint.second + fraction * (endPoint.second - startPoint.second);
            Pair<Float, Float> point = Pair.create(x, y);
            return point;
        }
    }
}
