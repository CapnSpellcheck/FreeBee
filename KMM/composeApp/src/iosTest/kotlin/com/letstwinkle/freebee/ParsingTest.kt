package com.letstwinkle.freebee

import com.goncalossilva.resources.Resource
import com.letstwinkle.freebee.screens.loader.parseGame
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ParsingTest {
   @Test fun test_Oct1_2024() = runTest {
      val date = "20241001"
      val gameData = parseGame(LocalDate.parse(date, format = LocalDate.Formats.ISO_BASIC), readGame(date))
      assertEquals('i'.code, gameData.centerLetterCode, "centerLetterCode is correct")
      assertEquals("cvtole".toSet(), gameData.otherLetters.toSet(), "otherLetters is correct")
      assertEquals(144, gameData.geniusScore, "geniusScore is correct")
      assertEquals(206, gameData.maximumScore, "maximumScore is correct")
      assertEquals(48, gameData.allowedWords.size, "allowedWords has correct size")
      assertContains(gameData.allowedWords, "elicit", "allowedWords contains elicit")
      assertContains(gameData.allowedWords, "licit", "allowedWords contains licit")
      assertContains(gameData.allowedWords, "toilet", "allowedWords contains toilet")
      assertContains(gameData.allowedWords, "collective", "allowedWords contains collective")
   }
   
   @Test fun test_Oct1_2018() = runTest {
      val date = "20181001"
      val gameData = parseGame(LocalDate.parse(date, format = LocalDate.Formats.ISO_BASIC), readGame(date))
      assertEquals('i'.code, gameData.centerLetterCode, "centerLetterCode is correct")
      assertEquals("clyntv".toSet(), gameData.otherLetters.toSet(), "otherLetters is correct")
      assertEquals(108, gameData.geniusScore, "geniusScore is correct")
      assertEquals(154, gameData.maximumScore, "maximumScore is correct")
      assertEquals(33, gameData.allowedWords.size, "allowedWords has correct size")
      assertContains(gameData.allowedWords, "civvy", "allowedWords contains civvy")
      assertContains(gameData.allowedWords, "incivility", "allowedWords contains incivility")
      assertContains(gameData.allowedWords, "tint", "allowedWords contains tint")
   }
   
   @Test fun test_Jul5_2020_usesOnCenterLetterNotUnique() = runTest {
      val date = "20200705"
      var onCenterLetterNotUniqueCalled = false
      var centerLetterDeterminedCode: Int? = null
      val onCenterLetterNotUnique: suspend (List<Char>) -> Char = {
         onCenterLetterNotUniqueCalled = true
         centerLetterDeterminedCode = it.first().code
         it.first()
      }
      val gameData = parseGame(
         LocalDate.parse(date, format = LocalDate.Formats.ISO_BASIC),
         readGame(date),
         onCenterLetterNotUnique
      )
      assertTrue(onCenterLetterNotUniqueCalled, "onCenterLetterNotUnique was called")
      assertEquals(centerLetterDeterminedCode, gameData.centerLetterCode, "parsing used onCenterLetterNotUnique return value")
   }
   
   fun readGame(iso8601Date: String): String =
      Resource("src/commonTest/resources/${iso8601Date}_response.html").readText()
}
