package com.example.trip_planner.ui.content

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.amap.api.maps.model.LatLng
import com.example.trip_planner.data.local.entity.FavoriteEntity
import com.example.trip_planner.network.model.DayPlan
import com.example.trip_planner.network.model.PlanHotel
import com.example.trip_planner.network.model.WeatherResponse
import com.example.trip_planner.viewModel.FavoriteViewModel
import com.example.trip_planner.viewModel.MainViewModel

/**
 * 颜色定义
 */
val SoftBackground = Color(0xFFF7F8FA)
val BrandTeal = Color(0xFF2A9D8F)
val TextPrimary = Color(0xFF2B2D33)
val TextSecondary = Color(0xFF8F92A1)
val CardBackground = Color(0xFFFFFFFF)

/**
 * 功能类型枚举
 */
enum class AgentType(val title: String, val icon: String) {
    ALL("全部", "🎯"),
    WEATHER("天气", "🌤️"),
    HOTEL("酒店", "🏨"),
    ATTRACTION("景点", "🏛️"),
    RESTAURANT("餐厅", "🍽️"),
    FAVORITES("收藏", "❤️")
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
 * 收藏项数据模型
 *
 * @property id 唯一标识（由类型+名称生成）
 * @property type 收藏类型（酒店/景点/餐厅）
 * @property name 名称
 * @property rating 评分
 * @property price 价格
 * @property address 地址
 * @property description 描述
 * @property timestamp 收藏时间戳
 */
data class FavoriteItem(
    val id: String,
    val type: PoiType,
    val name: String,
    val rating: String = "",
    val price: String = "",
    val address: String = "",
    val description: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * 旅行规划主屏幕 - 卡片式布局
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TravelPlannerScreen(
    modifier: Modifier = Modifier,
    viewModel: MainViewModel = viewModel(),
    favoriteViewModel: FavoriteViewModel = viewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState
    val weatherData by viewModel.weatherData
    val hotelData by viewModel.hotelData
    val restaurantData by viewModel.restaurantData
    val attractionData by viewModel.attractionData
    val dayPlans by viewModel.dayPlans
    val planHotels by viewModel.planHotels
    val overallTips by viewModel.overallTips
    val favoriteItems by favoriteViewModel.allFavorites.collectAsState()
    var selectedTab by remember { mutableStateOf(AgentType.ALL) }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🎒", fontSize = 24.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("旅行规划", fontWeight = FontWeight.Bold)
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
            // 输入区域
            InputCard(viewModel = viewModel)

            // 功能标签切换
            TabRow(
                selectedTabIndex = AgentType.entries.indexOf(selectedTab),
                containerColor = CardBackground,
                contentColor = BrandTeal,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)
            ) {
                AgentType.entries.forEach { tab ->
                    Tab(
                        selected = selectedTab == tab,
                        onClick = { selectedTab = tab },
                        text = {
                            Text(
                                "${tab.icon} ${tab.title}",
                                fontSize = 10.sp
                            )
                        }
                    )
                }
            }

            // 内容区域
            Box(modifier = Modifier.weight(1f)) {
                when (uiState) {
                    "Loading" -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator(color = BrandTeal)
                                Spacer(modifier = Modifier.height(16.dp))
                                when (selectedTab) {
                                    AgentType.WEATHER -> Text("正在获取天气...", color = TextSecondary)
                                    AgentType.HOTEL -> Text("正在推荐酒店...", color = TextSecondary)
                                    AgentType.ATTRACTION -> Text("正在推荐景点...", color = TextSecondary)
                                    AgentType.RESTAURANT ->Text("正在推荐美食...", color = TextSecondary)
                                    AgentType.ALL -> Text("正在规划您的行程...", color = TextSecondary)
                                    else -> {

                                    }
                                }

                            }
                        }
                    }
                    "Error" -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "❌ 加载失败，请检查网络后重试",
                                color = Color.Red,
                                fontSize = 14.sp
                            )
                        }
                    }
                    else -> {
                        when (selectedTab) {
                            AgentType.WEATHER -> WeatherContent(weatherData)
                            AgentType.HOTEL -> PoiContent(hotelData, AgentType.HOTEL, viewModel) {

                            }
                            AgentType.ATTRACTION -> PoiContent(attractionData, AgentType.ATTRACTION, viewModel){

                            }
                            AgentType.RESTAURANT -> PoiContent(restaurantData, AgentType.RESTAURANT, viewModel){

                            }
                            AgentType.ALL -> SummaryContent(dayPlans, planHotels, overallTips, viewModel)
                            AgentType.FAVORITES -> FavoritesContent(favoriteItems, favoriteViewModel)
                        }
                    }
                }
            }
        }
    }
}

/**
 * 输入卡片区域
 */
@Composable
fun InputCard(viewModel: MainViewModel) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 目的地输入
            OutlinedTextField(
                value = viewModel.destination.value,
                onValueChange = { viewModel.destination.value = it },
                placeholder = { Text("想去哪里？", color = TextSecondary) },
                leadingIcon = {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = null,
                        tint = BrandTeal
                    )
                },
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = BrandTeal,
                    unfocusedBorderColor = Color.Transparent,
                    focusedContainerColor = SoftBackground,
                    unfocusedContainerColor = SoftBackground
                ),
                singleLine = true
            )

            // 天数和偏好
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = viewModel.days.value,
                    onValueChange = { viewModel.days.value = it },
                    placeholder = { Text("天数", color = TextSecondary) },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BrandTeal,
                        unfocusedBorderColor = Color.Transparent,
                        focusedContainerColor = SoftBackground,
                        unfocusedContainerColor = SoftBackground
                    ),
                    singleLine = true
                )
                OutlinedTextField(
                    value = viewModel.preferences.value,
                    onValueChange = { viewModel.preferences.value = it },
                    placeholder = { Text("偏好", color = TextSecondary) },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BrandTeal,
                        unfocusedBorderColor = Color.Transparent,
                        focusedContainerColor = SoftBackground,
                        unfocusedContainerColor = SoftBackground
                    ),
                    singleLine = true
                )
            }

            // 开始规划按钮
            Button(
                onClick = { viewModel.generateTripPlan() },
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BrandTeal),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text(
                    "✨ 开始智能规划",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        }
    }
}

/**
 * 天气内容展示
 */
@Composable
fun WeatherContent(weatherList: List<WeatherResponse>) {
    if (weatherList.isEmpty()) {
        EmptyState(message = "暂无天气数据，请先点击开始规划")
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(weatherList) { weather ->
                WeatherCard(weather = weather)
            }
        }
    }
}

/**
 * 天气卡片
 */
@Composable
fun WeatherCard(weather: WeatherResponse) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = listOf(Color(0xFF80D0C7), Color(0xFF13547A))
                    )
                )
                .padding(24.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        weather.date,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Text(
                        weather.weather,
                        color = Color.White,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                Text(
                    weather.temperature,
                    color = Color.White,
                    fontSize = 20.sp,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                HorizontalDivider(
                    color = Color.White.copy(alpha = 0.3f),
                    modifier = Modifier.padding(vertical = 12.dp)
                )
                Text(
                    "💡 ${weather.tips}",
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 13.sp,
                    lineHeight = 20.sp
                )
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
    viewModel: MainViewModel,
    onFavoriteChanged: () -> Unit
) {
    if (poiList.isEmpty()) {
        EmptyState(message = "暂无${type.title}数据，请先点击开始规划")
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(poiList) { poi ->
                PoiCard(poi = poi, type = type, viewModel = viewModel, onFavoriteChanged = onFavoriteChanged)
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
    viewModel: MainViewModel,
    onFavoriteChanged: () -> Unit
) {
    val poiType = when (type) {
        AgentType.HOTEL -> PoiType.HOTEL
        AgentType.RESTAURANT -> PoiType.RESTAURANT
        else -> PoiType.ATTRACTION
    }
    val favoriteId = "${poiType.name}_${poi.name}"
    var isFavorite by remember { mutableStateOf(viewModel.isFavorite(favoriteId)) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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
                        val item = FavoriteItem(
                            id = favoriteId,
                            type = poiType,
                            name = poi.name,
                            rating = poi.rating,
                            price = poi.price.ifEmpty { poi.priceRange },
                            address = poi.desc,
                            description = poi.desc
                        )
                        isFavorite = viewModel.toggleFavorite(item)
                        onFavoriteChanged()
                    }
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "收藏",
                        tint = if (isFavorite) Color.Red else TextSecondary
                    )
                }
            }

            if (poi.rating.isNotEmpty()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Star,
                        contentDescription = null,
                        tint = Color(0xFFFFB800),
                        modifier = Modifier.size(16.dp)
                    )
                    Text(poi.rating, color = TextSecondary, fontSize = 14.sp)
                }
                Spacer(modifier = Modifier.height(4.dp))
            }

            // 价格
            if (poi.price.isNotEmpty() || poi.priceRange.isNotEmpty()) {
                Text(
                    "💰 ${if (poi.price.isNotEmpty()) poi.price else poi.priceRange}",
                    color = BrandTeal,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
            }

            // 地址/描述
            Text(
                "📍 ${poi.desc}",
                color = TextSecondary,
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
    hotels: List<PlanHotel>,
    tips: String,
    viewModel: MainViewModel? = null
) {
    if (dayPlans.isEmpty() && hotels.isEmpty()) {
        EmptyState(message = "暂无行程数据，请先点击开始规划")
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 收藏按钮
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    if (viewModel != null) {
                        Button(
                            onClick = { viewModel.saveCurrentTripPlan() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = BrandTeal,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.FavoriteBorder,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("收藏此行程", fontSize = 14.sp)
                        }
                    }
                }
            }

            // 出行建议
            if (tips.isNotEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = BrandTeal.copy(alpha = 0.1f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                "💡 出行建议",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                tips,
                                color = TextSecondary,
                                fontSize = 14.sp,
                                lineHeight = 22.sp
                            )
                        }
                    }
                }
            }

            // 酒店推荐
            if (hotels.isNotEmpty()) {
                item {
                    Text(
                        "🏨 酒店推荐",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
                items(hotels) { hotel ->
                    HotelCard(hotel = hotel)
                }
            }

            // 每日行程
            if (dayPlans.isNotEmpty()) {
                item {
                    Text(
                        "📅 每日行程",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
                items(dayPlans) { day ->
                    DayPlanCard(day = day)
                }
            }
        }
    }
}

/**
 * 酒店卡片
 */
@Composable
fun HotelCard(hotel: PlanHotel) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground)
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
                    color = BrandTeal,
                    fontWeight = FontWeight.Medium
                )
            }
            Text(
                "📍 ${hotel.address}",
                color = TextSecondary,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 4.dp)
            )
            if (hotel.advantage.isNotEmpty()) {
                Text(
                    "✨ ${hotel.advantage}",
                    color = TextSecondary,
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
@Composable
fun DayPlanCard(day: DayPlan) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground)
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
                    color = TextSecondary,
                    fontSize = 13.sp
                )
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 12.dp),
                color = SoftBackground
            )

            // 行程安排
            day.itinerary.forEach { item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        "⏰ ${item.time}",
                        color = BrandTeal,
                        fontSize = 13.sp,
                        modifier = Modifier.width(60.dp)
                    )
                    Text(
                        "📍 ${item.spot}",
                        fontSize = 14.sp,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // 餐饮
            day.meals?.let { meals ->
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 12.dp),
                    color = SoftBackground
                )
                meals.lunch?.let {
                    Text("🍱 午餐: ${it.name}", color = TextSecondary, fontSize = 13.sp)
                }
                meals.dinner?.let {
                    Text("🍲 晚餐: ${it.name}", color = TextSecondary, fontSize = 13.sp)
                }
            }

            // 温馨提示
            if (day.tips.isNotEmpty()) {
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 12.dp),
                    color = SoftBackground
                )
                Text(
                    "💡 ${day.tips}",
                    color = BrandTeal,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
            }
        }
    }
}

/**
 * 收藏内容展示
 */
@Composable
fun FavoritesContent(
    favorites: List<FavoriteEntity>,
    favoriteViewModel: FavoriteViewModel
) {
    if (favorites.isEmpty()) {
        EmptyState(message = "暂无收藏，快去收藏喜欢的酒店/景点吧")
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(favorites) { favorite ->
                FavoriteCard(favorite = favorite, favoriteViewModel = favoriteViewModel)
            }
        }
    }
}

/**
 * 收藏卡片
 */
@Composable
fun FavoriteCard(
    favorite: FavoriteEntity,
    favoriteViewModel: FavoriteViewModel
) {
    val typeIcon = when (favorite.type) {
        "HOTEL" -> "🏨"
        "RESTAURANT" -> "🍽️"
        "ATTRACTION" -> "🏛️"
        else -> "📌"
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
            IconButton(
                onClick = {
                    favoriteViewModel.removeFavorite(favorite.itemId)
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = "取消收藏",
                    tint = Color.Red
                )
            }
        }
    }
}

/**
 * 空状态提示
 */
@Composable
fun EmptyState(message: String) {
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
                color = TextSecondary,
                fontSize = 14.sp
            )
        }
    }
}
