package cobalt.lexing

import cobalt.exceptions.CobaltSyntaxError

class Lexer {

    //Possible delimiters to words
    private val delimiters = listOf(' ', '\t', '\r', '\n', ':', ';', '&', '|', '=', '!', '>', '<', '+', '-', '*', '/', '~', '(', ')')

    //Regex used to match identifiers
    private val identifierRegex = Regex("^[a-z,A-Z]+\\w*\$")

    fun tokenize(code:String):List<Token> {

        //Token list to build
        val tokens = mutableListOf<Token>()

        //Code position and line count to keep track of
        var position = 0
        var line = 1

        //Start tokenizing
        while (position < code.length) {

            val firstChar:Char = code[position];

            //Ignore whitespace
            if (isWhitespace(firstChar)) {
                position ++
            }

            //Check for EOL
            else if(isEOL(firstChar)) {
                if (firstChar == '\r' && code[position + 1] == '\n') {
                    position += 2
                } else {
                    position ++
                }
                line ++
            }

            //Other cases
            else {

                //Check for single-char tokens
                when(firstChar) {
                    ':' -> {
                        tokens.add(Token("colon", line))
                        position ++
                    }
                    ';' -> {
                        tokens.add(Token("semicolon", line))
                        position ++
                    }
                    '&' -> {
                        tokens.add(Token("and", line))
                        position ++
                    }
                    '|' -> {
                        tokens.add(Token("or", line))
                        position ++
                    }
                    '=' -> {
                        tokens.add(Token("equal", line))
                        position ++
                    }
                    '!' -> {
                        tokens.add(Token("not", line))
                        position ++
                    }
                    '<' -> {
                        tokens.add(Token("less", line))
                        position ++
                    }
                    '>' -> {
                        tokens.add(Token("greater", line))
                        position ++
                    }
                    '+' -> {
                        tokens.add(Token("plus", line))
                        position ++
                    }
                    '-' -> {
                        tokens.add(Token("minus", line))
                        position ++
                    }
                    '*' -> {
                        tokens.add(Token("asterisk", line))
                        position ++
                    }
                    '/' -> {
                        //Ignore EOL comment, jump to EOL
                        if (code[position + 1] == '/') {
                            val eolPosition = findNextInstanceOfCharInString(code, position, listOf('\r', '\n'))
                            if (eolPosition == -1) {
                                throw CobaltSyntaxError(line, "Could not find EOL for comment.")
                            } else {
                                position = eolPosition
                            }
                        } else {
                            tokens.add(Token("slash", line))
                            position++
                        }
                    }
                    '~' -> {
                        tokens.add(Token("tilde", line))
                        position ++
                    }
                    '(' -> {
                        tokens.add(Token("lparen", line))
                        position ++
                    }
                    ')' -> {
                        tokens.add(Token("rparen", line))
                        position ++
                    }

                    //Handle multi-char keywords and values
                    else -> {
                        val wordLimit = findNextInstanceOfCharInString(code, position, delimiters)
                        if (wordLimit > 0) {

                            val word = code.substring(position, wordLimit)
                            when (word) {
                                //Reserved keywords
                                "def" -> tokens.add(Token("declaration", line))
                                "stdin" -> tokens.add(Token("input", line))
                                "stdout" -> tokens.add(Token("output", line))
                                "bool" -> tokens.add(Token("type", line, word))
                                "int" -> tokens.add(Token("type", line, word))
                                "float" -> tokens.add(Token("type", line, word))
                                "true" -> tokens.add(Token("literal", line, "bool", word.toBoolean()))
                                "false" -> tokens.add(Token("literal", line, "bool", word.toBoolean()))

                                //Identifiers and number literals
                                else -> {
                                    val intValue = word.toIntOrNull()
                                    val floatValue = word.toFloatOrNull()

                                    //Number literals
                                    if (intValue != null) {
                                        tokens.add(Token("literal", line, "int", intValue))
                                    } else if(floatValue != null) {
                                        if (!word.startsWith('.') && !word.endsWith('.')) {
                                            tokens.add(Token("literal", line, "float", floatValue))
                                        } else {
                                            throw CobaltSyntaxError(line, "Floating point values have to have at least one figure before and one after the dot.")
                                        }
                                    }

                                    //Identifiers
                                    else {
                                        if (isValidIdentifier(word)) {
                                            tokens.add(Token("identifier", line, null, word))
                                        } else {
                                            throw CobaltSyntaxError(line, "Expected an identifier, but the word '$word' is not a valid identifier.")
                                        }
                                    }
                                }
                            }

                            //Increment position
                            position += word.length

                        } else {
                            throw CobaltSyntaxError(line, "Failed finding end of word.")
                        }
                    }

                }

            }

        }

        //Coalesce operators (eg. make a "lessEqual" token from a "less" token followed by an "equal" token)
        val coalescedTokens = coalesceOperators(tokens)

        //Enrich operators with arity and precedence information
        val enrichedTokens = enrichOperators(coalescedTokens)

        //Finished tokenizing the code without error!
        return enrichedTokens

    }

    //Coalesces operators (eg. make a "lessEqual" token from a "less" token followed by an "equal" token)
    private fun coalesceOperators(tokens:MutableList<Token>):MutableList<Token> {

        val tokenTypesToMatch = listOf("equal", "not", "less", "greater")

        var position = 0
        val result = mutableListOf<Token>()

        while(position < tokens.count()) {
            val token = tokens[position]
            if (position == tokens.count() - 1) {
                result.add(token)
                position ++
            } else if (tokenTypesToMatch.contains(token.type) && tokens[position + 1].type == "equal") {
                if (token.type == "equal") {
                    result.add(Token("equals", token.line))
                } else {
                    result.add(Token(token.type + "_equal", token.line))
                }
                position += 2
            } else {
                result.add(tokens[position])
                position ++
            }
        }

        return result
    }

    //Enriches operators with arity and precedence information
    private fun enrichOperators(tokens:MutableList<Token>):MutableList<Token> {

        for (token in tokens) {
            if (token.type == "and" || token.type == "or") {
                token.setOperator(1, 2)
            } else if (token.type == "not") {
                token.setOperator(2, 1)
            } else if(token.type == "equals" || token.type == "not_equal" ||
                token.type == "less" || token.type == "less_equal" ||
                token.type == "greater" || token.type == "greater_equal") {
                token.setOperator(3, 2)
            } else if(token.type == "plus" || token.type == "minus") {
                token.setOperator(4, 2)
            } else if(token.type == "asterisk" || token.type == "slash") {
                token.setOperator(5, 2)
            } else if(token.type == "tilde") {
                token.setOperator(6, 1)
            }
        }

        return tokens

    }

    private fun findNextInstanceOfCharInString(code:String, offset:Int, charsToFind:List<Char>, ignoreForChars:List<Char> = listOf()):Int {

        var position = offset
        var ignoreCount = 0

        while (position < code.length) {
            val nextChar:Char = code[position]
            if (charsToFind.contains(nextChar)) {
                if (ignoreCount > 0) {
                    ignoreCount --
                } else {
                    return position
                }
            } else if (ignoreForChars.contains(nextChar)) {
                ignoreCount ++
            }
            position ++
        }

        //No char found!
        return -1
    }

    private fun isWhitespace(char:Char):Boolean {
        return char == ' ' || char == '\t'
    }

    private fun isEOL(char:Char):Boolean {
        return char == '\r' || char == '\n'
    }

    private fun isValidIdentifier(word:String):Boolean {
        return identifierRegex.matches((word))
    }

}