package com.jia.fingerprint.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.jia.fingerprint.R;
import com.jia.fingerprint.utils.SharedPreferencesUtils;
import com.jia.jsfingerlib.FingerListener;
import com.jia.jsfingerlib.JsFingerUtils;

public class MainActivity extends AppCompatActivity implements FingerListener {

    private static final String TAG = "MainActivity";


    private TextView tv_name;
    private TextView tv_pwd;

    private AlertDialog dialog;

    private Context mContext;

    private JsFingerUtils jsFingerUtils;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (dialog != null && dialog.isShowing()) {
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

        tv_name.setText("手机号：  " + SharedPreferencesUtils.getData(mContext, "phone", "无数据"));
        tv_pwd.setText("密  码：  " + SharedPreferencesUtils.getData(mContext, "pwd", "无数据"));

        jsFingerUtils = new JsFingerUtils(mContext);

        checkFinger();

    }

    private void checkFinger() {
        if (!SharedPreferencesUtils.getData(mContext, "openFinger", false)) {

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
                    .setPositiveButton("确认设置", null)
                    .create();
            dialog.show();

            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    jsFingerUtils.startListening(MainActivity.this);
                }
            });
        }
    }


    @Override
    public void onStartListening() {
        dialog.setMessage("\n   识别中...");
    }

    @Override
    public void onStopListening() {

    }

    @Override
    public void onSuccess(FingerprintManager.AuthenticationResult result) {
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setVisibility(View.GONE);
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setVisibility(View.GONE);

        dialog.setMessage("\n   指纹认证成功，已开启指纹登录");
        // 认证成功，开启指纹登录
        SharedPreferencesUtils.saveData(mContext, "openFinger", true);

        handler.sendEmptyMessageDelayed(11, 1000);
    }

    @Override
    public void onFail(boolean isNormal, String info) {

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setVisibility(View.GONE);
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setVisibility(View.GONE);

        if (isNormal) {
            dialog.setMessage("\n   指纹认证失败，请稍后再试");
        } else {
            dialog.setMessage("\n   " + info);
        }

        jsFingerUtils.cancelListening();
        handler.sendEmptyMessageDelayed(11, 1000);
    }

    @Override
    public void onAuthenticationError(int errorCode, CharSequence errString) {

    }

    @Override
    public void onAuthenticationHelp(int helpCode, CharSequence helpString) {

    }
}
