package com.chanmi.app.ui.prayers.reminder

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.chanmi.app.data.model.Prayer
import com.chanmi.app.data.model.PrayerReminder
import com.chanmi.app.ui.theme.chanmiColors

private val weekdayLabels = listOf("일", "월", "화", "수", "목", "금", "토")
private val weekdayValues = listOf(1, 2, 3, 4, 5, 6, 7)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrayerReminderEditScreen(
    reminderId: String?,
    onSave: () -> Unit,
    onCancel: () -> Unit,
    viewModel: PrayerReminderViewModel = hiltViewModel()
) {
    val availablePrayers by viewModel.availablePrayers.collectAsStateWithLifecycle()

    // 편집 상태
    var selectedPrayerId by rememberSaveable { mutableStateOf("") }
    var selectedWeekdays by rememberSaveable { mutableStateOf(emptySet<Int>()) }
    var showPrayerSelection by rememberSaveable { mutableStateOf(false) }
    var existingReminder by remember { mutableStateOf<PrayerReminder?>(null) }
    var isInitialized by rememberSaveable { mutableStateOf(false) }

    val isEditing = reminderId != null

    // 기존 알림 데이터 로드 (편집 모드)
    LaunchedEffect(reminderId, availablePrayers) {
        if (isInitialized) return@LaunchedEffect
        if (reminderId != null) {
            val reminder = viewModel.getById(reminderId)
            if (reminder != null) {
                existingReminder = reminder
                selectedPrayerId = reminder.prayerId
                selectedWeekdays = reminder.weekdaySet()
            }
        } else if (availablePrayers.isNotEmpty() && selectedPrayerId.isEmpty()) {
            selectedPrayerId = availablePrayers.first().id
        }
        isInitialized = true
    }

    // TimePicker 상태 — key()로 existingReminder 로드 시 재생성하여 조건부 remember 회피
    val editTimePickerState = key(existingReminder?.id) {
        rememberTimePickerState(
            initialHour = existingReminder?.hour ?: 9,
            initialMinute = existingReminder?.minute ?: 0,
            is24Hour = false
        )
    }

    val selectedPrayerTitle = remember(availablePrayers, selectedPrayerId) {
        availablePrayers.find { it.id == selectedPrayerId }?.title ?: "선택"
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditing) "알림 편집" else "알림 추가") },
                navigationIcon = {
                    IconButton(onClick = onCancel) {
                        Icon(Icons.Default.Close, contentDescription = "취소")
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            val prayer = availablePrayers.find { it.id == selectedPrayerId }
                            if (prayer != null) {
                                val weekdaysStr = PrayerReminder.weekdaysToString(selectedWeekdays)
                                val reminder = if (isEditing && existingReminder != null) {
                                    existingReminder!!.copy(
                                        prayerId = prayer.id,
                                        prayerTitle = prayer.title,
                                        hour = editTimePickerState.hour,
                                        minute = editTimePickerState.minute,
                                        weekdays = weekdaysStr
                                    )
                                } else {
                                    PrayerReminder(
                                        prayerId = prayer.id,
                                        prayerTitle = prayer.title,
                                        hour = editTimePickerState.hour,
                                        minute = editTimePickerState.minute,
                                        weekdays = weekdaysStr
                                    )
                                }
                                viewModel.saveReminder(reminder)
                                onSave()
                            }
                        },
                        enabled = availablePrayers.isNotEmpty() && selectedPrayerId.isNotEmpty()
                    ) {
                        Text(
                            "저장",
                            fontWeight = FontWeight.SemiBold,
                            color = if (availablePrayers.isNotEmpty() && selectedPrayerId.isNotEmpty())
                                MaterialTheme.chanmiColors.goldAccent
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // MARK: - 기도문 선택
            SectionTitle("기도문")
            if (availablePrayers.isEmpty()) {
                Text(
                    "기도문을 불러오는 중...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showPrayerSelection = true }
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "기도문 선택",
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            selectedPrayerTitle,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Icon(
                            Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // MARK: - 시간 선택
            SectionTitle("시간")
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    TimePicker(
                        state = editTimePickerState,
                        colors = TimePickerDefaults.colors(
                            selectorColor = MaterialTheme.chanmiColors.goldAccent,
                            clockDialSelectedContentColor = Color.White
                        )
                    )
                }
            }

            // MARK: - 반복 요일
            WeekdaySection(
                selectedWeekdays = selectedWeekdays,
                onToggleWeekday = { weekday ->
                    selectedWeekdays = if (weekday in selectedWeekdays) {
                        selectedWeekdays - weekday
                    } else {
                        selectedWeekdays + weekday
                    }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    // 기도문 선택 BottomSheet
    if (showPrayerSelection) {
        PrayerSelectionBottomSheet(
            prayers = availablePrayers,
            selectedPrayerId = selectedPrayerId,
            onSelect = { prayerId ->
                selectedPrayerId = prayerId
                showPrayerSelection = false
            },
            onDismiss = { showPrayerSelection = false }
        )
    }
}

// MARK: - 섹션 제목

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

// MARK: - 요일 선택 섹션

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun WeekdaySection(
    selectedWeekdays: Set<Int>,
    onToggleWeekday: (Int) -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SectionTitle("반복 요일")
            Text(
                text = when {
                    selectedWeekdays.isEmpty() || selectedWeekdays.size == 7 -> "매일"
                    else -> selectedWeekdays.sorted().map { weekdayLabels[it - 1] }.joinToString(", ")
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            weekdayValues.zip(weekdayLabels).forEach { (value, label) ->
                val selected = value in selectedWeekdays
                FilterChip(
                    selected = selected,
                    onClick = { onToggleWeekday(value) },
                    label = { Text(label) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.chanmiColors.goldAccent,
                        selectedLabelColor = Color.White
                    ),
                    modifier = Modifier.semantics {
                        contentDescription = "${label}요일"
                        stateDescription = if (selected) "선택됨" else "선택 안 됨"
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "선택하지 않으면 매일 반복됩니다",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// MARK: - 기도문 선택 BottomSheet

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PrayerSelectionBottomSheet(
    prayers: List<Prayer>,
    selectedPrayerId: String,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Text(
            "기도문 선택",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
        HorizontalDivider()
        LazyColumn {
            items(prayers, key = { it.id }) { prayer ->
                ListItem(
                    headlineContent = { Text(prayer.title) },
                    trailingContent = {
                        if (prayer.id == selectedPrayerId) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = "선택됨",
                                tint = MaterialTheme.chanmiColors.goldAccent
                            )
                        }
                    },
                    modifier = Modifier
                        .clickable { onSelect(prayer.id) }
                        .semantics {
                            stateDescription = if (prayer.id == selectedPrayerId) "선택됨" else ""
                        }
                )
            }
        }
        Spacer(modifier = Modifier.height(32.dp))
    }
}
