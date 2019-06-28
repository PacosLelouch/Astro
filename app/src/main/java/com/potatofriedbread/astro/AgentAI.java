package com.potatofriedbread.astro;

public class AgentAI {
    private static final int type = Value.AI_TYPE[1];

    public static void go(Chess[][] chessList, int player, int rollNum){
        Chess chess = chooseAChess(chessList, player, rollNum);
        GameController.getInstance().go(chess);
    }

    private static Chess chooseAChess(Chess[][] chessList, int player, int rollNum){
        if(type == Value.AI_TYPE[0]) {
            return chooseAChessType0(chessList, player, rollNum);
        } else if(type == Value.AI_TYPE[1]){
            return chooseAChessType1(chessList, player, rollNum);
        } else if(type == Value.AI_TYPE[2]){
            return chooseAChessType2(chessList, player, rollNum);
        }
        return null;
    }

    // Peaceful
    private static Chess chooseAChessType0(Chess[][] chessList, int player, int rollNum){
        Chess[] yourChess = chessList[player];
        if(Value.TAKE_OFF_NUM.contains(rollNum)){
            for(int i = 0; i < yourChess.length; ++i){
                if(!yourChess[i].isFlying() && !yourChess[i].isCompleted()){
                    return yourChess[i];
                }
            }
        }
        for(int i = 0; i < yourChess.length; ++i){
            if(yourChess[i].getNowPos() + rollNum == Value.TERMINAL){
                return yourChess[i];
            }
        }
        for(int i = 0; i < yourChess.length; ++i){
            if(yourChess[i].isFlying() && Value.JUMP_POINT.contains(yourChess[i].getNowPos() + rollNum)){
                return yourChess[i];
            }
        }
        int maxIndex = 0, maxPos = 0;
        for(int i = 0; i < yourChess.length; ++i){
            if(yourChess[i].isFlying()){
                if(yourChess[i].getNowPos() >= maxPos){
                    maxPos = yourChess[i].getNowPos();
                    maxIndex = i;
                }
            }
        }
        return yourChess[maxIndex];
    }

    // A bit aggressive.
    private static Chess chooseAChessType1(Chess[][] chessList, int player, int rollNum){
        Chess[] yourChess = chessList[player];
        if(Value.TAKE_OFF_NUM.contains(rollNum)){
            for(int i = 0; i < yourChess.length; ++i){
                if(!yourChess[i].isFlying() && !yourChess[i].isCompleted()){
                    return yourChess[i];
                }
            }
        }
        for(int i = 0; i < yourChess.length; ++i){
            if(yourChess[i].getNowPos() + rollNum == Value.TERMINAL){
                return yourChess[i];
            }
        }
        for(int i = 0; i < yourChess.length; ++i){
            if(isConflict(chessList, player ,yourChess[i].getNowPos() + rollNum)){
                return yourChess[i];
            }
        }
        for(int i = 0; i < yourChess.length; ++i){
            if(yourChess[i].isFlying() && Value.JUMP_POINT.contains(yourChess[i].getNowPos() + rollNum)){
                return yourChess[i];
            }
        }
        int maxIndex = -1, maxPos = -1;
        for(int i = 0; i < yourChess.length; ++i){
            if(yourChess[i].isFlying()){
                if(yourChess[i].getNowPos() >= maxPos && !Value.SAFE_POINT.contains(yourChess[i].getNowPos())){
                    maxPos = yourChess[i].getNowPos();
                    maxIndex = i;
                }
            }
        }
        if(maxIndex == -1){
            for(int i = 0; i < yourChess.length; ++i){
                if(yourChess[i].isFlying()){
                    if(yourChess[i].getNowPos() >= maxPos){
                        maxPos = yourChess[i].getNowPos();
                        maxIndex = i;
                    }
                }
            }
        }
        return yourChess[maxIndex];
    }

    // Very aggressive.
    private static Chess chooseAChessType2(Chess[][] chessList, int player, int rollNum){
        Chess[] yourChess = chessList[player];
        for(int i = 0; i < yourChess.length; ++i){
            if(isConflict(chessList, player ,yourChess[i].getNowPos() + rollNum) ||
                    isConflictFly(chessList, player ,yourChess[i].getNowPos() + rollNum)){
                return yourChess[i];
            }
        }
        if(Value.TAKE_OFF_NUM.contains(rollNum)){
            for(int i = 0; i < yourChess.length; ++i){
                if(!yourChess[i].isFlying()  && !yourChess[i].isCompleted()){
                    return yourChess[i];
                }
            }
        }
        for(int i = 0; i < yourChess.length; ++i){
            if(yourChess[i].getNowPos() + rollNum == Value.TERMINAL){
                return yourChess[i];
            }
        }
        for(int i = 0; i < yourChess.length; ++i){
            if(yourChess[i].isFlying() && Value.JUMP_POINT.contains(yourChess[i].getNowPos() + rollNum)){
                return yourChess[i];
            }
        }
        int maxIndex = -1, maxPos = -1;
        for(int i = 0; i < yourChess.length; ++i){
            if(yourChess[i].isFlying()){
                if(yourChess[i].getNowPos() >= maxPos && !Value.SAFE_POINT.contains(yourChess[i].getNowPos())){
                    maxPos = yourChess[i].getNowPos();
                    maxIndex = i;
                }
            }
        }
        if(maxIndex == -1){
            for(int i = 0; i < yourChess.length; ++i){
                if(yourChess[i].isFlying()){
                    if(yourChess[i].getNowPos() >= maxPos){
                        maxPos = yourChess[i].getNowPos();
                        maxIndex = i;
                    }
                }
            }
        }
        return yourChess[maxIndex];
    }

    private static boolean isConflict(Chess[][] chessList, int player, int targetPos){
        if(targetPos > Value.TERMINAL || Value.SAFE_POINT.contains(targetPos)){
            return false;
        }
        int targetX = Value.PATHS_X[player][targetPos];
        int targetY = Value.PATHS_Y[player][targetPos];
        for(int i = 0; i < chessList.length; ++i){
            if(player == i){
                continue;
            }
            for(int j = 0; j < chessList[i].length; ++j) {
                Chess targetChess = chessList[i][j];
                if (targetX == targetChess.getX() && targetY == targetChess.getY()){
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean isConflictFly(Chess[][] chessList, int player, int targetPos){
        if(targetPos > Value.TERMINAL || Value.SAFE_POINT.contains(targetPos)){
            return false;
        }
        if(targetPos != Value.FLY_POINT && targetPos != Value.FLY_POINT - 4){
            return false;
        }
        int targetPlayer = chessList[player][0].rival();
        int targetX = Value.PATHS_X[player][targetPos];
        int targetY = Value.PATHS_Y[player][targetPos];
        for(int i = 0; i < chessList[targetPlayer].length; ++i){
            Chess targetChess = chessList[targetPlayer][i];
            if (targetX == targetChess.getX() && targetY == targetChess.getY()){
                return true;
            }
        }
        return false;
    }
}
