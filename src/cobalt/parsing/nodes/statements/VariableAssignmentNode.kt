package cobalt.parsing.nodes.statements

import cobalt.parsing.nodes.expressions.ExpressionNode
import cobalt.parsing.nodes.leafs.IdentifierNode

class VariableAssignmentNode(line:Int) : StatementNode("VariableAssignment", line) {

    var identifier: IdentifierNode? = null
        set(value) {
            field = value
            value?.parentNode = this
        }

    var expression: ExpressionNode? = null
        set(value) {
            field = value
            value?.parentNode = this
        }

}