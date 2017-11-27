package com.jia.jsfingerlib;

import android.Manifest;
import android.app.Activity;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.CancellationSignal;
import android.support.v4.app.ActivityCompat;

/**
 * Description: 指纹认证工具类
 * Created by jia on 2017/11/27.
 * 人之所以能，是相信能
 */
public class JsFingerUtils {

    private static final String TAG = "JsFingerUtils";

    private final static int REQUEST_CODE_CONFIRM_DEVICE_CREDENTIALS = 0;


    private FingerprintManager manager;
    private KeyguardManager mKeyManager;
    private CancellationSignal mCancellationSignal;
    //回调方法
    private FingerprintManager.AuthenticationCallback mSelfCancelled;

    private Context mContext;

    private FingerListener listener;

    public JsFingerUtils(Context mContext) {
        this.mContext = mContext;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            manager = (FingerprintManager) mContext.getSystemService(Context.FINGERPRINT_SERVICE);
            mKeyManager = (KeyguardManager) mContext.getSystemService(Context.KEYGUARD_SERVICE);
            mCancellationSignal = new CancellationSignal();
            initSelfCancelled();
        }
    }

    /**
     * 开始监听识别
     */
    public void startListening(FingerListener listener) {

        this.listener = listener;

        //android studio 上，没有这个会报错
        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.USE_FINGERPRINT) != PackageManager.PERMISSION_GRANTED) {
            listener.onFail(false, "未开启权限");
            return;
        }

        if (isFinger() == null) {
            listener.onStartListening();
            manager.authenticate(null, mCancellationSignal, 0, mSelfCancelled, null);
        } else {
            listener.onFail(false, isFinger());
        }
    }

    /**
     * 停止识别
     */
    public void cancelListening() {
        if (mCancellationSignal != null) {
            mCancellationSignal.cancel();
            listener.onStopListening();
        }
    }

    private void initSelfCancelled() {
        mSelfCancelled = new FingerprintManager.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode, CharSequence errString) {
                // 多次指纹密码验证错误后，进入此方法；并且，不能短时间内调用指纹验证
                listener.onAuthenticationError(errorCode, errString);
            }

            @Override
            public void onAuthenticationHelp(int helpCode, CharSequence helpString) {
                listener.onAuthenticationHelp(helpCode, helpString);
            }

            @Override
            public void onAuthenticationSucceeded(FingerprintManager.AuthenticationResult result) {
                listener.onSuccess(result);
            }

            @Override
            public void onAuthenticationFailed() {
                listener.onFail(true, "识别失败");
            }
        };
    }

    /**
     * 硬件是否支持
     * <p/>
     * 返回null则可以进行指纹识别
     * 否则返回对应的原因
     *
     * @return
     */
    public String isFinger() {

        //android studio 上，没有这个会报错
        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.USE_FINGERPRINT) != PackageManager.PERMISSION_GRANTED) {

            //android studio 上，没有这个会报错
            if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.USE_FINGERPRINT) != PackageManager.PERMISSION_GRANTED) {
                return "没有指纹识别权限";
            }
            //判断硬件是否支持指纹识别
            if (!manager.isHardwareDetected()) {
                return "没有指纹识别模块";
            }
            //判断 是否开启锁屏密码
            if (!mKeyManager.isKeyguardSecure()) {
                return "没有开启锁屏密码";
            }
            //判断是否有指纹录入

            if (!manager.hasEnrolledFingerprints()) {
                return "没有录入指纹";
            }
        }

        return null;
    }

    /**
     * 检查SDK版本
     *
     * @return
     */
    public boolean checkSDKVersion() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return true;
        }
        return false;
    }

    /**
     * 跳转锁屏密码
     */
    private void showAuthenticationScreen() {

        Intent intent = mKeyManager.createConfirmDeviceCredentialIntent("finger", "测试指纹识别");
        if (intent != null) {
            ((Activity) mContext).startActivityForResult(intent, REQUEST_CODE_CONFIRM_DEVICE_CREDENTIALS);
        }
    }
}
