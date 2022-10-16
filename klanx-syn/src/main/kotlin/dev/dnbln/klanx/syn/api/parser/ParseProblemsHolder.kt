package dev.dnbln.klanx.syn.api.parser

class ParseProblemsHolder {
    private val problems: MutableList<ParseProblem> = mutableListOf()
    
    fun add(problem: ParseProblem) {
        problems.add(problem)
    }
}
