fun showStatistics(dictionary: List<Word>, unlearnedWords: List<Word>) {
    val learnedWordInPercent =
        (((dictionary.size - unlearnedWords.size).toDouble() / dictionary.size) * 100).toInt()
    println("Выучено ${dictionary.size - unlearnedWords.size} из ${dictionary.size} слов | $learnedWordInPercent%")
}

fun learnWords(dictionary: List<Word>, unlearnedWords: MutableList<Word>) {
    if (unlearnedWords.isEmpty()) {
        println("Вы выучили все слова.")
        return
    }
    do {
        val correctWord: Word
        val fourShuffledWords = if (unlearnedWords.size >= NUMBER_OF_ANSWER_OPTIONS) {
            unlearnedWords.shuffled().take(NUMBER_OF_ANSWER_OPTIONS).also { correctWord = it.random() }
        } else {
            val learnedWords = dictionary - unlearnedWords.toSet()
            val missingWords = NUMBER_OF_ANSWER_OPTIONS - unlearnedWords.size
            unlearnedWords.also { correctWord = it.random() } + learnedWords.shuffled().take(missingWords)
        }

        println(correctWord.original)
        val wordsInTest = fourShuffledWords.shuffled()
        wordsInTest.forEachIndexed { index, word ->
            println("${index + 1}) ${word.translated}")
        }
        println("Введите 0 для выхода в главное меню")

        var inputAnswer: Int?
        do {
            inputAnswer = readln().toIntOrNull()
            if (inputAnswer == null || inputAnswer > NUMBER_OF_ANSWER_OPTIONS) {
                println("Введите цифру (вариант ответа) либо 0 для выхода в меню.")
                continue
            }
        } while (inputAnswer !in 0..NUMBER_OF_ANSWER_OPTIONS)

        when (inputAnswer) {
            wordsInTest.indexOf(correctWord) + 1 -> {
                correctWord.correctAnswersCount++
                dictionary.saveDictionary()
                if (correctWord.correctAnswersCount >= REQUIRED_CORRECT_ANSWERS) unlearnedWords.remove(correctWord)
                println("Правильно!")
            }

            0 -> continue
            else -> println("Неправильно: ${correctWord.original} - ${correctWord.translated}")
        }
    } while (inputAnswer != 0 && unlearnedWords.isNotEmpty())

}