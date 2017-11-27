package com.jia.jsfingerlib;

import android.hardware.fingerprint.FingerprintManager;

/**
 * Description: 指纹识别回调
 * Created by jia on 2017/11/27.
 * 人之所以能，是相信能
 */
public interface FingerListener {

    /**
     * 开始识别
     */
    void onStartListening();

    /**
     * 停止识别
     */
    void onStopListening();

    /**
     * 识别成功
     * @param result
     */
    void onSuccess(FingerprintManager.AuthenticationResult result);

    /**
     * 识别失败
     */
    void onFail(boolean isNormal,String info);

    /**
     * 多次识别失败 的 回调方法
     * @param errorCode
     * @param errString
     */
    void onAuthenticationError(int errorCode, CharSequence errString);

    /**
     * 识别提示
     */
    void onAuthenticationHelp(int helpCode, CharSequence helpString);

}
