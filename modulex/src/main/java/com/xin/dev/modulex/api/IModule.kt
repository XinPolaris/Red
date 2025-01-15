package com.xin.dev.modulex.api

import android.app.Application

/**
 *  Created by HuangXin on 2025/1/13.
 */
fun interface IModule {
    fun onCreate(application: Application)
}