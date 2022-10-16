package dev.dnbln.kllvm

import org.bytedeco.llvm.LLVM.LLVMPassManagerRef
import org.bytedeco.llvm.global.LLVM.*
import java.lang.ref.Cleaner

internal class PassManagerDisposer(private val ref: LLVMPassManagerRef): Runnable {
    override fun run() {
        LLVMDisposePassManager(ref)
    }
}

class KLLVMPassManager: AutoCloseable {
    internal val ref: LLVMPassManagerRef = LLVMCreatePassManager()

    private val cleanable: Cleaner.Cleanable = cleaner.register(this, PassManagerDisposer(ref))

    override fun close() {
        cleanable.clean()
    }
}

fun KLLVMPassManager.addAggressiveInstCombinerPass() {
    LLVMAddAggressiveInstCombinerPass(ref)
}

fun KLLVMPassManager.addNewGVNPass() {
    LLVMAddNewGVNPass(ref)
}

fun KLLVMPassManager.addCFGSimplificationPass() {
    LLVMAddCFGSimplificationPass(ref)
}

fun KLLVMPassManager.runPassManager(module: KLLVMModule) {
    LLVMRunPassManager(ref, module.ref)
}