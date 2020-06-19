package com.rokid.rokidfacesample.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.rokid.facelib.ImageRokidFace;
import com.rokid.facelib.api.IImageRokidFace;
import com.rokid.facelib.conf.DetectFaceConf;
import com.rokid.facelib.conf.RecogFaceConf;
import com.rokid.facelib.input.BitmapInput;
import com.rokid.facelib.model.FaceDO;
import com.rokid.facelib.model.FaceModel;
import com.rokid.facelib.utils.FaceLog;
import com.rokid.rokidfacesample.R;
import com.rokid.rokidfacesample.view.FaceModelView;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class ImageActivity extends Activity {
    private Button btn_sel;
    private TextView tv_text;
    IImageRokidFace imageFace;
    ImageView iv_bm;
    FaceModelView faceModelView;
    private static final String TAG = "ImageActivity";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.img_activity);
        iv_bm = findViewById(R.id.iv_bm);

        faceModelView = findViewById(R.id.faceModelView);
        imageFace = ImageRokidFace.create(getBaseContext());
        imageFace.dconfig(new DetectFaceConf());
        imageFace.sconfig(new RecogFaceConf().setRecog(true, "/sdcard/facesdk/"));
        btn_sel = findViewById(R.id.btn_sel);
        tv_text = findViewById(R.id.tv_text);
        btn_sel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");//相片类型
                startActivityForResult(intent, 0);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(data.getData()));
            FaceLog.d(TAG, "pic " + bitmap.getWidth() + " " + bitmap.getHeight());
            iv_bm.setImageBitmap(bitmap);

            imageFace.setImageFaceCallback(new BitmapInput(bitmap), model -> {
                FaceDO faceDO = model.getFaceList().get(0);
                StringBuilder sb = new StringBuilder();
                if(faceDO != null){
                    if (faceDO.featid != null) {
                        sb.append("faceDO.featid:" + faceDO.featid);
                    }
                    if (faceDO.pose != null) {
                        sb.append("\n pose:" + faceDO.pose[0] + "," + faceDO.pose[1] + "," + faceDO.pose[2] + "," + faceDO.pose[3]);
                    }
                    Rect rect = faceDO.toRect(bitmap.getWidth(), bitmap.getHeight());
                    if (rect != null) {
                        sb.append("\n rect:" + rect.left + "," + rect.top + "," + rect.right + "," + rect.bottom);
                    }
                    if(faceDO.quality!=0){
                        sb.append("\n quality:" + faceDO.quality);
                    }
                    sb.append("\n size:" + model.size());
                }
                faceModelView.setFaceModel(model);
                tv_text.setText(sb.toString());
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        imageFace.destroy();
    }
}
