package com.example.trip_planner.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.trip_planner.data.local.entity.FavoriteEntity
import com.example.trip_planner.data.local.entity.FavoriteType
import com.example.trip_planner.network.model.HotelInfoDto
import com.example.trip_planner.network.model.PlanHotel
import com.example.trip_planner.network.model.RestaurantInfoDto
import com.example.trip_planner.network.model.SpotInfo
import com.example.trip_planner.network.model.HotelDetailInfo
import com.example.trip_planner.network.model.AttractionDetailInfo
import com.example.trip_planner.network.model.RestaurantDetailInfo
import com.example.trip_planner.ui.theme.AppColors
import com.example.trip_planner.ui.theme.LocalAppColors
import com.example.trip_planner.viewModel.DetailState
import com.example.trip_planner.viewModel.DetailViewModel
import com.example.trip_planner.viewModel.FavoriteViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch

sealed class DetailType {
    abstract val data: Any
    data class HotelDetail(override val data: Any) : DetailType()
    data class AttractionDetail(override val data: Any) : DetailType()
    data class RestaurantDetail(override val data: Any) : DetailType()
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun DetailScreen(
    detailType: DetailType,
    onBack: () -> Unit,
    favoriteViewModel: FavoriteViewModel = viewModel(),
    detailViewModel: DetailViewModel = viewModel()
) {
    val favorites by favoriteViewModel.allFavorites.collectAsState()
    val detailState by detailViewModel.detailState.collectAsState()
    val scrollState = rememberScrollState()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    var isRefreshing by remember { mutableStateOf(false) }
    val appColors = LocalAppColors.current

    val pullRefreshState = rememberPullRefreshState(
        refreshing = isRefreshing,
        onRefresh = {
            isRefreshing = true
            when (detailType) {
                is DetailType.HotelDetail -> {
                    val hotel = detailType.data
                    if (hotel is HotelInfoDto) {
                        detailViewModel.clearCache(hotel.name)
                        detailViewModel.loadHotelDetail(hotel.name, hotel.latitude, hotel.longitude)
                    } else if (hotel is PlanHotel) {
                        detailViewModel.clearCache(hotel.name)
                        detailViewModel.loadHotelDetail(hotel.name, hotel.latitude, hotel.longitude)
                    }
                }
                is DetailType.AttractionDetail -> {
                    val spot = detailType.data
                    if (spot is SpotInfo) {
                        detailViewModel.clearCache(spot.name)
                        detailViewModel.loadAttractionDetail(spot.name, spot.latitude, spot.longitude)
                    }
                }
                is DetailType.RestaurantDetail -> {
                    val restaurant = detailType.data
                    if (restaurant is RestaurantInfoDto) {
                        detailViewModel.clearCache(restaurant.name)
                        detailViewModel.loadRestaurantDetail(restaurant.name, restaurant.latitude, restaurant.longitude)
                    }
                }
            }
        }
    )

    LaunchedEffect(detailState) {
        if (detailState is DetailState.HotelSuccess || 
            detailState is DetailState.AttractionSuccess || 
            detailState is DetailState.RestaurantSuccess ||
            detailState is DetailState.Error) {
            isRefreshing = false
        }
    }

    val onFavoriteAction: (Boolean, String, String) -> Unit = { isFav, itemName, actionType ->
        coroutineScope.launch {
            val message = if (isFav) "已取消收藏 $itemName" else "已收藏 $itemName"
            snackbarHostState.showSnackbar(message)
        }
    }

    LaunchedEffect(detailType) {
        when (detailType) {
            is DetailType.HotelDetail -> {
                val hotel = detailType.data
                if (hotel is HotelInfoDto) {
                    detailViewModel.loadHotelDetail(hotel.name, hotel.latitude, hotel.longitude)
                } else if (hotel is PlanHotel) {
                    detailViewModel.loadHotelDetail(hotel.name, hotel.latitude, hotel.longitude)
                }
            }
            is DetailType.AttractionDetail -> {
                val spot = detailType.data
                if (spot is SpotInfo) {
                    detailViewModel.loadAttractionDetail(spot.name, spot.latitude, spot.longitude)
                }
            }
            is DetailType.RestaurantDetail -> {
                val restaurant = detailType.data
                if (restaurant is RestaurantInfoDto) {
                    detailViewModel.loadRestaurantDetail(restaurant.name, restaurant.latitude, restaurant.longitude)
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        when (detailType) {
                            is DetailType.HotelDetail -> "酒店详情"
                            is DetailType.AttractionDetail -> "景点详情"
                            is DetailType.RestaurantDetail -> "餐厅详情"
                        },
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = appColors.brandTeal,
                    titleContentColor = appColors.cardBackground,
                    navigationIconContentColor = appColors.cardBackground
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .pullRefresh(pullRefreshState)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(appColors.softBackground)
                    .verticalScroll(scrollState)
            ) {
                when (detailState) {
                    is DetailState.Loading -> {
                        SkeletonLoadingContent(appColors = appColors)
                    }
                    is DetailState.Error -> {
                        ErrorContent(
                            message = (detailState as DetailState.Error).message,
                            onRetry = {
                                when (detailType) {
                                    is DetailType.HotelDetail -> {
                                        val hotel = detailType.data
                                        if (hotel is HotelInfoDto) {
                                            detailViewModel.loadHotelDetail(hotel.name, hotel.latitude, hotel.longitude)
                                        } else if (hotel is PlanHotel) {
                                            detailViewModel.loadHotelDetail(hotel.name, hotel.latitude, hotel.longitude)
                                        }
                                    }
                                    is DetailType.AttractionDetail -> {
                                        val spot = detailType.data
                                        if (spot is SpotInfo) {
                                            detailViewModel.loadAttractionDetail(spot.name, spot.latitude, spot.longitude)
                                        }
                                    }
                                    is DetailType.RestaurantDetail -> {
                                        val restaurant = detailType.data
                                        if (restaurant is RestaurantInfoDto) {
                                            detailViewModel.loadRestaurantDetail(restaurant.name, restaurant.latitude, restaurant.longitude)
                                        }
                                    }
                                }
                            },
                            appColors = appColors
                        )
                    }
                    is DetailState.HotelSuccess -> {
                        val hotelDetail = (detailState as DetailState.HotelSuccess).data
                        val hotel = detailType.data
                        if (hotel is HotelInfoDto) {
                            HotelDetailContent(
                                hotel = hotel,
                                detailInfo = hotelDetail,
                                favorites = favorites,
                                onFavoriteClick = { isFav ->
                                    val entity = FavoriteEntity(
                                        itemId = "HOTEL_${hotel.name}",
                                        type = FavoriteType.HOTEL.name,
                                        name = hotel.name,
                                        rating = hotelDetail.rating,
                                        price = hotelDetail.priceRange,
                                        address = hotelDetail.address,
                                        extraData = ""
                                    )
                                    if (isFav) favoriteViewModel.removeFavorite("HOTEL_${hotel.name}")
                                    else favoriteViewModel.addFavorite(entity)
                                    onFavoriteAction(isFav, hotel.name, "酒店")
                                },
                                appColors = appColors
                            )
                        } else if (hotel is PlanHotel) {
                            PlanHotelDetailContent(
                                hotel = hotel,
                                detailInfo = hotelDetail,
                                favorites = favorites,
                                onFavoriteClick = { isFav ->
                                    val entity = FavoriteEntity(
                                        itemId = "HOTEL_${hotel.name}",
                                        type = FavoriteType.HOTEL.name,
                                        name = hotel.name,
                                        rating = hotelDetail.rating,
                                        price = hotelDetail.priceRange,
                                        address = hotelDetail.address,
                                        extraData = ""
                                    )
                                    if (isFav) favoriteViewModel.removeFavorite("HOTEL_${hotel.name}")
                                    else favoriteViewModel.addFavorite(entity)
                                    onFavoriteAction(isFav, hotel.name, "酒店")
                                },
                                appColors = appColors
                            )
                        }
                    }
                    is DetailState.AttractionSuccess -> {
                        val attractionDetail = (detailState as DetailState.AttractionSuccess).data
                        val spot = detailType.data
                        if (spot is SpotInfo) {
                            AttractionDetailContent(
                                spot = spot,
                                detailInfo = attractionDetail,
                                favorites = favorites,
                                onFavoriteClick = { isFav ->
                                    val entity = FavoriteEntity(
                                        itemId = "ATTRACTION_${spot.name}",
                                        type = FavoriteType.ATTRACTION.name,
                                        name = spot.name,
                                        rating = attractionDetail.score,
                                        price = attractionDetail.ticketPrice,
                                        address = attractionDetail.address,
                                        extraData = ""
                                    )
                                    if (isFav) favoriteViewModel.removeFavorite("ATTRACTION_${spot.name}")
                                    else favoriteViewModel.addFavorite(entity)
                                    onFavoriteAction(isFav, spot.name, "景点")
                                },
                                appColors = appColors
                            )
                        }
                    }
                    is DetailState.RestaurantSuccess -> {
                        val restaurantDetail = (detailState as DetailState.RestaurantSuccess).data
                        val restaurant = detailType.data
                        if (restaurant is RestaurantInfoDto) {
                            RestaurantDetailContent(
                                restaurant = restaurant,
                                detailInfo = restaurantDetail,
                                favorites = favorites,
                                onFavoriteClick = { isFav ->
                                    val entity = FavoriteEntity(
                                        itemId = "RESTAURANT_${restaurant.name}",
                                        type = FavoriteType.RESTAURANT.name,
                                        name = restaurant.name,
                                        rating = restaurantDetail.score,
                                        price = restaurantDetail.avgPrice,
                                        address = restaurantDetail.address,
                                        extraData = ""
                                    )
                                    if (isFav) favoriteViewModel.removeFavorite("RESTAURANT_${restaurant.name}")
                                    else favoriteViewModel.addFavorite(entity)
                                    onFavoriteAction(isFav, restaurant.name, "餐厅")
                                },
                                appColors = appColors
                            )
                        }
                    }
                }
            }
            PullRefreshIndicator(
                refreshing = isRefreshing,
                state = pullRefreshState,
                modifier = Modifier.align(Alignment.TopCenter)
            )
        }
    }
}

@Composable
fun SkeletonLoadingContent(appColors: AppColors = LocalAppColors.current) {
    Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
        repeat(3) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .background(appColors.cardBackground.copy(alpha = 0.5f))
            )
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
fun ErrorContent(
    message: String,
    onRetry: () -> Unit,
    appColors: AppColors = LocalAppColors.current
) {
    Column(
        modifier = Modifier.padding(32.dp).fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("⚠️", fontSize = 48.sp)
        Spacer(modifier = Modifier.height(16.dp))
        Text(message, color = appColors.textSecondary, fontSize = 14.sp)
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(containerColor = appColors.brandTeal)
        ) {
            Text("重试")
        }
    }
}

@Composable
fun HotelDetailContent(
    hotel: HotelInfoDto,
    detailInfo: HotelDetailInfo,
    favorites: List<FavoriteEntity>,
    onFavoriteClick: (Boolean) -> Unit,
    appColors: AppColors = LocalAppColors.current
) {
    val favoriteId = "HOTEL_${hotel.name}"
    val isFavorite = favorites.any { it.itemId == favoriteId }

    Column {
        Column(modifier = Modifier.padding(24.dp)) {
            Text(hotel.name, fontWeight = FontWeight.Bold, fontSize = 24.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.LocationOn, contentDescription = null, tint = appColors.textSecondary, modifier = Modifier.size(16.dp))
                Text(detailInfo.address.ifEmpty { hotel.address }, color = appColors.textSecondary, fontSize = 14.sp, modifier = Modifier.padding(start = 4.dp))
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                if (detailInfo.rating.isNotEmpty()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Star, contentDescription = null, tint = appColors.warning, modifier = Modifier.size(16.dp))
                        Text(detailInfo.rating, fontWeight = FontWeight.Medium, fontSize = 15.sp, modifier = Modifier.padding(start = 4.dp))
                    }
                }
                if (detailInfo.priceRange.isNotEmpty()) {
                    Text(detailInfo.priceRange, color = appColors.textSecondary, fontSize = 15.sp)
                }
            }
        }

        HorizontalDivider()

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = { onFavoriteClick(isFavorite) },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = if (isFavorite) appColors.error else appColors.brandTeal),
                shape = MaterialTheme.shapes.small
            ) {
                Icon(if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (isFavorite) "取消收藏" else "收藏")
            }
            if (detailInfo.phone.isNotEmpty()) {
                OutlinedButton(
                    onClick = {},
                    modifier = Modifier.weight(1f),
                    shape = MaterialTheme.shapes.small
                ) {
                    Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("拨打电话")
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (detailInfo.feature.isNotEmpty() || hotel.feature.isNotEmpty()) {
            Text("酒店特色", fontWeight = FontWeight.Medium, fontSize = 16.sp, modifier = Modifier.padding(horizontal = 24.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(detailInfo.feature.ifEmpty { hotel.feature }, color = appColors.textSecondary, fontSize = 14.sp, lineHeight = 22.sp, modifier = Modifier.padding(horizontal = 24.dp))
            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(24.dp))
        }

        if (detailInfo.facilities.isNotEmpty()) {
            Text("设施", fontWeight = FontWeight.Medium, fontSize = 16.sp, modifier = Modifier.padding(horizontal = 24.dp))
            Spacer(modifier = Modifier.height(12.dp))
            FlowLayout(modifier = Modifier.padding(horizontal = 24.dp)) {
                detailInfo.facilities.forEach { facility ->
                    Text(facility, color = appColors.textSecondary, fontSize = 13.sp, modifier = Modifier.padding(end = 12.dp, bottom = 8.dp))
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(24.dp))
        }

        if (detailInfo.roomTypes.isNotEmpty()) {
            Text("房型", fontWeight = FontWeight.Medium, fontSize = 16.sp, modifier = Modifier.padding(horizontal = 24.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(detailInfo.roomTypes, color = appColors.textSecondary, fontSize = 14.sp, lineHeight = 22.sp, modifier = Modifier.padding(horizontal = 24.dp))
            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(24.dp))
        }

        Row(horizontalArrangement = Arrangement.spacedBy(24.dp), modifier = Modifier.padding(horizontal = 24.dp)) {
            if (detailInfo.checkInTime.isNotEmpty()) {
                Column {
                    Text("入住", fontSize = 12.sp, color = appColors.textSecondary)
                    Text(detailInfo.checkInTime, fontSize = 14.sp, modifier = Modifier.padding(top = 4.dp))
                }
            }
            if (detailInfo.checkOutTime.isNotEmpty()) {
                Column {
                    Text("退房", fontSize = 12.sp, color = appColors.textSecondary)
                    Text(detailInfo.checkOutTime, fontSize = 14.sp, modifier = Modifier.padding(top = 4.dp))
                }
            }
        }

        if (detailInfo.checkInTime.isNotEmpty() || detailInfo.checkOutTime.isNotEmpty()) {
            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(24.dp))
        }

        if (detailInfo.description.isNotEmpty()) {
            Text("简介", fontWeight = FontWeight.Medium, fontSize = 16.sp, modifier = Modifier.padding(horizontal = 24.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(detailInfo.description, color = appColors.textSecondary, fontSize = 14.sp, lineHeight = 22.sp, modifier = Modifier.padding(horizontal = 24.dp))
            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun PlanHotelDetailContent(
    hotel: PlanHotel,
    detailInfo: HotelDetailInfo,
    favorites: List<FavoriteEntity>,
    onFavoriteClick: (Boolean) -> Unit,
    appColors: AppColors = LocalAppColors.current
) {
    val favoriteId = "HOTEL_${hotel.name}"
    val isFavorite = favorites.any { it.itemId == favoriteId }

    Column {
        Column(modifier = Modifier.padding(24.dp)) {
            Text(hotel.name, fontWeight = FontWeight.Bold, fontSize = 24.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.LocationOn, contentDescription = null, tint = appColors.textSecondary, modifier = Modifier.size(16.dp))
                Text(detailInfo.address.ifEmpty { hotel.address }, color = appColors.textSecondary, fontSize = 14.sp, modifier = Modifier.padding(start = 4.dp))
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                if (detailInfo.rating.isNotEmpty()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Star, contentDescription = null, tint = appColors.warning, modifier = Modifier.size(16.dp))
                        Text(detailInfo.rating, fontWeight = FontWeight.Medium, fontSize = 15.sp, modifier = Modifier.padding(start = 4.dp))
                    }
                }
                val price = if (detailInfo.priceRange.isNotEmpty()) detailInfo.priceRange else hotel.price
                if (price.isNotEmpty()) {
                    Text(price, color = appColors.textSecondary, fontSize = 15.sp)
                }
            }
        }

        HorizontalDivider()

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { onFavoriteClick(isFavorite) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            colors = ButtonDefaults.buttonColors(containerColor = if (isFavorite) appColors.error else appColors.brandTeal),
            shape = MaterialTheme.shapes.small
        ) {
            Icon(if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(if (isFavorite) "取消收藏" else "收藏")
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (hotel.advantage.isNotEmpty() || detailInfo.feature.isNotEmpty()) {
            Text("酒店特色", fontWeight = FontWeight.Medium, fontSize = 16.sp, modifier = Modifier.padding(horizontal = 24.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(detailInfo.feature.ifEmpty { hotel.advantage }, color = appColors.textSecondary, fontSize = 14.sp, lineHeight = 22.sp, modifier = Modifier.padding(horizontal = 24.dp))
            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(24.dp))
        }

        if (detailInfo.facilities.isNotEmpty()) {
            Text("设施", fontWeight = FontWeight.Medium, fontSize = 16.sp, modifier = Modifier.padding(horizontal = 24.dp))
            Spacer(modifier = Modifier.height(12.dp))
            FlowLayout(modifier = Modifier.padding(horizontal = 24.dp)) {
                detailInfo.facilities.forEach { facility ->
                    Text(facility, color = appColors.textSecondary, fontSize = 13.sp, modifier = Modifier.padding(end = 12.dp, bottom = 8.dp))
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(24.dp))
        }

        if (detailInfo.roomTypes.isNotEmpty()) {
            Text("房型", fontWeight = FontWeight.Medium, fontSize = 16.sp, modifier = Modifier.padding(horizontal = 24.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(detailInfo.roomTypes, color = appColors.textSecondary, fontSize = 14.sp, lineHeight = 22.sp, modifier = Modifier.padding(horizontal = 24.dp))
            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(24.dp))
        }

        Row(horizontalArrangement = Arrangement.spacedBy(24.dp), modifier = Modifier.padding(horizontal = 24.dp)) {
            if (detailInfo.checkInTime.isNotEmpty()) {
                Column {
                    Text("入住", fontSize = 12.sp, color = appColors.textSecondary)
                    Text(detailInfo.checkInTime, fontSize = 14.sp, modifier = Modifier.padding(top = 4.dp))
                }
            }
            if (detailInfo.checkOutTime.isNotEmpty()) {
                Column {
                    Text("退房", fontSize = 12.sp, color = appColors.textSecondary)
                    Text(detailInfo.checkOutTime, fontSize = 14.sp, modifier = Modifier.padding(top = 4.dp))
                }
            }
        }

        if (detailInfo.checkInTime.isNotEmpty() || detailInfo.checkOutTime.isNotEmpty()) {
            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(24.dp))
        }

        if (detailInfo.description.isNotEmpty()) {
            Text("简介", fontWeight = FontWeight.Medium, fontSize = 16.sp, modifier = Modifier.padding(horizontal = 24.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(detailInfo.description, color = appColors.textSecondary, fontSize = 14.sp, lineHeight = 22.sp, modifier = Modifier.padding(horizontal = 24.dp))
            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun AttractionDetailContent(
    spot: SpotInfo,
    detailInfo: AttractionDetailInfo,
    favorites: List<FavoriteEntity>,
    onFavoriteClick: (Boolean) -> Unit,
    appColors: AppColors = LocalAppColors.current
) {
    val favoriteId = "ATTRACTION_${spot.name}"
    val isFavorite = favorites.any { it.itemId == favoriteId }

    Column {
        Column(modifier = Modifier.padding(24.dp)) {
            Text(spot.name, fontWeight = FontWeight.Bold, fontSize = 24.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.LocationOn, contentDescription = null, tint = appColors.textSecondary, modifier = Modifier.size(16.dp))
                Text(detailInfo.address.ifEmpty { spot.address }, color = appColors.textSecondary, fontSize = 14.sp, modifier = Modifier.padding(start = 4.dp))
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                val rating = detailInfo.rating.ifEmpty { spot.score }
                if (rating.isNotEmpty()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Star, contentDescription = null, tint = appColors.warning, modifier = Modifier.size(16.dp))
                        Text(rating, fontWeight = FontWeight.Medium, fontSize = 15.sp, modifier = Modifier.padding(start = 4.dp))
                    }
                }
                if (detailInfo.ticketPrice.isNotEmpty()) {
                    Text(detailInfo.ticketPrice, color = appColors.textSecondary, fontSize = 15.sp)
                }
            }
        }

        HorizontalDivider()

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { onFavoriteClick(isFavorite) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            colors = ButtonDefaults.buttonColors(containerColor = if (isFavorite) appColors.error else appColors.brandTeal),
            shape = MaterialTheme.shapes.small
        ) {
            Icon(if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(if (isFavorite) "取消收藏" else "收藏")
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (detailInfo.openTime.isNotEmpty()) {
            Text("开放时间", fontWeight = FontWeight.Medium, fontSize = 16.sp, modifier = Modifier.padding(horizontal = 24.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(detailInfo.openTime, fontSize = 15.sp, modifier = Modifier.padding(horizontal = 24.dp))
            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(24.dp))
        }

        if (detailInfo.suggestion.isNotEmpty()) {
            Text("游览建议", fontWeight = FontWeight.Medium, fontSize = 16.sp, modifier = Modifier.padding(horizontal = 24.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(detailInfo.suggestion, color = appColors.textSecondary, fontSize = 14.sp, lineHeight = 22.sp, modifier = Modifier.padding(horizontal = 24.dp))
            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(24.dp))
        }

        if (detailInfo.history.isNotEmpty()) {
            Text("历史文化", fontWeight = FontWeight.Medium, fontSize = 16.sp, modifier = Modifier.padding(horizontal = 24.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(detailInfo.history, color = appColors.textSecondary, fontSize = 14.sp, lineHeight = 22.sp, modifier = Modifier.padding(horizontal = 24.dp))
            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(24.dp))
        }

        if (detailInfo.description.isNotEmpty()) {
            Text("简介", fontWeight = FontWeight.Medium, fontSize = 16.sp, modifier = Modifier.padding(horizontal = 24.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(detailInfo.description, color = appColors.textSecondary, fontSize = 14.sp, lineHeight = 22.sp, modifier = Modifier.padding(horizontal = 24.dp))
            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(24.dp))
        }

        Row(horizontalArrangement = Arrangement.spacedBy(24.dp), modifier = Modifier.padding(horizontal = 24.dp)) {
            if (detailInfo.bestTime.isNotEmpty()) {
                Column {
                    Text("最佳时间", fontSize = 12.sp, color = appColors.textSecondary)
                    Text(detailInfo.bestTime, fontSize = 14.sp, modifier = Modifier.padding(top = 4.dp))
                }
            }
            if (detailInfo.duration.isNotEmpty()) {
                Column {
                    Text("建议时长", fontSize = 12.sp, color = appColors.textSecondary)
                    Text(detailInfo.duration, fontSize = 14.sp, modifier = Modifier.padding(top = 4.dp))
                }
            }
        }

        if (detailInfo.bestTime.isNotEmpty() || detailInfo.duration.isNotEmpty()) {
            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(24.dp))
        }

        if (detailInfo.phone.isNotEmpty()) {
            Text("联系电话", fontWeight = FontWeight.Medium, fontSize = 16.sp, modifier = Modifier.padding(horizontal = 24.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(detailInfo.phone, fontSize = 15.sp, modifier = Modifier.padding(horizontal = 24.dp))
            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun RestaurantDetailContent(
    restaurant: RestaurantInfoDto,
    detailInfo: RestaurantDetailInfo,
    favorites: List<FavoriteEntity>,
    onFavoriteClick: (Boolean) -> Unit,
    appColors: AppColors = LocalAppColors.current
) {
    val favoriteId = "RESTAURANT_${restaurant.name}"
    val isFavorite = favorites.any { it.itemId == favoriteId }

    Column {
        Column(modifier = Modifier.padding(24.dp)) {
            Text(restaurant.name, fontWeight = FontWeight.Bold, fontSize = 24.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.LocationOn, contentDescription = null, tint = appColors.textSecondary, modifier = Modifier.size(16.dp))
                Text(detailInfo.address.ifEmpty { restaurant.address }, color = appColors.textSecondary, fontSize = 14.sp, modifier = Modifier.padding(start = 4.dp))
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                val rating = detailInfo.rating.ifEmpty { detailInfo.score }.ifEmpty { restaurant.score }
                if (rating.isNotEmpty()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Star, contentDescription = null, tint = appColors.warning, modifier = Modifier.size(16.dp))
                        Text(rating, fontWeight = FontWeight.Medium, fontSize = 15.sp, modifier = Modifier.padding(start = 4.dp))
                    }
                }
                if (detailInfo.avgPrice.isNotEmpty()) {
                    Text(detailInfo.avgPrice, color = appColors.textSecondary, fontSize = 15.sp)
                }
            }
        }

        HorizontalDivider()

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { onFavoriteClick(isFavorite) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            colors = ButtonDefaults.buttonColors(containerColor = if (isFavorite) appColors.error else appColors.brandTeal),
            shape = MaterialTheme.shapes.small
        ) {
            Icon(if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(if (isFavorite) "取消收藏" else "收藏")
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (detailInfo.openTime.isNotEmpty()) {
            Text("营业时间", fontWeight = FontWeight.Medium, fontSize = 16.sp, modifier = Modifier.padding(horizontal = 24.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(detailInfo.openTime, fontSize = 15.sp, modifier = Modifier.padding(horizontal = 24.dp))
            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(24.dp))
        }

        if (detailInfo.featureDish.isNotEmpty()) {
            Text("特色菜品", fontWeight = FontWeight.Medium, fontSize = 16.sp, modifier = Modifier.padding(horizontal = 24.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(detailInfo.featureDish, color = appColors.textSecondary, fontSize = 14.sp, lineHeight = 22.sp, modifier = Modifier.padding(horizontal = 24.dp))
            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(24.dp))
        }

        if (detailInfo.cuisineType.isNotEmpty()) {
            Text("菜系类型", fontWeight = FontWeight.Medium, fontSize = 16.sp, modifier = Modifier.padding(horizontal = 24.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(detailInfo.cuisineType, color = appColors.textSecondary, fontSize = 14.sp, lineHeight = 22.sp, modifier = Modifier.padding(horizontal = 24.dp))
            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(24.dp))
        }

        if (detailInfo.description.isNotEmpty()) {
            Text("简介", fontWeight = FontWeight.Medium, fontSize = 16.sp, modifier = Modifier.padding(horizontal = 24.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(detailInfo.description, color = appColors.textSecondary, fontSize = 14.sp, lineHeight = 22.sp, modifier = Modifier.padding(horizontal = 24.dp))
            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(24.dp))
        }

        if (detailInfo.seats.isNotEmpty()) {
            Text("座位信息", fontWeight = FontWeight.Medium, fontSize = 16.sp, modifier = Modifier.padding(horizontal = 24.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(detailInfo.seats, color = appColors.textSecondary, fontSize = 14.sp, lineHeight = 22.sp, modifier = Modifier.padding(horizontal = 24.dp))
            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(24.dp))
        }

        if (detailInfo.phone.isNotEmpty()) {
            Text("联系电话", fontWeight = FontWeight.Medium, fontSize = 16.sp, modifier = Modifier.padding(horizontal = 24.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(detailInfo.phone, fontSize = 15.sp, modifier = Modifier.padding(horizontal = 24.dp))
            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun FlowLayout(
    modifier: Modifier = Modifier,
    content: @androidx.compose.runtime.Composable () -> Unit
) {
    Layout(
        modifier = modifier,
        content = content
    ) { measurables, constraints ->
        val rowWidth = constraints.maxWidth
        val placeables = mutableListOf<Placeable>()
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
