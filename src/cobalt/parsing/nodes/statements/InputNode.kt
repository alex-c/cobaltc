package cobalt.parsing.nodes.statements

import cobalt.parsing.nodes.leafs.IdentifierNode

class InputNode(line:Int) : StatementNode("Input", line) {

    var identifier: IdentifierNode? = null
        set(value) {
            field = value
            value?.parentNode = this
        }

}