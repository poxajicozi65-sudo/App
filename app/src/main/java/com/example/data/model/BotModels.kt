package com.example.data.model

import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

enum class BotNodeType(val label: String, val icon: String) {
    TRIGGER("Trigger / Command", "⚡"),
    MESSAGE("Bot Message", "💬"),
    INLINE_BUTTONS("Inline Keyboard", "🎛️"),
    REPLY_KEYBOARD("Reply Menu", "📋"),
    ACTION_LOGIC("Action & Logic", "⚙️"),
    WEBAPP_VIEW("Mini App Screen", "📱"),
    QUIZ_POLL("Quiz & Trivia", "🧠"),
    PAYMENT_STARS("Telegram Stars", "⭐")
}

data class BotButton(
    val id: String = UUID.randomUUID().toString(),
    val text: String,
    val type: String = "callback", // "callback", "webapp", "url"
    val payload: String = ""
) {
    fun toJson(): JSONObject = JSONObject().apply {
        put("id", id)
        put("text", text)
        put("type", type)
        put("payload", payload)
    }

    companion object {
        fun fromJson(json: JSONObject): BotButton = BotButton(
            id = json.optString("id", UUID.randomUUID().toString()),
            text = json.optString("text", "Button"),
            type = json.optString("type", "callback"),
            payload = json.optString("payload", "")
        )
    }
}

data class BotNode(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val type: BotNodeType,
    val content: String,
    val buttons: List<BotButton> = emptyList(),
    val posX: Float = 0f,
    val posY: Float = 0f,
    val nextNodeId: String? = null
) {
    fun toJson(): JSONObject = JSONObject().apply {
        put("id", id)
        put("title", title)
        put("type", type.name)
        put("content", content)
        put("posX", posX.toDouble())
        put("posY", posY.toDouble())
        if (nextNodeId != null) put("nextNodeId", nextNodeId)
        val btnArr = JSONArray()
        buttons.forEach { btnArr.put(it.toJson()) }
        put("buttons", btnArr)
    }

    companion object {
        fun fromJson(json: JSONObject): BotNode {
            val typeStr = json.optString("type", BotNodeType.MESSAGE.name)
            val type = try {
                BotNodeType.valueOf(typeStr)
            } catch (e: Exception) {
                BotNodeType.MESSAGE
            }
            val btnList = mutableListOf<BotButton>()
            val btnArr = json.optJSONArray("buttons")
            if (btnArr != null) {
                for (i in 0 until btnArr.length()) {
                    btnList.add(BotButton.fromJson(btnArr.getJSONObject(i)))
                }
            }
            return BotNode(
                id = json.optString("id", UUID.randomUUID().toString()),
                title = json.optString("title", "Node"),
                type = type,
                content = json.optString("content", ""),
                buttons = btnList,
                posX = json.optDouble("posX", 0.0).toFloat(),
                posY = json.optDouble("posY", 0.0).toFloat(),
                nextNodeId = if (json.has("nextNodeId")) json.optString("nextNodeId") else null
            )
        }
    }
}

data class BotFlow(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "My Telegram Bot",
    val username: String = "my_bot",
    val description: String = "Telegram bot built with MiniApp Forge",
    val nodes: List<BotNode> = emptyList(),
    val webappHtml: String = ""
) {
    fun toJson(): String {
        val root = JSONObject()
        root.put("id", id)
        root.put("name", name)
        root.put("username", username)
        root.put("description", description)
        root.put("webappHtml", webappHtml)
        val nodeArr = JSONArray()
        nodes.forEach { nodeArr.put(it.toJson()) }
        root.put("nodes", nodeArr)
        return root.toString(2)
    }

    companion object {
        fun fromJson(jsonStr: String): BotFlow {
            val json = JSONObject(jsonStr)
            val nodeList = mutableListOf<BotNode>()
            val arr = json.optJSONArray("nodes")
            if (arr != null) {
                for (i in 0 until arr.length()) {
                    nodeList.add(BotNode.fromJson(arr.getJSONObject(i)))
                }
            }
            return BotFlow(
                id = json.optString("id", UUID.randomUUID().toString()),
                name = json.optString("name", "My Telegram Bot"),
                username = json.optString("username", "my_bot"),
                description = json.optString("description", ""),
                nodes = nodeList,
                webappHtml = json.optString("webappHtml", "")
            )
        }
    }
}

data class BotIdea(
    val title: String,
    val category: String,
    val description: String,
    val keyFeatures: List<String>,
    val monetization: String,
    val starterCommands: List<String>
)
