package com.letstwinkle.freebee.screens.loader

import kotlinx.datetime.LocalDate

interface HTMLDocument {
   val rootElement: Node
}

interface Node {
   fun xpathNodes(expr: String): NodeList
   fun xpathNode(expr: String): Node?
   fun xpathStr(expr: String): String

   fun textContent(): String
}

interface NodeList : Iterable<Node> {
   val size: Int
   fun item(i: Int): Node

   override operator fun iterator() = object : Iterator<Node> {
      private var cur = 0
      override fun hasNext(): Boolean = cur < size

      override fun next(): Node = item(cur++)
   }
}

expect fun createHTMLDocument(html: String): HTMLDocument

private val endingNumberRegex = Regex("\\d+$")

data class GameData(
   val date: LocalDate,
   val allowedWords: Set<String>,
   val centerLetterCode: Int,
   val otherLetters: String,
   val geniusScore: Short,
   val maximumScore: Short
)

suspend fun parseGame(
   date: LocalDate,
   html: String,
   onCenterLetterNotUnique: (suspend (List<Char>) -> Char)? = null
): GameData
{
   val document = createHTMLDocument(html)
   val root = document.rootElement
   var answerList = root.xpathNode("//*[@id='main-answer-list'][1]")
   val puzzleNotes: Node?
   if (answerList != null) {
      puzzleNotes = root.xpathNode("//*[@id='puzzle-notes'][1]")
   } else {
      answerList = root.xpathNode("//*[@class='answer-list'][1]")
      puzzleNotes = answerList?.xpathNode("./following-sibling::div[1]")
   }

   if (answerList == null)
      throw ParseError("Couldn't find answer list")
   if (puzzleNotes == null)
      throw ParseError("Couldn't find scoring information")

   val answerNodes = answerList.xpathNodes("./ul/li//text()[not(parent::a)]")
   val allowedWords = answerNodes.mapNotNull {
      it.textContent().trim().let { it.ifEmpty { null } }
   }

   val maximumPuzzleScore =
      puzzleNotes.xpathNode(".//*[contains(., 'Maximum Puzzle Score')][1]")
         ?: throw ParseError("Couldn't find Maximum Puzzle Score")
   val neededForGenius = puzzleNotes.xpathNode(".//*[contains(., 'Needed for Genius')][1]")
      ?: throw ParseError("Couldn't find Needed for Genius")

   val maximumScore = endingNumberRegex.find(maximumPuzzleScore.textContent())?.value?.toShort()
      ?: throw ParseError("Couldn't extract Maximum Puzzle Score (text content was: ${maximumPuzzleScore.textContent()})")
   val geniusScore = endingNumberRegex.find(neededForGenius.textContent())?.value?.toShort()
      ?: throw ParseError("Couldn't extract Needed for Genius (text content was: ${neededForGenius.textContent()})")

   var lettersResult = determineLetters(allowedWords)
   if (lettersResult is DetermineLettersResult.NotUnique) {
      val centerLetter =
         onCenterLetterNotUnique?.invoke(lettersResult.centerLetterPossibilities)
      lettersResult = determineLetters(allowedWords, centerLetter)
   }
   if (lettersResult is DetermineLettersResult.Irreconcilable) {
      throw ParseError("Couldn't identify the game letters")
   }

   lettersResult as DetermineLettersResult.Unique
   return GameData(
      date,
      allowedWords.toSet(),
      lettersResult.centerLetter.code,
      lettersResult.otherLetters,
      geniusScore,
      maximumScore
   )
}

private fun determineLetters(words: List<String>, centerLetter: Char? = null): DetermineLettersResult {
   val foundLetters = HashSet<Char>(7)
   val centerCandidates = HashSet<Char>(7).apply {
      if (centerLetter != null)
         add(centerLetter)
      else
         addAll(words.first().asIterable())
   }

   for (word in words) {
      foundLetters.addAll(word.asIterable())
      centerCandidates.retainAll(word.toSet())
      if (foundLetters.size == 7 && (centerLetter != null || centerCandidates.size == 1))
         break
   }

   if (foundLetters.size != 7)
      return DetermineLettersResult.Irreconcilable
   if (centerCandidates.size != 1)
      return DetermineLettersResult.NotUnique(centerCandidates.toList())

   val centerLetter = centerLetter ?: centerCandidates.first()
   foundLetters.remove(centerLetter)
   return DetermineLettersResult.Unique(centerLetter, foundLetters.joinToString(""))
}

private sealed class DetermineLettersResult {
   data class Unique(val centerLetter: Char, val otherLetters: String) : DetermineLettersResult()
   data class NotUnique(val centerLetterPossibilities: List<Char>) : DetermineLettersResult()
   data object Irreconcilable : DetermineLettersResult()
}

private class ParseError(message: String) : Exception(message)
