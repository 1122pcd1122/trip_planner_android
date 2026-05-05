package com.example.trip_planner.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.animation.AnimatedVisibility
import coil.compose.AsyncImage
import dev.jeziellago.compose.markdowntext.MarkdownText
import com.example.trip_planner.data.local.entity.TripNoteEntity
import com.example.trip_planner.data.local.entity.TravelMood
import com.example.trip_planner.ui.theme.AppColors
import java.text.SimpleDateFormat
import java.util.*

/**
 * 旅行笔记组件（Markdown 编辑器风格）
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripNotesSection(
    notes: List<TripNoteEntity>,
    onNavigateToAdd: () -> Unit,
    onNavigateToEdit: (TripNoteEntity) -> Unit,
    onDeleteNote: (Long) -> Unit,
    @Suppress("UNUSED_PARAMETER") onUpdateNote: (TripNoteEntity) -> Unit,
    appColors: AppColors,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedDateFilter by remember { mutableStateOf<String?>(null) }
    var sortOrder by remember { mutableStateOf("date") }

    val filteredNotes = remember(notes, searchQuery, selectedDateFilter) {
        var filtered = notes
        if (selectedDateFilter != null) {
            filtered = filtered.filter { it.date == selectedDateFilter }
        }
        if (searchQuery.isNotEmpty()) {
            val query = searchQuery.lowercase()
            filtered = filtered.filter { note ->
                note.title.lowercase().contains(query) ||
                note.content.lowercase().contains(query) ||
                note.location.lowercase().contains(query) ||
                note.tags.lowercase().contains(query)
            }
        }
        filtered
    }

    val sortedNotes = remember(filteredNotes, sortOrder) {
        when (sortOrder) {
            "date" -> filteredNotes.sortedByDescending { it.timestamp }
            "mood" -> filteredNotes.sortedBy { it.mood }
            "location" -> filteredNotes.sortedBy { it.location }
            else -> filteredNotes
        }
    }

    val groupedNotes = remember(sortedNotes, selectedDateFilter) {
        sortedNotes.groupBy { it.date }
            .toSortedMap { d1, d2 -> 
                if (d1.isEmpty() && d2.isEmpty()) 0
                else if (d1.isEmpty()) 1
                else if (d2.isEmpty()) -1
                else d2.compareTo(d1)
            }
    }

    val uniqueDates = remember(notes) {
        notes.map { it.date }.filter { it.isNotEmpty() }.distinct().sortedDescending()
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize()
    ) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            placeholder = { Text("搜索笔记...", fontSize = 12.sp) },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp))
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Close, contentDescription = "清除", modifier = Modifier.size(18.dp))
                    }
                }
            },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = appColors.brandTeal,
                unfocusedBorderColor = appColors.divider
            ),
            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "旅行笔记 (${filteredNotes.size})",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = appColors.textSecondary
            )
            Row {
                IconButton(onClick = {
                    sortOrder = when (sortOrder) {
                        "date" -> "mood"
                        "mood" -> "location"
                        else -> "date"
                    }
                }) {
                    Icon(
                        Icons.AutoMirrored.Filled.Sort,
                        contentDescription = "排序",
                        tint = appColors.brandTeal,
                        modifier = Modifier.size(20.dp)
                    )
                }
                IconButton(onClick = { selectedDateFilter = null }) {
                    Icon(
                        if (selectedDateFilter == null) Icons.Default.FilterAlt else Icons.Default.FilterAltOff,
                        contentDescription = "筛选",
                        tint = if (selectedDateFilter == null) appColors.brandTeal else appColors.textSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                IconButton(onClick = { onNavigateToAdd() }) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "添加笔记",
                        tint = appColors.brandTeal,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        if (uniqueDates.isNotEmpty()) {
            val dateScrollState = rememberScrollState()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(dateScrollState)
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedDateFilter == null,
                    onClick = { selectedDateFilter = null },
                    label = { Text("全部", fontSize = 10.sp) },
                    leadingIcon = if (selectedDateFilter == null) {
                        { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                    } else null
                )
                uniqueDates.forEach { date ->
                    val displayDate = if (date.isNotEmpty()) {
                        try {
                            val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                            val outputFormat = SimpleDateFormat("MM-dd", Locale.getDefault())
                            val parsed = inputFormat.parse(date)
                            if (parsed != null) outputFormat.format(parsed) else date
                        } catch (e: Exception) {
                            date
                        }
                    } else {
                        "未标注"
                    }
                    FilterChip(
                        selected = selectedDateFilter == date,
                        onClick = { selectedDateFilter = if (selectedDateFilter == date) null else date },
                        label = { Text(displayDate, fontSize = 10.sp) },
                        leadingIcon = if (selectedDateFilter == date) {
                            { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                        } else null
                    )
                }
            }
        }

        if (notes.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(MaterialTheme.shapes.small)
                    .background(appColors.cardBackground),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("📝", fontSize = 32.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "暂无笔记",
                        fontSize = 14.sp,
                        color = appColors.textSecondary
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    TextButton(onClick = onNavigateToAdd) {
                        Text("记录第一笔", color = appColors.brandTeal, fontSize = 12.sp)
                    }
                }
            }
        } else if (groupedNotes.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("该日期暂无笔记", fontSize = 12.sp, color = appColors.textSecondary)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 400.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                groupedNotes.forEach { (date, dateNotes) ->
                    if (date.isNotEmpty()) {
                        item {
                            DateHeader(date = date, appColors = appColors)
                        }
                    }
                    items(dateNotes, key = { it.id }) { note ->
                        AnimatedVisibility(
                            visible = true,
                            enter = slideInVertically(animationSpec = tween(300)) { it / 2 } + fadeIn(animationSpec = tween(300)),
                            label = "NoteItemAnimation"
                        ) {
                            NoteCard(
                                note = note,
                                onDelete = { onDeleteNote(note.id) },
                                onEdit = { onNavigateToEdit(note) },
                                appColors = appColors
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * 日期分组头部
 */
@Composable
fun DateHeader(
    date: String,
    appColors: AppColors
) {
    val displayDate = try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val outputFormat = SimpleDateFormat("yyyy年MM月dd日", Locale.getDefault())
        val parsed = inputFormat.parse(date)
        if (parsed != null) outputFormat.format(parsed) else date
    } catch (e: Exception) {
        date
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(appColors.brandTeal)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            displayDate,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = appColors.textSecondary
        )
    }
}

/**
 * 笔记卡片（Markdown 预览风格）
 */
@Composable
fun NoteCard(
    note: TripNoteEntity,
    onDelete: () -> Unit,
    onEdit: () -> Unit,
    appColors: AppColors
) {
    val dateFormat = SimpleDateFormat("MM-dd HH:mm", Locale.getDefault())
    @Suppress("UNUSED_VARIABLE") val timeStr = dateFormat.format(Date(note.timestamp))
    val moodEmoji = TravelMood.entries.find { it.label == note.mood }?.emoji ?: ""
    val photoList = remember(note.photoPaths) {
        if (note.photoPaths.isNotEmpty()) {
            note.photoPaths.split("|").filter { it.isNotEmpty() }
        } else {
            emptyList()
        }
    }
    val tags = remember(note.tags) {
        if (note.tags.isNotEmpty()) {
            note.tags.split("|").filter { it.isNotEmpty() }
        } else {
            emptyList()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(appColors.cardBackground)
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (moodEmoji.isNotEmpty()) {
                    Text(moodEmoji, fontSize = 14.sp)
                }
                Text(
                    note.title.ifEmpty { "无标题" },
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = appColors.textPrimary
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onEdit, modifier = Modifier.size(28.dp)) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "编辑",
                        tint = appColors.brandTeal.copy(alpha = 0.6f),
                        modifier = Modifier.size(14.dp)
                    )
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "删除",
                        tint = appColors.error.copy(alpha = 0.5f),
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }

        if (note.location.isNotEmpty()) {
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = appColors.brandTeal,
                    modifier = Modifier.size(12.dp)
                )
                Text(note.location, fontSize = 10.sp, color = appColors.textSecondary)
            }
        }

        if (photoList.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                photoList.take(3).forEach { path ->
                    AsyncImage(
                        model = path,
                        contentDescription = null,
                        modifier = Modifier
                            .weight(1f)
                            .height(80.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(appColors.softBackground),
                        contentScale = ContentScale.Crop
                    )
                }
                if (photoList.size > 3) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(80.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(appColors.softBackground.copy(alpha = 0.5f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "+${photoList.size - 3}",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = appColors.textSecondary
                        )
                    }
                }
            }
        }

        if (note.content.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            MarkdownText(
                markdown = note.content,
                style = androidx.compose.ui.text.TextStyle(
                    fontSize = 11.sp,
                    color = appColors.textSecondary,
                    lineHeight = 16.sp
                ),
                maxLines = 6
            )
        }

        if (tags.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                tags.forEach { tag ->
                    NoteTagChip(tag = tag, appColors = appColors)
                }
            }
        }
    }
}

/**
 * Markdown 笔记编辑器对话框
 */
@Composable
fun MarkdownNoteDialog(
    initialNote: TripNoteEntity? = null,
    onDismiss: () -> Unit,
    onAdd: ((String, String, String, String, String, String, String) -> Unit)? = null,
    onUpdate: ((TripNoteEntity) -> Unit)? = null,
    appColors: AppColors
) {
    var title by remember { mutableStateOf(initialNote?.title ?: "") }
    var content by remember { mutableStateOf(initialNote?.content ?: "") }
    var location by remember { mutableStateOf(initialNote?.location ?: "") }
    var selectedMood by remember { mutableStateOf(
        TravelMood.entries.find { it.label == initialNote?.mood } ?: TravelMood.NORMAL
    ) }
    var selectedDate by remember { mutableStateOf(initialNote?.date ?: "") }
    var tags by remember { mutableStateOf(initialNote?.tags ?: "") }
    val selectedPhotos = remember { 
        mutableStateListOf<String>().apply {
            initialNote?.photoPaths?.let { paths ->
                if (paths.isNotEmpty()) {
                    addAll(paths.split("|").filter { it.isNotEmpty() })
                }
            }
        }
    }
    var showPreview by remember { mutableStateOf(false) }

    @Suppress("UNUSED_VARIABLE") val context = LocalContext.current
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 5),
        onResult = { uris ->
            selectedPhotos.clear()
            selectedPhotos.addAll(uris.map { it.toString() })
        }
    )

    val today = remember {
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }

    val isEditing = initialNote != null

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isEditing) "编辑笔记" else "新建笔记", fontSize = 16.sp) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("标题", fontSize = 12.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = appColors.brandTeal,
                        unfocusedBorderColor = appColors.softBackground
                    ),
                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 13.sp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("内容 (支持 Markdown)", fontSize = 12.sp, color = appColors.textSecondary)
                    Row {
                        TextButton(onClick = { showPreview = !showPreview }) {
                            Icon(
                                if (showPreview) Icons.Default.Edit else Icons.Default.Visibility,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (showPreview) "编辑" else "预览", fontSize = 11.sp)
                        }
                    }
                }

                if (showPreview) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(appColors.softBackground)
                            .padding(12.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        if (content.isEmpty()) {
                            Text("预览区域", fontSize = 11.sp, color = appColors.textSecondary)
                        } else {
                            MarkdownText(
                                markdown = content,
                                style = androidx.compose.ui.text.TextStyle(
                                    fontSize = 12.sp,
                                    color = appColors.textPrimary,
                                    lineHeight = 18.sp
                                )
                            )
                        }
                    }
                } else {
                    OutlinedTextField(
                        value = content,
                        onValueChange = { content = it },
                        label = { Text("Markdown 内容", fontSize = 12.sp) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = appColors.brandTeal,
                            unfocusedBorderColor = appColors.softBackground
                        ),
                        textStyle = androidx.compose.ui.text.TextStyle(
                            fontSize = 13.sp,
                            fontFamily = FontFamily.Monospace
                        ),
                        placeholder = {
                            Text(
                                "# 标题\n## 子标题\n**粗体** *斜体*\n- 列表项\n> 引用",
                                fontSize = 11.sp,
                                color = appColors.textSecondary,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        MarkdownToolbarButton(
                            icon = Icons.Default.FormatBold,
                            label = "B",
                            onClick = {
                                val selection = content
                                content = content.replace(selection, "**$selection**")
                            },
                            appColors = appColors
                        )
                        MarkdownToolbarButton(
                            icon = Icons.Default.FormatItalic,
                            label = "I",
                            onClick = {
                                val selection = content
                                content = content.replace(selection, "*$selection*")
                            },
                            appColors = appColors
                        )
                        MarkdownToolbarButton(
                            icon = Icons.Default.Title,
                            label = "H",
                            onClick = {
                                content += "\n# "
                            },
                            appColors = appColors
                        )
                        MarkdownToolbarButton(
                            icon = Icons.AutoMirrored.Filled.List,
                            label = "-",
                            onClick = {
                                content += "\n- "
                            },
                            appColors = appColors
                        )
                        MarkdownToolbarButton(
                            icon = Icons.Default.FormatQuote,
                            label = ">",
                            onClick = {
                                content += "\n> "
                            },
                            appColors = appColors
                        )
                    }
                }

                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("地点（可选）", fontSize = 12.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = appColors.brandTeal,
                        unfocusedBorderColor = appColors.softBackground
                    ),
                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 13.sp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = selectedDate,
                        onValueChange = { selectedDate = it },
                        label = { Text("日期（可选）", fontSize = 12.sp) },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = appColors.brandTeal,
                            unfocusedBorderColor = appColors.softBackground
                        ),
                        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 13.sp),
                        placeholder = { Text(today, fontSize = 11.sp, color = appColors.textSecondary) }
                    )

                    OutlinedButton(
                        onClick = { selectedDate = today },
                        modifier = Modifier.align(Alignment.CenterVertically)
                    ) {
                        Icon(Icons.Default.Today, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("今天", fontSize = 11.sp)
                    }
                }

                Text("心情", fontSize = 12.sp, color = appColors.textSecondary)
                val moodScrollState = rememberScrollState()
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(moodScrollState),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TravelMood.entries.forEach { mood ->
                        FilterChip(
                            selected = selectedMood == mood,
                            onClick = { selectedMood = mood },
                            label = { Text("${mood.emoji} ${mood.label}", fontSize = 10.sp) }
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("照片（最多5张）", fontSize = 12.sp, color = appColors.textSecondary)
                    TextButton(onClick = { photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }) {
                        Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("选择照片", fontSize = 11.sp)
                    }
                }

                if (selectedPhotos.isNotEmpty()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        selectedPhotos.forEach { path ->
                            AsyncImage(
                                model = path,
                                contentDescription = null,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(60.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(appColors.softBackground),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = tags,
                    onValueChange = { tags = it },
                    label = { Text("标签（用 | 分隔，可选）", fontSize = 12.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = appColors.brandTeal,
                        unfocusedBorderColor = appColors.softBackground
                    ),
                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 13.sp),
                    placeholder = { Text("例如：风景|美食|打卡", fontSize = 11.sp, color = appColors.textSecondary) }
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (isEditing && onUpdate != null && initialNote != null) {
                        onUpdate(
                            initialNote.copy(
                                title = title,
                                content = content,
                                date = selectedDate,
                                location = location,
                                mood = selectedMood.label,
                                photoPaths = selectedPhotos.joinToString("|"),
                                tags = tags,
                                timestamp = System.currentTimeMillis()
                            )
                        )
                    } else if (onAdd != null) {
                        onAdd(title, content, selectedDate, location, selectedMood.label, selectedPhotos.joinToString("|"), tags)
                    }
                },
                enabled = title.isNotEmpty() || content.isNotEmpty()
            ) {
                Text(if (isEditing) "更新" else "保存")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        },
        containerColor = appColors.cardBackground
    )
}

@Composable
fun NoteTagChip(
    tag: String,
    appColors: AppColors
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(appColors.brandTeal.copy(alpha = 0.15f))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(tag, fontSize = 9.sp, color = appColors.brandTeal)
    }
}

@Composable
fun MarkdownToolbarButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    label: String,
    onClick: () -> Unit,
    appColors: AppColors
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .border(1.dp, appColors.divider.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = label, tint = appColors.textSecondary, modifier = Modifier.size(14.dp))
        } else {
            Text(label, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = appColors.textSecondary)
        }
    }
}
