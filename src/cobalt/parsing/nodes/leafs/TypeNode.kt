package cobalt.parsing.nodes.leafs

import cobalt.parsing.Type
import cobalt.parsing.nodes.AstNode

class TypeNode(line:Int, val subtype:Type) : AstNode("Type", line) {

    override fun toString(): String {
        return subtype.toString()
    }

}