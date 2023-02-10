package cn.llnao.dtx.motion.media.body.posedetector;

import com.google.mlkit.vision.pose.Pose;

/**
 * @ProjectName: android-taichi
 * @Package: cn.llnao.et.taichi.components.patients.train.present
 * @ClassName: OnPoseDataOutputListener
 * @Description:
 * @Author: luochuan
 * @Email: ttcluo@163.com
 * @CreateDate: 2022/3/3 2:50 下午
 */
public interface OnPoseDataOutputListener {
    void poseOutput(Pose pose, PoseGraphic poseGraphic);
}
