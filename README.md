# FingerPrint
安卓6.0指纹识别库，及demo演示
对以下情况都已进行封装
- 版本判断，低于23不可以进行指纹识别
- 未开启权限
- 手机没有指纹识别功能
- 没有开启锁屏密码
- 没有录入指纹
- 识别失败
- 多次识别失败的情况

## 指纹识别登录  流程
![image](https://github.com/shuaijia/FingerPrint/blob/master/img/program.png)

## 使用
### step 1
```
allprojects {
  repositories {
  ...
  maven { url 'https://www.jitpack.io' }
  }
}
```
```
dependencies {
  compile 'com.github.shuaijia:FingerPrint:v1.0'
}
```

### step 2
创建工具类对象
```
JsFingerUtils jsFingerUtils = new JsFingerUtils(mContext);
```
### step 3
开始识别
```
jsFingerUtils.startListening(MainActivity.this);
```
取消识别
```
jsFingerUtils.cancelListening();
```
### step 4
当然了，在开始识别的时候需要传入识别回调
```
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
```
这样就ok了！

同时，我在工具类中也封装了一些判断权限，判断SDK，跳转设置锁屏密码的方法，就不再列举。

### 更过精彩内容，您可以关注我的微信公众号————**安卓干货营**
![image](https://github.com/shuaijia/FingerPrint/blob/master/img/weixin.jpg)
