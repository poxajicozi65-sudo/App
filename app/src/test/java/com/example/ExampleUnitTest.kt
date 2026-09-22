package com.example

import com.example.ai.DiffHelper
import com.example.data.model.BotButton
import com.example.data.model.BotFlow
import com.example.data.model.BotNode
import com.example.data.model.BotNodeType
import com.example.data.model.FormData
import com.example.data.model.Project
import com.example.data.templates.BotIdeas
import com.example.data.templates.BotTemplates
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleUnitTest {

    @Test
    fun testFormDataJsonRoundTrip() {
        val original = FormData(
            appName = "CoinStorm",
            appTagline = "Play & Win",
            accentColor = "#10B981",
            signupBonus = 250,
            adWatchReward = 35,
            referralBonus = 75,
            minWithdrawal = 1000,
            coinRateCoins = 2000,
            coinRateCurrency = 20.0,
            currencySymbol = "$",
            enableWatchEarn = true,
            enableSpinWheel = false,
            paymentMethods = listOf("PayPal", "USDT")
        )

        val json = original.toJson()
        val restored = FormData.fromJson(json)

        assertEquals("CoinStorm", restored.appName)
        assertEquals("Play & Win", restored.appTagline)
        assertEquals("#10B981", restored.accentColor)
        assertEquals(250, restored.signupBonus)
        assertEquals(35, restored.adWatchReward)
        assertEquals(75, restored.referralBonus)
        assertEquals(1000, restored.minWithdrawal)
        assertEquals(2000, restored.coinRateCoins)
        assertEquals(20.0, restored.coinRateCurrency, 0.001)
        assertEquals("$", restored.currencySymbol)
        assertEquals(true, restored.enableWatchEarn)
        assertEquals(false, restored.enableSpinWheel)
        assertEquals(listOf("PayPal", "USDT"), restored.paymentMethods)
    }

    @Test
    fun testBotFlowJsonRoundTrip() {
        val nodes = listOf(
            BotNode(
                id = "n_start",
                title = "Start Command",
                type = BotNodeType.TRIGGER,
                content = "/start",
                buttons = listOf(
                    BotButton(text = "Launch", type = "webapp", payload = "open")
                ),
                posX = 40f,
                posY = 60f,
                nextNodeId = "n_msg"
            ),
            BotNode(
                id = "n_msg",
                title = "Welcome Message",
                type = BotNodeType.MESSAGE,
                content = "Welcome to the bot!",
                posX = 40f,
                posY = 240f
            )
        )
        val flow = BotFlow(
            name = "TestMinerBot",
            username = "test_miner_bot",
            description = "Click to mine tokens",
            nodes = nodes,
            webappHtml = "<html><body>Hello</body></html>"
        )

        val json = flow.toJson()
        val restored = BotFlow.fromJson(json)

        assertEquals("TestMinerBot", restored.name)
        assertEquals("test_miner_bot", restored.username)
        assertEquals(2, restored.nodes.size)
        assertEquals("n_start", restored.nodes[0].id)
        assertEquals(BotNodeType.TRIGGER, restored.nodes[0].type)
        assertEquals(1, restored.nodes[0].buttons.size)
        assertEquals("Launch", restored.nodes[0].buttons[0].text)
        assertEquals("n_msg", restored.nodes[0].nextNodeId)
        assertTrue(restored.webappHtml.contains("Hello"))
    }

    @Test
    fun testBotTemplatesIntegrity() {
        val templates = BotTemplates.ALL_TEMPLATES
        assertTrue("Templates list should not be empty", templates.isNotEmpty())
        assertEquals(10, templates.size)

        for (template in templates) {
            assertTrue("Template title should not be blank", template.title.isNotBlank())
            assertTrue("Template category should not be blank", template.category.isNotBlank())
            assertTrue("Template flow should have nodes", template.flow.nodes.isNotEmpty())
            assertTrue("Template should contain webapp HTML", template.flow.webappHtml.isNotBlank())
        }
    }

    @Test
    fun testBotIdeasIntegrity() {
        val ideas = BotIdeas.CURATED_IDEAS
        assertTrue("Curated ideas should not be empty", ideas.isNotEmpty())
        for (idea in ideas) {
            assertTrue("Idea title should not be blank", idea.title.isNotBlank())
            assertTrue("Idea features should not be empty", idea.keyFeatures.isNotEmpty())
            assertTrue("Idea should have commands", idea.starterCommands.isNotEmpty())
        }
        val randomIdea = BotIdeas.getRandomIdea()
        assertNotNull(randomIdea)
    }

    @Test
    fun testDiffHelperAdditionsAndDeletions() {
        val original = """
            line 1
            line 2
            line 3
        """.trimIndent()

        val modified = """
            line 1
            line 2 modified
            line 3
            line 4 added
        """.trimIndent()

        val diff = DiffHelper.computeDiff(original, modified)
        assertTrue(diff.lines.isNotEmpty())
        assertTrue(diff.additionsCount >= 1)
    }

    @Test
    fun testProjectModel() {
        val project = Project(
            id = 5L,
            name = "Test App",
            formDataJson = "{}"
        )
        assertEquals(5L, project.id)
        assertEquals("Test App", project.name)
        assertNotNull(project.createdAt)
    }

    @Test
    fun testTemplateSourcesPreserved() {
        val formData = FormData(
            appName = "EarnFast Pro",
            templateSource = "earnfast"
        )
        val json = formData.toJson()
        val parsed = FormData.fromJson(json)
        assertEquals("earnfast", parsed.templateSource)
        assertEquals("EarnFast Pro", parsed.appName)
    }

    @Test
    fun testNewSourceTemplatesInBotTemplates() {
        val templates = BotTemplates.ALL_TEMPLATES
        assertTrue("Should contain at least 10 templates", templates.size >= 10)

        val earnFast = templates.find { it.id == "tpl_earnfast" }
        assertNotNull("EarnFast template should exist", earnFast)
        assertTrue(earnFast!!.flow.nodes.isNotEmpty())
        assertTrue(earnFast.flow.webappHtml.contains("Image Finder"))

        val realCash = templates.find { it.id == "tpl_realcash" }
        assertNotNull("RealCash template should exist", realCash)
        assertTrue(realCash!!.flow.webappHtml.contains("Ludo"))

        val gameToZone = templates.find { it.id == "tpl_gametozone" }
        assertNotNull("GameToZone template should exist", gameToZone)
        assertTrue(gameToZone!!.flow.webappHtml.contains("GameToZone"))

        val cashReward = templates.find { it.id == "tpl_cashreward" }
        assertNotNull("CashReward template should exist", cashReward)
        assertTrue(cashReward!!.flow.webappHtml.contains("CashReward"))
    }
}
