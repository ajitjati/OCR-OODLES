package com.example.aman.ocroodles;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.media.Image;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.os.Handler;

import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;


import org.w3c.dom.Text;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, MyTextListener {

    Button selectedImage, takePicture;
    ImageView image;
    GraphicOverlay<OCRGraphic> mGraphicOverlay;
    private final static int GET_IMAGE_CAPTURE_REQUEST_CODE = 101;
    private final static int GET_IMAGE_REQUEST_CODE = 102;
    Bitmap bitmap;
    Frame myFrame;
    private int REQUEST_CODE = 101;
    TextRecognizer textRecognizer;
    String mCurrentPhotoPath;
    File imageFile;
    public TextView recognizedTextView;
    String recognizedString = "";

    Image tempImage;
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViews();
        initialImageTextReader();
    }

    private void initialImageTextReader() {
        recognizedTextView.setText("");
        bitmap=BitmapFactory.decodeResource(getResources(),R.drawable.oodles_studio_logo);
        setMyFrame();
    }

    private void initViews() {
        Log.e("textCallBack", "OnCreate");
        textRecognizer = new TextRecognizer.Builder(getApplicationContext()).build();
        mGraphicOverlay = (GraphicOverlay<OCRGraphic>) findViewById(R.id.graphic_overlay);
        image = (ImageView) findViewById(R.id.image_view);

        selectedImage = (Button) findViewById(R.id.select_image);
        takePicture = (Button) findViewById(R.id.take_picture);

        recognizedTextView = (TextView) findViewById(R.id.text_view);
        if (recognizedTextView != null) {
            Log.e("dhfhfhfhfh", "fjfhhfhfhf");
        }
        selectedImage.setOnClickListener(this);
        takePicture.setOnClickListener(this);


    }

    private void setDetectedText() {
        if (myFrame != null) {
            recognizedString = "";
            recognizedTextView.setText("");
            Log.d("setDetectedText", myFrame.toString());
            textRecognizer.setProcessor(new OcrDetectorProcessor(mGraphicOverlay, this));


            Log.d("setDetectedText", String.valueOf(textRecognizer.isOperational()));
            if (!textRecognizer.isOperational()) {
                // Note: The first time that an app using a Vision API is installed on a
                // device, GMS will download a native libraries to the device in order to do detection.
                // Usually this completes before the app is run for the first time.  But if that
                // download has not yet completed, then the above call will not detect any text,
                // barcodes, or faces.
                //
                // isOperational() can be used to check if the required native libraries are currently
                // available.  The detectors will automatically become operational once the library
                // downloads complete on device.
                Log.w(TAG, "Detector dependencies are not yet available.");

                // Check for low storage.  If there is low storage, the native library will not be
                // downloaded, so detection will not become operational.
                IntentFilter lowstorageFilter = new IntentFilter(Intent.ACTION_DEVICE_STORAGE_LOW);
                boolean hasLowStorage = registerReceiver(null, lowstorageFilter) != null;

                if (hasLowStorage) {
                    Toast.makeText(this, "Low Storage Error", Toast.LENGTH_LONG).show();
                    Log.w(TAG, "Low Storage Error");
                }
            }
            setDetector(textRecognizer);

        }
    }

    @Override
    public void onClick(View v) {
        recognizedTextView.setText("");

        int itemId = v.getId();
        switch (itemId) {
            case R.id.select_image:
                openImageChooser();
                break;

            case R.id.take_picture:
                try {
                    openCamera();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
        }
    }

    private void openCamera() throws IOException {

        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (Build.VERSION.SDK_INT >= 23) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE);
        }
        Uri photoURI = FileProvider.getUriForFile(this, getApplicationContext().getPackageName() + ".provider", createImageFile());

        cameraIntent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, photoURI);
        cameraIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivityForResult(cameraIntent, GET_IMAGE_CAPTURE_REQUEST_CODE);
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DCIM), "Camera");
        imageFile = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = "file:" + imageFile.getAbsolutePath();
        return imageFile;
    }

    private void openImageChooser() {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), GET_IMAGE_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case GET_IMAGE_CAPTURE_REQUEST_CODE:
                    if (data != null) {
                        bitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath());
                        //bitmap = Bitmap.createScaledBitmap(bitmap,1280,1280,true);
                        image.setImageBitmap(bitmap);
                        setMyFrame();
                    }
                    break;
                case GET_IMAGE_REQUEST_CODE:
                    if (data != null) {
                        try {
                            bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), data.getData());
                            image.setImageBitmap(bitmap);
                            setMyFrame();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    break;
            }
        }
    }

    public void setMyFrame() {
        myFrame = new Frame.Builder().setBitmap(bitmap).build();
        setDetectedText();
    }


    public void setDetector(Detector<?> detector) {
        FrameProcessingRunnable obj = new FrameProcessingRunnable(detector);
        obj.start();
    }

    @Override
    public void textCallback(String text) {
        if (text != null) {
            recognizedString += text + "\n";
            Log.e("before", recognizedString.toString());
            runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    //stuff that updates ui
                    recognizedTextView.setText(recognizedString);

                }
            });

        }
    }


    private class FrameProcessingRunnable extends Thread {
        private Detector<?> mDetector;

        FrameProcessingRunnable(Detector<?> detector) {
            mDetector = detector;
        }


        @Override
        public void run() {
            mDetector.receiveFrame(myFrame);
            Log.d("frame", "run");
        }
    }
}