package com.example.trip_planner.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import dev.jeziellago.compose.markdowntext.MarkdownText
import com.example.trip_planner.data.local.entity.TripNoteEntity
import com.example.trip_planner.data.local.entity.TravelMood
import com.example.trip_planner.ui.theme.AppColors
import com.example.trip_planner.ui.theme.LocalAppColors
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteEditorScreen(
    tripId: String,
    userId: Long = 0,
    initialNote: TripNoteEntity? = null,
    onBack: () -> Unit,
    onSave: (String, String, String, String, String, String, String, Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val appColors = LocalAppColors.current

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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditing) "编辑笔记" else "新建笔记", fontSize = 16.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            onSave(
                                title,
                                content,
                                selectedDate,
                                location,
                                selectedMood.label,
                                selectedPhotos.joinToString("|"),
                                tags,
                                userId
                            )
                            onBack()
                        },
                        enabled = title.isNotEmpty() || content.isNotEmpty()
                    ) {
                        Text(if (isEditing) "更新" else "保存", fontSize = 14.sp)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = appColors.softBackground)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(appColors.softBackground)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("标题", fontSize = 12.sp) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = appColors.brandTeal,
                    unfocusedBorderColor = appColors.divider
                ),
                textStyle = androidx.compose.ui.text.TextStyle(fontSize = 14.sp)
            )

            MarkdownEditorSection(
                content = content,
                onContentChange = { content = it },
                showPreview = showPreview,
                onTogglePreview = { showPreview = !showPreview },
                appColors = appColors
            )

            OutlinedTextField(
                value = location,
                onValueChange = { location = it },
                label = { Text("地点（可选）", fontSize = 12.sp) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = appColors.brandTeal,
                    unfocusedBorderColor = appColors.divider
                ),
                textStyle = androidx.compose.ui.text.TextStyle(fontSize = 13.sp)
            )

            DateSelector(
                selectedDate = selectedDate,
                onDateChange = { selectedDate = it },
                today = today,
                appColors = appColors
            )

            MoodSelector(
                selectedMood = selectedMood,
                onMoodChange = { selectedMood = it },
                appColors = appColors
            )

            PhotoSelector(
                selectedPhotos = selectedPhotos,
                onPickPhotos = { photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                appColors = appColors
            )

            OutlinedTextField(
                value = tags,
                onValueChange = { tags = it },
                label = { Text("标签（用 | 分隔，可选）", fontSize = 12.sp) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = appColors.brandTeal,
                    unfocusedBorderColor = appColors.divider
                ),
                textStyle = androidx.compose.ui.text.TextStyle(fontSize = 13.sp),
                placeholder = { Text("例如：风景|美食|打卡", fontSize = 11.sp, color = appColors.textSecondary) }
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun MarkdownEditorSection(
    content: String,
    onContentChange: (String) -> Unit,
    showPreview: Boolean,
    onTogglePreview: () -> Unit,
    appColors: AppColors
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("内容 (支持 Markdown)", fontSize = 12.sp, color = appColors.textSecondary)
            TextButton(onClick = onTogglePreview) {
                Icon(
                    if (showPreview) Icons.Default.Edit else Icons.Default.Visibility,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(if (showPreview) "编辑" else "预览", fontSize = 11.sp)
            }
        }

        if (showPreview) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(appColors.cardBackground)
                    .padding(12.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                if (content.isEmpty()) {
                    Text("预览区域", fontSize = 11.sp, color = appColors.textSecondary)
                } else {
                    MarkdownText(
                        markdown = content,
                        fontSize = 12.sp,
                        color = appColors.textPrimary,
                        lineHeight = 18.sp
                    )
                }
            }
        } else {
            OutlinedTextField(
                value = content,
                onValueChange = onContentChange,
                label = { Text("Markdown 内容", fontSize = 12.sp) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = appColors.brandTeal,
                    unfocusedBorderColor = appColors.divider
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

            MarkdownToolbar(
                onInsert = { insertion ->
                    val currentContent = content
                    onContentChange(currentContent + insertion)
                },
                appColors = appColors
            )
        }
    }
}

@Composable
fun MarkdownToolbar(
    onInsert: (String) -> Unit,
    appColors: AppColors
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        MarkdownToolbarButton(
            label = "B",
            onClick = { onInsert("\n**粗体文本**") },
            appColors = appColors
        )
        MarkdownToolbarButton(
            label = "I",
            onClick = { onInsert("\n*斜体文本*") },
            appColors = appColors
        )
        MarkdownToolbarButton(
            label = "H",
            onClick = { onInsert("\n## 标题") },
            appColors = appColors
        )
        MarkdownToolbarButton(
            label = "-",
            onClick = { onInsert("\n- 列表项") },
            appColors = appColors
        )
        MarkdownToolbarButton(
            label = ">",
            onClick = { onInsert("\n> 引用") },
            appColors = appColors
        )
    }
}

@Composable
fun MarkdownToolbarButton(
    label: String,
    onClick: () -> Unit,
    appColors: AppColors
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .border(1.dp, appColors.divider.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(label, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = appColors.textSecondary)
    }
}

@Composable
fun DateSelector(
    selectedDate: String,
    onDateChange: (String) -> Unit,
    today: String,
    appColors: AppColors
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedTextField(
            value = selectedDate,
            onValueChange = onDateChange,
            label = { Text("日期（可选）", fontSize = 12.sp) },
            modifier = Modifier.weight(1f),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = appColors.brandTeal,
                unfocusedBorderColor = appColors.divider
            ),
            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 13.sp),
            placeholder = { Text(today, fontSize = 11.sp, color = appColors.textSecondary) }
        )

        OutlinedButton(
            onClick = { onDateChange(today) },
            modifier = Modifier.align(Alignment.CenterVertically)
        ) {
            Icon(Icons.Default.Today, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("今天", fontSize = 11.sp)
        }
    }
}

@Composable
fun MoodSelector(
    selectedMood: TravelMood,
    onMoodChange: (TravelMood) -> Unit,
    appColors: AppColors
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
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
                    onClick = { onMoodChange(mood) },
                    label = { Text("${mood.emoji} ${mood.label}", fontSize = 10.sp) }
                )
            }
        }
    }
}

@Composable
fun PhotoSelector(
    selectedPhotos: MutableList<String>,
    onPickPhotos: () -> Unit,
    appColors: AppColors
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("照片（最多5张）", fontSize = 12.sp, color = appColors.textSecondary)
            TextButton(onClick = onPickPhotos) {
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
                            .background(appColors.cardBackground),
                        contentScale = ContentScale.Crop
                    )
                }
            }
        }
    }
}
