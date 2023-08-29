package com.fin.facedetect5

import android.util.Log
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection

class FaceAnalyzer(private var listener: (Int) -> Unit) : ImageAnalysis.Analyzer {
    private val detector = FaceDetection.getClient()

    @ExperimentalGetImage
    override fun analyze(imageProxy: ImageProxy) {

        val mediaImage = imageProxy.image ?: return
        val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

        detector.process(image)
            .addOnSuccessListener { faces ->
                listener(faces.size)
            }
            .addOnFailureListener { e ->
                Log.e("FaceAnalyzer", "Face detection failure.", e)
            }
            .addOnCompleteListener {
                imageProxy.close()
            }
    }
}
