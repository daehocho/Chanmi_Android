package com.chanmi.app.ui.calendar

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.chanmi.app.data.model.DailyRecordWithDetails
import com.chanmi.app.data.model.GoodDeed
import com.chanmi.app.data.model.MysteryType
import com.chanmi.app.data.model.RosaryEntry
import com.chanmi.app.ui.theme.chanmiColors
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

// ===== 태블릿 인라인 상세 뷰 =====

@Composable
fun DayDetailInlineView(
    date: LocalDate,
    record: DailyRecordWithDetails?,
    viewModel: CalendarViewModel,
    modifier: Modifier = Modifier
) {
    var showAddGoodDeed by remember { mutableStateOf(false) }
    var showAddRosary by remember { mutableStateOf(false) }
    var editingDeed by remember { mutableStateOf<GoodDeed?>(null) }

    val dateString = remember(date) {
        date.format(DateTimeFormatter.ofPattern("M월 d일 EEEE", Locale.KOREAN))
    }

    Column(modifier = modifier.fillMaxSize()) {
        Text(
            text = dateString,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(16.dp)
        )

        DayDetailContent(
            record = record,
            onAddRosary = { showAddRosary = true },
            onDeleteRosary = viewModel::deleteRosaryEntry,
            onAddGoodDeed = { showAddGoodDeed = true },
            onEditGoodDeed = { editingDeed = it },
            onDeleteGoodDeed = viewModel::deleteGoodDeed,
            modifier = Modifier.weight(1f)
        )
    }

    if (showAddGoodDeed) {
        GoodDeedFormDialog(
            onDismiss = { showAddGoodDeed = false },
            onSave = { content, category ->
                viewModel.addGoodDeed(date, content, category)
                showAddGoodDeed = false
            }
        )
    }

    editingDeed?.let { deed ->
        GoodDeedFormDialog(
            editingDeed = deed,
            onDismiss = { editingDeed = null },
            onSave = { content, category ->
                viewModel.updateGoodDeed(deed, content, category)
                editingDeed = null
            }
        )
    }

    if (showAddRosary) {
        AddRosaryDialog(
            onDismiss = { showAddRosary = false },
            onSave = { mysteryType ->
                viewModel.addRosaryEntry(date, mysteryType)
                showAddRosary = false
            }
        )
    }
}

// ===== 폰 하단 컴팩트 상세 뷰 =====

@Composable
fun CompactDayDetailView(
    date: LocalDate,
    record: DailyRecordWithDetails?,
    viewModel: CalendarViewModel,
    modifier: Modifier = Modifier
) {
    var showAddGoodDeed by remember { mutableStateOf(false) }
    var showAddRosary by remember { mutableStateOf(false) }
    var editingDeed by remember { mutableStateOf<GoodDeed?>(null) }

    val dateString = remember(date) {
        date.format(DateTimeFormatter.ofPattern("M월 d일 EEEE", Locale.KOREAN))
    }

    Column(modifier = modifier) {
        HorizontalDivider()

        Text(
            text = dateString,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .padding(vertical = 8.dp)
        )

        DayDetailContent(
            record = record,
            onAddRosary = { showAddRosary = true },
            onDeleteRosary = viewModel::deleteRosaryEntry,
            onAddGoodDeed = { showAddGoodDeed = true },
            onEditGoodDeed = { editingDeed = it },
            onDeleteGoodDeed = viewModel::deleteGoodDeed,
            modifier = Modifier.weight(1f)
        )
    }

    if (showAddGoodDeed) {
        GoodDeedFormDialog(
            onDismiss = { showAddGoodDeed = false },
            onSave = { content, category ->
                viewModel.addGoodDeed(date, content, category)
                showAddGoodDeed = false
            }
        )
    }

    editingDeed?.let { deed ->
        GoodDeedFormDialog(
            editingDeed = deed,
            onDismiss = { editingDeed = null },
            onSave = { content, category ->
                viewModel.updateGoodDeed(deed, content, category)
                editingDeed = null
            }
        )
    }

    if (showAddRosary) {
        AddRosaryDialog(
            onDismiss = { showAddRosary = false },
            onSave = { mysteryType ->
                viewModel.addRosaryEntry(date, mysteryType)
                showAddRosary = false
            }
        )
    }
}

// ===== 공통 상세 콘텐츠 =====

@Composable
private fun DayDetailContent(
    record: DailyRecordWithDetails?,
    onAddRosary: () -> Unit,
    onDeleteRosary: (RosaryEntry) -> Unit,
    onAddGoodDeed: () -> Unit,
    onEditGoodDeed: (GoodDeed) -> Unit,
    onDeleteGoodDeed: (GoodDeed) -> Unit,
    modifier: Modifier = Modifier
) {
    val chanmiColors = MaterialTheme.chanmiColors
    val timeFormatter = remember {
        DateTimeFormatter.ofPattern("a h:mm", Locale.KOREAN)
    }

    LazyColumn(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        // 묵주기도 섹션 헤더
        item {
            SectionHeader("묵주기도")
        }

        val rosaryEntries = record?.rosaryEntries ?: emptyList()
        if (rosaryEntries.isEmpty()) {
            item {
                Text(
                    "묵주기도 기록이 없습니다",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        } else {
            items(rosaryEntries, key = { it.id }) { entry ->
                val mysteryName = try {
                    MysteryType.fromKey(entry.mysteryType).displayName
                } catch (_: Exception) {
                    entry.mysteryType
                }
                val completedTime = Instant.ofEpochMilli(entry.completedAt)
                    .atZone(ZoneId.systemDefault())
                    .toLocalTime()
                    .format(timeFormatter)

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                        .semantics { contentDescription = "$mysteryName 완료" },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Filled.AutoAwesome,
                        contentDescription = null,
                        tint = chanmiColors.rosaryIndicator,
                        modifier = Modifier.padding(end = 12.dp)
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(mysteryName, style = MaterialTheme.typography.bodyLarge)
                        Text(
                            completedTime,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = { onDeleteRosary(entry) }) {
                        Icon(
                            Icons.Filled.Delete,
                            contentDescription = "삭제",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        item {
            TextButton(onClick = onAddRosary) {
                Icon(Icons.Filled.AddCircleOutline, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("묵주기도 수동 추가")
            }
        }

        // 선행 기록 섹션 헤더
        item {
            SectionHeader(
                title = "선행 기록",
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        val goodDeeds = record?.goodDeeds ?: emptyList()
        if (goodDeeds.isEmpty()) {
            item {
                Text(
                    "선행 기록이 없습니다",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        } else {
            items(goodDeeds, key = { it.id }) { deed ->
                val createdTime = Instant.ofEpochMilli(deed.createdAt)
                    .atZone(ZoneId.systemDefault())
                    .toLocalTime()
                    .format(timeFormatter)

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onEditGoodDeed(deed) }
                        .padding(vertical = 6.dp)
                        .semantics {
                            contentDescription = "${goodDeedCategoryName(deed.category)}, ${deed.content}"
                        },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        goodDeedCategoryIcon(deed.category),
                        contentDescription = null,
                        tint = chanmiColors.goodDeedIndicator,
                        modifier = Modifier.padding(end = 12.dp)
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(deed.content, style = MaterialTheme.typography.bodyLarge)
                        Text(
                            goodDeedCategoryName(deed.category),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        createdTime,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    IconButton(onClick = { onDeleteGoodDeed(deed) }) {
                        Icon(
                            Icons.Filled.Delete,
                            contentDescription = "삭제",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        item {
            TextButton(onClick = onAddGoodDeed) {
                Icon(Icons.Filled.AddCircleOutline, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("선행 추가")
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String, modifier: Modifier = Modifier) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier.padding(vertical = 8.dp)
    )
}

// ===== 유틸리티 =====

fun goodDeedCategoryIcon(category: String): ImageVector = when (category) {
    "service" -> Icons.Filled.VolunteerActivism
    "donation" -> Icons.Filled.Favorite
    "prayer" -> Icons.Filled.AutoAwesome
    "forgiveness" -> Icons.Filled.Favorite
    "other" -> Icons.Filled.Star
    else -> Icons.Filled.Star
}

fun goodDeedCategoryName(category: String): String = when (category) {
    "service" -> "봉사"
    "donation" -> "기부"
    "prayer" -> "기도"
    "forgiveness" -> "용서"
    "other" -> "기타"
    else -> "기타"
}
