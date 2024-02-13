import java.io.File

const val REQUIRED_CORRECT_ANSWERS = 3
const val NUMBER_OF_ANSWER_OPTIONS = 4

fun main() {

    val wordsFile: File = File("words.txt")
    val dictionary = mutableListOf<Word>()
    val lines: List<String> = wordsFile.readLines()

    for (line in lines) {
        val line = line.split("|")
        val word = Word(original = line[0], translated = line[1], correctAnswersCount = line[2].toIntOrNull() ?: 0)
        dictionary.add(word)
    }
    val unlearnedWords: MutableList<Word> =
        dictionary.filter { it.correctAnswersCount < REQUIRED_CORRECT_ANSWERS }.toMutableList()

    while (true) {
        println("Меню: 1- Учить слова, 2 - Статистика, 0 - Выход")
        val menuInput = readln().toIntOrNull()
        when (menuInput) {
            1 -> learnWords(dictionary, unlearnedWords)
            2 -> showStatistics(dictionary, unlearnedWords)
            0 -> {
                println("Завершение программы.")
                break
            }

            else -> println("Введен некорректный символ")
        }
    }
}