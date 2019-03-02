package cobalt.optimization

import cobalt.parsing.nodes.AstNode

interface IAstTransformer {

    fun transform(node:AstNode)

}