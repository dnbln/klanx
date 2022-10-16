package dev.dnbln.kllvm.script

import dev.dnbln.kllvm.*
import kotlin.properties.PropertyDelegateProvider
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

class BuilderDelegateProvider<T>(private val f: (name: String) -> T) :
    PropertyDelegateProvider<Any?, BuilderDelegate<T>> {
    override fun provideDelegate(thisRef: Any?, property: KProperty<*>): BuilderDelegate<T> =
        BuilderDelegate(f(property.name))
        }

class BuilderDelegate<T>(private val value: T) : ReadOnlyProperty<Any?, T> {
    override fun getValue(thisRef: Any?, property: KProperty<*>): T = value
}

fun KLLVMBuilder.buildCmp(f: KLLVMCmpBuilder.() -> KLLVMCmpEvaluation) =
    BuilderDelegateProvider { name -> buildCmp(name, f) }

fun KLLVMBuilder.buildSub(left: KLLVMValueRef, right: KLLVMValueRef) =
    BuilderDelegateProvider { name -> buildSub(left, right, name) }

fun KLLVMBuilder.buildCall(
    target: KLLVMValueRef.KLLVMFnValueRef,
    args: List<KLLVMValueRef>,
    ) = BuilderDelegateProvider { name -> buildCall(target, args, name) }

fun KLLVMBuilder.buildMul(left: KLLVMValueRef, right: KLLVMValueRef) =
    BuilderDelegateProvider { name -> buildMul(left, right, name) }

fun KLLVMBuilder.buildPhi(
    ty: KLLVMTypeRef,
    size: Int = 0,
    f: PhiBuilder.() -> Unit
) = BuilderDelegateProvider { name -> buildPhi(ty, name, size, f) }


