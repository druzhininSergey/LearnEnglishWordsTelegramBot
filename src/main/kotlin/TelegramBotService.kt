
import java.net.URI
import java.net.URLEncoder
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.charset.StandardCharsets

const val TELEGRAM_BOT_API_BASE_URL = "https://api.telegram.org"

class TelegramBotService(private val botToken: String) {

    fun getUpdates(updateId: Int?): String {
        val urlGetUpdates = "$TELEGRAM_BOT_API_BASE_URL/bot$botToken/getUpdates?offset=$updateId"
        val client: HttpClient = HttpClient.newBuilder().build()
        val request: HttpRequest = HttpRequest.newBuilder().uri(URI.create(urlGetUpdates)).build()
        val response: HttpResponse<String> = client.send(request, HttpResponse.BodyHandlers.ofString())
        return response.body()
    }

    fun sendMessage(chatId: Int?, text: String): String {
        val encoded = URLEncoder.encode(
            text,
            StandardCharsets.UTF_8
        )
        println(encoded)
        val urlSendMessage = "$TELEGRAM_BOT_API_BASE_URL/bot$botToken/sendMessage?chat_id=$chatId&text=$encoded"
        val client: HttpClient = HttpClient.newBuilder().build()
        val request: HttpRequest = HttpRequest.newBuilder().uri(URI.create(urlSendMessage)).build()
        val response: HttpResponse<String> = client.send(request, HttpResponse.BodyHandlers.ofString())
        return response.body()
    }

    fun sendMenu(chatId: Int?): String {
        val urlSendMenu = "$TELEGRAM_BOT_API_BASE_URL/bot$botToken/sendMessage"
        val sendMenuBody = """
            {
                "chat_id": $chatId,
                "text": "Основное меню",
                "reply_markup": {
                    "inline_keyboard": [
                        [
                            {
                                "text": "Изучить слова",
                                "callback_data": "$LEARN_WORDS_CLICKED"
                            },
                            {
                                "text": "Статистика",
                                "callback_data": "$STATISTICS_CLICKED"
                            }
                        ]
                    ]
                }
            }
        """.trimIndent()
        val client: HttpClient = HttpClient.newBuilder().build()
        val request: HttpRequest = HttpRequest.newBuilder().uri(URI.create(urlSendMenu))
            .header("Content-type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(sendMenuBody))
            .build()
        val response: HttpResponse<String> = client.send(request, HttpResponse.BodyHandlers.ofString())
        return response.body()
    }

    private fun sendQuestion(chatId: Int?, question: Question?): String? {
        val urlSendMessage = "$TELEGRAM_BOT_API_BASE_URL/bot$botToken/sendMessage"

        val inlineKeyboard: String? = question?.variants?.mapIndexed { index, word ->
            """
                        {
                            "text": "${word.translated}",
                            "callback_data": "${CALLBACK_DATA_ANSWER_PREFIX}${index}"
                        }
                    """.trimIndent()
        }?.joinToString(",")

        val sendQuestionBody = """
            {
                "chat_id": $chatId,
                "text": "${question?.correctWord?.original}",
                "reply_markup": {
                    "inline_keyboard": [
                        [$inlineKeyboard]
                    ]
                }
            }
        """.trimIndent()
        val client: HttpClient = HttpClient.newBuilder().build()
        val request: HttpRequest = HttpRequest.newBuilder().uri(URI.create(urlSendMessage))
            .header("Content-type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(sendQuestionBody))
            .build()
        val response: HttpResponse<String> = client.send(request, HttpResponse.BodyHandlers.ofString())
        return response.body()
    }

    fun checkNextQuestionAndSend(trainer: LearnWordsTrainer, chatId: Int){
        val question = trainer.getNextQuestion()
        if (question == null) {
            sendMessage(chatId, "Вы выучили все слова в базе")
            return
        } else sendQuestion(chatId, question)
    }

}