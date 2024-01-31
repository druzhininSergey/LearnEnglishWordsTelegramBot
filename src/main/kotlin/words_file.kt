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
    val learnedWordsCount = dictionary.filter { it.correctAnswersCount >= REQUIRED_CORRECT_ANSWERS }
    val unlearnedWords = dictionary.filter { it.correctAnswersCount < REQUIRED_CORRECT_ANSWERS }

    while (true) {
        println("Меню: 1- Учить слова, 2 - Статистика, 0 - Выход")
        val menuInput = readln().toIntOrNull()
        when (menuInput) {
            1 -> {
                if (learnedWordsCount.size == lines.size) {
                    println("Вы выучили все слова.")
                    continue
                }
                val fourShuffledWords = unlearnedWords.shuffled().take(4)
                val correctWord = fourShuffledWords.random()
                var resultsCounter = 0

                println(correctWord.original)
                for (i in fourShuffledWords.shuffled()) {
                    resultsCounter++
                    println("${resultsCounter}) ${i.translated}")
                }
            }

            2 -> {
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