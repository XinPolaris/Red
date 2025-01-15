package com.axon.dev.modulex.placeholder

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.util.TypedValue
import android.view.Gravity
import android.widget.TextView

class PlaceholderView @SuppressLint("SetTextI18n") constructor(context: Context?) :
    TextView(context) {
    init {
        gravity = Gravity.CENTER
        setBackgroundColor(Color.WHITE)
        setTextColor(Color.BLACK)
        setTextSize(TypedValue.COMPLEX_UNIT_SP, 20f)
        text = "View not found"
    }
}