package com.mozhimen.scank.yolov8;

import android.content.res.AssetManager;
import android.graphics.Bitmap;

/**
 * @ClassName Yolov8Api
 * @Description TODO
 * @Author Mozhimen / Kolin Zhao
 * @Date 2024/2/3 17:33
 * @Version 1.0
 */
public class Yolov8Api {
    public native boolean init(AssetManager mgr, int modelid, int cpugpu);

    public native Obj[] detect(Bitmap bitmap);

    // 释放资源，放置OOM，释放后需要重新Init
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
    }
}
