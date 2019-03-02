package cobalt.parsing.nodes.leafs

import cobalt.parsing.nodes.expressions.ExpressionNode

open class LiteralNode(type:String, line:Int, val value:Any) : ExpressionNode(type, line) {

    override fun toString(): String {
        return "$type($value)"
    }

}