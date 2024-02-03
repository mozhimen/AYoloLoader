package com.mozhimen.scank.yolov8.test

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.media.ExifInterface
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import com.mozhimen.basick.elemk.androidx.appcompat.bases.databinding.BaseActivityVB
import com.mozhimen.scank.yolov8.Yolov8Api
import com.mozhimen.scank.yolov8.test.databinding.ActivityDetectBitmapBinding
import java.io.FileNotFoundException
import java.io.IOException

/**
 * @ClassName DetectBitmapActivity
 * @Description TODO
 * @Author Mozhimen / Kolin Zhao
 * @Date 2024/2/3 21:32
 * @Version 1.0
 */
class DetectBitmapActivity : BaseActivityVB<ActivityDetectBitmapBinding>() {

    private val SELECT_IMAGE = 1

    private var bitmap: Bitmap? = null
    private var yourSelectedImage: Bitmap? = null

    private val yolov8api: Yolov8Api = Yolov8Api()

    override fun initView(savedInstanceState: Bundle?) {
        val ret_init: Boolean = yolov8api.init(assets, 0, 0)
        if (!ret_init) {
            Log.e("MainActivity", "yolov8ncnn Init failed")
        }
        vb.buttonImage.setOnClickListener {
            val i = Intent(Intent.ACTION_PICK)
            i.setType("image/*")
            startActivityForResult(i, SELECT_IMAGE)
        }
        vb.buttonDetect.setOnClickListener(View.OnClickListener {
            if (yourSelectedImage == null) {
                return@OnClickListener
            }

            val objects: Array<Yolov8Api.Obj> = yolov8api.detect(yourSelectedImage)
            showObjects(objects)
        })

        // GPU检测效果不好
        vb.buttonDetectGPU.setOnClickListener(View.OnClickListener {
            if (yourSelectedImage == null) {
                return@OnClickListener
            }

            val objects: Array<Yolov8Api.Obj> = yolov8api.detect(yourSelectedImage)
            showObjects(objects)
        })
    }

    private fun showObjects(objects: Array<Yolov8Api.Obj>?) {
        if (objects == null) {
            vb.imageView.setImageBitmap(bitmap)
            return
        }

        // draw objects on bitmap
        val rgba = bitmap!!.copy(Bitmap.Config.ARGB_8888, true)
        val colors = intArrayOf(
            Color.rgb(54, 67, 244),
            Color.rgb(99, 30, 233),
            Color.rgb(176, 39, 156),
            Color.rgb(183, 58, 103),
            Color.rgb(181, 81, 63),
            Color.rgb(243, 150, 33),
            Color.rgb(244, 169, 3),
            Color.rgb(212, 188, 0),
            Color.rgb(136, 150, 0),
            Color.rgb(80, 175, 76),
            Color.rgb(74, 195, 139),
            Color.rgb(57, 220, 205),
            Color.rgb(59, 235, 255),
            Color.rgb(7, 193, 255),
            Color.rgb(0, 152, 255),
            Color.rgb(34, 87, 255),
            Color.rgb(72, 85, 121),
            Color.rgb(158, 158, 158),
            Color.rgb(139, 125, 96)
        )
        val canvas = Canvas(rgba)
        val paint = Paint()
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 4f
        val textbgpaint = Paint()
        textbgpaint.color = Color.WHITE
        textbgpaint.style = Paint.Style.FILL
        val textpaint = Paint()
        textpaint.color = Color.BLACK
        textpaint.textSize = 26f
        textpaint.textAlign = Paint.Align.LEFT
        for (i in objects.indices) {
            paint.color = colors[i % 19]
            canvas.drawRect(objects[i].x, objects[i].y, objects[i].x + objects[i].w, objects[i].y + objects[i].h, paint)

            // draw filled text inside image
            run {
                val text: String = objects[i].label.toString() + " = " + java.lang.String.format("%.1f", objects[i].prob * 100) + "%"
                val text_width = textpaint.measureText(text)
                val text_height = -textpaint.ascent() + textpaint.descent()
                var x: Float = objects[i].x
                var y: Float = objects[i].y - text_height
                if (y < 0) {
                    y = 0f
                }
                if (x + text_width > rgba.width) {
                    x = rgba.width - text_width
                }
                canvas.drawRect(x, y, x + text_width, y + text_height, textbgpaint)
                canvas.drawText(text, x, y - textpaint.ascent(), textpaint)
            }
        }
        vb.imageView.setImageBitmap(rgba)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && null != data) {
            val selectedImage = data.data
            try {
                if (requestCode == SELECT_IMAGE) {
                    bitmap = decodeUri(selectedImage)
                    yourSelectedImage = bitmap!!.copy(Bitmap.Config.ARGB_8888, true)
                    vb.imageView.setImageBitmap(bitmap)
                }
            } catch (e: FileNotFoundException) {
                Log.e("MainActivity", "FileNotFoundException")
                return
            }
        }
    }

    @Throws(FileNotFoundException::class)
    private fun decodeUri(selectedImage: Uri?): Bitmap? {
        // Decode image size
        val o = BitmapFactory.Options()
        o.inJustDecodeBounds = true
        BitmapFactory.decodeStream(contentResolver.openInputStream(selectedImage!!), null, o)

        // The new size we want to scale to
        val REQUIRED_SIZE = 640

        // Find the correct scale value. It should be the power of 2.
        var width_tmp = o.outWidth
        var height_tmp = o.outHeight
        var scale = 1
        while (true) {
            if (width_tmp / 2 < REQUIRED_SIZE
                || height_tmp / 2 < REQUIRED_SIZE
            ) {
                break
            }
            width_tmp /= 2
            height_tmp /= 2
            scale *= 2
        }

        // Decode with inSampleSize
        val o2 = BitmapFactory.Options()
        o2.inSampleSize = scale
        val bitmap = BitmapFactory.decodeStream(contentResolver.openInputStream(selectedImage), null, o2)

        // Rotate according to EXIF
        var rotate = 0
        try {
            val exif = ExifInterface(contentResolver.openInputStream(selectedImage)!!)
            val orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
            when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_270 -> rotate = 270
                ExifInterface.ORIENTATION_ROTATE_180 -> rotate = 180
                ExifInterface.ORIENTATION_ROTATE_90 -> rotate = 90
            }
        } catch (e: IOException) {
            Log.e("MainActivity", "ExifInterface IOException")
        }
        val matrix = Matrix()
        matrix.postRotate(rotate.toFloat())
        return Bitmap.createBitmap(bitmap!!, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }
}