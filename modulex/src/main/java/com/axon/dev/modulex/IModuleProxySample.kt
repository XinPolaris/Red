//package com.axon.dev.modulex
//
//import android.app.Application
//import com.axon.dev.modulex.proxy.Creator
//import com.axon.dev.modulex.proxy.IModuleProxy
//
///**
// *  Created by HuangXin on 2025/1/14.
// */
//class IModuleProxySample : IModuleProxy() {
//    override fun onCreate(application: Application) {
//        val module = ModuleImpl()
//        module.onCreate(application)
//    }
//
//    override fun initServices(services: MutableMap<Class<*>, Creator<*>>) {
//        services[IService::class.java] = object : Creator<IService> {
//            override fun create(): IService {
//                return ServiceImpl {}
//            }
//
//        }
//    }
//}