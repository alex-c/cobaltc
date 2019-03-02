package cobalt.tests

import cobalt.exceptions.CobaltSyntaxError
import cobalt.lexing.Lexer
import cobalt.parsing.Parser
import cobalt.parsing.Type
import cobalt.parsing.nodes.statements.VariableDeclarationNode
import cobalt.parsing.nodes.leafs.literals.*
import org.junit.jupiter.api.*
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ParsingTests {

    @Nested
    class ParserTests {

        private val lexer = Lexer()
        private val parser = Parser()

        @Test
        fun `valueless variable declarations`() {

            val intDeclaration = "def x:int;"
            val floatDeclaration = "def y:float;"
            val boolDeclaration = "def z:bool;"

            val intDeclarationAst = parser.parse(lexer.tokenize(intDeclaration))
            val floatDeclarationAst = parser.parse(lexer.tokenize(floatDeclaration))
            val boolDeclarationAst = parser.parse(lexer.tokenize(boolDeclaration))

            //Check statement count
            assertEquals(1, intDeclarationAst.code?.statements?.count())
            assertEquals(1, floatDeclarationAst.code?.statements?.count())
            assertEquals(1, boolDeclarationAst.code?.statements?.count())

            //Check variable declaration members
            val intDeclarationStatement = intDeclarationAst.code?.statements?.first()
            if (intDeclarationStatement is VariableDeclarationNode) {
                assertNotNull(intDeclarationStatement.identifier)
                assertEquals("x", intDeclarationStatement.identifier?.name)
                assertNotNull(intDeclarationStatement.explicitType)
                assertEquals(Type.INT, intDeclarationStatement.explicitType?.subtype)
                assertNull(intDeclarationStatement.expression)
            } else {
                throw Exception("Statement is not of expected 'VariableDeclaration' type.")
            }

            //Check variable declaration members
            val floatDeclarationStatement = floatDeclarationAst.code?.statements?.first()
            if (floatDeclarationStatement is VariableDeclarationNode) {
                assertNotNull(floatDeclarationStatement.identifier)
                assertEquals("y", floatDeclarationStatement.identifier?.name)
                assertNotNull(floatDeclarationStatement.explicitType)
                assertEquals(Type.FLOAT, floatDeclarationStatement.explicitType?.subtype)
                assertNull(floatDeclarationStatement.expression)
            } else {
                throw Exception("Statement is not of expected 'VariableDeclaration' type.")
            }

            //Check variable declaration members
            val boolDeclarationAstStatement = boolDeclarationAst.code?.statements?.first()
            if (boolDeclarationAstStatement is VariableDeclarationNode) {
                assertNotNull(boolDeclarationAstStatement.identifier)
                assertEquals("z", boolDeclarationAstStatement.identifier?.name)
                assertNotNull(boolDeclarationAstStatement.explicitType)
                assertEquals(Type.BOOL, boolDeclarationAstStatement.explicitType?.subtype)
                assertNull(boolDeclarationAstStatement.expression)
            } else {
                throw Exception("Statement is not of expected 'VariableDeclaration' type.")
            }

        }

        @Test
        fun `bad valueless and typeless variable declaration`() {

            val badCode1 = "def x:;"
            val badCode2 = "def x:=;"
            val badCode3 = "def x=;"

            assertFailsWith<CobaltSyntaxError> {
                parser.parse(lexer.tokenize(badCode1))
            }
            assertFailsWith<CobaltSyntaxError> {
                parser.parse(lexer.tokenize(badCode2))
            }
            assertFailsWith<CobaltSyntaxError> {
                parser.parse(lexer.tokenize(badCode3))
            }

        }

        @Test
        fun `typeless variable declaration with literal`() {

            val intDeclaration = "def x := 1;"
            val floatDeclaration = "def y := 0.5;"
            val boolDeclaration = "def z := true;"

            val intDeclarationAst = parser.parse(lexer.tokenize(intDeclaration))
            val floatDeclarationAst = parser.parse(lexer.tokenize(floatDeclaration))
            val boolDeclarationAst = parser.parse(lexer.tokenize(boolDeclaration))

            //Check statement count
            assertEquals(1, intDeclarationAst.code?.statements?.count())
            assertEquals(1, floatDeclarationAst.code?.statements?.count())
            assertEquals(1, boolDeclarationAst.code?.statements?.count())

            //Check variable declaration members
            val intDeclarationStatement = intDeclarationAst.code?.statements?.first()
            if (intDeclarationStatement is VariableDeclarationNode) {
                assertNotNull(intDeclarationStatement.identifier)
                assertEquals("x", intDeclarationStatement.identifier?.name)
                assertNull(intDeclarationStatement.explicitType)
                assertNotNull(intDeclarationStatement.expression)
                val expression = intDeclarationStatement.expression
                if (expression is IntLiteralNode) {
                    assertEquals(1, expression.value)
                } else {
                    throw Exception("Expression is not of expected 'IntLiteral' type.")
                }
            } else {
                throw Exception("Statement is not of expected 'VariableDeclaration' type.")
            }

            //Check variable declaration members
            val floatDeclarationStatement = floatDeclarationAst.code?.statements?.first()
            if (floatDeclarationStatement is VariableDeclarationNode) {
                assertNotNull(floatDeclarationStatement.identifier)
                assertEquals("y", floatDeclarationStatement.identifier?.name)
                assertNull(floatDeclarationStatement.explicitType)
                assertNotNull(floatDeclarationStatement.expression)
                val expression = floatDeclarationStatement.expression
                if (expression is FloatLiteralNode) {
                    assertEquals(0.5F, expression.value)
                } else {
                    throw Exception("Expression is not of expected 'FloatLiteral' type.")
                }
            } else {
                throw Exception("Statement is not of expected 'VariableDeclaration' type.")
            }

            //Check variable declaration members
            val boolDeclarationAstStatement = boolDeclarationAst.code?.statements?.first()
            if (boolDeclarationAstStatement is VariableDeclarationNode) {
                assertNotNull(boolDeclarationAstStatement.identifier)
                assertEquals("z", boolDeclarationAstStatement.identifier?.name)
                assertNull(boolDeclarationAstStatement.explicitType)
                assertNotNull(boolDeclarationAstStatement.expression)
                val expression = boolDeclarationAstStatement.expression
                if (expression is BoolLiteralNode) {
                    assertEquals(true, expression.value)
                } else {
                    throw Exception("Expression is not of expected 'FloatLiteral' type.")
                }
            } else {
                throw Exception("Statement is not of expected 'VariableDeclaration' type.")
            }
        }



    }

}