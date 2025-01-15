package com.xin.dev.modulex.placeholder

import android.annotation.SuppressLint
import android.app.Fragment
import android.graphics.Color
import android.os.Bundle
import android.text.TextUtils
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

class PlaceholderFragment : Fragment() {
    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle
    ): View {
        return TextView(inflater.context).apply {
            textSize = 30f
            text = "Fragment not found"
            gravity = Gravity.CENTER
            setTextColor(Color.RED)
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }
    }
}