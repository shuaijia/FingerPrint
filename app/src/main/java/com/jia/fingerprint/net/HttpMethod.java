package com.jia.fingerprint.net;

import android.util.Log;

import com.jia.fingerprint.model.Login;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Describtion:
 * Created by jia on 2017/6/7.
 * 人之所以能，是相信能
 */
public class HttpMethod {

    public static final String TAG = "HttpMethod";

    // 请求超时
    private static final int TIME_OUT = 5;

    private Retrofit retrofit;

    private BaseService service;


    // 私有构造
    private HttpMethod() {

        // 手动创建okhttpclient，并设置超时，添加和保存cookie
        OkHttpClient okHttpClient = new OkHttpClient();
        final OkHttpClient.Builder buidler = okHttpClient.newBuilder();

        // 设置各超时
        buidler.connectTimeout(TIME_OUT, TimeUnit.SECONDS);
        buidler.writeTimeout(TIME_OUT, TimeUnit.SECONDS);
        buidler.readTimeout(TIME_OUT, TimeUnit.SECONDS);
        // 设置重定向 其实默认也是true
        buidler.followRedirects(true);

        // 保存cookie
        buidler.addInterceptor(new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Response originalResponse = chain.proceed(chain.request());
                //这里获取请求返回的cookie
                if (!originalResponse.headers("Set-Cookie").isEmpty()) {
                    if (originalResponse.headers("Set-Cookie").size() == 2) {
                        CookieUtils.COOKIE = originalResponse.headers("Set-Cookie").get(1);
                        Log.e(TAG, "intercept: 保存cookie" + CookieUtils.COOKIE);
                    }
                }
                return originalResponse;
            }
        });

        // 添加cookie
        buidler.addInterceptor(new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Request original = chain.request();
                //添加请求参数
                HttpUrl url = original.url().newBuilder()
                        .addQueryParameter("loginType", CookieUtils.LOGIN_TYPE)
                        .addQueryParameter("params.loginType", CookieUtils.LOGIN_TYPE)
                        .addQueryParameter("unTyxlLoginToken", CookieUtils.UNTYXL_LOGIN_TOKEN)
                        .addQueryParameter("page.searchItem.siteCode", CookieUtils.SITE_CODE)
                        .build();
                //添加请求头
                Request request = original.newBuilder()
                        .addHeader("Cookie", CookieUtils.COOKIE)
                        .method(original.method(), original.body())
                        .url(url)
                        .build();
                return chain.proceed(request);

            }
        });

        retrofit = new Retrofit.Builder()
                .client(buidler.build())
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .baseUrl(UrlConfig.BASE_URL)
                .build();

        service = retrofit.create(BaseService.class);
    }


    //在访问HttpMethods时创建单例
    private static class SingletonHolder {
        private static final HttpMethod INSTANCE = new HttpMethod();
    }

    // 获取单例
    public static HttpMethod getInstance() {
        return SingletonHolder.INSTANCE;
    }

    /**
     * 用来统一处理Http的resultCode,并将HttpResult的Data部分剥离出来返回给subscriber
     *
     * @param <T> Subscriber真正需要的数据类型，也就是Data部分的数据类型
     */
    private class HttpResultFunc<T> implements Func1<HttpResult<T>, T> {

        @Override
        public T call(HttpResult<T> tHttpResult) {
            if (tHttpResult.getErrorCode().equals("0")) {
                throw new PreDealException(tHttpResult.getErrorMsg());
            }
            return tHttpResult.getData();
        }
    }


    //+++++++++++++++++++++++++++以下为具体网络请求方法++++++++++++++++++++++++++++++++++++++++++

    /**
     * 登录
     *
     * @param name
     * @param password
     * @param subscriber
     */
    public void login(String name, String password, Subscriber<Login> subscriber) {
        service.login(name, password)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(subscriber);
    }

}
