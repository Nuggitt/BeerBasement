package com.example.beerbasement.screens

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.google.firebase.auth.FirebaseUser
import org.junit.Rule
import org.junit.Test

class LoginTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testAuthenticationScreen() {
        var signInCalled = false
        var registerCalled = false

        // Set the content of the test
        composeTestRule.setContent {
            Authentication(
                user = null, // Pass null to simulate a guest user
                signIn = { email, password ->
                    signInCalled = true
                    assert(email == "123@123.dk") // Check if email is correct
                    assert(password == "123456") // Check if password is correct
                },
                register = { email, password ->
                    registerCalled = true
                }
            )
        }

        // Simulate user input for email and password
        composeTestRule.onNodeWithText("Email").performTextInput("123@123.dk")
        composeTestRule.onNodeWithText("Password").performTextInput("123456")

        // Click the Sign In button
        composeTestRule.onNodeWithText("Sign In").performClick()

        // Check that signIn function was called
        assert(signInCalled)

        // Click the Register button
        composeTestRule.onNodeWithText("Register").performClick()

        // Check that register function was called
        assert(registerCalled)
    }
}
