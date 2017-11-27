package com.jia.fingerprint.activity;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.CancellationSignal;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.jia.fingerprint.R;
import com.jia.fingerprint.utils.SharedPreferencesUtils;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private final static int REQUEST_CODE_CONFIRM_DEVICE_CREDENTIALS = 0;


    private TextView tv_name;
    private TextView tv_pwd;

    private FingerprintManager manager;
    private KeyguardManager mKeyManager;
    private CancellationSignal mCancellationSignal;
    //回调方法
    private FingerprintManager.AuthenticationCallback mSelfCancelled;

    private AlertDialog dialog;

    private Context mContext;

    private Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if(dialog!=null && dialog.isShowing()){
                dialog.dismiss();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = MainActivity.this;

        tv_name = (TextView) findViewById(R.id.tv_name);
        tv_pwd = (TextView) findViewById(R.id.tv_pwd);

//        tv_name.setText("手机号：  " + SharedPreferencesUtils.getData(mContext, "phone", "无数据"));
//        tv_pwd.setText("密  码：  " + SharedPreferencesUtils.getData(mContext, "pwd", "无数据"));

        tv_name.setText("手机号： *********** "  );
        tv_pwd.setText("密  码：  ********"  );

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            manager = (FingerprintManager) this.getSystemService(Context.FINGERPRINT_SERVICE);
            mKeyManager = (KeyguardManager) this.getSystemService(Context.KEYGUARD_SERVICE);
            mCancellationSignal = new CancellationSignal();
            initSelfCancelled();

            checkFinger();
        }
    }

    private void checkFinger() {
        if (!SharedPreferencesUtils.getData(mContext, "openFinger", false) && isFinger() == null) {

            dialog = new AlertDialog.Builder(mContext)
                    .setTitle("提示")
                    .setMessage("\n   是否开启指纹登录")
                    .setIcon(R.mipmap.finger_blue)
                    .setCancelable(false)
                    .setNegativeButton("以后再说", new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int i) {
                            dialog.dismiss();
                        }
                    })
                    .setPositiveButton("确认设置",null)
                    .create();
            dialog.show();

            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startListening(null);
                }
            });
        }
    }

    /**
     * 开始监听识别
     *
     * @param cryptoObject
     */
    public void startListening(FingerprintManager.CryptoObject cryptoObject) {

        //android studio 上，没有这个会报错
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.USE_FINGERPRINT) != PackageManager.PERMISSION_GRANTED) {
            dialog.setMessage("\n   没有指纹识别权限");
            return;
        }
        dialog.setMessage("\n   识别中...");
        manager.authenticate(cryptoObject, mCancellationSignal, 0, mSelfCancelled, null);

    }

    /**
     * 硬件是否支持
     *
     * @return
     */
    public String isFinger() {

        //android studio 上，没有这个会报错
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.USE_FINGERPRINT) != PackageManager.PERMISSION_GRANTED) {
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

        return null;
    }

    private void initSelfCancelled() {
        mSelfCancelled = new FingerprintManager.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode, CharSequence errString) {
                mCancellationSignal.cancel();
            }

            @Override
            public void onAuthenticationHelp(int helpCode, CharSequence helpString) {
                mCancellationSignal.cancel();
            }

            @Override
            public void onAuthenticationSucceeded(FingerprintManager.AuthenticationResult result) {

                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setVisibility(View.GONE);
                dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setVisibility(View.GONE);

                dialog.setMessage("\n   指纹认证成功，已开启指纹登录");
                // 认证成功，开启指纹登录
                SharedPreferencesUtils.saveData(mContext, "openFinger", true);

                handler.sendEmptyMessageDelayed(11,1000);
            }

            @Override
            public void onAuthenticationFailed() {

                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setVisibility(View.GONE);
                dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setVisibility(View.GONE);

                dialog.setMessage("\n   指纹认证失败，请稍后再试");
                mCancellationSignal.cancel();
                handler.sendEmptyMessageDelayed(11,1000);
            }
        };
    }
}
