package com.example.ui.bot.canvas

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.automirrored.filled.FormatAlignLeft
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.BotButton
import com.example.data.model.BotFlow
import com.example.data.model.BotNode
import com.example.data.model.BotNodeType
import com.example.ui.bot.BotStudioTab
import com.example.ui.bot.BotStudioViewModel
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CanvasBuilderView(
    viewModel: BotStudioViewModel,
    modifier: Modifier = Modifier
) {
    val flow by viewModel.botFlow.collectAsStateWithLifecycle()
    val selectedNode by viewModel.selectedNode.collectAsStateWithLifecycle()

    var showAddNodeDialog by remember { mutableStateOf(false) }
    var showEditNodeSheet by remember { mutableStateOf(false) }
    var showHtmlEditorSheet by remember { mutableStateOf(false) }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Grid pattern background & connection lines
        Canvas(modifier = Modifier.fillMaxSize()) {
            val gridStep = 40.dp.toPx()
            val primaryColor = Color(0xFF3B82F6).copy(alpha = 0.15f)

            // Subtle dot grid
            var x = 0f
            while (x < size.width) {
                var y = 0f
                while (y < size.height) {
                    drawCircle(
                        color = Color.Gray.copy(alpha = 0.12f),
                        radius = 1.5f,
                        center = Offset(x, y)
                    )
                    y += gridStep
                }
                x += gridStep
            }

            // Draw connection lines between linked nodes
            val nodeMap = flow.nodes.associateBy { it.id }
            for (node in flow.nodes) {
                if (node.nextNodeId != null && nodeMap.containsKey(node.nextNodeId)) {
                    val target = nodeMap[node.nextNodeId]!!
                    val startX = (node.posX + 110f) * density
                    val startY = (node.posY + 70f) * density
                    val endX = (target.posX + 110f) * density
                    val endY = (target.posY + 20f) * density

                    val path = Path().apply {
                        moveTo(startX, startY)
                        cubicTo(
                            startX, startY + 60f,
                            endX, endY - 60f,
                            endX, endY
                        )
                    }

                    drawPath(
                        path = path,
                        color = primaryColor.copy(alpha = 0.7f),
                        style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                    )

                    // Draw arrowhead dot
                    drawCircle(
                        color = Color(0xFF3B82F6),
                        radius = 5.dp.toPx(),
                        center = Offset(endX, endY)
                    )
                }
            }
        }

        // Draggable Nodes Container
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 80.dp)
        ) {
            flow.nodes.forEach { node ->
                CanvasNodeCard(
                    node = node,
                    isSelected = selectedNode?.id == node.id,
                    onSelect = {
                        viewModel.selectNode(node)
                        showEditNodeSheet = true
                    },
                    onDrag = { dx, dy ->
                        viewModel.updateNodePosition(node.id, dx, dy)
                    }
                )
            }
        }

        // Top Toolbar Overlay
        Surface(
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
            tonalElevation = 3.dp,
            shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Canvas Builder",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${flow.nodes.size} nodes • Drag to arrange",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = { showHtmlEditorSheet = true },
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Icon(Icons.Default.Code, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("MiniApp HTML", style = MaterialTheme.typography.labelMedium)
                    }

                    Button(
                        onClick = { viewModel.selectTab(BotStudioTab.PREVIEW) },
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Simulate", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }

        // Floating Action Buttons (Bottom Right)
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalAlignment = Alignment.End
        ) {
            // Auto Layout Button
            FloatingActionButton(
                onClick = {
                    // Auto-arrange in staggered vertical column
                    var currentY = 70f
                    flow.nodes.forEachIndexed { idx, n ->
                        viewModel.updateNodePosition(n.id, 40f - n.posX, currentY - n.posY)
                        currentY += 160f
                    }
                },
                shape = CircleShape,
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(44.dp)
            ) {
                Icon(Icons.AutoMirrored.Filled.FormatAlignLeft, contentDescription = "Auto Layout", modifier = Modifier.size(20.dp))
            }

            // Add Node FAB
            FloatingActionButton(
                onClick = { showAddNodeDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("canvas_add_node_fab")
            ) {
                Row(modifier = Modifier.padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Add, contentDescription = "Add Node")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Add Node", fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    // Add Node Dialog
    if (showAddNodeDialog) {
        AddNodeDialog(
            onDismiss = { showAddNodeDialog = false },
            onAdd = { type, title, content, buttons ->
                viewModel.addNode(type, title, content, buttons)
                showAddNodeDialog = false
            }
        )
    }

    // Node Inspector / Editor Bottom Sheet
    if (showEditNodeSheet && selectedNode != null) {
        ModalBottomSheet(
            onDismissRequest = { showEditNodeSheet = false },
            sheetState = sheetState
        ) {
            NodeEditorContent(
                node = selectedNode!!,
                allNodes = flow.nodes,
                onUpdate = { updated ->
                    viewModel.updateNode(updated)
                },
                onDelete = {
                    viewModel.deleteNode(selectedNode!!.id)
                    showEditNodeSheet = false
                },
                onClose = { showEditNodeSheet = false }
            )
        }
    }

    // MiniApp HTML Editor Sheet
    if (showHtmlEditorSheet) {
        ModalBottomSheet(
            onDismissRequest = { showHtmlEditorSheet = false },
            sheetState = sheetState
        ) {
            HtmlEditorContent(
                initialHtml = flow.webappHtml,
                onSave = { newHtml ->
                    viewModel.updateWebappHtml(newHtml)
                    showHtmlEditorSheet = false
                },
                onClose = { showHtmlEditorSheet = false }
            )
        }
    }
}

@Composable
fun CanvasNodeCard(
    node: BotNode,
    isSelected: Boolean,
    onSelect: () -> Unit,
    onDrag: (Float, Float) -> Unit
) {
    val borderColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
    val badgeColor = when (node.type) {
        BotNodeType.TRIGGER -> Color(0xFF0284C7)
        BotNodeType.MESSAGE -> Color(0xFF7C3AED)
        BotNodeType.INLINE_BUTTONS -> Color(0xFFD97706)
        BotNodeType.REPLY_KEYBOARD -> Color(0xFF2563EB)
        BotNodeType.ACTION_LOGIC -> Color(0xFF059669)
        BotNodeType.WEBAPP_VIEW -> Color(0xFF4F46E5)
        BotNodeType.QUIZ_POLL -> Color(0xFF9333EA)
        BotNodeType.PAYMENT_STARS -> Color(0xFFEAB308)
    }

    Card(
        modifier = Modifier
            .offset { IntOffset(node.posX.roundToInt(), node.posY.roundToInt()) }
            .width(220.dp)
            .pointerInput(node.id) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    onDrag(dragAmount.x / density, dragAmount.y / density)
                }
            }
            .clickable { onSelect() }
            .border(if (isSelected) 2.dp else 1.dp, borderColor, RoundedCornerShape(14.dp))
            .testTag("node_card_${node.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(if (isSelected) 8.dp else 3.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Node Header Badge
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = badgeColor.copy(alpha = 0.15f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(node.type.icon, fontSize = 12.sp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = node.type.label,
                            style = MaterialTheme.typography.labelSmall,
                            color = badgeColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        )
                    }
                }

                if (node.nextNodeId != null) {
                    Text("➜ Next", fontSize = 10.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Title
            Text(
                text = node.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )

            // Content preview
            Text(
                text = node.content,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                fontSize = 11.sp,
                lineHeight = 15.sp,
                modifier = Modifier.padding(top = 2.dp)
            )

            // Button chips preview
            if (node.buttons.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    node.buttons.take(2).forEach { btn ->
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "🎛️ ${btn.text}",
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 10.sp,
                                maxLines = 1,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }
                    }
                    if (node.buttons.size > 2) {
                        Text("+${node.buttons.size - 2} more buttons", fontSize = 9.sp, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NodeEditorContent(
    node: BotNode,
    allNodes: List<BotNode>,
    onUpdate: (BotNode) -> Unit,
    onDelete: () -> Unit,
    onClose: () -> Unit
) {
    var title by remember(node.id) { mutableStateOf(node.title) }
    var content by remember(node.id) { mutableStateOf(node.content) }
    var selectedType by remember(node.id) { mutableStateOf(node.type) }
    var nextNodeId by remember(node.id) { mutableStateOf(node.nextNodeId) }
    var buttons by remember(node.id) { mutableStateOf(node.buttons) }

    var typeExpanded by remember { mutableStateOf(false) }
    var nextExpanded by remember { mutableStateOf(false) }

    var newButtonText by remember { mutableStateOf("") }
    var newButtonType by remember { mutableStateOf("callback") }
    var newButtonPayload by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Edit Canvas Node",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete Node", tint = MaterialTheme.colorScheme.error)
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Title
        OutlinedTextField(
            value = title,
            onValueChange = {
                title = it
                onUpdate(node.copy(title = it))
            },
            label = { Text("Node Title") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Type dropdown
        ExposedDropdownMenuBox(
            expanded = typeExpanded,
            onExpandedChange = { typeExpanded = it }
        ) {
            OutlinedTextField(
                value = "${selectedType.icon} ${selectedType.label}",
                onValueChange = {},
                readOnly = true,
                label = { Text("Node Type") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeExpanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                shape = RoundedCornerShape(12.dp)
            )
            ExposedDropdownMenu(
                expanded = typeExpanded,
                onDismissRequest = { typeExpanded = false }
            ) {
                BotNodeType.values().forEach { t ->
                    DropdownMenuItem(
                        text = { Text("${t.icon} ${t.label}") },
                        onClick = {
                            selectedType = t
                            typeExpanded = false
                            onUpdate(node.copy(type = t))
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Content
        OutlinedTextField(
            value = content,
            onValueChange = {
                content = it
                onUpdate(node.copy(content = it))
            },
            label = { Text(if (selectedType == BotNodeType.TRIGGER) "Command Trigger (e.g. /start)" else "Message / Content") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 2,
            maxLines = 5,
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Next Connected Node Dropdown
        ExposedDropdownMenuBox(
            expanded = nextExpanded,
            onExpandedChange = { nextExpanded = it }
        ) {
            val targetTitle = allNodes.firstOrNull { it.id == nextNodeId }?.title ?: "None (End of flow)"
            OutlinedTextField(
                value = targetTitle,
                onValueChange = {},
                readOnly = true,
                label = { Text("Next Connected Node") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = nextExpanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                shape = RoundedCornerShape(12.dp)
            )
            ExposedDropdownMenu(
                expanded = nextExpanded,
                onDismissRequest = { nextExpanded = false }
            ) {
                DropdownMenuItem(
                    text = { Text("None (End of flow)") },
                    onClick = {
                        nextNodeId = null
                        nextExpanded = false
                        onUpdate(node.copy(nextNodeId = null))
                    }
                )
                allNodes.filter { it.id != node.id }.forEach { other ->
                    DropdownMenuItem(
                        text = { Text("${other.type.icon} ${other.title}") },
                        onClick = {
                            nextNodeId = other.id
                            nextExpanded = false
                            onUpdate(node.copy(nextNodeId = other.id))
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Buttons List (For Inline Keyboard / Message nodes)
        Text(
            text = "Interactive Buttons (${buttons.size})",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(6.dp))

        buttons.forEachIndexed { idx, btn ->
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(btn.text, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text("Type: ${btn.type} • Payload: ${btn.payload}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    IconButton(
                        onClick = {
                            val newBtns = buttons.filterIndexed { i, _ -> i != idx }
                            buttons = newBtns
                            onUpdate(node.copy(buttons = newBtns))
                        },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Remove", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }

        // Add new button input
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = newButtonText,
                onValueChange = { newButtonText = it },
                label = { Text("Button Text") },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(10.dp)
            )
            Button(
                onClick = {
                    if (newButtonText.isNotBlank()) {
                        val newBtn = BotButton(
                            text = newButtonText.trim(),
                            type = if (newButtonText.contains("MiniApp", ignoreCase = true) || newButtonText.contains("App", ignoreCase = true)) "webapp" else "callback",
                            payload = newButtonText.lowercase().replace(" ", "_")
                        )
                        val updatedList = buttons + newBtn
                        buttons = updatedList
                        newButtonText = ""
                        onUpdate(node.copy(buttons = updatedList))
                    }
                },
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Add")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onClose,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Done")
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun AddNodeDialog(
    onDismiss: () -> Unit,
    onAdd: (BotNodeType, String, String, List<BotButton>) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(BotNodeType.TRIGGER) }
    var content by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Node to Canvas") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Node Title (e.g. Help Command)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                Text("Choose Type:", style = MaterialTheme.typography.labelMedium)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        BotNodeType.TRIGGER,
                        BotNodeType.MESSAGE,
                        BotNodeType.INLINE_BUTTONS,
                        BotNodeType.WEBAPP_VIEW
                    ).forEach { type ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (selectedType == type) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    selectedType = type
                                    if (title.isBlank()) title = type.label
                                }
                        ) {
                            Column(
                                modifier = Modifier.padding(6.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(type.icon, fontSize = 16.sp)
                                Text(
                                    text = type.label.split(" ").first(),
                                    fontSize = 10.sp,
                                    color = if (selectedType == type) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text(if (selectedType == BotNodeType.TRIGGER) "Command (e.g. /help)" else "Message / Action text") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    shape = RoundedCornerShape(10.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        val buttons = if (selectedType == BotNodeType.INLINE_BUTTONS) {
                            listOf(BotButton(text = "🚀 Open Mini App", type = "webapp", payload = "open"))
                        } else emptyList()
                        onAdd(selectedType, title, content, buttons)
                    }
                },
                enabled = title.isNotBlank()
            ) {
                Text("Add to Canvas")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun HtmlEditorContent(
    initialHtml: String,
    onSave: (String) -> Unit,
    onClose: () -> Unit
) {
    var htmlText by remember { mutableStateOf(initialHtml) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("MiniApp HTML Source", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text("Single-file HTML/CSS/JS loaded in Telegram Mini App", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            IconButton(onClick = onClose) {
                Icon(Icons.Default.Edit, contentDescription = "Close")
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = htmlText,
            onValueChange = { htmlText = it },
            modifier = Modifier
                .fillMaxWidth()
                .height(380.dp),
            shape = RoundedCornerShape(12.dp),
            textStyle = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace, fontSize = 11.sp)
        )

        Spacer(modifier = Modifier.height(14.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedButton(
                onClick = onClose,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Cancel")
            }
            Button(
                onClick = { onSave(htmlText) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Save HTML")
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}
