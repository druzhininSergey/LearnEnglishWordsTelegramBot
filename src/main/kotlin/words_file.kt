import java.io.File

data class Word(
    val original: String,
    val translated: String,
    val correctAnswersCount: Int = 0,
)

fun main() {

    val wordsFile: File = File("words.txt")
    wordsFile.createNewFile()
    wordsFile.appendText("hello|привет|3\n")
    wordsFile.appendText("dog|собака|2\n")
    wordsFile.appendText("cat|кошка|")
    val dictionary = mutableListOf<Word>()
    val lines: List<String> = wordsFile.readLines()
    for (line in lines) {
        val line = line.split("|")
        val word = Word(original = line[0], translated = line[1], correctAnswersCount = line[2].toIntOrNull() ?: 0)
        dictionary.add(word)
    }
    dictionary.forEach {
        println("Оригинал: ${it.original}| Перевод: ${it.translated}| Количество правильных ответов: ${it.correctAnswersCount}")
    }
}