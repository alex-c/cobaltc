package cobalt.parsing.nodes.expressions

import cobalt.parsing.nodes.AstNode

open class UnaryExpressionNode(type:String, line:Int) : ExpressionNode(type, line) {

    var operand: AstNode? = null
        set(value) {
            field = value
            value?.parentNode = this
        }

}