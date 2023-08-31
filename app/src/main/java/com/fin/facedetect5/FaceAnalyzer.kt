package com.fin.facedetect5

import android.util.Log
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import kotlin.math.abs

class FaceAnalyzer(private var listener: (Int, Boolean) -> Unit) : ImageAnalysis.Analyzer {
    private val detector = FaceDetection.getClient()

    @ExperimentalGetImage
    override fun analyze(imageProxy: ImageProxy) {

        val mediaImage = imageProxy.image ?: return
        val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

        detector.process(image)
            .addOnSuccessListener { faces ->
                var isFrontFacing = false
                if (faces.size > 0) {
                    // For simplicity, we just check the first face detected
                    val face = faces[0]

                    val yaw = face.headEulerAngleZ
                    val pitch = face.headEulerAngleY
                    val roll = face.headEulerAngleX

                    // Let's say we give an allowance of 15 degrees for each angle
                    isFrontFacing = (abs(yaw) < 15) && (abs(pitch) < 15) && (abs(roll) < 15)


                }

                listener(faces.size, isFrontFacing)
            }
            .addOnFailureListener { e ->
                Log.e("FaceAnalyzer", "Face detection failure.", e)
            }
            .addOnCompleteListener {
                imageProxy.close()
            }
    }
}

