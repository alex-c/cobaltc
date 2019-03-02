package cobalt.optimization

import cobalt.exceptions.CompilerError
import cobalt.parsing.nodes.AstNode
import kotlin.reflect.KClass

class AstTraverser(private val name:String = "default") {

    private val transformers: MutableMap<KClass<AstNode>, IAstTransformer> = mutableMapOf()

    fun addTransformer(nodeType:KClass<AstNode>, transformer:IAstTransformer) {
        if (!transformers.containsKey(nodeType)) {
            transformers[nodeType] = transformer
        } else {
            throw CompilerError("Trying to add a transformer to traverser '$name' for node type ${nodeType.simpleName}.")
        }
    }

}