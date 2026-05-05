package com.example.trip_planner.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.trip_planner.ui.screens.PoiModel

/**
 * 排序方式枚举
 */
enum class SortType(val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    DEFAULT("默认排序", Icons.AutoMirrored.Filled.List),
    RATING_DESC("评分最高", Icons.Default.Star),
    RATING_ASC("评分最低", Icons.Default.StarBorder),
    NAME_ASC("名称A-Z", Icons.Default.SortByAlpha),
    NAME_DESC("名称Z-A", Icons.AutoMirrored.Filled.Sort)
}

/**
 * Agent筛选和排序工具栏
 */
@Composable
fun AgentFilterToolbar(
    poiList: List<PoiModel>,
    onFilteredListChanged: (List<PoiModel>) -> Unit,
    appColors: com.example.trip_planner.ui.theme.AppColors
) {
    var showFilterDialog by remember { mutableStateOf(false) }
    var selectedSortType by remember { mutableStateOf(SortType.DEFAULT) }
    var searchQuery by remember { mutableStateOf("") }
    var minRating by remember { mutableStateOf(0f) }
    var showFilterOptions by remember { mutableStateOf(false) }

    val filteredList = remember(poiList, selectedSortType, searchQuery, minRating) {
        var list = poiList.toMutableList()

        // 搜索过滤
        if (searchQuery.isNotBlank()) {
            list = list.filter { poi ->
                poi.name.contains(searchQuery, ignoreCase = true) ||
                poi.desc.contains(searchQuery, ignoreCase = true)
            }.toMutableList()
        }

        // 评分过滤
        if (minRating > 0) {
            list = list.filter { poi ->
                val rating = poi.rating.toFloatOrNull() ?: 0f
                rating >= minRating
            }.toMutableList()
        }

        // 排序
        list = when (selectedSortType) {
            SortType.RATING_DESC -> list.sortedByDescending { it.rating.toFloatOrNull() ?: 0f }
            SortType.RATING_ASC -> list.sortedBy { it.rating.toFloatOrNull() ?: 0f }
            SortType.NAME_ASC -> list.sortedBy { it.name }
            SortType.NAME_DESC -> list.sortedByDescending { it.name }
            SortType.DEFAULT -> list
        }.toMutableList()

        list
    }

    LaunchedEffect(filteredList) {
        onFilteredListChanged(filteredList)
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 搜索框
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("搜索...", fontSize = 13.sp, color = appColors.textSecondary) },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null, tint = appColors.textSecondary, modifier = Modifier.size(16.dp))
                },
                shape = MaterialTheme.shapes.small,
                modifier = Modifier.weight(1f),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = appColors.textSecondary.copy(alpha = 0.3f),
                    unfocusedBorderColor = appColors.textSecondary.copy(alpha = 0.15f),
                    focusedContainerColor = appColors.softBackground,
                    unfocusedContainerColor = appColors.softBackground
                ),
                singleLine = true,
                textStyle = androidx.compose.ui.text.TextStyle(fontSize = 13.sp)
            )

            // 筛选按钮
            IconButton(
                onClick = { showFilterOptions = !showFilterOptions },
                modifier = Modifier
                    .size(36.dp)
            ) {
                Icon(
                    Icons.Default.FilterList,
                    contentDescription = "筛选",
                    tint = if (minRating > 0) appColors.brandTeal else appColors.textSecondary,
                    modifier = Modifier.size(18.dp)
                )
            }

            // 排序按钮
            IconButton(
                onClick = { showFilterDialog = true },
                modifier = Modifier
                    .size(36.dp)
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.Sort,
                    contentDescription = "排序",
                    tint = appColors.textSecondary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        // 筛选选项展开
        if (showFilterOptions) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("最低评分筛选", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = appColors.textSecondary)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(0f, 3.0f, 3.5f, 4.0f, 4.5f).forEach { rating ->
                        FilterChip(
                            selected = minRating == rating,
                            onClick = { minRating = rating },
                            label = { Text(if (rating == 0f) "全部" else "$rating+", fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = appColors.brandTeal.copy(alpha = 0.12f),
                                selectedLabelColor = appColors.brandTeal
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = minRating == rating,
                                borderColor = if (minRating == rating) appColors.brandTeal else appColors.textSecondary.copy(alpha = 0.15f)
                            )
                        )
                    }
                }
            }
        }

        // 结果显示
        if (searchQuery.isNotBlank() || minRating > 0 || selectedSortType != SortType.DEFAULT) {
            Text(
                "找到 ${filteredList.size} 个结果",
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                fontSize = 12.sp,
                color = appColors.textSecondary
            )
        }
    }

    // 排序对话框
    if (showFilterDialog) {
        AlertDialog(
            onDismissRequest = { showFilterDialog = false },
            title = { Text("排序方式", fontSize = 15.sp, fontWeight = FontWeight.Medium) },
            text = {
                Column {
                    SortType.entries.forEach { sortType ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedSortType = sortType
                                    showFilterDialog = false
                                }
                                .padding(vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                sortType.icon,
                                contentDescription = null,
                                tint = if (selectedSortType == sortType) appColors.brandTeal else appColors.textSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                sortType.label,
                                fontSize = 14.sp,
                                color = if (selectedSortType == sortType) appColors.brandTeal else Color.Unspecified,
                                fontWeight = if (selectedSortType == sortType) FontWeight.Medium else FontWeight.Normal
                            )
                            if (selectedSortType == sortType) {
                                Spacer(modifier = Modifier.weight(1f))
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = null,
                                    tint = appColors.brandTeal,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showFilterDialog = false }) {
                    Text("关闭")
                }
            },
            containerColor = appColors.cardBackground
        )
    }
}
