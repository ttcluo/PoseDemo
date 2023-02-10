package cn.llnao.dtx.motion.media.hands;

import com.google.mediapipe.formats.proto.LandmarkProto;

import java.lang.ref.WeakReference;

/**
 * @ProjectName: DtxPoseMaster
 * @Package: cn.llnao.dtx.motion.media.hands
 * @ClassName: OnHandsDataOutputListener
 * @Description:
 * @Author: luochuan
 * @Email: ttcluo@163.com
 * @CreateDate: 2022/4/24 7:51 下午
 */
public interface OnHandsDataOutputListener {
    void handsOutput(int handsNum, WeakReference<Double[]> handsLeftArr, WeakReference<Double[]> handsRightArr);
    void fingerOutput(int handsNum, LandmarkProto.NormalizedLandmark fingerLeft, LandmarkProto.NormalizedLandmark fingerRight);
}
