package cobalt.parsing.nodes

class AstNode(val type:String, val line:Int) {

    override fun toString(): String {
        return "($type:$line)"
    }

}