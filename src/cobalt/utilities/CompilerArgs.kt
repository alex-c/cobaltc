package cobalt.utilities

class CompilerArgs {

    companion object {

        fun parse(args:Array<String>):Map<String,Any> {

            if (args.count() < 6) {
                throw Exception("Not enough command line arguments supplied!")
            }

            val options = mutableMapOf<String, Any>()
            val requiredOptions = listOf("input", "output", "target")
            var position = 0

            while (position <= args.count()) {
                when (args[position]) {
                    "--debug" -> options["debug"] = true
                    "-i", "--input" -> {
                        val option = parseArgumentWithValue(args, position, "input")
                        options[option.first] = option.second
                        position ++
                    }
                    "-o", "--output" -> {
                        val option = parseArgumentWithValue(args, position, "output")
                        options[option.first] = option.second
                        position ++
                    }
                }
            }

            for (option in requiredOptions) {
                if (!options.containsKey(option)) {
                    throw Exception("Arguments missing required option '$option'.")
                }
            }

            return options
        }

        private fun parseArgumentWithValue(args:Array<String>, position:Int, option:String):Pair<String, String> {
            if (position == args.count() - 1) throw Exception("Option '$option' is missing a value.")
            return Pair(option, args[position + 1])
        }

    }

}