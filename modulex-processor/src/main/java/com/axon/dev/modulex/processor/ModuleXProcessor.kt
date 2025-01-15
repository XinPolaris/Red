package com.axon.dev.modulex.processor

import com.axon.dev.modulex.api.anno.App
import com.axon.dev.modulex.api.anno.Clazz
import com.axon.dev.modulex.api.anno.Module
import com.axon.dev.modulex.api.anno.Service
import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.getVisibility
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSVisitorVoid
import com.google.devtools.ksp.symbol.Visibility

class ModuleXProcessor(private val environment: SymbolProcessorEnvironment) : SymbolProcessor {

    private lateinit var resolver: Resolver
    private val serviceImplSet = mutableSetOf<KSClassDeclaration>()
    private val clazzMap = mutableMapOf<String, KSClassDeclaration>()

    override fun process(resolver: Resolver): List<KSAnnotated> {
        this.resolver = resolver
        processClazz()
        processService()
        processModuleProxy()
        processAppProxy()
        return listOf()
    }

    private fun processClazz() {
        val symbols = resolver.getSymbolsWithAnnotation(Clazz::class.qualifiedName!!)
        symbols.toList().forEach { declaration ->
            if (declaration is KSClassDeclaration) {
                // 获取注解参数
                val clazzAnnotation =
                    declaration.annotations.firstOrNull { it.shortName.asString() == Clazz::class.simpleName }

                // 获取uri参数
                val uriValue =
                    clazzAnnotation?.arguments?.firstOrNull { it.name?.asString() == "uri" }?.value as? String
                if (uriValue.isNullOrEmpty()) {
                    throw RuntimeException("@Clazz's uri parameter must be non-empty and unique (Location: ${declaration.qualifiedName?.asString()})")
                }
                // 执行你的访问器
                declaration.accept(ClazzKSVisitor(uriValue), Unit)
            }
        }
    }

    private fun processService() {
        val symbols = resolver.getSymbolsWithAnnotation(Service::class.qualifiedName!!)
        symbols.toList().forEach { declaration ->
            declaration.accept(ServiceKSVisitor(), Unit)
        }
    }

    private fun processModuleProxy() {
        val symbols = resolver.getSymbolsWithAnnotation(Module::class.qualifiedName!!)
        val annotatedClass = symbols.toList()
        if (annotatedClass.size > 1) {
            throw RuntimeException("Only one class can have the @Module annotation in a component")
        }
        annotatedClass.forEach { declaration ->
            declaration.accept(ModuleKSVisitor(), Unit)
        }
    }

    private fun processAppProxy() {
        val symbols = resolver.getSymbolsWithAnnotation(App::class.qualifiedName!!)
        val annotatedClass = symbols.toList()
        if (annotatedClass.size > 1) {
            throw RuntimeException("Only one class can have the @App annotation in a component")
        }
        annotatedClass.forEach { declaration ->
            declaration.accept(AppKSVisitor(), Unit)
        }
    }

    inner class ClazzKSVisitor(private val uri: String) : KSVisitorVoid() {
        override fun visitClassDeclaration(classDeclaration: KSClassDeclaration, data: Unit) {
            clazzMap[uri] = classDeclaration
        }
    }

    inner class ServiceKSVisitor : KSVisitorVoid() {
        override fun visitClassDeclaration(classDeclaration: KSClassDeclaration, data: Unit) {
            // 检查类是否是 public
            if (classDeclaration.getVisibility() != Visibility.PUBLIC) {
                throw RuntimeException("@Service annotated class (${classDeclaration.simpleName.asString()}) must be public.")
            }
            // 检查类是否实现了多个接口或者没有实现接口
            val interfaces =
                classDeclaration.superTypes.filter { superType -> (superType.resolve().declaration as? KSClassDeclaration)?.classKind == ClassKind.INTERFACE }
                    .toList()

            if (interfaces.isEmpty()) {
                throw RuntimeException("@Service annotated class (${classDeclaration.simpleName.asString()}) must implement one interface, which is typically exposed to other components.")
            }

            if (interfaces.size > 1) {
                throw RuntimeException("@Service annotated class (${classDeclaration.simpleName.asString()}) cannot implement multiple interfaces.")
            }
            serviceImplSet.add(classDeclaration)
        }
    }

    inner class ModuleKSVisitor : KSVisitorVoid() {
        override fun visitClassDeclaration(classDeclaration: KSClassDeclaration, data: Unit) {
            // 检查类是否是 public
            if (classDeclaration.getVisibility() != Visibility.PUBLIC) {
                throw RuntimeException("@Module annotated class (${classDeclaration.qualifiedName?.asString()}) must be public.")
            }
            if (!classDeclaration.superTypes.any {
                    it.resolve().declaration.qualifiedName?.asString() == "com.axon.dev.modulex.api.IModule"
                }) {
                throw RuntimeException("@Module annotated class (${classDeclaration.qualifiedName?.asString()}) must implement the IModule interface!")
            }
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
                appendLine()
                // 生成initClazz方法
                appendLine("    override fun initClazz(clazzMap: MutableMap<String, Class<*>>) {")
                clazzMap.entries.forEach { entry ->
                    appendLine("        clazzMap[\"${entry.key}\"] = ${entry.value.qualifiedName!!.asString()}::class.java")
                }
                appendLine("    }")
                appendLine()
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

    inner class AppKSVisitor : KSVisitorVoid() {
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