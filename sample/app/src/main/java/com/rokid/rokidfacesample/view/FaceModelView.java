package com.rokid.rokidfacesample.view;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.rokid.facelib.model.FaceDO;
import com.rokid.facelib.model.FaceModel;
import com.rokid.facelib.utils.FaceBitmapUtils;
import com.rokid.facelib.utils.FaceFileUtils;
import com.rokid.rokidfacesample.userdb.UserInfoDao;

import java.io.File;


public class FaceModelView extends View {
    private FaceModel faceModel;
    private Paint paint;
    private Paint textPaint;
    private UserInfoDao userInfoDao;
    private static final String STORE_PATH = "/sdcard/store/";

    public FaceModelView(Context context) {
        this(context,null);
    }

    public FaceModelView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }

    public FaceModelView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        textPaint = new Paint();
        textPaint.setStyle(Paint.Style.FILL);
        textPaint.setTextSize(50);
        textPaint.setColor(Color.parseColor("#EF5350"));
        paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(Color.rgb(239,83,80));
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setAntiAlias(true);

        File file = new File(STORE_PATH);
        if(!file.exists()){
            file.mkdirs();
        }
        ValueAnimator paintAnim = ValueAnimator.ofFloat(0f, 1f,0f);
        paintAnim.setDuration(1000);
        paintAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float f = (float) animation.getAnimatedValue();
                float width = paint.getStrokeWidth()+5*f;
                paint.setStrokeWidth(width);
            }
        });
        paintAnim.setRepeatCount(ValueAnimator.INFINITE);
        paintAnim.start();

    }

    public void setUserDao(UserInfoDao userInfoDao){
        this.userInfoDao = userInfoDao;
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        try {
            if(faceModel!=null&&faceModel.getFaceList()!=null&&faceModel.getFaceList().size()>0) {
                for (FaceDO faceDO : faceModel.getFaceList()) {
                    Rect rect = faceDO.toRect(getWidth(),getHeight());
                    initPaint(faceDO,rect);
                    //画矩形框
                    drawRountRect(canvas,rect);
                    if(faceDO.featid==null) {
                        //画loading动画
//                        drawLoading(canvas, rect);
                    }else{
                        //识别成功
                        drawResult(canvas,rect,faceDO);
                    }
                }
            }else{
                canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }





    private Paint paintResult;
    private int preFaceId = -1;
    private int times;
    private void drawResult(Canvas canvas, Rect rect, FaceDO face) {
        //做id去重
        if(face.trackId != preFaceId){
            FaceFileUtils.saveBitmap(face.recogBitmap,STORE_PATH+(times++)+".png");
            preFaceId = face.trackId;
        }

        canvas.save();
        canvas.translate((rect.left+rect.right)/2f,(rect.top+rect.bottom)/2f);
        if(paintResult == null) {
            paintResult = new Paint();
            paintResult.setColor(Color.rgb(243, 170, 60));
        }
        paintResult.setTextSize(rect.width()/4);
        String result = null;
        if(userInfoDao!=null){
            result = userInfoDao.getUserInfo(face.featid).name;
        }else{
            result = face.featid;
        }
        if(result.length()!=0){
            canvas.drawText(result.split("\\.")[0],-rect.width()/4,0,paintResult);
        }
        canvas.restore();
    }

    Paint paintLoading;
    private void drawLoading(Canvas canvas, Rect rect) {
        canvas.save();
        canvas.translate((rect.left+rect.right)/2f,(rect.top+rect.bottom)/2f);
        if(paintLoading == null) {
            paintLoading = new Paint();
            paintLoading.setColor(Color.rgb(243, 170, 60));
        }
        paintLoading.setTextSize(rect.width()/4);
        canvas.drawText("LOADING...",-rect.width()/2,0,paintLoading);
        canvas.restore();
    }

    public void setFaceModel(FaceModel faceModel){
        this.faceModel = faceModel;
        postInvalidate();
    }

    private void initPaint(FaceDO faceDO, Rect rect){
        paint.setStrokeWidth(rect.width()/30);
    }

    private void drawRountRect(Canvas canvas, Rect rect) {

        Log.i("drawRountRect","width:"+rect.width());
        canvas.save();

        canvas.translate((rect.left+rect.right)/2f, (rect.top+rect.bottom) / 2f);

        drawRect(canvas, 0,rect.width(),rect.height());
        drawRect(canvas, 90,rect.width(),rect.height());
        drawRect(canvas, 180,rect.width(),rect.height());
        drawRect(canvas, 270,rect.width(),rect.height());

        canvas.restore();
    }

    private void drawRect(Canvas canvas, int angle,int width,int height) {
        int line_width = width/10;
        int radius = width/20;

        canvas.save();
        canvas.rotate(angle);

        Path path = new Path();
        path.moveTo(-width / 2, height / 2 - line_width);
        path.lineTo(-width / 2, height / 2 - radius);

        RectF rectF = new RectF(-width / 2f, height / 2f - 2 * radius,
                -width / 2f + 2 * radius, height / 2f);

        path.addArc(rectF, 90, 90f);

        path.moveTo(-width / 2 + radius, height / 2);
        path.lineTo(-width / 2 + line_width, height / 2);
        canvas.drawPath(path, paint);

        canvas.restore();
    }


}
