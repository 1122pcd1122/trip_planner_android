package com.example.trip_planner.ui.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.*
import androidx.compose.runtime.derivedStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.trip_planner.data.local.entity.FavoriteEntity
import com.example.trip_planner.data.local.entity.FavoriteType
import com.example.trip_planner.utils.SearchUtils
import com.example.trip_planner.viewModel.FavoriteViewModel
import com.example.trip_planner.ui.screens.DetailType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * 收藏筛选类型枚举
 */
enum class FilterType(val displayName: String, val favoriteType: FavoriteType?) {
    ALL("全部", null),
    TRIP_PLAN("行程规划", FavoriteType.TRIP_PLAN),
    HOTEL("酒店", FavoriteType.HOTEL),
    ATTRACTION("景点", FavoriteType.ATTRACTION),
    RESTAURANT("餐厅", FavoriteType.RESTAURANT)
}

/**
 * 收藏页面（极简现代风）
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun FavoriteScreen(
    modifier: Modifier = Modifier,
    favoriteViewModel: FavoriteViewModel = viewModel(),
    onNavigateToDetail: (DetailType) -> Unit = {}
) {
    val allFavorites by favoriteViewModel.allFavorites.collectAsState()
    var selectedFilter by remember { mutableStateOf(FilterType.ALL) }
    var searchQuery by remember { mutableStateOf("") }
    val appColors = com.example.trip_planner.ui.theme.LocalAppColors.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var isSelectionMode by remember { mutableStateOf(false) }
    val selectedItems = remember { mutableStateListOf<String>() }

    val filteredFavorites by remember {
        derivedStateOf {
            var filtered = when (selectedFilter) {
                FilterType.ALL -> allFavorites
                else -> allFavorites.filter { it.type == selectedFilter.favoriteType?.name }
            }
            
            if (searchQuery.isNotBlank()) {
                filtered = SearchUtils.filterAndSort(
                    items = filtered,
                    query = searchQuery,
                    getText = { "${it.name} ${it.address} ${it.description}" }
                )
            }
            
            filtered
        }
    }

    val filterCounts by remember {
        derivedStateOf {
            FilterType.entries.associateWith { filter ->
                when (filter) {
                    FilterType.ALL -> allFavorites.size
                    else -> allFavorites.count { it.type == filter.favoriteType?.name }
                }
            }
        }
    }

    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            if (isSelectionMode) {
                TopAppBar(
                    title = { Text("已选择 ${selectedItems.size} 项", fontSize = 16.sp) },
                    actions = {
                        TextButton(onClick = {
                            scope.launch {
                                val count = selectedItems.size
                                selectedItems.forEach { favoriteViewModel.removeFavorite(it) }
                                selectedItems.clear()
                                isSelectionMode = false
                                snackbarHostState.showSnackbar("已删除 $count 个收藏")
                            }
                        }) {
                            Icon(Icons.Default.Delete, contentDescription = null, tint = appColors.error)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("删除", color = appColors.error)
                        }
                        TextButton(onClick = {
                            selectedItems.clear()
                            isSelectionMode = false
                        }) {
                            Text("取消", color = appColors.brandTeal)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = appColors.cardBackground)
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(appColors.softBackground)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text("搜索收藏", color = appColors.textSecondary, fontSize = 13.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = appColors.textSecondary, modifier = Modifier.size(18.dp)) },
                singleLine = true,
                shape = MaterialTheme.shapes.small,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = appColors.divider,
                    unfocusedBorderColor = appColors.divider.copy(alpha = 0.5f),
                    focusedContainerColor = appColors.cardBackground,
                    unfocusedContainerColor = appColors.cardBackground
                )
            )

            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(FilterType.entries) { filter ->
                    val count = filterCounts[filter] ?: 0
                    FilterChip(
                        selected = selectedFilter == filter,
                        onClick = { selectedFilter = filter },
                        label = {
                            Text("${filter.displayName} ($count)", fontSize = 13.sp)
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = appColors.brandTeal,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            if (filteredFavorites.isEmpty()) {
                EmptyFavoriteState(appColors = appColors)
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .animateContentSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    items(filteredFavorites, key = { it.id }) { favorite ->
                        SwipeableFavoriteCard(
                            favorite = favorite,
                            isSelected = isSelectionMode && selectedItems.contains(favorite.itemId),
                            onDelete = { favoriteViewModel.removeFavorite(favorite.itemId) },
                            onLongClick = {
                                isSelectionMode = true
                                selectedItems.add(favorite.itemId)
                            },
                            onClick = {
                                if (isSelectionMode) {
                                    if (selectedItems.contains(favorite.itemId)) {
                                        selectedItems.remove(favorite.itemId)
                                    } else {
                                        selectedItems.add(favorite.itemId)
                                    }
                                } else {
                                    when (favorite.type) {
                                        FavoriteType.HOTEL.name -> {
                                            onNavigateToDetail(DetailType.HotelDetail(
                                                com.example.trip_planner.network.model.PlanHotel(
                                                    name = favorite.name,
                                                    address = favorite.address,
                                                    price = favorite.price,
                                                    advantage = favorite.description,
                                                    latitude = "",
                                                    longitude = ""
                                                )
                                            ))
                                        }
                                        FavoriteType.RESTAURANT.name -> {
                                            onNavigateToDetail(DetailType.RestaurantDetail(
                                                com.example.trip_planner.network.model.RestaurantInfoDto(
                                                    name = favorite.name,
                                                    latitude = "",
                                                    longitude = "",
                                                    address = favorite.address,
                                                    featureDish = "",
                                                    score = favorite.rating
                                                )
                                            ))
                                        }
                                        FavoriteType.ATTRACTION.name -> {
                                            onNavigateToDetail(DetailType.AttractionDetail(
                                                com.example.trip_planner.network.model.SpotInfo(
                                                    name = favorite.name,
                                                    latitude = "",
                                                    longitude = "",
                                                    address = favorite.address,
                                                    score = favorite.rating,
                                                    intro = favorite.description
                                                )
                                            ))
                                        }
                                    }
                                }
                            },
                            onNavigateToDetail = onNavigateToDetail,
                            appColors = appColors
                        )
                        HorizontalDivider(color = appColors.divider.copy(alpha = 0.3f))
                    }
                }
            }
        }
    }
}

/**
 * 收藏项卡片（极简现代风）
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FavoriteItemCard(
    favorite: FavoriteEntity,
    isSelected: Boolean = false,
    onDelete: () -> Unit,
    onLongClick: () -> Unit = {},
    onClick: () -> Unit = {},
    onNavigateToDetail: (DetailType) -> Unit = {},
    appColors: com.example.trip_planner.ui.theme.AppColors
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .background(
                if (isSelected) appColors.brandTeal.copy(alpha = 0.1f) else Color.Transparent
            )
            .padding(vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            if (isSelected) {
                Icon(
                    Icons.Default.Edit,
                    contentDescription = null,
                    tint = appColors.brandTeal,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    favorite.name,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    color = appColors.textPrimary
                )
                
                Spacer(modifier = Modifier.height(6.dp))

                if (favorite.type != FavoriteType.TRIP_PLAN.name) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        if (favorite.rating.isNotEmpty()) {
                            Text(
                                favorite.rating,
                                color = appColors.textSecondary,
                                fontSize = 13.sp
                            )
                        }
                        if (favorite.price.isNotEmpty()) {
                            Text(
                                favorite.price,
                                color = appColors.brandTeal,
                                fontSize = 13.sp
                            )
                        }
                    }
                    if (favorite.address.isNotEmpty()) {
                        Text(
                            favorite.address,
                            color = appColors.textSecondary,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                } else {
                    if (favorite.description.isNotEmpty()) {
                        Text(
                            favorite.description,
                            color = appColors.textSecondary,
                            fontSize = 13.sp,
                            lineHeight = 18.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
            TextButton(onClick = onDelete) {
                Text("删除", color = appColors.error.copy(alpha = 0.7f), fontSize = 12.sp)
            }
        }
    }
}

/**
 * 可滑动收藏卡片（极简现代风）
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeableFavoriteCard(
    favorite: FavoriteEntity,
    isSelected: Boolean = false,
    onDelete: () -> Unit,
    onLongClick: () -> Unit = {},
    onClick: () -> Unit = {},
    onNavigateToDetail: (DetailType) -> Unit = {},
    appColors: com.example.trip_planner.ui.theme.AppColors
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { dismissValue ->
            when (dismissValue) {
                SwipeToDismissBoxValue.EndToStart -> {
                    onDelete()
                    true
                }
                else -> false
            }
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            val direction = dismissState.dismissDirection
            val color = when {
                direction == SwipeToDismissBoxValue.EndToStart -> appColors.error.copy(alpha = 0.15f)
                else -> Color.Transparent
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color)
                    .padding(horizontal = 20.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                if (direction == SwipeToDismissBoxValue.EndToStart) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null,
                        tint = appColors.error,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        },
        content = {
            FavoriteItemCard(
                favorite = favorite,
                isSelected = isSelected,
                onDelete = onDelete,
                onLongClick = onLongClick,
                onClick = onClick,
                onNavigateToDetail = onNavigateToDetail,
                appColors = appColors
            )
        }
    )
}

/**
 * 空收藏状态（极简现代风）
 */
@Composable
fun EmptyFavoriteState(
    appColors: com.example.trip_planner.ui.theme.AppColors,
    onNavigateToPlan: (() -> Unit)? = null
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                "还没有收藏哦",
                color = appColors.textSecondary,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "快去收藏喜欢的行程、酒店、景点吧",
                color = appColors.textSecondary,
                fontSize = 12.sp
            )
            if (onNavigateToPlan != null) {
                Spacer(modifier = Modifier.height(16.dp))
                TextButton(onClick = onNavigateToPlan) {
                    Text(
                        "去规划行程",
                        color = appColors.brandTeal,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}
