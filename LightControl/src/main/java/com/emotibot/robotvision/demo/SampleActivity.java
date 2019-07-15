package com.emotibot.robotvision.demo;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.emotibot.intellieyecoreaar.InferResult;
import com.emotibot.intellieyecoreaar.IntelliEyeCoreManager;

import org.json.JSONException;
import org.json.JSONObject;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.io.IOException;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

import static org.opencv.core.Core.FILLED;
import static org.opencv.core.Core.flip;

public class SampleActivity extends AppCompatActivity {

    private static final String LIB_NAME_OPEN_CV = "opencv_java3";

    static {
        try {
            System.loadLibrary(LIB_NAME_OPEN_CV);
        } catch (Exception e) {
            e.printStackTrace();
        } catch (Error e) {
            e.printStackTrace();
        }
    }

    JavaCameraView mCameraView = null;
    @BindView(R.id.btnRed)
    Button btnRed;
    @BindView(R.id.btnBlue)
    Button btnBlue;
    @BindView(R.id.btnYellow)
    Button btnYellow;
    @BindView(R.id.btnGreen)
    Button btnGreen;
    @BindView(R.id.txtRawData)
    TextView txtRawData;

    private Mat mCameraBuffer = null;

    private StringBuilder stringBuilder = null;
    BfopService bfopService;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        Utility.setWindow(this);
        this.setContentView(R.layout.sample_main_layout);
        ButterKnife.bind(this);

        Retrofit retrofit = new Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                .addConverterFactory(ScalarsConverterFactory.create())
                .baseUrl("http://poc1.emotibot.com:8141/")
                .build();
        bfopService = retrofit.create(BfopService.class);

        stringBuilder = new StringBuilder();
        mCameraView = (JavaCameraView) findViewById(R.id.camera_impl);
        IntelliEyeCoreManager.getInstance().init(this, false, true, false, false, false);
        mCameraView.setCvCameraViewListener(new CameraFrameProcessor());

    }


    @Override
    protected void onResume() {
        super.onResume();
        if (null != mCameraView) {
            mCameraView.setVisibility(View.VISIBLE);
            mCameraView.enableView();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // RobotVisionManager.getInstance().onPause();
        if (null != mCameraView) {
            mCameraView.disableView();
            mCameraView.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // RobotVisionManager.getInstance().deInit();

        if (null != mCameraView) {
            mCameraView.disableView();
        }
    }

    private ArrayList<EmotionPoint> rects = new ArrayList<>();

    int count = 0 ;

    private class CameraFrameProcessor implements CameraBridgeViewBase.CvCameraViewListener2 {
        @Override
        public void onCameraViewStarted(int width, int height) {
            mCameraBuffer = new Mat(width, height, CvType.CV_8UC1);
        }

        @Override
        public void onCameraViewStopped() {
            if (null != mCameraBuffer) {
                mCameraBuffer.release();
                mCameraBuffer = null;
            }
        }

        @Override
        public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
            long startTime = System.currentTimeMillis();
            Mat cameraFrame = inputFrame.rgba();
//            flip(cameraFrame, cameraFrame, 1);
            count ++;
            if (count % 3 == 0) {
                produceNewPoint();
            }
            cameraFrame = drawRectPoint(cameraFrame);
            Imgproc.cvtColor(cameraFrame, mCameraBuffer, Imgproc.COLOR_RGBA2BGR);
//            flip(mCameraBuffer, mCameraBuffer, 1);

            final InferResult mInferResult = IntelliEyeCoreManager.getInstance().processFrame(mCameraBuffer, cameraFrame
                    , true);
//            runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    emotionToBfop(mInferResult.getEmotionData());
////                    showRawData(mInferResult);
//                }
//            });
            Log.d("test","total used time:"+(System.currentTimeMillis()-startTime));
            // RobotVisionManager.getInstance().processFrame(System.currentTimeMillis(), mCameraBuffer, cameraFrame, true);
            return cameraFrame;
        }
    }
    private class EmotionPoint{
        public Point startPoint;
        public int emotion;
        public EmotionPoint(Point startPoint , int emotion){
            this.startPoint = startPoint;
            this.emotion = emotion;
        }
    }

    private Mat drawRectPoint(Mat frame) {
        ArrayList<Point> remove = new ArrayList<>();
        for (int i = 0; i < rects.size(); i++) {
            Point end = new Point(rects.get(i).startPoint.x + 100, rects.get(i).startPoint.y + 100);
            rects.get(i).startPoint.y += 40;
            Scalar color;
            if (rects.get(i).emotion == 1) {
                color = new Scalar(255,0,0,0);
            } else if (rects.get(i).emotion == 2) {
                color = new Scalar(0,0,255,0);
            } else if (rects.get(i).emotion == 3) {
                color = new Scalar(255,255,0,0);
            } else if (rects.get(i).emotion == 4) {
                color = new Scalar(0,255,0,0);
            } else {
                color = new Scalar(255,0,0,0);
            }
            Imgproc.rectangle(frame, rects.get(i).startPoint, end, color, FILLED);
            if (rects.get(i).startPoint.y > 1024)
                remove.add(rects.get(i).startPoint);
        }
        rects.removeAll(remove);

        return frame;
    }

    private void produceNewPoint() {
        int emotion = (int) (Math.random() * 4) +1 ;
        int x = (emotion-1) * 132 + 20;
        EmotionPoint point = new EmotionPoint(new Point(x,0),emotion);
        rects.add(point);
    }

    @SuppressLint("StringFormatInvalid")
    private void showRawData(InferResult inferResult) {
        stringBuilder.setLength(0);
        float[] emotionData = inferResult.getEmotionData();
        String emotions = getString(R.string.rawData_emotion_bar_summary
                , emotionData[0] * 100, emotionData[1] * 100, emotionData[2] * 100
                , emotionData[3] * 100, emotionData[4] * 100, emotionData[5] * 100, emotionData[6] * 100
                , emotionData[7] * 100, emotionData[8] * 100);
        stringBuilder.append(emotions);
        txtRawData.setText(stringBuilder.toString());
    }

    public String emotionToBfop(float[] emotionData) {
        // nine emotion 0 angry 1 disgust 2 happy 3 sad 4 surprise 5 fear 6 neutral 7 contempt 8 confused
        // four emotion for edu 0 normal 1 passion 2 upset 3 confuse
        // nine -> four : 0,1,3,5,7 -> 2 ; 2 -> 1 ; 6 -> 0; 4,8 -> 3
        int firstAt = 0;
        int secondAt = 0;
        float max = 0;
        for (int i = 0; i < emotionData.length; i++) {

            float emotion = emotionData[i];
            if (emotion > max) {
                max = emotion;
                firstAt = i;
            }
        }
        String color = mapColor(firstAt);

        btnRed.setBackgroundColor(ContextCompat.getColor(this, R.color.btn_background_red_dark));
        btnGreen.setBackgroundColor(ContextCompat.getColor(this, R.color.btn_background_green_dark));
        btnBlue.setBackgroundColor(ContextCompat.getColor(this, R.color.btn_background_blue_dark));
        btnYellow.setBackgroundColor(ContextCompat.getColor(this, R.color.btn_background_yellow_dark));
        if (color.equals("紅色")) {
            btnRed.setBackgroundColor(ContextCompat.getColor(this, R.color.btn_background_red_light));
        } else if (color.equals("綠色")) {
            btnGreen.setBackgroundColor(ContextCompat.getColor(this, R.color.btn_background_green_light));
        } else if (color.equals("黃色")) {
            btnYellow.setBackgroundColor(ContextCompat.getColor(this, R.color.btn_background_yellowlight));
        } else if (color.equals("藍色")) {
            btnBlue.setBackgroundColor(ContextCompat.getColor(this, R.color.btn_background_blue_light));
        } else {
            btnBlue.setBackgroundColor(ContextCompat.getColor(this, R.color.btn_background_blue_light));
        }
        String colorString = "{ \"text\": \"" + "卧室灯调成" + color + "\", \"customInfo\": { \"deviceId\": \"1\" } }";
//
//        Call<String> call = bfopService.changeColor("0f908d82af1c4350a9b4c96b8124d344","0059f88e-5a52-449b-b489-f0cc78c5e82f"
//                ,"huaiwei_test",colorString);
//
//        Log.d("test","colorString is "+colorString+",header:"+call.request().headers());
//        Log.d("test","content-type:"+call.request().header("Content-Type")+call.request().body().contentType());
//        call.enqueue(new Callback<String>() {
//
//            @Override
//            public void onResponse(Call<String> call, Response<String> response) {
//                try {
//                    Log.d("test","res:"+response.isSuccessful()+","+response.errorBody().string()+call.request().body().contentType());
//                    Log.d("test","message:"+response.code());
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//
//            @Override
//            public void onFailure(Call<String> call, Throwable t) {
//                t.printStackTrace();
//            }
//        });






//        OkHttpClient client = new OkHttpClient();
//
//        MediaType mediaType = MediaType.parse("application/json");
//        RequestBody body = RequestBody.create(mediaType, colorString);
//        Request request = new Request.Builder()
//                .url("http://poc1.emotibot.com:8141/v1/openapi")
//                .post(body)
//                .addHeader("appid", "0f908d82af1c4350a9b4c96b8124d344")
//                .addHeader("userid", "0059f88e-5a52-449b-b489-f0cc78c5e82f")
//                .addHeader("sessionid", "test_session")
//                .build();
//
//        okhttp3.Call mcall = client.newCall(request);
//        mcall.enqueue(new okhttp3.Callback() {
//            @Override
//            public void onFailure(okhttp3.Call call, IOException e) {
//                e.printStackTrace();
//            }
//
//            @Override
//            public void onResponse(okhttp3.Call call, okhttp3.Response response) throws IOException {
//                if (null != response.cacheResponse()) {
//                    String str = response.cacheResponse().toString();
//
//                } else {
//                    response.body().string();
//                    String str = response.networkResponse().toString();
//                    Log.i("test", "network---" + str);
//                }
//                Log.i("test", response.message());
////                runOnUiThread(new Runnable() {
////                    @Override
////                    public void run() {
////                        Toast.makeText(getApplicationContext(), "卧室灯调成"+color, Toast.LENGTH_SHORT).show();
////                    }
////                });
//            }
//        });
//        Response response = client.newCall(request).enqueue();
        return colorString;
    }

    private String mapColor(int emotion) {
        switch (emotion) {
            case 0:
                return "紅色";
            case 1:
                return "綠色";
            case 2:
                return "黃色";
            case 3:
                return "紅色";
            case 4:
                return "黃色";
            case 5:
                return "藍色";
            case 6:
                return "藍色";
            case 7:
                return "綠色";
            case 8:
                return "藍色";
        }
        return "藍色";
    }
}
