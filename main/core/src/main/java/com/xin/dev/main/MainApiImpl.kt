package com.xin.dev.main

import android.util.Log
import com.xin.dev.modulex.api.anno.Service
import com.xin.dev.main.api.MainApi

/**
 *  Created by HuangXin on 2025/1/13.
 */
@Service
class MainApiImpl : MainApi {
    override fun refresh(): Int {
        Log.i(TAG, "refresh: $this")
        return 1
    }

    override fun doSomeThing() {
        Log.i(TAG, "doSomeThing: $this")
    }

    companion object {
        private const val TAG = "MainApiImpl"
    }
}

interface IMainService1 {}

@Service
class MainService1 : IMainService1 {
}

interface IMainService2 {}

@Service
class MainService2 : IMainService2 {
}

interface IMainService3 {}

@Service
class MainService3 : IMainService3 {
}

interface IMainService4 {}

@Service
class MainService4 : IMainService4 {
}