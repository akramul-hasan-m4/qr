package org.atp.qrcodesample.custom

import android.content.Context
import android.content.res.Configuration
import android.graphics.Point
import android.os.Build
import android.view.WindowManager


object DisplayUtils {
    fun getScreenResolution(context: Context): Point {
        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val display = wm.defaultDisplay
        val screenResolution = Point()
        if (Build.VERSION.SDK_INT >= 13) {
            display.getSize(screenResolution)
        } else {
            screenResolution[display.width] = display.height
        }
        return screenResolution
    }

    fun getScreenOrientation(context: Context): Int {
        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val display = wm.defaultDisplay
        var orientation = Configuration.ORIENTATION_UNDEFINED
        orientation = if (display.width == display.height) {
            Configuration.ORIENTATION_SQUARE
        } else {
            if (display.width < display.height) {
                Configuration.ORIENTATION_PORTRAIT
            } else {
                Configuration.ORIENTATION_LANDSCAPE
            }
        }
        return orientation
    }
}