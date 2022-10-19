package dev.dnbln.kllvm

import org.bytedeco.llvm.LLVM.LLVMContextRef
import org.bytedeco.llvm.global.LLVM.*
import java.lang.ref.Cleaner

internal class ContextDisposer(private val context: LLVMContextRef) : Runnable {
    override fun run() {
        println("Disposing context")
        LLVMContextDispose(context)
    }
}

class KLLVMContext : AutoCloseable {
    private val cleanable: Cleaner.Cleanable
    internal val ref: LLVMContextRef = LLVMContextCreate()

    init {
        cleanable = cleaner.register(this, ContextDisposer(ref))
    }

    override fun close() {
        cleanable.clean()
    }
}

val KLLVMContext.newBuilder: KLLVMBuilder
    get() = KLLVMBuilder(this)

fun KLLVMContext.newModule(name: String): KLLVMModule = KLLVMModule(name, this)