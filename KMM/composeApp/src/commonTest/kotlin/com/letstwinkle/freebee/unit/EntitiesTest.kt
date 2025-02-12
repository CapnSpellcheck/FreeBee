package com.letstwinkle.freebee.unit

import com.letstwinkle.freebee.database.*
import com.letstwinkle.freebee.util.*
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlin.test.*

class EntitiesTest {
   lateinit var game: IGame<*>
   lateinit var gameWithWords: IGameWithWords<*>
   
   @BeforeTest fun setUp() {
      game = MockGame(
         LocalDate.fromEpochDays(0),
         setOf("abc", "abcd"),
         'a'.code,
         "bcdefg",
         1,
         1,
         linkedSetOf(MockWord(value = "foo"), MockWord(value = "bar")),
      )
      gameWithWords = game as MockGame
   }
   
   @Test fun testGame_currentWordDisplay() = runTest {
      assertEquals("_", game.currentWordDisplay, "currentWord is empty")
      
      game.currentWord = "test"
      assertEquals("TEST_", game.currentWordDisplay, "currentWord is \"test\"")
   }
   
   @Test fun testGame_centerLetterCharacter() = runTest {
      assertEquals('a', game.centerLetterCharacter)
   }
   
   @Test fun testGame_isComplete() = runTest {
      assertFalse(game.isComplete, "score < maximumScore - game not complete")
      
      game.score = game.maximumScore
      assertTrue(game.isComplete, "score == maximumScore - game complete")
   }
   
   @Test fun testGame_isGenius() = runTest {
      assertFalse(game.isGenius, "score < geniusScore - game not genius")
      
      game.score = game.geniusScore
      assertTrue(game.isGenius, "score == geniusScore - game genius")
   }
   
   @Test fun testGame_isPangram() = runTest {
      assertFalse(game.isPangram("a"), "\"a\" is not a pangram")
      assertFalse(game.isPangram("bcdefg"), "\"bcdefg\" is not a pangram")
      assertFalse(game.isPangram("bcdefgbcdefg"), "\"bcdefgbcdefg\" is not a pangram")
      
      assertTrue(game.isPangram("bcdaefg"), "\"bcdaefg\" is a pangram")
      assertTrue(game.isPangram("bcdaefgbag"), "\"bcdaefgbag\" is a pangram")
   }
   
   @Test fun testGameWithWords_hasEnterted() = runTest {
      assertFalse(gameWithWords.hasEntered("baz"), "word isn't in enteredWords")
      assertTrue(gameWithWords.hasEntered("foo"), "word is in enteredWords")
   }
   
   @Test fun testGameWithWords_isAllowed() = runTest {
      assertFalse(gameWithWords.isAllowed("abd"), "word isn't in allowedWords")
      assertTrue(gameWithWords.isAllowed("abcd"), "word is in allowedWords")
   }
}
