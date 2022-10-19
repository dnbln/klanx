package dev.dnbln.kllvm

import dev.dnbln.kllvm.script.*
import org.bytedeco.llvm.global.LLVM
import org.bytedeco.llvm.global.LLVM.*

fun main(args: Array<String>) {
    println("Initializing")
    initKLLVM {
        core(globalPassRegistry)
        linkInMCJIT

        nativeAsmPrinter
        nativeAsmParser
        nativeTarget
    }

    val context = KLLVMContext()
    val builder = context.newBuilder
    val module = context.newModule("factorial")

    val i32 = context.i32
    val factorialFunTy = buildFnTy { fn(i32) returns i32 }
    val factorial = module.addFunction("factorial", factorialFunTy) {
        callConv = LLVMCCallConv
    }

    val n = factorial.param(0)
    val zero = KLLVMValueRef.constInt(i32, 0)
    val one = KLLVMValueRef.constInt(i32, 1)

    val entry by context.newBasicBlock(factorial)
    val ifFalse by context.newBasicBlock(factorial)
    val end by context.newBasicBlock(factorial)

    builder.buildBlock(entry) {
        val nEquals0 by buildCmp { v(n) intEq v(zero) }
        buildCondBr(If = nEquals0, Then = end, Else = ifFalse)
    }

    val resultIfFalse = builder.buildBlock(ifFalse) {
        val nMinusOne by buildSub(n, one)
        val recursiveResult by buildCall(factorial, listOf(nMinusOne))
        val resultIfFalse by buildMul(n, recursiveResult)

        buildBr(end)

        resultIfFalse
    }

    builder.buildBlock(end) {
        val result by buildPhi(i32) {
            phi(entry) then one
            phi(ifFalse) then resultIfFalse
        }

        buildRet(result)
    }

    module.verify(LLVMPrintMessageAction) { _, _ ->
        error("verification failed")
    }

    module.dump()

    // Stage 4: Create a pass pipeline using the legacy pass manager
    KLLVMPassManager.setupAndRun {
        configure {
            aggressiveInstCombiner
            gvnPass
            cfgSimplificationPass
        }

        runPassManager(module)
    }

    module.dump()

    module.writeBitcodeToFile("x.bc")

    //// Stage 5: Execute the code using MCJIT
    //LLVMExecutionEngineRef engine = new LLVMExecutionEngineRef();
    //LLVMMCJITCompilerOptions options = new LLVMMCJITCompilerOptions();
    //if (LLVMCreateMCJITCompilerForModule(engine, module, options, 3, error) != 0) {
    //    System.err.println("Failed to create JIT compiler: " + error.getString());
    //    LLVMDisposeMessage(error);
    //    return;
    //}
    //
    //LLVMGenericValueRef argument = LLVMCreateGenericValueOfInt(i32Type, 10, /* signExtend */ 0);
    //LLVMGenericValueRef result = LLVMRunFunction(engine, factorial, /* argumentCount */ 1, argument);
    //System.out.println();
    //System.out.println("; Running factorial(10) with MCJIT...");
    //System.out.println("; Result: " + LLVMGenericValueToInt(result, /* signExtend */ 0));

    println("Closing")
}