package com.tuya.myapplication;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

/**
 * created by xsj
 **/
public class DeviceListBroadCast extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action.equals("ipc.serverInfo.flush")) {
            String str = intent.getStringExtra("strvalue");
            Log.d("xsj", "DeviceListBroadCast action is ipc.serverInfo.flush string is " + str);
            SharedPreferences sharedPreferences = context.getSharedPreferences("connection_info", Context.MODE_PRIVATE);
            String valueStr = sharedPreferences.getString("str_value", "");

            if (!valueStr.equals("")) {
                boolean isInclude = false;
                String[] strings = valueStr.split("&&&&");
                if (strings.length > 0) {
                    for (String i : strings) {
                        if (i.equals(str) && !i.equals("")) {
                            isInclude = true;
                            break;
                        }
                    }
                    if (!isInclude) {
                        //不包含处理心跳
                        sharedPreferences.edit().putString("str_value", valueStr + "&&&&" + str).commit();
                    }
                }
            } else {
                sharedPreferences.edit().putString("str_value", valueStr + "&&&&" + str).commit();
            }
            //杀掉当前进程，心跳连接重新初始化，可以根据业务需要，也可以不杀当前进程
//            Runtime.getRuntime().exit(0);
        }
    }
}
