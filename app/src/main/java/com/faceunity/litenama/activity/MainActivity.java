package com.faceunity.litenama.activity;

import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.faceunity.litenama.R;
import com.faceunity.litenama.gles.core.GlUtil;
import com.faceunity.litenama.render.CameraRenderer;
import com.faceunity.litenama.util.CameraUtils;
import com.faceunity.litenama.util.LifeCycleSensorManager;
import com.faceunity.nama.FURenderer;
import com.faceunity.nama.ui.BeautyParameterModel;
import com.faceunity.nama.ui.FaceUnityView;
import com.faceunity.nama.ui.ToastUtil;

import java.util.Locale;

/**
 * 相机页
 *
 * @author Richie on 2019.12.20
 */
public class MainActivity extends AppCompatActivity implements CameraRenderer.OnRendererStatusListener,
        View.OnClickListener, LifeCycleSensorManager.OnAccelerometerChangedListener, FURenderer.OnDebugListener,
        FURenderer.OnTrackStatusChangedListener, FURenderer.OnSystemErrorListener {
    private static final String TAG = "MainActivity";
    private CameraRenderer mCameraRenderer;
    private FURenderer mFuRenderer;
    private TextView mTvFps;
    private TextView mTvTrackStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setBackgroundDrawable(null);
        setContentView(R.layout.activity_main);

        GLSurfaceView glSurfaceView = findViewById(R.id.gl_surface);
        glSurfaceView.setEGLContextClientVersion(GlUtil.getSupportGLVersion(this));
        mCameraRenderer = new CameraRenderer(this, glSurfaceView, this);
        glSurfaceView.setRenderer(mCameraRenderer);
        glSurfaceView.setKeepScreenOn(true);
        glSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);

        findViewById(R.id.iv_switch_camera).setOnClickListener(this);
        mTvTrackStatus = findViewById(R.id.tv_track_status);
        mTvFps = findViewById(R.id.tv_fps);
        FaceUnityView faceUnityView = findViewById(R.id.fu_view);
        LinearLayout llSaveParams = findViewById(R.id.ll_save_params);
        boolean isOpenEffect = getIntent().getBooleanExtra(SplashActivity.OPEN_EFFECT, true);
        if (isOpenEffect) {
            int cameraFacing = FURenderer.CAMERA_FACING_FRONT;
            mFuRenderer = new FURenderer.Builder(this)
                    .setInputTextureType(FURenderer.INPUT_TEXTURE_EXTERNAL_OES)
                    .setCameraFacing(cameraFacing)
                    .setInputImageOrientation(CameraUtils.getCameraOrientation(cameraFacing))
                    .setRunBenchmark(true)
                    .setOnDebugListener(this)
                    .setOnTrackStatusChangedListener(this)
                    .setOnSystemErrorListener(this)
                    .build();
            faceUnityView.setModuleManager(mFuRenderer);
            LifeCycleSensorManager lifeCycleSensorManager = new LifeCycleSensorManager(this, getLifecycle());
            lifeCycleSensorManager.setOnAccelerometerChangedListener(this);
        } else {
            faceUnityView.setVisibility(View.GONE);
            llSaveParams.setVisibility(View.GONE);
        }
        llSaveParams.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BeautyParameterModel.saveParams();
                ToastUtil.showToast(MainActivity.this, "美颜参数保存成功");
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        mCameraRenderer.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mCameraRenderer.onPause();
    }

    @Override
    public void onSurfaceCreated() {
        if (mFuRenderer != null) {
            mFuRenderer.onSurfaceCreated();
        }
    }

    @Override
    public void onSurfaceChanged(int viewWidth, int viewHeight) {

    }

    @Override
    public int onDrawFrame(byte[] nv21Byte, int texId, int cameraWidth, int cameraHeight, float[] mvpMatrix, float[] texMatrix, long timeStamp) {
        if (mFuRenderer != null) {
            return mFuRenderer.onDrawFrameDualInput(nv21Byte, texId, cameraWidth, cameraHeight);
        }
        return 0;
    }

    @Override
    public void onSurfaceDestroy() {
        if (mFuRenderer != null) {
            mFuRenderer.onSurfaceDestroyed();
        }
    }

    @Override
    public void onCameraChanged(int cameraFacing, int cameraOrientation) {
        if (mFuRenderer != null) {
            mFuRenderer.onCameraChanged(cameraFacing, cameraOrientation);
            if (mFuRenderer.getMakeupModule() != null) {
                mFuRenderer.getMakeupModule().setIsMakeupFlipPoints(cameraFacing == FURenderer.CAMERA_FACING_BACK ? 1 : 0);
            }
        }
    }

    @Override
    public void onClick(View v) {
        mCameraRenderer.switchCamera();
    }

    @Override
    public void onAccelerometerChanged(float x, float y, float z) {
        if (Math.abs(x) > 3 || Math.abs(y) > 3) {
            if (Math.abs(x) > Math.abs(y)) {
                mFuRenderer.onDeviceOrientationChanged(x > 0 ? 0 : 180);
            } else {
                mFuRenderer.onDeviceOrientationChanged(y > 0 ? 90 : 270);
            }
        }
    }

    @Override
    public void onTrackStatusChanged(final int type, final int status) {
        Log.i(TAG, "onTrackStatusChanged() called with: type = [" + type + "], status = [" + status + "]");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mTvTrackStatus.setText(type == FURenderer.TRACK_TYPE_FACE ? R.string.toast_not_detect_face : R.string.toast_not_detect_body);
                mTvTrackStatus.setVisibility(status > 0 ? View.INVISIBLE : View.VISIBLE);
            }
        });
    }

    @Override
    public void onFpsChanged(final double fps, final double callTime) {
        Log.d(TAG, "onFpsChanged() called with: fps = [" + (int) fps + "], callTime = [" + String.format("%.2f", (float) callTime) + "]");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mTvFps.setText(String.format(Locale.getDefault(), "FPS: %d", (int) fps));
            }
        });
    }

    @Override
    public void onSystemError(int code, String message) {
        Log.w(TAG, "onSystemError code: " + code + ", message: " + message);
    }
}
