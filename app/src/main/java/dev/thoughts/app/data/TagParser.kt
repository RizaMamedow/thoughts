package dev.thoughts.app.data

/** Достаёт #теги из текста: "купить молоко #дом #срочно" -> текст "купить молоко", теги [дом, срочно]. */
object TagParser {
    private val tagRegex = Regex("""#([\p{L}\p{N}_-]+)""")

    data class Parsed(val text: String, val tags: Set<String>)

    fun parse(raw: String): Parsed {
        val tags = tagRegex.findAll(raw).map { it.groupValues[1].lowercase() }.toSet()
        val stripped = raw.replace(tagRegex, "")
            .lines()
            .joinToString("\n") { it.replace(Regex(" {2,}"), " ").trim() }
            .trim()
        // Если мысль состояла только из тегов — оставляем исходный текст, чтобы запись не была пустой
        return Parsed(text = stripped.ifEmpty { raw.trim() }, tags = tags)
    }

    /** Обратная склейка для редактирования. */
    fun join(text: String, tags: Collection<String>): String =
        if (tags.isEmpty()) text else text + "\n" + tags.joinToString(" ") { "#$it" }
}
