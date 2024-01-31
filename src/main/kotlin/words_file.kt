import java.io.File

const val REQUIRED_CORRECT_ANSWERS = 3

data class Word(
    val original: String,
    val translated: String,
    val correctAnswersCount: Int = 0,
)

fun main() {

    val wordsFile: File = File("words.txt")
    val dictionary = mutableListOf<Word>()
    val lines: List<String> = wordsFile.readLines()

    for (line in lines) {
        val line = line.split("|")
        val word = Word(original = line[0], translated = line[1], correctAnswersCount = line[2].toIntOrNull() ?: 0)
        dictionary.add(word)
    }

    while (true) {
        println("Меню: 1- Учить слова, 2 - Статистика, 0 - Выход")
        val input = readln().toIntOrNull()
        when (input) {
            1 -> println("Выбран пункт \"Учить слова\".")
            2 -> {
                val learnedWordsCount = dictionary.filter { it.correctAnswersCount >= REQUIRED_CORRECT_ANSWERS }
                val learnedWordInPercent = ((learnedWordsCount.size.toDouble() / lines.size) * 100).toInt()
                println("Выучено ${learnedWordsCount.size} из ${lines.size} слов | $learnedWordInPercent%")
            }

            0 -> {
                println("Завершение программы.")
                break
            }

            else -> println("Введен некорректный символ")
        }
    }
}