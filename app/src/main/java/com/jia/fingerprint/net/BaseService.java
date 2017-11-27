package com.jia.fingerprint.net;

import com.jia.fingerprint.model.Login;

import retrofit2.http.POST;
import retrofit2.http.Query;
import rx.Observable;

/**
 * Describtion:
 * Created by jia on 2017/6/6.
 * 人之所以能，是相信能
 */
public interface BaseService {

    @POST("mobile/login_mobileLogin.action")
    Observable<Login> login(@Query("loginId") String name, @Query("passwd") String password);


}
