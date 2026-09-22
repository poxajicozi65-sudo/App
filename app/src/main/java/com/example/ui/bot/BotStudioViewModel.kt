package com.example.ui.bot

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.GeminiService
import com.example.data.model.BotButton
import com.example.data.model.BotFlow
import com.example.data.model.BotIdea
import com.example.data.model.BotNode
import com.example.data.model.BotNodeType
import com.example.data.preferences.SecurePreferences
import com.example.data.templates.BotIdeas
import com.example.data.templates.BotTemplate
import com.example.data.templates.BotTemplates
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

enum class BotStudioTab(val label: String, val icon: String) {
    PROJECTS("Forge", "🛠️"),
    STORE("Asset Store", "🛍️"),
    AI_CHAT("AI Architect", "💬"),
    CANVAS("Canvas Flow", "🎨"),
    IDEAS_TEMPLATES("Ideas & Kits", "💡"),
    PREVIEW("Live Simulator", "📱")
}

data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val sender: String, // "user" | "bot"
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val generatedFlow: BotFlow? = null
)

data class SimMessage(
    val id: String = UUID.randomUUID().toString(),
    val isFromBot: Boolean,
    val text: String,
    val buttons: List<BotButton> = emptyList(),
    val timestamp: Long = System.currentTimeMillis()
)

enum class PreviewMode {
    TELEGRAM_CHAT,
    MINIAPP_WEB
}

class BotStudioViewModel(application: Application) : AndroidViewModel(application) {

    private val securePrefs = SecurePreferences(application)
    private val geminiService = GeminiService()

    // Active bottom navigation tab
    private val _currentTab = MutableStateFlow(BotStudioTab.PROJECTS)
    val currentTab: StateFlow<BotStudioTab> = _currentTab.asStateFlow()

    fun selectTab(tab: BotStudioTab) {
        _currentTab.value = tab
    }

    // --- Active Bot Flow (Canvas state) ---
    private val _botFlow = MutableStateFlow<BotFlow>(BotTemplates.ALL_TEMPLATES.first().flow)
    val botFlow: StateFlow<BotFlow> = _botFlow.asStateFlow()

    private val _selectedNode = MutableStateFlow<BotNode?>(null)
    val selectedNode: StateFlow<BotNode?> = _selectedNode.asStateFlow()

    fun selectNode(node: BotNode?) {
        _selectedNode.value = node
    }

    fun updateNodePosition(nodeId: String, deltaX: Float, deltaY: Float) {
        val current = _botFlow.value
        val updatedNodes = current.nodes.map { node ->
            if (node.id == nodeId) {
                node.copy(
                    posX = (node.posX + deltaX).coerceAtLeast(10f),
                    posY = (node.posY + deltaY).coerceAtLeast(10f)
                )
            } else node
        }
        _botFlow.value = current.copy(nodes = updatedNodes)
        if (_selectedNode.value?.id == nodeId) {
            _selectedNode.value = updatedNodes.firstOrNull { it.id == nodeId }
        }
    }

    fun updateNode(updatedNode: BotNode) {
        val current = _botFlow.value
        val updatedNodes = current.nodes.map { if (it.id == updatedNode.id) updatedNode else it }
        _botFlow.value = current.copy(nodes = updatedNodes)
        if (_selectedNode.value?.id == updatedNode.id) {
            _selectedNode.value = updatedNode
        }
    }

    fun addNode(type: BotNodeType, title: String, content: String, buttons: List<BotButton> = emptyList()) {
        val current = _botFlow.value
        val count = current.nodes.size
        val newNode = BotNode(
            id = "node_${System.currentTimeMillis()}",
            title = title,
            type = type,
            content = content,
            buttons = buttons,
            posX = 50f + (count % 3) * 60f,
            posY = 100f + (count * 70f)
        )
        _botFlow.value = current.copy(nodes = current.nodes + newNode)
        _selectedNode.value = newNode
    }

    fun deleteNode(nodeId: String) {
        val current = _botFlow.value
        val updatedNodes = current.nodes.filter { it.id != nodeId }.map { node ->
            if (node.nextNodeId == nodeId) node.copy(nextNodeId = null) else node
        }
        _botFlow.value = current.copy(nodes = updatedNodes)
        if (_selectedNode.value?.id == nodeId) {
            _selectedNode.value = null
        }
    }

    fun connectNodes(sourceNodeId: String, targetNodeId: String?) {
        val current = _botFlow.value
        val updatedNodes = current.nodes.map {
            if (it.id == sourceNodeId) it.copy(nextNodeId = targetNodeId) else it
        }
        _botFlow.value = current.copy(nodes = updatedNodes)
    }

    fun loadTemplate(template: BotTemplate) {
        _botFlow.value = template.flow
        _selectedNode.value = template.flow.nodes.firstOrNull()
        resetSimulator()
    }

    fun loadFlow(flow: BotFlow) {
        _botFlow.value = flow
        _selectedNode.value = flow.nodes.firstOrNull()
        resetSimulator()
    }

    fun updateWebappHtml(newHtml: String) {
        _botFlow.value = _botFlow.value.copy(webappHtml = newHtml)
    }

    // --- AI Chat Architect State ---
    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(
        listOf(
            ChatMessage(
                sender = "bot",
                text = "⚡ Welcome to MiniApp Forge AI Studio!\n\nI can help you build custom Telegram Bots & Mini Apps with Gemini. Ask me to:\n• Generate an e-commerce shop with Telegram Stars\n• Add a clicker game with combo multipliers\n• Design multi-level referral reward flows\n• Connect WebApp pages & write full code"
            )
        )
    )
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    private val _isChatGenerating = MutableStateFlow(false)
    val isChatGenerating: StateFlow<Boolean> = _isChatGenerating.asStateFlow()

    fun sendChatMessage(prompt: String) {
        if (prompt.isBlank() || _isChatGenerating.value) return

        val userMsg = ChatMessage(sender = "user", text = prompt)
        _chatMessages.value = _chatMessages.value + userMsg

        viewModelScope.launch {
            _isChatGenerating.value = true
            val apiKey = securePrefs.getGeminiApiKey()

            if (apiKey.isBlank()) {
                _chatMessages.value = _chatMessages.value + ChatMessage(
                    sender = "bot",
                    text = "⚠️ Gemini API key is missing. Please add your key in the top Settings (Key icon) or in AI Studio Secrets to unlock real-time Gemini chat generation!"
                )
                _isChatGenerating.value = false
                return@launch
            }

            // Build history pairs
            val history = _chatMessages.value.dropLast(1).map {
                (if (it.sender == "user") "user" else "model") to it.text
            }

            val flowSummary = "Bot: ${_botFlow.value.name} (@${_botFlow.value.username})\nNodes: ${_botFlow.value.nodes.size} nodes (${_botFlow.value.nodes.joinToString { it.title }})"
            val result = geminiService.chatWithBotArchitect(apiKey, history, prompt, flowSummary)

            result.onSuccess { responseText ->
                _chatMessages.value = _chatMessages.value + ChatMessage(
                    sender = "bot",
                    text = responseText
                )
            }.onFailure { err ->
                _chatMessages.value = _chatMessages.value + ChatMessage(
                    sender = "bot",
                    text = "❌ Error from Gemini: ${err.message ?: "Failed to generate response. Please check your API key."}"
                )
            }
            _isChatGenerating.value = false
        }
    }

    // --- Idea & Templates State ---
    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _currentIdea = MutableStateFlow<BotIdea>(BotIdeas.CURATED_IDEAS.first())
    val currentIdea: StateFlow<BotIdea> = _currentIdea.asStateFlow()

    private val _isIdeaGenerating = MutableStateFlow(false)
    val isIdeaGenerating: StateFlow<Boolean> = _isIdeaGenerating.asStateFlow()

    fun setIdeaCategory(category: String) {
        _selectedCategory.value = category
    }

    fun generateNewIdea(category: String = _selectedCategory.value, keywords: String = "") {
        viewModelScope.launch {
            val apiKey = securePrefs.getGeminiApiKey()
            if (apiKey.isNotBlank()) {
                _isIdeaGenerating.value = true
                val result = geminiService.generateBotIdea(apiKey, if (category == "All") "Trending Telegram Bots" else category, keywords)
                result.onSuccess {
                    _currentIdea.value = it
                }.onFailure {
                    // Fallback to rich curated idea
                    _currentIdea.value = BotIdeas.getRandomIdea(category)
                }
                _isIdeaGenerating.value = false
            } else {
                _currentIdea.value = BotIdeas.getRandomIdea(category)
            }
        }
    }

    fun buildIdeaInCanvas(idea: BotIdea) {
        viewModelScope.launch {
            val apiKey = securePrefs.getGeminiApiKey()
            if (apiKey.isNotBlank()) {
                _isIdeaGenerating.value = true
                val flowResult = geminiService.generateFlowFromPrompt(apiKey, "${idea.title}: ${idea.description}")
                flowResult.onSuccess { flow ->
                    loadFlow(flow)
                    selectTab(BotStudioTab.CANVAS)
                }.onFailure {
                    // Create default structure for idea
                    val defaultFlow = BotFlow(
                        name = idea.title,
                        username = idea.title.lowercase().replace(" ", "_") + "_bot",
                        description = idea.description,
                        nodes = listOf(
                            BotNode(id = "n_1", title = "Start Command", type = BotNodeType.TRIGGER, content = "/start", posX = 40f, posY = 60f, nextNodeId = "n_2"),
                            BotNode(id = "n_2", title = "Welcome & Info", type = BotNodeType.MESSAGE, content = "Welcome to ${idea.title}!\n\n${idea.description}", posX = 40f, posY = 240f, nextNodeId = "n_3"),
                            BotNode(id = "n_3", title = "Action Buttons", type = BotNodeType.INLINE_BUTTONS, content = "Main Menu", buttons = listOf(BotButton(text = "🚀 Open Mini App", type = "webapp", payload = "open")), posX = 40f, posY = 440f)
                        ),
                        webappHtml = BotTemplates.ALL_TEMPLATES.first().flow.webappHtml
                    )
                    loadFlow(defaultFlow)
                    selectTab(BotStudioTab.CANVAS)
                }
                _isIdeaGenerating.value = false
            } else {
                val defaultFlow = BotFlow(
                    name = idea.title,
                    username = idea.title.lowercase().replace(" ", "_") + "_bot",
                    description = idea.description,
                    nodes = listOf(
                        BotNode(id = "n_1", title = "Start Command", type = BotNodeType.TRIGGER, content = "/start", posX = 40f, posY = 60f, nextNodeId = "n_2"),
                        BotNode(id = "n_2", title = "Welcome & Info", type = BotNodeType.MESSAGE, content = "Welcome to ${idea.title}!\n\n${idea.description}", posX = 40f, posY = 240f, nextNodeId = "n_3"),
                        BotNode(id = "n_3", title = "Action Buttons", type = BotNodeType.INLINE_BUTTONS, content = "Main Menu", buttons = listOf(BotButton(text = "🚀 Open Mini App", type = "webapp", payload = "open")), posX = 40f, posY = 440f)
                    ),
                    webappHtml = BotTemplates.ALL_TEMPLATES.first().flow.webappHtml
                )
                loadFlow(defaultFlow)
                selectTab(BotStudioTab.CANVAS)
            }
        }
    }

    // --- Live Simulator State ---
    private val _simMessages = MutableStateFlow<List<SimMessage>>(emptyList())
    val simMessages: StateFlow<List<SimMessage>> = _simMessages.asStateFlow()

    private val _isSimMiniAppOpen = MutableStateFlow(false)
    val isSimMiniAppOpen: StateFlow<Boolean> = _isSimMiniAppOpen.asStateFlow()

    private val _previewMode = MutableStateFlow(PreviewMode.TELEGRAM_CHAT)
    val previewMode: StateFlow<PreviewMode> = _previewMode.asStateFlow()

    fun setPreviewMode(mode: PreviewMode) {
        _previewMode.value = mode
    }

    fun openSimMiniApp() {
        _isSimMiniAppOpen.value = true
    }

    fun closeSimMiniApp() {
        _isSimMiniAppOpen.value = false
    }

    init {
        resetSimulator()
    }

    fun resetSimulator() {
        val flow = _botFlow.value
        val startNode = flow.nodes.firstOrNull { it.type == BotNodeType.TRIGGER && it.content.contains("/start") }
            ?: flow.nodes.firstOrNull { it.type == BotNodeType.TRIGGER }
            ?: flow.nodes.firstOrNull()

        val welcomeMsg = flow.nodes.firstOrNull { it.type == BotNodeType.MESSAGE }
        val inlineBtns = flow.nodes.firstOrNull { it.type == BotNodeType.INLINE_BUTTONS }?.buttons ?: emptyList()

        _simMessages.value = listOf(
            SimMessage(
                isFromBot = true,
                text = welcomeMsg?.content ?: "👋 Hello! I am ${flow.name} (@${flow.username}). Tap /start to begin or click buttons below:",
                buttons = inlineBtns
            )
        )
        _isSimMiniAppOpen.value = false
    }

    fun sendSimUserMessage(userText: String) {
        if (userText.isBlank()) return
        val currentFlow = _botFlow.value

        val userSimMsg = SimMessage(isFromBot = false, text = userText)
        _simMessages.value = _simMessages.value + userSimMsg

        // Check if triggers matched in canvas nodes
        val matchedTrigger = currentFlow.nodes.firstOrNull {
            it.type == BotNodeType.TRIGGER && (it.content.equals(userText.trim(), ignoreCase = true) || userText.contains(it.content, ignoreCase = true))
        }

        if (matchedTrigger != null) {
            val nextNode = currentFlow.nodes.firstOrNull { it.id == matchedTrigger.nextNodeId }
            if (nextNode != null && nextNode.type == BotNodeType.MESSAGE) {
                val attachedBtns = currentFlow.nodes.firstOrNull { it.id == nextNode.nextNodeId && it.type == BotNodeType.INLINE_BUTTONS }?.buttons ?: nextNode.buttons
                _simMessages.value = _simMessages.value + SimMessage(
                    isFromBot = true,
                    text = nextNode.content,
                    buttons = attachedBtns
                )
                return
            }
        }

        // Generic handling for standard commands
        when (userText.trim().lowercase()) {
            "/start" -> {
                val welcome = currentFlow.nodes.firstOrNull { it.type == BotNodeType.MESSAGE }
                val buttons = currentFlow.nodes.firstOrNull { it.type == BotNodeType.INLINE_BUTTONS }?.buttons ?: emptyList()
                _simMessages.value = _simMessages.value + SimMessage(
                    isFromBot = true,
                    text = welcome?.content ?: "👋 Welcome to ${currentFlow.name}!\n\n${currentFlow.description}",
                    buttons = buttons
                )
            }
            "/help" -> {
                _simMessages.value = _simMessages.value + SimMessage(
                    isFromBot = true,
                    text = "ℹ️ Available Commands:\n/start - Start or restart the bot\n/help - Show this guide\n/miniapp - Launch the Mini App\n/balance - Check in-game balance"
                )
            }
            "/miniapp" -> {
                _simMessages.value = _simMessages.value + SimMessage(
                    isFromBot = true,
                    text = "📱 Launching ${currentFlow.name} Mini App...",
                    buttons = listOf(BotButton(text = "🚀 Open Mini App", type = "webapp", payload = "open"))
                )
                openSimMiniApp()
            }
            else -> {
                _simMessages.value = _simMessages.value + SimMessage(
                    isFromBot = true,
                    text = "🤖 Received: \"$userText\"\nUse /start or tap one of the interactive buttons to navigate."
                )
            }
        }
    }

    fun clickSimButton(button: BotButton) {
        if (button.type == "webapp") {
            openSimMiniApp()
        } else {
            _simMessages.value = _simMessages.value + SimMessage(
                isFromBot = false,
                text = "👉 [Tapped]: ${button.text}"
            )
            // Simulated action response
            _simMessages.value = _simMessages.value + SimMessage(
                isFromBot = true,
                text = "⚡ Executed action: \"${button.payload}\"\nStatus: Success ✅"
            )
        }
    }
}
