const val STATISTICS_CLICKED = "statistics_clicked"
const val LEARN_WORDS_CLICKED = "learn_words_clicked"
const val RESET_CLICKED = "reset_clicked"
const val CALLBACK_DATA_ANSWER_PREFIX = "answer_"
const val MENU_CLICKED = "/start"

fun main(args: Array<String>) {

    val botToken = args[0]
    var lastUpdateId: Long? = 0L
    val telegramBotService = TelegramBotService(botToken)

    val trainers = HashMap<Long, LearnWordsTrainer>()

    while (true) {
        Thread.sleep(2000)
        val response: Response? = telegramBotService.getUpdates(lastUpdateId)
        if (response?.result?.isEmpty() == true) continue
        val sortedUpdates = response?.result?.sortedBy { it.updateId }
        sortedUpdates?.forEach { handleUpdate(it, trainers, telegramBotService) }
        lastUpdateId = sortedUpdates?.last()?.updateId?.plus(1)
    }
}

fun handleUpdate(update: Update, trainers: HashMap<Long, LearnWordsTrainer>, telegramBotService: TelegramBotService) {

    val userText = update.message?.text
    val chatId = update.message?.chat?.id ?: update.callbackQuery?.message?.chat?.id ?: return
    val data = update.callbackQuery?.data

    val trainer = trainers.getOrPut(chatId) { LearnWordsTrainer("$chatId.txt") }

    if (userText?.lowercase() == MENU_CLICKED) telegramBotService.sendMenu(chatId)
    if (data?.lowercase() == STATISTICS_CLICKED) {
        val statistics = trainer.getStatistics()
        val statisticsString = "Выучено ${statistics.learned} из ${statistics.total} слов | ${statistics.percent}%"
        telegramBotService.sendMessage(chatId, statisticsString)
    }
    if (data?.lowercase() == LEARN_WORDS_CLICKED) {
        telegramBotService.checkNextQuestionAndSend(trainer, chatId)
    }

    if (data?.startsWith(CALLBACK_DATA_ANSWER_PREFIX) == true) {
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

    if (data == RESET_CLICKED) {
        trainer.resetProgress(trainers, chatId)
        telegramBotService.sendMessage(chatId, "Прогресс сброшен")
    }

    if (data == MENU_CLICKED) {
        telegramBotService.sendMenu(chatId)
    }
}