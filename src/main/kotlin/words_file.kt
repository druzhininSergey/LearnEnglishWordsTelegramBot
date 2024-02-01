import java.io.File

const val REQUIRED_CORRECT_ANSWERS = 3
const val NUMBER_OF_ANSWER_OPTIONS = 4

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
    val unlearnedWords = dictionary.filter { it.correctAnswersCount < REQUIRED_CORRECT_ANSWERS }

    while (true) {
        println("Меню: 1- Учить слова, 2 - Статистика, 0 - Выход")
        val menuInput = readln().toIntOrNull()
        when (menuInput) {
            1 -> {
                if (unlearnedWords.isEmpty()) {
                    println("Вы выучили все слова.")
                    continue
                }
                val fourShuffledWords = if (unlearnedWords.size >= 4) {
                    unlearnedWords.shuffled().take(4)
                } else {
                    val learnedWords = dictionary - unlearnedWords.toSet()
                    val missingWords = NUMBER_OF_ANSWER_OPTIONS - unlearnedWords.size
                    unlearnedWords + learnedWords.shuffled().take(missingWords)
                }
                val correctWord = fourShuffledWords.filter { it in unlearnedWords }.random()

                println(correctWord.original)
                fourShuffledWords.shuffled().forEachIndexed { index, word ->
                    println("${index + 1}) ${word.translated}")
                }
            }

            2 -> {
                val learnedWordInPercent =
                    (((dictionary.size - unlearnedWords.size).toDouble() / dictionary.size) * 100).toInt()
                println("Выучено ${dictionary.size - unlearnedWords.size} из ${dictionary.size} слов | $learnedWordInPercent%")
            }

            0 -> {
                println("Завершение программы.")
                break
            }

            else -> println("Введен некорректный символ")
        }
    }
}