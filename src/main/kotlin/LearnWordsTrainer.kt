import kotlinx.serialization.Serializable
import java.io.File

@Serializable
data class Word(
    val original: String,
    val translated: String,
    var correctAnswersCount: Int = 0,
)

data class Statistics(
    val learned: Int,
    val total: Int,
    val percent: Int,
)

data class Question(
    val variants: List<Word>,
    val correctWord: Word,
)

class LearnWordsTrainer(
    private val fileName: String = "words.txt",
    private val requiredCorrectAnswers: Int = 3,
    val numberOfQuestionWords: Int = 4,
) {

    private val dictionary = loadDictionary()
    private val unlearnedWords: MutableList<Word> =
        dictionary.filter { it.correctAnswersCount < requiredCorrectAnswers }.toMutableList()
    var question: Question? = null

    fun getNextQuestion(): Question? {
        if (unlearnedWords.isEmpty()) return null
        val correctWord: Word
        val fourShuffledWords = if (unlearnedWords.size >= numberOfQuestionWords) {
            unlearnedWords.shuffled().take(numberOfQuestionWords).also { correctWord = it.random() }
        } else {
            val learnedWords = dictionary - unlearnedWords.toSet()
            val missingWords = numberOfQuestionWords - unlearnedWords.size
            unlearnedWords.also { correctWord = it.random() } + learnedWords.shuffled().take(missingWords)
        }
        question = Question(fourShuffledWords.shuffled(), correctWord)
        return question
    }

    fun checkAnswer(userAnswerIndex: Int?): Boolean {
        return question?.let {
            val correctAnswerIndex = it.variants.indexOf(it.correctWord)
            if (userAnswerIndex == correctAnswerIndex) {
                it.correctWord.correctAnswersCount++
                saveDictionary()
                if (it.correctWord.correctAnswersCount >= requiredCorrectAnswers) unlearnedWords.remove(it.correctWord)
                true
            } else false
        } ?: false
    }

    fun getStatistics(): Statistics {
        val learned = dictionary.size - unlearnedWords.size
        val total = dictionary.size
        val percent = try {
            learned * 100 / total
        } catch (e: ArithmeticException) {
            println("В словарь не добавлены слова")
            100
        }
        return Statistics(learned, total, percent)
    }

    private fun loadDictionary(): List<Word> {
        try {
            val wordsFile: File = File(fileName)
            if (!wordsFile.exists()) {
                File("words.txt").copyTo(wordsFile)
            }
            val dictionary = mutableListOf<Word>()
            wordsFile.readLines().forEach {
                val splitLine = it.split("|")
                dictionary.add(Word(splitLine[0], splitLine[1], splitLine[2].toIntOrNull() ?: 0))
            }
            return dictionary
        } catch (e: IndexOutOfBoundsException) {
            throw IllegalStateException("Некорректный файл")
        }
    }

    private fun saveDictionary() {
        val wordsFile: File = File(fileName)
        wordsFile.writeText("")
        for (word in dictionary) {
            wordsFile.appendText("${word.original}|${word.translated}|${word.correctAnswersCount}\n")
        }
    }

    fun resetProgress(trainers: HashMap<Long, LearnWordsTrainer>, chatId: Long) {
        dictionary.forEach { it.correctAnswersCount = 0 }
        saveDictionary()
        trainers[chatId] = LearnWordsTrainer("$chatId.txt")
    }

}