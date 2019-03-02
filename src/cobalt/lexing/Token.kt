package cobalt.lexing

class Token(val type:String, val line:Int, val subtype:String? = null, val data:Any? = null) {

    var isOperator:Boolean = false
    var precedence:Int = 0
    var arity:Int = 0

    fun setOperator(precedence:Int, arity:Int) {
        isOperator = true
        this.precedence = precedence
        this.arity = arity
    }

    override fun toString():String {
        var fullType = type
        var dataAppendix = ""
        if (subtype != null) fullType += "_$subtype"
        if (data != null) dataAppendix = "{$data}"
        return "($fullType$dataAppendix:$line)"
    }

}