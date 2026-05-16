package com.example.femverd

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.example.femverd.ui.screens.auth.LoginScreen
import org.junit.Rule
import org.junit.Test

// UI Test
class LoginUiTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun loginScreen_displaysAppTitleAndButton() {
        // Arrange
        // Set the content to our LoginScreen component
        composeTestRule.setContent {
            LoginScreen(
                onLoginSuccess = {},
                onNavigateToRegister = {}
            )
        }

        // Act (nothing)

        // Assert
        // Checks if the title "FemVerd" exists and is visible on screen
        composeTestRule.onNodeWithText("FemVerd").assertIsDisplayed()
        // Checks if the specific action button exists
        composeTestRule.onNodeWithText("LOG IN").assertIsDisplayed()
    }
}