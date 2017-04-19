package com.example.aman.ocroodles;

import android.util.Log;
import android.util.SparseArray;
import android.widget.TextView;

import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.text.Text;
import com.google.android.gms.vision.text.TextBlock;

import java.util.List;

public class OcrDetectorProcessor implements Detector.Processor<TextBlock> {

    private GraphicOverlay<OCRGraphic> mGraphicOverlay;
    MyTextListener myTextListener;
    String tempRecognized;

    OcrDetectorProcessor(GraphicOverlay<OCRGraphic> graphicOverlay, MainActivity mainActivity) {
        myTextListener = mainActivity;
        tempRecognized= mainActivity.recognizedString;
        mGraphicOverlay = graphicOverlay;

    }

    @Override
    public void release() {
        Log.d("release","release2121");
       // mGraphicOverlay.clear();
        //tempRecognized="";
    }

    /**
     * Called by the detector to deliver detection results.
     * If your application called for it, this could be a place to check for
     * equivalent detections by tracking TextBlocks that are similar in location and content from
     * previous frames, or reduce noise by eliminating TextBlocks that have not persisted through
     * multiple detections.
     */
    @Override
    public void receiveDetections(Detector.Detections<TextBlock> detections) {

       // mGraphicOverlay.clear();
       // tempRecognized="";
        //Log.d("receiveDetections",detections.toString());
        SparseArray<TextBlock> items = detections.getDetectedItems();
        Log.d("receiveDetections3232", items.toString());
        for (int i = 0; i < items.size(); ++i) {
            TextBlock item = items.valueAt(i);
            Log.d("receiveDetections", item.toString());

            OCRGraphic graphic = new OCRGraphic(mGraphicOverlay,item,myTextListener);
            mGraphicOverlay.add(graphic);
        }
        Log.d("item got finished", "hello");

    }

}
