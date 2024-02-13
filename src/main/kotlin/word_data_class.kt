import java.io.File

data class Word(
    val original: String,
    val translated: String,
    var correctAnswersCount: Int = 0,
)

fun List<Word>.saveDictionary() {
    val wordsFile: File = File("words.txt")
    wordsFile.writeText("")
    this.forEach {
        wordsFile.appendText("${it.original}|${it.translated}|${it.correctAnswersCount}\n")
    }
}