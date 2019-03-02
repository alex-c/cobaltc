package cobalt.parsing.nodes.leafs.literals

import cobalt.parsing.nodes.leafs.LiteralNode

class FloatLiteralNode(line:Int, value:Float) : LiteralNode("FloatLiteral", line, value)