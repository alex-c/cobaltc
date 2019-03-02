package cobalt.parsing.nodes

import cobalt.exceptions.CompilerError
import cobalt.analysis.Symbol
import cobalt.analysis.SymbolTableException
import cobalt.exceptions.CobaltNameConflictError
import cobalt.parsing.nodes.statements.VariableDeclarationNode

class CobaltProgram : AstNode("CobaltProgram", 1) {

    var code:CodeBlockNode? = null

    fun buildSymbolTables(astNode: AstNode = this) {
        if (astNode is CobaltProgram) {
            val codeBlock = astNode.code
            if (codeBlock != null) {
                buildSymbolTables(codeBlock)
            } else {
                throw CompilerError("[CobaltProgram.buildSymbolTables] called but the code node is null.")
            }
        } else if (astNode is CodeBlockNode) {
            val statements = astNode.statements
            for (statement in statements) {
                if (statement is VariableDeclarationNode) {
                    val identifier = statement.identifier
                    val explicitType = statement.explicitType
                    if (identifier != null) {
                        val symbol = Symbol(identifier.name, identifier.line)
                        if (explicitType != null) {
                            symbol.type = explicitType.subtype
                        }
                        try {
                            statement.registerSymbol(symbol)
                        } catch (exception: SymbolTableException) {
                            throw CobaltNameConflictError(statement.line, exception.message)
                        }
                    } else {
                        throw CompilerError("[buildSymbolTables] found variable declaration node without identifier information.")
                    }
                }
            }
        }

    }

}