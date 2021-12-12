package org.atp.qrcodesample.custom

import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import org.atp.qrcodesample.custom.CameraUtils.getCameraInstance


// This code is mostly based on the top answer here: http://stackoverflow.com/questions/18149964/best-use-of-handlerthread-over-other-similar-classes
class CameraHandlerThread(private val mScannerView: BarcodeScannerView) :
    HandlerThread("CameraHandlerThread") {
    fun startCamera(cameraId: Int) {
        val localHandler = Handler(looper)
        localHandler.post {
            val camera = getCameraInstance(cameraId)
            val mainHandler = Handler(Looper.getMainLooper())
            mainHandler.post {
                mScannerView.setupCameraPreview(
                    CameraWrapper.getWrapper(
                        camera,
                        cameraId
                    )
                )
            }
        }
    }

    companion object {
        private const val LOG_TAG = "CameraHandlerThread"
    }

    init {
        start()
    }
}