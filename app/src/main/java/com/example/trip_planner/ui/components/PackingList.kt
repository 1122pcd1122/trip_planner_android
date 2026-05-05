package com.example.trip_planner.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.trip_planner.data.local.entity.PackingItemEntity
import com.example.trip_planner.ui.theme.AppColors

/**
 * 打包清单组件（用户自定义分类/标签）
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PackingListSection(
    items: List<PackingItemEntity>,
    packedCount: Int,
    totalCount: Int,
    onAddItem: (String, String, String, Int) -> Unit,
    onTogglePacked: (Long, Boolean) -> Unit,
    onDeleteItem: (Long) -> Unit,
    onAddDefaults: () -> Unit,
    onResetAll: () -> Unit = {},
    onSaveTemplate: (String) -> Unit = {},
    onLoadTemplate: (Long) -> Unit = {},
    appColors: AppColors,
    modifier: Modifier = Modifier
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var showTemplateDialog by remember { mutableStateOf(false) }
    var showSaveTemplateDialog by remember { mutableStateOf(false) }
    val progress = if (totalCount > 0) packedCount.toFloat() / totalCount else 0f
    
    val groupedItems = remember(items) {
        items.groupBy { it.category }
    }
    
    val expandedCategories = remember { mutableStateMapOf<String, Boolean>() }
    
    items.map { it.category }.distinct().forEach { category ->
        if (!expandedCategories.containsKey(category)) {
            expandedCategories[category] = true
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "打包清单 ($packedCount/$totalCount)",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = appColors.textSecondary
            )
            Row {
                if (packedCount > 0) {
                    IconButton(onClick = onResetAll) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = "重置",
                            tint = appColors.brandTeal,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                if (totalCount > 0) {
                    IconButton(onClick = {
                        items.forEach { item ->
                            if (!item.isPacked) {
                                onTogglePacked(item.id, true)
                            }
                        }
                    }) {
                        Icon(
                            Icons.Default.DoneAll,
                            contentDescription = "一键检查",
                            tint = appColors.brandTeal,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                if (totalCount > 0) {
                    IconButton(onClick = { showTemplateDialog = true }) {
                        Icon(
                            Icons.Default.Folder,
                            contentDescription = "模板",
                            tint = appColors.brandTeal,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                if (totalCount == 0) {
                    TextButton(onClick = onAddDefaults) {
                        Text("默认", color = appColors.brandTeal, fontSize = 11.sp)
                    }
                }
                IconButton(onClick = { showAddDialog = true }) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "添加物品",
                        tint = appColors.brandTeal,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        if (totalCount > 0) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                LinearProgressIndicator(
                    progress = progress,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(MaterialTheme.shapes.extraSmall),
                    color = if (progress >= 1.0f) appColors.success else appColors.brandTeal,
                    trackColor = appColors.divider.copy(alpha = 0.3f)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        if (items.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(MaterialTheme.shapes.small)
                    .background(appColors.cardBackground),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🎒", fontSize = 32.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "暂无物品",
                        fontSize = 14.sp,
                        color = appColors.textSecondary
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    TextButton(onClick = onAddDefaults) {
                        Text("添加默认", color = appColors.brandTeal, fontSize = 12.sp)
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 400.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                groupedItems.forEach { (category, categoryItems) ->
                    item {
                        AnimatedVisibility(
                            visible = true,
                            enter = slideInVertically(animationSpec = tween(300)) { it / 3 } + fadeIn(animationSpec = tween(300)),
                            label = "CategoryAnimation"
                        ) {
                            CategorySection(
                                category = category,
                                items = categoryItems,
                                isExpanded = expandedCategories[category] ?: true,
                                onToggleExpand = {
                                    expandedCategories[category] = !(expandedCategories[category] ?: true)
                                },
                                onTogglePacked = onTogglePacked,
                                onDeleteItem = onDeleteItem,
                                appColors = appColors
                            )
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddPackingItemDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { name, category, tags, quantity ->
                onAddItem(name, category, tags, quantity)
                showAddDialog = false
            },
            existingCategories = items.map { it.category }.distinct(),
            appColors = appColors
        )
    }

    if (showTemplateDialog) {
        TemplateListDialog(
            onDismiss = { showTemplateDialog = false },
            onLoadTemplate = { templateId ->
                onLoadTemplate(templateId)
                showTemplateDialog = false
            },
            onSaveTemplate = {
                showTemplateDialog = false
                showSaveTemplateDialog = true
            },
            appColors = appColors
        )
    }

    if (showSaveTemplateDialog) {
        SaveTemplateDialog(
            onDismiss = { showSaveTemplateDialog = false },
            onSave = { name ->
                onSaveTemplate(name)
                showSaveTemplateDialog = false
            },
            appColors = appColors
        )
    }
}

/**
 * 分类折叠区域
 */
@Composable
fun CategorySection(
    category: String,
    items: List<PackingItemEntity>,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onTogglePacked: (Long, Boolean) -> Unit,
    onDeleteItem: (Long) -> Unit,
    appColors: AppColors
) {
    val packedInCategory = items.count { it.isPacked }
    val totalInCategory = items.size

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(appColors.cardBackground)
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    category,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = appColors.textPrimary
                )
                Text(
                    "($packedInCategory/$totalInCategory)",
                    fontSize = 10.sp,
                    color = appColors.textSecondary
                )
            }
            IconButton(
                onClick = onToggleExpand,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (isExpanded) "收起" else "展开",
                    tint = appColors.textSecondary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        AnimatedVisibility(
            visible = isExpanded,
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            Column(
                modifier = Modifier.padding(start = 16.dp, top = 4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items.forEach { item ->
                    PackingItemRow(
                        item = item,
                        onToggle = { onTogglePacked(item.id, !item.isPacked) },
                        onDelete = { onDeleteItem(item.id) },
                        appColors = appColors
                    )
                }
            }
        }
    }
}

/**
 * 打包物品行
 */
@Composable
fun PackingItemRow(
    item: PackingItemEntity,
    onToggle: () -> Unit,
    onDelete: () -> Unit,
    appColors: AppColors
) {
    val tags = remember(item.tags) {
        if (item.tags.isNotEmpty()) {
            item.tags.split("|").filter { it.isNotEmpty() }
        } else {
            emptyList()
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(appColors.cardBackground)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = item.isPacked,
                onCheckedChange = { onToggle() },
                colors = CheckboxDefaults.colors(
                    checkedColor = appColors.brandTeal,
                    uncheckedColor = appColors.textSecondary
                )
            )
            Column {
                Text(
                    item.name,
                    fontSize = 12.sp,
                    color = appColors.textPrimary,
                    textDecoration = if (item.isPacked) TextDecoration.LineThrough else null
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (item.quantity > 1) {
                        Text(
                            "x${item.quantity}",
                            fontSize = 10.sp,
                            color = appColors.textSecondary
                        )
                    }
                    if (tags.isNotEmpty()) {
                        tags.forEach { tag ->
                            TagChip(tag = tag, appColors = appColors)
                        }
                    }
                }
            }
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

@Composable
fun TagChip(
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

/**
 * 添加物品对话框（用户自定义分类/标签）
 */
@Composable
fun AddPackingItemDialog(
    onDismiss: () -> Unit,
    onAdd: (String, String, String, Int) -> Unit,
    existingCategories: List<String>,
    appColors: AppColors
) {
    var name by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var newCategory by remember { mutableStateOf("") }
    var tags by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("1") }
    var useNewCategory by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("添加物品", fontSize = 16.sp) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("物品名称", fontSize = 12.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = appColors.brandTeal,
                        unfocusedBorderColor = appColors.softBackground
                    ),
                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 13.sp)
                )

                OutlinedTextField(
                    value = quantity,
                    onValueChange = { quantity = it.filter { c -> c.isDigit() } },
                    label = { Text("数量", fontSize = 12.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = appColors.brandTeal,
                        unfocusedBorderColor = appColors.softBackground
                    ),
                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 13.sp)
                )

                Text("分类", fontSize = 12.sp, color = appColors.textSecondary)
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { useNewCategory = false },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (!useNewCategory) appColors.brandTeal.copy(alpha = 0.1f) else appColors.cardBackground
                        )
                    ) {
                        Text("选择已有", fontSize = 11.sp)
                    }
                    OutlinedButton(
                        onClick = { useNewCategory = true },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (useNewCategory) appColors.brandTeal.copy(alpha = 0.1f) else appColors.cardBackground
                        )
                    ) {
                        Text("新建分类", fontSize = 11.sp)
                    }
                }

                if (useNewCategory) {
                    OutlinedTextField(
                        value = newCategory,
                        onValueChange = { newCategory = it },
                        label = { Text("新分类名称", fontSize = 12.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = appColors.brandTeal,
                            unfocusedBorderColor = appColors.softBackground
                        ),
                        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 13.sp)
                    )
                } else if (existingCategories.isNotEmpty()) {
                    val categoryScrollState = rememberScrollState()
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(categoryScrollState),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        existingCategories.forEach { cat ->
                            FilterChip(
                                selected = category == cat,
                                onClick = { category = cat },
                                label = { Text(cat, fontSize = 10.sp) }
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
                    placeholder = { Text("例如：重要|易忘|季节性", fontSize = 11.sp, color = appColors.textSecondary) }
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val qty = quantity.toIntOrNull() ?: 1
                    val finalCategory = if (useNewCategory) newCategory else category
                    if (name.isNotEmpty() && finalCategory.isNotEmpty()) {
                        onAdd(name, finalCategory, tags, qty)
                    }
                },
                enabled = name.isNotEmpty() && (if (useNewCategory) newCategory.isNotEmpty() else category.isNotEmpty())
            ) {
                Text("添加")
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
fun TemplateListDialog(
    onDismiss: () -> Unit,
    onLoadTemplate: (Long) -> Unit,
    onSaveTemplate: () -> Unit,
    appColors: AppColors
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("模板管理", fontSize = 16.sp) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onSaveTemplate,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = appColors.brandTeal.copy(alpha = 0.1f)
                    )
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("保存当前清单为模板", fontSize = 12.sp)
                }
                Divider(color = appColors.divider)
                Text("已有模板", fontSize = 12.sp, color = appColors.textSecondary)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                ) {
                    Text("暂无模板", fontSize = 12.sp, color = appColors.textSecondary, modifier = Modifier.align(Alignment.Center))
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("关闭")
            }
        },
        containerColor = appColors.cardBackground
    )
}

@Composable
fun SaveTemplateDialog(
    onDismiss: () -> Unit,
    onSave: (String) -> Unit,
    appColors: AppColors
) {
    var templateName by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("保存模板", fontSize = 16.sp) },
        text = {
            OutlinedTextField(
                value = templateName,
                onValueChange = { templateName = it },
                label = { Text("模板名称", fontSize = 12.sp) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = appColors.brandTeal,
                    unfocusedBorderColor = appColors.divider
                ),
                textStyle = androidx.compose.ui.text.TextStyle(fontSize = 13.sp)
            )
        },
        confirmButton = {
            TextButton(
                onClick = { if (templateName.isNotEmpty()) onSave(templateName) },
                enabled = templateName.isNotEmpty()
            ) {
                Text("保存")
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
