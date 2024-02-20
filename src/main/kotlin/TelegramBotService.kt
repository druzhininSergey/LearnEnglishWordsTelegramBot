import java.net.URI
import java.net.URL
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

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
        val urlSendMessage = "$TELEGRAM_BOT_API_BASE_URL/bot$botToken/sendMessage?chat_id=$chatId&text=$text"
        val response = URL(urlSendMessage).readText()
        return response
    }
}