package dev.dnbln.kllvm

import org.bytedeco.llvm.LLVM.LLVMPassRegistryRef
import org.bytedeco.llvm.global.LLVM

class KLLVMPassRegistry private constructor(internal val ref: LLVMPassRegistryRef) {
    companion object {
        val globalPassRegistry: KLLVMPassRegistry
            get() = KLLVMPassRegistry(LLVM.LLVMGetGlobalPassRegistry())
    }
}
