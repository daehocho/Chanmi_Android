package com.chanmi.app.ui.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.chanmi.app.data.model.DailyRecordWithDetails
import com.chanmi.app.ui.theme.chanmiColors
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    widthSizeClass: WindowWidthSizeClass = WindowWidthSizeClass.Compact,
    viewModel: CalendarViewModel = hiltViewModel()
) {
    val currentMonth by viewModel.currentMonth.collectAsStateWithLifecycle()
    val selectedDate by viewModel.selectedDate.collectAsStateWithLifecycle()
    val dailyRecords by viewModel.dailyRecords.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("달력") },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        if (widthSizeClass == WindowWidthSizeClass.Expanded || widthSizeClass == WindowWidthSizeClass.Medium) {
            // 태블릿/폴드 펼침: 달력 | 상세 (HStack)
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                CalendarGridView(
                    currentMonth = currentMonth,
                    selectedDate = selectedDate,
                    dailyRecords = dailyRecords,
                    onDateSelected = viewModel::selectDate,
                    onPreviousMonth = viewModel::moveToPreviousMonth,
                    onNextMonth = viewModel::moveToNextMonth,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                )

                VerticalDivider(
                    modifier = Modifier.fillMaxHeight()
                )

                if (selectedDate != null) {
                    DayDetailInlineView(
                        date = selectedDate!!,
                        record = selectedDate?.let { viewModel.recordFor(it) },
                        viewModel = viewModel,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "날짜를 선택하세요",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            // 폰: 달력 위 + 상세 아래 (VStack)
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                CalendarGridView(
                    currentMonth = currentMonth,
                    selectedDate = selectedDate,
                    dailyRecords = dailyRecords,
                    onDateSelected = viewModel::selectDate,
                    onPreviousMonth = viewModel::moveToPreviousMonth,
                    onNextMonth = viewModel::moveToNextMonth,
                    modifier = Modifier.fillMaxWidth()
                )

                if (selectedDate != null) {
                    CompactDayDetailView(
                        date = selectedDate!!,
                        record = selectedDate?.let { viewModel.recordFor(it) },
                        viewModel = viewModel,
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    )
                } else {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

// ===== 달력 그리드 =====

@Composable
private fun CalendarGridView(
    currentMonth: YearMonth,
    selectedDate: LocalDate?,
    dailyRecords: Map<LocalDate, DailyRecordWithDetails>,
    onDateSelected: (LocalDate) -> Unit,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    modifier: Modifier = Modifier
) {
    val monthFormatter = remember {
        DateTimeFormatter.ofPattern("yyyy년 M월", Locale.KOREAN)
    }
    val weekdays = remember { listOf("일", "월", "화", "수", "목", "금", "토") }
    val days = remember(currentMonth) {
        val firstDay = currentMonth.atDay(1)
        val firstDayOfWeek = firstDay.dayOfWeek.value % 7 // Sunday=0
        val blanks = List<LocalDate?>(firstDayOfWeek) { null }
        val dates = (1..currentMonth.lengthOfMonth()).map { currentMonth.atDay(it) as LocalDate? }
        blanks + dates
    }

    var dragAccumulator by remember { mutableFloatStateOf(0f) }

    Column(
        modifier = modifier
            .background(MaterialTheme.colorScheme.background)
            .pointerInput(currentMonth) {
                detectHorizontalDragGestures(
                    onDragStart = { dragAccumulator = 0f },
                    onHorizontalDrag = { _, dragAmount -> dragAccumulator += dragAmount },
                    onDragEnd = {
                        if (dragAccumulator < -50f) onNextMonth()
                        else if (dragAccumulator > 50f) onPreviousMonth()
                    },
                    onDragCancel = { dragAccumulator = 0f }
                )
            }
    ) {
        // 월 헤더
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onPreviousMonth) {
                Icon(
                    Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                    contentDescription = "이전 달"
                )
            }

            Text(
                text = currentMonth.atDay(1).format(monthFormatter),
                style = MaterialTheme.typography.headlineMedium
            )

            IconButton(onClick = onNextMonth) {
                Icon(
                    Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = "다음 달"
                )
            }
        }

        // 요일 헤더
        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 8.dp),
            userScrollEnabled = false
        ) {
            items(weekdays) { day ->
                val chanmiColors = MaterialTheme.chanmiColors
                Text(
                    text = day,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                    color = when (day) {
                        "일" -> chanmiColors.sundayColor
                        "토" -> chanmiColors.saturdayColor
                        else -> MaterialTheme.colorScheme.onBackground
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // 날짜 그리드
        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            userScrollEnabled = false
        ) {
            items(days) { date ->
                if (date != null) {
                    DayCellView(
                        date = date,
                        isToday = date == LocalDate.now(),
                        isSelected = date == selectedDate,
                        record = dailyRecords[date],
                        onClick = { onDateSelected(date) }
                    )
                } else {
                    Spacer(modifier = Modifier.height(50.dp))
                }
            }
        }
    }
}

// ===== 날짜 셀 =====

@Composable
private fun DayCellView(
    date: LocalDate,
    isToday: Boolean,
    isSelected: Boolean,
    record: DailyRecordWithDetails?,
    onClick: () -> Unit
) {
    val hasRosary = record?.rosaryEntries?.isNotEmpty() == true
    val hasGoodDeeds = record?.goodDeeds?.isNotEmpty() == true
    val chanmiColors = MaterialTheme.chanmiColors

    val accessLabel = buildString {
        append("${date.monthValue}월 ${date.dayOfMonth}일")
        if (hasRosary) append(", 묵주기도 완료")
        if (hasGoodDeeds) append(", 선행 ${record?.goodDeeds?.size}건")
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .size(width = 44.dp, height = 50.dp)
            .clickable(onClick = onClick)
            .semantics { contentDescription = accessLabel }
    ) {
        // 날짜 숫자
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(
                    when {
                        isToday -> MaterialTheme.colorScheme.primary
                        isSelected -> MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                        else -> MaterialTheme.colorScheme.background
                    }
                )
        ) {
            Text(
                text = "${date.dayOfMonth}",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                color = when {
                    isToday -> MaterialTheme.colorScheme.onPrimary
                    isSelected -> MaterialTheme.colorScheme.primary
                    else -> MaterialTheme.colorScheme.onBackground
                }
            )
        }

        // 인디케이터 도트
        Row(
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            modifier = Modifier.height(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (hasRosary) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(chanmiColors.rosaryIndicator)
                )
            }
            if (hasGoodDeeds) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(chanmiColors.goodDeedIndicator)
                )
            }
        }
    }
}
