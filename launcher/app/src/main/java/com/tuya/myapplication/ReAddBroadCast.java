package com.tuya.myapplication;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * created by xsj
 **/
public class ReAddBroadCast extends BroadcastReceiver {

    interface ReaddListener{
        void reAddHerat(int index);
    }

    public static ReaddListener mReaddListener;


    public static void setReaddListener(ReaddListener readdListener){
        mReaddListener = readdListener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action.equals("ipc.serverInfo.readd")) {
           int index = intent.getIntExtra("index", 0);
            Log.d("xsj","readd index is " + index);
           if(mReaddListener != null){
               mReaddListener.reAddHerat(index);
           }
        }
    }
}
