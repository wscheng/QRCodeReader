package com.asus.amax.qrcodereader;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;

import java.io.File;

public class SaveImageTask extends AsyncTask<Bitmap, Void, File> {
    private static final String TAG = "SaveImageTask";
    // listener when save complete

    private final Callback mCallback;

    SaveImageTask(Callback callback) {
        this.mCallback = callback;
    }

    @Override
    protected File doInBackground(Bitmap... bitmaps) {
        Bitmap image = bitmaps[0];
        return Utilities.saveBitmapToFile(image);
    }

    @Override
    protected void onPostExecute(File file) {
        super.onPostExecute(file);
        mCallback.onSaveComplete(file);
        Log.i(TAG, "save complete!");
    }

    public interface Callback {
        void onSaveComplete(File filePath);
    }

}
