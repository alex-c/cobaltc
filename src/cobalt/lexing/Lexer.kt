package cobalt.lexing

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
        var line = 0

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
                                //TODO: throw exception
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
                                "true" -> tokens.add(Token("literal", line, "bool", word))
                                "false" -> tokens.add(Token("literal", line, "bool", word))

                                //Identifiers and number literals
                                else -> {
                                    val intValue = word.toIntOrNull()
                                    val floatValue = word.toFloatOrNull()

                                    //Number literals
                                    if (intValue != null) {
                                        tokens.add(Token("literal", line, "int", intValue))
                                    } else if(floatValue != null) {
                                        tokens.add(Token("literal", line, "float", floatValue))
                                    }

                                    //Identifiers
                                    else {
                                        if (isValidIdentifier(word)) {
                                            tokens.add(Token("identifier", line, null, word))
                                        } else {
                                            //TODO: throw exception
                                        }
                                    }
                                }
                            }

                            //Increment position
                            position += word.length

                        } else {
                            //TODO: throw exception
                        }
                    }

                }

            }

        }

        //Coalesce operators (eg. make a "lessEqual" token from a "less" token followed by an "equal" token)
        val cleanTokens = coalesceOperators(tokens)

        //Finished tokenizing the code without error!
        return cleanTokens

    }

    private fun coalesceOperators(tokens:MutableList<Token>):MutableList<Token> {

        val tokenTypesToMatch = listOf("equal", "not", "less", "greater")

        var position = 0
        val result = mutableListOf<Token>()

        while(position < tokens.count()) {
            val token = tokens[position]
            if (tokenTypesToMatch.contains(token.type) && tokens[position + 1].type == "equal") {
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