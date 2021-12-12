package org.atp.qrcodesample.custom

import android.content.Context
import android.content.res.Configuration
import android.graphics.Point
import android.hardware.Camera.*
import android.os.Handler
import android.util.AttributeSet
import android.util.Log
import android.view.*
import java.lang.Exception
import java.lang.RuntimeException


class CameraPreview : SurfaceView, SurfaceHolder.Callback {
    private var mCameraWrapper: CameraWrapper? = null
    private var mAutoFocusHandler: Handler? = null
    private var mPreviewing = true
    private var mAutoFocus = true
    private var mSurfaceCreated = false
    private var mShouldScaleToFill = true
    private var mPreviewCallback: PreviewCallback? = null
    private var mAspectTolerance = 0.1f

    constructor(
        context: Context?,
        cameraWrapper: CameraWrapper?,
        previewCallback: PreviewCallback?
    ) : super(context) {
        init(cameraWrapper, previewCallback)
    }

    constructor(
        context: Context?,
        attrs: AttributeSet?,
        cameraWrapper: CameraWrapper?,
        previewCallback: PreviewCallback?
    ) : super(context, attrs) {
        init(cameraWrapper, previewCallback)
    }

    fun init(cameraWrapper: CameraWrapper?, previewCallback: PreviewCallback?) {
        setCamera(cameraWrapper, previewCallback)
        mAutoFocusHandler = Handler()
        holder.addCallback(this)
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS)
    }

    fun setCamera(cameraWrapper: CameraWrapper?, previewCallback: PreviewCallback?) {
        mCameraWrapper = cameraWrapper
        mPreviewCallback = previewCallback
    }

    fun setShouldScaleToFill(scaleToFill: Boolean) {
        mShouldScaleToFill = scaleToFill
    }

    fun setAspectTolerance(aspectTolerance: Float) {
        mAspectTolerance = aspectTolerance
    }

    override fun surfaceCreated(surfaceHolder: SurfaceHolder) {
        mSurfaceCreated = true
    }

    override fun surfaceChanged(surfaceHolder: SurfaceHolder, i: Int, i2: Int, i3: Int) {
        if (surfaceHolder.surface == null) {
            return
        }
        stopCameraPreview()
        showCameraPreview()
    }

    override fun surfaceDestroyed(surfaceHolder: SurfaceHolder) {
        mSurfaceCreated = false
        stopCameraPreview()
    }

    fun showCameraPreview() {
        if (mCameraWrapper != null) {
            try {
                holder.addCallback(this)
                mPreviewing = true
                setupCameraParameters()
                mCameraWrapper!!.mCamera.setPreviewDisplay(holder)
                mCameraWrapper!!.mCamera.setDisplayOrientation(displayOrientation)
                mCameraWrapper!!.mCamera.setOneShotPreviewCallback(mPreviewCallback)
                mCameraWrapper!!.mCamera.startPreview()
                if (mAutoFocus) {
                    if (mSurfaceCreated) { // check if surface created before using autofocus
                        safeAutoFocus()
                    } else {
                        scheduleAutoFocus() // wait 1 sec and then do check again
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, e.toString(), e)
            }
        }
    }

    fun safeAutoFocus() {
        try {
            mCameraWrapper!!.mCamera.autoFocus(autoFocusCB)
        } catch (re: RuntimeException) {
            // Horrible hack to deal with autofocus errors on Sony devices
            // See https://github.com/dm77/barcodescanner/issues/7 for example
            scheduleAutoFocus() // wait 1 sec and then do check again
        }
    }

    fun stopCameraPreview() {
        if (mCameraWrapper != null) {
            try {
                mPreviewing = false
                holder.removeCallback(this)
                mCameraWrapper!!.mCamera.cancelAutoFocus()
                mCameraWrapper!!.mCamera.setOneShotPreviewCallback(null)
                mCameraWrapper!!.mCamera.stopPreview()
            } catch (e: Exception) {
                Log.e(TAG, e.toString(), e)
            }
        }
    }

    fun setupCameraParameters() {
        val optimalSize = optimalPreviewSize
        val parameters = mCameraWrapper!!.mCamera.parameters
        parameters.setPreviewSize(optimalSize!!.width, optimalSize.height)
        mCameraWrapper!!.mCamera.parameters = parameters
        adjustViewSize(optimalSize)
    }

    private fun adjustViewSize(cameraSize: Size?) {
        val previewSize = convertSizeToLandscapeOrientation(
            Point(
                width, height
            )
        )
        val cameraRatio = cameraSize!!.width.toFloat() / cameraSize.height
        val screenRatio = previewSize.x.toFloat() / previewSize.y
        if (screenRatio > cameraRatio) {
            setViewSize((previewSize.y * cameraRatio).toInt(), previewSize.y)
        } else {
            setViewSize(previewSize.x, (previewSize.x / cameraRatio).toInt())
        }
    }

    private fun convertSizeToLandscapeOrientation(size: Point): Point {
        return if (displayOrientation % 180 == 0) {
            size
        } else {
            Point(size.y, size.x)
        }
    }

    private fun setViewSize(width: Int, height: Int) {
        val layoutParams = layoutParams
        var tmpWidth: Int
        var tmpHeight: Int
        if (displayOrientation % 180 == 0) {
            tmpWidth = width
            tmpHeight = height
        } else {
            tmpWidth = height
            tmpHeight = width
        }
        if (mShouldScaleToFill) {
            val parentWidth = (parent as View).width
            val parentHeight = (parent as View).height
            val ratioWidth = parentWidth.toFloat() / tmpWidth.toFloat()
            val ratioHeight = parentHeight.toFloat() / tmpHeight.toFloat()
            val compensation: Float
            compensation = if (ratioWidth > ratioHeight) {
                ratioWidth
            } else {
                ratioHeight
            }
            tmpWidth = Math.round(tmpWidth * compensation)
            tmpHeight = Math.round(tmpHeight * compensation)
        }
        layoutParams.width = tmpWidth
        layoutParams.height = tmpHeight
        setLayoutParams(layoutParams)
    }// back-facing// compensate the mirror

    //If we don't have a camera set there is no orientation so return dummy value
    val displayOrientation: Int
        get() {
            if (mCameraWrapper == null) {
                //If we don't have a camera set there is no orientation so return dummy value
                return 0
            }
            val info = CameraInfo()
            if (mCameraWrapper!!.mCameraId == -1) {
                getCameraInfo(CameraInfo.CAMERA_FACING_BACK, info)
            } else {
                getCameraInfo(mCameraWrapper!!.mCameraId, info)
            }
            val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            val display = wm.defaultDisplay
            val rotation = display.rotation
            var degrees = 0
            when (rotation) {
                Surface.ROTATION_0 -> degrees = 0
                Surface.ROTATION_90 -> degrees = 90
                Surface.ROTATION_180 -> degrees = 180
                Surface.ROTATION_270 -> degrees = 270
            }
            var result: Int
            if (info.facing == CameraInfo.CAMERA_FACING_FRONT) {
                result = (info.orientation + degrees) % 360
                result = (360 - result) % 360 // compensate the mirror
            } else {  // back-facing
                result = (info.orientation - degrees + 360) % 360
            }
            return result
        }

    // Try to find an size match aspect ratio and size

    // Cannot find the one match the aspect ratio, ignore the requirement
    private val optimalPreviewSize: Size?
        private get() {
            if (mCameraWrapper == null) {
                return null
            }
            val sizes = mCameraWrapper!!.mCamera.parameters.supportedPreviewSizes
            var w = width
            var h = height
            if (DisplayUtils.getScreenOrientation(context) === Configuration.ORIENTATION_PORTRAIT) {
                val portraitWidth = h
                h = w
                w = portraitWidth
            }
            val targetRatio = w.toDouble() / h
            if (sizes == null) return null
            var optimalSize: Size? = null
            var minDiff = Double.MAX_VALUE
            val targetHeight = h

            // Try to find an size match aspect ratio and size
            for (size in sizes) {
                val ratio = size.width.toDouble() / size.height
                if (Math.abs(ratio - targetRatio) > mAspectTolerance) continue
                if (Math.abs(size.height - targetHeight) < minDiff) {
                    optimalSize = size
                    minDiff = Math.abs(size.height - targetHeight).toDouble()
                }
            }

            // Cannot find the one match the aspect ratio, ignore the requirement
            if (optimalSize == null) {
                minDiff = Double.MAX_VALUE
                for (size in sizes) {
                    if (Math.abs(size.height - targetHeight) < minDiff) {
                        optimalSize = size
                        minDiff = Math.abs(size.height - targetHeight).toDouble()
                    }
                }
            }
            return optimalSize
        }

    fun setAutoFocus(state: Boolean) {
        if (mCameraWrapper != null && mPreviewing) {
            if (state == mAutoFocus) {
                return
            }
            mAutoFocus = state
            if (mAutoFocus) {
                if (mSurfaceCreated) { // check if surface created before using autofocus
                    Log.v(TAG, "Starting autofocus")
                    safeAutoFocus()
                } else {
                    scheduleAutoFocus() // wait 1 sec and then do check again
                }
            } else {
                Log.v(TAG, "Cancelling autofocus")
                mCameraWrapper!!.mCamera.cancelAutoFocus()
            }
        }
    }

    private val doAutoFocus = Runnable {
        if (mCameraWrapper != null && mPreviewing && mAutoFocus && mSurfaceCreated) {
            safeAutoFocus()
        }
    }

    // Mimic continuous auto-focusing
    var autoFocusCB =
        AutoFocusCallback { success, camera -> scheduleAutoFocus() }

    private fun scheduleAutoFocus() {
        mAutoFocusHandler!!.postDelayed(doAutoFocus, 1000)
    }

    companion object {
        private const val TAG = "CameraPreview"
    }
}