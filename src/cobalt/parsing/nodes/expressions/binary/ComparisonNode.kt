package cobalt.parsing.nodes.expressions.binary

import cobalt.parsing.nodes.expressions.BinaryExpressionNode

class ComparisonNode(val comparisonType:String, line:Int) : BinaryExpressionNode("Comparison", line)