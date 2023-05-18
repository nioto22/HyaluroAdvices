package com.aprouxdev.hyaluroadvices.android.viewmodels

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import android.view.View
import android.widget.AdapterView
import androidx.lifecycle.ViewModel
import com.aprouxdev.hyaluroadvices.android.services.FacialRecognitionService
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetector
import com.google.mlkit.vision.face.FaceDetectorOptions
import kotlinx.coroutines.flow.MutableStateFlow
import java.io.IOException
import java.io.InputStream


class MainCameraCaptureViewModel : ViewModel() {

    //region VARS
    private var mImageMaxWidth: Int? = null
    private var mImageMaxHeight: Int? = null

    private var mUiViewWidth: Int = 0
    private var mUiViewHeight: Int = 0

    //region FACE CONTOUR
    private val _faceContourState: MutableStateFlow<ModelState> = MutableStateFlow(ModelState.None)
    val faceContourState get() = _faceContourState
    sealed class ModelState {
        object None : ModelState()
        data class Ongoing(val percent: Float? = null) : ModelState()
        data class Success(val faces: List<Any>) : ModelState()
        data class Error(val message: String? = null) : ModelState()
    }
    //endregion

    //region GRAPHIC OVERLAY
    private val _graphicOverlayState: MutableStateFlow<GraphicOverlayState> =
        MutableStateFlow(GraphicOverlayState.NONE)
    val graphicOverlayState get() = _graphicOverlayState
    enum class GraphicOverlayState {
        NONE,
        CLEAR,
    }
    //endregion

    private val _selectedImage = MutableStateFlow<Bitmap?>(null)
    val selectedImage get() = _selectedImage


    fun setUiViewSize(width: Int, height: Int) {
        mUiViewWidth = width
        mUiViewHeight = height
    }
    //endregion


    fun runFaceContourDetection(bitmap: Bitmap?) {
        bitmap?.let {
            _faceContourState.value = ModelState.Ongoing()

            val image: InputImage = InputImage.fromBitmap(it, 0)
            val detector: FaceDetector = FacialRecognitionService.getDetector()
            detector.process(image)
                .addOnSuccessListener(
                    OnSuccessListener<List<Any>> { faces ->
                        Log.d("TAG_debug", "runFaceContourDetection: Success $faces")
                        _faceContourState.value = ModelState.Success(faces)
                    })
                .addOnFailureListener(
                    OnFailureListener { e -> // Task failed with an exception
                        _faceContourState.value = ModelState.Error()
                        e.printStackTrace()
                    })
        }
    }


// Functions for loading images from app assets.

    // Functions for loading images from app assets.
// Returns max image width, always for portrait mode. Caller needs to swap width / height for
// landscape mode.
    private fun getImageMaxWidth(): Int {

        if (mImageMaxWidth == null) {
            // Calculate the max width in portrait mode. This is done lazily since we need to
            // wait for
            // a UI layout pass to get the right values. So delay it to first time image
            // rendering time.
            mImageMaxWidth = mUiViewWidth
        }
        return mImageMaxWidth!!
    }

    // Returns max image height, always for portrait mode. Caller needs to swap width / height for
// landscape mode.
    private fun getImageMaxHeight(): Int {
        if (mImageMaxHeight == null) {
            // Calculate the max width in portrait mode. This is done lazily since we need to
            // wait for
            // a UI layout pass to get the right values. So delay it to first time image
            // rendering time.
            mImageMaxHeight = mUiViewHeight
        }
        return mImageMaxHeight!!
    }

    // Gets the targeted width / height.
    private fun getTargetedWidthHeight(): Pair<Int, Int> {
        val targetWidth: Int
        val targetHeight: Int
        val maxWidthForPortraitMode = getImageMaxWidth()
        val maxHeightForPortraitMode = getImageMaxHeight()
        targetWidth = maxWidthForPortraitMode
        targetHeight = maxHeightForPortraitMode
        return Pair(targetWidth, targetHeight)
    }

    fun onItemSelected(parent: AdapterView<*>?, v: View?, position: Int, id: Long) {
        _graphicOverlayState.value = GraphicOverlayState.CLEAR
        val selectedImage = _selectedImage.value ?: return
//        when (position) {
//            0 -> _selectedImage.value = getBitmapFromAsset(this, "Please_walk_on_the_grass.jpg")
//            1 -> _selectedImage.value = getBitmapFromAsset(this, "grace_hopper.jpg")
//            // Whatever you want to happen when the thrid item gets selected
//        }
        if (_selectedImage.value != null) {
            // Get the dimensions of the View
            val (targetWidth, maxHeight) = getTargetedWidthHeight()

            // Determine how much to scale down the image
            val scaleFactor =
                (selectedImage.width as Float / targetWidth.toFloat()).coerceAtLeast(
                    selectedImage.height as Float / maxHeight.toFloat()
                )
            val resizedBitmap = Bitmap.createScaledBitmap(
                selectedImage,
                (selectedImage.width / scaleFactor) as Int,
                (selectedImage.height / scaleFactor) as Int,
                true
            )
            _selectedImage.value = resizedBitmap
        }
    }

    fun onNothingSelected(parent: AdapterView<*>?) {
        // Do nothing
    }

    fun getBitmapFromAsset(context: Context, filePath: String?): Bitmap? {
        val assetManager = context.assets
        val `is`: InputStream
        var bitmap: Bitmap? = null
        try {
            `is` = assetManager.open(filePath!!)
            bitmap = BitmapFactory.decodeStream(`is`)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return bitmap
    }
}