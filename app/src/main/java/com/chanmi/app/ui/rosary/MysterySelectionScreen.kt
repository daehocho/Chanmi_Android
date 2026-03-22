package com.chanmi.app.ui.rosary

import android.app.Activity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import android.provider.Settings
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.hilt.navigation.compose.hiltViewModel
import com.chanmi.app.data.model.DecadeStep
import com.chanmi.app.data.model.MysteryType
import com.chanmi.app.data.model.RosaryPhase
import com.chanmi.app.DevicePosture
import com.chanmi.app.LocalDevicePosture
import com.chanmi.app.ui.theme.chanmiColors
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MysterySelectionScreen(
    widthSizeClass: WindowWidthSizeClass = WindowWidthSizeClass.Compact,
    viewModel: RosaryViewModel = hiltViewModel(),
) {
    val currentPhase by viewModel.currentPhase.collectAsStateWithLifecycle()
    val selectedMystery by viewModel.selectedMystery.collectAsStateWithLifecycle()
    val isPraying by viewModel.isPraying.collectAsStateWithLifecycle()
    val numberOfDecades by viewModel.numberOfDecades.collectAsStateWithLifecycle()
    val preferredHand by viewModel.preferredHand.collectAsStateWithLifecycle()
    val hasSeenSwipeGuide by viewModel.hasSeenSwipeGuide.collectAsStateWithLifecycle()
    val reviewInfo by viewModel.reviewInfo.collectAsStateWithLifecycle()

    var showCompletion by rememberSaveable { mutableStateOf(false) }
    var isPrayerTextExpanded by rememberSaveable { mutableStateOf(false) }
    var showSwipeGuide by rememberSaveable { mutableStateOf(false) }

    // In-App Review: ReviewInfo가 준비되면 동일 ReviewManager 인스턴스로 launchReviewFlow 실행
    val activity = LocalContext.current as? Activity
    LaunchedEffect(reviewInfo) {
        reviewInfo ?: return@LaunchedEffect
        activity?.let { viewModel.launchReview(it) }
    }

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
            onSave = { viewModel.saveCompletedRosary() },
            onRequestReview = { viewModel.requestReviewIfNeeded() },
            onDismiss = {
                showCompletion = false
                viewModel.reset()
            }
        )
        return
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("묵주기도") },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                ),
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
                    widthSizeClass = widthSizeClass,
                )
            } else {
                NotPrayingLayout(
                    viewModel = viewModel,
                    selectedMystery = selectedMystery,
                    numberOfDecades = numberOfDecades,
                    widthSizeClass = widthSizeClass,
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
    widthSizeClass: WindowWidthSizeClass = WindowWidthSizeClass.Compact,
) {
    val haptic = LocalHapticFeedback.current
    val currentDecade by viewModel.currentDecade.collectAsStateWithLifecycle()
    val phaseTitle by viewModel.currentPhaseTitle.collectAsStateWithLifecycle()
    val prayerText by viewModel.currentPrayerText.collectAsStateWithLifecycle()
    val meditationTopic by viewModel.currentMeditationTopic.collectAsStateWithLifecycle()
    val progressPercent by viewModel.progressPercent.collectAsStateWithLifecycle()
    val formattedTime by viewModel.formattedTime.collectAsStateWithLifecycle()
    val devicePosture = LocalDevicePosture.current
    val isFlexMode = devicePosture == DevicePosture.HALF_OPENED_HORIZONTAL

    if (isFlexMode) {
        // 갤럭시 플립 플렉스 모드: 상단에 비드, 하단에 스와이프+기도문
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 4.dp, bottom = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 상단 절반: 비드 원 + 진행률
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    RosaryBeadCircle(
                        isPraying = true,
                        currentPhase = currentPhase,
                        selectedMystery = selectedMystery,
                        currentDecade = currentDecade,
                        onTap = {},
                        widthSizeClass = widthSizeClass,
                        viewModel = viewModel
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    ProgressTimerSection(
                        progressPercent = progressPercent,
                        formattedTime = formattedTime,
                    )
                }
            }
            // 힌지 영역 여백
            Spacer(modifier = Modifier.height(8.dp))
            // 하단 절반: 스와이프 + 기도문
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (currentPhase !is RosaryPhase.ClosingPrayer && currentPhase !is RosaryPhase.Completed) {
                    SwipeArea(
                        preferredHand = preferredHand,
                        onAdvance = {
                            val isDecadeChange = viewModel.isDecadeTransition()
                            val advanced = viewModel.debouncedAdvance()
                            if (advanced) {
                                haptic.performHapticFeedback(
                                    if (isDecadeChange) HapticFeedbackType.LongPress
                                    else HapticFeedbackType.TextHandleMove
                                )
                            }
                        }
                    )
                } else {
                    Spacer(modifier = Modifier.height(80.dp))
                }
                PrayerTextCard(
                    isPraying = true,
                    phaseTitle = phaseTitle,
                    prayerText = prayerText,
                    meditationTopic = meditationTopic,
                    isExpanded = isPrayerTextExpanded,
                    onToggle = onTogglePrayerText,
                )
            }
        }
    } else {
        // 일반 모드
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
                currentDecade = currentDecade,
                onTap = {},
                widthSizeClass = widthSizeClass,
                viewModel = viewModel
            )

            // Swipe area
            if (currentPhase !is RosaryPhase.ClosingPrayer && currentPhase !is RosaryPhase.Completed) {
                SwipeArea(
                    preferredHand = preferredHand,
                    onAdvance = {
                        val isDecadeChange = viewModel.isDecadeTransition()
                        val advanced = viewModel.debouncedAdvance()
                        if (advanced) {
                            haptic.performHapticFeedback(
                                if (isDecadeChange) HapticFeedbackType.LongPress
                                else HapticFeedbackType.TextHandleMove
                            )
                        }
                    }
                )
            } else {
                Spacer(modifier = Modifier.height(80.dp))
            }

            PrayerTextCard(
                isPraying = true,
                phaseTitle = phaseTitle,
                prayerText = prayerText,
                meditationTopic = meditationTopic,
                isExpanded = isPrayerTextExpanded,
                onToggle = onTogglePrayerText,
            )

            Spacer(modifier = Modifier.weight(1f))

            ProgressTimerSection(
                progressPercent = progressPercent,
                formattedTime = formattedTime,
            )
        }
    }
}

@Composable
private fun NotPrayingLayout(
    viewModel: RosaryViewModel,
    selectedMystery: MysteryType,
    numberOfDecades: Int,
    widthSizeClass: WindowWidthSizeClass = WindowWidthSizeClass.Compact,
) {
    val progressPercent by viewModel.progressPercent.collectAsStateWithLifecycle()
    val formattedTime by viewModel.formattedTime.collectAsStateWithLifecycle()

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
            onTap = { viewModel.startPraying() },
            widthSizeClass = widthSizeClass,
            viewModel = viewModel
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
            progressPercent = progressPercent,
            formattedTime = formattedTime,
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
    onTap: () -> Unit,
    widthSizeClass: WindowWidthSizeClass = WindowWidthSizeClass.Compact,
    viewModel: RosaryViewModel? = null
) {
    val isWide = widthSizeClass == WindowWidthSizeClass.Expanded || widthSizeClass == WindowWidthSizeClass.Medium
    val beadSize = when {
        isWide && isPraying -> 260.dp
        isWide -> 300.dp
        isPraying -> 180.dp
        else -> 220.dp
    }
    val containerSize = when {
        isWide && isPraying -> 300.dp
        isWide -> 340.dp
        isPraying -> 220.dp
        else -> 260.dp
    }
    val goldAccent = MaterialTheme.chanmiColors.goldAccent
    val primary = MaterialTheme.colorScheme.primary
    val trackColor = MaterialTheme.colorScheme.outlineVariant

    // 접근성: 모션 감소 설정 확인
    val reduceMotion = LocalReduceMotion

    // Animate bead radii for smooth state transitions
    val beadScales = (0 until 10).map { i ->
        val state = beadStateFor(i, isPraying, currentPhase)
        animateFloatAsState(
            targetValue = if (state == BeadState.CURRENT) 1.2f else 1.0f,
            animationSpec = if (reduceMotion) tween(durationMillis = 0) else tween(durationMillis = 300),
            label = "bead_scale_$i"
        )
    }

    Box(
        modifier = Modifier
            .size(containerSize)
            .clickable(onClick = onTap)
            .semantics {
                contentDescription = if (isPraying) {
                    "${currentDecade?.let { "제${it}단" } ?: ""} 진행 중"
                } else {
                    "${selectedMystery.displayName} 묵주 구슬, 탭하여 시작"
                }
                if (isPraying && viewModel != null) {
                    customActions = listOf(
                        CustomAccessibilityAction("다음 기도로 진행") {
                            viewModel.debouncedAdvance()
                            true
                        }
                    )
                }
            },
        contentAlignment = Alignment.Center
    ) {
        androidx.compose.foundation.Canvas(
            modifier = Modifier.size(beadSize)
        ) {
            // Draw circle track (theme-aware color for dark mode support)
            drawCircle(
                color = trackColor,
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
                val r = beadRadius * beadScales[i].value

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
            .semantics {
                contentDescription = "아래로 쓸어내려 다음 기도로 넘어가기"
                customActions = listOf(
                    CustomAccessibilityAction("다음 기도로 진행") {
                        onAdvance()
                        true
                    }
                )
            },
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
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .semantics {
                        stateDescription = if (isExpanded) "펼쳐짐" else "접혀짐"
                    },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.MenuBook,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        phaseTitle,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.semantics {
                            liveRegion = LiveRegionMode.Polite
                        }
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

// ===== Decade Selector (Slider 기반) =====

@Composable
private fun DecadeSelector(
    selected: Int,
    onSelect: (Int) -> Unit
) {
    val haptic = LocalHapticFeedback.current
    var previousValue by remember { mutableStateOf(selected) }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(horizontal = 16.dp)
    ) {
        Text(
            "기도 단수",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            "${selected}단",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            estimatedTime(selected),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))
        Slider(
            value = selected.toFloat(),
            onValueChange = { newValue ->
                val rounded = newValue.roundToInt()
                if (rounded != previousValue) {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    previousValue = rounded
                }
                onSelect(rounded)
            },
            valueRange = 5f..50f,
            steps = 8,
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.primary,
                activeTrackColor = MaterialTheme.colorScheme.primary,
                inactiveTrackColor = MaterialTheme.colorScheme.outlineVariant
            ),
            modifier = Modifier
                .fillMaxWidth()
                .semantics {
                    contentDescription = "기도 단수 선택, 현재 ${selected}단"
                }
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                "5단",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                "50단",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun estimatedTime(decades: Int): String {
    val minutes = decades * 3
    return if (minutes >= 60) {
        val hours = minutes / 60
        val remainMinutes = minutes % 60
        if (remainMinutes == 0) "약 ${hours}시간 소요"
        else "약 ${hours}시간 ${remainMinutes}분 소요"
    } else {
        "약 ${minutes}분 소요"
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

// ===== 접근성: 모션 감소 설정 =====

private val LocalReduceMotion: Boolean
    @Composable
    get() {
        val context = LocalContext.current
        return remember {
            Settings.Global.getFloat(
                context.contentResolver,
                Settings.Global.ANIMATOR_DURATION_SCALE,
                1f
            ) == 0f
        }
    }
