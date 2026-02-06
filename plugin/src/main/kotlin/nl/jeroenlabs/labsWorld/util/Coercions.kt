package nl.jeroenlabs.labsWorld.util

fun anyToInt(value: Any?, default: Int): Int = when (value) {
    is Number -> value.toInt()
    is String -> value.trim().toIntOrNull() ?: default
    else -> default
}

fun anyToDouble(value: Any?, default: Double): Double = when (value) {
    is Number -> value.toDouble()
    is String -> value.trim().toDoubleOrNull() ?: default
    else -> default
}

fun anyToString(value: Any?): String? = when (value) {
    null -> null
    is String -> value.trim().takeIf { it.isNotEmpty() }
    else -> value.toString().trim().takeIf { it.isNotEmpty() }
}

fun anyToBool(value: Any?, default: Boolean? = null): Boolean? = when (value) {
    is Boolean -> value
    is String -> value.trim().lowercase().let { s ->
        when (s) {
            "true", "yes", "1" -> true
            "false", "no", "0" -> false
            else -> default
        }
    }
    is Number -> value.toInt() != 0
    else -> default
}

fun anyToStringList(value: Any?): List<String> = when (value) {
    is List<*> -> value.mapNotNull { anyToString(it) }
    is String -> value.split(",").mapNotNull { it.trim().takeIf { s -> s.isNotEmpty() } }
    else -> emptyList()
}
