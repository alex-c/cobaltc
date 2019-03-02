package cobalt.parsing.nodes

import cobalt.analysis.Symbol
import cobalt.analysis.SymbolTable
import cobalt.analysis.SymbolTableException
import cobalt.exceptions.CompilerError

open class AstNode(val type:String, val line:Int) {

    var parentNode:AstNode? = null

    open val definesScope = false
    val symbols = SymbolTable()

    fun registerSymbol(symbol:Symbol) {
        if (definesScope) {
            symbols.registerSymbol(symbol)
        } else if (parentNode != null) {
            parentNode?.registerSymbol(symbol)
        } else {
            throw CompilerError("Failed registering symbol with name '${symbol.name}': no symbol table found.")
        }
    }

    fun lookupSymbol(name:String):Symbol {
        if (definesScope) {
            return symbols.lookupSymbol(name)
        } else {
            val parentNode = parentNode
            if (parentNode != null) {
                return parentNode.lookupSymbol(name)
            } else {
                throw SymbolTableException("Failed looking up symbol with name '$name'.")
            }
        }
    }

    override fun toString():String {
        return type
    }

}