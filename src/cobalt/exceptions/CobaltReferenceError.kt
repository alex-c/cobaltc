package cobalt.exceptions

class CobaltReferenceError(line:Int, message:String) : CobaltException("reference", line, message)