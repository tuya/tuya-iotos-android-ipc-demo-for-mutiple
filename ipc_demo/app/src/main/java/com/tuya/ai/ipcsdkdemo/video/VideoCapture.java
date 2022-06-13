package com.tuya.ai.ipcsdkdemo.video;

import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceHolder;

import com.tuya.smart.aiipc.ipc_sdk.api.Common;
import com.tuya.smart.aiipc.ipc_sdk.api.IMediaTransManager;
import com.tuya.smart.aiipc.ipc_sdk.api.IParamConfigManager;
import com.tuya.smart.aiipc.ipc_sdk.service.IPCServiceManager;
import com.tuya.smart.aiipc.netconfig.ConfigProvider;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class VideoCapture {

    private Camera mCamera;
    private byte[] pixelBuffer;
//    VideoCodec mCodec;
    H264Encoder h264Encoder;
    private SurfaceTexture mSurfaceTexture;

    private int mChannel;

    public VideoCapture(int channel) {

        pixelBuffer = new byte[1280 * 720 * 3 / 2];
//        mCodec = new VideoCodec(mChannel = channel);
        h264Encoder = new H264Encoder(1280, 720, 25, 1000*1000);
    }
    public void startVideoCapture() {
        startPreview();
    }

    private void startPreview() {
//        openCamera();
        mCamera = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK);

        Camera.Parameters p = mCamera.getParameters();
        //根据自己的设置更改
        p.setPreviewFormat(ImageFormat.YV12);
        Log.d("xsj","p.getPreviewFormat() is " + p.getPreviewFormat());
//        p.setPreviewFormat(ImageFormat.YV12);
        p.setPreviewFpsRange(25000, 25000);
        p.setPreviewSize(1280, 720);

        Log.d("Preview", "ccc startPreview1111 ");

        try {
            if (ConfigProvider.getConfig(ConfigProvider.QR_OUTPUT) instanceof SurfaceHolder) {
                mCamera.setPreviewDisplay((SurfaceHolder) ConfigProvider.getConfig(ConfigProvider.QR_OUTPUT));
                ConfigProvider.setConfig(ConfigProvider.QR_OUTPUT, null);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        IParamConfigManager configManager = IPCServiceManager.getInstance().getService(IPCServiceManager.IPCService.MEDIA_PARAM_SERVICE);
        int configRate = configManager.getInt(mChannel, Common.ParamKey.KEY_VIDEO_FRAME_RATE);

        List<int[]> supported = p.getSupportedPreviewFpsRange();
        boolean isFound = false;
        for (int[] fps : supported){
            if (fps.length == 2 && fps[0] == fps[1] && fps[0] == configRate * 1000) {
                isFound = true;
                break;
            }
        }

        if (isFound){
            p.setPreviewFpsRange(configRate * 1000, configRate * 1000);
        }else{
            p.setPreviewFpsRange(supported.get(supported.size()-1)[0], supported.get(supported.size()-1)[1]);
        }

        mCamera.setParameters(p);
        mSurfaceTexture = new SurfaceTexture(0);

        mCamera.addCallbackBuffer(pixelBuffer);

        mCamera.setPreviewCallbackWithBuffer(new Camera.PreviewCallback() {
            @Override
            public void onPreviewFrame(byte[] data, Camera camera) {
                if(count < 100){
                    try {
                        outputStream.write(data);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    count ++;
                }

                //编码
                byte[] pixelData = new byte[1280 * 720 * 3 / 2];
                System.arraycopy(data, 0, pixelData, 0, data.length);
//                mCodec.encodeH264(pixelData);
                if(h264Encoder != null){
                    byte[] encodeData = new byte[1280 * 720 * 3 / 2];
                    int ret  =  h264Encoder.Encode(pixelData, encodeData);
//                    transManager.pushMediaStream()
//                    try {
//                        outputStream.write(encodeData, 0, ret);
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
                }

                camera.addCallbackBuffer(pixelBuffer);
            }
        });
        mCamera.startPreview();
        Log.d("Preview", "ccc startPreview2222 ");

    }

    int count = 0;

    FileOutputStream outputStream;

    {
        try {
            outputStream = new FileOutputStream("/sdcard/h264.data");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}
