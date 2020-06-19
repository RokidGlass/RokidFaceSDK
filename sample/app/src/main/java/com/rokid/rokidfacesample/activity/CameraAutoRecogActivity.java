package com.rokid.rokidfacesample.activity;

import android.app.Activity;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.transition.Explode;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.rokid.camerakit.cameralibrary.view.DefaultCameraView;
import com.rokid.facelib.VideoRokidFace;
import com.rokid.facelib.api.IVideoRokidFace;
import com.rokid.facelib.conf.DetectFaceConf;
import com.rokid.facelib.conf.RecogFaceConf;
import com.rokid.facelib.conf.VideoDetectFaceConf;
import com.rokid.facelib.face.FaceDbHelper;
import com.rokid.facelib.input.VideoInput;
import com.rokid.facelib.model.FaceSize;
import com.rokid.facelib.utils.FaceLog;
import com.rokid.facelib.utils.FaceRectUtils;
import com.rokid.rokidfacesample.R;
import com.rokid.rokidfacesample.gles.CameraFrameRect;
import com.rokid.rokidfacesample.userdb.UserDatabase;
import com.rokid.rokidfacesample.userdb.UserInfoDao;
import com.rokid.rokidfacesample.view.FaceModelView;

import java.io.File;

/**
 * 相机人脸跟踪+检测+识别
 * 识别过程自动集成在跟踪和检测的流程中
 */
public class CameraAutoRecogActivity extends Activity {

    private static final String TAG = CameraAutoRecogActivity.class.getSimpleName();

    private static final int WIDTH_720P = 1280;
    private static final int HEIGHT_720P = 720;

    private static final int WIDTH_1080P = 1920;
    private static final int HEIGHT_1080P = 1080;

    private int PREVIEW_WIDTH = WIDTH_720P;
    private int PREVIEW_HEIGHT = HEIGHT_720P;

    DefaultCameraView cameraView;
    IVideoRokidFace videoFace;

    boolean stop;
    long crrentTime;
    Button btn_switch_recog;
    private RecogFaceConf sFaceConf;
    private DetectFaceConf dFaceConf;
    boolean recog;
    private FaceModelView faceModelView;
    private UserInfoDao userDao;
    private UserDatabase userDatabase;
    private static final String PATH = FaceDbHelper.PATH_OUTPUT;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        crrentTime = SystemClock.elapsedRealtime();
        getWindow().setEnterTransition(new Explode());
        setContentView(R.layout.activity_camera);
        initView();
        init();
        initUserDb();
    }

    /**
     * 初始化人脸信息数据库
     */
    private void initUserDb() {
        userDatabase = UserDatabase.create(this, "user.db");
        userDao = userDatabase.getUserInfoDao();
        faceModelView.setUserDao(userDao);
    }

    private void init() {
        faceTrackRecog();
        initCam();
    }

    private void initView() {
        faceModelView = findViewById(R.id.faceModelView);
        btn_switch_recog = findViewById(R.id.btn_switch_recog);
        cameraView = findViewById(R.id.cameraview);
        sFaceConf = new RecogFaceConf().setRecog(false, PATH);

        btn_switch_recog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recog = !recog;
                File file1 = new File(PATH+"SearchEngine.bin");
                if(recog&&!file1.exists()){
                    return;
                }
                sFaceConf = new RecogFaceConf().setRecog(recog, PATH);
                videoFace.sconfig(sFaceConf);
                if(!recog){
                    btn_switch_recog.setText("开启识别");
                }else{
                    btn_switch_recog.setText("关闭识别识别");
                }
            }
        });
    }

    /**
     * 初始化人脸识别SDK
     */
    private void faceTrackRecog() {
        //设置输入数据的宽高
        dFaceConf = new VideoDetectFaceConf().setSize(PREVIEW_WIDTH, PREVIEW_HEIGHT);
        //设置人脸识别的区域为整个CameraPreview的区域
        dFaceConf.setRoi(new Rect(0,0,PREVIEW_WIDTH,PREVIEW_HEIGHT));
        videoFace = VideoRokidFace.create(getBaseContext(),dFaceConf);
        videoFace.sconfig(sFaceConf);
        videoFace.startTrack(model -> {
            Log.i(TAG,"model:"+model.toString());
            //用于显示返回的人脸model
            faceModelView.setFaceModel(model);
        });
    }

    /**
     * 处理相机返回数据
     */
    private void initCam() {
        cameraView.addPreviewCallBack((bytes, camera) -> {
            //将相机数据放入SDK
            if (videoFace != null&&!stop) {
                videoFace.setData(new VideoInput(bytes));
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        cameraView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        cameraView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (videoFace != null) {
            videoFace.destroy();
        }
        cameraView.onDestroy();
    }
}
