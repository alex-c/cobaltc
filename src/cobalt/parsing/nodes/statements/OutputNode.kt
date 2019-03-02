package cobalt.parsing.nodes.statements

import cobalt.parsing.nodes.expressions.ExpressionNode

class OutputNode(line:Int) : StatementNode("Output", line) {

    var expression: ExpressionNode? = null
        set(value) {
            field = value
            value?.parentNode = this
        }

}