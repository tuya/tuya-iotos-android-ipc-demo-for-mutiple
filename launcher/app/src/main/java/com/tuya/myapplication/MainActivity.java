package com.tuya.myapplication;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresPermission;
import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import com.tuya.lowpower.LowpowerManager;

import java.util.ArrayList;

import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();

    private final int PERMISSION_CODE = 123;

    private String[] requiredPermissions = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE};

    private ArrayList<HeartBean> heartBeanArrayList = new ArrayList<>();

    int localIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (!EasyPermissions.hasPermissions(this, requiredPermissions)) {
            EasyPermissions.requestPermissions(this, "需要授予权限以使用设备", PERMISSION_CODE, requiredPermissions);
        }

        LowpowerManager.INSTANCE.aliveInit(new LowpowerManager.LowpowerListener() {
            @Override
            public void wakeUp(int index) {
                HeartBean heartBean = heartBeanArrayList.get(index);
                Log.d(TAG, "wakeUp: " + "uid" + heartBean.getUid());
                Log.d(TAG, "wakeUp: " + "pid" + heartBean.getPid());
                Log.d(TAG, "wakeUp: " + "key" + heartBean.getAuthkey());
                //唤醒
                //先kill ipc进程
//                Intent intentTwo = new Intent("ipc.action.boot");
//                intentTwo.setComponent(new ComponentName("com.tuya.ai.ipcsdkdemo", "com.tuya.ai.ipcsdkdemo.BootBroadCastRevicer"));
//                sendBroadcast(intentTwo);

                new Handler().postDelayed(() -> {
                    try {
                        //再重启ipc
                        Intent intent = getPackageManager().getLaunchIntentForPackage("com.tuya.ai.ipcsdkdemo");
                        if (intent != null) {
                            intent.putExtra("uid", heartBean.getUid());
                            intent.putExtra("pid", heartBean.getPid());
                            intent.putExtra("key", heartBean.getAuthkey());
                            startActivity(intent);
                        }
                    } catch (Exception ignored) {

                    }
                }, 500);
            }

            @Override
            public void closeResult(int index, int result) {
                Log.d(TAG, "closeResult: " + index + " |||| " + result);
                //0表示正常关闭，比如主进程拉起，包活链路被主动踢掉的情况。非零，表示异常。此时用户可以重新add 包活
                if (result != 0) {
                    HeartBean heartBean = heartBeanArrayList.get(index);
                    LowpowerManager.INSTANCE.addDeviceHeart(index, Integer.parseInt(heartBean.getIp()), Integer.parseInt(heartBean.getPort()), heartBean.getDeviceId(),
                            heartBean.getDeviceId().length(), heartBean.getKey(), heartBean.getKey().length());
                }
            }
        });

        //读取建立心跳连接
        SharedPreferences sharedPreferences = getSharedPreferences("connection_info", Context.MODE_PRIVATE);
        String str = sharedPreferences.getString("str_value", "");
        Log.d("xsj", "str_value is " + str);
        if (!str.equals("")) {
            String[] strings = str.split("&&&&");
            if (strings.length > 0) {
                for (int i = 0; i < strings.length; i++) {
                    if (!strings[i].equals("")) {
                        HeartBean heartBean = new Gson().fromJson(strings[i], HeartBean.class);
                        Log.d("xsj","device id is " + heartBean.getDeviceId());
                        heartBeanArrayList.add(heartBean);
                        LowpowerManager.INSTANCE.addDeviceHeart(
                                localIndex,
                                Integer.parseInt(heartBean.getIp()),
                                Integer.parseInt(heartBean.getPort()),
                                heartBean.getDeviceId(),
                                heartBean.getDeviceId().length(),
                                heartBean.getKey(),
                                heartBean.getKey().length()
                        );
                        localIndex ++;
                    }
                }
            }
        }

        findViewById(R.id.btn_jump).setOnClickListener(v -> {
            Log.d(TAG, "onCreate: btn_jump");
            //先kill ipc进程
            Intent intentTwo = new Intent("ipc.action.boot");
            intentTwo.setComponent(new ComponentName("com.tuya.ai.ipcsdkdemo", "com.tuya.ai.ipcsdkdemo.BootBroadCastRevicer"));
            sendBroadcast(intentTwo);

            new Handler().postDelayed(() -> {
                //重启ipc
                Intent intent = getPackageManager().getLaunchIntentForPackage("com.tuya.ai.ipcsdkdemo");
                if (intent != null) {
                    intent.putExtra("pid", "g5xhwnzlmy64wlby");
                    intent.putExtra("uid", "tuya83c7ea992d0a8313");
                    intent.putExtra("key", "rnMToHjvU2m75VjByLU5MxM7gfbZRPHp");
                    intent.putExtra("index", 0);
                    startActivity(intent);
                }
            }, 500);
        });

        findViewById(R.id.btn_jump2).setOnClickListener(v -> {
            Log.d(TAG, "onCreate: btn_jump2");
            //先kill ipc进程
            Intent intentTwo = new Intent("ipc.action.boot");
            intentTwo.setComponent(new ComponentName("com.tuya.ai.ipcsdkdemo", "com.tuya.ai.ipcsdkdemo.BootBroadCastRevicer"));
            sendBroadcast(intentTwo);

            new Handler().postDelayed(() -> {
                //重启ipc
                Intent intent = getPackageManager().getLaunchIntentForPackage("com.tuya.ai.ipcsdkdemo");
                if (intent != null) {
                    intent.putExtra("pid", "g5xhwnzlmy64wlby");
                    intent.putExtra("uid", "tuyaef9afe9a692ed792");
                    intent.putExtra("key", "92TQS5kcOEGxDHZgCv3GYEoyKjGA6M9T");
                    intent.putExtra("index", 1);
                    startActivity(intent);
                }
            }, 500);
        });


        com.tuya.myapplication.ReAddBroadCast.setReaddListener(new com.tuya.myapplication.ReAddBroadCast.ReaddListener() {
            @Override
            public void reAddHerat(int index) {
                Log.d("xsj", "reAddHerat index is " + index);
//                HeartBean heartBean = heartBeanArrayList.get(index);
//                LowpowerManager.INSTANCE.addDeviceHeart(index, Integer.parseInt(heartBean.getIp()), Integer.parseInt(heartBean.getPort()), heartBean.getDeviceId(),
//                        heartBean.getDeviceId().length(), heartBean.getKey(), heartBean.getKey().length());

                //读取建立心跳连接
                SharedPreferences sharedPreferences = getSharedPreferences("connection_info", Context.MODE_PRIVATE);
                String str = sharedPreferences.getString("str_value", "");
                Log.d("xsj", "str_value is " + str);
                if (!str.equals("")) {
                    String[] strings = str.split("&&&&");
                    if (strings.length > 0) {
                        for (int i = 0; i < strings.length; i++) {
                            if (!strings[i].equals("")) {
                                HeartBean heartBean = new Gson().fromJson(strings[i], HeartBean.class);
                                Log.d("xsj","device id is " + heartBean.getDeviceId());
                                heartBeanArrayList.add(heartBean);
                                LowpowerManager.INSTANCE.addDeviceHeart(
                                        localIndex,
                                        Integer.parseInt(heartBean.getIp()),
                                        Integer.parseInt(heartBean.getPort()),
                                        heartBean.getDeviceId(),
                                        heartBean.getDeviceId().length(),
                                        heartBean.getKey(),
                                        heartBean.getKey().length()
                                );
                                localIndex ++;
                            }
                        }
                    }
                }

            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    class ReAddBroadCast extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

        }
    }
}