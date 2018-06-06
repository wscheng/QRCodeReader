/*
 * Copyright (C) The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.asus.amax.qrcodereader.ui.camera;

import android.Manifest;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.RequiresPermission;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup;

import com.google.android.gms.common.images.Size;
import com.google.android.gms.vision.CameraSource;

import java.io.IOException;

public class CameraSourcePreview extends ViewGroup {
    private static final String TAG = "CameraSourcePreview";

    private Context mContext;
    private SurfaceView mSurfaceView;
    private boolean mStartRequested;
    private boolean mSurfaceAvailable;
    private CameraSource mCameraSource;

    private GraphicOverlay mOverlay;

    public CameraSourcePreview(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        mStartRequested = false;
        mSurfaceAvailable = false;
        mSurfaceView = new SurfaceView(context);
        mSurfaceView.getHolder().addCallback(new SurfaceCallback());
        addView(mSurfaceView);
    }

    @RequiresPermission(Manifest.permission.CAMERA)
    public void start(CameraSource cameraSource) throws IOException, SecurityException {
        if (cameraSource == null) {
            stop();
        }

        mCameraSource = cameraSource;
        Size size = mCameraSource.getPreviewSize();
        if(size == null) {
            Log.i(TAG, "start() getPreviewSize() is null");
        } else {
            Log.i(TAG, "start() size="+size);
        }
        if (mCameraSource != null) {
            mStartRequested = true;
            startIfReady();
        }
    }

    @RequiresPermission(Manifest.permission.CAMERA)
    public void start(CameraSource cameraSource, GraphicOverlay overlay) throws IOException, SecurityException {
        mOverlay = overlay;
        start(cameraSource);
    }

    public void stop() {
        if (mCameraSource != null) {
            mCameraSource.stop();
        }
    }

    public void release() {
        if (mCameraSource != null) {
            mCameraSource.release();
            mCameraSource = null;
        }
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        float lineLength = 100;
        float boxSize = 600;
        float strokeWidth = 20;
        float initialX = (mChildWidth - boxSize) / 2 + mPreviewInitialX;
        float initialY = (mChildHeight - boxSize) / 2 + mPreviewInitialY;
        Paint paint = new Paint();
        paint.setStrokeWidth(strokeWidth);
        paint.setColor(Color.BLUE);
        Log.i(TAG, "dispathcDraw(), mPreviewWidth=" + mChildWidth + ", mPreviewInitialY=" + mChildHeight);
        Log.i(TAG, "dispathcDraw(), mPreviewInitialX=" + mPreviewInitialX);
        Log.i(TAG, "dispathcDraw(), mPreviewInitialY=" + mPreviewInitialY);
        // top left
        canvas.drawLine(initialX - strokeWidth/2, initialY, initialX + lineLength, initialY, paint);
        canvas.drawLine(initialX, initialY - strokeWidth/2, initialX, initialY + lineLength, paint);
        // top right
        float topRightX = initialX + boxSize;
        canvas.drawLine(topRightX + strokeWidth/2, initialY, topRightX - lineLength, initialY, paint);
        canvas.drawLine(topRightX, initialY - strokeWidth/2, topRightX, initialY + lineLength, paint);
        // bottom left
        float bottomLeftY = initialY + boxSize;
        canvas.drawLine(initialX - strokeWidth/2, bottomLeftY, initialX + lineLength, bottomLeftY, paint);
        canvas.drawLine(initialX, bottomLeftY + strokeWidth/2, initialX, bottomLeftY - lineLength, paint);
        // bottom right
        canvas.drawLine(topRightX + strokeWidth/2, bottomLeftY, topRightX - lineLength, bottomLeftY, paint);
        canvas.drawLine(topRightX, bottomLeftY + strokeWidth/2, topRightX, bottomLeftY - lineLength, paint);


        Size size = mCameraSource.getPreviewSize();
        if (size != null) {
            float width = size.getWidth();
            float  height = size.getHeight();
            Log.i(TAG, "dispatchDraw(), CameraSourcePreviewSize, width=" + width + ", height=" + height);
        } else {
            Log.i(TAG, "dispatchDraw(), CameraSource.getPreviewSize() is null");
        }
    }

    @RequiresPermission(Manifest.permission.CAMERA)
    private void startIfReady() throws IOException, SecurityException {
        if (mStartRequested && mSurfaceAvailable) {
            mCameraSource.start(mSurfaceView.getHolder());
            if (mOverlay != null) {
                Size size = mCameraSource.getPreviewSize();
                int min = Math.min(size.getWidth(), size.getHeight());
                int max = Math.max(size.getWidth(), size.getHeight());
                if (isPortraitMode()) {
                    // Swap width and height sizes when in portrait, since it will be rotated by
                    // 90 degrees
                    mOverlay.setCameraInfo(min, max, mCameraSource.getCameraFacing());
                } else {
                    mOverlay.setCameraInfo(max, min, mCameraSource.getCameraFacing());
                }
                mOverlay.clear();
            }
            mStartRequested = false;
        }
        invalidate();
    }

    private class SurfaceCallback implements SurfaceHolder.Callback {
        @Override
        public void surfaceCreated(SurfaceHolder surface) {
            mSurfaceAvailable = true;
            try {
                startIfReady();
            } catch (SecurityException se) {
                Log.e(TAG,"Do not have permission to start the camera", se);
            } catch (IOException e) {
                Log.e(TAG, "Could not start camera source.", e);
            }
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder surface) {
            mSurfaceAvailable = false;
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        }
    }

    int mChildWidth;
    int mChildHeight;
    int mPreviewInitialX;
    int mPreviewInitialY;
    float mPreviewLongerSize;
    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        // TODO change the width and height
        int width = 1600;
        int height = 1200;
        Log.i(TAG, "onLayout()");
        if (mCameraSource != null) {
            Log.i(TAG, "mCameraSource is not null");
            Size size = mCameraSource.getPreviewSize();
            if (size != null) {
                width = size.getWidth();
                height = size.getHeight();
                Log.i(TAG, "mCameraSourcePreviewSize, width=" + width + ", height=" + height);
            } else {
                Log.i(TAG, "mCameraSource.getPreviewSize() is null");
            }
        }
        Log.i(TAG, "onLayout(), width=" + width + ", height=" + height);

        // Swap width and height sizes when in portrait, since it will be rotated 90 degrees
        if (isPortraitMode()) {
            int tmp = width;
            //noinspection SuspiciousNameCombination
            width = height;
            height = tmp;
        }

        Log.i(TAG, "left=" + left + ", right=" + right + ", bottom=" + bottom + ", top= " + top);
        final int layoutWidth = right - left;
        final int layoutHeight = bottom - top;

        // Computes height and width for potentially doing fit width.
        mChildWidth = layoutWidth;
        mChildHeight = (int)(((float) layoutWidth / (float) width) * height);

        // If height is too tall using fit width, does fit height instead.
        if (mChildHeight > layoutHeight) {
            mChildHeight = layoutHeight;
            mChildWidth = (int)(((float) layoutHeight / (float) height) * width);
            Log.i(TAG, "childHeight > layoutHeight, childHeight=" + mChildHeight + "layoutHeight=" + layoutHeight);
        }
        Log.i(TAG, "layoutWidth=" + layoutWidth + ", layoutHeight=" + layoutHeight +
                ", ChildWidth=" + mChildWidth + ", childHeight=" + mChildHeight);
        mPreviewInitialX = (layoutWidth - mChildWidth) / 2;
        mPreviewInitialY = (layoutHeight - mChildHeight) / 2;
        mPreviewLongerSize = (mChildHeight > mChildWidth) ? mChildHeight : mChildWidth;

        Log.d(TAG, "LLLL="+ mPreviewInitialX +", TTT="+ mPreviewInitialY);
        for (int i = 0; i < getChildCount(); ++i) {
            Log.i(TAG, "child["+i+"]"+getChildAt(i));
            //getChildAt(i).layout(0, 0, childWidth, childHeight);
            getChildAt(i).layout(mPreviewInitialX, mPreviewInitialY,
                    (layoutWidth + mChildWidth) / 2, (layoutHeight + mChildHeight) / 2);
        }

        try {
            startIfReady();
        } catch (SecurityException se) {
            Log.e(TAG,"Do not have permission to start the camera", se);
        } catch (IOException e) {
            Log.e(TAG, "Could not start camera source.", e);
        }
    }

    private boolean isPortraitMode() {
        int orientation = mContext.getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            return false;
        }
        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            return true;
        }

        Log.d(TAG, "isPortraitMode returning false by default");
        return false;
    }
}
