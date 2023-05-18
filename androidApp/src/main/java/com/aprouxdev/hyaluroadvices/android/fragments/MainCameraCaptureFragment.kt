package com.aprouxdev.hyaluroadvices.android.fragments

import android.R
import android.os.Bundle
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
import com.aprouxdev.hyaluroadvices.android.services.FaceContourGraphic
import com.aprouxdev.hyaluroadvices.android.viewmodels.MainCameraCaptureViewModel
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
        binding.buttonFace.setOnClickListener(View.OnClickListener { viewModel.runFaceContourDetection() })
        val items = arrayOf("Test Image 1 (Text)", "Test Image 2 (Face)")
        context?.let {
        val adapter =
             ArrayAdapter(it, R.layout.simple_spinner_dropdown_item, items)
            binding.spinner.adapter = adapter
            binding.spinner.onItemSelectedListener = this
        }
    }

    //region DATA OBSERVERS
    private fun setupDataObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED){
               // LAUNCH
                viewModel.faceContourState.collect { faceContourState ->
                    when (faceContourState) {
                        is MainCameraCaptureViewModel.ModelState.Error -> showToast(faceContourState.message ?: "Error")
                        MainCameraCaptureViewModel.ModelState.None -> Unit
                        is MainCameraCaptureViewModel.ModelState.Ongoing -> Unit // TODO in MainActivity
                        is MainCameraCaptureViewModel.ModelState.Success -> {
                            faceContourState.faces.mapNotNull { it as? Face }.let {
                                processFaceContourDetectionResult(it)
                            }
                        }
                    }
                }
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
