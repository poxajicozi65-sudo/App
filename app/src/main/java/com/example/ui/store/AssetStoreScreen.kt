package com.example.ui.store

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.store.AssetCategory
import com.example.data.store.AssetStoreRepository
import com.example.data.store.AssetType
import com.example.data.store.SavedDriveAsset
import com.example.data.store.StoreAsset
import com.example.util.ConvertedLinkResult
import com.example.util.DriveLinkConverter

enum class StoreMainTab(val label: String, val icon: String) {
    DRIVE_CONVERTER("Drive Converter", "🔗"),
    ICONS("Icon Store", "🎨"),
    BANNERS("Banners & Art", "🖼️"),
    SAVED("My Assets", "⭐")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssetStoreScreen(
    onSelectAsset: ((String) -> Unit)? = null, // When used as a picker dialog/screen
    onClose: (() -> Unit)? = null,
    initialTab: StoreMainTab = StoreMainTab.DRIVE_CONVERTER
) {
    val context = LocalContext.current
    val repository = remember { AssetStoreRepository(context) }

    var selectedTab by remember { mutableStateOf(initialTab) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(AssetCategory.ALL) }
    var savedAssets by remember { mutableStateOf(repository.getSavedDriveAssets()) }

    // Drive converter state
    var driveInputUrl by remember { mutableStateOf("") }
    var convertedResult by remember { mutableStateOf<ConvertedLinkResult?>(null) }
    var isTestingImage by remember { mutableStateOf(false) }
    var imageTestSuccess by remember { mutableStateOf<Boolean?>(null) }
    var showDriveGuideDialog by remember { mutableStateOf(false) }

    fun copyToClipboard(text: String, label: String = "Asset Link") {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(label, text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "$label copied to clipboard! ✓", Toast.LENGTH_SHORT).show()
    }

    // Auto-convert drive links whenever user types or pastes
    LaunchedEffect(driveInputUrl) {
        if (driveInputUrl.isNotBlank()) {
            val result = DriveLinkConverter.convert(driveInputUrl)
            convertedResult = result
            imageTestSuccess = null
        } else {
            convertedResult = null
            imageTestSuccess = null
        }
    }

    if (showDriveGuideDialog) {
        DriveSharingGuideDialog(onDismiss = { showDriveGuideDialog = false })
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Asset & Icon Store",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.primaryContainer,
                                modifier = Modifier.padding(horizontal = 4.dp)
                            ) {
                                Text(
                                    text = if (onSelectAsset != null) "Picker Mode" else "Hub",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = "Find icons, curated images & convert Google Drive share links",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.sp
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showDriveGuideDialog = true }) {
                        Icon(
                            Icons.AutoMirrored.Filled.HelpOutline,
                            contentDescription = "Drive Guide",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    if (onClose != null) {
                        IconButton(onClick = onClose) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Main Top Tabs
            PrimaryTabRow(
                selectedTabIndex = selectedTab.ordinal,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                StoreMainTab.values().forEach { tab ->
                    val isSelected = selectedTab == tab
                    Tab(
                        selected = isSelected,
                        onClick = { selectedTab = tab },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(tab.icon, fontSize = 14.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = tab.label,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 12.sp,
                                    maxLines = 1
                                )
                            }
                        },
                        modifier = Modifier.testTag("store_tab_${tab.name.lowercase()}")
                    )
                }
            }

            // Tab Content
            when (selectedTab) {
                StoreMainTab.DRIVE_CONVERTER -> {
                    DriveConverterTabContent(
                        inputUrl = driveInputUrl,
                        onUrlChange = { driveInputUrl = it },
                        convertedResult = convertedResult,
                        onCopy = { text, label -> copyToClipboard(text, label) },
                        onSaveAsset = {
                            convertedResult?.let { res ->
                                val saved = repository.saveDriveAsset(
                                    title = "Drive Asset",
                                    originalUrl = res.originalUrl,
                                    directUrl = res.directImageUrl
                                )
                                savedAssets = repository.getSavedDriveAssets()
                                Toast.makeText(context, "Saved to My Assets! ✓", Toast.LENGTH_SHORT).show()
                            }
                        },
                        onSelectForApp = onSelectAsset?.let { callback ->
                            { url ->
                                callback(url)
                                onClose?.invoke()
                            }
                        },
                        onOpenGuide = { showDriveGuideDialog = true }
                    )
                }

                StoreMainTab.ICONS -> {
                    CuratedAssetsTabContent(
                        query = searchQuery,
                        onQueryChange = { searchQuery = it },
                        selectedCategory = selectedCategory,
                        onCategorySelect = { selectedCategory = it },
                        categoryOptions = listOf(
                            AssetCategory.ALL,
                            AssetCategory.CRYPTO,
                            AssetCategory.GAMING,
                            AssetCategory.REWARDS,
                            AssetCategory.TELEGRAM
                        ),
                        assets = repository.searchAssets(searchQuery, selectedCategory).filter {
                            it.category != AssetCategory.BANNERS && it.category != AssetCategory.AVATARS
                        },
                        onCopyLink = { url -> copyToClipboard(url, "Icon Image Link") },
                        onSelectAsset = onSelectAsset?.let { callback ->
                            { url ->
                                callback(url)
                                onClose?.invoke()
                            }
                        }
                    )
                }

                StoreMainTab.BANNERS -> {
                    CuratedAssetsTabContent(
                        query = searchQuery,
                        onQueryChange = { searchQuery = it },
                        selectedCategory = selectedCategory,
                        onCategorySelect = { selectedCategory = it },
                        categoryOptions = listOf(
                            AssetCategory.ALL,
                            AssetCategory.BANNERS,
                            AssetCategory.AVATARS
                        ),
                        assets = repository.searchAssets(searchQuery, selectedCategory).filter {
                            it.category == AssetCategory.BANNERS || it.category == AssetCategory.AVATARS
                        },
                        onCopyLink = { url -> copyToClipboard(url, "Banner / Avatar Link") },
                        onSelectAsset = onSelectAsset?.let { callback ->
                            { url ->
                                callback(url)
                                onClose?.invoke()
                            }
                        }
                    )
                }

                StoreMainTab.SAVED -> {
                    SavedAssetsTabContent(
                        savedAssets = savedAssets,
                        onDelete = { id ->
                            repository.deleteSavedDriveAsset(id)
                            savedAssets = repository.getSavedDriveAssets()
                            Toast.makeText(context, "Deleted asset", Toast.LENGTH_SHORT).show()
                        },
                        onCopy = { url -> copyToClipboard(url, "Direct Image URL") },
                        onSelectAsset = onSelectAsset?.let { callback ->
                            { url ->
                                callback(url)
                                onClose?.invoke()
                            }
                        },
                        onGoToConverter = { selectedTab = StoreMainTab.DRIVE_CONVERTER }
                    )
                }
            }
        }
    }
}

/**
 * Drive Link to Direct Image Converter Tab
 */
@Composable
private fun DriveConverterTabContent(
    inputUrl: String,
    onUrlChange: (String) -> Unit,
    convertedResult: ConvertedLinkResult?,
    onCopy: (String, String) -> Unit,
    onSaveAsset: () -> Unit,
    onSelectForApp: ((String) -> Unit)?,
    onOpenGuide: () -> Unit
) {
    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Explainer Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("🔗", fontSize = 24.sp)
                        }
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Google Drive Image Converter",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Standard Google Drive links do not display in HTML. Paste your sharing link to generate an ultra-fast, direct CDN image link.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Input Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Paste Cloud / Google Drive Link",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = inputUrl,
                        onValueChange = onUrlChange,
                        label = { Text("Google Drive URL") },
                        placeholder = { Text("https://drive.google.com/file/d/1aB2c.../view?usp=sharing") },
                        leadingIcon = {
                            Icon(Icons.Default.Link, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        },
                        trailingIcon = {
                            if (inputUrl.isNotBlank()) {
                                IconButton(onClick = { onUrlChange("") }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear")
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_drive_url"),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Quick Paste & Sample Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = clipboard.primaryClip
                                if (clip != null && clip.itemCount > 0) {
                                    val text = clip.getItemAt(0).text?.toString() ?: ""
                                    if (text.isNotBlank()) {
                                        onUrlChange(text)
                                    }
                                }
                            },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.ContentPaste, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Paste Clipboard", fontSize = 12.sp)
                        }

                        OutlinedButton(
                            onClick = {
                                onUrlChange("https://drive.google.com/file/d/1X5X9r9W9q9L4P2B1Z8Y/view?usp=sharing")
                            },
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Sample Link", fontSize = 12.sp)
                        }

                        IconButton(onClick = onOpenGuide) {
                            Icon(Icons.Default.Info, contentDescription = "Guide", tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }

        // Live Conversion Result Card
        if (convertedResult != null) {
            item {
                val result = convertedResult
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.primary)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Default.Check,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Direct Image Link Ready!",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Live Preview Box
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                AsyncImage(
                                    model = ImageRequest.Builder(LocalContext.current)
                                        .data(result.directImageUrl)
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = "Converted Image Live Preview",
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(8.dp)
                                        .clip(RoundedCornerShape(8.dp)),
                                    contentScale = ContentScale.Fit
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Direct CDN Link Row
                        Text(
                            text = "Direct Image URL (Google UserContent CDN):",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = result.directImageUrl,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontFamily = FontFamily.Monospace,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                IconButton(
                                    onClick = { onCopy(result.directImageUrl, "Direct Image URL") },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        Icons.Default.ContentCopy,
                                        contentDescription = "Copy",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Secondary Export URL Row
                        Text(
                            text = "Secondary Direct Link (drive.google.com/uc):",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = result.alternativeUrl,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontFamily = FontFamily.Monospace,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                IconButton(
                                    onClick = { onCopy(result.alternativeUrl, "Alternative Drive Direct URL") },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        Icons.Default.ContentCopy,
                                        contentDescription = "Copy",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Action Buttons: Copy All, Save to Assets, Apply as Logo
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { onCopy(result.directImageUrl, "Direct Image Link") },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Copy Link")
                            }

                            OutlinedButton(
                                onClick = onSaveAsset,
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.Star, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Save Asset")
                            }
                        }

                        if (onSelectForApp != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = { onSelectForApp(result.directImageUrl) },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("✓ Apply as Mini App Logo")
                            }
                        }
                    }
                }
            }
        }

        // Instructions Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "💡 How to make Google Drive images work:",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "1. In Google Drive, click Share on your file.\n" +
                                "2. Change General access from 'Restricted' to 'Anyone with the link'.\n" +
                                "3. Click 'Copy link' and paste it here.\n" +
                                "4. Use the generated direct CDN link in your app logo, banners, or HTML code!",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 20.sp
                    )
                }
            }
        }
    }
}

/**
 * Curated Assets (Icons, Crypto, Gaming, Banners, Avatars) Tab
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CuratedAssetsTabContent(
    query: String,
    onQueryChange: (String) -> Unit,
    selectedCategory: AssetCategory,
    onCategorySelect: (AssetCategory) -> Unit,
    categoryOptions: List<AssetCategory>,
    assets: List<StoreAsset>,
    onCopyLink: (String) -> Unit,
    onSelectAsset: ((String) -> Unit)?
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Search TextField
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            placeholder = { Text("Search assets (e.g. coin, game, ton, dog, flame)...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            trailingIcon = {
                if (query.isNotBlank()) {
                    IconButton(onClick = { onQueryChange("") }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear")
                    }
                }
            },
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Category Filter Chips
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(categoryOptions) { cat ->
                val isSelected = selectedCategory == cat
                FilterChip(
                    selected = isSelected,
                    onClick = { onCategorySelect(cat) },
                    label = { Text("${cat.icon} ${cat.label}") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Grid of Asset Cards
        if (assets.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No assets match \"$query\". Try a different search term or category.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 150.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 64.dp)
            ) {
                items(assets, key = { it.id }) { asset ->
                    StoreAssetCard(
                        asset = asset,
                        onCopyLink = { onCopyLink(asset.directUrl) },
                        onSelectAsset = onSelectAsset?.let { callback ->
                            { callback(asset.directUrl) }
                        }
                    )
                }
            }
        }
    }
}

/**
 * Individual Asset Card in the Store Grid
 */
@Composable
private fun StoreAssetCard(
    asset: StoreAsset,
    onCopyLink: () -> Unit,
    onSelectAsset: (() -> Unit)?
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Visual Preview Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(if (asset.category == AssetCategory.BANNERS) 80.dp else 68.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                if (asset.directUrl.isNotBlank()) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(asset.directUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = asset.title,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(4.dp),
                        contentScale = if (asset.category == AssetCategory.BANNERS) ContentScale.Crop else ContentScale.Fit
                    )
                } else {
                    Text(asset.previewEmoji, fontSize = 32.sp)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = asset.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = asset.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 10.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 13.sp,
                modifier = Modifier.padding(vertical = 2.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Action Buttons
            if (onSelectAsset != null) {
                Button(
                    onClick = onSelectAsset,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Select", fontSize = 11.sp)
                }
            } else {
                OutlinedButton(
                    onClick = onCopyLink,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Copy URL", fontSize = 11.sp)
                }
            }
        }
    }
}

/**
 * Saved Assets Tab
 */
@Composable
private fun SavedAssetsTabContent(
    savedAssets: List<SavedDriveAsset>,
    onDelete: (String) -> Unit,
    onCopy: (String) -> Unit,
    onSelectAsset: ((String) -> Unit)?,
    onGoToConverter: () -> Unit
) {
    if (savedAssets.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("⭐", fontSize = 48.sp)
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "No Saved Assets Yet",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Convert any Google Drive share link and click 'Save Asset' to access it anytime here.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = onGoToConverter) {
                    Text("Open Drive Converter")
                }
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(savedAssets, key = { it.id }) { item ->
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Image Thumbnail
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.size(52.dp)
                        ) {
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(item.directImageUrl)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = item.title,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = item.title,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = item.directImageUrl,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        IconButton(onClick = { onCopy(item.directImageUrl) }) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy Link", tint = MaterialTheme.colorScheme.primary)
                        }

                        if (onSelectAsset != null) {
                            IconButton(onClick = { onSelectAsset(item.directImageUrl) }) {
                                Icon(Icons.Default.Check, contentDescription = "Select", tint = MaterialTheme.colorScheme.secondary)
                            }
                        }

                        IconButton(onClick = { onDelete(item.id) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }
    }
}

/**
 * Visual step-by-step modal guide for Google Drive sharing
 */
@Composable
private fun DriveSharingGuideDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("📁", fontSize = 20.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Text("How to Get Drive Image Links")
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Follow these simple steps so your Google Drive images load smoothly in any Telegram Mini App:",
                    style = MaterialTheme.typography.bodySmall
                )

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = "Step 1: Open Google Drive",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.labelMedium
                        )
                        Text(
                            text = "Locate your image file, tap the three dots or right click, and choose Share.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = "Step 2: Change Access Permission",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.labelMedium
                        )
                        Text(
                            text = "Under 'General access', change from 'Restricted' to 'Anyone with the link can view'.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = "Step 3: Copy and Convert Here",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.labelMedium
                        )
                        Text(
                            text = "Tap 'Copy link', paste it in this Drive Converter tab, and get the high-speed CDN direct URL instantly!",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Got It!")
            }
        }
    )
}
