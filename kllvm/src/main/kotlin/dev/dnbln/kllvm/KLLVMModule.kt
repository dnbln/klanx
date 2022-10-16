package dev.dnbln.kllvm

import org.bytedeco.llvm.LLVM.LLVMModuleRef
import org.bytedeco.llvm.global.LLVM.*
import java.lang.ref.Cleaner

internal class ModuleDisposer(private val ref: LLVMModuleRef): Runnable {
    override fun run() {
        LLVMDisposeModule(ref)
    }
}

class KLLVMModule {
    @HoldRef
    private val ctxRef: KLLVMContext?

    internal val ref: LLVMModuleRef
    private val cleanable: Cleaner.Cleanable

    constructor(name: String, ctxRef: KLLVMContext) {
        ref = LLVMModuleCreateWithNameInContext(name, ctxRef.ref)
        this.ctxRef = ctxRef

        cleanable = cleaner.register(this, ModuleDisposer(ref))
    }

    constructor(name: String) {
        ref = LLVMModuleCreateWithName(name)
        ctxRef = null

        cleanable = cleaner.register(this, ModuleDisposer(ref))
    }

    fun addFunction(name: String, funTy: KLLVMTypeRef.KLLVMFunTypeRef): KLLVMValueRef.KLLVMFnValueRef {
        return KLLVMValueRef.KLLVMFnValueRef(LLVMAddFunction(ref, name, funTy.ref), funTy)
    }
}