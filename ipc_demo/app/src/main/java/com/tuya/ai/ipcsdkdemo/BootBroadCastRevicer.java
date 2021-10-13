package com.tuya.ai.ipcsdkdemo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * created by xsj
 **/
public class BootBroadCastRevicer extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("ipc.action.boot")) {
            Runtime.getRuntime().exit(0);
        }
    }
}
