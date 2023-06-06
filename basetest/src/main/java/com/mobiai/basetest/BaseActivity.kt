package com.mobiai.basetest

import android.content.Context
import android.content.Intent
import android.net.*
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.util.Log
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.viewbinding.ViewBinding

abstract class BaseActivity<V:ViewBinding> : AppCompatActivity(){

    companion object{
        private val  TAG  = BaseActivity::class.java.name
    }
    protected lateinit var binding : V
    private var onFullscreen = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = getViewBinding()
        setContentView(binding.root)
        decorView = window.decorView
        setFullscreen()
        createView()
    }

    protected abstract fun getLayoutResourceId(): Int

    protected abstract fun getViewBinding() : V

    protected abstract fun createView()

    private var decorView: View? = null
    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus && onFullscreen) {
            decorView!!.systemUiVisibility = hideSystemBars()
        }
    }


    open fun hideSystemBars(): Int {
        return (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
    }



    protected open fun setFullscreen() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false)
            val windowInsetsController = window.insetsController
            if (windowInsetsController != null) {
                windowInsetsController.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
                windowInsetsController.systemBarsBehavior =
                    WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            window.decorView.systemUiVisibility = hideSystemBars()
        }
    }

    open fun showKeyboard(view: View?) {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
    }

    open fun hideKeyboard() {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(window.decorView.rootView.windowToken, 0)
    }

    fun handleBackpress() {
        if (supportFragmentManager.backStackEntryCount > 0) {
            supportFragmentManager.popBackStackImmediate();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.statusBarColor = ContextCompat.getColor(this, android.R.color.transparent);
        }

        if (supportFragmentManager.backStackEntryCount == 0) {
            finish()
        }
    }

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            onNetworkAvailable()
        }

        override fun onLost(network: Network) {
            onNetworkLost()
        }
    }

    protected open fun onNetworkAvailable() {
        Log.d(TAG, "-> onNetworkAvailable ")
    }

    protected open fun onNetworkLost() {
        Log.d(TAG, "-> onNetworkLost ")
    }

    override fun onResume() {
        super.onResume()
        val connectivityManager =
            getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    }

    override fun onPause() {
        super.onPause()
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        connectivityManager.unregisterNetworkCallback(networkCallback)
    }

    private fun isOptimizeBattery() : Boolean{
        val powerManager = getSystemService(POWER_SERVICE) as PowerManager?
        if (powerManager != null) {
            return if (powerManager.isIgnoringBatteryOptimizations(packageName)) {
                Log.d(TAG, "-> has NOT ignoringBatteryBattery")
                false
            } else {
                Log.d(TAG, "-> has  ignoringBatteryBattery")
                true
            }
        }
        return true
    }

    protected fun requestBatteryOptimize(){
        val intent = Intent()
        intent.action = Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
        val uri: Uri = Uri.fromParts("package", packageName, null)
        intent.data = uri
        startActivity(intent)
    }

    override fun onDestroy() {
        super.onDestroy()

    }

    protected fun showFullscreen(on: Boolean) {
        onFullscreen = on
        if (on)
            setFullscreen()
    }


}

