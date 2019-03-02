package cobalt.analysis

import cobalt.exceptions.CobaltReferenceError
import cobalt.exceptions.CompilerError

class SymbolTable {

    private val symbols:MutableMap<String, Symbol> = mutableMapOf()

    fun hasSymbol(name:String):Boolean {
        return symbols.containsKey(name)
    }

    fun registerSymbol(symbol: Symbol) {
        if (!hasSymbol(symbol.name)) {
            symbols[symbol.name] = symbol
        } else {
            throw SymbolTableException("Failed registering symbol with name '${symbol.name}' as there is already a symbol with that name registered.")
        }
    }

    fun lookupSymbol(name:String): Symbol {
        if (hasSymbol(name)) {
            val symbol = symbols[name]
            if (symbol != null) {
                return symbol
            } else {
                throw CompilerError("[SymbolTable.lookupSymbol] failed to return symbol.")
            }
        } else {
            throw SymbolTableException("Failed looking up symbol with name '$name'.")
        }
    }

}