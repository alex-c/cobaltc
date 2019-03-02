package cobalt.parsing

import cobalt.exceptions.CobaltSyntaxError
import cobalt.exceptions.CompilerError
import cobalt.lexing.Token
import cobalt.parsing.nodes.*
import cobalt.parsing.nodes.expressions.BinaryExpressionNode
import cobalt.parsing.nodes.expressions.ExpressionNode
import cobalt.parsing.nodes.expressions.UnaryExpressionNode
import cobalt.parsing.nodes.expressions.binary.*
import cobalt.parsing.nodes.expressions.unary.ArithmeticNegationNode
import cobalt.parsing.nodes.expressions.unary.LogicalNegationNode
import cobalt.parsing.nodes.leafs.IdentifierNode
import cobalt.parsing.nodes.leafs.LiteralNode
import cobalt.parsing.nodes.leafs.TypeNode
import cobalt.parsing.nodes.leafs.literals.BoolLiteralNode
import cobalt.parsing.nodes.leafs.literals.FloatLiteralNode
import cobalt.parsing.nodes.leafs.literals.IntLiteralNode
import cobalt.parsing.nodes.statements.InputNode
import cobalt.parsing.nodes.statements.OutputNode
import cobalt.parsing.nodes.statements.VariableAssignmentNode
import cobalt.parsing.nodes.statements.VariableDeclarationNode
import cobalt.utilities.*

class Parser {

    fun parse(tokens: List<Token>):CobaltProgram {

        if (tokens.isEmpty()) {
            throw CobaltSyntaxError(0, "Program empty!")
        }

        val program = CobaltProgram()
        program.code = parseCodeBlock(tokens, 0, tokens.count())

        return program
    }

    private fun parseCodeBlock(tokens:List<Token>, offset:Int, limit:Int):CodeBlockNode {

        if (offset < 0 || limit <= offset || limit > tokens.count()) {
            throw CompilerError("Parser.parseCodeBlock called with bad offset and/or limit parameters.")
        }

        val codeBlock = CodeBlockNode(tokens.first().line)

        var position = offset

        while (position < limit) {
            when (tokens[position].type) {
                "declaration" -> {
                    val statementEnd = findNextTokenOfType(tokens, position, "semicolon")
                    codeBlock.addStatement(parseVariableDeclaration(tokens, position, statementEnd))
                    position = statementEnd + 1
                }
                "input" -> {
                    if (tokens[position + 1].type == "identifier" && tokens[position + 2].type == "semicolon") {
                        codeBlock.addStatement(parseInputStatement(tokens[position + 1]))
                        position += 3
                    } else {
                        throw CobaltSyntaxError(tokens[position + 1].line, "Malformed input statement, expected the keyword 'stdin' to be followed by an identifier and semicolon.")
                    }
                }
                "output" -> {
                    val statementEnd = findNextTokenOfType(tokens, position, "semicolon")
                    codeBlock.addStatement(parseOutputStatement(tokens, position, statementEnd))
                    position = statementEnd + 1
                }
                "identifier" -> {
                    if (tokens[position + 1].type == "colon" && tokens[position + 2].type == "equal") {
                        val statementEnd = findNextTokenOfType(tokens, position, "semicolon")
                        codeBlock.addStatement(parseVariableAssignment(tokens, position, statementEnd))
                        position = statementEnd + 1
                    } else {
                        throw CobaltSyntaxError(tokens[position].line, "Malformed assignment statement, expected the identifier to be followed by a colon and an equal sign.")
                    }
                }
                else -> throw CobaltSyntaxError(tokens[position].line, "No valid statement, expected any of variable declaration, variable assignment, input or output statement.")
            }
        }

        return codeBlock
    }

    private fun parseVariableDeclaration(tokens:List<Token>, offset:Int, limit:Int): VariableDeclarationNode {

        val variableDeclaration = VariableDeclarationNode(tokens[offset].line)

        if (tokens[offset].type == "declaration") {
            if (tokens[offset + 1].type == "identifier") {
                variableDeclaration.identifier = parseIdentifier(tokens[offset + 1])
                if (tokens[offset + 2].type == "colon") {
                    if (tokens[offset + 3].type == "type") {
                        variableDeclaration.explicitType = parseType(tokens[offset + 3])
                        if (tokens[offset + 4].type == "equal") {
                            variableDeclaration.expression = parseExpression(tokens, offset + 5, limit)
                        } else if (tokens[offset + 4].type == "semicolon") {
                            //Done, declaration with explicit type and no expression
                        } else {
                            throw CobaltSyntaxError(tokens[offset + 2].line, "Expected an equal or semicolon in an explicitly typed variable declaration, got '${tokens[offset + 4].type}' instead.")
                        }
                    } else if(tokens[offset + 3].type == "equal") {
                        variableDeclaration.expression = parseExpression(tokens, offset + 4, limit)
                    } else {
                        throw CobaltSyntaxError(tokens[offset + 2].line, "Expected an equal in a non-typed variable declaration, got '${tokens[offset + 3].type}' instead.")
                    }
                } else {
                    throw CobaltSyntaxError(tokens[offset + 2].line, "Expected a colon in a variable declaration, got '${tokens[offset + 2].type}' instead.")
                }
            } else {
                throw CobaltSyntaxError(tokens[offset + 1].line, "Expected an identifier in a variable declaration, got '${tokens[offset + 1].type}' instead.")
            }
        } else {
            throw CompilerError("[parseVariableDeclaration] called but token at offset is not a declaration token.")
        }

        return variableDeclaration

    }

    private fun parseVariableAssignment(tokens:List<Token>, offset:Int, limit:Int): VariableAssignmentNode {

        if (tokens[offset].type == "identifier" &&
            tokens[offset + 1].type == "colon" &&
            tokens[offset + 2].type == "equal") {

            val variableAssignment = VariableAssignmentNode(tokens[offset].line)
            variableAssignment.expression = parseExpression(tokens, offset + 3, limit)
            return variableAssignment

        } else {
            throw CobaltSyntaxError(tokens[offset].line, "Malformed assignment statement, expected the identifier to be followed by a colon and an equal sign.")
        }

    }

    private fun parseInputStatement(token:Token): InputNode {

        val inputStatement = InputNode(token.line)
        inputStatement.identifier = parseIdentifier(token)
        return inputStatement

    }

    private fun parseOutputStatement(tokens:List<Token>, offset:Int, limit:Int): OutputNode {

        val outputStatement = OutputNode(tokens.first().line)
        outputStatement.expression = parseExpression(tokens, offset + 1, limit)
        return outputStatement

    }

    private fun parseExpression(tokens:List<Token>, offset:Int, limit:Int): ExpressionNode {

        if (tokens.subList(offset, limit).isEmpty()) throw CobaltSyntaxError(tokens[offset - 1].line, "An expression cannot be empty!")

        val outputStack = Stack<AstNode>()
        val operatorStack = UnsafeStack<Token>()

        //Iterate through all tokens of the expression
        for (token in tokens.subList(offset, limit)) {

            //Parse identifiers and literals and push them onto the output stack
            if (token.type == "identifier") {
                outputStack.push(parseIdentifier((token)))
            } else if (token.type == "literal") {
                outputStack.push(parseLiteral(token))
            }

            //For operators, while precedence is lower or equal than the precedence on the top of the operator stack,
            //  parse sub-expressions depending on arity and push them onto the output stack
            else if (token.isOperator) {
                if (token.precedence <= 0) throw CompilerError("[parseExpression] Token on operator stack has illegal precedence ${token.precedence}.")
                while (!operatorStack.isEmpty() && token.precedence <= operatorStack.peek().precedence) {
                    val operator = operatorStack.pop()
                    parseSubExpression(operator, outputStack)
                }
                operatorStack.push(token)
            }

            //Left-hand parenthesis: push onto operator stack
            else if (token.type == "lparen") {
                operatorStack.push(token)
            }

            //Right-hand parenthesis: parse sub-expressions until left-hand parentesis is reached at the top of the operator stack!
            else if (token.type == "rparen") {
                while (!operatorStack.isEmpty() && operatorStack.peek().type != "lparen") {
                    val operator = operatorStack.pop()
                    parseSubExpression(operator, outputStack)

                    //A left-hand parenthesis is now expected to be on top of the operator stack!
                    if (!operatorStack.isEmpty() && operatorStack.peek().type == "lparen") {
                        operatorStack.pop()
                    } else {
                        throw CobaltSyntaxError(tokens[offset].line, "Parenteses missmatched in expression.")
                    }
                }
            } else {
                throw CompilerError("[parseExpression] Illegal token of type '${token.type}' in expression.")
            }
        }

        //While there are operator tokens left on the operator stack, pop and parse sub-expressions
        while (!operatorStack.isEmpty()) {
            val operator = operatorStack.pop()

            //There should be no parentheses left on the operator stack!
            if(operator.type == "lparen" || operator.type == "rparen") {
                throw CobaltSyntaxError(operator.line, "Parantheses missmatched in expression.");
            }

            parseSubExpression(operator, outputStack)
        }

        //The output stack is now expected to have exactly one unary or binary expression node
        if(outputStack.count() == 1) {
            val resultNode = outputStack.pop()
            if (resultNode is ExpressionNode) {
                return resultNode
            } else {
                throw CobaltSyntaxError(resultNode?.line?:0, "Bad expression syntax.")
            }
        } else {
            throw CompilerError("[parseExpression] Parsing expression failed, ${outputStack.count()} elements left on the output stack, expected 1.")
        }

    }

    private fun parseSubExpression(operator:Token, outputStack:Stack<AstNode>) {
        when (operator.arity) {
            2 -> {
                val rightOperand = outputStack.pop()
                val leftOperand = outputStack.pop()
                if (leftOperand != null && rightOperand != null) {
                    outputStack.push(parseBinaryExpression(operator, leftOperand, rightOperand))
                } else {
                    throw CompilerError("[parseExpression] An operand read from output stack is null!.");
                }
            }
            1 -> {
                val operand = outputStack.pop()
                if (operand != null) {
                    outputStack.push(parseUnaryExpression(operator, operand));
                } else {
                    throw CompilerError("[parseExpression] An operand read from output stack is null!.");
                }
            }
            else -> throw CompilerError("[parseExpression] Token on operator stack has illegal arity ${operator.arity}.")
        }
    }

    private fun parseBinaryExpression(operator:Token, leftOperand:AstNode, rightOperand:AstNode):AstNode {
        if (!operator.isOperator) throw CompilerError("[parseBinaryExpression] called with non-operator token.")
        val expression: BinaryExpressionNode
        when (operator.type) {
            "plus" -> expression = AdditionNode(operator.line)
            "minus" -> expression = SubstractionNode(operator.line)
            "asterisk" -> expression = MultiplicationNode(operator.line)
            "slash" -> expression = DivisionNode(operator.line)
            "and" -> expression = LogicalAndNode(operator.line)
            "or" -> expression = LogicalOrNode(operator.line)
            else -> if (operator.type == "equals" || operator.type == "not_equal" ||
                operator.type == "greater" || operator.type == "greater_equal" ||
                operator.type == "less" || operator.type == "less_equal" ) {
                expression = ComparisonNode(operator.type, operator.line)
            } else {
                throw CompilerError("[parseBinaryExpression] called with illegal operator token '${operator.type}'.")
            }
        }
        expression.leftOperand = leftOperand
        expression.rightOperand = rightOperand
        return expression
    }

    private fun parseUnaryExpression(operator:Token, operand:AstNode):AstNode {
        if (!operator.isOperator) throw CompilerError("[parseUnaryExpression] called with non-operator token.")
        val expression: UnaryExpressionNode
        when (operator.type) {
            "not" -> expression = LogicalNegationNode(operator.line)
            "tilde" -> expression = ArithmeticNegationNode(operator.line)
            else -> throw CompilerError("[parseUnaryExpression] called with illegal operator token '${operator.type}'.")
        }
        expression.operand = operand
        return expression
    }

    private fun parseLiteral(token:Token): LiteralNode {

        if (token.type == "literal") {
            if (token.subtype != null && token.data != null) {
                when (token.subtype) {
                    "bool" -> return parseBoolLiteral(token)
                    "int" -> return parseIntLiteral(token)
                    "float" -> return parseFloatLiteral(token)
                    else -> throw CompilerError("[parseLiteral] called but passed token does not have a valid subtype (is '${token.subtype}').")
                }
            } else {
                throw CompilerError("[parseLiteral] called but passed literal token does not have valid subtype and/or data properties.")
            }
        } else {
            throw CompilerError("[parseLiteral] called but passed token is not a literal token (is of type '${token.type}' instead).")
        }

    }

    private fun parseBoolLiteral(token:Token): BoolLiteralNode {

        if (token.type == "literal" && token.subtype == "bool") {
            if (token.data is Boolean) {
                return BoolLiteralNode(token.line, token.data)
            } else {
                throw CompilerError("[parseBoolLiteral] called but passed token data is not of type 'Boolean'.")
            }
        } else {
            throw CompilerError("[parseBoolLiteral] called but passed token is not of 'literal/bool' type.")
        }

    }

    private fun parseIntLiteral(token:Token): IntLiteralNode {

        if (token.type == "literal" && token.subtype == "int") {
            if (token.data is Int) {
                return IntLiteralNode(token.line, token.data)
            } else {
                throw CompilerError("[parseIntLiteral] called but passed token data is not of type 'Int'.")
            }
        } else {
            throw CompilerError("[parseIntLiteral] called but passed token is not of 'literal/int' type.")
        }

    }

    private fun parseFloatLiteral(token:Token): FloatLiteralNode {

        if (token.type == "literal" && token.subtype == "float") {
            if (token.data is Float) {
                return FloatLiteralNode(token.line, token.data)
            } else {
                throw CompilerError("[parseFloatLiteral] called but passed token data is not of type 'Float'.")
            }
        } else {
            throw CompilerError("[parseFloatLiteral] called but passed token is not of 'literal/float' type.")
        }

    }

    private fun parseIdentifier(token:Token): IdentifierNode {
        if (token.data is String) {
            return IdentifierNode(token.line, token.data)
        } else {
            throw CompilerError("Could not parse a valid name while parsing an identifier token.")
        }
    }

    private fun parseType(token:Token): TypeNode {
        if (token.subtype != null) {
            when (token.subtype) {
                "bool" ->  return TypeNode(token.line, Type.BOOL)
                "int" ->  return TypeNode(token.line, Type.INT)
                "float" ->  return TypeNode(token.line, Type.FLOAT)
                else -> throw CompilerError("Invalid type keyword subtype '${token.subtype}'.")
            }
        } else {
            throw CompilerError("Could not parse a valid subtype while parsing a type keyword token.")
        }
    }

    private fun findNextTokenOfType(tokens:List<Token>, offset:Int, tokenType:String):Int {

        var position = offset

        while(position < tokens.count()) {
            if (tokens[position].type == tokenType) {
                return position
            }
            position ++
        }

        //Not found!
        return -1
    }

}