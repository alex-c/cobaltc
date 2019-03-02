package cobalt.exceptions

open class CobaltException(type:String, line:Int, message:String) : Exception("Cobalt $type error on line $line: $message")