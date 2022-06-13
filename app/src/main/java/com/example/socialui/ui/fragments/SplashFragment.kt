package com.example.socialui.ui.fragments

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.example.socialui.R
import com.example.socialui.data.sharedpref.getOnBoardingStatus
import com.example.socialui.data.sharedpref.setOnBoardingStatus
import com.example.socialui.databinding.FragmentSplashBinding
import kotlinx.coroutines.delay
import java.lang.Thread.sleep


class SplashFragment : Fragment() {

    private lateinit var binding: FragmentSplashBinding
    private lateinit var navController: NavController
    private var hasScreenChanged = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = FragmentSplashBinding.inflate(layoutInflater)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)

        setupBlink()

        if (requireContext().getOnBoardingStatus()) {
            navigateToOnBoarding()
            requireContext().setOnBoardingStatus(false)
        }else{
            navigateToHome()
        }
    }

    private fun setupBlink() {
        with(binding){
            splashText.apply {
                val animation = AnimationUtils.loadAnimation(this.context,R.anim.text_blink)
                startAnimation(animation)
            }
        }
    }

    private fun navigateToHome() {
        Handler(Looper.getMainLooper()).postDelayed({
            hasScreenChanged = true
            navController.navigate(R.id.action_splashFragment_to_homeFragment)
        },1200)
    }

    private fun navigateToOnBoarding() {
        Handler(Looper.getMainLooper()).postDelayed({
            hasScreenChanged = true
            navController.navigate(R.id.action_splashFragment_to_onBoardingFragment)
        },1200)
    }

    companion object {
        private const val TAG = "Splash_Fragment"
    }
}