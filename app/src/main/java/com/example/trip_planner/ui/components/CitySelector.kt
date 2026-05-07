package com.example.trip_planner.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties

/**
 * 城市选择器（支持输入和选择）
 */
@Composable
fun CitySelector(
    selectedCity: String,
    onCitySelected: (String) -> Unit,
    appColors: com.example.trip_planner.ui.theme.AppColors
) {
    var inputText by remember { mutableStateOf(selectedCity) }
    var showDropdown by remember { mutableStateOf(false) }

    // 同步外部选中值
    LaunchedEffect(selectedCity) {
        inputText = selectedCity
    }

    val allCities = remember {
        listOf(
            "成都", "北京", "上海", "杭州", "西安", "重庆",
            "厦门", "丽江", "三亚", "桂林", "广州", "深圳",
            "南京", "苏州", "青岛", "大连", "昆明", "拉萨",
            "张家界", "九寨沟"
        )
    }

    val filteredCities = remember(inputText, allCities) {
        if (inputText.isBlank()) {
            allCities
        } else {
            allCities.filter { city ->
                city.contains(inputText, ignoreCase = true)
            }
        }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = inputText,
            onValueChange = { newText ->
                inputText = newText
                showDropdown = newText.isNotEmpty()
                onCitySelected(newText)
            },
            placeholder = { Text("搜索或输入城市", color = appColors.textSecondary, fontSize = 13.sp) },
            leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null, tint = appColors.brandTeal, modifier = Modifier.size(18.dp)) },
            trailingIcon = {
                if (inputText.isNotEmpty()) {
                    IconButton(onClick = {
                        inputText = ""
                        showDropdown = false
                        onCitySelected("")
                    }) {
                        Icon(Icons.Default.Close, contentDescription = "清除", tint = appColors.textSecondary, modifier = Modifier.size(16.dp))
                    }
                }
            },
            shape = MaterialTheme.shapes.small,
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = appColors.brandTeal,
                unfocusedBorderColor = appColors.divider,
                focusedContainerColor = appColors.cardBackground,
                unfocusedContainerColor = appColors.cardBackground
            ),
            singleLine = true,
            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 13.sp)
        )

        if (showDropdown && inputText.isNotEmpty() && filteredCities.isNotEmpty()) {
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
                                        inputText = city
                                        onCitySelected(city)
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
                                        Icons.Default.Check,
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
