package cobalt.exceptions

class CobaltTypeError(line:Int, message:String) : CobaltException("type", line, message)