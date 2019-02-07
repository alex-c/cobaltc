package cobalt

import cobalt.lexing.Lexer
import cobalt.parsing.Parser

class Compiler {

    private val Lexer:Lexer = Lexer()
    private val Parser:Parser = Parser()

    fun compile(code:String) {

        val tokens = Lexer.tokenize(code)
        val ast = Parser.parse(tokens)
        //TODO: optimize AST
        //TODO: generate code

    }

}