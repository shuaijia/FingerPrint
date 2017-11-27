package com.jia.fingerprint.activity;

import android.app.KeyguardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.hardware.fingerprint.FingerprintManager;
import android.os.CancellationSignal;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import com.jia.fingerprint.R;
import com.jia.fingerprint.model.Login;
import com.jia.fingerprint.net.HttpMethod;
import com.jia.fingerprint.utils.SharedPreferencesUtils;
import com.jia.jsfingerlib.FingerListener;
import com.jia.jsfingerlib.JsFingerUtils;

import rx.Subscriber;


/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity implements OnClickListener, FingerListener {

    private static final String TAG = "LoginActivity";

    private EditText mEmailView;
    private EditText mPasswordView;

    private Button bt_login;

    private Context mContext;

    private AlertDialog dialog;

    private int error_num = 0;

    private JsFingerUtils jsFingerUtils;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login2);
        mContext = LoginActivity.this;


        mEmailView = (EditText) findViewById(R.id.email);
        bt_login = (Button) findViewById(R.id.bt_login);
        bt_login.setOnClickListener(this);
        mPasswordView = (EditText) findViewById(R.id.password);

        jsFingerUtils = new JsFingerUtils(mContext);

        // 判断是否开启指纹登录
        if (SharedPreferencesUtils.getData(mContext, "openFinger", false)) {

            mEmailView.setText(SharedPreferencesUtils.getData(mContext, "phone", "无数据"));
            mPasswordView.setText(SharedPreferencesUtils.getData(mContext, "pwd", "无数据"));

            dialog = new AlertDialog.Builder(mContext)
                    .setTitle("登录")
                    .setMessage("\n   开始识别指纹")
                    .setIcon(R.mipmap.finger_blue)
                    .setCancelable(false)
                    .setPositiveButton("账号密码登录", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            jsFingerUtils.cancelListening();
                            dialog.dismiss();
                        }
                    }).create();
            dialog.show();

            jsFingerUtils.startListening(this);
        }
    }

    /**
     * 请求网络  去  登录
     *
     * @param phone
     * @param pwd
     */
    private void loginFromNet(final String phone, final String pwd) {

        HttpMethod.getInstance().login(phone, pwd, new Subscriber<Login>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {
                Log.e(TAG, "onError: " + e.toString());
            }

            @Override
            public void onNext(Login login) {
                Log.e(TAG, "onNext: " + login.toString());

                if (dialog != null && dialog.isShowing()) {
                    dialog.dismiss();
                }

                SharedPreferencesUtils.saveData(mContext, "phone", phone);
                SharedPreferencesUtils.saveData(mContext, "pwd", pwd);

                startActivity(new Intent(mContext, MainActivity.class));
                finish();

            }
        });
    }


    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.bt_login) {

            loginFromNet(mEmailView.getText().toString(), mPasswordView.getText().toString());

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
        dialog.setMessage("\n   指纹识别成功，登录中...");

        /**
         * 识别成功  去登录
         */
        loginFromNet(SharedPreferencesUtils.getData(mContext, "phone", ""), SharedPreferencesUtils.getData(mContext, "pwd", ""));
    }

    @Override
    public void onFail(boolean isNormal, String info) {
        if (isNormal) {
            if (error_num == 2) {
                dialog.setMessage("\n   指纹识别失败三次，请使用密码登录");
                jsFingerUtils.cancelListening();
            } else {
                error_num++;
                dialog.setMessage("\n   指纹识别失败" + error_num);
            }
        } else {
            dialog.setMessage("\n   " + info);
        }
    }

    @Override
    public void onAuthenticationError(int errorCode, CharSequence errString) {

    }

    @Override
    public void onAuthenticationHelp(int helpCode, CharSequence helpString) {

    }
}

