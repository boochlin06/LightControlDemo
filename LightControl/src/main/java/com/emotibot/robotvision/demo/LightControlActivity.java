package com.emotibot.robotvision.demo;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

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
    @BindView(R.id.imgFirst)
    ImageView imgFirst;
    @BindView(R.id.txtFirst)
    TextView txtFirst;
    @BindView(R.id.imgSecond)
    ImageView imgSecond;
    @BindView(R.id.txtSecond)
    TextView txtSecond;
    @BindView(R.id.imgThird)
    ImageView imgThird;
    @BindView(R.id.txtThird)
    TextView txtThird;
    @BindView(R.id.llinEmotionNormal)
    LinearLayout llinEmotionNormal;
    @BindView(R.id.btnRightChange)
    Button btnRightChange;
    @BindView(R.id.relEmotionNormal)
    RelativeLayout relEmotionNormal;
    @BindView(R.id.btnLeftChange)
    Button btnLeftChange;
    @BindView(R.id.imgAngry)
    ImageView imgAngry;
    @BindView(R.id.txtAngry)
    TextView txtAngry;
    @BindView(R.id.imgDigust)
    ImageView imgDigust;
    @BindView(R.id.txtDigust)
    TextView txtDigust;
    @BindView(R.id.imgSad)
    ImageView imgSad;
    @BindView(R.id.txtSad)
    TextView txtSad;
    //    @BindView(R.id.llinEmotionLine1)
//    LinearLayout llinEmotionLine1;
    @BindView(R.id.imgConfuse)
    ImageView imgConfuse;
    @BindView(R.id.txtConfuse)
    TextView txtConfuse;
    @BindView(R.id.imgFear)
    ImageView imgFear;
    @BindView(R.id.txtFear)
    TextView txtFear;
    @BindView(R.id.imgSurprise)
    ImageView imgSurprise;
    @BindView(R.id.txtSurprise)
    TextView txtSurprise;
    //    @BindView(R.id.llinEmotionLine2)
//    LinearLayout llinEmotionLine2;
    @BindView(R.id.imgContempt)
    ImageView imgContempt;
    @BindView(R.id.txtContempt)
    TextView txtContempt;
    @BindView(R.id.imgHappy)
    ImageView imgHappy;
    @BindView(R.id.txtHappy)
    TextView txtHappy;
    @BindView(R.id.imgNeutral)
    ImageView imgNeutral;
    @BindView(R.id.txtNeutral)
    TextView txtNeutral;
    //    @BindView(R.id.llinEmotionLine3)
//    LinearLayout llinEmotionLine3;
    @BindView(R.id.relEmotionDetail)
    LinearLayout relEmotionDetail;
    @BindView(R.id.imgBulb)
    ImageButton imgBulb;
    @BindView(R.id.txtBulbSignal)
    TextView txtBulbSignal;
    @BindView(R.id.imgLogo)
    ImageView imgLogo;
    @BindView(R.id.relEmotionNone)
    RelativeLayout relEmotionNone;
    @BindView(R.id.camera_impl)
    JavaCameraView cameraImpl;
    @BindView(R.id.txtScoreFirst)
    TextView txtScoreFirst;
    @BindView(R.id.txtScoreSecond)
    TextView txtScoreSecond;
    @BindView(R.id.txtScoreThird)
    TextView txtScoreThird;
    @BindView(R.id.imgFourth)
    ImageView imgFourth;
    @BindView(R.id.txtFourth)
    TextView txtFourth;
    @BindView(R.id.txtScoreFourth)
    TextView txtScoreFourth;
    @BindView(R.id.imgFifth)
    ImageView imgFifth;
    @BindView(R.id.txtFifth)
    TextView txtFifth;
    @BindView(R.id.txtScoreFifth)
    TextView txtScoreFifth;
    @BindView(R.id.txtScoreAngry)
    TextView txtScoreAngry;

    @BindView(R.id.txtScoreConfuse)
    TextView txtScoreConfuse;

    @BindView(R.id.txtScoreContempt)
    TextView txtScoreContempt;
    @BindView(R.id.txtScoreDigust)
    TextView txtScoreDigust;
    @BindView(R.id.txtScoreFear)
    TextView txtScoreFear;
    @BindView(R.id.txtScoreHappy)
    TextView txtScoreHappy;
    @BindView(R.id.txtScoreSad)
    TextView txtScoreSad;
    @BindView(R.id.txtScoreSurprise)
    TextView txtScoreSurprise;
    @BindView(R.id.txtScoreNeutral)
    TextView txtScoreNeutral;
    @BindView(R.id.txtTitle)
    TextView txtTitle;

    private Mat mCameraBuffer = null;

    private StringBuilder stringBuilder = null;
    private String[] emotionColorArray;
    private RemoteLightControlService remoteLightControlService;

    public static final long LIGHT_CHANGE_GAP = 3000;

    private long prevProcessorTime = 0;
    private List<EmotionData> emotionDataList;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        this.setContentView(R.layout.activity_light_control);
        ButterKnife.bind(this);
        remoteLightControlService = RemoteLightControlService.getInstance();
        emotionColorArray = getResources().getStringArray(R.array.emotion_color_list);

        stringBuilder = new StringBuilder();
        mCameraView = (JavaCameraView) findViewById(R.id.camera_impl);
        emotionDataList = new ArrayList<>();

        initBtnColor();
        IntelliEyeCoreManager.getInstance().init(this, false, true, false, false, false);
        mCameraView.setCvCameraViewListener(new CameraFrameProcessor());
        showEmotionNormalView();
//        btnBlock.setVisibility(View.VISIBLE);
    }

    @OnClick({R.id.btnLeftChange, R.id.btnRightChange})
    public void onPageChangeClick(View view) {
        int id = view.getId();
        if (id == R.id.btnLeftChange) {
            showEmotionNormalView();

        } else if (id == R.id.btnRightChange) {
            showEmotionDetailView();
        }
    }

    private void showEmotionDetailView() {
        relEmotionDetail.setVisibility(View.VISIBLE);
        relEmotionNormal.setVisibility(View.INVISIBLE);
        relEmotionNone.setVisibility(View.INVISIBLE);
        btnRightChange.setVisibility(View.VISIBLE);
        btnLeftChange.setVisibility(View.VISIBLE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            btnRightChange.setBackground(getDrawable(R.drawable.right_active));
            btnRightChange.setTextColor(getColor(R.color.btn_actice_txt));
            btnLeftChange.setBackground(getDrawable(R.drawable.left_inactive));
            btnLeftChange.setTextColor(getColor(R.color.text_signal));

        }

    }

    private void showEmotionNormalView() {
        relEmotionDetail.setVisibility(View.INVISIBLE);
        relEmotionNormal.setVisibility(View.VISIBLE);
        relEmotionNone.setVisibility(View.INVISIBLE);
        btnRightChange.setVisibility(View.VISIBLE);
        btnLeftChange.setVisibility(View.VISIBLE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            btnLeftChange.setBackground(getDrawable(R.drawable.left_active));
            btnLeftChange.setTextColor(getColor(R.color.btn_actice_txt));
            btnRightChange.setBackground(getDrawable(R.drawable.right_inactive));
            btnRightChange.setTextColor(getColor(R.color.text_signal));

        }

    }

    private void showEmotionNoneView() {
        relEmotionDetail.setVisibility(View.INVISIBLE);
        relEmotionNormal.setVisibility(View.INVISIBLE);
        relEmotionNone.setVisibility(View.VISIBLE);
        btnRightChange.setVisibility(View.INVISIBLE);
        btnLeftChange.setVisibility(View.INVISIBLE);

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
        if (null != mCameraView) {
            mCameraView.disableView();
            mCameraView.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (null != mCameraView) {
            mCameraView.disableView();
        }
    }

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
            if (System.currentTimeMillis() - prevProcessorTime > LIGHT_CHANGE_GAP) {
                prevProcessorTime = System.currentTimeMillis();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        updateEmotionView(mInferResult.getEmotionData());
                    }
                });
            }
//            Log.d(TAG, "total process time:" + (System.currentTimeMillis() - startTime));
            return cameraFrame;
        }
    }

    class EmotionData implements Comparable<EmotionData> {

        private int viewId;
        private String textColor;
        private int index;
        private float value;
        private int imgColor;
        private int viewScoreId;

        public int getViewScoreId() {
            return viewScoreId;
        }

        public void setViewScoreId(int viewScoreId) {
            this.viewScoreId = viewScoreId;
        }


        public int getImgBulbColor() {
            return imgBulbColor;
        }

        public void setImgBulbColor(int imgBulbColor) {
            this.imgBulbColor = imgBulbColor;
        }

        private int imgBulbColor;
        private String textEmotion;

        public int getImgColor() {
            return imgColor;
        }

        public void setImgColor(int imgColor) {
            this.imgColor = imgColor;
        }


        public String getTextEmotion() {
            return textEmotion;
        }

        public void setTextEmotion(String textEmotion) {
            this.textEmotion = textEmotion;
        }


        public int getViewId() {
            return viewId;
        }

        public void setViewId(int viewId) {
            this.viewId = viewId;
        }

        public String getTextColor() {
            return textColor;
        }

        public void setTextColor(String text) {
            this.textColor = text;
        }

        public int getIndex() {
            return index;
        }

        public void setIndex(int index) {
            this.index = index;
        }

        public float getValue() {
            return value;
        }

        public void setValue(float value) {
            this.value = value;
        }

        @Override
        public int compareTo(EmotionData o) {
            if (this.getValue() > o.getValue()) {
                return -1;
            }
            if (this.getValue() < o.getValue()) {
                return 1;
            }
            return 0;
        }
    }

    private void updateEmotionView(float[] emotionData) {
        emotionDataList = new ArrayList<>();
        for (int i = 0; i < emotionData.length / 2; i++) {
            EmotionData data = new EmotionData();

            data.setTextColor(emotionColorArray[i]);
            data.setIndex(i);
            data.setValue(emotionData[i] * 100);

            switch (i) {
                case 0:
                    data.setViewId(R.id.txtAngry);
                    data.setViewScoreId(R.id.txtScoreAngry);
                    data.setTextEmotion(getString(R.string.emotion_angry));
                    data.setImgColor(R.drawable.group_0_angry);
                    data.setImgBulbColor(R.drawable.group_light_bulb_angry);
                    break;
                case 1:
                    data.setViewId(R.id.txtDigust);
                    data.setViewScoreId(R.id.txtScoreDigust);
                    data.setTextEmotion(getString(R.string.emotion_disgust));
                    data.setImgBulbColor(R.drawable.group_light_bulb_disgust);
                    data.setImgColor(R.drawable.group_1_disgust);
                    break;
                case 2:
                    data.setViewId(R.id.txtHappy);
                    data.setViewScoreId(R.id.txtScoreHappy);
                    data.setTextEmotion(getString(R.string.emotion_happy));
                    data.setImgBulbColor(R.drawable.group_light_bulb_happy);
                    data.setImgColor(R.drawable.group_2_happy);
                    break;
                case 3:
                    data.setViewId(R.id.txtSad);
                    data.setViewScoreId(R.id.txtScoreSad);
                    data.setTextEmotion(getString(R.string.emotion_sad));
                    data.setImgBulbColor(R.drawable.group_light_bulb_sad);
                    data.setImgColor(R.drawable.group_3_sad);
                    break;
                case 4:
                    data.setViewId(R.id.txtSurprise);
                    data.setViewScoreId(R.id.txtScoreSurprise);
                    data.setTextEmotion(getString(R.string.emotion_surprise));
                    data.setImgBulbColor(R.drawable.group_light_bulb_surprise);
                    data.setImgColor(R.drawable.group_4_surprise);
                    break;
                case 5:
                    data.setViewId(R.id.txtFear);
                    data.setViewScoreId(R.id.txtScoreFear);
                    data.setTextEmotion(getString(R.string.emotion_fear));
                    data.setImgBulbColor(R.drawable.group_light_bulb_fear);
                    data.setImgColor(R.drawable.group_5_fear);
                    break;
                case 6:
                    data.setViewId(R.id.txtNeutral);
                    data.setViewScoreId(R.id.txtScoreNeutral);
                    data.setTextEmotion(getString(R.string.emotion_neutral));
                    data.setImgBulbColor(R.drawable.group_light_bulb_neutral);
                    data.setImgColor(R.drawable.group_6_neutral);
                    break;
                case 7:
                    data.setViewId(R.id.txtContempt);
                    data.setViewScoreId(R.id.txtScoreContempt);
                    data.setTextEmotion(getString(R.string.emotion_contempt));
                    data.setImgColor(R.drawable.group_7_contempt);
                    data.setImgBulbColor(R.drawable.group_light_bulb_contempt);
                    break;
                case 8:
                    data.setViewId(R.id.txtConfuse);
                    data.setViewScoreId(R.id.txtScoreConfuse);
                    data.setTextEmotion(getString(R.string.emotion_confuse));
                    data.setImgColor(R.drawable.group_8_confused);
                    data.setImgBulbColor(R.drawable.group_light_bulb_confused);
                    break;

            }
            String value = String.format("% 4d", ((int) (data.getValue())));
            String s = data.getTextEmotion();
            ((TextView) findViewById(data.getViewId())).setText(s);
            ((TextView) findViewById(data.getViewScoreId())).setText(value + "%");
            emotionDataList.add(data);
        }
        Collections.sort(emotionDataList);

        for (int i = 0; i < 5; i++) {

            String s = emotionDataList.get(i).getTextEmotion();
            String value = ((int) (emotionDataList.get(i).getValue()) + "%");
            if (i == 0) {
                txtFirst.setText(s);
                txtScoreFirst.setText(value);
                imgFirst.setImageDrawable(ContextCompat.getDrawable(this, emotionDataList.get(i).getImgColor()));
            } else if (i == 1) {
                txtSecond.setText(s);
                txtScoreSecond.setText(value);
                imgSecond.setImageDrawable(ContextCompat.getDrawable(this, emotionDataList.get(i).getImgColor()));
            } else if (i == 2) {
                txtThird.setText(s);
                txtScoreThird.setText(value);
                imgThird.setImageDrawable(ContextCompat.getDrawable(this, emotionDataList.get(i).getImgColor()));
            } else if (i == 3) {
                txtFourth.setText(s);
                txtScoreFourth.setText(value);
                imgFourth.setImageDrawable(ContextCompat.getDrawable(this, emotionDataList.get(i).getImgColor()));
            } else {
                txtFifth.setText(s);
                txtScoreFifth.setText(value);
                imgFifth.setImageDrawable(ContextCompat.getDrawable(this, emotionDataList.get(i).getImgColor()));
            }
        }

        imgBulb.setBackground(ContextCompat.getDrawable(this, emotionDataList.get(0).getImgBulbColor()));
        if (emotionData[0] == 0 && emotionData[1] == 0) {
            txtBulbSignal.setText("OFF");
            imgBulb.setBackground(ContextCompat.getDrawable(this, R.drawable.group_light_bulb_transparent));
            showEmotionNoneView();
        } else {
            if (btnLeftChange.getVisibility() == View.INVISIBLE) {
                showEmotionNormalView();
            }
            txtBulbSignal.setText("ON");
        }
        emotionToControlService(emotionDataList.get(0).index);
    }


    private String emotionToControlService(int emotionIndex) {
        // nine emotion 0 angry 1 disgust 2 happy 3 sad 4 surprise 5 fear 6 neutral 7 contempt 8 confused
        String colorString = "{ \"text\": \"" + "客厅的灯调成" + emotionColorArray[emotionIndex] + "\", \"customInfo\": { \"deviceId\": \"1\" } }";
        Log.d(TAG, "colorString:" + colorString);
        remoteLightControlService.sendControlMessage(colorString, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), "網路不通", Toast.LENGTH_SHORT).show();
                    }
                });
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (null != response.cacheResponse()) {
                    String str = response.cacheResponse().toString();

                    Log.i(TAG, "cache network---" + str);

                } else {
                    String str = response.body().string();
//                    runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            Toast.makeText(getApplicationContext(), str, Toast.LENGTH_SHORT).show();
//                        }
//                    });
                    Log.d(TAG, "network---" + str);
                }

            }
        });
        return colorString;
    }


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
                    String colorString = "{ \"text\": \"" + "卧室灯调成" + emotionColorArray[finalI] + ", \"customInfo\": { \"deviceId\": \"1\" } }";
                    remoteLightControlService.sendControlMessage(colorString, new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getApplicationContext(), "網路不通", Toast.LENGTH_SHORT).show();
                                }
                            });
                            e.printStackTrace();
                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
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
}
