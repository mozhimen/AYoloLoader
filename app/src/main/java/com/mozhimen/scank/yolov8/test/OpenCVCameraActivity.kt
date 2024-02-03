package com.mozhimen.scank.yolov8.test

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.PixelFormat
import android.os.Bundle
import android.util.Log
import android.view.SurfaceHolder
import android.view.View
import android.widget.AdapterView
import com.mozhimen.basick.elemk.android.view.ScreenKeepProxy
import com.mozhimen.basick.elemk.androidx.appcompat.bases.databinding.BaseActivityVB
import com.mozhimen.basick.lintk.optin.OptInApiCall_BindLifecycle
import com.mozhimen.basick.lintk.optin.OptInApiInit_ByLazy
import com.mozhimen.manifestk.xxpermissions.XXPermissionsRequestUtil
import com.mozhimen.scank.yolov8.Yolov8Ncnn
import com.mozhimen.scank.yolov8.test.databinding.ActivityOpencvCameraBinding

/**
 * @ClassName MainActivity
 * @Description TODO
 * @Author Mozhimen / Kolin Zhao
 * @Date 2024/2/3 17:22
 * @Version 1.0
 */

class OpenCVCameraActivity : BaseActivityVB<ActivityOpencvCameraBinding>(), SurfaceHolder.Callback {
    companion object {
        const val REQUEST_CAMERA = 100
    }

    private val yolov8ncnn: Yolov8Ncnn = Yolov8Ncnn()
    private var facing = 0 // backward camera

    private var current_model = 0 // yolov8n
    private var current_cpugpu = 0 // CPU

    @OptIn(OptInApiInit_ByLazy::class, OptInApiCall_BindLifecycle::class)
    private val _screenKeepProxy by lazy { ScreenKeepProxy(this) }

    @SuppressLint("MissingPermission")
    override fun initData(savedInstanceState: Bundle?) {
        XXPermissionsRequestUtil.requestCameraPermission(this, {
            super.initData(savedInstanceState)
        }, {

        })
    }

    /** Called when the activity is first created.  */

    @OptIn(OptInApiInit_ByLazy::class, OptInApiCall_BindLifecycle::class)
    override fun initView(savedInstanceState: Bundle?) {
        _screenKeepProxy.apply {
            bindLifecycle(this@OpenCVCameraActivity)
        }

        vb.cameraview.holder.setFormat(PixelFormat.RGBA_8888)
        vb.cameraview.holder.addCallback(this)

        vb.buttonSwitchCamera.setOnClickListener {
            val new_facing = 1 - facing
            yolov8ncnn.closeCamera()
            yolov8ncnn.openCamera(new_facing)
            facing = new_facing
        }
        vb.spinnerModel.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(arg0: AdapterView<*>?, arg1: View, position: Int, id: Long) {
                if (position != current_model) {
                    current_model = position
                    reload()
                }
            }

            override fun onNothingSelected(arg0: AdapterView<*>?) {}
        }
        vb.spinnerCPUGPU.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(arg0: AdapterView<*>?, arg1: View, position: Int, id: Long) {
                if (position != current_cpugpu) {
                    current_cpugpu = position
                    reload()
                }
            }

            override fun onNothingSelected(arg0: AdapterView<*>?) {}
        }
        reload()
    }

    private fun reload() {
        val ret_init: Boolean = yolov8ncnn.loadModel(assets, current_model, current_cpugpu)
        if (!ret_init) {
            Log.e(TAG, "yolov8ncnn loadModel failed")
        }
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        yolov8ncnn.setOutputWindow(holder.surface)
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
    }

    override fun onResume() {
        super.onResume()
        yolov8ncnn.openCamera(facing)
    }

    override fun onPause() {
        yolov8ncnn.closeCamera()
        super.onPause()
    }
}