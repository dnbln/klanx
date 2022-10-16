package dev.dnbln.kllvm

import org.bytedeco.llvm.global.LLVM.*

val KLLVMContext.i32: KLLVMTypeRef
    get() = KLLVMTypeRef(LLVMInt32TypeInContext(ref), this)
val KLLVMContext.void: KLLVMTypeRef.KLLVMVoidTypeRef
    get() = KLLVMTypeRef.KLLVMVoidTypeRef(LLVMVoidTypeInContext(ref), this)
