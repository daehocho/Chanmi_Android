package com.chanmi.app.ui.prayers

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel

private const val MIN_FONT_SIZE = 14f
private const val MAX_FONT_SIZE = 32f
private const val FONT_STEP = 2f

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrayerDetailScreen(
    prayerId: String,
    onNavigateBack: () -> Unit,
    viewModel: PrayersViewModel = hiltViewModel()
) {
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val prayer = remember(categories, prayerId) {
        categories.flatMap { it.prayers }.find { it.id == prayerId }
    }
    val fontSize by viewModel.prayerFontSize.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(prayer?.title ?: "") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "뒤로 가기"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            BottomAppBar(
                containerColor = MaterialTheme.colorScheme.surface,
                actions = {
                    TextButton(
                        onClick = { if (fontSize > MIN_FONT_SIZE) viewModel.updatePrayerFontSize(fontSize - FONT_STEP) },
                        enabled = fontSize > MIN_FONT_SIZE,
                        modifier = Modifier.semantics {
                            contentDescription = "글씨 작게, 현재 ${fontSize.toInt()}포인트"
                        }
                    ) {
                        Text("가", style = MaterialTheme.typography.bodySmall)
                    }

                    Text(
                        text = "${fontSize.toInt()}pt",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )

                    TextButton(
                        onClick = { if (fontSize < MAX_FONT_SIZE) viewModel.updatePrayerFontSize(fontSize + FONT_STEP) },
                        enabled = fontSize < MAX_FONT_SIZE,
                        modifier = Modifier.semantics {
                            contentDescription = "글씨 크게, 현재 ${fontSize.toInt()}포인트"
                        }
                    ) {
                        Text("가", style = MaterialTheme.typography.bodyLarge)
                    }
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        if (prayer != null) {
            Text(
                text = prayer.content,
                style = TextStyle(
                    fontSize = fontSize.sp,
                    lineHeight = (fontSize + 6).sp
                ),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
                    .fillMaxWidth()
                    .semantics { contentDescription = prayer.content }
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "기도문을 찾을 수 없습니다",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
