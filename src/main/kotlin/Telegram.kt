fun main(args: Array<String>) {

    val botToken = args[0]
    var updateId: Int? = 0
    val telegramBotService = TelegramBotService()

    while (true) {
        Thread.sleep(2000)
        val updates: String = telegramBotService.getUpdates(botToken, updateId)
        println(updates)

        updateId = getValueByRegex(findBy = "\"update_id\":(.+?),", updates = updates)?.toInt()
        updateId = updateId?.plus(1)

        val chatId = getValueByRegex("\"chat\":.\"id\":(\\d+),", updates)?.toInt()
        val userText = getValueByRegex("\"text\":\"(.+?)\"", updates)

        if (userText == "Hello") telegramBotService.sendMessage(botToken, chatId, "Hello")
    }
}

fun getValueByRegex(findBy: String, updates: String): String? {
    val valueRegex: Regex = findBy.toRegex()
    val matchResult: MatchResult? = valueRegex.find(updates)
    val groups = matchResult?.groups
    return groups?.get(1)?.value
}