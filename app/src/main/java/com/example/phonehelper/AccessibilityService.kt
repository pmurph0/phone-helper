package com.example.phonehelper

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.annotation.SuppressLint
import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Path
import android.graphics.PixelFormat
import android.hardware.biometrics.BiometricPrompt
import android.media.AudioManager
import android.net.Uri
import android.os.CancellationSignal
import android.provider.MediaStore
import android.util.Log
import android.util.TypedValue
import android.view.*
import android.view.accessibility.AccessibilityEvent
import android.widget.ImageView
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.concurrent.TimeUnit


class AccessibilityService : AccessibilityService() {

    private val isDeviceLocked: Boolean get() = keyguardManager.isDeviceLocked

    private val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
    private val windowManager: WindowManager get() = getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private val audioManager get() = getSystemService(Context.AUDIO_SERVICE) as AudioManager

    private val gestureStartDelay = 0L
    private val gestureDuration = TimeUnit.MILLISECONDS.toMillis(150L)
    private val gestureEndX get() = dpToPx(700f).toFloat()
    private val finger1YPos get() = dpToPx(300f).toFloat()
    private val finger2YPos get() = dpToPx(500f).toFloat()
    private val gestureStartX get() = touchAreaWidth.toFloat()
    private val touchAreaHeight: Int get() = dpToPx(600f)
    private val touchAreaWidth: Int get() = dpToPx(16f)
    private val shareBtnWidth: Int get() = dpToPx(24f)
    private val shareBtnHeight: Int get() = dpToPx(24f)

    override fun onInterrupt() {
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        log("onAccessibilityEvent")
        log(event.toString())
        when {
            event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
                    && (event.packageName == "com.oneplus.gallery")
                    && isDeviceLocked-> tryShowShareBtn()
        }
    }

    private fun tryShowShareBtn() {
        log("tryShowShareBtn")
        addShareBtn()
    }

    private fun addShareBtn() {
        val view = ImageView(this).apply {
            setImageResource(R.drawable.ic_share_black_24dp)
        }
        windowManager.addView(view, WindowManager.LayoutParams(
            shareBtnWidth,
            shareBtnHeight,
            WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
            getTouchAreaWindowFlags(),
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.RIGHT or Gravity.BOTTOM
        })
        view.setOnClickListener {
            shareLastImgInCameraFolder()
        }
    }


    private fun shareLastImgInCameraFolder() {
        BiometricPrompt.Builder(this)
            .setTitle("Authenticate to share")
            .setDeviceCredentialAllowed(true)
            .build()
            .authenticate(
                CancellationSignal(),
                mainExecutor,
                object: BiometricPrompt.AuthenticationCallback() {
                    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult?) {
                        log("onAuthenticationSucceeded")
                        launchShareIntent()
                    }

                    override fun onAuthenticationError(errorCode: Int, errString: CharSequence?) {
                        log("onAuthenticationError: $errString")
                    }

                    override fun onAuthenticationFailed() {
                        log("onAuthenticationFailed")
                    }

                    override fun onAuthenticationHelp(helpCode: Int, helpString: CharSequence?) {
                        log("onAuthenticationHelp: $helpString")
                    }
                })
    }

    private fun launchShareIntent() {
        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, "This is my text to send.")
            type = "text/plain"
        }

        val shareIntent = Intent.createChooser(sendIntent, null)
        startActivity(shareIntent.apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        })


//        val bitmap: Bitmap = ImageDecoder.decodeBitmap(
//            ImageDecoder.createSource(
//                this.contentResolver,
//                getLastImageInCameraFolder(this)!!
//            )
//        )
//        val intent = Intent(Intent.ACTION_SEND)
//        intent.type = "image/*"
//        intent.putExtra(Intent.EXTRA_STREAM, getBitmapFromView(bitmap))
//        startActivity(Intent.createChooser(intent, "Share Image"))
    }

    fun getBitmapFromView(bmp: Bitmap?): Uri? {
        var bmpUri: Uri? = null
        try {
            val file = File(this.externalCacheDir, System.currentTimeMillis().toString() + ".jpg")

            val out = FileOutputStream(file)
            bmp?.compress(Bitmap.CompressFormat.JPEG, 90, out)
            out.close()
            bmpUri = Uri.fromFile(file)

        } catch (e: IOException) {
            e.printStackTrace()
        }
        return bmpUri
    }

    protected fun getLastImageInCameraFolder(c: Context): Uri? {
        val resolver = c.contentResolver ?: return null
        val proj = arrayOf(MediaStore.Images.Media.DATA)
        val cursor =
            resolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, proj, null, null, null)
        val count = cursor!!.count
        val position = 0
        if (!cursor.moveToPosition(position)) {
            return null
        }
        val column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
        val path = cursor.getString(column_index)
        cursor.close()
        return Uri.fromFile(File(path))
    }

    override fun onServiceConnected() {
        //Add view
        addRightEdgeView()
        addLeftEdgeView()
    }

    private fun addLeftEdgeView() {
        val view = View(this)
        windowManager.addView(view, WindowManager.LayoutParams(
            touchAreaWidth,
            touchAreaHeight,
            WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
            getTouchAreaWindowFlags(),
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.LEFT or Gravity.BOTTOM
        })
        val gestureDetector = GestureDetector(this, leftEdgeGestureListener)
        view.setOnTouchListener { _, motionEvent ->
            log("onTouch $motionEvent")
            gestureDetector.onTouchEvent(motionEvent)
        }
    }

    @SuppressLint("RtlHardcoded")
    private fun addRightEdgeView() {
        val view = View(this)
        windowManager.addView(view, WindowManager.LayoutParams(
            touchAreaWidth,
            touchAreaHeight,
            WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
            getTouchAreaWindowFlags(),
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.RIGHT or Gravity.CENTER_VERTICAL
        })
        val gestureDetector = GestureDetector(this, rightEdgeGestureListener)
        view.setOnTouchListener { _, motionEvent ->
            log("onTouch $motionEvent")
            gestureDetector.onTouchEvent(motionEvent)
        }
    }

    private fun getTouchAreaWindowFlags() = WindowManager.LayoutParams.FLAG_IGNORE_CHEEK_PRESSES or
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE


    private fun dpToPx(dip: Float): Int {
        val r: Resources = resources
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dip,
            r.displayMetrics
        ).toInt()
    }

    private val rightEdgeGestureListener = object: GestureDetector.SimpleOnGestureListener() {
        override fun onFling(
            e1: MotionEvent,
            e2: MotionEvent,
            velocityX: Float,
            velocityY: Float
        ): Boolean {
            log("onFling")
            //TODO reject drag by checking velocity
            if (e1.y > e2.y) {
                //fling up
                volumeUp()
            } else {
                //fling down
                volumeDown()
            }
            return true
        }

        override fun onDoubleTap(e: MotionEvent?): Boolean {
            log("onDoubleTap")
            tryOpenNavDrawer()
            return true
        }
    }

    private val leftEdgeGestureListener = object: GestureDetector.SimpleOnGestureListener() {
        override fun onFling(
            e1: MotionEvent,
            e2: MotionEvent,
            velocityX: Float,
            velocityY: Float
        ): Boolean {
            log("onFling")
            //TODO reject drag by checking velocity
            if (e1.y > e2.y) {
                tryOpenNavDrawer()
            }
            return true
        }

        override fun onDoubleTap(e: MotionEvent?): Boolean {
            log("onDoubleTap")
            tryOpenNavDrawer()
            return true
        }
    }

    private fun log(msg: String?) {
        if (msg != null) Log.d(com.example.phonehelper.AccessibilityService::class.java.simpleName, msg)
    }

    private fun volumeDown() {
        log("volume down")
        audioManager.adjustVolume(AudioManager.ADJUST_LOWER, AudioManager.FLAG_SHOW_UI)
    }

    private fun volumeUp() {
        log("volume up")
        audioManager.adjustVolume(AudioManager.ADJUST_RAISE, AudioManager.FLAG_SHOW_UI)
    }

    private fun tryOpenNavDrawer() {
        try {
            dispatchGesture(
                buildTwoFingerSwipeGesture(),
                object: GestureResultCallback() {
                    override fun onCancelled(gestureDescription: GestureDescription?) {
                        log("gesture cancelled")
                    }
                    override fun onCompleted(gestureDescription: GestureDescription?) {
                        log("gesture completed")
                    }
                },
                null).also { gestureDispatched ->
                log(if (gestureDispatched) "gesture dispatched" else "gesture not dispatched")
            }
        } catch (e:Exception) {
            println(e.message)
        }
    }


    private fun buildTwoFingerSwipeGesture(): GestureDescription {
        return GestureDescription.Builder()
            .addStroke(buildFinger1Swipe())
            .addStroke(buildFinger2Swipe())
            .build()
    }

    private fun buildFinger1Swipe(): GestureDescription.StrokeDescription {
        return GestureDescription.StrokeDescription(
            Path().apply {
                this.moveTo(gestureStartX, finger1YPos)
                this.lineTo(gestureEndX, finger1YPos)
            },
            gestureStartDelay,
            TimeUnit.MILLISECONDS.toMillis(gestureDuration)
        )
    }

    private fun buildFinger2Swipe(): GestureDescription.StrokeDescription {
        return GestureDescription.StrokeDescription(
            Path().apply {
                this.moveTo(gestureStartX, finger2YPos)
                this.lineTo(gestureEndX, finger2YPos)
            },
            gestureStartDelay,
            TimeUnit.MILLISECONDS.toMillis(gestureDuration)
        )
    }
}