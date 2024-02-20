data class Word(
    val original: String,
    val translated: String,
    var correctAnswersCount: Int = 0,
)

fun Question.asConsoleString(): String {
    val variants = this.variants
        .mapIndexed { index: Int, word: Word -> "${index + 1}) ${word.translated}" }
        .joinToString("\n")
    return this.correctWord.original + "\n" + variants + "\n0) Выйти в меню"
}

fun main() {

    val trainer = try {
        LearnWordsTrainer()
    } catch (e: Exception) {
        println("Невозможно запустить словарь")
        return
    }

    while (true) {
        println("Меню: 1 - Учить слова, 2 - Статистика, 0 - Выход")
        val menuInput = readln().toIntOrNull()
        when (menuInput) {
            1 -> {
                while (true) {
                    val question = trainer.getNextQuestion()

                    if (question == null) {
                        println("Вы выучили все слова.")
                        break
                    } else {
                        println(question.asConsoleString())

                        var inputAnswer: Int?
                        do {
                            inputAnswer = readln().toIntOrNull()
                            if (inputAnswer == null || inputAnswer > trainer.numberOfQuestionWords) {
                                println("Введите цифру (вариант ответа) либо 0 для выхода в меню.")
                                continue
                            }
                        } while (inputAnswer !in 0..trainer.numberOfQuestionWords)
                        if (inputAnswer == 0) break

                        if (trainer.checkAnswer(inputAnswer?.minus(1))) {
                            println("Правильно!")
                        } else println("Неправильно: ${question.correctWord.original} - ${question.correctWord.translated}")

                    }
                }
            }

            2 -> {
                val statistics = trainer.getStatistics()
                println("Выучено ${statistics.learned} из ${statistics.total} слов | ${statistics.percent}%")
            }

            0 -> {
                println("Завершение программы.")
                break
            }

            else -> println("Введен некорректный символ")
        }
    }
}