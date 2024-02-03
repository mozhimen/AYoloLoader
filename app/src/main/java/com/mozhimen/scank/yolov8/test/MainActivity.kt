package com.mozhimen.scank.yolov8.test

import android.view.View
import com.mozhimen.basick.elemk.androidx.appcompat.bases.databinding.BaseActivityVB
import com.mozhimen.basick.utilk.android.content.startContext
import com.mozhimen.scank.yolov8.test.databinding.ActivityMainBinding

/**
 * @ClassName MainActivity
 * @Description TODO
 * @Author Mozhimen / Kolin Zhao
 * @Date 2024/2/3 21:25
 * @Version 1.0
 */
class MainActivity : BaseActivityVB<ActivityMainBinding>() {
    fun goOpenCVCamera(view: View) {
        startContext<OpenCVCameraActivity>()
    }

    fun goDetectBitmap(view: View) {
        startContext<DetectBitmapActivity>()
    }
}