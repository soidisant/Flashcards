package flashcards

import java.io.File

data class FlashCard(
    val term: String,
    var definition: String,
    var wrongAnswer: Int = 0
)

class FlashCardsGame {

    companion object {
        private enum class MenuItems(val display: String) {
            WRONG_INPUT("try again"),
            ADD("add"),
            REMOVE("remove"),
            IMPORT("import"),
            EXPORT("export"),
            ASK("ask"),
            EXIT("exit"),
            LOG("log"),
            HARDEST("hardest card"),
            RESET("reset stats")
        }
    }

    private var cards = mutableListOf<FlashCard>()
    private var logs: String = ""

    private fun println(text: Any?) {
        print("$text\n")
    }

    private fun print(text: Any?) {
        log(text.toString())
        kotlin.io.print(text)
    }

    private fun log(text: String) {
        logs += text
    }

    private fun readln(): String {
        return kotlin.io.readln().also { log("$it\n") }
    }

    fun start(initialImportFile: String = "", exportFileOnExit: String = "") {

        if (initialImportFile.isNotBlank()) {
            importFlashCard(initialImportFile)
        }
        do {
            printMenu()

            when (
                readln().let { input ->
                    MenuItems.values().firstOrNull { it.display == input } ?: MenuItems.WRONG_INPUT
                }
            ) {
                MenuItems.WRONG_INPUT -> println(MenuItems.WRONG_INPUT.display)
                MenuItems.ADD -> addFlashCard()
                MenuItems.REMOVE -> remove()
                MenuItems.IMPORT -> importFlashCard()
                MenuItems.EXPORT -> exportFlashCards()
                MenuItems.ASK -> ask()
                MenuItems.LOG -> writeLogs()
                MenuItems.HARDEST -> hardestCard()
                MenuItems.RESET -> resetCards()
                MenuItems.EXIT -> {
                    if (exportFileOnExit.isNotEmpty()) {
                        exportFlashCards(exportFileOnExit)
                    }
                    println("Bye bye!")
                    return
                }
            }
        } while (true)
    }

    private fun resetCards() {
        cards.forEach { it.wrongAnswer = 0 }
        println("Card statistics have been reset.")
    }

    private fun hardestCard() {
        val hardestCards = cards.filter { it.wrongAnswer > 0 && it.wrongAnswer == cards.maxOf { it.wrongAnswer } }

        if (hardestCards.isEmpty()) {
            println("There are no cards with errors.")
        } else if (hardestCards.size == 1) {
            println("The hardest card is \"${hardestCards.first().term}\". You have ${hardestCards.first().wrongAnswer} errors answering it")
        } else {
            val str = hardestCards.joinToString(", ") { "\"${it.term}\"" }
            println("The hardest cards are $str. You have ${hardestCards.first().wrongAnswer} errors answering them.")
        }
    }

    private fun writeLogs() {
        println("File name:")
        readln().let { filename ->
            if (filename.isNotBlank()) {
                File(filename).also { file ->
                    file.writeText(logs)
                    println("The log has been saved.")
                }
            }
        }
    }

    private fun ask() {
        var nb: Int? = null
        do {
            println("How many times to ask?")
            try {
                nb = readln().toInt()
            } catch (e: java.lang.NumberFormatException) {
                println("not a number!")
            }
        } while (nb == null)
        play(nb)
    }

    private fun exportFlashCards() {
        println("File name:")
        exportFlashCards(readln())
    }

    private fun exportFlashCards(fileName: String) {
        if (fileName.isNotBlank()) {
            File(fileName).also { file ->
                file.writeText("")
                cards.forEach {
                    file.appendText("\"${it.term}\"=\"${it.definition}\"${it.wrongAnswer}\n")
                }
                println("${cards.size} cards have been saved.")
            }
        }
    }

    private fun importFlashCard(term: String, definition: String, wrongAnswer: Int) {
        cards.firstOrNull {
            it.term == term
        }?.also {
            it.definition = definition
        } ?: cards.add(FlashCard(term, definition, wrongAnswer))
    }

    private fun importFlashCard() {
        println("File name:")
        importFlashCard(readln())
    }

    private fun importFlashCard(fileName: String) {
        val regex = "\"(?<term>.*)\"=\"(?<def>.*)\"(?<wrongs>\\d*)?".toRegex()
        File(fileName).also { file ->
            if (file.exists()) {
                var count = 0
                file.forEachLine { line ->
                    if (regex.matches(line)) {
                        importFlashCard(
                            regex.find(line)!!.groups["term"]!!.value,
                            regex.find(line)!!.groups["def"]!!.value,
                            regex.find(line)!!.groups["wrongs"]?.value?.toIntOrNull() ?: 0
                        )
                        count++
                    }
                }
//                count = cards.size - count
                println("$count cards have been loaded.")
            } else {
                println("File not found.")
            }
        }
    }

    private fun remove() {
        println("Which card?")
        readln().let { cardToDelete ->
            cards.firstOrNull {
                it.term == cardToDelete
            }?.also {
                cards.remove(it)
                println("The card has been removed.")
            } ?: println("Can't remove \"$cardToDelete\": there is no such card.")
        }
    }

    private fun play(nbCards: Int) {
        for (i in 1..nbCards) {
//            val card = cards[Random.nextInt(0, cards.size)]
            val card = cards[i - 1]
            println("Print the definition of \"${card.term}\":")
            checkAnswer(card, readln())
        }
    }

    private fun addFlashCard() {
        println("The card:")
        var term: String? = null
        do {
            if (term != null) {
                println("The card \"$term\" already exists.")
                return
            }
            term = readln()
        } while (term.isNullOrEmpty() ||
            cards.count { it.term == term } > 0
        )
        println("The definition of the card:")
        var definition: String? = null
        do {
            if (definition != null) {
                println("The definition \"$definition\" already exists.")
                return
            }
            definition = readln()
        } while (definition.isNullOrEmpty() ||
            cards.count { it.definition == definition } > 0
        )

        cards.add(FlashCard(term, definition))
        println("The pair (\"$term\":\"$definition\") has been added")
    }

    private fun checkAnswer(card: FlashCard, answer: String) {
        if (card.definition == answer) {
            println("Correct!")
        } else {
            print("Wrong. The right answer is \"${card.definition}\"")
            card.wrongAnswer++
            try {
                println(", but your definition is correct for \"${cards.first { it.definition == answer }.term}\"")
            } catch (e: NoSuchElementException) {
                println(".")
            }
        }
    }

    private fun printMenu() {
        println(
            "Input the action (${
            MenuItems.values().filter { it.ordinal > 0 }.joinToString(", ") { it.display }
            }):"
        )
    }
}

fun main(args: Array<String>) {
    val flashCardsGame = FlashCardsGame()
    if (args.isEmpty()) {
        flashCardsGame.start()
    } else if (args.size % 2 == 0) {
        var importFile = ""
        var exportFile = ""
        var i = 0
        while (i <= (args.size / 2)) {
            val param = args[i]
            try {
                when (param) {
                    "-import" -> importFile = args[i + 1]
                    "-export" -> exportFile = args[i + 1]
                    else -> {
                        println("wrong paramaters")
                        return
                    }
                }
            } finally {
                i = i + 2
            }
        }
        flashCardsGame.start(importFile, exportFile)
    }
}
