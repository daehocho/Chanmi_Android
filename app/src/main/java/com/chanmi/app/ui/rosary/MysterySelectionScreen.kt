package com.chanmi.app.ui.rosary

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.chanmi.app.data.model.DecadeStep
import com.chanmi.app.data.model.MysteryType
import com.chanmi.app.data.model.RosaryPhase
import com.chanmi.app.data.repository.CalendarRepository
import com.chanmi.app.ui.theme.ChanmiTheme
import java.time.LocalDate
import javax.inject.Inject
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MysterySelectionScreen(
    viewModel: RosaryViewModel = hiltViewModel(),
    calendarRepository: CalendarRepository? = null,
    onRosaryCompleted: (() -> Unit)? = null
) {
    val currentPhase by viewModel.currentPhase.collectAsState()
    val selectedMystery by viewModel.selectedMystery.collectAsState()
    val isPraying by viewModel.isPraying.collectAsState()
    val numberOfDecades by viewModel.numberOfDecades.collectAsState()
    val elapsedSeconds by viewModel.elapsedSeconds.collectAsState()
    val preferredHand by viewModel.preferredHand.collectAsState()
    val hasSeenSwipeGuide by viewModel.hasSeenSwipeGuide.collectAsState()

    var showCompletion by remember { mutableStateOf(false) }
    var isPrayerTextExpanded by remember { mutableStateOf(false) }
    var showSwipeGuide by remember { mutableStateOf(false) }

    LaunchedEffect(currentPhase) {
        if (currentPhase is RosaryPhase.Completed) {
            viewModel.stopPraying()
            showCompletion = true
        }
    }

    LaunchedEffect(isPraying) {
        if (isPraying) {
            isPrayerTextExpanded = true
            if (!hasSeenSwipeGuide) {
                showSwipeGuide = true
            }
        }
    }

    if (showCompletion) {
        RosaryCompletionScreen(
            selectedMystery = selectedMystery,
            numberOfDecades = numberOfDecades,
            calendarRepository = calendarRepository,
            onDismiss = {
                showCompletion = false
                viewModel.reset()
            }
        )
        return
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("묵주기도") },
                actions = {
                    if (isPraying) {
                        if (currentPhase is RosaryPhase.ClosingPrayer) {
                            TextButton(onClick = { viewModel.advance() }) {
                                Text("완료", fontWeight = FontWeight.SemiBold)
                            }
                        } else {
                            TextButton(onClick = {
                                viewModel.stopPraying()
                                viewModel.reset()
                            }) {
                                Text("중단")
                            }
                        }
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            if (isPraying) {
                PrayingLayout(
                    viewModel = viewModel,
                    currentPhase = currentPhase,
                    selectedMystery = selectedMystery,
                    isPrayerTextExpanded = isPrayerTextExpanded,
                    onTogglePrayerText = { isPrayerTextExpanded = !isPrayerTextExpanded },
                    preferredHand = preferredHand,
                )
            } else {
                NotPrayingLayout(
                    viewModel = viewModel,
                    selectedMystery = selectedMystery,
                    numberOfDecades = numberOfDecades,
                )
            }

            if (showSwipeGuide) {
                SwipeGuideOverlay(onDismiss = {
                    viewModel.markSwipeGuideSeen()
                    showSwipeGuide = false
                })
            }
        }
    }
}

@Composable
private fun PrayingLayout(
    viewModel: RosaryViewModel,
    currentPhase: RosaryPhase,
    selectedMystery: MysteryType,
    isPrayerTextExpanded: Boolean,
    onTogglePrayerText: () -> Unit,
    preferredHand: String,
) {
    val haptic = LocalHapticFeedback.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 8.dp, bottom = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        RosaryBeadCircle(
            isPraying = true,
            currentPhase = currentPhase,
            selectedMystery = selectedMystery,
            currentDecade = viewModel.currentDecade,
            onTap = {}
        )

        // Swipe area
        if (currentPhase !is RosaryPhase.ClosingPrayer && currentPhase !is RosaryPhase.Completed) {
            SwipeArea(
                preferredHand = preferredHand,
                onAdvance = {
                    viewModel.debouncedAdvance()
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                }
            )
        } else {
            Spacer(modifier = Modifier.height(80.dp))
        }

        PrayerTextCard(
            isPraying = true,
            phaseTitle = viewModel.currentPhaseTitle,
            prayerText = viewModel.currentPrayerText,
            meditationTopic = viewModel.currentMeditationTopic,
            isExpanded = isPrayerTextExpanded,
            onToggle = onTogglePrayerText,
        )

        Spacer(modifier = Modifier.weight(1f))

        ProgressTimerSection(
            progressPercent = viewModel.progressPercent,
            formattedTime = viewModel.formattedTime,
        )
    }
}

@Composable
private fun NotPrayingLayout(
    viewModel: RosaryViewModel,
    selectedMystery: MysteryType,
    numberOfDecades: Int,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(top = 8.dp, bottom = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        MysterySegmentControl(
            selected = selectedMystery,
            onSelect = { viewModel.selectMystery(it) }
        )

        RosaryBeadCircle(
            isPraying = false,
            currentPhase = RosaryPhase.MysterySelection,
            selectedMystery = selectedMystery,
            currentDecade = null,
            onTap = { viewModel.startPraying() }
        )

        PrayerTextCard(
            isPraying = false,
            phaseTitle = "",
            prayerText = "",
            meditationTopic = null,
            isExpanded = false,
            onToggle = {},
        )

        DecadeSelector(
            selected = numberOfDecades,
            onSelect = { viewModel.setNumberOfDecades(it) }
        )

        ProgressTimerSection(
            progressPercent = viewModel.progressPercent,
            formattedTime = viewModel.formattedTime,
        )
    }
}

// ===== Mystery Segment Control =====

@Composable
private fun MysterySegmentControl(
    selected: MysteryType,
    onSelect: (MysteryType) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(4.dp)
    ) {
        MysteryType.entries.forEach { mystery ->
            val isSelected = mystery == selected
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
                    .clickable { onSelect(mystery) }
                    .padding(vertical = 10.dp)
                    .semantics { contentDescription = mystery.displayName },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = mysteryShortName(mystery),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// ===== Rosary Bead Circle =====

@Composable
private fun RosaryBeadCircle(
    isPraying: Boolean,
    currentPhase: RosaryPhase,
    selectedMystery: MysteryType,
    currentDecade: Int?,
    onTap: () -> Unit
) {
    val beadSize = if (isPraying) 180.dp else 220.dp
    val radius = if (isPraying) 90f else 110f
    val goldAccent = ChanmiTheme.colors.goldAccent
    val primary = MaterialTheme.colorScheme.primary

    Box(
        modifier = Modifier
            .size(if (isPraying) 220.dp else 260.dp)
            .clickable(onClick = onTap)
            .semantics {
                contentDescription = if (isPraying) {
                    "${currentDecade?.let { "제${it}단" } ?: ""} 진행 중"
                } else {
                    "${selectedMystery.displayName} 묵주 구슬, 탭하여 시작"
                }
            },
        contentAlignment = Alignment.Center
    ) {
        androidx.compose.foundation.Canvas(
            modifier = Modifier.size(beadSize)
        ) {
            // Draw circle track
            drawCircle(
                color = Color.Gray.copy(alpha = 0.3f),
                radius = size.minDimension / 2,
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.dp.toPx())
            )

            // Draw beads
            val beadRadius = 10.dp.toPx()
            val circleRadius = size.minDimension / 2
            for (i in 0 until 10) {
                val angle = (i.toDouble() / 10.0) * 360.0 - 90.0
                val radian = Math.toRadians(angle)
                val x = center.x + circleRadius * cos(radian).toFloat()
                val y = center.y + circleRadius * sin(radian).toFloat()

                val state = beadStateFor(i, isPraying, currentPhase)
                val color = when (state) {
                    BeadState.COMPLETED -> primary
                    BeadState.CURRENT -> primary.copy(alpha = 0.6f)
                    BeadState.INCOMPLETE -> goldAccent
                }
                val r = if (state == BeadState.CURRENT) beadRadius * 1.2f else beadRadius

                drawCircle(color = color, radius = r, center = Offset(x, y))
            }
        }

        // Center text
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            if (isPraying) {
                if (currentDecade != null) {
                    Text(
                        "제${currentDecade}단",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                } else {
                    Text("✝", style = MaterialTheme.typography.headlineLarge)
                }
            } else {
                Text(
                    selectedMystery.displayName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    mysteryShortName(selectedMystery),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// ===== Swipe Area =====

@Composable
private fun SwipeArea(
    preferredHand: String,
    onAdvance: () -> Unit
) {
    var dragOffset by remember { mutableFloatStateOf(0f) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .padding(horizontal = 16.dp)
    ) {
        if (preferredHand == "left") {
            SwipeZone(dragOffset = dragOffset, onDragChange = { dragOffset = it }, onAdvance = {
                onAdvance()
                dragOffset = 0f
            })
            Spacer(modifier = Modifier.weight(1f))
        } else {
            Spacer(modifier = Modifier.weight(1f))
            SwipeZone(dragOffset = dragOffset, onDragChange = { dragOffset = it }, onAdvance = {
                onAdvance()
                dragOffset = 0f
            })
        }
    }
}

@Composable
private fun SwipeZone(
    dragOffset: Float,
    onDragChange: (Float) -> Unit,
    onAdvance: () -> Unit
) {
    val primary = MaterialTheme.colorScheme.primary

    Box(
        modifier = Modifier
            .width(60.dp)
            .height(80.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(
                androidx.compose.ui.graphics.Brush.verticalGradient(
                    colors = listOf(primary.copy(alpha = 0.15f), primary.copy(alpha = 0.05f))
                )
            )
            .pointerInput(Unit) {
                detectVerticalDragGestures(
                    onDragEnd = {
                        if (dragOffset >= 50f) {
                            onAdvance()
                        } else {
                            onDragChange(0f)
                        }
                    },
                    onDragCancel = { onDragChange(0f) },
                    onVerticalDrag = { _, dragAmount ->
                        if (dragAmount > 0) {
                            onDragChange(dragOffset + dragAmount)
                        }
                    }
                )
            }
            .semantics { contentDescription = "아래로 쓸어내려 다음 기도로 넘어가기" },
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.offset { IntOffset(0, (dragOffset * 0.3f).roundToInt()) },
            verticalArrangement = Arrangement.spacedBy(4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                tint = primary.copy(alpha = 0.6f),
                modifier = Modifier.size(16.dp)
            )
            Icon(
                Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                tint = primary.copy(alpha = 0.4f),
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

// ===== Prayer Text Card =====

@Composable
private fun PrayerTextCard(
    isPraying: Boolean,
    phaseTitle: String,
    prayerText: String,
    meditationTopic: String?,
    isExpanded: Boolean,
    onToggle: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
    ) {
        if (isPraying) {
            // Collapsible header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onToggle)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.MenuBook,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        phaseTitle,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    if (meditationTopic != null) {
                        Text(
                            meditationTopic,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Icon(
                    if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = if (isExpanded) "접기" else "펼치기",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column {
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    Text(
                        text = prayerText,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .height(180.dp)
                            .verticalScroll(rememberScrollState())
                    )
                }
            }
        } else {
            // Not praying - hint
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Default.TouchApp,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    "묵주를 탭하여 기도를 시작하세요",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// ===== Decade Selector =====

@Composable
private fun DecadeSelector(
    selected: Int,
    onSelect: (Int) -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            "기도 단수",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            for (decade in 1..5) {
                val isSelected = decade == selected
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(
                            if (isSelected) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.surface
                        )
                        .clickable { onSelect(decade) }
                        .semantics { contentDescription = "${decade}단" },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "$decade",
                        style = MaterialTheme.typography.titleMedium,
                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                        else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

// ===== Progress & Timer =====

@Composable
private fun ProgressTimerSection(
    progressPercent: String,
    formattedTime: String,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        StatCard(modifier = Modifier.weight(1f), label = "진행률", value = progressPercent)
        StatCard(modifier = Modifier.weight(1f), label = "시간", value = formattedTime)
    }
}

@Composable
private fun StatCard(modifier: Modifier, label: String, value: String) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            value,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
    }
}

// ===== Helpers =====

private enum class BeadState { INCOMPLETE, CURRENT, COMPLETED }

private fun beadStateFor(index: Int, isPraying: Boolean, currentPhase: RosaryPhase): BeadState {
    if (!isPraying) return BeadState.INCOMPLETE
    if (currentPhase !is RosaryPhase.Decade) return BeadState.INCOMPLETE

    return when (currentPhase.step) {
        is DecadeStep.HailMary -> {
            val m = currentPhase.step.count
            when {
                index < m - 1 -> BeadState.COMPLETED
                index == m - 1 -> BeadState.CURRENT
                else -> BeadState.INCOMPLETE
            }
        }
        is DecadeStep.Glory, is DecadeStep.Fatima -> BeadState.COMPLETED
        else -> BeadState.INCOMPLETE
    }
}

private fun mysteryShortName(mystery: MysteryType): String = when (mystery) {
    MysteryType.JOYFUL -> "환희"
    MysteryType.SORROWFUL -> "고통"
    MysteryType.GLORIOUS -> "영광"
    MysteryType.LUMINOUS -> "빛"
}
