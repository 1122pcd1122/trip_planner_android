package com.example.trip_planner.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.trip_planner.ui.theme.LocalAppColors
import kotlinx.coroutines.launch

data class OnboardingPage(
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val title: String,
    val description: String,
    val backgroundColor: Color
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(
    onFinish: () -> Unit
) {
    val appColors = LocalAppColors.current
    val scope = rememberCoroutineScope()
    val pages = listOf(
        OnboardingPage(
            icon = Icons.Default.LocationOn,
            title = "智能行程规划",
            description = "输入目的地和偏好，AI 为你生成完整旅行计划\n包含景点、酒店、餐厅和每日行程安排",
            backgroundColor = appColors.brandTeal
        ),
        OnboardingPage(
            icon = Icons.Default.WbSunny,
            title = "实时天气查询",
            description = "出行前查看目的地天气预报\n让你的旅行不受天气影响",
            backgroundColor = appColors.brandTeal
        ),
        OnboardingPage(
            icon = Icons.Default.Hotel,
            title = "精选酒店推荐",
            description = "根据你的偏好和预算推荐优质酒店\n价格透明，一键预订",
            backgroundColor = appColors.brandTeal
        ),
        OnboardingPage(
            icon = Icons.Default.Restaurant,
            title = "地道美食探索",
            description = "发现当地特色餐厅和美食\n让你的味蕾也来一场旅行",
            backgroundColor = appColors.brandTeal
        )
    )

    val pagerState = rememberPagerState(pageCount = { pages.size })

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f)
            ) { page ->
                OnboardingPageContent(pages[page], appColors)
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(pages.size) { index ->
                    val isSelected = pagerState.currentPage == index
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .clip(CircleShape)
                            .background(
                                if (isSelected) appColors.textPrimary else appColors.textSecondary.copy(alpha = 0.2f)
                            )
                            .size(if (isSelected) 6.dp else 4.dp)
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (pagerState.currentPage > 0) {
                    TextButton(onClick = {
                        scope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage - 1)
                        }
                    }) {
                        Text("上一页", color = appColors.textSecondary, fontSize = 14.sp)
                    }
                } else {
                    Spacer(modifier = Modifier.width(80.dp))
                }

                if (pagerState.currentPage < pages.size - 1) {
                    Button(
                        onClick = {
                            scope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = appColors.brandTeal)
                    ) {
                        Text("下一页", fontSize = 14.sp)
                    }
                } else {
                    Button(
                        onClick = onFinish,
                        colors = ButtonDefaults.buttonColors(containerColor = appColors.brandTeal)
                    ) {
                        Text("开始使用", fontSize = 14.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun OnboardingPageContent(
    page: OnboardingPage,
    appColors: com.example.trip_planner.ui.theme.AppColors
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 40.dp, vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = page.icon,
            contentDescription = null,
            tint = page.backgroundColor,
            modifier = Modifier.size(48.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = page.title,
            fontSize = 20.sp,
            fontWeight = FontWeight.Medium,
            color = appColors.textPrimary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = page.description,
            fontSize = 14.sp,
            color = appColors.textSecondary,
            textAlign = TextAlign.Center,
            lineHeight = 22.sp
        )
    }
}
