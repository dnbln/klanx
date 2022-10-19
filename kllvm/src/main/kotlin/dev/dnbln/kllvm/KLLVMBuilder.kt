package dev.dnbln.kllvm

import org.bytedeco.javacpp.PointerPointer
import org.bytedeco.llvm.LLVM.*
import org.bytedeco.llvm.global.LLVM.*
import java.lang.ref.Cleaner

internal class BuilderDisposer(private val ref: LLVMBuilderRef, @HoldRef private val ctxRef: KLLVMContext? = null) : Runnable {
    override fun run() {
        println("Disposing builder")
        LLVMDisposeBuilder(ref)
    }
}

class KLLVMBuilder : AutoCloseable {
    @HoldRef
    private val ctxRef: KLLVMContext?
    internal val ref: LLVMBuilderRef

    private val cleanable: Cleaner.Cleanable

    constructor() {
        ctxRef = null
        ref = LLVMCreateBuilder()
        cleanable = cleaner.register(this, BuilderDisposer(ref))
    }

    constructor(ctxRef: KLLVMContext) {
        this.ctxRef = ctxRef
        ref = LLVMCreateBuilderInContext(ctxRef.ref)
        cleanable = cleaner.register(this, BuilderDisposer(ref, ctxRef))
    }

    override fun close() {
        cleanable.clean()
    }

    fun positionAtEnd(block: KLLVMBasicBlockRef) {
        LLVMPositionBuilderAtEnd(ref, block.ref)
    }

}

fun KLLVMBuilder.buildCmp(
    name: String,
    f: KLLVMCmpBuilder.() -> KLLVMCmpEvaluation
): KLLVMValueRef.KLLVMBooleanValueRef {
    val cmpEvaluation = f(KLLVMCmpBuilder())
    val ret = LLVMBuildICmp(ref, cmpEvaluation.predicate, cmpEvaluation.left.ref, cmpEvaluation.right.ref, name)

    return KLLVMValueRef.KLLVMBooleanValueRef(ret)
}

class KLLVMCmpBuilder {
    fun v(valueRef: KLLVMValueRef): KLLVMCmpValue = KLLVMCmpValue(valueRef)
}

class KLLVMCmpValue(private val valueRef: KLLVMValueRef) {
    private fun evaluation(predicate: LLVMIntPredicate, other: KLLVMCmpValue) =
        KLLVMCmpEvaluation(valueRef, predicate, other.valueRef)

    infix fun intEq(other: KLLVMCmpValue) = evaluation(LLVMIntEQ, other)
    infix fun intNe(other: KLLVMCmpValue) = evaluation(LLVMIntNE, other)
    infix fun intUgt(other: KLLVMCmpValue) = evaluation(LLVMIntUGT, other)
    infix fun intUge(other: KLLVMCmpValue) = evaluation(LLVMIntUGE, other)
    infix fun intUlt(other: KLLVMCmpValue) = evaluation(LLVMIntULT, other)
    infix fun intUle(other: KLLVMCmpValue) = evaluation(LLVMIntULE, other)
    infix fun intSgt(other: KLLVMCmpValue) = evaluation(LLVMIntSGT, other)
    infix fun intSge(other: KLLVMCmpValue) = evaluation(LLVMIntSGE, other)
    infix fun intSlt(other: KLLVMCmpValue) = evaluation(LLVMIntSLT, other)
    infix fun intSle(other: KLLVMCmpValue) = evaluation(LLVMIntSLE, other)
}

typealias LLVMIntPredicate = Int

class KLLVMCmpEvaluation(val left: KLLVMValueRef, val predicate: LLVMIntPredicate, val right: KLLVMValueRef)

fun KLLVMBuilder.buildCondBr(
    If: KLLVMValueRef.KLLVMBooleanValueRef,
    Then: KLLVMBasicBlockRef,
    Else: KLLVMBasicBlockRef
) {
    LLVMBuildCondBr(ref, If.ref, Then.ref, Else.ref)
}

fun KLLVMBuilder.buildSub(left: KLLVMValueRef, right: KLLVMValueRef, name: String): KLLVMValueRef =
    KLLVMValueRef(LLVMBuildSub(ref, left.ref, right.ref, name))

fun KLLVMBuilder.buildCall(
    target: KLLVMValueRef.KLLVMFnValueRef,
    args: List<KLLVMValueRef>,
    name: String
): KLLVMValueRef =
    PointerPointer(*args.toTypedArray()).use { argsPtr ->
        KLLVMValueRef(LLVMBuildCall2(ref, target.funTy.ref, target.ref, argsPtr, args.size, name))
    }


fun KLLVMBuilder.buildMul(left: KLLVMValueRef, right: KLLVMValueRef, name: String): KLLVMValueRef =
    KLLVMValueRef(LLVMBuildMul(ref, left.ref, right.ref, name))

fun KLLVMBuilder.buildBr(to: KLLVMBasicBlockRef) {
    LLVMBuildBr(ref, to.ref)
}

fun KLLVMBuilder.buildPhi(
    ty: KLLVMTypeRef,
    name: String,
    size: Int = 0,
    f: PhiBuilder.() -> Unit
): KLLVMValueRef.KLLVMPhiValueRef {
    val phi = KLLVMValueRef.KLLVMPhiValueRef(LLVMBuildPhi(ref, ty.ref, name))

    val phiTable = when (size) {
        0 -> PhiBuilderUnknownSize().apply(f).build()
        else -> PhiBuilderKnownSize(size).apply(f).build()
    }

    phiTable.lower().use { loweredPhiTable ->
        phi.addIncoming(loweredPhiTable)
    }


    return phi
}

internal fun KLLVMValueRef.KLLVMPhiValueRef.addIncoming(loweredPhiTable: LoweredPhiTable) {
    LLVMAddIncoming(ref, loweredPhiTable.values, loweredPhiTable.blocks, loweredPhiTable.size)
}

class Phi {
    lateinit var block: KLLVMBasicBlockRef
    lateinit var value: KLLVMValueRef
}

interface PhiBuilder {
    fun phi(f: Phi.() -> Unit)
}

fun PhiBuilder.phi(block: KLLVMBasicBlockRef): PhiBuilderTemp = PhiBuilderTemp(this, block)

class PhiBuilderTemp(private val builder: PhiBuilder, private val block: KLLVMBasicBlockRef) {
    infix fun then(value: KLLVMValueRef) {
        builder.phi {
            this.block = this@PhiBuilderTemp.block
            this.value = value
        }
    }
}

internal class PhiTable(private val size: Int, private val values: List<KLLVMValueRef>, private val blocks: List<KLLVMBasicBlockRef>) {
    fun lower(): LoweredPhiTable {
        val loweredValues = PointerPointer(*values.toTypedArray())
        val loweredBlocks = PointerPointer(*blocks.toTypedArray())

        return LoweredPhiTable(size, loweredValues, loweredBlocks)
    }
}

internal class LoweredPhiTable(
    val size: Int,
    val values: PointerPointer<KLLVMValueRef>,
    val blocks: PointerPointer<KLLVMBasicBlockRef>
) : AutoCloseable {
    override fun close() {
        values.close()
        blocks.close()
    }
}

private fun buildPhiTable(size: Int, iter: Iterator<Phi>): PhiTable {
    val (values, blocks) = iter.asSequence().map { it.value to it.block }.unzip()

    return PhiTable(size, values, blocks)
}

internal class PhiBuilderKnownSize(private val size: Int) : PhiBuilder {
    private val phis = arrayOfNulls<Phi>(size)
    private var currentPtr = 0

    override fun phi(f: Phi.() -> Unit) {
        if (currentPtr >= size) error("Tried to store more phis than there was space for, increase the size")

        val phi = Phi().apply(f)

        phis[currentPtr++] = phi
    }

    fun build(): PhiTable {
        if (currentPtr != size) error("Phi table not filled, reduce size")

        @Suppress("UNCHECKED_CAST") // filled whole array with values, it's safe to cast
        return buildPhiTable(size, (phis as Array<Phi>).iterator())
    }
}

internal class PhiBuilderUnknownSize : PhiBuilder {
    private val phis = mutableListOf<Phi>()

    override fun phi(f: Phi.() -> Unit) {
        val phi = Phi().apply(f)
        phis.add(phi)
    }

    fun build(): PhiTable {
        return buildPhiTable(phis.size, phis.iterator())
    }
}

fun KLLVMBuilder.buildRet(value: KLLVMValueRef) {
    KLLVMValueRef(LLVMBuildRet(ref, value.ref))
}

fun KLLVMBuilder.buildRet() {
    LLVMBuildRetVoid(ref)
}

fun <T> KLLVMBuilder.buildBlock(block: KLLVMBasicBlockRef, f: KLLVMBuilder.() -> T): T {
    positionAtEnd(block)

    return f(this)
}