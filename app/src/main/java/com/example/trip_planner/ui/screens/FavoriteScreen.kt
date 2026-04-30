package com.example.trip_planner.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.trip_planner.data.local.entity.FavoriteEntity
import com.example.trip_planner.data.local.entity.TripPlanEntity
import com.example.trip_planner.ui.content.BrandTeal
import com.example.trip_planner.ui.content.CardBackground
import com.example.trip_planner.ui.content.SoftBackground
import com.example.trip_planner.ui.content.TextSecondary
import com.example.trip_planner.viewModel.FavoriteViewModel
import com.example.trip_planner.viewModel.TripPlanViewModel

/**
 * 收藏类型枚举
 */
enum class FavoriteType(val title: String, val icon: String) {
    ALL("全部", "🎯"),
    PLANS("行程", "📋"),
    HOTEL("酒店", "🏨"),
    ATTRACTION("景点", "🏛️"),
    RESTAURANT("餐厅", "🍽️")
}

/**
 * 收藏页面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoriteScreen(
    modifier: Modifier = Modifier,
    favoriteViewModel: FavoriteViewModel = viewModel(),
    tripPlanViewModel: TripPlanViewModel = viewModel()
) {
    val allFavorites by favoriteViewModel.allFavorites.collectAsState()
    val filteredFavorites by favoriteViewModel.filteredFavorites.collectAsState()
    val selectedType by favoriteViewModel.selectedType.collectAsState()
    val allTripPlans by tripPlanViewModel.allTripPlans.collectAsState()

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("❤️", fontSize = 24.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("我的收藏", fontWeight = FontWeight.Bold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BrandTeal,
                    titleContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(SoftBackground)
        ) {
            // 分类筛选
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(FavoriteType.values().toList()) { type ->
                    val count = when (type) {
                        FavoriteType.ALL -> allFavorites.size + allTripPlans.size
                        FavoriteType.PLANS -> allTripPlans.size
                        else -> favoriteViewModel.getTypeCount(type.name)
                    }
                    FilterChip(
                        selected = selectedType == type.name || (type == FavoriteType.ALL && selectedType == null),
                        onClick = {
                            favoriteViewModel.selectType(if (type == FavoriteType.ALL) null else type.name)
                        },
                        label = {
                            Text("${type.icon} ${type.title} ($count)")
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = BrandTeal,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            // 内容列表
            when (selectedType) {
                "PLANS" -> {
                    // 行程规划列表
                    if (allTripPlans.isEmpty()) {
                        EmptyTripPlanState()
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(allTripPlans, key = { it.id }) { plan ->
                                TripPlanCard(
                                    tripPlan = plan,
                                    onDelete = { tripPlanViewModel.deleteTripPlan(plan.id) }
                                )
                            }
                        }
                    }
                }
                null, "ALL" -> {
                    // 全部内容
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        if (allTripPlans.isNotEmpty()) {
                            item {
                                Text("📋 行程规划", fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.padding(vertical = 8.dp))
                            }
                            items(allTripPlans, key = { it.id }) { plan ->
                                TripPlanCard(tripPlan = plan, onDelete = { tripPlanViewModel.deleteTripPlan(plan.id) })
                            }
                        }
                        if (filteredFavorites.isNotEmpty()) {
                            item {
                                Text("❤️ 单项收藏", fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.padding(vertical = 8.dp))
                            }
                            items(filteredFavorites, key = { it.id }) { favorite ->
                                FavoriteItemCard(favorite = favorite, onDelete = { favoriteViewModel.removeFavorite(favorite.itemId) })
                            }
                        }
                        if (allTripPlans.isEmpty() && filteredFavorites.isEmpty()) {
                            item { EmptyFavoriteState() }
                        }
                    }
                }
                else -> {
                    // 单项收藏
                    if (filteredFavorites.isEmpty()) {
                        EmptyFavoriteState()
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(filteredFavorites, key = { it.id }) { favorite ->
                                FavoriteItemCard(favorite = favorite, onDelete = { favoriteViewModel.removeFavorite(favorite.itemId) })
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * 收藏项卡片
 */
@Composable
fun FavoriteItemCard(
    favorite: FavoriteEntity,
    onDelete: () -> Unit
) {
    val typeIcon = when (favorite.type) {
        "HOTEL" -> "🏨"
        "RESTAURANT" -> "🍽️"
        "ATTRACTION" -> "🏛️"
        else -> "📍"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "$typeIcon ${favorite.name}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
                if (favorite.rating.isNotEmpty()) {
                    Text(
                        "⭐ ${favorite.rating}",
                        color = TextSecondary,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                if (favorite.price.isNotEmpty()) {
                    Text(
                        "💰 ${favorite.price}",
                        color = BrandTeal,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
                if (favorite.address.isNotEmpty()) {
                    Text(
                        "📍 ${favorite.address}",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "删除",
                    tint = Color.Red.copy(alpha = 0.7f)
                )
            }
        }
    }
}

/**
 * 空收藏状态
 */
@Composable
fun EmptyFavoriteState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("💔", fontSize = 48.sp)
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "还没有收藏哦",
                color = TextSecondary,
                fontSize = 14.sp
            )
            Text(
                "快去收藏喜欢的酒店、景点吧",
                color = TextSecondary,
                fontSize = 12.sp
            )
        }
    }
}

/**
 * 行程规划卡片
 */
@Composable
fun TripPlanCard(
    tripPlan: TripPlanEntity,
    onDelete: () -> Unit
) {
    val dateFormat = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault())
    val dateStr = dateFormat.format(java.util.Date(tripPlan.timestamp))

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "📋 ${tripPlan.destination} - ${tripPlan.days}天",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
                if (tripPlan.preferences.isNotEmpty()) {
                    Text(
                        "偏好: ${tripPlan.preferences}",
                        color = TextSecondary,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                Text(
                    "🕐 $dateStr",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "删除",
                    tint = Color.Red.copy(alpha = 0.7f)
                )
            }
        }
    }
}

/**
 * 空行程规划状态
 */
@Composable
fun EmptyTripPlanState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("📋", fontSize = 48.sp)
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "还没有行程规划哦",
                color = TextSecondary,
                fontSize = 14.sp
            )
            Text(
                "快去创建你的第一个旅行规划吧",
                color = TextSecondary,
                fontSize = 12.sp
            )
        }
    }
}
