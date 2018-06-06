package com.asus.amax.qrcodereader;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.util.Log;
import android.util.SparseArray;
import android.widget.Toast;

import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.Frame;

import java.io.ByteArrayOutputStream;
import java.io.File;

public class BoxDetector<T> extends Detector<T> implements SaveImageTask.Callback {

    private static final String TAG = "BoxDetector";
    private Detector mDelegate;
    private int mBoxWidth, mBoxHeight;
    private Context mContext;

    BoxDetector(Context context, Detector<T> delegate, int boxWidth, int boxHeight) {
        mContext = context;
        mDelegate = delegate;
        mBoxWidth = boxWidth;
        mBoxHeight = boxHeight;
    }

    @Override
    public SparseArray<T> detect(Frame frame) {
        int width = frame.getMetadata().getWidth();
        int height = frame.getMetadata().getHeight();
        Log.i(TAG, "Frame width="+width + ", height="+height);
        int right = (width / 2) + (mBoxHeight / 2);
        int left = (width / 2) - (mBoxHeight / 2);
        int bottom = (height / 2) + (mBoxWidth / 2);
        int top = (height / 2) - (mBoxWidth / 2);
        Log.i(TAG, "smBoxWidth="+mBoxWidth + ", mBoxHeight="+mBoxHeight);
        Log.i(TAG, "right="+right + ", left="+left + ", bottom="+bottom+", top="+top);

        YuvImage yuvImage = new YuvImage(frame.getGrayscaleImageData().array(), ImageFormat.NV21, width, height, null);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        yuvImage.compressToJpeg(new Rect(left, top, right, bottom), 100, byteArrayOutputStream);
        byte[] jpegArray = byteArrayOutputStream.toByteArray();
        Bitmap bitmap = BitmapFactory.decodeByteArray(jpegArray, 0, jpegArray.length);
        new SaveImageTask(this).execute(bitmap);

        Frame croppedFrame =
                new Frame.Builder()
                        .setBitmap(bitmap)
                        .setRotation(frame.getMetadata().getRotation())
                        .build();
        Log.i(TAG, "DETECT before");
        return mDelegate.detect(croppedFrame);
//        return mDelegate.detect(frame);
    }

    public boolean isOperational() {
        return mDelegate.isOperational();
    }

    public boolean setFocus(int id) {
        return mDelegate.setFocus(id);
    }

    public void onSaveComplete(File filePath) {
        if(filePath == null){
            Log.w(TAG, "onSaveComplete: file dir error");
            return;
        }

        Toast saveCompleteMsg =
                Toast.makeText(mContext, "save completed", Toast.LENGTH_SHORT);
        saveCompleteMsg.show();

        Log.d(TAG, "onSaveComplete: save image in "+filePath);

        MediaScannerConnection.scanFile(
                mContext,
                new String[]{filePath.getAbsolutePath()},
                null,
                new MediaScannerConnection.MediaScannerConnectionClient() {
                    @Override
                    public void onMediaScannerConnected() {
                        Log.d(TAG, "onMediaScannerConnected");
                    }

                    @Override
                    public void onScanCompleted(String path, Uri uri) {
                        Log.d(TAG, "onScanCompleted");
                    }
                }
        );

    }
}
