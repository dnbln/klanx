package dev.dnbln.kllvm

import org.bytedeco.javacpp.Pointer
import org.bytedeco.llvm.LLVM.LLVMBasicBlockRef
import org.bytedeco.llvm.global.LLVM.*
import kotlin.properties.PropertyDelegateProvider
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

fun KLLVMContext.newBasicBlock(function: KLLVMValueRef.KLLVMFnValueRef, name: String): KLLVMBasicBlockRef =
    KLLVMBasicBlockRef(LLVMAppendBasicBlockInContext(ref, function.ref, name), this)

fun KLLVMContext.newBasicBlock(function: KLLVMValueRef.KLLVMFnValueRef): KLLVMBasicBlockRefDelegateFactory =
    KLLVMBasicBlockRefDelegateFactory(this, function)

class KLLVMBasicBlockRefDelegateFactory(
    private val context: KLLVMContext,
    private val function: KLLVMValueRef.KLLVMFnValueRef
) : PropertyDelegateProvider<Any?, KLLVMBasicBlockRefDelegate> {
    override fun provideDelegate(thisRef: Any?, property: KProperty<*>): KLLVMBasicBlockRefDelegate =
        KLLVMBasicBlockRefDelegate(context.newBasicBlock(function, property.name))
}

class KLLVMBasicBlockRefDelegate(private val blockRef: KLLVMBasicBlockRef) :
    ReadOnlyProperty<Any?, KLLVMBasicBlockRef> {
    override fun getValue(thisRef: Any?, property: KProperty<*>): KLLVMBasicBlockRef = blockRef
}

class KLLVMBasicBlockRef internal constructor(
    internal val ref: LLVMBasicBlockRef,
    @HoldRef private val ctxRef: KLLVMContext? = null
): Pointer(ref)
