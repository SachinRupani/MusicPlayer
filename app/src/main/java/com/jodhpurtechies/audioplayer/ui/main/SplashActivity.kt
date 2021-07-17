package com.jodhpurtechies.audioplayer.ui.main

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Window
import android.view.WindowManager
import android.widget.LinearLayout
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import com.jodhpurtechies.audioplayer.R
import com.jodhpurtechies.audioplayer.utils.AndroidPermissions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Suppress("DEPRECATION")
class SplashActivity : AppCompatActivity() {

    private val tag = "SplashActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        setContentView(R.layout.activity_splash)

        askReadWritePermissions()

    }

    private fun askReadWritePermissions() {
        AndroidPermissions.askReadWritePermissions(
            activity = this,
            onPermissionsGranted = {
                Log.d(tag, "Permissions Granted")
                proceedToAudioListing()
            },
            onPermissionError = { errMsg ->
                Log.e(tag, errMsg)
                Snackbar.make(
                    findViewById<LinearLayout>(R.id.linLayoutRoot),
                    getString(R.string.please_provide_permissions),
                    Snackbar.LENGTH_LONG
                ).show()
                CoroutineScope(Dispatchers.Main).launch {
                    delay(1000)
                    askReadWritePermissions()
                }
            })
    }

    /**
     * Function to navigate to audio listing activity
     */
    private fun proceedToAudioListing() {
        lifecycleScope.launch(Dispatchers.Main) {
            delay(1000)
            startActivity(Intent(applicationContext, AudioListingActivity::class.java))
            finish()
        }
    }
}