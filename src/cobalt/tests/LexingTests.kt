package cobalt.tests

import cobalt.exceptions.CobaltSyntaxError
import cobalt.lexing.Lexer
import org.junit.jupiter.api.*
import kotlin.test.assertEquals

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class LexingTests {

    @Nested
    class LexerTests {

        private val lexer = Lexer()

        @Test
        fun `whitespace handling`() {
            val tokens = lexer.tokenize("   \t     \t\t ")
            assertEquals(0, tokens.count())
        }

        @Test
        fun `new line handling`() {
            val tokens = lexer.tokenize("1\r2\n3\r\n4 ")
            assertEquals(listOf(1,2,3,4), tokens.map { t -> t.line })
        }

        @Test
        fun `single char keywords`() {
            val tokens = lexer.tokenize(":;&|=!<>+-*/~()")
            assertEquals(
                listOf("colon", "semicolon", "and", "or", "equal", "not", "less", "greater", "plus", "minus", "asterisk", "slash", "tilde", "lparen", "rparen"),
                tokens.map { t -> t.type }
            )
        }

        @Test
        fun `end-of-line comments`() {
            val tokens = lexer.tokenize("test//foo\r\ntest//bar\rtest//foobar\ntest ")
            assertEquals(
                listOf("test", "test", "test", "test"),
                tokens.map { t -> t.data }
            )
        }

        @Test
        fun `operator coalescing`() {
            val tokens = lexer.tokenize("== = != <= >= =")
            assertEquals(6, tokens.count())
            assertEquals(
                listOf(true, false, true, true, true, false),
                tokens.map { t -> t.isOperator }
            )
        }

        @Test
        fun `operator enriching`() {
            val tokens = lexer.tokenize("!&|==!=<=>=<>+-*/~")
            assertEquals(
                listOf(2, 1, 1, 3, 3, 3, 3, 3, 3, 4, 4, 5, 5, 6),
                tokens.map { t -> t.precedence }
            )
            assertEquals(
                listOf(1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 1),
                tokens.map { t -> t.arity }
            )
        }

        @Test
        fun `reserved multiletter keywords`() {
            val tokensSimple = lexer.tokenize("def stdin stdout ")
            val tokensTypes = lexer.tokenize("bool int float ")
            val tokensLiterals = lexer.tokenize("true false ")
            assertEquals(
                listOf("declaration", "input", "output"),
                tokensSimple.map { t -> t.type }
            )
            assertEquals(
                listOf("type", "type", "type"),
                tokensTypes.map { t -> t.type }
            )
            assertEquals(
                listOf("bool", "int", "float"),
                tokensTypes.map { t -> t.subtype }
            )
            assertEquals(
                listOf("literal", "literal"),
                tokensLiterals.map { t -> t.type }
            )
            assertEquals(
                listOf("bool", "bool"),
                tokensLiterals.map { t -> t.subtype }
            )
            assertEquals(
                listOf(true, false),
                tokensLiterals.map { t -> t.data }
            )
        }

        @Test
        fun `valid identifiers`() {
            val tokens = lexer.tokenize("test hello_world a1 b2 a_1 B2_ Z abc_123_1_a ")
            assertEquals(
                MutableList(8) {"identifier"},
                tokens.map { t -> t.type }
            )
        }

        @Test
        fun `bad identifier starts with number`() {
            assertThrows<CobaltSyntaxError> {
                lexer.tokenize("123test ")
            }
        }

        @Test
        fun `bad identifier starts with underline`() {
            assertThrows<CobaltSyntaxError> {
                lexer.tokenize("_123test ")
            }
        }

        @Test
        fun `number literals`() {
            val intTokens = lexer.tokenize("123 5 0913239 007 ")
            val floatTokens = lexer.tokenize("123.5 12.12 0.1 1.0 ")
            assertEquals(
                listOf(123, 5, 913239, 7),
                intTokens.map { t -> t.data }
            )
            assertEquals(
                listOf(123.5F, 12.12F, 0.1F, 1.0F),
                floatTokens.map { t -> t.data }
            )
        }

        @Test
        fun `bad floating point format`() {
            assertThrows<CobaltSyntaxError> {
                lexer.tokenize(".123 ")
            }
            assertThrows<CobaltSyntaxError> {
                lexer.tokenize("12. ")
            }
        }

    }

}