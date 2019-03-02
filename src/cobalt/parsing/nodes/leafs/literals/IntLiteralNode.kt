package cobalt.parsing.nodes.leafs.literals

import cobalt.parsing.nodes.leafs.LiteralNode

class IntLiteralNode(line:Int, value:Int) : LiteralNode("IntLiteral", line, value)