package dev.dnbln.kllvm

import dev.dnbln.kllvm.script.*
import org.bytedeco.javacpp.BytePointer
import org.bytedeco.llvm.global.LLVM.*

fun main(args: Array<String>) {
    println("Initializing")
    LLVMInitializeCore(LLVMGetGlobalPassRegistry())
    LLVMLinkInMCJIT()
    LLVMInitializeNativeAsmPrinter()
    LLVMInitializeNativeAsmParser()
    LLVMInitializeNativeTarget()

    val context = KLLVMContext()
    val builder = KLLVMBuilder(context)
    val module = KLLVMModule("factorial", context)

    val i32 = context.i32
    val factorialFunTy = buildFnTy { fn(i32) returns i32 }
    val factorial = module.addFunction("factorial", factorialFunTy)
    factorial.setCallConv(LLVMCCallConv)

    val n = factorial.param(0)
    val zero = KLLVMValueRef.constInt(i32, 0)
    val one = KLLVMValueRef.constInt(i32, 1)

    val entry by context.newBasicBlock(factorial)
    val ifFalse by context.newBasicBlock(factorial)
    val end by context.newBasicBlock(factorial)

    builder.positionAtEnd(entry)
    val nEquals0 by builder.buildCmp { v(n) intEq v(zero) }
    builder.conditionalBranch(If = nEquals0, Then = end, Else = ifFalse)

    builder.positionAtEnd(ifFalse)
    val nMinusOne by builder.buildSub(n, one)
    val factorialArgs = listOf(nMinusOne)

    val recursiveResult by builder.buildCall(factorial, factorialArgs)
    val resultIfFalse by builder.buildMul(n, recursiveResult)

    builder.buildBr(end)

    builder.positionAtEnd(end)

    val result by builder.buildPhi(i32) {
        phi {
            value = one
            block = entry
        }
        phi {
            value = resultIfFalse
            block = ifFalse
        }
    }

    builder.buildRet(result)

    val error = BytePointer()

    if (LLVMVerifyModule(module.ref, LLVMPrintMessageAction, error) != 0) {
        LLVMDisposeMessage(error)
        return
    }

    // Stage 4: Create a pass pipeline using the legacy pass manager
    val pm = LLVMCreatePassManager()
    LLVMAddAggressiveInstCombinerPass(pm)
    LLVMAddNewGVNPass(pm)
    LLVMAddCFGSimplificationPass(pm)
    LLVMRunPassManager(pm, module.ref)
    LLVMDumpModule(module.ref)

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