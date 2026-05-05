package com.example.trip_planner.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties

/**
 * 城市选择器（极简现代风）
 */
@Composable
fun CitySelector(
    selectedCity: String,
    onCitySelected: (String) -> Unit,
    appColors: com.example.trip_planner.ui.theme.AppColors
) {
    var searchQuery by remember { mutableStateOf("") }
    var showDropdown by remember { mutableStateOf(false) }

    val allCities = remember {
        listOf(
            "成都", "北京", "上海", "杭州", "西安", "重庆",
            "厦门", "丽江", "三亚", "桂林", "广州", "深圳",
            "南京", "苏州", "青岛", "大连", "昆明", "拉萨",
            "张家界", "九寨沟"
        )
    }

    val filteredCities = remember(searchQuery, allCities) {
        if (searchQuery.isBlank()) {
            allCities
        } else {
            allCities.filter { city ->
                city.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { 
                    showDropdown = true
                }
        ) {
            OutlinedTextField(
                value = if (selectedCity.isNotEmpty() && searchQuery.isEmpty()) selectedCity else searchQuery,
                onValueChange = { 
                    searchQuery = it
                    showDropdown = it.isNotEmpty()
                },
                placeholder = { Text("搜索或选择城市", color = appColors.textSecondary, fontSize = 13.sp) },
                leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null, tint = appColors.brandTeal, modifier = Modifier.size(18.dp)) },
                trailingIcon = {
                    if (selectedCity.isNotEmpty() && searchQuery.isEmpty()) {
                        IconButton(onClick = { 
                            onCitySelected("")
                            showDropdown = true
                        }) {
                            Icon(Icons.Default.Close, contentDescription = "清除", tint = appColors.textSecondary, modifier = Modifier.size(16.dp))
                        }
                    }
                },
                shape = MaterialTheme.shapes.small,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = appColors.divider,
                    unfocusedBorderColor = appColors.divider,
                    focusedContainerColor = appColors.cardBackground,
                    unfocusedContainerColor = appColors.cardBackground
                ),
                singleLine = true,
                textStyle = TextStyle(fontSize = 13.sp),
                readOnly = true,
                enabled = false
            )
        }

        if (showDropdown && filteredCities.isNotEmpty()) {
            Popup(
                alignment = Alignment.TopStart,
                properties = PopupProperties(focusable = true, dismissOnBackPress = true, dismissOnClickOutside = true),
                onDismissRequest = { showDropdown = false }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp)
                        .offset(y = 4.dp)
                        .background(appColors.cardBackground)
                ) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 200.dp)
                            .padding(8.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        items(filteredCities) { city ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onCitySelected(city)
                                        searchQuery = ""
                                        showDropdown = false
                                    }
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    city,
                                    fontSize = 14.sp,
                                    color = if (city == selectedCity) appColors.brandTeal else appColors.textPrimary
                                )
                                if (city == selectedCity) {
                                    Spacer(modifier = Modifier.weight(1f))
                                    Icon(
                                        Icons.Default.LocationOn,
                                        contentDescription = null,
                                        tint = appColors.brandTeal,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
