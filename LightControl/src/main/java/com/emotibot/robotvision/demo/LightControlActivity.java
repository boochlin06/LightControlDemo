package com.emotibot.robotvision.demo;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.emotibot.intellieyecoreaar.InferResult;
import com.emotibot.intellieyecoreaar.IntelliEyeCoreManager;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;

import static org.opencv.core.Core.flip;

public class LightControlActivity extends AppCompatActivity {

    String TAG = LightControlActivity.class.getSimpleName();

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
    @BindView(R.id.txtRawData)
    TextView txtRawData;
    @BindView(R.id.btnBlock)
    LinearLayout btnBlock;

    private Mat mCameraBuffer = null;

    private StringBuilder stringBuilder = null;
    private String[] emotionColorArray;
    private RemoteService remoteService;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Utility.setWindow(this);
        this.setContentView(R.layout.activity_light_control);
        ButterKnife.bind(this);
        remoteService = RemoteService.getInstance();
        emotionColorArray = getResources().getStringArray(R.array.emotion_color_list);


        stringBuilder = new StringBuilder();
        mCameraView = (JavaCameraView) findViewById(R.id.camera_impl);

        initBtnColor();
        IntelliEyeCoreManager.getInstance().init(this, false, true, false, false, false);
        mCameraView.setCvCameraViewListener(new CameraFrameProcessor());
    }

    //    nine emotion 0 angry 1 disgust 2 happy 3 sad 4 surprise 5 fear 6 neutral 7 contempt 8 confused
//    <string name="color_angry">"紅色“</string>
//    <string name="color_confuse">"淡黃色“</string>
//    <string name="color_contempt">"紫棕色“</string>
//    <string name="color_disgust">"綠色“</string>
//    <string name="color_fear">"紫色“</string>
//    <string name="color_happy">"黃色“</string>
//    <string name="color_sad">"藍色“</string>
//    <string name="color_surprise">"橘色“</string>
//    <string name="color_neutral">"白色“</string>
    private void initBtnColor() {
        LayoutInflater vi = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        for (int i = 0; i < emotionColorArray.length; i++) {
            View btnLayout = vi.inflate(R.layout.btn_color_control, null);
            Button btnColor = (Button) btnLayout.findViewById(R.id.btnColor);
            btnLayout.setTag(emotionColorArray[i]);
            int finalI = i;
            btnColor.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showBtnColorlight(finalI);
                    String colorString = "{ \"text\": \"" + "卧室灯调成" + emotionColorArray[finalI] + "\", \"customInfo\": { \"deviceId\": \"1\" } }";
                    remoteService.sendControlMessage(colorString, new okhttp3.Callback() {
                        @Override
                        public void onFailure(okhttp3.Call call, IOException e) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getApplicationContext(), "網路不通", Toast.LENGTH_SHORT).show();
                                }
                            });
                            e.printStackTrace();
                        }

                        @Override
                        public void onResponse(okhttp3.Call call, okhttp3.Response response) throws IOException {
                            if (null != response.cacheResponse()) {
                                String str = response.cacheResponse().toString();

                                Log.i(TAG, "cache network---" + str);

                            } else {
                                String str = response.body().string();
                                Log.i(TAG, "network---" + str);
                            }

                        }
                    });

                }
            });
            if (emotionColorArray[i].equals(getString(R.string.color_angry))) {
                btnColor.setBackgroundColor(ContextCompat.getColor(this, R.color.md_red_500));
            } else if (emotionColorArray[i].equals(getString(R.string.color_confuse))) {
                btnColor.setBackgroundColor(ContextCompat.getColor(this, R.color.md_yellow_500));
            } else if (emotionColorArray[i].equals(getString(R.string.color_contempt))) {
                btnColor.setBackgroundColor(ContextCompat.getColor(this, R.color.md_purple_500));
            } else if (emotionColorArray[i].equals(getString(R.string.color_disgust))) {
                btnColor.setBackgroundColor(ContextCompat.getColor(this, R.color.md_green_500));
            } else if (emotionColorArray[i].equals(getString(R.string.color_fear))) {
                btnColor.setBackgroundColor(ContextCompat.getColor(this, R.color.md_amber_500));
            } else if (emotionColorArray[i].equals(getString(R.string.color_happy))) {
                btnColor.setBackgroundColor(ContextCompat.getColor(this, R.color.md_pink_500));
            } else if (emotionColorArray[i].equals(getString(R.string.color_surprise))) {
                btnColor.setBackgroundColor(ContextCompat.getColor(this, R.color.md_orange_500));
            } else if (emotionColorArray[i].equals(getString(R.string.color_neutral))) {
                btnColor.setBackgroundColor(ContextCompat.getColor(this, R.color.md_grey_500));
            } else if (emotionColorArray[i].equals(getString(R.string.color_sad))) {
                btnColor.setBackgroundColor(ContextCompat.getColor(this, R.color.md_blue_500));
            }
            btnBlock.addView(btnLayout);
        }
    }

    private void showBtnColorlight(int emotion) {
        for (int i = 0; i < emotionColorArray.length; i++) {
            LinearLayout btnLayout = btnBlock.findViewWithTag(emotionColorArray[i]);
            Button btnColor = (Button) btnLayout.findViewById(R.id.btnColor);
            if (emotionColorArray[i].equals(getString(R.string.color_angry))) {
                btnColor.setBackgroundColor(ContextCompat.getColor(this, R.color.md_red_500));
                if (i == emotion)
                    btnColor.setBackgroundColor(ContextCompat.getColor(this, R.color.md_red_200));
            } else if (emotionColorArray[i].equals(getString(R.string.color_confuse))) {
                btnColor.setBackgroundColor(ContextCompat.getColor(this, R.color.md_yellow_500));
                if (i == emotion)
                    btnColor.setBackgroundColor(ContextCompat.getColor(this, R.color.md_yellow_200));
            } else if (emotionColorArray[i].equals(getString(R.string.color_contempt))) {
                btnColor.setBackgroundColor(ContextCompat.getColor(this, R.color.md_purple_500));
                if (i == emotion)
                    btnColor.setBackgroundColor(ContextCompat.getColor(this, R.color.md_purple_200));
            } else if (emotionColorArray[i].equals(getString(R.string.color_disgust))) {
                btnColor.setBackgroundColor(ContextCompat.getColor(this, R.color.md_green_500));
                if (i == emotion)
                    btnColor.setBackgroundColor(ContextCompat.getColor(this, R.color.md_green_200));
            } else if (emotionColorArray[i].equals(getString(R.string.color_fear))) {
                btnColor.setBackgroundColor(ContextCompat.getColor(this, R.color.md_amber_500));
                if (i == emotion)
                    btnColor.setBackgroundColor(ContextCompat.getColor(this, R.color.md_amber_200));
            } else if (emotionColorArray[i].equals(getString(R.string.color_happy))) {
                btnColor.setBackgroundColor(ContextCompat.getColor(this, R.color.md_pink_500));
                if (i == emotion)
                    btnColor.setBackgroundColor(ContextCompat.getColor(this, R.color.md_pink_200));
            } else if (emotionColorArray[i].equals(getString(R.string.color_surprise))) {
                btnColor.setBackgroundColor(ContextCompat.getColor(this, R.color.md_orange_500));
                if (i == emotion)
                    btnColor.setBackgroundColor(ContextCompat.getColor(this, R.color.md_orange_200));
            } else if (emotionColorArray[i].equals(getString(R.string.color_neutral))) {
                btnColor.setBackgroundColor(ContextCompat.getColor(this, R.color.md_grey_500));
                if (i == emotion)
                    btnColor.setBackgroundColor(ContextCompat.getColor(this, R.color.md_grey_200));
            } else if (emotionColorArray[i].equals(getString(R.string.color_sad))) {
                btnColor.setBackgroundColor(ContextCompat.getColor(this, R.color.md_blue_500));
                if (i == emotion)
                    btnColor.setBackgroundColor(ContextCompat.getColor(this, R.color.md_blue_200));
            }
        }
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

    long prevProcessorTime = 0;

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
            flip(cameraFrame, cameraFrame, 1);
            Imgproc.cvtColor(cameraFrame, mCameraBuffer, Imgproc.COLOR_RGBA2BGR);
            flip(mCameraBuffer, mCameraBuffer, 1);

            final InferResult mInferResult = IntelliEyeCoreManager.getInstance().processFrame(mCameraBuffer, cameraFrame
                    , true);
            Log.d(TAG, "mInferResult is null:" + (mInferResult == null));
            if (System.currentTimeMillis() - prevProcessorTime > 2000) {
                prevProcessorTime = System.currentTimeMillis();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        emotionToBfop(mInferResult.getEmotionData());
//                    showRawData(mInferResult);
                    }
                });
            }
            Log.d(TAG, "total used time:" + (System.currentTimeMillis() - startTime));
            return cameraFrame;
        }
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

    private String emotionToBfop(float[] emotionData) {
        // nine emotion 0 angry 1 disgust 2 happy 3 sad 4 surprise 5 fear 6 neutral 7 contempt 8 confused
        // four emotion for edu 0 normal 1 passion 2 upset 3 confuse
        // nine -> four : 0,1,3,5,7 -> 2 ; 2 -> 1 ; 6 -> 0; 4,8 -> 3
        int firstAt = -1;
        int secondAt = 0;
        float max = 0;
        for (int i = 0; i < emotionData.length / 2; i++) {

            float emotion = emotionData[i];
            if (emotion > max) {
                max = emotion;
                firstAt = i;
            }
        }
        showBtnColorlight(firstAt);
        if (firstAt < 0)
            return "";
        String colorString = "{ \"text\": \"" + "卧室灯调成" + emotionColorArray[firstAt] + "\", \"customInfo\": { \"deviceId\": \"1\" } }";
        remoteService.sendControlMessage(colorString, new okhttp3.Callback() {
            @Override
            public void onFailure(okhttp3.Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), "網路不通", Toast.LENGTH_SHORT).show();
                    }
                });
                e.printStackTrace();
            }

            @Override
            public void onResponse(okhttp3.Call call, okhttp3.Response response) throws IOException {
                if (null != response.cacheResponse()) {
                    String str = response.cacheResponse().toString();

                    Log.i(TAG, "cache network---" + str);

                } else {
                    String str = response.body().string();
                    Log.i(TAG, "network---" + str);
                }

            }
        });


        return colorString;
    }
}
