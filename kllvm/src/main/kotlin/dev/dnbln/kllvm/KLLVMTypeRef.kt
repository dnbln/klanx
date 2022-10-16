package dev.dnbln.kllvm

import org.bytedeco.javacpp.PointerPointer
import org.bytedeco.llvm.LLVM.LLVMTypeRef
import org.bytedeco.llvm.global.LLVM

open class KLLVMTypeRef internal constructor(internal val ref: LLVMTypeRef, @HoldRef private val ctxRef: KLLVMContext? = null) {
    class KLLVMFunTypeRef internal constructor(ref: LLVMTypeRef, ctxRef: KLLVMContext? = null) : KLLVMTypeRef(ref, ctxRef)
    open class KLLVMVoidTypeRef internal constructor(ref: LLVMTypeRef, ctxRef: KLLVMContext? = null): KLLVMTypeRef(ref, ctxRef)

    object KLLVMVoidTy: KLLVMVoidTypeRef(LLVM.LLVMVoidType())

    companion object {
        fun function(returnType: KLLVMTypeRef, param: KLLVMTypeRef, isVararg: Boolean): KLLVMFunTypeRef {
            return KLLVMFunTypeRef(LLVM.LLVMFunctionType(returnType.ref, param.ref, 1, isVararg.LLVMBool))
        }

        fun function(returnType: KLLVMTypeRef, params: List<KLLVMTypeRef>, isVararg: Boolean): KLLVMFunTypeRef =
            PointerPointer(*params.map(KLLVMTypeRef::ref).toTypedArray()).use { paramsPtr ->
                KLLVMFunTypeRef(LLVM.LLVMFunctionType(returnType.ref, paramsPtr, params.size, isVararg.LLVMBool))
            }
    }
}
