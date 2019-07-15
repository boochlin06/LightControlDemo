package com.emotibot.robotvision.demo;

import android.util.Log;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class RemoteService {
    private static volatile RemoteService mInstance;
    public static final String APP_ID = "0f908d82af1c4350a9b4c96b8124d344";
    public static final String USER_ID = "0059f88e-5a52-449b-b489-f0cc78c5e82f";
    public static final String SESSION_ID = "LIGHT_CONTROL";
    public static final String BFOP_URL = "http://poc1.emotibot.com:8141/v1/openapi/";
    BfopService bfopService;


    private RemoteService() {
        Retrofit retrofit = new Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                .addConverterFactory(ScalarsConverterFactory.create())
                .baseUrl(BFOP_URL)
                .build();
        bfopService = retrofit.create(BfopService.class);
    }

    public static RemoteService getInstance() {
        if (mInstance == null) {
            synchronized (RemoteService.class) {
                if (mInstance == null) {
                    mInstance = new RemoteService();
                }
            }
        }
        return mInstance;
    }

    public void sendControlMessage(String colorString, okhttp3.Callback callback) {
        Log.d("test", "send message:" + colorString);
        OkHttpClient client = new OkHttpClient();

        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create(mediaType, colorString);
        Request request = new Request.Builder()
                .url("http://poc1.emotibot.com:8141/v1/openapi")
                .post(body)
                .addHeader("appid", APP_ID)
                .addHeader("userid", USER_ID)
                .addHeader("sessionid", SESSION_ID)
                .build();

        okhttp3.Call mcall = client.newCall(request);
        mcall.enqueue(callback);
    }

    public void sendControlMessage(String colorString, Callback<String> callback) {
        Call<String> call = bfopService.changeColor(APP_ID, USER_ID
                , SESSION_ID, colorString);

        Log.d("test", "colorString is " + colorString + ",header:" + call.request().headers());
        Log.d("test", "content-type:" + call.request().header("Content-Type") + call.request().body().contentType());
        call.enqueue(callback);
    }
}
