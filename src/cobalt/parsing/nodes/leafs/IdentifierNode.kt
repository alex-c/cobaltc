package cobalt.parsing.nodes.leafs

import cobalt.parsing.nodes.expressions.ExpressionNode

class IdentifierNode(line:Int, val name:String) : ExpressionNode("Identifier", line) {

    override fun toString(): String {
        return "$type($name)"
    }

}