package com.example.michail.photomanager;

import android.Manifest;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Paint;
import android.graphics.SurfaceTexture;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Size;
import android.util.SparseArray;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

import java.io.File;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


public class MainActivity extends AppCompatActivity {
    Clients c;
    private static final String TAG = " MyTag ";
    private static final int REQUEST_CAMERA_PERMISSION_RESULT = 0;
    public static String LOG_TAG = null;
    private TextureView mTextureView;
    private int counter = 0;
    private File mImageFolder;
    private String mImageFileName;
    private Size mImageSize;
    private Surface mPreviewSurface;
    private HandlerThread mBackgroundHandlerThread;
    private HandlerThread mPhotoImageHandlerThread;
    private Handler mBackgroundHandler;
    private String mCameraId;
    private Size mPreviewSize;
    private CaptureRequest.Builder mCaptureRequestBuilder;
    private ImageButton switch_On_or_Off, exitButton;
    private boolean isFlashOn = false;
    private boolean hasFlash;
    private CameraCaptureSession mSession;
    private CameraCharacteristics mCameraCharacteristics;
    private ImageReader mImageReader;
    private Surface mImageReaderSurface;
    private SurfaceTexture mSurfaceTexture;
    private final int STATE_PREVIEW = 0;
    private final int STATE_WAIT_LOCK = 1;
    private int mCaptureState = STATE_PREVIEW;
    private Thread mCatchThread;
    private Paint myRectPaint;
    private ImageView mImageView;
    private SurfaceHolder mSurfaceHolder;
    private SurfaceView mSurfaceView;
    private TextView mEditText;
    private ImageView imageView;
    private ShapeDrawable mRect;
    private int increment = 0;
    private boolean clickOn = false;
    private String id;


    private TextureView.SurfaceTextureListener mSurfaceTextureListener = new TextureView.SurfaceTextureListener() {
        @Override

        /**
         * Вызывается, когда SurfaceTexture TextureView готов к использованию.
         * @width - Ширина поверхности
         * @height - Высота поверхности
         *
         */

        public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int width, int height) {
            setupCamera(width, height);
            connectCamera();
            mSurfaceTexture = surfaceTexture;
        }

        @Override

        /**
         *Вызывается при изменении размера буфера SurfaceTexture.
         * @SurfaceTexture  - поверхность, возвращаемая getSurfaceTexture ()
         * @i -  int: новая ширина поверхности
         * @i1 - Int: новая высота поверхности
         */

        public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int i, int i1) {

        }

        @Override

        /**
         * Вызывается, когда указанное SurfaceTexture уничтожено
         * @surfaceTexture - поверхность, подлежащая уничтожению
         */

        public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
            return false;
        }

        @Override

        /**
         * Вызывается, когда указанная SurfaceTexture обновляется с помощью updateTextImage ().
         * @surfaceTexture только что обновленная поверхность
         */

        public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {
            counter++;
            if (counter == 50) {
                lockFocus();
                counter = 0;
            }
        }
    };
    CameraDevice mCameraDevice;
    private CameraDevice.StateCallback mCameraDeviceStateCallback = new CameraDevice.StateCallback() {
        @Override

        /**
         * Метод вызывается, когда устройство камеры завершает открытие.
         * @param cameraDevice -Устройство камеры, которое было открыто
         */

        public void onOpened(@NonNull CameraDevice cameraDevice) {
            mCameraDevice = cameraDevice;
            startPreview();
            Toast.makeText(getApplicationContext(), "Camera connection made :)", Toast.LENGTH_SHORT).show();

        }

        @Override

        /**
         * Метод вызывается, когда устройство камеры больше недоступно для использования.
         * @param cameraDevice - Устройство, которое было отключено
         */

        public void onDisconnected(@NonNull CameraDevice cameraDevice) {
            cameraDevice.close();
            mCameraDevice = null;
        }

        @Override

        /**
         * Метод вызван, когда устройство камеры обнаружило серьезную ошибку.
         * @param cameraDevice - Устройство, сообщившее об ошибке.
         * @param error -Код ошибки, одно из значений StateCallback.ERROR_ *.
         */

        public void onError(@NonNull CameraDevice cameraDevice, int error) {
            cameraDevice.close();
            mCameraDevice = null;
        }
    };


    private CameraCaptureSession.CaptureCallback mPreviewCaptureCallback = new CameraCaptureSession.CaptureCallback() {

        private void process(CaptureResult mCaptureResult) {
            startStillCaptureRequest();
        }

        @Override

        /**
         * Этот метод вызывается, когда захват изображения полностью завершен, и доступны все метаданные результата.
         * @param request - Запрос, который был передан в CameraDevice
         * @param result - Общий объем выходных метаданных из захвата, включая окончательные параметры захвата и состояние системы камеры во время захвата.
         */

        public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
            super.onCaptureCompleted(session, request, result);
            process(result);
        }
    };

    /**
     * Возможные положения девайса
     */

    private static SparseIntArray ORIENTATIONS = new SparseIntArray();

    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 0);
        ORIENTATIONS.append(Surface.ROTATION_90, 90);
        ORIENTATIONS.append(Surface.ROTATION_180, 180);
        ORIENTATIONS.append(Surface.ROTATION_270, 0);
    }

    private static class CompareSizeByArea implements Comparator<Size> {

        @Override
        public int compare(Size lhs, Size rhs) {
            return Long.signum((long) lhs.getWidth() * lhs.getHeight() / (long) rhs.getWidth() * rhs.getHeight());
        }

    }


    @Override

    /**
     * Вызывается, при  создании activity
     * */

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCameraId = "0";
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_main);
        mTextureView = (TextureView) findViewById(R.id.textureView);
        imageView = (ImageView) findViewById(R.id.imageView);
        switch_On_or_Off = (ImageButton) findViewById(R.id.imageButton);
       // exitButton = (ImageButton) findViewById(R.id.exitButton);
        myRectPaint = new Paint();
        myRectPaint.setStrokeWidth(10);
        myRectPaint.setColor(Color.YELLOW);
        myRectPaint.setStyle(Paint.Style.STROKE);
        setDrawable();
        switch_On_or_Off.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mCameraId == "0") {
                    closeCamera();
                    stopBackgroundThread();
                    mCameraId = "1";
                    connectCamera();
                } else {
                    closeCamera();
                    stopBackgroundThread();
                    mCameraId = "0";
                    connectCamera();
                }

            }

        });
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                closeCamera();
                try {
                    Intent intent = new Intent(MainActivity.this, InfoActivity.class);
                    intent.putExtra("id", id);
                    startActivity(intent);
                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(), "Intent error", Toast.LENGTH_LONG).show();
                }
            }
        });

       /* exitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentManager fragmentManager = getFragmentManager();
                ExitDialogFragment exit = new ExitDialogFragment();
                exit.show(fragmentManager, "dialog");
            }
        });*/

    }


    private void setDrawable() {
        imageView.setImageResource(R.drawable.rectangle);
        imageView.setVisibility(View.INVISIBLE);
    }

    @Override

    /**
     * Вызывается после onRestoreInstanceState (Bundle), onRestart () или onPause (), для работы с пользователем
     */

    protected void onResume() {
        super.onResume();
        startBackgroundThread();
        if (mTextureView.isAvailable()) {
            setupCamera(mTextureView.getWidth(), mTextureView.getHeight());
            connectCamera();

        } else {
            mTextureView.setSurfaceTextureListener(mSurfaceTextureListener);
        }
    }


    @Override

    /**
     * Обратный вызов для результата запроса прав доступа
     * @param requestCode - Код запроса, переданный в requestPermissions (String [], int).
     * @param permissions -  Запрошенные разрешения. Никогда не пусто.
     * @param grantResults - Результаты гранта для соответствующих разрешений, которые являются либо PERMISSION_GRANTED, либо PERMISSION_DENIED. Никогда не пусто.
     */

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION_RESULT) {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(getApplicationContext(), "Application will not run without camera services", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override

    /**
     * Вызывается, когда текущее окно активности сворачивается
     */
    protected void onPause() {
        closeCamera();
        stopBackgroundThread();
        super.onPause();

    }

    @Override

    /**
     * Вызывается, когда текущее окно активности получает или теряет фокус
     * @param hasFocus - Окно этой операции имеет фокус.
     */

    public void onWindowFocusChanged(boolean hasFocus) {

        super.onWindowFocusChanged(hasFocus);
        View decorView = getWindow().getDecorView();
        if (hasFocus) {
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);

        }
    }

    /**
     * Конфигурация камеры , создание модели камеры
     */

    private void setupCamera(int width, int height) {
        CameraManager cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {

            mCameraCharacteristics = (CameraCharacteristics) cameraManager.getCameraCharacteristics(mCameraId);
            if (mCameraCharacteristics.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_FRONT) {
                //continue;
            }
            StreamConfigurationMap map = mCameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            int deviceOrientation = getWindowManager().getDefaultDisplay().getRotation();
            int totalRotation = sensorToDeviceRotation(mCameraCharacteristics, deviceOrientation);
            boolean swapRotation = totalRotation == 90 || totalRotation == 270;
            int rotatedWidth = width;
            int rotatedHeight = height;
            if (swapRotation) {
                rotatedWidth = height;
                rotatedHeight = width;
            }
            mPreviewSize = chooseOptimalSize(map.getOutputSizes(SurfaceTexture.class), rotatedWidth, rotatedHeight);
            mImageSize = chooseOptimalSize(map.getOutputSizes(ImageFormat.JPEG), rotatedWidth, rotatedHeight);
            mImageReader = ImageReader.newInstance(mImageSize.getWidth(), mImageSize.getHeight(), ImageFormat.JPEG, 10);
            mImageReader.setOnImageAvailableListener(mOnImageAvailableListener, mBackgroundHandler);
            mImageReaderSurface = mImageReader.getSurface();

        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * Создание камеры и её открытие , проверка возможности открытия камеры для данного девайса
     */

    private void connectCamera() {
        CameraManager cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                    cameraManager.openCamera(mCameraId, mCameraDeviceStateCallback, mBackgroundHandler);
                } else {
                    if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
                        Toast.makeText(this, "App required access to camera", Toast.LENGTH_SHORT).show();
                    }
                    requestPermissions(new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION_RESULT);
                }
            } else {
                cameraManager.openCamera(mCameraId, mCameraDeviceStateCallback, mBackgroundHandler);
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * Отображение превью на SurfaceTexture
     */

    private final ImageReader.OnImageAvailableListener mOnImageAvailableListener = new ImageReader.OnImageAvailableListener() {

        @Override
        public void onImageAvailable(ImageReader imageReader) {
            mBackgroundHandler.post(new ImageRelease(imageReader.acquireLatestImage()));
        }

    };


    /**
     * Данный Thread используется для идентификации в превью barcode и визуализации его местоположения
     */

    private class ImageRelease implements Runnable {
        private final Image mImage;

        public ImageRelease(Image image) {
            mImage = image;
        }

        @Override
        public void run() {

            try {
                ByteBuffer buffer = mImage.getPlanes()[0].getBuffer();
                byte[] bytes = new byte[buffer.remaining()];
                buffer.get(bytes);
                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                final Bitmap tempBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.RGB_565);
                BarcodeDetector barcodeDetector = new BarcodeDetector.Builder(getApplicationContext())
                        .setBarcodeFormats(Barcode.ALL_FORMATS).build();
                if (!barcodeDetector.isOperational()) {
                    Toast.makeText(getApplicationContext(), "Could not set up the detector!", Toast.LENGTH_SHORT).show();
                }
                Frame frame = new Frame.Builder().setBitmap(bitmap).build();
                final SparseArray<Barcode> barcodes = barcodeDetector.detect(frame);

                if (barcodes.size() != 0) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //Log.i("s", String.valueOf(barcodes.size()));
                            imageView.setVisibility(View.VISIBLE);
                            final Barcode thisCode = barcodes.valueAt(0);
                            ShapeDrawable rect = new ShapeDrawable(new RectShape());
                            //Log.i("sdsf", thisCode.rawValue);
                            rect.getPaint().set(myRectPaint);
                            rect.setIntrinsicHeight(thisCode.getBoundingBox().width());
                            rect.setIntrinsicWidth(thisCode.getBoundingBox().height());
                            imageView.setBackground(rect);
                            imageView.setY(thisCode.cornerPoints[0].x);
                            imageView.setX(thisCode.cornerPoints[0].y);
                            id = thisCode.rawValue;
                        }

                    });

                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            imageView.setVisibility(View.INVISIBLE);
                            //mEditText.setVisibility(View.INVISIBLE);

                        }

                    });
                }
                //barcodes.clear();
                mImage.close();
            } catch (NullPointerException e) {

            }
        }


    }

    /**
     * Отображение превью на SurfaceTexture
     */

    private void startPreview() {
        SurfaceTexture surfaceTexture = mTextureView.getSurfaceTexture();
        surfaceTexture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());
        final Surface previewSurface = new Surface(surfaceTexture);
        try {
            mCaptureRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            mCaptureRequestBuilder.addTarget(previewSurface);

            mCameraDevice.createCaptureSession(Arrays.asList(previewSurface, mImageReaderSurface), new CameraCaptureSession.StateCallback() {
                @RequiresApi(api = Build.VERSION_CODES.M)
                @Override
                public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                    try {
                        mPreviewSurface = previewSurface;
                        mSession = cameraCaptureSession;
                        mSession.setRepeatingRequest(mCaptureRequestBuilder.build(), null, null); ////////////


                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
                    Toast.makeText(getApplicationContext(), "Unable to setup camera preview", Toast.LENGTH_SHORT).show();

                }
            }, null);

        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

    }

    /**
     * Необходимо  , для дальнейшего получения кадров с превью
     */

    private void startStillCaptureRequest() {
        try {
            mCaptureRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            mCaptureRequestBuilder.addTarget(mImageReader.getSurface());

            CameraCaptureSession.CaptureCallback stillCaptureCallback = new CameraCaptureSession.CaptureCallback() {
                @Override
                public void onCaptureStarted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, long timestamp, long frameNumber) {
                    super.onCaptureStarted(session, request, timestamp, frameNumber);

                }
            };

            mSession.capture(mCaptureRequestBuilder.build(), stillCaptureCallback, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * Закрытие камеры
     */

    private void closeCamera() {
        if (mCameraDevice != null) {
            mCameraDevice.close();
            mCameraDevice = null;
        }
    }

    /**
     * Запуск фонового потока
     */

    private void startBackgroundThread() {
        mBackgroundHandlerThread = new HandlerThread("Camera2");
        mBackgroundHandlerThread.start();
        mBackgroundHandler = new Handler(mBackgroundHandlerThread.getLooper());

    }

    /**
     * Остановка фонового потока
     */

    private void stopBackgroundThread() {
        mBackgroundHandlerThread.quitSafely();
        try {
            mBackgroundHandlerThread.join();
            mBackgroundHandlerThread = null;
            mBackgroundHandler = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    /**
     * Определение положения девайса
     */

    private static int sensorToDeviceRotation(CameraCharacteristics cameraCharacteristics, int deviceOrientation) {
        int sensorOrientation = cameraCharacteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
        deviceOrientation = ORIENTATIONS.get(deviceOrientation);
        return (sensorOrientation + deviceOrientation + 360) % 360;
    }

    private static Size chooseOptimalSize(Size[] choices, int width, int height) {
        List<Size> bigEnough = new ArrayList<Size>();
        for (Size option : choices) {
            if (option.getHeight() == option.getWidth() * height / width && option.getWidth() >= width && option.getHeight() >= height) {
                bigEnough.add(option);
            }
        }
        if (bigEnough.size() > 0) {
            return Collections.min(bigEnough, new CompareSizeByArea());
        } else {
            return choices[0];
        }
    }

    /**
     * Запуск сессии превью
     */

    private void lockFocus() {
        try {
            mSession.capture(mCaptureRequestBuilder.build(), mPreviewCaptureCallback, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

}