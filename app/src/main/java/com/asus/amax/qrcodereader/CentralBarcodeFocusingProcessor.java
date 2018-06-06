package com.asus.amax.qrcodereader;

import android.util.Log;
import android.util.SparseArray;

import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.FocusingProcessor;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.Tracker;
import com.google.android.gms.vision.barcode.Barcode;

public class CentralBarcodeFocusingProcessor extends FocusingProcessor<Barcode> {

    private static final String TAG = "CentralBarcodeFP";

    CentralBarcodeFocusingProcessor(Detector<Barcode> detector, Tracker<Barcode> tracker) {
        super(detector, tracker);
    }

    @Override
    public int selectFocus(Detector.Detections detections) {
        SparseArray<Barcode> barcodes = detections.getDetectedItems();
        Log.i(TAG, "barcodes.size()=" + barcodes.size());
        Frame.Metadata meta = detections.getFrameMetadata();

        Log.i(TAG, "meta.getWidth=" + meta.getWidth());
        Log.i(TAG, "meta.getHeight=" + meta.getHeight());
        double nearestDistance = Double.MAX_VALUE;
        int id = -1;

        for (int i = 0; i < barcodes.size(); ++i) {
            int tempId = barcodes.keyAt(i);
            Barcode barcode = barcodes.get(tempId);
            float dx = Math.abs((meta.getWidth() / 2) - barcode.getBoundingBox().centerX());
            float dy = Math.abs((meta.getHeight() / 2) - barcode.getBoundingBox().centerY());

            double distanceFromCenter = Math.sqrt((dx * dx) + (dy * dy));
            Log.i(TAG, "i=" + i + ", distanceFromCenter=" + distanceFromCenter);

            if (distanceFromCenter < nearestDistance) {
                id = tempId;
                nearestDistance = distanceFromCenter;
            }
        }
        return id;
    }

}
