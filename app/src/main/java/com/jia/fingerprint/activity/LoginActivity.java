package com.jia.fingerprint.activity;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.fingerprint.FingerprintManager;
import android.os.CancellationSignal;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.app.LoaderManager.LoaderCallbacks;

import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;

import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import com.jia.fingerprint.R;
import com.jia.fingerprint.model.Login;
import com.jia.fingerprint.net.HttpMethod;
import com.jia.fingerprint.utils.SharedPreferencesUtils;

import rx.Subscriber;


/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity implements OnClickListener {

    private static final String TAG = "LoginActivity";

    private final static int REQUEST_CODE_CONFIRM_DEVICE_CREDENTIALS = 0;

    private EditText mEmailView;
    private EditText mPasswordView;

    private Button bt_login;

    private FingerprintManager manager;
    private KeyguardManager mKeyManager;
    private CancellationSignal mCancellationSignal;
    //回调方法
    private FingerprintManager.AuthenticationCallback mSelfCancelled;

    private Context mContext;

    private AlertDialog dialog;

    private int error_num = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login2);
        mContext = LoginActivity.this;


        mEmailView = (EditText) findViewById(R.id.email);
        bt_login = (Button) findViewById(R.id.bt_login);
        bt_login.setOnClickListener(this);
        mPasswordView = (EditText) findViewById(R.id.password);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            manager = (FingerprintManager) this.getSystemService(Context.FINGERPRINT_SERVICE);
            mKeyManager = (KeyguardManager) this.getSystemService(Context.KEYGUARD_SERVICE);
            mCancellationSignal = new CancellationSignal();
            initSelfCancelled();
        }

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
                            mCancellationSignal.cancel();
                            dialog.dismiss();
                        }
                    }).create();
            dialog.show();

            // 判断是否可以指纹识别
            if (isFinger() != null) {

                dialog.setMessage("\n   " + isFinger());

            } else {
                /**
                 * 开始监听指纹输入
                 */
                startListening(null);
            }
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

                if(dialog!=null && dialog.isShowing()){
                    dialog.dismiss();
                }

                SharedPreferencesUtils.saveData(mContext,"phone",phone);
                SharedPreferencesUtils.saveData(mContext,"pwd",pwd);

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
                // 多次指纹密码验证错误后，进入此方法；并且，不能短时间内调用指纹验证
//            dialog.setMessage("\n   " + errString);
//            showAuthenticationScreen();
            }

            @Override
            public void onAuthenticationHelp(int helpCode, CharSequence helpString) {

//            dialog.setMessage("\n   " + helpString);
            }

            @Override
            public void onAuthenticationSucceeded(FingerprintManager.AuthenticationResult result) {
                dialog.setMessage("\n   指纹识别成功，登录中...");

                /**
                 * 识别成功  去登录
                 */
                loginFromNet(SharedPreferencesUtils.getData(mContext, "phone", ""), SharedPreferencesUtils.getData(mContext, "pwd", ""));
            }

            @Override
            public void onAuthenticationFailed() {
                if (error_num == 2) {
                    dialog.setMessage("\n   指纹识别失败三次，请使用密码登录");
                    mCancellationSignal.cancel();
                } else {
                    error_num++;
                    dialog.setMessage("\n   指纹识别失败" + error_num);
                }
            }
        };
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
     * 锁屏密码
     */
    private void showAuthenticationScreen() {

        Intent intent = mKeyManager.createConfirmDeviceCredentialIntent("finger", "测试指纹识别");
        if (intent != null) {
            startActivityForResult(intent, REQUEST_CODE_CONFIRM_DEVICE_CREDENTIALS);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_CONFIRM_DEVICE_CREDENTIALS) {
            // Challenge completed, proceed with using cipher
            if (resultCode == RESULT_OK) {
                Toast.makeText(this, "识别成功", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "识别失败", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void Log(String tag, String msg) {
        Log.d(tag, msg);
    }
}

