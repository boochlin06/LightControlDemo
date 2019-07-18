package com.emotibot.robotvision.demo;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.http.Body;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface LightControlService {
    public static final String BFOP_BASE_PATH = "v1/openapi";
    public static final String BFOP_APP_ID = "0f908d82af1c4350a9b4c96b8124d344";


    @POST("v1/openapi")
    Call<String> changeColor(@Header("appid") String appid, @Header("userid") String userid
            ,@Header("sessionid") String sessionid,@Body String msg);

}
