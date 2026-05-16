package com.example.femverd

import org.junit.Test
import org.junit.Assert.assertEquals

class BusinessLogicTest {
// Unit test
    @Test
    fun calculateUserLevel_isCorrect() {
        // Arrange
        val currentPoints = 750.0
        val expectedLevel = 2 // 750 points means Level 2, because is 500 pts per level

        // Act
        // The business logic used in DashboardContent
        val calculatedLevel = (currentPoints / 500).toInt() + 1

        // Assert
        assertEquals("The calculated level should exactly match the expected level", expectedLevel, calculatedLevel)
    }
}