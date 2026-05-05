package com.example.trip_planner.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.trip_planner.ui.components.PackingListSection
import com.example.trip_planner.ui.theme.LocalAppColors
import com.example.trip_planner.viewModel.PackingListViewModel
import com.example.trip_planner.viewModel.PackingTemplateViewModel
import kotlinx.coroutines.launch

/**
 * 打包清单页面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PackingListScreen(
    tripId: String,
    tripName: String = "",
    userId: Long = 0,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    packingViewModel: PackingListViewModel = viewModel(),
    templateViewModel: PackingTemplateViewModel = viewModel()
) {
    val appColors = LocalAppColors.current
    val items by packingViewModel.items.collectAsState()
    val packedCount by packingViewModel.packedCount.collectAsState()
    val totalCount by packingViewModel.totalItemCount.collectAsState()
    val scope = rememberCoroutineScope()

    LaunchedEffect(tripId) {
        if (tripId.isNotEmpty()) {
            packingViewModel.setTripId(tripId)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("打包清单", fontSize = 16.sp)
                        if (tripName.isNotEmpty()) {
                            Text(tripName, fontSize = 11.sp, color = appColors.textSecondary)
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = appColors.softBackground)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(appColors.softBackground)
        ) {
            PackingListSection(
                items = items,
                packedCount = packedCount,
                totalCount = totalCount,
                onAddItem = { name, category, tags, quantity ->
                    packingViewModel.addItem(tripId, userId, name, category, tags, quantity)
                },
                onTogglePacked = { id, isPacked ->
                    packingViewModel.togglePacked(id, isPacked)
                },
                onDeleteItem = { id ->
                    packingViewModel.deleteItem(id)
                },
                onAddDefaults = {
                    packingViewModel.addDefaultItems(tripId, userId)
                },
                onResetAll = {
                    items.forEach { item ->
                        if (item.isPacked) {
                            packingViewModel.togglePacked(item.id, false)
                        }
                    }
                },
                onSaveTemplate = { name ->
                    val templateItems = items.map { item ->
                        com.example.trip_planner.data.local.entity.PackingTemplateItem(
                            name = item.name,
                            category = item.category,
                            tags = item.tags,
                            quantity = item.quantity
                        )
                    }
                    templateViewModel.saveTemplate(name, templateItems)
                },
                onLoadTemplate = { templateId ->
                    scope.launch {
                        val templateItems = templateViewModel.getTemplateItems(templateId)
                        templateItems?.forEach { templateItem ->
                            packingViewModel.addItem(
                                tripId,
                                userId,
                                templateItem.name,
                                templateItem.category,
                                templateItem.tags,
                                templateItem.quantity
                            )
                        }
                    }
                },
                appColors = appColors,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}
