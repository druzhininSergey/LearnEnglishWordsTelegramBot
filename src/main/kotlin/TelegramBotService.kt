import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

const val TELEGRAM_BOT_API_BASE_URL = "https://api.telegram.org"

class TelegramBotService(private val botToken: String) {

    fun getUpdates(updateId: Long?): String {
        val urlGetUpdates = "$TELEGRAM_BOT_API_BASE_URL/bot$botToken/getUpdates?offset=$updateId"
        val client: HttpClient = HttpClient.newBuilder().build()
        val request: HttpRequest = HttpRequest.newBuilder().uri(URI.create(urlGetUpdates)).build()
        val response: HttpResponse<String> = client.send(request, HttpResponse.BodyHandlers.ofString())
        return response.body()
    }

    private fun sendMessage(json: Json, chatId: Long, text: String): String {
        val urlSendMenu = "$TELEGRAM_BOT_API_BASE_URL/bot$botToken/sendMessage"
        val requestBody = SendMessageRequest(
            chatId = chatId,
            text = text,
        )
        val requestBodyString = json.encodeToString(requestBody)
        val client: HttpClient = HttpClient.newBuilder().build()
        val request: HttpRequest = HttpRequest.newBuilder().uri(URI.create(urlSendMenu))
            .header("Content-type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(requestBodyString))
            .build()
        val response: HttpResponse<String> = client.send(request, HttpResponse.BodyHandlers.ofString())
        return response.body()
    }

    private fun sendMenu(json: Json, chatId: Long): String {
        val urlSendMenu = "$TELEGRAM_BOT_API_BASE_URL/bot$botToken/sendMessage"
        val requestBody = SendMessageRequest(
            chatId = chatId,
            text = "Основное меню",
            replyMarkup = ReplyMarkup(
                listOf(
                    listOf(
                        InlineKeyboard(text = "Изучать слова", callbackData = LEARN_WORDS_CLICKED),
                        InlineKeyboard(text = "Статистика", callbackData = STATISTICS_CLICKED),
                    ),
                    listOf(
                        InlineKeyboard(text = "Сбросить статистику", callbackData = RESET_CLICKED)
                    )
                )
            )
        )
        val requestBodyString = json.encodeToString(requestBody)
        val client: HttpClient = HttpClient.newBuilder().build()
        val request: HttpRequest = HttpRequest.newBuilder().uri(URI.create(urlSendMenu))
            .header("Content-type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(requestBodyString))
            .build()
        val response: HttpResponse<String> = client.send(request, HttpResponse.BodyHandlers.ofString())
        return response.body()
    }

    private fun sendQuestion(json: Json, chatId: Long, question: Question?): String? {
        val urlSendMessage = "$TELEGRAM_BOT_API_BASE_URL/bot$botToken/sendMessage"

        val requestBody = SendMessageRequest(
            chatId = chatId,
            text = question?.correctWord?.original.toString(),
            replyMarkup = ReplyMarkup(
                if (question == null) return null
                else {
                    listOf(question.variants.mapIndexed { index, word ->
                        InlineKeyboard(
                            text = word.translated, callbackData = "$CALLBACK_DATA_ANSWER_PREFIX$index"
                        )
                    })
                }
            )
        )
        val requestBodyString = json.encodeToString(requestBody)
        val client: HttpClient = HttpClient.newBuilder().build()
        val request: HttpRequest = HttpRequest.newBuilder().uri(URI.create(urlSendMessage))
            .header("Content-type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(requestBodyString))
            .build()
        val response: HttpResponse<String> = client.send(request, HttpResponse.BodyHandlers.ofString())
        return response.body()
    }

    private fun checkNextQuestionAndSend(json: Json, trainer: LearnWordsTrainer, chatId: Long) {
        val question = trainer.getNextQuestion()
        if (question == null) {
            sendMessage(json, chatId, "Вы выучили все слова в базе")
            return
        } else sendQuestion(json, chatId, question)
    }

    fun handleUpdate(update: Update, json: Json, trainers: HashMap<Long, LearnWordsTrainer>) {

        val userText = update.message?.text
        val chatId = update.message?.chat?.id ?: update.callbackQuery?.message?.chat?.id ?: return
        val data = update.callbackQuery?.data

        val trainer = trainers.getOrPut(chatId) { LearnWordsTrainer("$chatId.txt") }

        if (userText?.lowercase() == "hello") sendMessage(json, chatId, "Hello")
        if (userText?.lowercase() == "/start") sendMenu(json, chatId)
        if (data?.lowercase() == STATISTICS_CLICKED) {
            val statistics = trainer.getStatistics()
            val statisticsString = "Выучено ${statistics.learned} из ${statistics.total} слов | ${statistics.percent}%"
            sendMessage(json, chatId, statisticsString)
        }
        if (data?.lowercase() == LEARN_WORDS_CLICKED) {
            checkNextQuestionAndSend(json, trainer, chatId)
        }

        if (data?.startsWith(CALLBACK_DATA_ANSWER_PREFIX) == true) {
            val indexChosen = data.substringAfter(CALLBACK_DATA_ANSWER_PREFIX).toInt()
            if (trainer.checkAnswer(indexChosen)) {
                sendMessage(json, chatId, "Правильно")
            } else {
                sendMessage(
                    json,
                    chatId,
                    "Не правильно: ${trainer.question?.correctWord?.original} - ${trainer.question?.correctWord?.translated}"
                )
            }
            checkNextQuestionAndSend(json, trainer, chatId)
        }

        if (data == RESET_CLICKED) {
            trainer.resetProgress(trainers, chatId)
            sendMessage(json, chatId, "Прогресс сброшен")
        }
    }
}