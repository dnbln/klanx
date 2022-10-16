package dev.dnbln.kllvm

internal val Boolean.LLVMBool: Int
    get() = if (this) 1 else 0