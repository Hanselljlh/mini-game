package net.sclan.minigames

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Boots the real MainActivity end to end: repositories construct (DataStore
 * hydration, billing connect attempt) and the Home hub renders.
 */
@RunWith(AndroidJUnit4::class)
class AppLaunchTest {

    @get:Rule
    val rule = createAndroidComposeRule<MainActivity>()

    @Test
    fun homeHubRenders() {
        rule.onNodeWithText("Pocket Arcade Offline").assertExists()
    }
}
