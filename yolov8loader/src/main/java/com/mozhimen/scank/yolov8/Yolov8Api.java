package com.mozhimen.scank.yolov8;

import android.content.res.AssetManager;
import android.graphics.Bitmap;

import com.mozhimen.kotlin.lintk.optins.OApiInit_ByLazy;


/**
 * @ClassName Yolov8Api
 * @Description TODO
 * @Author Mozhimen / Kolin Zhao
 * @Date 2024/2/3 17:33
 * @Version 1.0
 */
@OApiInit_ByLazy
public class Yolov8Api {
    public native boolean init(AssetManager mgr, int modelid, int cpugpu);

    public native Obj[] detect(Bitmap bitmap);

    // 释放资源，防止OOM，释放后需要重新Init
    public native void release();

    static {
        System.loadLibrary("yolov8ncnn"); // load the library of yolov8ncnn
    }

    public class Obj
    {
        public float x;
        public float y;
        public float w;
        public float h;
        public int label;
        public float prob;

        @Override
        public String toString() {
            return "Obj{" +
                    "label=" + label +
                    ", prob=" + prob +
                    ", x=" + x +
                    ", y=" + y +
                    ", w=" + w +
                    ", h=" + h +
                    '}';
        }
    }
}
