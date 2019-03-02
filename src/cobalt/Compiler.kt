package cobalt

import cobalt.codegen.ICodeGenerator
import cobalt.lexing.Lexer
import cobalt.optimization.Optimizer
import cobalt.parsing.Parser

class Compiler(val generator:ICodeGenerator, val debug:Boolean) {

    private val lexer = Lexer()
    private val parser = Parser()
    private val optimizer = Optimizer()

    fun compile(code:String):String {

        //Lexical analysis
        val tokens = lexer.tokenize(code)

        //Syntax analysis
        val ast = parser.parse(tokens)

        //Semantic analysis
        ast.buildSymbolTables()

        //Optimization
        optimizer.optimize(ast)

        //Code generation
        return generator.generateCode(ast)

    }

}