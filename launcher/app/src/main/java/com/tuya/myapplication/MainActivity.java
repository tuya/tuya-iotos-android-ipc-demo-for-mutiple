package com.tuya.myapplication;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.btn_jump).setOnClickListener(v -> {
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

//        findViewById(R.id.btn_kill).setOnClickListener(v -> {
//
//        });
    }
}