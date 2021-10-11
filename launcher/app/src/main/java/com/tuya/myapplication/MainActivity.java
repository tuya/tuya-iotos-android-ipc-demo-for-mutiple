package com.tuya.myapplication;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;

import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();

    private final int PERMISSION_CODE = 123;

    private String[] requiredPermissions = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (!EasyPermissions.hasPermissions(this, requiredPermissions)) {
            EasyPermissions.requestPermissions(this, "需要授予权限以使用设备", PERMISSION_CODE, requiredPermissions);
        }

        SharedPreferences sharedPreferences = getSharedPreferences("connection_info", Context.MODE_PRIVATE);
        String str = sharedPreferences.getString("str_value", "");
        Log.d("xsj", "str_value is " + str);
        if (!str.equals("")) {
            String[] strings = str.split("&&&&");
            if (strings.length > 0) {
                for (String deviceId : strings) {
                    if (!deviceId.equals("")) {
                        HeartManager.INSTANCE.addHeart(new Gson().fromJson(deviceId, HeartBean.class));
                    }
                }
            }
        }

        findViewById(R.id.btn_jump).setOnClickListener(v -> {
            Intent intent = getPackageManager().getLaunchIntentForPackage("com.tuya.ai.ipcsdkdemo");
            if (intent != null) {

//                iNetConfigManager.setPID("dktp1xuqw7h9rohr");
//                iNetConfigManager.setAuthorKey("7rMKwAlelL2m9HZ0uKjuxhnbjqA53arW");
//                iNetConfigManager.setUserId("tuya3626b051b53c0635");

//                pid = "dktp1xuqw7h9rohr";
//                uid = "tuya3626b051b53c0635";
//                authkey = "7rMKwAlelL2m9HZ0uKjuxhnbjqA53arW";


                intent.putExtra("uid", "tuya3626b051b53c0635");
                intent.putExtra("pid", "dktp1xuqw7h9rohr");
                intent.putExtra("key", "7rMKwAlelL2m9HZ0uKjuxhnbjqA53arW");
                startActivity(intent);
            }
        });

//        new Handler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                HeartManager.INSTANCE.wakeUpCallback(MainActivity.this);
//            }
//        }, 10000);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }
}