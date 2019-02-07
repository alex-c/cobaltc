import cobalt.lexing.Lexer

fun main(args: Array<String>) {

    val lexer = Lexer()

    val tokens = lexer.tokenize("def x:int = 3 <= 4;\r\ndef y:bool = true;\r\ndef z:float=true==false;")

    val output = tokens.joinToString(", ")

    println(output)
}