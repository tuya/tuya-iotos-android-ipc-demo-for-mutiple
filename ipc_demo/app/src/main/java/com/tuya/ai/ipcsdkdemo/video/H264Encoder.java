package com.tuya.ai.ipcsdkdemo.video;

import android.annotation.SuppressLint;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaCodecInfo.CodecCapabilities;
import android.media.MediaCodecList;
import android.media.MediaFormat;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.tuya.smart.aiipc.ipc_sdk.api.Common;
import com.tuya.smart.aiipc.ipc_sdk.api.IMediaTransManager;
import com.tuya.smart.aiipc.ipc_sdk.service.IPCServiceManager;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;

public class H264Encoder {
    public final String TAG = H264Encoder.class.getSimpleName();

    public final static int MTK_YUV_COLORFORMAT = 0x7f000200;

    private MediaCodec mediaCodec;
    private MediaFormat mediaFormat;
    private int m_framerate;
    private int m_frame_count;
    private byte[] m_info = null;
    private int m_colorFormat;
    private byte[] m_uvTmp;
    private boolean m_isNeedSwap = true; // shi
    private int mBitrate;
    //    private boolean isTs1500 = false;
    private int mWidth, mHeight;
    IMediaTransManager transManager;

    @SuppressLint("NewApi")
    public H264Encoder(int width, int height, int framerate, int bitrate) {
        transManager = IPCServiceManager.getInstance().getService(IPCServiceManager.IPCService.MEDIA_TRANS_SERVICE);
        mHeight = height;
        mWidth = width;
        m_framerate = framerate;
        m_frame_count = 0;
        m_uvTmp = new byte[width * height / 2];
        mBitrate = bitrate;
        // 创建编码器
        try {
            Log.e(TAG, "width is " + width + "    height is " + height + "    framerate is " + framerate + "    bitrate is " + bitrate);
            mediaCodec = MediaCodec.createEncoderByType("video/avc");
            mediaFormat = MediaFormat.createVideoFormat("video/avc", width, height); // 宽和高
            getPreferColorFormat();

            mediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, m_colorFormat); // YUV颜色格式
            mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, bitrate); // 比特率
            mediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, framerate); // 帧率
            mediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 2); // I帧间隔

            mediaCodec.configure(mediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE); // 设置媒体格式
            mediaCodec.start();
        } catch (IOException e) {
            e.printStackTrace();
        } // 开启编码器
        m_isNeedSwap = !(("MediaPad 10 Link+").equals(Build.MODEL));
    }

    public void resetH264Encoder() {
        if (mediaCodec == null) {
            return;
        }
        close();
        try {
            m_frame_count = 0;
            mediaCodec = MediaCodec.createEncoderByType("video/avc");
            mediaCodec.configure(mediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
            mediaCodec.start();
        } catch (Exception e) {
            // TODO: handle exception
            Log.e(TAG, "error is " + e.getMessage());
        }
    }

    public void close() {
        try {
            mediaCodec.stop();
            mediaCodec.release();
            mediaCodec = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int getSurportColorFormat() {
        return m_colorFormat;
    }

    /**
     * 请求I帧
     */
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void requestSyncFrame() {
        if (mediaCodec != null) {
            Bundle params = new Bundle();
            params.putInt(MediaCodec.PARAMETER_KEY_REQUEST_SYNC_FRAME, 0);
            mediaCodec.setParameters(params);
        }
    }

    public int Encode(byte[] input, byte[] output) {
        byte[] i420 = new byte[(int) (mWidth * mHeight * 1.5)];
        YV12toNV21(input, i420, mWidth, mHeight);
        int pos = 0;
        try {
            ByteBuffer[] inputBuffers = mediaCodec.getInputBuffers();
            ByteBuffer[] outputBuffers = mediaCodec.getOutputBuffers();
            int inputBufferIndex = mediaCodec.dequeueInputBuffer(0); // 获取输入缓冲去所有权

            if (inputBufferIndex >= 0) {
                ByteBuffer inputBuffer = inputBuffers[inputBufferIndex];
                inputBuffer.clear();
                inputBuffer.put(i420);
                long presentationTimeUs = 1000000L * m_frame_count / m_framerate;
                m_frame_count++;
                mediaCodec.queueInputBuffer(inputBufferIndex, 0, i420.length, presentationTimeUs, 0); // 释放输入缓冲去所有权
            } else {
                Log.d("media", "resetH264Encoder");
                resetH264Encoder();
            }

            MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
            int outputBufferIndex = mediaCodec.dequeueOutputBuffer(bufferInfo, 0);
            if (outputBufferIndex >= 0) {
                ByteBuffer outputBuffer = outputBuffers[outputBufferIndex];
                byte[] outData = new byte[bufferInfo.size];
                outputBuffer.get(outData);

                if ((outData[0] == 0x0) && (outData[1] == 0x0) && (outData[2] == 0x0) && (outData[3] == 0x1)) {
                    // sps and pps
                    if (((outData[4] & 0x1f) == 0x07) || ((outData[4] & 0x1f) == 0x08)) {
                        // Log.d("H264Encoder sps or pps");
                        if (m_info == null) {
                            m_info = new byte[outData.length];
                            System.arraycopy(outData, 0, m_info, 0, outData.length);
//                            transManager.pushMediaStream(0, Common.NAL_TYPE.NAL_TYPE_IDR, outData);
                        }
                    }
                    // I frame
                    else if ((outData[4] & 0x1f) == 0x05) {
                        // Log.d("H264Encoder I Frame");
                        System.arraycopy(m_info, 0, output, 0, m_info.length);
                        pos += m_info.length;
                        System.arraycopy(outData, 0, output, m_info.length, outData.length);
                        pos += outData.length;
                        byte[] dstData = new byte[pos];
                        System.arraycopy(output, 0, dstData, 0, pos);
                        transManager.pushMediaStream(0, Common.NAL_TYPE.NAL_TYPE_IDR, dstData);
                    }
                    // P/B frame
                    else {
                        // Log.d("H264Encoder P/B Frame");
                        System.arraycopy(outData, 0, output, 0, outData.length);
                        pos += outData.length;
                        byte[] dstData = new byte[pos];
                        System.arraycopy(output, 0, dstData, 0, pos);
                        transManager.pushMediaStream(0, Common.NAL_TYPE.NAL_TYPE_PB, dstData);
                    }
                } else {
                    Log.d(TAG, "wrong h264 stream" + outData);
                    return -1;
                }
                mediaCodec.releaseOutputBuffer(outputBufferIndex, false);
            } else {
                Log.d(TAG, "wrong h264 stream" + outputBufferIndex);
            }

        } catch (Throwable t) {
            Log.d(TAG, "error, drop buffer");
            t.printStackTrace();
        }
        return pos;
    }

    private void getPreferColorFormat() {
        // 查看编码器支持的颜色格式，并保存下来，作为后面YUV转化的标识
        int codecCount = MediaCodecList.getCodecCount();
        MediaCodecInfo mci = null;
        int i = 0;
        for (i = 0; i < codecCount; i++) {
            mci = MediaCodecList.getCodecInfoAt(i);
            if (mci.isEncoder()) {
                Log.d("media", "codec name: " + mci.getName());
                if (mci.getName().contains("264") || mci.getName().contains("AVC") || mci.getName().contains("avc") || mci.getName().contains("OMX.hisi.video.encoder")) {
                    break;
                }
            }
        }
        if (i == codecCount) {
            Log.e("media", "codec name find error");
        }
        if (mci != null) {
            Log.d("media", "get codec name: " + mci.getName());
            try {
                int colorFormat2[] = mci.getCapabilitiesForType("video/avc").colorFormats; // 部分设备该函数调用抛出异常
                ArrayList<Integer> listCF = new ArrayList<Integer>();
                for (int cf : colorFormat2) {
                    listCF.add(cf);
                }
                m_colorFormat = listCF.contains(CodecCapabilities.COLOR_FormatYUV420Planar) ? CodecCapabilities.COLOR_FormatYUV420Planar : CodecCapabilities.COLOR_FormatYUV420SemiPlanar;
            } catch (Exception e) {
                e.printStackTrace();
            }
            Log.d("media", "m_colorFormat = " + m_colorFormat);
        }
    }

    private void YV12toNV21(final byte[] input, final byte[] output, final int width, final int height) {
        final int frameSize = width * height;
        final int qFrameSize = frameSize / 4;
        final int tempFrameSize = frameSize * 5 / 4;

        System.arraycopy(input, 0, output, 0, frameSize); // Y

        for (int i = 0; i < qFrameSize; i++) {
            output[frameSize + i * 2] = input[frameSize + i]; // Cb (U)
            output[frameSize + i * 2 + 1] = input[tempFrameSize + i]; // Cr (V)
        }
    }
}
