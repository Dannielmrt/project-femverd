package com.example.femverd

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.example.femverd.ui.screens.help.FaqCard
import org.junit.Rule
import org.junit.Test

class FaqCardUiTest {

    // Rule to launch the Compose environment for UI Node Tree verification
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun faqCard_displaysQuestionCorrectly() {
        // Arrange
        val testQuestion = "How do I recycle plastic?"
        val testAnswer = "Put it in the yellow bin."

        // Set the content to our isolated FaqCard component
        composeTestRule.setContent {
            FaqCard(question = testQuestion,answer = testAnswer)
        }

        // Act (nothing)

        // Assert
        // Checks if the question text exists and is visible on screen
        composeTestRule.onNodeWithText(testQuestion).assertIsDisplayed()
    }
}