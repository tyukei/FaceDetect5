package com.fin.facedetect5

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ImageFormat
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PointF
import android.graphics.Rect
import android.graphics.YuvImage
import android.util.Log
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceContour
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import com.google.mlkit.vision.face.FaceLandmark
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer

class FaceAnalyzer(
    private var listener: (Int, Boolean, Face?, Bitmap?) -> Unit,
    private var bitmapListener: FaceAnalyzerListener
) :
    ImageAnalysis.Analyzer {
    interface FaceAnalyzerListener {
        fun onBitmapReady(bitmap: Bitmap)
    }

    private val options = FaceDetectorOptions.Builder()
        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
        .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
        .setContourMode(FaceDetectorOptions.CONTOUR_MODE_ALL)
        .setContourMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
        .build()

    private val detector = FaceDetection.getClient(options)


    @ExperimentalGetImage
    override fun analyze(imageProxy: ImageProxy) {

        val mediaImage = imageProxy.image ?: return
        val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
        var face: Face? = null
        var visualizedBitmap: Bitmap? = null

        detector.process(image)
            .addOnSuccessListener { faces ->
                var isFrontFacing = false
                var isFullFace = false
                if (faces.size > 0) {
                    Log.d("FaceAnalyzer", "faces.size: ${faces.size}")
                    face = faces[0]
                    val leftEye = face?.getLandmark(FaceLandmark.LEFT_EYE)
                    val leftEyeContour = leftEye?.position
                    val rightEye = face?.getLandmark(FaceLandmark.RIGHT_EYE)
                    val rightEyeContour = rightEye?.position
                    val leftEar = face?.getLandmark(FaceLandmark.LEFT_EAR)
                    val leftEarContour = leftEar?.position
                    val rightEar = face?.getLandmark(FaceLandmark.RIGHT_EAR)
                    val rightEarContour = rightEar?.position
                    val mouthLeft = face?.getLandmark(FaceLandmark.MOUTH_LEFT)
                    val mouthLeftContour = mouthLeft?.position
                    val mouthRight = face?.getLandmark(FaceLandmark.MOUTH_RIGHT)
                    val mouthRightContour = mouthRight?.position
                    val noseBase = face?.getLandmark(FaceLandmark.NOSE_BASE)
                    val noseBaseContour = noseBase?.position
                    Log.d(
                        "FaceAnalyzer",
                        "Contour: $leftEyeContour, $rightEyeContour, $leftEarContour, $rightEarContour, $mouthLeftContour, $mouthRightContour, $noseBaseContour"
                    )
                    val faceContours = face?.getContour(FaceContour.FACE)?.points
                    Log.d("FaceAnalyzer", "faceContours: $faceContours")
                    val bitmap = imageProxy.toBitmap()
                    visualizedBitmap = drawContoursOnBitmap(bitmap, faceContours)
                    bitmapListener?.onBitmapReady(visualizedBitmap!!)
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
                listener(faces.size, isFrontFacing, face, visualizedBitmap)

            }
            .addOnFailureListener { e ->
                Log.e("FaceAnalyzer", "Face detection failure.", e)
            }
            .addOnCompleteListener {
                imageProxy.close()
            }
    }

    private fun drawContoursOnBitmap(bitmap: Bitmap, faceContours: List<PointF>?): Bitmap {
        val mutableBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(mutableBitmap)
        val paint = Paint().apply {
            color = Color.RED
            strokeWidth = 5f
            style = Paint.Style.STROKE
        }

        faceContours?.let {
            val path = Path()
            path.moveTo(it[0].x, it[0].y)
            for (i in 1 until it.size) {
                path.lineTo(it[i].x, it[i].y)
            }
            canvas.drawPath(path, paint)
        }

        return mutableBitmap
    }

    // Convert ImageProxy to Bitmap
    private fun ImageProxy.toBitmap(): Bitmap {
        val yBuffer = planes[0].buffer
        val uBuffer = planes[1].buffer
        val vBuffer = planes[2].buffer

        val ySize = yBuffer.remaining()
        val uSize = uBuffer.remaining()
        val vSize = vBuffer.remaining()

        val nv21 = ByteArray(ySize + uSize + vSize)

        nv21.fill(yBuffer)
        nv21.fill(vBuffer)
        nv21.fill(uBuffer)

        val yuvImage = YuvImage(nv21, ImageFormat.NV21, width, height, null)
        val out = ByteArrayOutputStream()
        yuvImage.compressToJpeg(Rect(0, 0, yuvImage.width, yuvImage.height), 100, out)
        val imageBytes = out.toByteArray()
        return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
    }

    private fun ByteArray.fill(byteBuffer: ByteBuffer) {
        byteBuffer.put(this)
    }


}

