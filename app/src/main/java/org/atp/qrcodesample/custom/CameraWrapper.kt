package org.atp.qrcodesample.custom

import android.hardware.Camera
import java.lang.NullPointerException


class CameraWrapper private constructor(camera: Camera, cameraId: Int) {
    val mCamera: Camera
    val mCameraId: Int

    companion object {
        fun getWrapper(camera: Camera?, cameraId: Int): CameraWrapper? {
            return camera?.let { CameraWrapper(it, cameraId) }
        }
    }

    init {
        if (camera == null) {
            throw NullPointerException("Camera cannot be null")
        }
        mCamera = camera
        mCameraId = cameraId
    }
}