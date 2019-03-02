package cobalt.parsing.nodes

import cobalt.analysis.SymbolTable
import cobalt.parsing.nodes.statements.StatementNode

class CodeBlockNode(line:Int) : AstNode("CodeBlock", line) {

    override val definesScope = true

    val statements = mutableListOf<StatementNode>()

    fun addStatement(statement: StatementNode) {
        statements.add(statement)
        statement.parentNode = this
    }

    override fun toString(): String {
        return "$type\n+-${statements.joinToString("\n+-")}"
    }

}