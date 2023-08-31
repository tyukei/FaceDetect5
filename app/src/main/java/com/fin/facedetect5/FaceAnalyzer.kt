package com.fin.facedetect5

import android.util.Log
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import com.google.mlkit.vision.face.FaceLandmark

class FaceAnalyzer(private var listener: (Int, Boolean, Face?) -> Unit) : ImageAnalysis.Analyzer {
    private val options = FaceDetectorOptions.Builder()
        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
        .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
        .build()

    private val detector = FaceDetection.getClient(options)


    @ExperimentalGetImage
    override fun analyze(imageProxy: ImageProxy) {

        val mediaImage = imageProxy.image ?: return
        val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
        var face: Face? = null

        detector.process(image)
            .addOnSuccessListener { faces ->
                var isFrontFacing = false
                var isFullFace = false
                if (faces.size > 0) {
                    Log.d("FaceAnalyzer", "faces.size: ${faces.size}")
                    face = faces[0]

                    val leftEye = face?.getLandmark(FaceLandmark.LEFT_EYE)
                    val rightEye = face?.getLandmark(FaceLandmark.RIGHT_EYE)
                    val leftEar = face?.getLandmark(FaceLandmark.LEFT_EAR)
                    val rightEar = face?.getLandmark(FaceLandmark.RIGHT_EAR)
                    val mouthLeft = face?.getLandmark(FaceLandmark.MOUTH_LEFT)
                    val mouthRight = face?.getLandmark(FaceLandmark.MOUTH_RIGHT)
                    val noseBase = face?.getLandmark(FaceLandmark.NOSE_BASE)
                    Log.d("FaceAnalyzer", "leftEye: $leftEye")

                    isFullFace = leftEye != null && rightEye != null &&
                            leftEar != null && rightEar != null &&
                            mouthLeft != null && mouthRight != null &&
                            noseBase != null


                    isFrontFacing = if (isFullFace) {
                        val yaw = face?.headEulerAngleY
                        val roll = face?.headEulerAngleZ
                        (kotlin.math.abs(yaw!!) < 5) && (kotlin.math.abs(roll!!) < 5)
                    } else {
                        false
                    }
                }
                listener(faces.size, isFrontFacing, face)

            }
            .addOnFailureListener { e ->
                Log.e("FaceAnalyzer", "Face detection failure.", e)
            }
            .addOnCompleteListener {
                imageProxy.close()
            }
    }

}

