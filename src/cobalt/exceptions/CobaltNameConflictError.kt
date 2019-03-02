package cobalt.exceptions

class CobaltNameConflictError(line:Int, message:String) : CobaltException("name conflict", line, message)