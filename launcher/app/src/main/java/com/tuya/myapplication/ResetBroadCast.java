package com.tuya.myapplication;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

/**
 * created by xsj
 **/
public class ResetBroadCast extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action.equals("ipc.serverInfo.reset")) {
            String str = intent.getStringExtra("strvalue");
            Log.d("xsj", "ResetBroadCast action is ipc.serverInfo.flush string is " + str);
            SharedPreferences sharedPreferences = context.getSharedPreferences("connection_info", Context.MODE_PRIVATE);
            String valueStr = sharedPreferences.getString("str_value", "");
            Log.d("xsj", "ResetBroadCast str_value is " + valueStr);
            try {
                if (!valueStr.equals("")) {
                    String[] strings = str.split("&&&&");
                    String[] newData = new String[strings.length - 1];
                    if (strings.length > 0) {
                        int count = 0;
                        for (String i : strings) {
                            if (!i.equals(str) && !i.equals("")) {
                                newData[count] = i;
                                count++;
                                Log.d("xsj", "new data ResetBroadCast str_value is " + valueStr);
                            }
                        }
                        if (newData.length == 0) {
                            sharedPreferences.edit().putString("str_value", "").commit();
                        } else {
                            for (String i : newData) {
                                String currentvalueStr = sharedPreferences.getString("str_value", "");
                                sharedPreferences.edit().putString("str_value", currentvalueStr + "&&&&" + i).commit();
                            }
                        }
                    }
                }
            } catch (Exception e) {

            }
        }
        //杀掉当前进程，心跳连接重新初始化，可以根据业务需要，也可以不杀当前进程
        Runtime.getRuntime().exit(0);
    }
}

