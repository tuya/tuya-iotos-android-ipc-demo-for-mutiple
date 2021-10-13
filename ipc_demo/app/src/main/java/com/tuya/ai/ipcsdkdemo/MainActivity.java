package com.tuya.ai.ipcsdkdemo;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.SurfaceView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import com.tuya.ai.ipcsdkdemo.audio.FileAudioCapture;
import com.tuya.ai.ipcsdkdemo.video.VideoCapture;
import com.tuya.smart.aiipc.base.permission.PermissionUtil;
import com.tuya.smart.aiipc.ipc_sdk.IPCSDK;
import com.tuya.smart.aiipc.ipc_sdk.api.Common;
import com.tuya.smart.aiipc.ipc_sdk.api.IDeviceManager;
import com.tuya.smart.aiipc.ipc_sdk.api.IFeatureManager;
import com.tuya.smart.aiipc.ipc_sdk.api.IMediaTransManager;
import com.tuya.smart.aiipc.ipc_sdk.api.IMqttProcessManager;
import com.tuya.smart.aiipc.ipc_sdk.api.INetConfigManager;
import com.tuya.smart.aiipc.ipc_sdk.api.IParamConfigManager;
import com.tuya.smart.aiipc.ipc_sdk.callback.IMqttStatusCallback;
import com.tuya.smart.aiipc.ipc_sdk.callback.NetConfigCallback;
import com.tuya.smart.aiipc.ipc_sdk.service.IPCServiceManager;
import com.tuya.smart.aiipc.netconfig.ConfigProvider;
import com.tuya.smart.aiipc.netconfig.mqtt.TuyaNetConfig;
import com.tuya.smart.aiipc.trans.ServeInfo;
import com.tuya.smart.aiipc.trans.TransJNIInterface;

import java.util.TimeZone;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "IPC_DEMO";

    SurfaceView surfaceView;

    VideoCapture videoCapture;

    FileAudioCapture fileAudioCapture;

    private Handler mHandler;

    String pid = "";
    String uid = "";
    String authkey = "";

    private boolean isFirst = true;
    private LocalDataBean localDataBean;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        pid = getIntent().getStringExtra("pid");
        uid = getIntent().getStringExtra("uid");
        authkey = getIntent().getStringExtra("key");

        Log.d(TAG, "pid is " + pid + " uid is " + uid + " key is " + authkey);

        surfaceView = findViewById(R.id.surface);
        mHandler = new Handler();

        findViewById(R.id.reset).setOnClickListener(v -> IPCServiceManager.getInstance().reset());

        findViewById(R.id.start_record).setOnClickListener(v -> TransJNIInterface.getInstance().startLocalStorage());

        findViewById(R.id.stop_record).setOnClickListener(v -> TransJNIInterface.getInstance().stopLocalStorage());

        findViewById(R.id.call).setOnClickListener(v -> {

            IDeviceManager iDeviceManager = IPCServiceManager.getInstance().getService(IPCServiceManager.IPCService.DEVICE_SERVICE);
            // check register status
            int regStat = iDeviceManager.getRegisterStatus();
            Log.d(TAG, "ccc getting qrcode, register status: " + regStat);
            if (regStat != 2) {
                // get short url for qrcode
                String code = iDeviceManager.getQrCode(null);
                Log.d(TAG, "ccc qrcode: " + code);
            }
        });

        PermissionUtil.check(this, new String[]{
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WAKE_LOCK,
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.CAMERA
        }, this::initSDK);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        IPCSDK.closeWriteLog();
    }

    private void initSDK() {
        if (uid == null || pid == null || authkey == null) {
            return;
        }

        IPCSDK.initSDK(this);
//        IPCSDK.openWriteLog(this, "/sdcard/tuya_log/ipc", 3);
        LoadParamConfig();

        INetConfigManager iNetConfigManager = IPCServiceManager.getInstance().getService(IPCServiceManager.IPCService.NET_CONFIG_SERVICE);

        iNetConfigManager.config("QR_OUTPUT", surfaceView.getHolder());

        iNetConfigManager.setPID(pid);
        iNetConfigManager.setUserId(uid);
        iNetConfigManager.setAuthorKey(authkey);

        TuyaNetConfig.setDebug(true);

        // Note: network must be ok before enable mqtt active
        ConfigProvider.enableMQTT(true);

        IPCServiceManager.getInstance().setResetHandler(isHardward -> {

            if (mHandler != null) {
                //通知对端从心跳列表中去除
                if (localDataBean != null) {
                    Intent intent = new Intent("ipc.serverInfo.reset");
                    intent.putExtra("strvalue", new Gson().toJson(localDataBean));
                    intent.setComponent(new ComponentName("com.tuya.myapplication", "com.tuya.myapplication.ResetBroadCast"));
                    sendBroadcast(intent);
                    Log.d("xsj", "ResetBroadCast is send");
                }

                mHandler.postDelayed(() -> {
                    //restart
                    Intent mStartActivity = getPackageManager().getLaunchIntentForPackage(getPackageName());
                    if (mStartActivity != null) {
                        int mPendingIntentId = 123456;
                        PendingIntent mPendingIntent = PendingIntent.getActivity(this, mPendingIntentId
                                , mStartActivity, PendingIntent.FLAG_CANCEL_CURRENT);
                        AlarmManager mgr = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                        mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, mPendingIntent);
                        Runtime.getRuntime().exit(0);
                    }

                }, 1500);
            }
        });

        NetConfigCallback netConfigCallback = new NetConfigCallback() {

            @Override
            public void configOver(boolean first, String token) {
                Log.d(TAG, "configOver: token: " + token);
                IMediaTransManager transManager = IPCServiceManager.getInstance().getService(IPCServiceManager.IPCService.MEDIA_TRANS_SERVICE);
                IMqttProcessManager mqttProcessManager = IPCServiceManager.getInstance().getService(IPCServiceManager.IPCService.MQTT_SERVICE);
                IMediaTransManager mediaTransManager = IPCServiceManager.getInstance().getService(IPCServiceManager.IPCService.MEDIA_TRANS_SERVICE);
                IFeatureManager featureManager = IPCServiceManager.getInstance().getService(IPCServiceManager.IPCService.FEATURE_SERVICE);

                mqttProcessManager.setMqttStatusChangedCallback(new IMqttStatusCallback() {
                    @Override
                    public void onMqttStatus(int i) {
                        Log.d("xsj", "strvalue STATUS_CLOUD_CONN is " + i);
                        if (i == Common.MqttConnectStatus.STATUS_CLOUD_CONN) {
                            if (isFirst) {
                                isFirst = false;
                                //获取心跳信息，保存
                                ServeInfo serveInfo = transManager.getIpcLowPowerServer();
                                localDataBean = new LocalDataBean();
                                localDataBean.ip = serveInfo.ip;
                                localDataBean.port = serveInfo.port;
                                localDataBean.deviceId = transManager.getIpcDeviceId();
                                localDataBean.key = transManager.getIpcLocalKey();
                                localDataBean.pid = pid;
                                localDataBean.authkey = authkey;
                                localDataBean.uid = uid;
                                try {
                                    Intent intent = new Intent("ipc.serverInfo.flush");
                                    intent.putExtra("strvalue", new Gson().toJson(localDataBean));
                                    intent.setComponent(new ComponentName("com.tuya.myapplication", "com.tuya.myapplication.DeviceListBroadCast"));
                                    sendBroadcast(intent);
                                    Log.d("xsj", "strvalue sendbroadcast");
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }

                                //  start push media
                                transManager.startMultiMediaTrans(5);

                                // video stream from camera
                                videoCapture = new VideoCapture(Common.ChannelIndex.E_CHANNEL_VIDEO_MAIN);
                                videoCapture.startVideoCapture();

                                // audio stream from local file
                                fileAudioCapture = new FileAudioCapture(MainActivity.this);
                                fileAudioCapture.startFileCapture();
                            }
                        }
                    }
                });

                IDeviceManager iDeviceManager = IPCServiceManager.getInstance().getService(IPCServiceManager.IPCService.DEVICE_SERVICE);
                // set region
                iDeviceManager.setRegion(IDeviceManager.IPCRegion.REGION_CN);

                transManager.initTransSDK(token, "/sdcard/tuya_ipc/", "/sdcard/tuya_ipc/", pid, uid, authkey);

                featureManager.initDoorBellFeatureEnv();

                runOnUiThread(() -> findViewById(R.id.call).setEnabled(true));

                mediaTransManager.setDoorBellCallStatusCallback(status -> {

                    Log.d(TAG, "doorbell back: " + status);

                });
                syncTimeZone();
            }

            @Override
            public void startConfig() {
                Log.d(TAG, "startConfig: ");
            }

            @Override
            public void recConfigInfo() {
                Log.d(TAG, "recConfigInfo: ");
            }

            @Override
            public void onNetConnectFailed(int i, String s) {

            }

            @Override
            public void onNetPrepareFailed(int i, String s) {

            }
        };

        iNetConfigManager.configNetInfo(netConfigCallback);

    }

    private void LoadParamConfig() {
        IParamConfigManager configManager = IPCServiceManager.getInstance().getService(IPCServiceManager.IPCService.MEDIA_PARAM_SERVICE);

        configManager.setInt(Common.ChannelIndex.E_CHANNEL_VIDEO_MAIN, Common.ParamKey.KEY_VIDEO_WIDTH, 1280);
        configManager.setInt(Common.ChannelIndex.E_CHANNEL_VIDEO_MAIN, Common.ParamKey.KEY_VIDEO_HEIGHT, 720);
        configManager.setInt(Common.ChannelIndex.E_CHANNEL_VIDEO_MAIN, Common.ParamKey.KEY_VIDEO_FRAME_RATE, 24);
        configManager.setInt(Common.ChannelIndex.E_CHANNEL_VIDEO_MAIN, Common.ParamKey.KEY_VIDEO_I_FRAME_INTERVAL, 2);
        configManager.setInt(Common.ChannelIndex.E_CHANNEL_VIDEO_MAIN, Common.ParamKey.KEY_VIDEO_BIT_RATE, 1024000);

        configManager.setInt(Common.ChannelIndex.E_CHANNEL_AUDIO, Common.ParamKey.KEY_AUDIO_CHANNEL_NUM, 1);
        configManager.setInt(Common.ChannelIndex.E_CHANNEL_AUDIO, Common.ParamKey.KEY_AUDIO_SAMPLE_RATE, 8000);
        configManager.setInt(Common.ChannelIndex.E_CHANNEL_AUDIO, Common.ParamKey.KEY_AUDIO_SAMPLE_BIT, 16);
        configManager.setInt(Common.ChannelIndex.E_CHANNEL_AUDIO, Common.ParamKey.KEY_AUDIO_FRAME_RATE, 25);
    }

    private static void syncTimeZone() {
        int rawOffset = TransJNIInterface.getInstance().getAppTimezoneBySecond();
        String[] availableIDs = TimeZone.getAvailableIDs(rawOffset * 1000);
        if (availableIDs.length > 0) {
            android.util.Log.d(TAG, "syncTimeZone: " + rawOffset + " , " + availableIDs[0] + " ,  ");
        }
    }
}
