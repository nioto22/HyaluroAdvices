package com.aprouxdev.hyaluroadvices.android

import androidx.appcompat.app.AppCompatActivity
import com.aprouxdev.hyaluroadvices.android.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {

    private var _binding: ActivityMainBinding? = null
    private val binding get() = requireNotNull(_binding)

    private val rootViewModel by lazy {
        RootViewModel()
    }


    /**
     * States :

     * Launch loader view
     * Start animation and horizontal scroll
     */

}