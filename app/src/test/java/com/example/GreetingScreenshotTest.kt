package com.example

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.example.data.model.FormData
import com.example.data.model.Project
import com.example.ui.projects.BannerHeaderCard
import com.example.ui.projects.ProjectItemCard
import com.example.ui.theme.MiniAppForgeTheme
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [36])
class GreetingScreenshotTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun project_card_screenshot() {
        val testProject = Project(
            id = 1L,
            name = "TurboReward",
            createdAt = 1700000000000L,
            updatedAt = 1700000000000L,
            formDataJson = FormData().toJson()
        )

        composeTestRule.setContent {
            MiniAppForgeTheme {
                ProjectItemCard(
                    project = testProject,
                    onOpen = {},
                    onExport = {},
                    onDuplicate = {},
                    onDelete = {}
                )
            }
        }

        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/project_card.png")
    }
}
