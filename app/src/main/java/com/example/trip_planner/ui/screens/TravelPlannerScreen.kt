package com.example.trip_planner.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.trip_planner.data.local.entity.FavoriteEntity
import com.example.trip_planner.data.local.entity.FavoriteType
import com.example.trip_planner.utils.ErrorUtils
import com.example.trip_planner.utils.NetworkMonitor
import com.example.trip_planner.utils.ShareUtils
import com.example.trip_planner.network.model.DayPlan
import com.example.trip_planner.network.model.ItineraryItem
import com.example.trip_planner.network.model.MealInfo
import com.example.trip_planner.network.model.PlanHotel
import com.example.trip_planner.network.model.RestaurantInfoDto
import com.example.trip_planner.network.model.SpotInfo
import com.example.trip_planner.network.model.WeatherResponse
import com.example.trip_planner.viewModel.FavoriteViewModel
import com.example.trip_planner.viewModel.MainViewModel
import com.example.trip_planner.ui.theme.appColors
import com.example.trip_planner.ui.components.SkeletonLoader
import com.example.trip_planner.ui.components.ListItemSkeleton
import com.example.trip_planner.ui.components.OfflineBanner
import com.google.android.gms.maps.model.LatLng
import com.example.trip_planner.ui.screens.DetailType

/**
 * 功能类型枚举
 */
enum class AgentType(val title: String, val icon: String) {
    ALL("全部", "🎯"),
    WEATHER("天气", "🌤️"),
    HOTEL("酒店", "🏨"),
    ATTRACTION("景点", "🏛️"),
    RESTAURANT("餐厅", "🍽️")
}

/**
 * POI类型
 */
enum class PoiType {
    HOTEL, RESTAURANT, ATTRACTION
}

/**
 * POI数据模型
 */
data class PoiModel(
    val name: String,
    val rating: String,
    val price: String,
    val distance: String,
    val latLng: LatLng,
    val desc: String,
    val priceRange: String = "",
    val featureDish: String = "",
    val poiType: PoiType = PoiType.ATTRACTION
)

/**
 * 旅行规划主屏幕 - 卡片式布局
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TravelPlannerScreen(
    modifier: Modifier = Modifier,
    viewModel: MainViewModel = viewModel(),
    favoriteViewModel: FavoriteViewModel = viewModel(),
    onNavigateToDetail: (DetailType) -> Unit = {},
    historyPlanId: Long? = null,
    onHistoryPlanLoaded: () -> Unit = {}
) {
    val appColors = MaterialTheme.appColors
    val weatherData by viewModel.weatherData.collectAsState()
    val hotelData by viewModel.hotelData.collectAsState()
    val restaurantData by viewModel.restaurantData.collectAsState()
    val attractionData by viewModel.attractionData.collectAsState()
    val dayPlans by viewModel.dayPlans.collectAsState()
    val planHotels by viewModel.planHotels.collectAsState()
    val overallTips by viewModel.overallTips.collectAsState()
    var selectedTab by remember { mutableStateOf(AgentType.ALL) }
    val context = LocalContext.current
    val networkMonitor = remember { NetworkMonitor(context) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var isOffline by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        networkMonitor.startListening(scope, snackbarHostState)
        isOffline = !networkMonitor.isNetworkAvailable()
        onDispose {
            networkMonitor.stopListening()
        }
    }

    LaunchedEffect(selectedTab) {
        viewModel.setSelectedAgent(selectedTab)
    }

    LaunchedEffect(historyPlanId) {
        if (historyPlanId != null) {
            viewModel.loadTripPlanFromHistory(historyPlanId)
            onHistoryPlanLoaded()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(appColors.softBackground)
    ) {
        if (isOffline) {
            OfflineBanner(appColors = appColors)
        }
        
        // 功能标签切换（极简现代风）
        TabRow(
            selectedTabIndex = AgentType.entries.indexOf(selectedTab),
            containerColor = Color.Transparent,
            contentColor = appColors.brandTeal,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
            divider = {},
            indicator = { tabPositions ->
                val selectedIndex = AgentType.entries.indexOf(selectedTab)
                if (selectedIndex in tabPositions.indices) {
                    Box(
                        Modifier
                            .tabIndicatorOffset(tabPositions[selectedIndex])
                            .height(2.dp)
                            .padding(horizontal = 12.dp)
                            .background(
                                color = appColors.brandTeal,
                                shape = RoundedCornerShape(2.dp)
                            )
                    )
                }
            }
        ) {
            AgentType.entries.forEach { tab ->
                Tab(
                    selected = selectedTab == tab,
                    onClick = {
                        selectedTab = tab
                        viewModel.setSelectedAgent(tab)
                    },
                    text = {
                        Text(
                            "${tab.icon} ${tab.title}",
                            fontSize = 13.sp,
                            fontWeight = if (selectedTab == tab) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (selectedTab == tab) appColors.brandTeal else appColors.textSecondary
                        )
                    }
                )
            }
        }

        // 内容区域（每个 tab 自带输入控件）
        Box(modifier = Modifier.weight(1f)) {
            val currentAgentState = viewModel.agentUiStates[selectedTab] ?: "Idle"
            val isLoadingThisTab = currentAgentState == "Loading"
            val isErrorThisTab = currentAgentState == "Error"

            when (selectedTab) {
                AgentType.WEATHER -> WeatherTabContent(
                    weatherData = weatherData,
                    viewModel = viewModel,
                    isLoading = isLoadingThisTab,
                    appColors = appColors
                )
                AgentType.HOTEL -> HotelTabContent(
                    poiList = hotelData,
                    viewModel = viewModel,
                    favoriteViewModel = favoriteViewModel,
                    onItemClick = { index ->
                        if (index < viewModel.hotelInfoList.value.size) {
                            val hotel = viewModel.hotelInfoList.value[index]
                            onNavigateToDetail(DetailType.HotelDetail(hotel))
                        }
                    },
                    isLoading = isLoadingThisTab,
                    appColors = appColors
                )
                AgentType.ATTRACTION -> AttractionTabContent(
                    poiList = attractionData,
                    viewModel = viewModel,
                    favoriteViewModel = favoriteViewModel,
                    onItemClick = { index ->
                        if (index < viewModel.spotInfoList.value.size) {
                            val spot = viewModel.spotInfoList.value[index]
                            onNavigateToDetail(DetailType.AttractionDetail(spot))
                        }
                    },
                    isLoading = isLoadingThisTab,
                    appColors = appColors
                )
                AgentType.RESTAURANT -> RestaurantTabContent(
                    poiList = restaurantData,
                    viewModel = viewModel,
                    favoriteViewModel = favoriteViewModel,
                    onItemClick = { index ->
                        if (index < viewModel.restaurantInfoList.value.size) {
                            val restaurant = viewModel.restaurantInfoList.value[index]
                            onNavigateToDetail(DetailType.RestaurantDetail(restaurant))
                        }
                    },
                    isLoading = isLoadingThisTab,
                    appColors = appColors
                )
                AgentType.ALL -> AllInOneTabContent(
                    dayPlans = dayPlans,
                    planHotels = planHotels,
                    overallTips = overallTips,
                    viewModel = viewModel,
                    favoriteViewModel = favoriteViewModel,
                    onNavigateToDetail = onNavigateToDetail,
                    isLoading = isLoadingThisTab,
                    appColors = appColors
                )
            }

            // 错误状态覆盖层
            if (isErrorThisTab) {
                ErrorRecoveryOverlay(
                    errorMessage = ErrorUtils.getFriendlyErrorMessage(viewModel.resultData.value),
                    onRetry = { viewModel.generateTripPlan() },
                    onDismiss = { viewModel.resetCurrentAgentState() },
                    appColors = appColors
                )
            }
        }
    }
}

/**
 * 错误恢复覆盖层
 * 显示错误信息，提供重试和返回按钮
 */
@Composable
fun ErrorRecoveryOverlay(
    errorMessage: String,
    onRetry: () -> Unit,
    onDismiss: () -> Unit,
    appColors: com.example.trip_planner.ui.theme.AppColors
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(appColors.cardBackground.copy(alpha = 0.95f)),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            shape = MaterialTheme.shapes.medium,
            colors = CardDefaults.cardColors(containerColor = appColors.cardBackground),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(24.dp)
            ) {
                Text(
                    "⚠️",
                    fontSize = 48.sp
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "请求失败",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = appColors.textPrimary
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    errorMessage,
                    color = appColors.textSecondary,
                    fontSize = 14.sp,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                Spacer(modifier = Modifier.height(24.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = MaterialTheme.shapes.small,
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = appColors.textSecondary
                        )
                    ) {
                        Text("返回")
                    }
                    Button(
                        onClick = onRetry,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = appColors.brandTeal,
                            contentColor = Color.White
                        ),
                        shape = MaterialTheme.shapes.small
                    ) {
                        Text("重试")
                    }
                }
            }
        }
    }
}

/**
 * 输入卡片区域（极简现代风）
 */
@Composable
fun InputCard(
    viewModel: MainViewModel,
    appColors: com.example.trip_planner.ui.theme.AppColors
) {
    var showDestinations by remember { mutableStateOf(false) }
    val selectedPrefs = remember { mutableStateListOf<String>() }
    
    LaunchedEffect(viewModel.preferences.value) {
        selectedPrefs.clear()
        if (viewModel.preferences.value.isNotEmpty()) {
            selectedPrefs.addAll(viewModel.preferences.value.split(",").map { it.trim() }.filter { it.isNotEmpty() })
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("目的地", fontWeight = FontWeight.Medium, fontSize = 14.sp, color = appColors.textPrimary)
        
        var searchQuery by remember { mutableStateOf("") }
        val filteredDestinations = remember(searchQuery) {
            if (searchQuery.isBlank()) {
                POPULAR_DESTINATIONS
            } else {
                POPULAR_DESTINATIONS.filter { (city, _) ->
                    city.contains(searchQuery, ignoreCase = true)
                }
            }
        }

        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = viewModel.destination.value,
                onValueChange = { 
                    viewModel.setDestination(it)
                    searchQuery = it
                    showDestinations = it.isNotEmpty()
                },
                placeholder = { Text("想去哪里？", color = appColors.textSecondary) },
                leadingIcon = {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = null,
                        tint = appColors.brandTeal
                    )
                },
                trailingIcon = {
                    if (viewModel.destination.value.isNotEmpty()) {
                        IconButton(onClick = { 
                            viewModel.setDestination("")
                            showDestinations = false
                            searchQuery = ""
                        }) {
                            Icon(Icons.Default.Close, contentDescription = "清除", tint = appColors.textSecondary)
                        }
                    }
                },
                shape = MaterialTheme.shapes.small,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = appColors.brandTeal,
                    unfocusedBorderColor = appColors.divider,
                    focusedContainerColor = appColors.softBackground,
                    unfocusedContainerColor = appColors.softBackground
                ),
                singleLine = true
            )

            if (showDestinations && filteredDestinations.isNotEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomStart)
                        .offset(y = 4.dp),
                    shape = MaterialTheme.shapes.small,
                    colors = CardDefaults.cardColors(containerColor = appColors.cardBackground),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(vertical = 4.dp)
                    ) {
                        items(filteredDestinations) { (city, icon) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.setDestination(city)
                                        showDestinations = false
                                        searchQuery = ""
                                    }
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(icon, fontSize = 16.sp)
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    city,
                                    fontSize = 14.sp,
                                    color = appColors.textPrimary,
                                    modifier = Modifier.weight(1f)
                                )
                                if (viewModel.destination.value == city) {
                                    Icon(
                                        Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = appColors.brandTeal,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Text("天数", fontWeight = FontWeight.Medium, fontSize = 14.sp, color = appColors.textPrimary)
        
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = {
                    val current = viewModel.days.value.toIntOrNull() ?: 1
                    if (current > 1) viewModel.setDays((current - 1).toString())
                },
                enabled = (viewModel.days.value.toIntOrNull() ?: 1) > 1,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(Icons.Default.ArrowDropUp, contentDescription = "减少", tint = appColors.brandTeal, modifier = Modifier.graphicsLayer { rotationZ = 180f })
            }
            
            OutlinedTextField(
                value = viewModel.days.value,
                onValueChange = { 
                    val num = it.toIntOrNull()
                    if (num == null || num in 1..30) {
                        viewModel.setDays(it)
                    }
                },
                placeholder = { Text("天数", color = appColors.textSecondary) },
                shape = MaterialTheme.shapes.small,
                modifier = Modifier.weight(1f),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = appColors.brandTeal,
                    unfocusedBorderColor = appColors.divider,
                    focusedContainerColor = appColors.softBackground,
                    unfocusedContainerColor = appColors.softBackground
                ),
                singleLine = true
            )
            
            IconButton(
                onClick = {
                    val current = viewModel.days.value.toIntOrNull() ?: 1
                    if (current < 30) viewModel.setDays((current + 1).toString())
                },
                enabled = (viewModel.days.value.toIntOrNull() ?: 1) < 30,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(Icons.Default.ArrowDropUp, contentDescription = "增加", tint = appColors.brandTeal)
            }
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            listOf(2, 3, 5, 7).forEach { days ->
                FilterChip(
                    selected = viewModel.days.value == days.toString(),
                    onClick = { viewModel.setDays(days.toString()) },
                    label = { Text("${days}天", fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = appColors.brandTeal,
                        selectedLabelColor = Color.White
                    )
                )
            }
        }

        Text("偏好标签（可多选）", fontWeight = FontWeight.Medium, fontSize = 14.sp, color = appColors.textPrimary)
        
        var expandedCategory by remember { mutableStateOf<TagCategory?>(null) }
        
        TagCategory.entries.forEach { category ->
            val categoryTags = PREFERENCE_TAGS.filter { it.category == category }
            val isExpanded = expandedCategory == category
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expandedCategory = if (isExpanded) null else category }
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "${category.icon} ${category.title}",
                    fontWeight = FontWeight.Medium,
                    fontSize = 13.sp,
                    color = appColors.textPrimary
                )
                Icon(
                    if (isExpanded) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                    contentDescription = null,
                    tint = appColors.textSecondary
                )
            }
            
            AnimatedVisibility(visible = isExpanded) {
                FlowLayout(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                ) {
                    categoryTags.forEach { tag ->
                        val isSelected = selectedPrefs.contains(tag.label)
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                if (isSelected) {
                                    selectedPrefs.remove(tag.label)
                                } else {
                                    selectedPrefs.add(tag.label)
                                }
                                viewModel.setPreferences(selectedPrefs.joinToString(","))
                            },
                            leadingIcon = { Text(tag.icon, fontSize = 14.sp) },
                            label = { Text(tag.label, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = appColors.brandTeal.copy(alpha = 0.15f),
                                selectedLabelColor = appColors.brandTeal
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = if (isSelected) appColors.brandTeal else appColors.divider
                            )
                        )
                    }
                }
            }
        }

        Button(
            onClick = {
                val currentAgentState = viewModel.getCurrentAgentUiState()
                if (currentAgentState == "Loading") {
                    viewModel.cancelCurrentRequest()
                    viewModel.resetCurrentAgentState()
                } else {
                    viewModel.generateTripPlan()
                }
            },
            shape = MaterialTheme.shapes.small,
            colors = ButtonDefaults.buttonColors(
                containerColor = if (viewModel.getCurrentAgentUiState() == "Loading") Color.Red else appColors.brandTeal,
                contentColor = Color.White
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            enabled = viewModel.destination.value.isNotBlank() && 
                      (viewModel.days.value.toIntOrNull() ?: 0) > 0
        ) {
            if (viewModel.getCurrentAgentUiState() == "Loading") {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    color = Color.White
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "停止规划",
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp
                )
            } else {
                Text(
                    "开始智能规划",
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp
                )
            }
        }
    }
}

/**
 * 天气 tab 内容（带输入）
 */
@Composable
fun WeatherTabContent(
    weatherData: List<WeatherResponse>,
    viewModel: MainViewModel,
    isLoading: Boolean = false,
    appColors: com.example.trip_planner.ui.theme.AppColors
) {
    Column(modifier = Modifier.fillMaxSize()) {
        WeatherInputCard(viewModel = viewModel, appColors = appColors)
        if (isLoading) {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(16.dp)
            ) {
                items(3) {
                    ListItemSkeleton(appColors = appColors)
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        } else if (weatherData.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🌤️", fontSize = 48.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "输入目的地和天数，获取天气信息",
                        color = appColors.textSecondary,
                        fontSize = 14.sp
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(16.dp)
            ) {
                items(
                    items = weatherData,
                    key = { it.date },
                    contentType = { "weather_card" }
                ) { weather ->
                    WeatherCard(weather = weather, appColors = appColors)
                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

/**
 * 天气输入卡片
 */
@Composable
fun WeatherInputCard(
    viewModel: MainViewModel,
    appColors: com.example.trip_planner.ui.theme.AppColors
) {
    val selectedPrefs = remember { mutableStateListOf<String>() }
    
    LaunchedEffect(viewModel.preferences.value) {
        selectedPrefs.clear()
        if (viewModel.preferences.value.isNotEmpty()) {
            selectedPrefs.addAll(viewModel.preferences.value.split(",").map { it.trim() }.filter { it.isNotEmpty() })
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = appColors.cardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            com.example.trip_planner.ui.components.CitySelector(
                selectedCity = viewModel.getAgentDestination(AgentType.WEATHER),
                onCitySelected = { viewModel.setAgentDestination(AgentType.WEATHER, it) },
                appColors = appColors
            )

            com.example.trip_planner.ui.components.DateRangePicker(
                startDate = viewModel.getAgentStartDate(AgentType.WEATHER),
                endDate = viewModel.getAgentEndDate(AgentType.WEATHER),
                onDateRangeSelected = { start: String, end: String ->
                    viewModel.setAgentDateRange(AgentType.WEATHER, start, end)
                },
                appColors = appColors,
                key = "weather"
            )

            PreferenceSearchSelector(
                selectedPrefs = selectedPrefs,
                viewModel = viewModel,
                appColors = appColors,
                agentType = AgentType.WEATHER
            )

            Button(
                onClick = {
                    val currentAgentState = viewModel.getCurrentAgentUiState()
                    if (currentAgentState == "Loading") {
                        viewModel.cancelCurrentRequest()
                        viewModel.resetCurrentAgentState()
                    } else {
                        viewModel.generateTripPlan()
                    }
                },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (viewModel.getCurrentAgentUiState() == "Loading") Color.Red else appColors.brandTeal
                ),
                modifier = Modifier.fillMaxWidth(),
                enabled = viewModel.getAgentDestination(AgentType.WEATHER).isNotBlank()
            ) {
                if (viewModel.getCurrentAgentUiState() == "Loading") {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("停止获取", fontWeight = FontWeight.Bold)
                } else {
                    Text("获取天气", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

/**
 * 天气内容展示
 */
@Composable
fun WeatherContent(
    weatherList: List<WeatherResponse>,
    appColors: com.example.trip_planner.ui.theme.AppColors
) {
    if (weatherList.isEmpty()) {
        EmptyState(message = "暂无天气数据，请先点击开始规划")
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(weatherList) { weather ->
                WeatherCard(weather = weather, appColors = appColors)
            }
        }
    }
}

/**
 * 天气卡片（极简现代风）
 */
@Composable
fun WeatherCard(
    weather: WeatherResponse,
    appColors: com.example.trip_planner.ui.theme.AppColors
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                weather.date,
                fontWeight = FontWeight.Medium,
                fontSize = 15.sp,
                color = appColors.textPrimary
            )
            Text(
                weather.weather,
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium,
                color = appColors.textPrimary
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            weather.temperature,
            fontSize = 16.sp,
            color = appColors.textSecondary
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            weather.tips,
            color = appColors.textSecondary,
            fontSize = 13.sp,
            lineHeight = 18.sp
        )
    }
}

/**
 * 酒店 tab 内容（带输入）
 */
@Composable
fun HotelTabContent(
    poiList: List<PoiModel>,
    viewModel: MainViewModel,
    favoriteViewModel: FavoriteViewModel,
    onItemClick: (Int) -> Unit = {},
    isLoading: Boolean = false,
    appColors: com.example.trip_planner.ui.theme.AppColors
) {
    var filteredPoiList by remember { mutableStateOf(poiList) }

    Column(modifier = Modifier.fillMaxSize()) {
        HotelInputCard(viewModel = viewModel, appColors = appColors)
        if (isLoading) {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(16.dp)
            ) {
                items(4) {
                    ListItemSkeleton(appColors = appColors)
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        } else if (poiList.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🏨", fontSize = 48.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "输入目的地和天数，获取酒店推荐",
                        color = appColors.textSecondary,
                        fontSize = 14.sp
                    )
                }
            }
        } else {
            com.example.trip_planner.ui.components.AgentFilterToolbar(
                poiList = poiList,
                onFilteredListChanged = { filteredPoiList = it },
                appColors = appColors
            )
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                itemsIndexed(filteredPoiList) { index, poi ->
                    PoiCard(
                        poi = poi,
                        type = AgentType.HOTEL,
                        favoriteViewModel = favoriteViewModel,
                        onClick = { onItemClick(index) },
                        appColors = appColors
                    )
                }
            }
        }
    }
}

/**
 * 酒店输入卡片
 */
@Composable
fun HotelInputCard(
    viewModel: MainViewModel,
    appColors: com.example.trip_planner.ui.theme.AppColors
) {
    val selectedPrefs = remember { mutableStateListOf<String>() }
    
    LaunchedEffect(viewModel.preferences.value) {
        selectedPrefs.clear()
        if (viewModel.preferences.value.isNotEmpty()) {
            selectedPrefs.addAll(viewModel.preferences.value.split(",").map { it.trim() }.filter { it.isNotEmpty() })
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = appColors.cardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            com.example.trip_planner.ui.components.CitySelector(
                selectedCity = viewModel.getAgentDestination(AgentType.RESTAURANT),
                onCitySelected = { viewModel.setAgentDestination(AgentType.RESTAURANT, it) },
                appColors = appColors
            )

            com.example.trip_planner.ui.components.DateRangePicker(
                startDate = viewModel.getAgentStartDate(AgentType.RESTAURANT),
                endDate = viewModel.getAgentEndDate(AgentType.RESTAURANT),
                onDateRangeSelected = { start: String, end: String ->
                    viewModel.setAgentDateRange(AgentType.RESTAURANT, start, end)
                },
                appColors = appColors,
                key = "restaurant"
            )

            PreferenceSearchSelector(
                selectedPrefs = selectedPrefs,
                viewModel = viewModel,
                appColors = appColors,
                agentType = AgentType.RESTAURANT
            )

            Button(
                onClick = {
                    val currentAgentState = viewModel.getCurrentAgentUiState()
                    if (currentAgentState == "Loading") {
                        viewModel.cancelCurrentRequest()
                        viewModel.resetCurrentAgentState()
                    } else {
                        viewModel.generateTripPlan()
                    }
                },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (viewModel.getCurrentAgentUiState() == "Loading") Color.Red else appColors.brandTeal
                ),
                modifier = Modifier.fillMaxWidth(),
                enabled = viewModel.getAgentDestination(AgentType.RESTAURANT).isNotBlank()
            ) {
                if (viewModel.getCurrentAgentUiState() == "Loading") {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("停止推荐", fontWeight = FontWeight.Bold)
                } else {
                    Text("推荐餐厅", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

/**
 * 景点输入卡片
 */
@Composable
fun AttractionInputCard(
    viewModel: MainViewModel,
    appColors: com.example.trip_planner.ui.theme.AppColors
) {
    val selectedPrefs = remember { mutableStateListOf<String>() }
    
    LaunchedEffect(viewModel.preferences.value) {
        selectedPrefs.clear()
        if (viewModel.preferences.value.isNotEmpty()) {
            selectedPrefs.addAll(viewModel.preferences.value.split(",").map { it.trim() }.filter { it.isNotEmpty() })
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = appColors.cardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            com.example.trip_planner.ui.components.CitySelector(
                selectedCity = viewModel.getAgentDestination(AgentType.ATTRACTION),
                onCitySelected = { viewModel.setAgentDestination(AgentType.ATTRACTION, it) },
                appColors = appColors
            )

            com.example.trip_planner.ui.components.DateRangePicker(
                startDate = viewModel.getAgentStartDate(AgentType.ATTRACTION),
                endDate = viewModel.getAgentEndDate(AgentType.ATTRACTION),
                onDateRangeSelected = { start: String, end: String ->
                    viewModel.setAgentDateRange(AgentType.ATTRACTION, start, end)
                },
                appColors = appColors,
                key = "attraction"
            )

            PreferenceSearchSelector(
                selectedPrefs = selectedPrefs,
                viewModel = viewModel,
                appColors = appColors,
                agentType = AgentType.ATTRACTION
            )

            Button(
                onClick = {
                    val currentAgentState = viewModel.getCurrentAgentUiState()
                    if (currentAgentState == "Loading") {
                        viewModel.cancelCurrentRequest()
                        viewModel.resetCurrentAgentState()
                    } else {
                        viewModel.generateTripPlan()
                    }
                },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (viewModel.getCurrentAgentUiState() == "Loading") Color.Red else appColors.brandTeal
                ),
                modifier = Modifier.fillMaxWidth(),
                enabled = viewModel.getAgentDestination(AgentType.ATTRACTION).isNotBlank()
            ) {
                if (viewModel.getCurrentAgentUiState() == "Loading") {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("停止获取", fontWeight = FontWeight.Bold)
                } else {
                    Text("获取酒店", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

/**
 * 景点 tab 内容（带输入）
 */
@Composable
fun AttractionTabContent(
    poiList: List<PoiModel>,
    viewModel: MainViewModel,
    favoriteViewModel: FavoriteViewModel,
    onItemClick: (Int) -> Unit = {},
    isLoading: Boolean = false,
    appColors: com.example.trip_planner.ui.theme.AppColors
) {
    var filteredPoiList by remember { mutableStateOf(poiList) }

    Column(modifier = Modifier.fillMaxSize()) {
        AttractionInputCard(viewModel = viewModel, appColors = appColors)
        if (isLoading) {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(16.dp)
            ) {
                items(5) {
                    ListItemSkeleton(appColors = appColors)
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        } else if (poiList.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🏛️", fontSize = 48.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "输入目的地和天数，获取景点推荐",
                        color = appColors.textSecondary,
                        fontSize = 14.sp
                    )
                }
            }
        } else {
            com.example.trip_planner.ui.components.AgentFilterToolbar(
                poiList = poiList,
                onFilteredListChanged = { filteredPoiList = it },
                appColors = appColors
            )
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                itemsIndexed(filteredPoiList) { index, poi ->
                    PoiCard(
                        poi = poi,
                        type = AgentType.ATTRACTION,
                        favoriteViewModel = favoriteViewModel,
                        onClick = { onItemClick(index) },
                        appColors = appColors
                    )
                }
            }
        }
    }
}

/**
 * 餐厅 tab 内容（带输入）
 */
@Composable
fun RestaurantTabContent(
    poiList: List<PoiModel>,
    viewModel: MainViewModel,
    favoriteViewModel: FavoriteViewModel,
    onItemClick: (Int) -> Unit = {},
    isLoading: Boolean = false,
    appColors: com.example.trip_planner.ui.theme.AppColors
) {
    val selectedPrefs = remember { mutableStateListOf<String>() }
    var filteredPoiList by remember { mutableStateOf(poiList) }
    
    LaunchedEffect(viewModel.preferences.value) {
        selectedPrefs.clear()
        if (viewModel.preferences.value.isNotEmpty()) {
            selectedPrefs.addAll(viewModel.preferences.value.split(",").map { it.trim() }.filter { it.isNotEmpty() })
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        RestaurantInputCard(
            viewModel = viewModel,
            selectedPrefs = selectedPrefs,
            appColors = appColors
        )
        if (isLoading) {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(16.dp)
            ) {
                items(4) {
                    ListItemSkeleton(appColors = appColors)
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        } else if (poiList.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🍽️", fontSize = 48.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "输入目的地和偏好，获取美食推荐",
                        color = appColors.textSecondary,
                        fontSize = 14.sp
                    )
                }
            }
        } else {
            com.example.trip_planner.ui.components.AgentFilterToolbar(
                poiList = poiList,
                onFilteredListChanged = { filteredPoiList = it },
                appColors = appColors
            )
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                itemsIndexed(filteredPoiList) { index, poi ->
                    PoiCard(
                        poi = poi,
                        type = AgentType.RESTAURANT,
                        favoriteViewModel = favoriteViewModel,
                        onClick = { onItemClick(index) },
                        appColors = appColors
                    )
                }
            }
        }
    }
}

/**
 * 餐厅输入卡片（带偏好标签）
 */
@Composable
fun RestaurantInputCard(
    viewModel: MainViewModel,
    selectedPrefs: MutableList<String>,
    appColors: com.example.trip_planner.ui.theme.AppColors
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = appColors.cardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            com.example.trip_planner.ui.components.CitySelector(
                selectedCity = viewModel.getAgentDestination(AgentType.HOTEL),
                onCitySelected = { viewModel.setAgentDestination(AgentType.HOTEL, it) },
                appColors = appColors
            )

            com.example.trip_planner.ui.components.DateRangePicker(
                startDate = viewModel.getAgentStartDate(AgentType.HOTEL),
                endDate = viewModel.getAgentEndDate(AgentType.HOTEL),
                onDateRangeSelected = { start: String, end: String ->
                    viewModel.setAgentDateRange(AgentType.HOTEL, start, end)
                },
                appColors = appColors,
                key = "hotel"
            )

            PreferenceSearchSelector(
                selectedPrefs = selectedPrefs,
                viewModel = viewModel,
                appColors = appColors,
                agentType = AgentType.HOTEL
            )

            Button(
                onClick = {
                    val currentAgentState = viewModel.getCurrentAgentUiState()
                    if (currentAgentState == "Loading") {
                        viewModel.cancelCurrentRequest()
                        viewModel.resetCurrentAgentState()
                    } else {
                        viewModel.generateTripPlan()
                    }
                },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (viewModel.getCurrentAgentUiState() == "Loading") Color.Red else appColors.brandTeal
                ),
                modifier = Modifier.fillMaxWidth(),
                enabled = viewModel.getAgentDestination(AgentType.HOTEL).isNotBlank()
            ) {
                if (viewModel.getCurrentAgentUiState() == "Loading") {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("停止推荐", fontWeight = FontWeight.Bold)
                } else {
                    Text("推荐美食", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

/**
 * 全部规划 tab 内容（带完整输入）
 */
@Composable
fun AllInOneTabContent(
    dayPlans: List<DayPlan>,
    planHotels: List<PlanHotel>,
    overallTips: String,
    viewModel: MainViewModel,
    favoriteViewModel: FavoriteViewModel,
    onNavigateToDetail: (DetailType) -> Unit = {},
    isLoading: Boolean = false,
    appColors: com.example.trip_planner.ui.theme.AppColors
) {
    val selectedPrefs = remember { mutableStateListOf<String>() }
    
    LaunchedEffect(viewModel.preferences.value) {
        selectedPrefs.clear()
        if (viewModel.preferences.value.isNotEmpty()) {
            selectedPrefs.addAll(viewModel.preferences.value.split(",").map { it.trim() }.filter { it.isNotEmpty() })
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        AllInOneInputCard(
            viewModel = viewModel,
            selectedPrefs = selectedPrefs,
            appColors = appColors
        )
        if (isLoading) {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(16.dp)
            ) {
                items(5) {
                    ListItemSkeleton(appColors = appColors)
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        } else if (dayPlans.isEmpty() && planHotels.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🎯", fontSize = 48.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "输入完整信息，一键智能规划行程",
                        color = appColors.textSecondary,
                        fontSize = 14.sp
                    )
                }
            }
        } else {
            SummaryContent(
                dayPlans = dayPlans,
                planHotels = planHotels,
                overallTips = overallTips,
                viewModel = viewModel,
                favoriteViewModel = favoriteViewModel,
                onNavigateToDetail = onNavigateToDetail,
                appColors = appColors
            )
        }
    }
}

/**
 * 全部规划输入卡片
 */
@Composable
fun AllInOneInputCard(
    viewModel: MainViewModel,
    selectedPrefs: MutableList<String>,
    appColors: com.example.trip_planner.ui.theme.AppColors
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = appColors.cardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            com.example.trip_planner.ui.components.CitySelector(
                selectedCity = viewModel.destination.value,
                onCitySelected = { viewModel.setDestination(it) },
                appColors = appColors
            )

            com.example.trip_planner.ui.components.DateRangePicker(
                startDate = viewModel.startDate.value,
                endDate = viewModel.endDate.value,
                onDateRangeSelected = { start: String, end: String ->
                    viewModel.setStartDate(start)
                    viewModel.setEndDate(end)
                },
                appColors = appColors
            )

            PreferenceSearchSelector(
                selectedPrefs = selectedPrefs,
                viewModel = viewModel,
                appColors = appColors
            )

            Button(
                onClick = {
                    val currentAgentState = viewModel.getCurrentAgentUiState()
                    if (currentAgentState == "Loading") {
                        viewModel.cancelCurrentRequest()
                        viewModel.resetCurrentAgentState()
                    } else {
                        viewModel.generateTripPlan()
                    }
                },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (viewModel.getCurrentAgentUiState() == "Loading") Color.Red else appColors.brandTeal
                ),
                modifier = Modifier.fillMaxWidth(),
                enabled = viewModel.destination.value.isNotBlank()
            ) {
                if (viewModel.getCurrentAgentUiState() == "Loading") {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("停止规划", fontWeight = FontWeight.Bold)
                } else {
                    Text("开始规划", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

/**
 * POI内容展示（酒店/景点/餐厅）
 */
@Composable
fun PoiContent(
    poiList: List<PoiModel>,
    type: AgentType,
    favoriteViewModel: FavoriteViewModel,
    onItemClick: (Int) -> Unit = {},
    appColors: com.example.trip_planner.ui.theme.AppColors
) {
    if (poiList.isEmpty()) {
        EmptyState(message = "暂无${type.title}数据，请先点击开始规划")
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            itemsIndexed(poiList) { index, poi ->
                PoiCard(
                    poi = poi,
                    type = type,
                    favoriteViewModel = favoriteViewModel,
                    onClick = { onItemClick(index) },
                    appColors = appColors
                )
            }
        }
    }
}

/**
 * POI卡片（带收藏功能）
 */
@Composable
fun PoiCard(
    poi: PoiModel,
    type: AgentType,
    favoriteViewModel: FavoriteViewModel,
    onClick: () -> Unit = {},
    appColors: com.example.trip_planner.ui.theme.AppColors
) {
    val poiType = when (type) {
        AgentType.HOTEL -> PoiType.HOTEL
        AgentType.RESTAURANT -> PoiType.RESTAURANT
        else -> PoiType.ATTRACTION
    }
    val favoriteId = "${poiType.name}_${poi.name}"
    val favorites by favoriteViewModel.allFavorites.collectAsState()
    val isFavorite = remember(favoriteId, favorites) {
        favorites.any { it.itemId == favoriteId }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = appColors.cardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        onClick = onClick
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // 标题行
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "${type.icon} ${poi.name}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    modifier = Modifier.weight(1f)
                )
                IconButton(
                    onClick = {
                        val favoriteType = when (poiType) {
                            PoiType.HOTEL -> FavoriteType.HOTEL
                            PoiType.RESTAURANT -> FavoriteType.RESTAURANT
                            PoiType.ATTRACTION -> FavoriteType.ATTRACTION
                        }
                        val entity = FavoriteEntity(
                            itemId = favoriteId,
                            type = favoriteType.name,
                            name = poi.name,
                            rating = poi.rating,
                            price = poi.price.ifEmpty { poi.priceRange },
                            address = poi.desc,
                            description = poi.desc
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
                        tint = if (isFavorite) appColors.error else appColors.textSecondary
                    )
                }
            }

            if (poi.rating.isNotEmpty()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Star,
                        contentDescription = null,
                        tint = appColors.warning,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(poi.rating, color = appColors.textSecondary, fontSize = 14.sp)
                }
                Spacer(modifier = Modifier.height(4.dp))
            }

            // 价格
            if (poi.price.isNotEmpty() || poi.priceRange.isNotEmpty()) {
                Text(
                    "💰 ${poi.price.ifEmpty { poi.priceRange }}",
                    color = appColors.brandTeal,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
            }

            // 地址/描述
            Text(
                "📍 ${poi.desc}",
                color = appColors.textSecondary,
                fontSize = 13.sp,
                lineHeight = 18.sp
            )
        }
    }
}

/**
 * 汇总内容展示
 */
@Composable
fun SummaryContent(
    dayPlans: List<DayPlan>,
    planHotels: List<PlanHotel>,
    overallTips: String,
    viewModel: MainViewModel,
    favoriteViewModel: FavoriteViewModel,
    onNavigateToDetail: (DetailType) -> Unit = {},
    appColors: com.example.trip_planner.ui.theme.AppColors
) {
    val context = LocalContext.current
    val favorites by favoriteViewModel.allFavorites.collectAsState()
    val tripPlanId = "TRIP_${viewModel.destination.value}_${viewModel.days.value}"
    val isTripFavorite = favorites.any { it.itemId == tripPlanId }
    val isPlanSaved by viewModel.isPlanSaved.collectAsState()
    val currentSavedPlan by viewModel.currentSavedPlan.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var isEditMode by remember { mutableStateOf(false) }

    if (dayPlans.isEmpty() && planHotels.isEmpty()) {
        EmptyState(message = "暂无行程数据，请先点击开始规划")
    } else {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            containerColor = Color.Transparent
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { isEditMode = !isEditMode },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isEditMode) appColors.warning else appColors.brandTeal,
                                contentColor = appColors.cardBackground
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)
                        ) {
                            Icon(
                                imageVector = if (isEditMode) Icons.Default.Visibility else Icons.Default.Edit,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (isEditMode) "预览" else "编辑", fontSize = 12.sp, maxLines = 1)
                        }

                        Button(
                            onClick = {
                                if (!isPlanSaved) {
                                    val success = viewModel.savePlanToHistory()
                                    if (success) {
                                        scope.launch {
                                            snackbarHostState.showSnackbar("✅ 行程已保存到历史记录")
                                        }
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isPlanSaved) appColors.success else appColors.brandTeal,
                                contentColor = appColors.cardBackground
                            ),
                            shape = RoundedCornerShape(12.dp),
                            enabled = !isPlanSaved,
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)
                        ) {
                            Icon(
                                imageVector = if (isPlanSaved) Icons.Default.CheckCircle else Icons.Default.Save,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (isPlanSaved) "已保存" else "保存", fontSize = 12.sp, maxLines = 1)
                        }

                        Button(
                            onClick = {
                                val entity = FavoriteEntity(
                                    itemId = tripPlanId,
                                    type = FavoriteType.TRIP_PLAN.name,
                                    name = "${viewModel.destination.value} ${viewModel.days.value}日游",
                                    description = overallTips,
                                    extraData = ""
                                )
                                if (isTripFavorite) {
                                    favoriteViewModel.removeFavorite(tripPlanId)
                                } else {
                                    favoriteViewModel.addFavorite(entity)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isTripFavorite) appColors.error else appColors.brandTeal,
                                contentColor = appColors.cardBackground
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)
                        ) {
                            Icon(
                                imageVector = if (isTripFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (isTripFavorite) "已收藏" else "收藏", fontSize = 12.sp, maxLines = 1)
                        }

                        Button(
                            onClick = {
                                val savedPlan = currentSavedPlan
                                if (savedPlan != null) {
                                    ShareUtils.shareTripPlan(context, savedPlan)
                                } else {
                                    scope.launch {
                                        snackbarHostState.showSnackbar("请先保存行程后再分享")
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = appColors.brandTeal,
                                contentColor = appColors.cardBackground
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("分享", fontSize = 12.sp, maxLines = 1)
                        }
                    }
                }

                if (isEditMode) {
                    item {
                        com.example.trip_planner.ui.components.ItineraryEditor(
                            dayPlans = dayPlans,
                            planHotels = planHotels,
                            overallTips = overallTips,
                            onPlansUpdated = { updatedDays, updatedHotels, updatedTips ->
                                viewModel.updatePlans(updatedDays, updatedHotels, updatedTips)
                            },
                            appColors = appColors
                        )
                    }
                } else {

            // 出行建议
            if (overallTips.isNotEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = appColors.brandTeal.copy(alpha = 0.1f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                "💡 出行建议",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                overallTips,
                                color = appColors.textSecondary,
                                fontSize = 14.sp,
                                lineHeight = 22.sp
                            )
                        }
                    }
                }
            }

            // 酒店推荐
            if (planHotels.isNotEmpty()) {
                item {
                    Text(
                        "🏨 酒店推荐",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
                items(
                    items = planHotels,
                    key = { hotel -> "${hotel.name}_${hotel.address}" },
                    contentType = { "hotel_card" }
                ) { hotel ->
                    HotelCard(
                        hotel = hotel,
                        onClick = {
                            onNavigateToDetail(DetailType.HotelDetail(hotel))
                        },
                        appColors = appColors
                    )
                }
            }

            // 每日行程
            if (dayPlans.isNotEmpty()) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "📅 每日行程",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Text(
                            "长按可删除景点/餐厅",
                            color = appColors.textSecondary,
                            fontSize = 12.sp
                        )
                    }
                }
                itemsIndexed(
                    items = dayPlans,
                    key = { index, day -> "day_${day.date}" },
                    contentType = { index, day -> "day_plan_card" }
                ) { dayIndex, day ->
                    DayPlanCard(
                        day = day,
                        dayIndex = dayIndex,
                        onAttractionClick = { item, _ ->
                            onNavigateToDetail(DetailType.AttractionDetail(
                                SpotInfo(
                                    name = item.spot,
                                    latitude = item.latitude,
                                    longitude = item.longitude,
                                    address = item.address,
                                    score = "",
                                    intro = ""
                                )
                            ))
                        },
                        onRestaurantClick = { meal, mealType ->
                            onNavigateToDetail(DetailType.RestaurantDetail(
                                RestaurantInfoDto(
                                    name = meal.name,
                                    latitude = "",
                                    longitude = "",
                                    address = meal.address,
                                    featureDish = meal.dish,
                                    score = ""
                                )
                            ))
                        },
                        onRemoveAttraction = { itemIndex ->
                            viewModel.removeItineraryItem(dayIndex, itemIndex)
                        },
                        onRemoveLunch = {
                            viewModel.removeLunch(dayIndex)
                        },
                        onRemoveDinner = {
                            viewModel.removeDinner(dayIndex)
                        },
                        onMoveItemUp = { itemIndex ->
                            if (itemIndex > 0) {
                                viewModel.moveItineraryItem(dayIndex, itemIndex, itemIndex - 1)
                            }
                        },
                        onMoveItemDown = { itemIndex ->
                            if (itemIndex < day.itinerary.size - 1) {
                                viewModel.moveItineraryItem(dayIndex, itemIndex, itemIndex + 1)
                            }
                        },
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
 * 酒店卡片
 */
@Composable
fun HotelCard(
    hotel: PlanHotel,
    onClick: () -> Unit = {},
    appColors: com.example.trip_planner.ui.theme.AppColors
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = appColors.cardBackground),
        onClick = onClick
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "🏨 ${hotel.name}",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            if (hotel.price.isNotEmpty()) {
                Text(
                    "💰 ${hotel.price}",
                    color = appColors.brandTeal,
                    fontWeight = FontWeight.Medium
                )
            }
            Text(
                "📍 ${hotel.address}",
                color = appColors.textSecondary,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 4.dp)
            )
            if (hotel.advantage.isNotEmpty()) {
                Text(
                    "✨ ${hotel.advantage}",
                    color = appColors.textSecondary,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
    }
}

/**
 * 每日行程卡片
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DayPlanCard(
    day: DayPlan,
    dayIndex: Int = 0,
    onAttractionClick: (item: ItineraryItem, itemIndex: Int) -> Unit = { _, _ -> },
    onRestaurantClick: (meal: MealInfo, mealType: String) -> Unit = { _, _ -> },
    onRemoveAttraction: (itemIndex: Int) -> Unit = {},
    onRemoveLunch: () -> Unit = {},
    onRemoveDinner: () -> Unit = {},
    onMoveItemUp: (itemIndex: Int) -> Unit = {},
    onMoveItemDown: (itemIndex: Int) -> Unit = {},
    appColors: com.example.trip_planner.ui.theme.AppColors
) {
    var expandedItemIndex by remember { mutableStateOf<Int?>(null) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = appColors.cardBackground)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // 标题
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "📅 第${day.dayNum}天 - ${day.date}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(
                    day.weather,
                    color = appColors.textSecondary,
                    fontSize = 13.sp
                )
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 12.dp),
                color = appColors.softBackground
            )

            // 行程安排
            day.itinerary.forEachIndexed { itemIndex, item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable {
                            onAttractionClick(item, itemIndex)
                        }
                        .combinedClickable(
                            onClick = { onAttractionClick(item, itemIndex) },
                            onLongClick = {
                                expandedItemIndex = if (expandedItemIndex == itemIndex) null else itemIndex
                            }
                        ),
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        "⏰ ${item.time}",
                        color = appColors.brandTeal,
                        fontSize = 13.sp,
                        modifier = Modifier.width(60.dp)
                    )
                    Text(
                        "📍 ${item.spot}",
                        fontSize = 14.sp,
                        modifier = Modifier.weight(1f)
                    )

                    // 操作按钮
                    if (expandedItemIndex == itemIndex) {
                        Row {
                            if (itemIndex > 0) {
                                IconButton(onClick = { onMoveItemUp(itemIndex) }) {
                                    Icon(Icons.Default.ArrowDropUp, contentDescription = "上移", tint = appColors.brandTeal)
                                }
                            }
                            if (itemIndex < day.itinerary.size - 1) {
                                IconButton(onClick = { onMoveItemDown(itemIndex) }) {
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = "下移", tint = appColors.brandTeal)
                                }
                            }
                            IconButton(onClick = { onRemoveAttraction(itemIndex) }) {
                                Icon(Icons.Default.Delete, contentDescription = "删除", tint = appColors.error.copy(alpha = 0.7f))
                            }
                            IconButton(onClick = { expandedItemIndex = null }) {
                                Icon(Icons.Default.Close, contentDescription = "关闭", tint = appColors.textSecondary)
                            }
                        }
                    }
                }
            }

            // 餐饮
            day.meals?.let { meals ->
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 12.dp),
                    color = appColors.softBackground
                )
                meals.lunch?.let { lunch ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .combinedClickable(
                                onClick = { onRestaurantClick(lunch, "lunch") },
                                onLongClick = { onRemoveLunch() }
                            )
                    ) {
                        Text("🍱 午餐: ${lunch.name}", color = appColors.textSecondary, fontSize = 13.sp, modifier = Modifier.weight(1f))
                        Text("长按删除", color = appColors.textSecondary.copy(alpha = 0.5f), fontSize = 10.sp)
                    }
                }
                meals.dinner?.let { dinner ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .combinedClickable(
                                onClick = { onRestaurantClick(dinner, "dinner") },
                                onLongClick = { onRemoveDinner() }
                            )
                    ) {
                        Text("🍲 晚餐: ${dinner.name}", color = appColors.textSecondary, fontSize = 13.sp, modifier = Modifier.weight(1f))
                        Text("长按删除", color = appColors.textSecondary.copy(alpha = 0.5f), fontSize = 10.sp)
                    }
                }
            }

            // 温馨提示
            if (day.tips.isNotEmpty()) {
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 12.dp),
                    color = appColors.softBackground
                )
                Text(
                    "💡 ${day.tips}",
                    color = appColors.brandTeal,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
            }
        }
    }
}

/**
 * 空状态提示
 */
@Composable
fun EmptyState(message: String, appColors: com.example.trip_planner.ui.theme.AppColors = com.example.trip_planner.ui.theme.LocalAppColors.current) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                "📭",
                fontSize = 48.sp
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                message,
                color = appColors.textSecondary,
                fontSize = 14.sp
            )
        }
    }
}

/**
 * 流式布局
 */
@Composable
fun FlowLayout(
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    content: @androidx.compose.runtime.Composable () -> Unit
) {
    androidx.compose.ui.layout.Layout(
        modifier = modifier,
        content = content
    ) { measurables, constraints ->
        val rowWidth = constraints.maxWidth
        val placeables = mutableListOf<androidx.compose.ui.layout.Placeable>()
        val coordinates = mutableListOf<Pair<Int, Int>>()
        var x = 0
        var y = 0
        var rowHeight = 0

        val measurablePlaceables = measurables.map { measurable ->
            measurable.measure(constraints.copy(minWidth = 0, minHeight = 0))
        }

        measurablePlaceables.forEach { placeable ->
            if (x + placeable.width > rowWidth && x > 0) {
                x = 0
                y += rowHeight
                rowHeight = 0
            }
            coordinates.add(x to y)
            placeables.add(placeable)
            x += placeable.width
            rowHeight = maxOf(rowHeight, placeable.height)
        }

        layout(rowWidth, y + rowHeight) {
            placeables.forEachIndexed { index, placeable ->
                val (xCoord, yCoord) = coordinates[index]
                placeable.placeRelative(xCoord, yCoord)
            }
        }
    }
}

/**
 * 偏好标签数据
 */
data class PreferenceTag(val icon: String, val label: String, val category: TagCategory)

enum class TagCategory(val title: String, val icon: String) {
    BUDGET("预算", "💰"),
    HOTEL("酒店类型", "🏨"),
    RESTAURANT("餐饮风味", "🍽️"),
    ATTRACTION("景点偏好", "🏛️"),
    TRANSPORT("交通方式", "🚗"),
    PACE("行程节奏", "⏱️"),
    GENERAL("通用偏好", "✨")
}

val PREFERENCE_TAGS = listOf(
    PreferenceTag("💸", "100元/天", TagCategory.BUDGET),
    PreferenceTag("💰", "300元/天", TagCategory.BUDGET),
    PreferenceTag("💵", "500元/天", TagCategory.BUDGET),
    PreferenceTag("💎", "1000元/天", TagCategory.BUDGET),
    PreferenceTag("👑", "不限预算", TagCategory.BUDGET),
    PreferenceTag("🎒", "穷游", TagCategory.BUDGET),
    PreferenceTag("💳", "性价比", TagCategory.BUDGET),
    
    PreferenceTag("🏠", "经济型", TagCategory.HOTEL),
    PreferenceTag("🏨", "舒适型", TagCategory.HOTEL),
    PreferenceTag("💎", "高档型", TagCategory.HOTEL),
    PreferenceTag("👑", "豪华型", TagCategory.HOTEL),
    PreferenceTag("🏡", "民宿", TagCategory.HOTEL),
    PreferenceTag("🏕️", "青旅", TagCategory.HOTEL),
    PreferenceTag("👨‍👩‍👧‍👦", "亲子酒店", TagCategory.HOTEL),
    PreferenceTag("💑", "情侣酒店", TagCategory.HOTEL),
    PreferenceTag("🚇", "近地铁", TagCategory.HOTEL),
    PreferenceTag("🍳", "含早餐", TagCategory.HOTEL),
    PreferenceTag("🅿️", "停车场", TagCategory.HOTEL),
    PreferenceTag("🏊", "带泳池", TagCategory.HOTEL),
    PreferenceTag("💪", "健身房", TagCategory.HOTEL),
    PreferenceTag("🌅", "景观房", TagCategory.HOTEL),
    
    PreferenceTag("🌶️", "不吃辣", TagCategory.RESTAURANT),
    PreferenceTag("🥬", "素食", TagCategory.RESTAURANT),
    PreferenceTag("🍲", "火锅", TagCategory.RESTAURANT),
    PreferenceTag("🍢", "烧烤", TagCategory.RESTAURANT),
    PreferenceTag("🥟", "小吃", TagCategory.RESTAURANT),
    PreferenceTag("🍱", "日料", TagCategory.RESTAURANT),
    PreferenceTag("🌮", "西餐", TagCategory.RESTAURANT),
    PreferenceTag("⭐", "老字号", TagCategory.RESTAURANT),
    PreferenceTag("🦐", "海鲜", TagCategory.RESTAURANT),
    PreferenceTag("🍜", "面食", TagCategory.RESTAURANT),
    PreferenceTag("🥩", "烤肉", TagCategory.RESTAURANT),
    PreferenceTag("🍕", "快餐", TagCategory.RESTAURANT),
    PreferenceTag("🍰", "甜品", TagCategory.RESTAURANT),
    PreferenceTag("🍵", "清淡", TagCategory.RESTAURANT),
    PreferenceTag("🌿", "有机", TagCategory.RESTAURANT),
    PreferenceTag("🍺", "夜宵", TagCategory.RESTAURANT),
    PreferenceTag("💰", "人均50内", TagCategory.RESTAURANT),
    PreferenceTag("💵", "人均100内", TagCategory.RESTAURANT),
    PreferenceTag("💎", "人均200+", TagCategory.RESTAURANT),
    
    PreferenceTag("🏔️", "自然风光", TagCategory.ATTRACTION),
    PreferenceTag("🏛️", "人文历史", TagCategory.ATTRACTION),
    PreferenceTag("🎢", "主题乐园", TagCategory.ATTRACTION),
    PreferenceTag("👨‍👩‍👧‍👦", "亲子游", TagCategory.ATTRACTION),
    PreferenceTag("💑", "情侣约会", TagCategory.ATTRACTION),
    PreferenceTag("📸", "拍照打卡", TagCategory.ATTRACTION),
    PreferenceTag("🚶", "轻松休闲", TagCategory.ATTRACTION),
    PreferenceTag("🧗", "体力挑战", TagCategory.ATTRACTION),
    PreferenceTag("🎭", "文化体验", TagCategory.ATTRACTION),
    PreferenceTag("🛍️", "购物", TagCategory.ATTRACTION),
    PreferenceTag("🎪", "演出展览", TagCategory.ATTRACTION),
    PreferenceTag("🏖️", "海滩度假", TagCategory.ATTRACTION),
    PreferenceTag("🌸", "赏花", TagCategory.ATTRACTION),
    PreferenceTag("🎿", "滑雪", TagCategory.ATTRACTION),
    PreferenceTag("🚣", "水上活动", TagCategory.ATTRACTION),
    
    PreferenceTag("🚶", "步行", TagCategory.TRANSPORT),
    PreferenceTag("🚌", "公交", TagCategory.TRANSPORT),
    PreferenceTag("🚇", "地铁", TagCategory.TRANSPORT),
    PreferenceTag("🚕", "打车", TagCategory.TRANSPORT),
    PreferenceTag("🚗", "租车自驾", TagCategory.TRANSPORT),
    PreferenceTag("🚲", "骑行", TagCategory.TRANSPORT),
    PreferenceTag("🛵", "电动车", TagCategory.TRANSPORT),
    
    PreferenceTag("🐌", "慢节奏", TagCategory.PACE),
    PreferenceTag("🚶", "适中", TagCategory.PACE),
    PreferenceTag("🏃", "特种兵", TagCategory.PACE),
    PreferenceTag("😴", "睡到自然醒", TagCategory.PACE),
    PreferenceTag("📅", "行程紧凑", TagCategory.PACE),
    PreferenceTag("🌅", "早起", TagCategory.PACE),
    PreferenceTag("🌙", "夜猫子", TagCategory.PACE),
    
    PreferenceTag("📷", "摄影", TagCategory.GENERAL),
    PreferenceTag("🎁", "伴手礼", TagCategory.GENERAL),
    PreferenceTag("🐕", "宠物友好", TagCategory.GENERAL),
    PreferenceTag("♿", "无障碍", TagCategory.GENERAL),
    PreferenceTag("👶", "婴儿车", TagCategory.GENERAL),
    PreferenceTag("🎒", "背包客", TagCategory.GENERAL)
)

/**
 * 热门目的地列表
 */
val POPULAR_DESTINATIONS = listOf(
    "成都" to "🐼",
    "北京" to "🏯",
    "上海" to "🌃",
    "杭州" to "🌊",
    "西安" to "🏺",
    "重庆" to "🌶️",
    "厦门" to "🏖️",
    "丽江" to "🏔️",
    "三亚" to "🌴",
    "桂林" to "⛰️"
)

val AGENT_TAG_MAP = mapOf(
    AgentType.HOTEL to listOf(TagCategory.HOTEL, TagCategory.BUDGET),
    AgentType.RESTAURANT to listOf(TagCategory.RESTAURANT, TagCategory.BUDGET),
    AgentType.ATTRACTION to listOf(TagCategory.ATTRACTION),
    AgentType.WEATHER to listOf(TagCategory.PACE, TagCategory.GENERAL),
    AgentType.ALL to TagCategory.entries
)

@Composable
fun PreferenceSearchSelector(
    selectedPrefs: MutableList<String>,
    viewModel: MainViewModel,
    appColors: com.example.trip_planner.ui.theme.AppColors,
    agentType: AgentType = AgentType.ALL
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedParentCategory by remember { mutableStateOf<TagCategory?>(null) }
    val allTags = remember { PREFERENCE_TAGS }
    val relevantCategories = remember { AGENT_TAG_MAP[agentType] ?: TagCategory.entries }

    val filteredTags = remember(searchQuery, allTags, relevantCategories) {
        val base = allTags.filter { it.category in relevantCategories }
        if (searchQuery.isBlank()) {
            base
        } else {
            base.filter {
                it.label.contains(searchQuery, ignoreCase = true) ||
                it.icon.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    LaunchedEffect(relevantCategories) {
        selectedParentCategory = relevantCategories.firstOrNull()
    }

    val currentSubTags = remember(selectedParentCategory, filteredTags) {
        if (selectedParentCategory == null) {
            emptyList()
        } else {
            filteredTags.filter { it.category == selectedParentCategory }
        }
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        if (selectedPrefs.isNotEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("已选标签", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = appColors.brandTeal)
                TextButton(onClick = {
                    selectedPrefs.clear()
                    viewModel.setPreferences("")
                }) {
                    Text("清空", fontSize = 11.sp, color = appColors.textSecondary)
                }
            }
            FlowLayout(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                selectedPrefs.forEach { tag ->
                    FilterChip(
                        selected = true,
                        onClick = {
                            selectedPrefs.remove(tag)
                            viewModel.setPreferences(selectedPrefs.joinToString(","))
                        },
                        leadingIcon = {
                            val matched = allTags.find { it.label == tag }
                            if (matched != null) Text(matched.icon, fontSize = 12.sp)
                        },
                        label = { Text(tag, fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = appColors.brandTeal.copy(alpha = 0.12f),
                            selectedLabelColor = appColors.brandTeal
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = true,
                            borderColor = appColors.brandTeal,
                            borderWidth = 1.dp
                        ),
                        trailingIcon = {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "移除",
                                modifier = Modifier.size(14.dp),
                                tint = appColors.brandTeal
                            )
                        }
                    )
                }
            }
        }

        if (searchQuery.isBlank()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("分类", fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = appColors.textPrimary)
                Spacer(modifier = Modifier.width(8.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(relevantCategories) { category ->
                        val isSelected = selectedParentCategory == category
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = if (isSelected) appColors.brandTeal.copy(alpha = 0.15f) else appColors.softBackground,
                            border = BorderStroke(if (isSelected) 1.5.dp else 1.dp, if (isSelected) appColors.brandTeal else appColors.divider),
                            modifier = Modifier.clickable { 
                                // 切换展开/关闭：已选中则关闭，否则展开
                                selectedParentCategory = if (isSelected) null else category 
                            }
                        ) {
                            Text(
                                "${category.icon} ${category.title}",
                                fontSize = 11.sp,
                                color = if (isSelected) appColors.brandTeal else appColors.textSecondary,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }

            if (selectedParentCategory != null && currentSubTags.isNotEmpty()) {
                Text(selectedParentCategory?.let { "${it.icon} ${it.title}" } ?: "", fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = appColors.textPrimary)
                FlowLayout(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    currentSubTags.forEach { tag ->
                        val isSelected = selectedPrefs.contains(tag.label)
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                if (isSelected) {
                                    selectedPrefs.remove(tag.label)
                                } else {
                                    selectedPrefs.add(tag.label)
                                }
                                viewModel.setPreferences(selectedPrefs.joinToString(","))
                            },
                            leadingIcon = { Text(tag.icon, fontSize = 13.sp) },
                            label = { Text(tag.label, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = appColors.brandTeal.copy(alpha = 0.15f),
                                selectedLabelColor = appColors.brandTeal,
                                containerColor = appColors.softBackground,
                                labelColor = appColors.textSecondary
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = if (isSelected) appColors.brandTeal else appColors.divider
                            )
                        )
                    }
                }
            }
        } else {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("搜索标签...", color = appColors.textSecondary, fontSize = 12.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = appColors.brandTeal, modifier = Modifier.size(18.dp)) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (allTags.none { it.label == searchQuery }) {
                                TextButton(
                                    onClick = {
                                        selectedPrefs.add(searchQuery)
                                        viewModel.setPreferences(selectedPrefs.joinToString(","))
                                        viewModel.saveUserPreferenceTag(searchQuery)
                                        searchQuery = ""
                                    },
                                    contentPadding = PaddingValues(horizontal = 4.dp)
                                ) {
                                    Text("+ 添加", fontSize = 12.sp, color = appColors.brandTeal, fontWeight = FontWeight.Bold)
                                }
                            }
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "清除", tint = appColors.textSecondary, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = appColors.brandTeal,
                    unfocusedBorderColor = appColors.divider,
                    focusedContainerColor = appColors.cardBackground,
                    unfocusedContainerColor = appColors.cardBackground
                ),
                singleLine = true,
                textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp)
            )

            if (filteredTags.isNotEmpty()) {
                FlowLayout(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    filteredTags.forEach { tag ->
                        val isSelected = selectedPrefs.contains(tag.label)
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                if (isSelected) {
                                    selectedPrefs.remove(tag.label)
                                } else {
                                    selectedPrefs.add(tag.label)
                                }
                                viewModel.setPreferences(selectedPrefs.joinToString(","))
                            },
                            leadingIcon = { Text(tag.icon, fontSize = 13.sp) },
                            label = { Text(tag.label, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = appColors.brandTeal.copy(alpha = 0.15f),
                                selectedLabelColor = appColors.brandTeal,
                                containerColor = appColors.softBackground,
                                labelColor = appColors.textSecondary
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = if (isSelected) appColors.brandTeal else appColors.divider
                            )
                        )
                    }
                }
            }
        }
    }
}
