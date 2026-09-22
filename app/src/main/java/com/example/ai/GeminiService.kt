package com.example.ai

import com.example.data.model.BotButton
import com.example.data.model.BotFlow
import com.example.data.model.BotNode
import com.example.data.model.BotNodeType
import com.example.data.model.BotIdea
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID
import java.util.concurrent.TimeUnit

class GeminiService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    suspend fun refineCode(
        apiKey: String,
        originalFileContent: String,
        instruction: String,
        useThinking: Boolean = false,
        fileName: String = "index.html"
    ): Result<String> = withContext(Dispatchers.IO) {
        if (apiKey.isBlank()) {
            return@withContext Result.failure(IllegalArgumentException("Gemini API key is missing. Please enter your API key in Settings."))
        }

        val model = if (useThinking) "gemini-3.1-pro-preview" else "gemini-3.5-flash"
        val endpoint = "https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent?key=$apiKey"

        val prompt = """
You are editing a self-contained HTML/JS Telegram Mini App file ($fileName).
Only output the complete, modified file content — no explanation, no markdown fences.
Current file:
---
$originalFileContent
---
Requested change: $instruction
        """.trimIndent()

        val rootJson = JSONObject()
        val contentsArray = JSONArray()
        val contentObj = JSONObject()
        val partsArray = JSONArray()
        val partObj = JSONObject()
        partObj.put("text", prompt)
        partsArray.put(partObj)
        contentObj.put("parts", partsArray)
        contentsArray.put(contentObj)
        rootJson.put("contents", contentsArray)

        val genConfig = JSONObject()
        genConfig.put("temperature", 0.2)
        if (useThinking) {
            val thinkingConfig = JSONObject()
            thinkingConfig.put("thinkingLevel", "HIGH")
            genConfig.put("thinkingConfig", thinkingConfig)
        }
        rootJson.put("generationConfig", genConfig)

        val mediaType = "application/json; charset=utf-8".toMediaType()
        val requestBody = rootJson.toString().toRequestBody(mediaType)

        val request = Request.Builder()
            .url(endpoint)
            .post(requestBody)
            .build()

        try {
            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""
            if (!response.isSuccessful) {
                val errorMsg = try {
                    val errJson = JSONObject(responseBody)
                    errJson.optJSONObject("error")?.optString("message") ?: "API Error (${response.code})"
                } catch (e: Exception) {
                    "HTTP ${response.code}: $responseBody"
                }
                return@withContext Result.failure(Exception(errorMsg))
            }

            val resJson = JSONObject(responseBody)
            val candidates = resJson.optJSONArray("candidates")
            val firstCandidate = candidates?.optJSONObject(0)
            val content = firstCandidate?.optJSONObject("content")
            val parts = content?.optJSONArray("parts")
            var text = parts?.optJSONObject(0)?.optString("text") ?: ""

            if (text.isBlank()) {
                return@withContext Result.failure(Exception("Received empty response from Gemini model."))
            }

            text = text.trim()
            if (text.startsWith("```html")) {
                text = text.removePrefix("```html").trimStart()
            } else if (text.startsWith("```")) {
                text = text.removePrefix("```").trimStart()
            }
            if (text.endsWith("```")) {
                text = text.removeSuffix("```").trimEnd()
            }

            Result.success(text)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Chat with Gemini about Telegram Mini Bots and Mini Apps.
     * Takes chat history and responds conversationally, including code snippets or structured suggestions.
     */
    suspend fun chatWithBotArchitect(
        apiKey: String,
        history: List<Pair<String, String>>, // role ("user" | "model") to text
        userMessage: String,
        currentBotContext: String? = null
    ): Result<String> = withContext(Dispatchers.IO) {
        if (apiKey.isBlank()) {
            return@withContext Result.failure(IllegalArgumentException("Gemini API key is missing. Please enter your API key in Settings."))
        }

        val endpoint = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"

        val rootJson = JSONObject()

        // System instruction
        val systemInstruction = JSONObject()
        val sysParts = JSONArray()
        val sysPart = JSONObject()
        sysPart.put("text", """
You are MiniApp Forge's expert Telegram Bot & Mini App Architect.
You help creators design, code, and launch Telegram bots and HTML/JS Mini Apps.
You can:
1. Explain bot workflows, inline keyboards, callback queries, and Telegram WebApp integration.
2. Generate ready-to-run bot code (Python python-telegram-bot, Node.js Telegraf, or HTML5 WebApps).
3. If the user asks for a bot flow or new feature, explain it clearly and provide concise, executable code or flow blocks.
Keep your answers enthusiastic, concise, and practically focused on Telegram platform features like Telegram Stars, WebApp buttons, and interactive keyboards.
        """.trimIndent())
        sysParts.put(sysPart)
        systemInstruction.put("parts", sysParts)
        rootJson.put("systemInstruction", systemInstruction)

        val contentsArray = JSONArray()

        // Add history
        for (item in history) {
            val role = if (item.first == "user") "user" else "model"
            val turn = JSONObject()
            turn.put("role", role)
            val parts = JSONArray()
            val part = JSONObject()
            part.put("text", item.second)
            parts.put(part)
            turn.put("parts", parts)
            contentsArray.put(turn)
        }

        // Current user message
        val currentTurn = JSONObject()
        currentTurn.put("role", "user")
        val curParts = JSONArray()
        val curPart = JSONObject()
        var fullUserPrompt = userMessage
        if (!currentBotContext.isNullOrBlank()) {
            fullUserPrompt += "\n\n[Active Bot Context]:\n$currentBotContext"
        }
        curPart.put("text", fullUserPrompt)
        curParts.put(curPart)
        currentTurn.put("parts", curParts)
        contentsArray.put(currentTurn)

        rootJson.put("contents", contentsArray)

        val genConfig = JSONObject()
        genConfig.put("temperature", 0.7)
        rootJson.put("generationConfig", genConfig)

        val mediaType = "application/json; charset=utf-8".toMediaType()
        val requestBody = rootJson.toString().toRequestBody(mediaType)

        val request = Request.Builder()
            .url(endpoint)
            .post(requestBody)
            .build()

        try {
            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""
            if (!response.isSuccessful) {
                val errorMsg = try {
                    val errJson = JSONObject(responseBody)
                    errJson.optJSONObject("error")?.optString("message") ?: "API Error (${response.code})"
                } catch (e: Exception) {
                    "HTTP ${response.code}: $responseBody"
                }
                return@withContext Result.failure(Exception(errorMsg))
            }

            val resJson = JSONObject(responseBody)
            val candidates = resJson.optJSONArray("candidates")
            val firstCandidate = candidates?.optJSONObject(0)
            val content = firstCandidate?.optJSONObject("content")
            val parts = content?.optJSONArray("parts")
            val text = parts?.optJSONObject(0)?.optString("text") ?: ""

            if (text.isBlank()) {
                return@withContext Result.failure(Exception("Received empty response from Gemini."))
            }

            Result.success(text)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Generate a new Telegram Bot Idea with Gemini.
     */
    suspend fun generateBotIdea(
        apiKey: String,
        category: String,
        keywords: String = ""
    ): Result<BotIdea> = withContext(Dispatchers.IO) {
        if (apiKey.isBlank()) {
            return@withContext Result.failure(IllegalArgumentException("Gemini API key is missing."))
        }

        val endpoint = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"

        val prompt = """
You are a viral product strategist for Telegram Bots and Mini Apps.
Generate an innovative, highly viral Telegram Bot idea in the category: "$category".
${if (keywords.isNotBlank()) "Include these focus keywords: $keywords" else ""}

Output ONLY a valid JSON object matching this schema:
{
  "title": "Short Catchy Name",
  "category": "$category",
  "description": "2-3 sentence overview of what the bot does and why users love it",
  "keyFeatures": ["Feature 1", "Feature 2", "Feature 3", "Feature 4"],
  "monetization": "Explanation of how it makes money (e.g. Telegram Stars, subscriptions, ads, affiliate)",
  "starterCommands": ["/start", "/command1", "/command2", "/command3"]
}
No markdown fences, no explanatory text outside the JSON.
        """.trimIndent()

        val rootJson = JSONObject()
        val contentsArray = JSONArray()
        val contentObj = JSONObject()
        val partsArray = JSONArray()
        val partObj = JSONObject()
        partObj.put("text", prompt)
        partsArray.put(partObj)
        contentObj.put("parts", partsArray)
        contentsArray.put(contentObj)
        rootJson.put("contents", contentsArray)

        val genConfig = JSONObject()
        genConfig.put("temperature", 0.8)
        genConfig.put("responseMimeType", "application/json")
        rootJson.put("generationConfig", genConfig)

        val mediaType = "application/json; charset=utf-8".toMediaType()
        val requestBody = rootJson.toString().toRequestBody(mediaType)

        val request = Request.Builder()
            .url(endpoint)
            .post(requestBody)
            .build()

        try {
            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""
            if (!response.isSuccessful) {
                return@withContext Result.failure(Exception("API Error (${response.code}): $responseBody"))
            }

            val resJson = JSONObject(responseBody)
            val candidates = resJson.optJSONArray("candidates")
            val firstCandidate = candidates?.optJSONObject(0)
            val content = firstCandidate?.optJSONObject("content")
            val parts = content?.optJSONArray("parts")
            var text = parts?.optJSONObject(0)?.optString("text") ?: ""

            text = text.trim()
            if (text.startsWith("```json")) text = text.removePrefix("```json").trimStart()
            if (text.startsWith("```")) text = text.removePrefix("```").trimStart()
            if (text.endsWith("```")) text = text.removeSuffix("```").trimEnd()

            val ideaObj = JSONObject(text)
            val featuresList = mutableListOf<String>()
            val featArr = ideaObj.optJSONArray("keyFeatures")
            if (featArr != null) {
                for (i in 0 until featArr.length()) featuresList.add(featArr.getString(i))
            }
            val commandsList = mutableListOf<String>()
            val cmdArr = ideaObj.optJSONArray("starterCommands")
            if (cmdArr != null) {
                for (i in 0 until cmdArr.length()) commandsList.add(cmdArr.getString(i))
            }

            val idea = BotIdea(
                title = ideaObj.optString("title", "Telegram Bot"),
                category = ideaObj.optString("category", category),
                description = ideaObj.optString("description", ""),
                keyFeatures = if (featuresList.isNotEmpty()) featuresList else listOf("Interactive Telegram Mini App", "Telegram Stars Checkout", "Daily Streaks"),
                monetization = ideaObj.optString("monetization", "Telegram Stars & In-App Upgrades"),
                starterCommands = if (commandsList.isNotEmpty()) commandsList else listOf("/start", "/menu", "/help")
            )
            Result.success(idea)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Generate a visual BotFlow (nodes, buttons, and HTML) from a natural language prompt.
     */
    suspend fun generateFlowFromPrompt(
        apiKey: String,
        prompt: String
    ): Result<BotFlow> = withContext(Dispatchers.IO) {
        if (apiKey.isBlank()) {
            return@withContext Result.failure(IllegalArgumentException("Gemini API key is missing."))
        }

        val endpoint = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"

        val sysPrompt = """
You are a Telegram Bot visual canvas generator. Given a user concept, design a complete interactive Bot Flow.
Output a JSON object with:
- "name": Bot name
- "username": Bot username (lowercase with _bot)
- "description": Short description
- "webappHtml": A beautiful, complete, single-file HTML/CSS/JS page for the Telegram Mini App (dark theme, responsive, no external assets needed).
- "nodes": Array of 3 to 6 connected nodes:
  - "id": string unique id like "node_1"
  - "title": string
  - "type": one of "TRIGGER", "MESSAGE", "INLINE_BUTTONS", "REPLY_KEYBOARD", "ACTION_LOGIC", "WEBAPP_VIEW", "QUIZ_POLL", "PAYMENT_STARS"
  - "content": text content or command
  - "buttons": array of {"text": string, "type": "callback"|"webapp"|"url", "payload": string}
  - "posX": float (e.g. 40, 40, 40, 320, 320)
  - "posY": float (e.g. 60, 240, 440, 160, 360)
  - "nextNodeId": string or null
Return ONLY valid JSON matching this structure.
        """.trimIndent()

        val rootJson = JSONObject()
        val contentsArray = JSONArray()
        val contentObj = JSONObject()
        val partsArray = JSONArray()
        val partObj = JSONObject()
        partObj.put("text", "$sysPrompt\n\nUser Concept: $prompt")
        partsArray.put(partObj)
        contentObj.put("parts", partsArray)
        contentsArray.put(contentObj)
        rootJson.put("contents", contentsArray)

        val genConfig = JSONObject()
        genConfig.put("temperature", 0.4)
        genConfig.put("responseMimeType", "application/json")
        rootJson.put("generationConfig", genConfig)

        val mediaType = "application/json; charset=utf-8".toMediaType()
        val requestBody = rootJson.toString().toRequestBody(mediaType)

        val request = Request.Builder()
            .url(endpoint)
            .post(requestBody)
            .build()

        try {
            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""
            if (!response.isSuccessful) {
                return@withContext Result.failure(Exception("API Error (${response.code})"))
            }

            val resJson = JSONObject(responseBody)
            val candidates = resJson.optJSONArray("candidates")
            val firstCandidate = candidates?.optJSONObject(0)
            val content = firstCandidate?.optJSONObject("content")
            val parts = content?.optJSONArray("parts")
            var text = parts?.optJSONObject(0)?.optString("text") ?: ""

            text = text.trim()
            if (text.startsWith("```json")) text = text.removePrefix("```json").trimStart()
            if (text.startsWith("```")) text = text.removePrefix("```").trimStart()
            if (text.endsWith("```")) text = text.removeSuffix("```").trimEnd()

            val flow = BotFlow.fromJson(text)
            Result.success(flow)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
