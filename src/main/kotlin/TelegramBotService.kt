import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

const val TELEGRAM_BOT_API_BASE_URL = "https://api.telegram.org"

class TelegramBotService(private val botToken: String) {

    private val json = Json { ignoreUnknownKeys = true }

    fun getUpdates(updateId: Long?): Response? {
        val urlGetUpdates = "$TELEGRAM_BOT_API_BASE_URL/bot$botToken/getUpdates?offset=$updateId"
        val client: HttpClient = HttpClient.newBuilder().build()
        val request: HttpRequest = HttpRequest.newBuilder().uri(URI.create(urlGetUpdates)).build()
        val result: Result<HttpResponse<String>> =
            runCatching { client.send(request, HttpResponse.BodyHandlers.ofString()) }
        println(result.getOrNull()?.body())
        return result.getOrNull()?.body()
            ?.let { responseString -> json.decodeFromString<Response>(responseString) }
    }

    fun sendMessage(chatId: Long, text: String): String? {
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
        val result: Result<HttpResponse<String>> =
            runCatching { client.send(request, HttpResponse.BodyHandlers.ofString()) }
        return result.getOrNull()?.body()
    }

    fun sendMenu(chatId: Long): String? {
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
        val result: Result<HttpResponse<String>> =
            runCatching { client.send(request, HttpResponse.BodyHandlers.ofString()) }
        return result.getOrNull()?.body()
    }

    private fun sendQuestion(chatId: Long, question: Question?): String? {
        val urlSendMessage = "$TELEGRAM_BOT_API_BASE_URL/bot$botToken/sendMessage"

        val requestBody = SendMessageRequest(
            chatId = chatId,
            text = question?.correctWord?.original.toString().replaceFirstChar { it.titlecase() },
            replyMarkup = ReplyMarkup(
                if (question == null) return null
                else {
                    listOf(
                        question.variants.take(2).mapIndexed { index, word ->
                            InlineKeyboard(
                                text = word.translated.replaceFirstChar { it.titlecase() },
                                callbackData = "$CALLBACK_DATA_ANSWER_PREFIX$index"
                            )
                        },
                        question.variants.drop(2).mapIndexed { index, word ->
                            InlineKeyboard(
                                text = word.translated.replaceFirstChar { it.titlecase() },
                                callbackData = "$CALLBACK_DATA_ANSWER_PREFIX${index + 2}"
                            )
                        },
                        listOf(
                            InlineKeyboard(text = "Вернуться в меню", callbackData = MENU_CLICKED)
                        )
                    )
                }
            )
        )
        val requestBodyString = json.encodeToString(requestBody)
        val client: HttpClient = HttpClient.newBuilder().build()
        val request: HttpRequest = HttpRequest.newBuilder().uri(URI.create(urlSendMessage))
            .header("Content-type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(requestBodyString))
            .build()
        val result: Result<HttpResponse<String>> =
            runCatching { client.send(request, HttpResponse.BodyHandlers.ofString()) }
        return result.getOrNull()?.body()
    }

    fun checkNextQuestionAndSend(trainer: LearnWordsTrainer, chatId: Long) {
        val question = trainer.getNextQuestion()
        if (question == null) {
            sendMessage(chatId, "Вы выучили все слова в базе")
            return
        } else sendQuestion(chatId, question)
    }
}