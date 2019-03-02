package cobalt.codegen

import cobalt.parsing.nodes.AstNode

interface ICodeGenerator {

    fun generateCode(ast: AstNode):String

}