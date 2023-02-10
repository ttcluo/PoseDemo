package cn.llnao.dtx.posedemo;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.mlkit.vision.pose.Pose;
import com.google.mlkit.vision.pose.PoseDetectorOptionsBase;
import com.google.mlkit.vision.pose.PoseLandmark;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import cn.llnao.dtx.motion.media.body.CameraSource;
import cn.llnao.dtx.motion.media.body.CameraSourcePreview;
import cn.llnao.dtx.motion.media.body.GraphicOverlay;
import cn.llnao.dtx.motion.media.body.posedetector.OnPoseDataOutputListener;
import cn.llnao.dtx.motion.media.body.posedetector.PoseDetectorProcessor;
import cn.llnao.dtx.motion.media.body.posedetector.PoseGraphic;
import cn.llnao.dtx.motion.media.body.preference.PreferenceUtils;

public class MainActivity extends AppCompatActivity implements ActivityCompat.OnRequestPermissionsResultCallback, OnPoseDataOutputListener {

    private static final String TAG = "MainActivity";
    private static final int PERMISSION_REQUESTS = 1;
    private CameraSource cameraSource = null;
    CameraSourcePreview preview;
    GraphicOverlay graphicOverlay;
    private boolean onCreate = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //        preview = new CameraSourcePreview(this,null);
        preview = findViewById(cn.llnao.dtx.motion.R.id.preview_view);
//        graphicOverlay = new GraphicOverlay(this,null);
        graphicOverlay = findViewById(cn.llnao.dtx.motion.R.id.graphic_overlay);
        preview.setKeepScreenOn(true);

    }

    @Override
    protected void onResume() {
        super.onResume();
        onMediaResume();
    }

    public void onMediaResume() {
        if (!onCreate) {
            if (allPermissionsGranted()) {
                createCameraSource();
            } else {
                getRuntimePermissions();
            }
        } else {
            startCameraSource();
        }

    }

    /**
     * Stops the camera.
     */
    @Override
    protected void onPause() {
        super.onPause();
        if (preview != null)
            preview.stop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (cameraSource != null) {
            cameraSource.release();
        }
    }

    public void onCameraClose() {
        if (cameraSource != null) {
            cameraSource.release();
        }
    }

    private void createCameraSource() {
        // If there's no existing cameraSource, create one.
        if (cameraSource == null) {
            cameraSource = new CameraSource(this, graphicOverlay);
            onCreate = true;
        }
        try {
            PoseDetectorOptionsBase poseDetectorOptions =
                    PreferenceUtils.getPoseDetectorOptionsForLivePreview(this);

            Log.i(TAG, "Using Pose Detector with options " + poseDetectorOptions);
            boolean shouldShowInFrameLikelihood =
                    PreferenceUtils.shouldShowPoseDetectionInFrameLikelihoodLivePreview(this);
            boolean visualizeZ = PreferenceUtils.shouldPoseDetectionVisualizeZ(this);
            boolean rescaleZ = PreferenceUtils.shouldPoseDetectionRescaleZForVisualization(this);
            boolean runClassification = PreferenceUtils.shouldPoseDetectionRunClassification(this);

            cameraSource.setMachineLearningFrameProcessor(
                    new PoseDetectorProcessor(
                            this,
                            poseDetectorOptions,
                            false,
                            false,
                            false,
                            false,
                            /* isStreamMode = */ true,
                            this));
//            /*shouldShowInFrameLikelihood */true,
//                    /*visualizeZ */true,
//                    /*rescaleZ */true,
//                    /*runClassification */true,
//                    /* isStreamMode = */ true,
//                    this));

            startCameraSource();
        } catch (RuntimeException e) {
            Toast.makeText(
                    getApplicationContext(),
                    "Can not create image processor: " + e.getMessage(),
                    Toast.LENGTH_LONG)
                    .show();
        }

    }

    /**
     * Starts or restarts the camera source, if it exists. If the camera source doesn't exist yet
     * (e.g., because onResume was called before the camera source was created), this will be called
     * again when the camera source is created.
     */
    public void startCameraSource() {
        if (cameraSource != null) {
            try {
                if (preview == null) {
                    Log.d(TAG, "resume: Preview is null");
                }
                if (graphicOverlay == null) {
                    Log.d(TAG, "resume: graphOverlay is null");
                }
                preview.start(cameraSource, graphicOverlay);
//                getCameraCharacteristics(getContext(),1);
            } catch (IOException e) {
                Log.e(TAG, "Unable to start camera source.", e);
                cameraSource.release();
                cameraSource = null;
            }
        }
    }


    private String[] getRequiredPermissions() {
        try {
            PackageInfo info =
                    this.getPackageManager()
                            .getPackageInfo(this.getPackageName(), PackageManager.GET_PERMISSIONS);
//            String[] ps = info.requestedPermissions;
            String[] ps = new String[2];
            ps[0] = "android.permission.CAMERA";
            ps[1] = "android.permission.READ_EXTERNAL_STORAGE";
            if (ps != null && ps.length > 0) {
                return ps;
            } else {
                return new String[0];
            }
        } catch (Exception e) {
            return new String[0];
        }
    }

    private boolean allPermissionsGranted() {
        for (String permission : getRequiredPermissions()) {
            if (!isPermissionGranted(this, permission)) {
                return false;
            }
        }
        return true;
    }

    private void getRuntimePermissions() {
        List<String> allNeededPermissions = new ArrayList<>();
        for (String permission : getRequiredPermissions()) {
            if (!isPermissionGranted(this, permission)) {
                allNeededPermissions.add(permission);
            }
        }

        if (!allNeededPermissions.isEmpty()) {
            ActivityCompat.requestPermissions(
                    this, allNeededPermissions.toArray(new String[0]), PERMISSION_REQUESTS);
        }
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, String[] permissions, int[] grantResults) {
        Log.i(TAG, "Permission granted!");
        if (allPermissionsGranted()) {
            createCameraSource();
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private static boolean isPermissionGranted(Context context, String permission) {
        if (ContextCompat.checkSelfPermission(context, permission)
                == PackageManager.PERMISSION_GRANTED) {
            Log.i(TAG, "Permission granted: " + permission);
            return true;
        }
        Log.i(TAG, "Permission NOT granted: " + permission);
        return false;
    }

    protected void hideBottomUIMenu() {
//隐藏虚拟按键，并且全屏
        if (Build.VERSION.SDK_INT > 11 && Build.VERSION.SDK_INT < 19) { // lower api
            View v = this.getWindow().getDecorView();
            v.setSystemUiVisibility(View.GONE);
        } else if (Build.VERSION.SDK_INT >= 19) {
//for new api versions.
            View decorView = getWindow().getDecorView();
            int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_FULLSCREEN;
            decorView.setSystemUiVisibility(uiOptions);
        }

    }

    @Override
    public void poseOutput(Pose pose, PoseGraphic poseGraphic) {
        PoseLandmark leftWrist = pose.getPoseLandmark(PoseLandmark.LEFT_WRIST);
        PoseLandmark rightWrist = pose.getPoseLandmark(PoseLandmark.RIGHT_WRIST);
        if (leftWrist == null || rightWrist == null) {
            return;
        }

//        float x1= BigDecimal.valueOf(poseGraphic.translateX(leftWrist.getPosition3D().getX())).floatValue() / 1920;
//        float y1=BigDecimal.valueOf(poseGraphic.translateY(leftWrist.getPosition3D().getY())).floatValue() / 1200;
//
//        String out = "左手腕 X:" + BigDecimal.valueOf(poseGraphic.translateX(leftWrist.getPosition3D().getX())).floatValue() / 1920
//                + "\\n            Y:" + BigDecimal.valueOf(poseGraphic.translateY(leftWrist.getPosition3D().getY())).floatValue() / 1200
//                + "\\n            可信度:" + BigDecimal.valueOf(leftWrist.getInFrameLikelihood()).setScale(2, 4).floatValue()
//                + "\\n右手腕 X:" + BigDecimal.valueOf(poseGraphic.translateX(rightWrist.getPosition3D().getX())).floatValue() / 1920
//                + "\\n            Y:" + BigDecimal.valueOf(poseGraphic.translateY(rightWrist.getPosition3D().getY())).floatValue() / 1200
//                + "\\n            可信度:" + BigDecimal.valueOf(rightWrist.getInFrameLikelihood()).setScale(2, 4).floatValue();
//
//
//        StringBuffer sb = new StringBuffer();
//        for (PoseLandmark poseLandmark : pose.getAllPoseLandmarks()) {
//            sb.append("[" + (double) poseLandmark.getPosition3D().getX() + ",");
//            sb.append((double) poseLandmark.getPosition3D().getY() + ",");
//            sb.append((double) poseLandmark.getPosition3D().getZ() + "]");
//        }
//        Log.e("p", sb.toString());
    }
}