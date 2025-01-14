package com.axon.dev.modulex.processor

import com.axon.dev.modulex.api.anno.App
import com.axon.dev.modulex.api.anno.Clazz
import com.axon.dev.modulex.api.anno.Service
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSVisitorVoid
import com.google.devtools.ksp.validate

class ModuleXProcessor(private val environment: SymbolProcessorEnvironment) : SymbolProcessor {

    private val

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val ret = mutableListOf<KSAnnotated>()
        process(ret, resolver, Clazz::class.qualifiedName!!, ClazzKSVisitor(environment))
        process(ret, resolver, Service::class.qualifiedName!!, ServiceKSVisitor(environment))
        process(ret, resolver, Module::class.qualifiedName!!, ModuleKSVisitor(environment))
        process(ret, resolver, App::class.qualifiedName!!, AppKSVisitor(environment))
        return ret
    }

    private fun process(
        items: MutableList<KSAnnotated>,
        resolver: Resolver, annotationName: String, visitor: KSVisitorVoid
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
}

internal class ClazzKSVisitor(private val environment: SymbolProcessorEnvironment) :
    KSVisitorVoid() {
    override fun visitClassDeclaration(classDeclaration: KSClassDeclaration, data: Unit) {
        val packageName = classDeclaration.containingFile!!.packageName.asString()//获取这个类的包名
        val originalClassName = classDeclaration.simpleName.asString()//获取类名
        val className = "ModuleX_${originalClassName}"
        val file = environment.codeGenerator.createNewFile(//创建新的文件(默认.kt)
            Dependencies(
                true, classDeclaration.containingFile!!
            ), packageName, className
        )
        file.write("package $packageName\n\n".toByteArray())//写入文件
        file.write("class $className {\n".toByteArray())
        file.write("    val fields = ".toByteArray())
        val fields = classDeclaration.getAllProperties().map {//遍历所有的属性
            val name = it.simpleName.getShortName()//属性名
            val type = it.type.resolve().toString()//属性类型
            "$name: $type"
        }.joinToString()
        file.write("\"$fields\"\n".toByteArray())
        file.write("}".toByteArray())
        file.close()
    }
}

internal class ServiceKSVisitor(private val environment: SymbolProcessorEnvironment) :
    KSVisitorVoid() {
    override fun visitClassDeclaration(classDeclaration: KSClassDeclaration, data: Unit) {
    }
}

internal class ModuleKSVisitor(private val environment: SymbolProcessorEnvironment) :
    KSVisitorVoid() {
    override fun visitClassDeclaration(classDeclaration: KSClassDeclaration, data: Unit) {
    }
}

internal class AppKSVisitor(private val environment: SymbolProcessorEnvironment) :
    KSVisitorVoid() {
    override fun visitClassDeclaration(classDeclaration: KSClassDeclaration, data: Unit) {
    }
}

internal class ModuleXProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        return ModuleXProcessor(environment)
    }

}