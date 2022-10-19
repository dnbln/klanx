package dev.dnbln.kllvm

import org.bytedeco.javacpp.BytePointer
import org.bytedeco.llvm.LLVM.LLVMModuleRef
import org.bytedeco.llvm.global.LLVM.*
import java.lang.ref.Cleaner

internal class ModuleDisposer(private val ref: LLVMModuleRef) : Runnable {
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
}

fun KLLVMModule.addFunction(
    name: String,
    funTy: KLLVMTypeRef.KLLVMFnTypeRef,
    f: KLLVMFnConfigurator.() -> Unit = {}
): KLLVMValueRef.KLLVMFnValueRef {
    val value = KLLVMValueRef.KLLVMFnValueRef(LLVMAddFunction(ref, name, funTy.ref), funTy)

    f(KLLVMFnConfigurator(value))

    return value
}

class KLLVMFnConfigurator(private val fn: KLLVMValueRef.KLLVMFnValueRef) {
    var callConv: KLLVMCallConv = 0
        set(value) {
            fn.setCallConv(value)

            field = value
        }
}

fun KLLVMModule.dump() {
    LLVMDumpModule(ref)
}

fun KLLVMModule.verify(verifierFailureAction: Int, f: (Int, BytePointer) -> Unit) {
    val error = BytePointer()

    LLVMVerifyModule(ref, verifierFailureAction, error)
        .takeUnless { it == 0 }
        ?.let { f(it, error) }
}

fun KLLVMModule.writeBitcodeToFile(path: String) {
    LLVMWriteBitcodeToFile(ref, path)
        .takeUnless { it == 0 }
        ?.let { throw KLLVMFail(it) }
}

