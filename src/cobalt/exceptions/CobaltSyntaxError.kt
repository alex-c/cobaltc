package cobalt.exceptions

class CobaltSyntaxError(line:Int, message:String) : CobaltException("syntax", line, message)