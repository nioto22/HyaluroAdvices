package com.aprouxdev.hyaluroadvices.android.fragments

import android.R
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.aprouxdev.hyaluroadvices.android.databinding.FragmentMainCameraCaptureBinding
import com.aprouxdev.hyaluroadvices.android.services.CameraService
import com.aprouxdev.hyaluroadvices.android.services.FaceContourGraphic
import com.aprouxdev.hyaluroadvices.android.viewmodels.MainCameraCaptureViewModel
import com.aprouxdev.hyaluroadvices.android.viewmodels.MainCameraCaptureViewModel.GraphicOverlayState.*
import com.google.mlkit.vision.face.Face
import kotlinx.coroutines.launch


class MainCameraCaptureFragment : Fragment(), OnItemSelectedListener {

    companion object {
        fun newInstance(): MainCameraCaptureFragment {
            val args = Bundle()

            val fragment = MainCameraCaptureFragment()
            fragment.arguments = args
            return fragment
        }
    }

    private var _binding: FragmentMainCameraCaptureBinding? = null
    private val binding get() = requireNotNull(_binding)

    private lateinit var viewModel: MainCameraCaptureViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMainCameraCaptureBinding.inflate(inflater, container, false)

        viewModel = ViewModelProvider(this)[MainCameraCaptureViewModel::class.java]

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        /** Take a picture
         *  - Send Picture to the model
         *  //TODO
         *
         * Display face cam
         * Add animation on face cam and informations
         * */

        setupDataObservers()
        viewModel.setUiViewSize(binding.imageView.width, binding.imageView.height)
        setupUiViews()


    }

    private fun setupUiViews() {


        binding.buttonText.setOnClickListener(View.OnClickListener { showToast("NOT DEV") })
        binding.buttonFace.setOnClickListener(View.OnClickListener {
            // TODO inside view model
            CameraService.takePicture()?.let {
                viewModel.runFaceContourDetection(it)
            }
        })

        val items = arrayOf("Test Image 1 (Text)", "Test Image 2 (Face)")
        context?.let {
            val adapter =
                ArrayAdapter(it, R.layout.simple_spinner_dropdown_item, items)
            binding.spinner.adapter = adapter
            binding.spinner.onItemSelectedListener = this
        }
    }

    private fun drawableToBitmap(drawable: Drawable): Bitmap? {
        if (drawable is BitmapDrawable) {
            return drawable.bitmap
        }
        try {
            val bitmap = Bitmap.createBitmap(
                drawable.intrinsicWidth,
                drawable.intrinsicHeight,
                Bitmap.Config.ARGB_8888
            )
            val canvas = Canvas(bitmap)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)

            return bitmap
        } catch (e: Exception) {
            Log.e("TAG_DEBUG", "drawableToBitmap: $e")
        }
        return null
    }


    //region DATA OBSERVERS
    private fun setupDataObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                // faceContourState
                launch {
                    viewModel.faceContourState.collect { faceContourState ->
                        when (faceContourState) {
                            is MainCameraCaptureViewModel.ModelState.Error -> showToast(
                                faceContourState.message ?: "Error"
                            )

                            MainCameraCaptureViewModel.ModelState.None -> Unit
                            is MainCameraCaptureViewModel.ModelState.Ongoing -> Unit // TODO in MainActivity with interface
                            is MainCameraCaptureViewModel.ModelState.Success -> {
                                faceContourState.faces.mapNotNull { it as? Face }.let {
                                    processFaceContourDetectionResult(it)
                                }
                            }
                        }
                    }
                }

                //GRAPHIC OVERLAY
                launch {
                    viewModel.graphicOverlayState.collect { graphicOverlayState ->
                        when (graphicOverlayState) {
                            NONE -> Unit
                            CLEAR -> binding.graphicOverlay.clear()
                        }
                    }
                }
                //
            }
        }
    }

    //endregion
    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    private fun processFaceContourDetectionResult(faces: List<Face>) {
        // Task completed successfully
        if (faces.size == 0) {
            showToast("No face found")
            return
        }
        with(binding.graphicOverlay) {
            this.clear()
            for (i in faces.indices) {
                val face: Face = faces[i]
                val faceGraphic = FaceContourGraphic(this)
                this.add(faceGraphic)
                faceGraphic.updateFace(face)
            }
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        viewModel.onItemSelected(parent, view, position, id)
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
        viewModel.onNothingSelected(parent)
    }


}
