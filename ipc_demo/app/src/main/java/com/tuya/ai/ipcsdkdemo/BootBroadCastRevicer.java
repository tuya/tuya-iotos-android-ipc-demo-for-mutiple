package com.tuya.ai.ipcsdkdemo;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * created by xsj
 **/
public class BootBroadCastRevicer extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("ipc.action.boot")) {
            Intent mStartActivity = context.getPackageManager().getLaunchIntentForPackage(context.getPackageName());
            if (mStartActivity != null) {
                int mPendingIntentId = 123456;
                PendingIntent mPendingIntent = PendingIntent.getActivity(context, mPendingIntentId
                        , mStartActivity, PendingIntent.FLAG_CANCEL_CURRENT);
                AlarmManager mgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, mPendingIntent);
                Runtime.getRuntime().exit(0);
            }
        }
    }
}
