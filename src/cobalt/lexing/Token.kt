package cobalt.lexing

class Token(val type:String, val line:Int, val subtype:String? = null, val data:Any? = null) {

    override fun toString():String {
        var fullType = type
        var dataAppendix = ""
        if (subtype != null) fullType += "_$subtype"
        if (data != null) dataAppendix = "{$data}"
        return "($fullType$dataAppendix:$line)"
    }

}