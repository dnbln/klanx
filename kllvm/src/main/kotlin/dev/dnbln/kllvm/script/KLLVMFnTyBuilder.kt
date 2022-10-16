package dev.dnbln.kllvm.script

import dev.dnbln.kllvm.KLLVMTypeRef

fun buildFnTy(f: FunTyBuilder.() -> Unit): KLLVMTypeRef.KLLVMFunTypeRef {
    val builder = FunTyBuilder()

    f(builder)

    return KLLVMTypeRef.function(builder.returnType, builder.params, builder.varargs)
}

class FunTyBuilder internal constructor() {
    var returnType: KLLVMTypeRef = KLLVMTypeRef.KLLVMVoidTy
    var varargs: Boolean = false
    var params: List<KLLVMTypeRef> = listOf()

    fun fn(vararg params: KLLVMTypeRef): FunTyBuilder = apply {
        this.params = params.toList()
    }

    infix fun returns(returnType: KLLVMTypeRef): FunTyBuilder = apply {
        this.returnType = returnType
    }

    infix fun varargs(varargs: Boolean): FunTyBuilder = apply {
        this.varargs = varargs
    }
}

