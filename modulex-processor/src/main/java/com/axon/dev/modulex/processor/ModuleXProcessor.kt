package com.axon.dev.modulex.processor

import com.axon.dev.modulex.api.anno.App
import com.axon.dev.modulex.api.anno.Clazz
import com.axon.dev.modulex.api.anno.Module
import com.axon.dev.modulex.api.anno.Service
import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSDeclaration
import com.google.devtools.ksp.symbol.KSVisitorVoid
import com.google.devtools.ksp.validate


class ModuleXProcessor(private val environment: SymbolProcessorEnvironment) : SymbolProcessor {

    private val serviceImplList = mutableListOf<KSClassDeclaration>()
    private val clazzList = mutableListOf<KSClassDeclaration>()

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val ret = mutableListOf<KSAnnotated>()
        process(ret, resolver, Clazz::class.qualifiedName!!, ClazzKSVisitor(environment))
        process(ret, resolver, Service::class.qualifiedName!!, ServiceKSVisitor(environment))
        process(ret, resolver, Module::class.qualifiedName!!, ModuleKSVisitor(environment))
        process(ret, resolver, App::class.qualifiedName!!, AppKSVisitor(resolver, environment))
        return ret
    }

    private fun process(
        items: MutableList<KSAnnotated>,
        resolver: Resolver,
        annotationName: String,
        visitor: KSVisitorVoid
    ) {
        val symbols = resolver.getSymbolsWithAnnotation(annotationName)
        symbols.toList().forEach {
            if (!it.validate()) {
                items.add(it)
            } else {
                it.accept(visitor, Unit)
            }
        }
    }

    inner class ClazzKSVisitor(private val environment: SymbolProcessorEnvironment) :
        KSVisitorVoid() {
        override fun visitClassDeclaration(classDeclaration: KSClassDeclaration, data: Unit) {
            clazzList.add(classDeclaration)
        }
    }

    inner class ServiceKSVisitor(private val environment: SymbolProcessorEnvironment) :
        KSVisitorVoid() {
        override fun visitClassDeclaration(classDeclaration: KSClassDeclaration, data: Unit) {
            serviceImplList.add(classDeclaration)
        }
    }

    inner class ModuleKSVisitor(private val environment: SymbolProcessorEnvironment) :
        KSVisitorVoid() {
        override fun visitClassDeclaration(classDeclaration: KSClassDeclaration, data: Unit) {
            // 获取类名和包名
            val className = classDeclaration.simpleName.asString()
            val packageName = classDeclaration.packageName.asString()

            // 生成代理类名
            val proxyClassPackageName = NAME_MODULE_PACKAGE
            val proxyClassName = "${packageName.replace('.', '_')}_${className}Proxy"

            // 创建 IModuleProxy 实现类的代码
            val fileContent = buildString {
                // 包名
                appendLine("package $proxyClassPackageName")
                appendLine()
                // 导入语句
                appendLine("import android.app.Application")
                appendLine("import com.axon.dev.modulex.proxy.Creator")
                appendLine("import com.axon.dev.modulex.proxy.IModuleProxy")
                serviceImplList.forEach { serviceImpl ->
                    val serviceFullName = serviceImpl.superTypes.first()
                        .resolve().declaration.qualifiedName?.asString()
                    appendLine("import $serviceFullName")
                }
                appendLine()
                // 生成类声明
                appendLine("class $proxyClassName : IModuleProxy() {")
                // 属性声明
                appendLine()
                appendLine("    private val module = ${classDeclaration.qualifiedName?.asString()}()")
                appendLine()
                // 生成 onCreate 方法
                appendLine("    override fun onCreate(application: Application) {")
                appendLine("        module.onCreate(application)")
                appendLine("    }")
                appendLine()
                // 生成 initServices 方法
                appendLine("    override fun initServices(services: MutableMap<Class<*>, Creator<*>>) {")
                serviceImplList.forEach { serviceImpl ->
                    val serviceClass = serviceImpl.superTypes.first().resolve()
                    appendLine("        services[${serviceClass}::class.java] = object : Creator<$serviceClass> {")
                    appendLine("            override fun create(): $serviceClass = ${serviceImpl.qualifiedName?.asString()}()")
                    appendLine("        }")
                }
                appendLine("    }")
                // 结束类
                appendLine("}")
            }
            // 写入文件
            val generatedFile = environment.codeGenerator.createNewFile(
                Dependencies(true, classDeclaration.containingFile!!),
                proxyClassPackageName,
                proxyClassName
            )
            generatedFile.write(fileContent.toByteArray())
            generatedFile.close()
        }
    }

    inner class AppKSVisitor(
        private val resolver: Resolver, private val environment: SymbolProcessorEnvironment
    ) : KSVisitorVoid() {
        @OptIn(KspExperimental::class)
        override fun visitClassDeclaration(classDeclaration: KSClassDeclaration, data: Unit) {
            val moduleNames = mutableSetOf<String>()
            val metas: Sequence<KSDeclaration> =
                resolver.getDeclarationsFromPackage(NAME_MODULE_PACKAGE)
            val si = metas.iterator()
            while (si.hasNext()) {
                val it = si.next() as KSAnnotated
                val ksClazz = it as? KSClassDeclaration
                ksClazz?.qualifiedName?.asString()?.let {
                    moduleNames.add(it)
                }
            }
            val className = NAME_APP_NAME
            val packageName = NAME_APP_PACKAGE

            val fileContent = buildString {
                appendLine("package $packageName")
                appendLine()
                appendLine("import com.axon.dev.modulex.proxy.IAppProxy")
                appendLine("import com.axon.dev.modulex.proxy.IModuleProxy")
                appendLine()
                appendLine("class $className: IAppProxy() {")
                appendLine()
                appendLine("    override fun initModules(modules: MutableList<IModuleProxy>) {")
                moduleNames.forEach { moduleName ->
                    appendLine("        modules.add($moduleName())")
                }
                appendLine("    }")
                appendLine("}")
            }

            // 写入文件
            val generatedFile = environment.codeGenerator.createNewFile(
                Dependencies(true, classDeclaration.containingFile!!), packageName, className
            )
            generatedFile.write(fileContent.toByteArray())
            generatedFile.close()
        }
    }

    companion object {
        private const val NAME_MODULE_PACKAGE = "com.axon.dev.modulex.modpxy"
        private const val NAME_APP_PACKAGE = "com.axon.dev.modulex.apppxy"
        private const val NAME_APP_NAME = "com_axon_dev_modulex_AppProxy"
    }
}

internal class ModuleXProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        return ModuleXProcessor(environment)
    }

}