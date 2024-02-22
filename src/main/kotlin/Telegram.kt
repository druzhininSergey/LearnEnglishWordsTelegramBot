const val STATISTICS_CLICKED = "statistics_clicked"
const val LEARN_WORDS_CLICKED = "learn_words_clicked"
const val CALLBACK_DATA_ANSWER_PREFIX = "answer_"

fun main(args: Array<String>) {

    val botToken = args[0]
    var lastUpdateId: Int? = 0
    val telegramBotService = TelegramBotService(botToken)

    val updateIdRegex: Regex = "\"update_id\":(.+?),".toRegex()
    val messageTextRegex: Regex = "\"text\":\"(.+?)\"".toRegex()
    val chatIdRegex: Regex = "\"chat\":.\"id\":(\\d+),".toRegex()
    val dataRegex: Regex = "\"data\":\"(.+?)\"".toRegex()

    val trainer = try {
        LearnWordsTrainer()
    } catch (e: Exception) {
        println("Невозможно запустить словарь")
        return
    }

    while (true) {
        Thread.sleep(2000)
        val updates: String = telegramBotService.getUpdates(lastUpdateId)
        println(updates)

        val updateId = updateIdRegex.find(updates)?.groups?.get(1)?.value?.toIntOrNull() ?: continue
        lastUpdateId = updateId.plus(1)

        val chatId = chatIdRegex.find(updates)?.groups?.get(1)?.value?.toInt()
        val userText = messageTextRegex.find(updates)?.groups?.get(1)?.value
        val data = dataRegex.find(updates)?.groups?.get(1)?.value

        if (userText?.lowercase() == "hello" && chatId != null) telegramBotService.sendMessage(chatId, "Hello")
        if (userText?.lowercase() == "/start" && chatId != null) telegramBotService.sendMenu(chatId)
        if (data?.lowercase() == STATISTICS_CLICKED && chatId != null) {
            val statistics = trainer.getStatistics()
            val statisticsString = "Выучено ${statistics.learned} из ${statistics.total} слов | ${statistics.percent}%"
            telegramBotService.sendMessage(chatId, statisticsString)
        }
        if (data?.lowercase() == LEARN_WORDS_CLICKED && chatId != null) {
            telegramBotService.checkNextQuestionAndSend(trainer, chatId)
        }
        if (data?.startsWith(CALLBACK_DATA_ANSWER_PREFIX) == true && chatId != null) {
            val indexChosen = data.substringAfter(CALLBACK_DATA_ANSWER_PREFIX).toInt()
            if (trainer.checkAnswer(indexChosen)) {
                telegramBotService.sendMessage(chatId, "Правильно")
            } else {
                telegramBotService.sendMessage(
                    chatId,
                    "Не правильно: ${trainer.question?.correctWord?.original} - ${trainer.question?.correctWord?.translated}"
                )
            }
            telegramBotService.checkNextQuestionAndSend(trainer, chatId)
        }
    }
}