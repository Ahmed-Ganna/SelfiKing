package com.selfi.util;

import android.util.Log;

/**
 * Created by Ahmed on 01/07/2016.
 */
public class Logger {

    public static final String NOT_EXIST_ERROR = "snapshot not exist";

    public static void forDebug(String msg){
        Log.d("Logger : ",msg );
    }

    public static void forError(String msg){
        Log.e("Logger : ",msg );
    }


    public static void forInfo(String msg) {
        Log.i("Logger : ",msg );
    }
}
