package dev.dnbln.kllvm

import org.bytedeco.llvm.LLVM.LLVMValueRef
import org.bytedeco.llvm.global.LLVM.*

open class KLLVMValueRef internal constructor(internal val ref: LLVMValueRef) {
    class KLLVMFnValueRef(ref: LLVMValueRef, internal val funTy: KLLVMTypeRef.KLLVMFunTypeRef) : KLLVMValueRef(ref) {
        fun setCallConv(conv: KLLVMCallConv) {
            LLVMSetFunctionCallConv(ref, conv)
        }

        fun param(i: Int): KLLVMValueRef = KLLVMValueRef(LLVMGetParam(ref, i))
    }

    class KLLVMConstValueRef(ref: LLVMValueRef): KLLVMValueRef(ref)

    class KLLVMConditionValueRef(ref: LLVMValueRef): KLLVMValueRef(ref)

    class KLLVMPhiValueRef(ref: LLVMValueRef): KLLVMValueRef(ref)

    companion object {
        fun constInt(type: KLLVMTypeRef, value: Long, signExtend: Boolean = false): KLLVMConstValueRef =
            KLLVMConstValueRef(LLVMConstInt(type.ref, value, signExtend.LLVMBool))
    }
}