package com.aprouxdev.hyaluroadvices.android.services

import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetector
import com.google.mlkit.vision.face.FaceDetectorOptions

object FacialRecognitionService {

    private val detector: FaceDetector


    init {
        val options = FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
            .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL) // Whether to attempt to identify facial "landmarks": eyes, ears, nose, cheeks, mouth, and so on.
            .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_NONE) // Whether or not to classify faces into categories such as "smiling", and "eyes open".
            .setContourMode(FaceDetectorOptions.CONTOUR_MODE_NONE)
            .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_NONE)
            .setMinFaceSize(0.15f) //Sets the smallest desired face size, expressed as the ratio of the width of the head to width of the image.
            .build()
        //enableTracking() Whether to enable face tracking.

        detector = FaceDetection.getClient(options)
    }

    fun detectFaces(image: InputImage) {
        detector.process(image)
            .addOnSuccessListener { faces ->
                for (face in faces) {
                    val bounds = face.boundingBox
                    val rotY = face.headEulerAngleY  // Head is rotated to the right rotY degrees
                    val rotZ = face.headEulerAngleZ  // Head is tilted sideways rotZ degrees
                    // Etc ...
                }
            }
            .addOnFailureListener { e ->
                // Handle error
            }
    }
}