package com.example.trip_planner.ui.components

import androidx.compose.animation.animateColor
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import com.example.trip_planner.data.local.entity.FavoriteEntity
import com.example.trip_planner.data.local.entity.FavoriteType
import com.example.trip_planner.ui.theme.AppColors
import com.example.trip_planner.viewModel.FavoriteViewModel

/**
 * 空状态提示组件（极简现代风）
 */
@Composable
fun EmptyState(
    message: String,
    actionText: String? = null,
    onAction: (() -> Unit)? = null,
    appColors: AppColors = com.example.trip_planner.ui.theme.LocalAppColors.current
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Text(
                message,
                color = appColors.textSecondary,
                fontSize = 15.sp,
                lineHeight = 22.sp
            )
            if (actionText != null && onAction != null) {
                Spacer(modifier = Modifier.height(16.dp))
                TextButton(onClick = onAction) {
                    Text(
                        actionText,
                        color = appColors.brandTeal,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

/**
 * 通用 POI 卡片组件（极简现代风）
 */
@Composable
fun PoiCard(
    name: String,
    rating: String = "",
    price: String = "",
    description: String,
    icon: String,
    favoriteId: String,
    favoriteType: FavoriteType,
    favoriteViewModel: FavoriteViewModel,
    onClick: () -> Unit = {},
    appColors: AppColors = com.example.trip_planner.ui.theme.LocalAppColors.current
) {
    val favorites by favoriteViewModel.allFavorites.collectAsState()
    val isFavorite = remember(favoriteId, favorites) {
        favorites.any { it.itemId == favoriteId }
    }
    var isPressed by remember { mutableStateOf(false) }
    val alpha by animateFloatAsState(
        targetValue = if (isPressed) 0.7f else 1.0f,
        label = "CardPressAlpha"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .alpha(alpha)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onPress = {
                            isPressed = true
                            tryAwaitRelease()
                            isPressed = false
                        }
                    )
                },
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                name,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
                color = appColors.textPrimary,
                modifier = Modifier.weight(1f)
            )
            FavoriteButton(
                isFavorite = isFavorite,
                favoriteId = favoriteId,
                favoriteType = favoriteType,
                name = name,
                rating = rating,
                price = price,
                address = description,
                favoriteViewModel = favoriteViewModel
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (rating.isNotEmpty()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Star,
                        contentDescription = null,
                        tint = appColors.warning,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(rating, color = appColors.textSecondary, fontSize = 13.sp)
                }
            }

            if (price.isNotEmpty()) {
                Text(
                    price,
                    color = appColors.brandTeal,
                    fontWeight = FontWeight.Medium,
                    fontSize = 13.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            description,
            color = appColors.textSecondary,
            fontSize = 13.sp,
            lineHeight = 18.sp,
            maxLines = 2
        )
    }
}

/**
 * 收藏按钮组件
 * 用于在卡片/详情页面添加收藏功能
 *
 * @param isFavorite 是否已收藏
 * @param favoriteId 收藏唯一标识
 * @param favoriteType 收藏类型
 * @param name 名称
 * @param rating 评分
 * @param price 价格
 * @param address 地址
 * @param favoriteViewModel 收藏 ViewModel
 */
@Composable
fun FavoriteButton(
    isFavorite: Boolean,
    favoriteId: String,
    favoriteType: FavoriteType,
    name: String,
    rating: String = "",
    price: String = "",
    address: String,
    favoriteViewModel: FavoriteViewModel
) {
    val appColors = com.example.trip_planner.ui.theme.LocalAppColors.current
    val scale by animateFloatAsState(
        targetValue = if (isFavorite) 1.2f else 1.0f,
        label = "FavoriteScale"
    )
    val iconTint by animateColorAsState(
        targetValue = if (isFavorite) appColors.error else appColors.textSecondary,
        label = "FavoriteTint"
    )

    IconButton(
        onClick = {
            val entity = FavoriteEntity(
                itemId = favoriteId,
                type = favoriteType.name,
                name = name,
                rating = rating,
                price = price,
                address = address,
                description = address
            )
            if (isFavorite) {
                favoriteViewModel.removeFavorite(favoriteId)
            } else {
                favoriteViewModel.addFavorite(entity)
            }
        }
    ) {
        Icon(
            imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
            contentDescription = "收藏",
            tint = iconTint,
            modifier = Modifier.scale(scale)
        )
    }
}

/**
 * 加载状态组件（极简现代风）
 */
@Composable
fun LoadingState(
    message: String,
    appColors: AppColors = com.example.trip_planner.ui.theme.LocalAppColors.current
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(
                color = appColors.brandTeal,
                strokeWidth = 2.dp,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                message, 
                color = appColors.textSecondary,
                fontSize = 13.sp
            )
        }
    }
}

/**
 * 错误状态组件（极简现代风）
 */
@Composable
fun ErrorState(
    errorMessage: String,
    onRetry: () -> Unit,
    appColors: AppColors = com.example.trip_planner.ui.theme.LocalAppColors.current
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Text(
                errorMessage,
                color = appColors.textSecondary,
                fontSize = 14.sp,
                lineHeight = 20.sp
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(
                    containerColor = appColors.brandTeal,
                    contentColor = Color.White
                ),
                shape = MaterialTheme.shapes.small
            ) {
                Text("重试", fontSize = 14.sp)
            }
        }
    }
}

/**
 * 骨架屏加载组件（极简现代风）
 */
@Composable
fun SkeletonLoader(
    modifier: Modifier = Modifier,
    appColors: AppColors = com.example.trip_planner.ui.theme.LocalAppColors.current
) {
    val infiniteTransition = rememberInfiniteTransition()
    val baseColor = appColors.divider
    val highlightColor = if (appColors.softBackground == Color(0xFFF5F5F7)) {
        baseColor.copy(alpha = 0.5f)
    } else {
        baseColor.copy(alpha = 0.7f)
    }
    val animatedColor by infiniteTransition.animateColor(
        initialValue = baseColor,
        targetValue = highlightColor,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        )
    )
    
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(animatedColor)
    )
}

/**
 * 列表项骨架屏（极简现代风）
 */
@Composable
fun ListItemSkeleton(
    appColors: AppColors = com.example.trip_planner.ui.theme.LocalAppColors.current
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
    ) {
        SkeletonLoader(
            modifier = Modifier
                .width(200.dp)
                .height(16.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            SkeletonLoader(
                modifier = Modifier
                    .width(60.dp)
                    .height(14.dp)
            )
            SkeletonLoader(
                modifier = Modifier
                    .width(80.dp)
                    .height(14.dp)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        SkeletonLoader(
            modifier = Modifier
                .fillMaxWidth()
                .height(14.dp)
        )
    }
}

/**
 * 离线状态提示组件（极简现代风）
 */
@Composable
fun OfflineBanner(
    appColors: AppColors = com.example.trip_planner.ui.theme.LocalAppColors.current
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(appColors.warning.copy(alpha = 0.1f))
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Text(
            "当前处于离线模式，部分功能可能受限",
            color = appColors.warning,
            fontSize = 12.sp
        )
    }
}
