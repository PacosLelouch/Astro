package com.potatofriedbread.astro;

import java.util.ArrayList;
import java.util.Arrays;

public class Value {
    static final int RED = 0;
    static final int YELLOW = 1;
    static final int BLUE = 2;
    static final int GREEN = 3;

    static final int AI = 0;
    static final int ONLINE_HUMAN = 1;
    static final int LOCAL_HUMAN = 2;
    static final int REPLAY = 3;

    static final int ONLINE = 0;
    static final int LOCAL = 1;

    static final int AI_TYPE[] = new int[]{0, 1, 2, 3};

    static final int TERMINAL = 56;
    static final int CONFLICT = 53;
    static final int[] JUMP_POINT = new int[]{2, 6, 10, 14, /*18,*/ 22, 26, 30, 34, 38, 42, 46, 50};
    static final int FLY_POINT = 18;
    static final ArrayList<Integer> TAKE_OFF_NUM = (ArrayList)Arrays.asList(6);
    static final ArrayList<Integer> COMBO_NUM = (ArrayList)Arrays.asList(6);

    static final String[] PLAYER_COLOR = new String[]{"Red", "Yellow", "Blue", "Green"};

    static final int[] RED_PATH_X = new int[]{
            25,
            23, 24, 24, 23, 25, 27, 29,
            31, 32, 32, 32, 32, 32, 31,
            29, 27, 25, 23, 24, 24, 23,
            21, 19, 17, 15, 13,
            11, 10, 10, 11,  9,  7,  5,
             3,  2,  2,  2,  2,  2,  3,
             5,  7,  9, 11, 10, 10, 11,
            13, 15,
            17, 17, 17, 17, 17, 17, 17
    };
    static final int[] YELLOW_PATH_X = new int[]{
            33,
            31, 29, 27, 25, 23, 24, 24,
            23, 21, 19, 17, 15, 13, 11,
            10, 10, 11,  9,  7,  5,  3,
             2,  2,  2,  2,  2,
             3,  5,  7,  9, 11, 10, 10,
            11, 13, 15, 17, 19, 21, 23,
            24, 24, 23, 25, 27, 29, 31,
            32, 32,
            32, 29, 27, 25, 23, 21, 19
    };
    static final int[] BLUE_PATH_X = new int[]{
             9,
            11, 10, 10, 11,  9,  7,  5,
             3,  2,  2,  2,  2,  2,  3,
             5,  7,  9, 11, 10, 10, 11,
            13, 15, 17, 19, 21,
            23, 24, 24, 23, 25, 27, 29,
            31, 32, 32, 32, 32, 32, 31,
            29, 27, 25, 23, 24, 24, 23,
            21, 19,
            17, 17, 17, 17, 17, 17, 17
    };
    static final int[] GREEN_PATH_X = new int[]{
             1,
             3,  5,  7,  9, 11, 10, 10,
            11, 13, 15, 17, 19, 21, 23,
            24, 24, 23, 25, 27, 29, 31,
            32, 32, 32, 32, 32,
            31, 29, 27, 25, 23, 24, 24,
            23, 21, 19, 17, 15, 13, 11,
            10, 10, 11,  9,  7,  5,  3,
             2,  2,
             2,  5,  7,  9, 11, 13, 15
    };

    static final int[] RED_PATH_Y = new int[]{
             1,
             3,  5,  7,  9, 11, 10, 10,
            11, 13, 15, 17, 19, 21, 23,
            24, 24, 23, 25, 27, 29, 31,
            32, 32, 32, 32, 32,
            31, 29, 27, 25, 23, 24, 24,
            23, 21, 19, 17, 15, 13, 11,
            10, 10, 11,  9,  7,  5,  3,
             2,  2,
             2,  5,  7,  9, 11, 13, 15
    };
    static final int[] YELLOW_PATH_Y = new int[]{
            25,
            23, 24, 24, 23, 25, 27, 29,
            31, 32, 32, 32, 32, 32, 31,
            29, 27, 25, 23, 24, 24, 23,
            21, 19, 17, 15, 13,
            11, 10, 10, 11,  9,  7,  5,
             3,  2,  2,  2,  2,  2,  3,
             5,  7,  9, 11, 10, 10, 11,
            13, 15,
            17, 17, 17, 17, 17, 17, 17
    };
    static final int[] BLUE_PATH_Y = new int[]{
            33,
            31, 29, 27, 25, 23, 24, 24,
            23, 21, 19, 17, 15, 13, 11,
            10, 10, 11,  9,  7,  5,  3,
             2,  2,  2,  2,  2,
             3,  5,  7,  9, 11, 10, 10,
            11, 13, 15, 17, 19, 21, 23,
            24, 24, 23, 25, 27, 29, 31,
            32, 32,
            32, 29, 27, 25, 23, 21, 19
    };
    static final int[] GREEN_PATH_Y = new int[]{
             9,
            11, 10, 10, 11,  9,  7,  5,
             3,  2,  2,  2,  2,  2,  3,
             5,  7,  9, 11, 10, 10, 11,
            13, 15, 17, 19, 21,
            23, 24, 24, 23, 25, 27, 29,
            31, 32, 32, 32, 32, 32, 31,
            29, 27, 25, 23, 24, 24, 23,
            21, 19,
            17, 17, 17, 17, 17, 17, 17
    };

    static final int[][] PATHS_X = new int[][]{
            RED_PATH_X,
            YELLOW_PATH_X,
            BLUE_PATH_X,
            GREEN_PATH_X
    };

    static final int[][] PATHS_Y = new int[][]{
            RED_PATH_Y,
            YELLOW_PATH_Y,
            BLUE_PATH_Y,
            GREEN_PATH_Y
    };
}
