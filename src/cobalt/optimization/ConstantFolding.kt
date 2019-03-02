package cobalt.optimization

import cobalt.parsing.nodes.AstNode
import cobalt.parsing.nodes.CodeBlockNode
import cobalt.parsing.nodes.statements.OutputNode
import cobalt.parsing.nodes.statements.VariableAssignmentNode
import cobalt.parsing.nodes.statements.VariableDeclarationNode
import cobalt.parsing.nodes.expressions.binary.*
import com.sun.org.apache.xpath.internal.ExpressionNode

class ConstantFolding : IAstTransformer {

    override fun transform(node: AstNode) {

        //Pass through
        if (node is CodeBlockNode) {
            for (statement in node.statements) {
                transform(statement)
            }
        } else if (node is VariableDeclarationNode) {
            val expression = node.expression
            if (expression != null) {
                transform(expression)
            }
        } else if (node is VariableAssignmentNode) {
            val expression = node.expression
            if (expression != null) {
                transform(expression)
            }
        } else if (node is OutputNode) {
            val expression = node.expression
            if (expression != null) {
                transform(expression)
            }
        }

        //Apply transformation
        else if (node is ExpressionNode) {
            if (node is AdditionNode) {

            }
        }
    }

}