package com.example.ui.bot.preview

import android.annotation.SuppressLint
import android.view.View
import android.webkit.ConsoleMessage
import android.webkit.RenderProcessGoneDetail
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Launch
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.BotButton
import com.example.ui.bot.BotStudioViewModel
import com.example.ui.bot.PreviewMode
import com.example.ui.bot.SimMessage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BotPreviewView(
    viewModel: BotStudioViewModel,
    modifier: Modifier = Modifier
) {
    val currentFlow by viewModel.botFlow.collectAsStateWithLifecycle()
    val previewMode by viewModel.previewMode.collectAsStateWithLifecycle()
    val isMiniAppOpen by viewModel.isSimMiniAppOpen.collectAsStateWithLifecycle()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Mode Switcher Tab
        TabRow(
            selectedTabIndex = if (previewMode == PreviewMode.TELEGRAM_CHAT) 0 else 1,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary
        ) {
            Tab(
                selected = previewMode == PreviewMode.TELEGRAM_CHAT,
                onClick = { viewModel.setPreviewMode(PreviewMode.TELEGRAM_CHAT) },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Telegram Chat", fontWeight = FontWeight.Bold)
                    }
                }
            )
            Tab(
                selected = previewMode == PreviewMode.MINIAPP_WEB,
                onClick = { viewModel.setPreviewMode(PreviewMode.MINIAPP_WEB) },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.PhoneAndroid, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Mini App Web", fontWeight = FontWeight.Bold)
                    }
                }
            )
        }

        Box(modifier = Modifier.fillMaxSize()) {
            if (previewMode == PreviewMode.TELEGRAM_CHAT) {
                TelegramChatSimulator(viewModel = viewModel)
            } else {
                TelegramMiniAppWebPreview(
                    htmlContent = currentFlow.webappHtml,
                    botName = currentFlow.name,
                    onClose = { viewModel.setPreviewMode(PreviewMode.TELEGRAM_CHAT) }
                )
            }

            // Slide-up MiniApp Modal when launched from inside chat simulator
            this@Column.AnimatedVisibility(
                visible = isMiniAppOpen && previewMode == PreviewMode.TELEGRAM_CHAT,
                enter = slideInVertically(initialOffsetY = { it }),
                exit = slideOutVertically(targetOffsetY = { it })
            ) {
                TelegramMiniAppWebPreview(
                    htmlContent = currentFlow.webappHtml,
                    botName = currentFlow.name,
                    onClose = { viewModel.closeSimMiniApp() }
                )
            }
        }
    }
}

@Composable
fun TelegramChatSimulator(
    viewModel: BotStudioViewModel
) {
    val flow by viewModel.botFlow.collectAsStateWithLifecycle()
    val messages by viewModel.simMessages.collectAsStateWithLifecycle()
    var inputCommand by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    // Telegram authentic theme colors
    val tgHeaderBg = Color(0xFF242F3D)
    val tgChatBg = Color(0xFF0E1621)
    val tgBotBubble = Color(0xFF182533)
    val tgUserBubble = Color(0xFF2B5278)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(tgChatBg)
            .imePadding()
    ) {
        // Telegram Header Bar
        Surface(
            color = tgHeaderBg,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFF2AABEE),
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(flow.name.take(1).uppercase(), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = flow.name,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("✓", color = Color(0xFF2AABEE), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                        Text(
                            text = "bot • @${flow.username}",
                            color = Color(0xFF8E9BA8),
                            fontSize = 12.sp
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { viewModel.resetSimulator() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Restart Bot", tint = Color(0xFF8E9BA8))
                    }
                }
            }
        }

        // Chat Messages
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(messages, key = { it.id }) { msg ->
                if (msg.isFromBot) {
                    BotChatBubble(
                        message = msg,
                        bubbleColor = tgBotBubble,
                        onButtonClick = { btn ->
                            viewModel.clickSimButton(btn)
                        }
                    )
                } else {
                    UserChatBubble(
                        text = msg.text,
                        bubbleColor = tgUserBubble
                    )
                }
            }
        }

        // Quick Command Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(tgHeaderBg)
                .padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("/start", "/help", "/miniapp").forEach { cmd ->
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFF17212B),
                    modifier = Modifier.clickable {
                        viewModel.sendSimUserMessage(cmd)
                    }
                ) {
                    Text(
                        text = cmd,
                        color = Color(0xFF6AB2F2),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }
            }
        }

        // Input Field
        Surface(
            color = tgHeaderBg,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = inputCommand,
                    onValueChange = { inputCommand = it },
                    placeholder = { Text("Write a message or /command...", color = Color(0xFF8E9BA8), fontSize = 14.sp) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("sim_chat_input"),
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFF17212B),
                        unfocusedContainerColor = Color(0xFF17212B),
                        focusedBorderColor = Color(0xFF2AABEE),
                        unfocusedBorderColor = Color(0xFF232E3C),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = {
                        if (inputCommand.isNotBlank()) {
                            val text = inputCommand.trim()
                            inputCommand = ""
                            viewModel.sendSimUserMessage(text)
                        }
                    },
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color(0xFF2AABEE))
                        .size(42.dp)
                        .testTag("sim_chat_send_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun BotChatBubble(
    message: SimMessage,
    bubbleColor: Color,
    onButtonClick: (BotButton) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.Start
    ) {
        Surface(
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomEnd = 16.dp, bottomStart = 4.dp),
            color = bubbleColor,
            modifier = Modifier.widthIn(max = 300.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = message.text,
                    color = Color.White,
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )
            }
        }

        // Inline Keyboard Buttons
        if (message.buttons.isNotEmpty()) {
            Spacer(modifier = Modifier.height(4.dp))
            Column(
                modifier = Modifier.widthIn(max = 300.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                message.buttons.forEach { btn ->
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFF243343),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onButtonClick(btn) }
                            .testTag("sim_btn_${btn.payload}")
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 10.dp, horizontal = 12.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (btn.type == "webapp") {
                                Text("🚀 ", fontSize = 14.sp)
                            }
                            Text(
                                text = btn.text,
                                color = Color(0xFF6AB2F2),
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun UserChatBubble(
    text: String,
    bubbleColor: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End
    ) {
        Surface(
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 4.dp),
            color = bubbleColor,
            modifier = Modifier.widthIn(max = 280.dp)
        ) {
            Text(
                text = text,
                color = Color.White,
                fontSize = 14.sp,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
            )
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun TelegramMiniAppWebPreview(
    htmlContent: String,
    botName: String,
    onClose: () -> Unit
) {
    var webViewRef by remember { mutableStateOf<WebView?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
    ) {
        // Simulated Telegram WebApp Top Header
        Surface(
            color = Color(0xFF1E293B),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onClose) {
                        Icon(Icons.Default.Close, contentDescription = "Close Mini App", tint = Color.White)
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Column {
                        Text(
                            text = botName,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Text(
                            text = "Telegram Mini App WebSDK 7.0",
                            color = Color(0xFF94A3B8),
                            fontSize = 11.sp
                        )
                    }
                }

                IconButton(onClick = { webViewRef?.reload() }) {
                    Icon(Icons.Default.Refresh, contentDescription = "Reload", tint = Color.White)
                }
            }
        }

        // Web View Rendering
        AndroidView(
            modifier = Modifier
                .fillMaxSize()
                .testTag("miniapp_webview"),
            factory = { context ->
                WebView(context).apply {
                    setLayerType(View.LAYER_TYPE_SOFTWARE, null)
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    settings.loadWithOverviewMode = true
                    settings.useWideViewPort = true
                    settings.cacheMode = WebSettings.LOAD_NO_CACHE

                    webChromeClient = object : WebChromeClient() {
                        override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
                            return true
                        }
                    }
                    webViewClient = object : WebViewClient() {
                        override fun onRenderProcessGone(view: WebView?, detail: RenderProcessGoneDetail?): Boolean {
                            view?.destroy()
                            return true
                        }
                    }

                    val injectedSdkHtml = injectTelegramSdk(htmlContent)
                    loadDataWithBaseURL("https://telegram.org", injectedSdkHtml, "text/html", "UTF-8", null)
                    webViewRef = this
                }
            },
            update = { view ->
                val injectedSdkHtml = injectTelegramSdk(htmlContent)
                view.loadDataWithBaseURL("https://telegram.org", injectedSdkHtml, "text/html", "UTF-8", null)
            }
        )
    }
}

/**
 * Injects a mock Telegram.WebApp JavaScript SDK so that window.Telegram.WebApp methods
 * work seamlessly inside Android WebView without breaking!
 */
private fun injectTelegramSdk(originalHtml: String): String {
    val mockScript = """
<script>
  window.Telegram = window.Telegram || {};
  window.Telegram.WebApp = {
    initData: "user=%7B%22id%22%3A1234567%2C%22first_name%22%3A%22Alex%22%2C%22username%22%3A%22alex_tg%22%7D",
    initDataUnsafe: { user: { id: 1234567, first_name: "Alex", username: "alex_tg" } },
    version: "7.0",
    platform: "android",
    colorScheme: "dark",
    themeParams: { bg_color: "#0f172a", text_color: "#ffffff", button_color: "#3b82f6", button_text_color: "#ffffff" },
    isExpanded: true,
    viewportHeight: 700,
    viewportStableHeight: 700,
    headerColor: "#1e293b",
    backgroundColor: "#0f172a",
    MainButton: {
      text: "CONTINUE",
      color: "#3b82f6",
      textColor: "#ffffff",
      isVisible: false,
      isActive: true,
      setText: function(t) { this.text = t; },
      show: function() { this.isVisible = true; },
      hide: function() { this.isVisible = false; },
      onClick: function(fn) { this.callback = fn; }
    },
    BackButton: {
      isVisible: false,
      show: function() { this.isVisible = true; },
      hide: function() { this.isVisible = false; }
    },
    HapticFeedback: {
      impactOccurred: function(style) { console.log('Haptic impact: ' + style); },
      notificationOccurred: function(type) { console.log('Haptic notif: ' + type); },
      selectionChanged: function() { console.log('Haptic selection'); }
    },
    ready: function() {},
    expand: function() {},
    close: function() {}
  };
</script>
    """.trimIndent()

    return if (originalHtml.contains("<head>")) {
        originalHtml.replace("<head>", "<head>$mockScript")
    } else {
        "$mockScript\n$originalHtml"
    }
}
