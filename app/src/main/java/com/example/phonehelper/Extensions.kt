package com.example.phonehelper

import android.content.Context
import android.content.res.Resources
import android.util.DisplayMetrics
import android.util.Log
import android.util.Size
import android.util.TypedValue
import android.view.WindowManager

val Context.screenSize: Size
    get () {
        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        return Size(displayMetrics.widthPixels, displayMetrics.heightPixels)
    }

val Context.windowManager get() = getSystemService(Context.WINDOW_SERVICE) as WindowManager

fun log(msg: String?) {
    if (msg != null) Log.d("PhoneHelper", msg)
}

fun Context.toPx(value: Float): Int {
    val r: Resources = resources
    return TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        value,
        r.displayMetrics
    ).toInt()
}

fun Int.toPx(context: Context): Int {
    return context.toPx(this.toFloat())
}
fun Float.toPx(context: Context): Int {
    return context.toPx(this)
}