package cobalt.parsing.nodes.statements

import cobalt.parsing.nodes.expressions.ExpressionNode
import cobalt.parsing.nodes.leafs.TypeNode
import cobalt.parsing.nodes.leafs.IdentifierNode

class VariableDeclarationNode(line:Int) : StatementNode("VariableDeclaration", line) {

    var identifier: IdentifierNode? = null
        set(value) {
            field = value
            value?.parentNode = this
        }

    var explicitType: TypeNode? = null
        set(value) {
            field = value
            value?.parentNode = this
        }

    var expression: ExpressionNode? = null
        set(value) {
            field = value
            value?.parentNode = this
        }

    override fun toString(): String {
        return "$type: $identifier:${explicitType?:"?"}"
    }
}