package dev.dnbln.kllvm

import org.bytedeco.llvm.global.LLVM.*

fun initKLLVM(f: KLLVMInitializer.() -> Unit) {
    KLLVMInitializer().apply(f).initialize()
}

class KLLVMInitializer {
    private val components = mutableListOf<KLLVMInitializerComponent>()

    internal fun addComponent(component: KLLVMInitializerComponent) {
        components.add(component)
    }

    internal fun initialize() {
        components.forEach(KLLVMInitializerComponent::initComponent)
    }
}

internal sealed class KLLVMInitializerComponent {
    internal abstract fun initComponent()

    internal class Core(private val passRegistry: KLLVMPassRegistry) : KLLVMInitializerComponent() {
        override fun initComponent() {
            LLVMInitializeCore(passRegistry.ref)
        }
    }

    internal object LinkInMCJIT: KLLVMInitializerComponent() {
        override fun initComponent() {
            LLVMLinkInMCJIT()
        }
    }

    internal object NativeAsmPrinter: KLLVMInitializerComponent() {
        override fun initComponent() {
            LLVMInitializeNativeAsmPrinter()
        }
    }

    internal object NativeAsmParser: KLLVMInitializerComponent() {
        override fun initComponent() {
            LLVMInitializeNativeAsmParser()
        }
    }

    internal object NativeTarget: KLLVMInitializerComponent() {
        override fun initComponent() {
            LLVMInitializeNativeTarget()
        }
    }
}

val KLLVMInitializer.globalPassRegistry: KLLVMPassRegistry
        by KLLVMPassRegistry.Companion::globalPassRegistry

fun KLLVMInitializer.core(passRegistry: KLLVMPassRegistry) =
    addComponent(KLLVMInitializerComponent.Core(passRegistry))

val KLLVMInitializer.linkInMCJIT: Unit
    get() = addComponent(KLLVMInitializerComponent.LinkInMCJIT)

val KLLVMInitializer.nativeAsmPrinter: Unit
    get() = addComponent(KLLVMInitializerComponent.NativeAsmPrinter)

val KLLVMInitializer.nativeAsmParser: Unit
    get() = addComponent(KLLVMInitializerComponent.NativeAsmParser)

val KLLVMInitializer.nativeTarget: Unit
    get() = addComponent(KLLVMInitializerComponent.NativeTarget)
