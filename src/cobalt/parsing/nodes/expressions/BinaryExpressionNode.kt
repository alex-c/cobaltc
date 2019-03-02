package cobalt.parsing.nodes.expressions

import cobalt.parsing.nodes.AstNode

open class BinaryExpressionNode(type:String, line:Int) : ExpressionNode(type, line) {

    var leftOperand: AstNode? = null
        set(value) {
            field = value
            value?.parentNode = this
        }

    var rightOperand: AstNode? = null
        set(value) {
            field = value
            value?.parentNode = this
        }

}