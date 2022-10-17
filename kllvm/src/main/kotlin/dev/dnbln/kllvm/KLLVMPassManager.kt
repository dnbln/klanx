package dev.dnbln.kllvm

import org.bytedeco.llvm.LLVM.LLVMPassManagerRef
import org.bytedeco.llvm.global.LLVM.*
import java.lang.ref.Cleaner

internal class PassManagerDisposer(private val ref: LLVMPassManagerRef) : Runnable {
    override fun run() {
        LLVMDisposePassManager(ref)
    }
}

class KLLVMPassManager : AutoCloseable {
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

fun KLLVMPassManager.configure(f: KLLVMPassManagerInitializer.() -> Unit) {
    KLLVMPassManagerInitializer()
        .apply(f)
        .init(this)
}

sealed class KLLVMPass(val initializer: KLLVMPassManager.() -> Unit) {
    object AggressiveInstCombinerPass : KLLVMPass(KLLVMPassManager::addAggressiveInstCombinerPass)
    object NewGVNPass : KLLVMPass(KLLVMPassManager::addNewGVNPass)
    object CFGSimplificationPass : KLLVMPass(KLLVMPassManager::addCFGSimplificationPass)
}

class KLLVMPassManagerInitializer {
    private val passes = mutableListOf<KLLVMPass>()

    internal fun addPass(pass: KLLVMPass) {
        passes.add(pass)
    }

    internal fun init(manager: KLLVMPassManager) {
        passes.forEach { it.initializer(manager) }
    }
}

val KLLVMPassManagerInitializer.aggressiveInstCombiner: Unit
    get() = addPass(KLLVMPass.AggressiveInstCombinerPass)

val KLLVMPassManagerInitializer.gvnPass: Unit
    get() = addPass(KLLVMPass.NewGVNPass)

val KLLVMPassManagerInitializer.cfgSimplificationPass: Unit
    get() = addPass(KLLVMPass.CFGSimplificationPass)
