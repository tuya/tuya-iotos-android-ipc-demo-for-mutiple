package com.tuya.myapplication;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
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
                //唤醒
                //先kill ipc进程
                Intent intentTwo = new Intent("ipc.action.boot");
                intentTwo.setComponent(new ComponentName("com.tuya.ai.ipcsdkdemo", "com.tuya.ai.ipcsdkdemo.BootBroadCastRevicer"));
                sendBroadcast(intentTwo);

                try {
                    HeartBean heartBean = heartBeanArrayList.get(index);
                    //再重启ipc
                    Intent intent = getPackageManager().getLaunchIntentForPackage("com.tuya.ai.ipcsdkdemo");
                    if (intent != null) {
                        intent.putExtra("uid", heartBean.getUid());
                        intent.putExtra("pid", heartBean.getPid());
                        intent.putExtra("key", heartBean.getAuthkey());
                        startActivity(intent);
                    }
                } catch (Exception e) {

                }
            }

            @Override
            public void closeResult(int index, int result) {
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
                        heartBeanArrayList.add(heartBean);
                        LowpowerManager.INSTANCE.addDeviceHeart(i, Integer.parseInt(heartBean.getIp()), Integer.parseInt(heartBean.getPort()), heartBean.getDeviceId(),
                                heartBean.getDeviceId().length(), heartBean.getKey(), heartBean.getKey().length());
                    }
                }
            }
        }

        LowpowerManager.INSTANCE.addDeviceHeart(1, 1, 44, "11", 2, "11", 2);

        findViewById(R.id.btn_jump).setOnClickListener(v -> {
            //先kill ipc进程
            Intent intentTwo = new Intent("ipc.action.boot");
            intentTwo.setComponent(new ComponentName("com.tuya.ai.ipcsdkdemo", "com.tuya.ai.ipcsdkdemo.BootBroadCastRevicer"));
            sendBroadcast(intentTwo);

            //再重启ipc
            Intent intent = getPackageManager().getLaunchIntentForPackage("com.tuya.ai.ipcsdkdemo");
            if (intent != null) {
//                iNetConfigManager.setPID("dktp1xuqw7h9rohr");
//                iNetConfigManager.setAuthorKey("7rMKwAlelL2m9HZ0uKjuxhnbjqA53arW");
//                iNetConfigManager.setUserId("tuya3626b051b53c0635");
                intent.putExtra("uid", "tuya3626b051b53c0635");
                intent.putExtra("pid", "dktp1xuqw7h9rohr");
                intent.putExtra("key", "7rMKwAlelL2m9HZ0uKjuxhnbjqA53arW");
                startActivity(intent);
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }
}