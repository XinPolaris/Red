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

class ModuleXProcessor(private val environment: SymbolProcessorEnvironment) : SymbolProcessor {

    private val serviceImplSet = mutableSetOf<KSClassDeclaration>()
    private val clazzSet = mutableSetOf<KSClassDeclaration>()

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val ret = mutableListOf<KSAnnotated>()
        process(resolver, Clazz::class.qualifiedName!!, ClazzKSVisitor())
        process(resolver, Service::class.qualifiedName!!, ServiceKSVisitor())
        process(resolver, Module::class.qualifiedName!!, ModuleKSVisitor(environment))
        process(resolver, App::class.qualifiedName!!, AppKSVisitor(resolver, environment))
        return ret
    }

    private fun process(
        resolver: Resolver,
        annotationName: String,
        visitor: KSVisitorVoid
    ) {
        val symbols = resolver.getSymbolsWithAnnotation(annotationName)
        symbols.toList().forEach { declaration ->
            if (declaration !is KSClassDeclaration) {
                val qualifiedName =
                    (declaration as? KSDeclaration)?.qualifiedName?.asString() ?: "Unknown"
                throw IllegalArgumentException("Expected a class, but found: $qualifiedName.($annotationName)")
            }
            declaration.accept(visitor, Unit)
        }
    }

    inner class ClazzKSVisitor : KSVisitorVoid() {
        override fun visitClassDeclaration(classDeclaration: KSClassDeclaration, data: Unit) {
            clazzSet.add(classDeclaration)
        }
    }

    inner class ServiceKSVisitor : KSVisitorVoid() {
        override fun visitClassDeclaration(classDeclaration: KSClassDeclaration, data: Unit) {
            serviceImplSet.add(classDeclaration)
        }
    }

    inner class ModuleKSVisitor(private val environment: SymbolProcessorEnvironment) :
        KSVisitorVoid() {
        override fun visitClassDeclaration(classDeclaration: KSClassDeclaration, data: Unit) {
            // 获取类名和包名
            val className = classDeclaration.simpleName.asString()
            val packageName = classDeclaration.packageName.asString()

            // 生成代理类名
            val proxyClassName = "${packageName.replace('.', '_')}_${className}Proxy"
            val proxyClassPackageName = NAME_MODULE_PACKAGE

            // 创建 ModuleProxy 实现类的代码
            val fileContent = buildString {
                // 包名
                appendLine("package $proxyClassPackageName")
                appendLine()
                // 导入语句
                appendLine("import android.app.Application")
                appendLine("import com.axon.dev.modulex.proxy.Creator")
                appendLine("import com.axon.dev.modulex.proxy.ModuleProxy")
                appendLine()
                // 生成类声明
                appendLine("class $proxyClassName : ModuleProxy() {")
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
                serviceImplSet.forEach { serviceImpl ->
                    val superType = serviceImpl.superTypes.firstOrNull()?.resolve()?.declaration
                    superType?.qualifiedName?.asString()?.let { serviceFullName ->
                        appendLine("        services[${serviceFullName}::class.java] = object : Creator<$serviceFullName> {")
                        appendLine("            override fun create(): $serviceFullName = ${serviceImpl.qualifiedName?.asString()}()")
                        appendLine("        }")
                    }
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
            val si = resolver.getDeclarationsFromPackage(NAME_MODULE_PACKAGE).iterator()
            while (si.hasNext()) {
                val ksClazz = si.next() as? KSClassDeclaration
                ksClazz?.qualifiedName?.asString()?.let {
                    moduleNames.add(it)
                }
            }
            val proxyClassName = NAME_APP_NAME
            val proxyClassPackageName = NAME_APP_PACKAGE

            val fileContent = buildString {
                appendLine("package $proxyClassPackageName")
                appendLine()
                appendLine("import com.axon.dev.modulex.proxy.AppProxy")
                appendLine("import com.axon.dev.modulex.proxy.ModuleProxy")
                appendLine()
                appendLine("class $proxyClassName: AppProxy() {")
                appendLine()
                appendLine("    override fun initModules(modules: MutableList<ModuleProxy>) {")
                moduleNames.forEach { moduleName ->
                    appendLine("        modules.add($moduleName())")
                }
                appendLine("    }")
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