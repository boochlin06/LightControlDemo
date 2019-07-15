package com.emotibot.robotvision.demo;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.http.Body;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface BfopService {
    public static final String BFOP_BASE_PATH = "v1/openapi";
    public static final String BFOP_APP_ID = "0f908d82af1c4350a9b4c96b8124d344";


    @POST("v1/openapi")
    Call<String> changeColor(@Header("appid") String appid, @Header("userid") String userid
            ,@Header("sessionid") String sessionid,@Body String msg);

}


/**
 * curl -X POST \
 * http://172.16.100.30:8080/v1/openapi \
 * -H 'Content-Type: application/json' \
 * -H 'Postman-Token: 41bcdb0a-ca8f-4f5b-a77c-d4e3ab3c63b4,714640c6-28a0-42d3-9848-e0be34919015,b43e9d8c-f8a7-4275-b4ac-0891cde3cce4,7ec53027-6da5-44d1-8c8b-99b1ba2fe3f7' \
 * -H 'appid: 0f908d82af1c4350a9b4c96b8124d344' \
 * -H 'cache-control: no-cache,no-cache,no-cache,no-cache' \
 * -H 'sessionId: 1' \
 * -H 'userid: 0059f88e-5a52-449b-b489-f0cc78c5e82f' \
 * -d '{
 * "text": "卧室灯调成红色",
 * "customInfo": {
 * "deviceId": "1"
 * }
 * }'
 **/