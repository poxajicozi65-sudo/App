package com.example.ui.builder

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.ui.store.AssetStoreScreen
import com.example.ui.store.StoreMainTab
import com.example.util.DriveLinkConverter
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.FormData

private val STEP_TITLES = listOf(
    "Branding",
    "Firebase",
    "Economy",
    "Features",
    "Ad Network",
    "Review"
)

private val PRESET_COLORS = listOf(
    "#0284C7", // Cyan / Sky
    "#10B981", // Emerald
    "#8B5CF6", // Purple
    "#F59E0B", // Amber
    "#EC4899", // Pink
    "#06B6D4", // Teal
    "#EF4444"  // Rose
)

private val FONT_OPTIONS = listOf(
    "Inter",
    "Roboto",
    "Poppins",
    "Outfit",
    "Space Grotesk",
    "Montserrat"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BuilderScreen(
    viewModel: BuilderViewModel,
    onNavigateBack: () -> Unit,
    onGenerated: (Long) -> Unit
) {
    val currentStep by viewModel.currentStep.collectAsStateWithLifecycle()
    val formData by viewModel.formData.collectAsStateWithLifecycle()
    val isGenerating by viewModel.isGenerating.collectAsStateWithLifecycle()
    val generationSuccess by viewModel.generationSuccess.collectAsStateWithLifecycle()
    val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(generationSuccess) {
        generationSuccess?.let { projectId ->
            viewModel.clearGenerationSuccess()
            onGenerated(projectId)
        }
    }

    LaunchedEffect(errorMessage) {
        errorMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearError()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = formData.appName.ifBlank { "New Mini App" },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                        Text(
                            text = "Step ${currentStep + 1} of 6: ${STEP_TITLES[currentStep]}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp,
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (currentStep > 0) {
                        OutlinedButton(
                            onClick = { viewModel.prevStep() },
                            modifier = Modifier.testTag("btn_prev_step")
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Previous")
                        }
                    } else {
                        Spacer(modifier = Modifier.width(1.dp))
                    }

                    if (currentStep < 5) {
                        Button(
                            onClick = { viewModel.nextStep() },
                            modifier = Modifier.testTag("btn_next_step")
                        ) {
                            Text("Next")
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                        }
                    } else {
                        Button(
                            onClick = { viewModel.generateProject() },
                            enabled = !isGenerating,
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                            modifier = Modifier.testTag("btn_forge_app")
                        ) {
                            if (isGenerating) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    color = MaterialTheme.colorScheme.onSecondary,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Forging...")
                            } else {
                                Icon(Icons.Default.RocketLaunch, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Forge Mini App")
                            }
                        }
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Step Indicators Row
            StepIndicatorBar(
                currentStep = currentStep,
                onStepClicked = { viewModel.setStep(it) }
            )

            // Step Content
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            ) {
                when (currentStep) {
                    0 -> StepBranding(formData = formData, onUpdate = viewModel::updateFormData, onLogoUri = viewModel::handleLogoUri)
                    1 -> StepFirebase(formData = formData, onUpdate = viewModel::updateFormData, onPasteConfig = viewModel::parsePastedFirebaseConfig)
                    2 -> StepEconomy(formData = formData, onUpdate = viewModel::updateFormData)
                    3 -> StepFeatures(formData = formData, onUpdate = viewModel::updateFormData)
                    4 -> StepAdNetwork(formData = formData, onUpdate = viewModel::updateFormData)
                    5 -> StepReview(formData = formData, onUpdate = viewModel::updateFormData)
                }
            }
        }
    }
}

@Composable
fun StepIndicatorBar(
    currentStep: Int,
    onStepClicked: (Int) -> Unit
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        itemsIndexed(STEP_TITLES) { index, title ->
            val isSelected = currentStep == index
            val isCompleted = currentStep > index
            val bgColor = when {
                isSelected -> MaterialTheme.colorScheme.primary
                isCompleted -> MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                else -> MaterialTheme.colorScheme.surfaceVariant
            }
            val textColor = when {
                isSelected -> MaterialTheme.colorScheme.onPrimary
                isCompleted -> MaterialTheme.colorScheme.primary
                else -> MaterialTheme.colorScheme.onSurfaceVariant
            }

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = bgColor,
                modifier = Modifier.clickable { onStepClicked(index) }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${index + 1}",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = title,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = textColor
                    )
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------------------
// STEP 1: BRANDING
// -----------------------------------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StepBranding(
    formData: FormData,
    onUpdate: ((FormData) -> FormData) -> Unit,
    onLogoUri: (android.net.Uri) -> Unit
) {
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let { onLogoUri(it) }
    }

    var showAssetStoreDialog by remember { mutableStateOf(false) }
    var rawLogoInput by remember(formData.logoData) {
        mutableStateOf(if (formData.logoData.startsWith("http")) formData.logoData else "")
    }

    if (showAssetStoreDialog) {
        Dialog(
            onDismissRequest = { showAssetStoreDialog = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
            ) {
                AssetStoreScreen(
                    onSelectAsset = { selectedUrl ->
                        onUpdate { it.copy(logoData = selectedUrl) }
                        showAssetStoreDialog = false
                    },
                    onClose = { showAssetStoreDialog = false },
                    initialTab = StoreMainTab.ICONS
                )
            }
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Choose Template & Source Engine",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Select a battle-tested template architecture or customize your own from scratch.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(10.dp))

            val templatePresets = listOf(
                Triple("earnfast", "⚡ EarnFast", "Image Finder + Tic-Tac-Toe AI + Math Quiz + Live Counter"),
                Triple("realcash", "🎲 RealCash", "1v1 Ludo Board Canvas + Spin Wheel + Scratch Card"),
                Triple("gametozone", "🎮 GameToZone", "Arcade Games Iframe + 30s Timer + Watch Cooldown"),
                Triple("cashreward", "🎨 CashReward", "Color RGB Guessing + 60s Special Ad Tasks + Visit"),
                Triple("default", "🛠️ Custom Classic", "Standard Watch & Earn, Referrals & Tasks")
            )

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(templatePresets.size) { idx ->
                    val (key, title, subtitle) = templatePresets[idx]
                    val isSelected = formData.templateSource.equals(key, ignoreCase = true)
                    Card(
                        modifier = Modifier
                            .width(220.dp)
                            .clickable {
                                onUpdate {
                                    val updated = when (key) {
                                        "earnfast" -> it.copy(
                                            templateSource = "earnfast",
                                            appName = if (it.appName == "TurboReward" || it.appName.isBlank()) "EarnFast" else it.appName,
                                            appTagline = "Play & Win Coins",
                                            accentColor = "#6366F1"
                                        )
                                        "realcash" -> it.copy(
                                            templateSource = "realcash",
                                            appName = if (it.appName == "TurboReward" || it.appName.isBlank()) "RealCash Ludo" else it.appName,
                                            appTagline = "1v1 Ludo Battle & Daily Spin",
                                            accentColor = "#4F46E5"
                                        )
                                        "gametozone" -> it.copy(
                                            templateSource = "gametozone",
                                            appName = if (it.appName == "TurboReward" || it.appName.isBlank()) "GameToZone" else it.appName,
                                            appTagline = "Play HTML5 Games & Watch Ads",
                                            accentColor = "#EA580C"
                                        )
                                        "cashreward" -> it.copy(
                                            templateSource = "cashreward",
                                            appName = if (it.appName == "TurboReward" || it.appName.isBlank()) "CashReward" else it.appName,
                                            appTagline = "Color Games & Special Tasks",
                                            accentColor = "#FACC15"
                                        )
                                        else -> it.copy(templateSource = "default")
                                    }
                                    updated
                                }
                            },
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                        ),
                        border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = title,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleSmall,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                )
                                if (isSelected) {
                                    Icon(
                                        Icons.Default.Check,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = subtitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 2
                            )
                        }
                    }
                }
            }
        }

        item {
            Text(
                text = "Mini App Identity",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Define the visual styling, theme, and logo for your Telegram Mini App.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        item {
            OutlinedTextField(
                value = formData.appName,
                onValueChange = { newVal -> onUpdate { it.copy(appName = newVal) } },
                label = { Text("App Name") },
                placeholder = { Text("e.g. TurboReward") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_app_name")
            )
        }

        item {
            OutlinedTextField(
                value = formData.appTagline,
                onValueChange = { newVal -> onUpdate { it.copy(appTagline = newVal) } },
                label = { Text("Tagline / Subtitle") },
                placeholder = { Text("e.g. Watch videos, play games & earn rewards") },
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Accent Color
        item {
            Text(
                text = "Accent Color",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                PRESET_COLORS.forEach { hex ->
                    val color = try { Color(android.graphics.Color.parseColor(hex)) } catch (e: Exception) { Color.Cyan }
                    val isSelected = formData.accentColor.equals(hex, ignoreCase = true)
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(color)
                            .border(
                                width = if (isSelected) 3.dp else 1.dp,
                                color = if (isSelected) Color.White else Color.Transparent,
                                shape = CircleShape
                            )
                            .clickable { onUpdate { it.copy(accentColor = hex) } },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSelected) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = formData.accentColor,
                onValueChange = { newVal -> onUpdate { it.copy(accentColor = newVal) } },
                label = { Text("Custom Hex Color") },
                placeholder = { Text("#0284C7") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Logo & Asset Store
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "App Logo / Brand Icon",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Pick curated icons, upload from gallery, or paste a Google Drive link",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp
                            )
                        }

                        // Logo Live Preview Thumbnail
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(46.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                when {
                                    formData.logoData.startsWith("http") -> {
                                        AsyncImage(
                                            model = ImageRequest.Builder(LocalContext.current)
                                                .data(formData.logoData)
                                                .crossfade(true)
                                                .build(),
                                            contentDescription = "Current Logo",
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .clip(CircleShape),
                                            contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                        )
                                    }
                                    formData.logoData.isNotBlank() -> {
                                        Icon(
                                            Icons.Default.Check,
                                            contentDescription = "Logo Loaded",
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                    else -> {
                                        Text("⚡", fontSize = 20.sp)
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Action buttons: Asset Store & Gallery
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { showAssetStoreDialog = true },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("🛍️ Browse Icon Store", fontSize = 12.sp)
                        }

                        OutlinedButton(
                            onClick = {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Gallery", fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Direct Link or Drive Link Input
                    OutlinedTextField(
                        value = rawLogoInput,
                        onValueChange = { newVal ->
                            rawLogoInput = newVal
                            val converted = DriveLinkConverter.sanitizeOrConvert(newVal)
                            onUpdate { it.copy(logoData = converted) }
                        },
                        label = { Text("Image URL or Google Drive Link") },
                        placeholder = { Text("https://drive.google.com/file/d/... or direct image URL") },
                        leadingIcon = {
                            Icon(Icons.Default.Link, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        },
                        trailingIcon = {
                            if (formData.logoData.isNotBlank()) {
                                IconButton(onClick = {
                                    rawLogoInput = ""
                                    onUpdate { it.copy(logoData = "") }
                                }) {
                                    Icon(Icons.Default.Close, contentDescription = "Clear", tint = MaterialTheme.colorScheme.error)
                                }
                            }
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    if (DriveLinkConverter.isGoogleDriveLink(rawLogoInput)) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "✓ Google Drive link detected & converted to direct CDN stream!",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    } else if (formData.logoData.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "✓ Custom logo active",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        // Font Family Dropdown
        item {
            var expanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                OutlinedTextField(
                    value = formData.fontFamily,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Google Font") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = true)
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    FONT_OPTIONS.forEach { font ->
                        DropdownMenuItem(
                            text = { Text(font) },
                            onClick = {
                                onUpdate { it.copy(fontFamily = font) }
                                expanded = false
                            }
                        )
                    }
                }
            }
        }

        // Theme
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Dark Theme", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                        Text(
                            text = if (formData.theme == "dark") "Modern Telegram dark canvas" else "Clean bright light canvas",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = formData.theme == "dark",
                        onCheckedChange = { checked ->
                            onUpdate { it.copy(theme = if (checked) "dark" else "light") }
                        }
                    )
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------------------
// STEP 2: FIREBASE CONFIG
// -----------------------------------------------------------------------------------------
@Composable
fun StepFirebase(
    formData: FormData,
    onUpdate: ((FormData) -> FormData) -> Unit,
    onPasteConfig: (String) -> Boolean
) {
    var pastedJsonText by remember { mutableStateOf(formData.fullConfigJson) }
    var pasteStatus by remember { mutableStateOf<String?>(null) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Firebase Backend",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Your Telegram Mini App connects directly to your Firebase Firestore database for real-time coin balances and payouts.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Admin Email
        item {
            OutlinedTextField(
                value = formData.adminEmail,
                onValueChange = { email -> onUpdate { it.copy(adminEmail = email.trim()) } },
                label = { Text("Admin Email (Required for Admin Panel)") },
                placeholder = { Text("admin@example.com") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_admin_email")
            )
            Text(
                text = "Used by firestore.rules to protect administrative queries and allow login to admin.html.",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        // Paste Full Firebase Config Box
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Quick Paste: Firebase Config Object",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Paste the JSON object from Firebase Console > Project settings > General > Your apps",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = pastedJsonText,
                        onValueChange = { pastedJsonText = it },
                        placeholder = { Text("{\n  \"apiKey\": \"AIza...\",\n  \"projectId\": \"my-app\"\n}") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = {
                                val ok = onPasteConfig(pastedJsonText)
                                pasteStatus = if (ok) "Successfully parsed & populated config!" else "Invalid JSON format"
                            }
                        ) {
                            Text("Parse & Fill Fields")
                        }
                        pasteStatus?.let {
                            Text(
                                text = it,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (it.startsWith("Success")) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
        }

        // Discrete Fields
        item {
            Text(
                text = "Individual Configuration Fields",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
        }

        item {
            OutlinedTextField(
                value = formData.apiKey,
                onValueChange = { v -> onUpdate { it.copy(apiKey = v.trim()) } },
                label = { Text("API Key") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            OutlinedTextField(
                value = formData.projectId,
                onValueChange = { v -> onUpdate { it.copy(projectId = v.trim()) } },
                label = { Text("Project ID") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            OutlinedTextField(
                value = formData.authDomain,
                onValueChange = { v -> onUpdate { it.copy(authDomain = v.trim()) } },
                label = { Text("Auth Domain") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            OutlinedTextField(
                value = formData.storageBucket,
                onValueChange = { v -> onUpdate { it.copy(storageBucket = v.trim()) } },
                label = { Text("Storage Bucket (Optional)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            OutlinedTextField(
                value = formData.appId,
                onValueChange = { v -> onUpdate { it.copy(appId = v.trim()) } },
                label = { Text("App ID") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

// -----------------------------------------------------------------------------------------
// STEP 3: ECONOMY
// -----------------------------------------------------------------------------------------
@Composable
fun StepEconomy(
    formData: FormData,
    onUpdate: ((FormData) -> FormData) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Token Economics & Currency",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Configure coin incentives, signup gifts, and real-world currency exchange rates.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Live Rate Preview Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Active Conversion Rate",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Text(
                        text = "${formData.coinRateCoins} Coins = ${formData.currencySymbol}${formData.coinRateCurrency}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    val minFiat = ((formData.minWithdrawal.toDouble() / formData.coinRateCoins) * formData.coinRateCurrency)
                    Text(
                        text = "Minimum withdrawal (${formData.minWithdrawal} coins) evaluates to ${formData.currencySymbol}${String.format("%.2f", minFiat)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = formData.coinRateCoins.toString(),
                    onValueChange = { v -> onUpdate { it.copy(coinRateCoins = v.toIntOrNull() ?: 1000) } },
                    label = { Text("Rate Coins") },
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = formData.currencySymbol,
                    onValueChange = { v -> onUpdate { it.copy(currencySymbol = v) } },
                    label = { Text("Currency Symbol") },
                    modifier = Modifier.weight(0.8f)
                )
                OutlinedTextField(
                    value = formData.coinRateCurrency.toString(),
                    onValueChange = { v -> onUpdate { it.copy(coinRateCurrency = v.toDoubleOrNull() ?: 10.0) } },
                    label = { Text("Fiat Amount") },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            OutlinedTextField(
                value = formData.signupBonus.toString(),
                onValueChange = { v -> onUpdate { it.copy(signupBonus = v.toIntOrNull() ?: 0) } },
                label = { Text("Welcome / Signup Bonus (Coins)") },
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            OutlinedTextField(
                value = formData.adWatchReward.toString(),
                onValueChange = { v -> onUpdate { it.copy(adWatchReward = v.toIntOrNull() ?: 0) } },
                label = { Text("Reward per Video Ad Watch (Coins)") },
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            OutlinedTextField(
                value = formData.referralBonus.toString(),
                onValueChange = { v -> onUpdate { it.copy(referralBonus = v.toIntOrNull() ?: 0) } },
                label = { Text("Referral Bonus per Invited Friend (Coins)") },
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            OutlinedTextField(
                value = formData.minWithdrawal.toString(),
                onValueChange = { v -> onUpdate { it.copy(minWithdrawal = v.toIntOrNull() ?: 100) } },
                label = { Text("Minimum Withdrawal Threshold (Coins)") },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

// -----------------------------------------------------------------------------------------
// STEP 4: FEATURES
// -----------------------------------------------------------------------------------------
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun StepFeatures(
    formData: FormData,
    onUpdate: ((FormData) -> FormData) -> Unit
) {
    var newMethodInput by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "Feature Toggles & Mini Games",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Disabled features are cleanly stripped out of the generated HTML template without leaving orphan code.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        item {
            FeatureToggleCard(
                title = "Watch & Earn (Video Ads)",
                desc = "Rewarded video ad button with countdown and instant coin credit",
                icon = "📺",
                checked = formData.enableWatchEarn,
                onCheckedChange = { ch -> onUpdate { it.copy(enableWatchEarn = ch) } }
            )
        }

        item {
            FeatureToggleCard(
                title = "Visit & Earn (Link Clicker)",
                desc = "15-second partner website visits with reward timer",
                icon = "🌐",
                checked = formData.enableVisitEarn,
                onCheckedChange = { ch -> onUpdate { it.copy(enableVisitEarn = ch) } }
            )
        }

        item {
            FeatureToggleCard(
                title = "Social Tasks",
                desc = "Join Telegram channel and follow official accounts",
                icon = "📢",
                checked = formData.enableSocialTasks,
                onCheckedChange = { ch -> onUpdate { it.copy(enableSocialTasks = ch) } }
            )
        }

        item {
            FeatureToggleCard(
                title = "Lucky Spin Wheel",
                desc = "Interactive 8-segment physics canvas fortune wheel",
                icon = "🎡",
                checked = formData.enableSpinWheel,
                onCheckedChange = { ch -> onUpdate { it.copy(enableSpinWheel = ch) } }
            )
        }

        item {
            FeatureToggleCard(
                title = "Color Guess Game",
                desc = "Guess next gem color for 2x to 5x multiplier prizes",
                icon = "💎",
                checked = formData.enableColorGuess,
                onCheckedChange = { ch -> onUpdate { it.copy(enableColorGuess = ch) } }
            )
        }

        item {
            FeatureToggleCard(
                title = "Web3 / Crypto Quiz",
                desc = "Multiple choice trivia quiz with coin reward",
                icon = "🧠",
                checked = formData.enableQuizGame,
                onCheckedChange = { ch -> onUpdate { it.copy(enableQuizGame = ch) } }
            )
        }

        item {
            FeatureToggleCard(
                title = "Tic-Tac-Toe vs Bot",
                desc = "Classic 3x3 interactive board game with victory coins",
                icon = "❌",
                checked = formData.enableTicTacToe,
                onCheckedChange = { ch -> onUpdate { it.copy(enableTicTacToe = ch) } }
            )
        }

        item {
            FeatureToggleCard(
                title = "Referral System",
                desc = "Unique Telegram invite links, copy button, and friend counter",
                icon = "🤝",
                checked = formData.enableReferral,
                onCheckedChange = { ch -> onUpdate { it.copy(enableReferral = ch) } }
            )
        }

        item {
            FeatureToggleCard(
                title = "Cash Withdrawal System",
                desc = "User payout requests submitted to Firestore for admin review",
                icon = "💳",
                checked = formData.enableWithdrawal,
                onCheckedChange = { ch -> onUpdate { it.copy(enableWithdrawal = ch) } }
            )
        }

        // Payment Methods list
        if (formData.enableWithdrawal) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "Supported Payout Methods",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            formData.paymentMethods.forEach { method ->
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = MaterialTheme.colorScheme.surface,
                                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(text = method, style = MaterialTheme.typography.labelMedium)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Remove",
                                            modifier = Modifier
                                                .size(16.dp)
                                                .clickable {
                                                    val updated = formData.paymentMethods.filter { it != method }
                                                    onUpdate { it.copy(paymentMethods = updated) }
                                                }
                                        )
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = newMethodInput,
                                onValueChange = { newMethodInput = it },
                                placeholder = { Text("e.g. Bank Transfer, TON") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            Button(
                                onClick = {
                                    val trimmed = newMethodInput.trim()
                                    if (trimmed.isNotBlank() && !formData.paymentMethods.contains(trimmed)) {
                                        val updated = formData.paymentMethods + trimmed
                                        onUpdate { it.copy(paymentMethods = updated) }
                                        newMethodInput = ""
                                    }
                                }
                            ) {
                                Text("Add")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FeatureToggleCard(
    title: String,
    desc: String,
    icon: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(icon, fontSize = 20.sp)
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
                Text(text = desc, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Switch(checked = checked, onCheckedChange = onCheckedChange)
        }
    }
}

// -----------------------------------------------------------------------------------------
// STEP 5: AD NETWORK
// -----------------------------------------------------------------------------------------
@Composable
fun StepAdNetwork(
    formData: FormData,
    onUpdate: ((FormData) -> FormData) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Ad Network Integration",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Configure rewarded video ad delivery via Telegram Mini App ad networks such as Adsgram, Monetag, or custom scripts.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        item {
            OutlinedTextField(
                value = formData.adSdkScriptUrl,
                onValueChange = { v -> onUpdate { it.copy(adSdkScriptUrl = v.trim()) } },
                label = { Text("Ad SDK Script URL") },
                placeholder = { Text("https://sad.adsgram.ai/js/sad.min.js") },
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            OutlinedTextField(
                value = formData.adZoneId,
                onValueChange = { v -> onUpdate { it.copy(adZoneId = v.trim()) } },
                label = { Text("Ad Zone ID / Block ID") },
                placeholder = { Text("e.g. 1024") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "💡 Zero-Ad Safety Simulation",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Even if no live Ad SDK is available during initial development, the template includes a built-in simulation fallback timer with realistic cooldowns, haptics, and instant reward crediting so users and testers can experience the full loop.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 18.sp
                    )
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------------------
// STEP 6: REVIEW & GENERATE
// -----------------------------------------------------------------------------------------
@Composable
fun StepReview(
    formData: FormData,
    onUpdate: ((FormData) -> FormData) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Review & Finalize",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Inspect your app configuration. Tapping Forge generates a production-ready package right on this device.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Summary Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    ReviewRow("Source Preset", formData.templateSource.uppercase())
                    ReviewRow("App Name", formData.appName)
                    ReviewRow("Tagline", formData.appTagline)
                    ReviewRow("Admin Email", formData.adminEmail)
                    ReviewRow("Accent Color", formData.accentColor)
                    ReviewRow("Theme / Font", "${formData.theme.uppercase()} · ${formData.fontFamily}")
                    ReviewRow("Exchange Rate", "${formData.coinRateCoins} = ${formData.currencySymbol}${formData.coinRateCurrency}")
                    ReviewRow("Payout Methods", formData.paymentMethods.joinToString(", "))
                }
            }
        }

        // AI Fine-Tuning Toggle
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "AI Refinement Mode",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Enables Gemini conversational editing and diff preview on the export screen to refine HTML code with natural language.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Switch(
                        checked = formData.fineTuneWithAi,
                        onCheckedChange = { checked ->
                            onUpdate { it.copy(fineTuneWithAi = checked) }
                        }
                    )
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "📦 What Will Be Generated:",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("• index.html — Self-contained Telegram WebApp", style = MaterialTheme.typography.bodySmall)
                    Text("• admin.html — Live administrator panel for payouts", style = MaterialTheme.typography.bodySmall)
                    Text("• firestore.rules — Production database security rules", style = MaterialTheme.typography.bodySmall)
                    Text("• SETUP.md — Detailed deployment instructions", style = MaterialTheme.typography.bodySmall)
                    Text("• ZIP Bundle — Ready to share or export", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

@Composable
fun ReviewRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
    }
}
