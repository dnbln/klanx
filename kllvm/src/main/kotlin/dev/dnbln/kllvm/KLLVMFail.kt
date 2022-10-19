package dev.dnbln.kllvm

class KLLVMFail(val retCode: Int) : Exception("LLVM failed, returned code $retCode")