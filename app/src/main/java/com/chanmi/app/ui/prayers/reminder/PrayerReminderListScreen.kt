package com.chanmi.app.ui.prayers.reminder

import android.Manifest
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.ui.unit.Dp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.chanmi.app.data.model.PrayerReminder
import com.chanmi.app.ui.theme.chanmiColors
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrayerReminderListScreen(
    onNavigateToEdit: (reminderId: String?) -> Unit,
    onBack: () -> Unit,
    viewModel: PrayerReminderViewModel = hiltViewModel()
) {
    val reminders by viewModel.reminders.collectAsStateWithLifecycle()
    val canScheduleExact by viewModel.canScheduleExact.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    // 알림 권한 상태
    var notificationPermissionGranted by rememberSaveable { mutableStateOf(true) }
    var showRationaleDialog by rememberSaveable { mutableStateOf(false) }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        notificationPermissionGranted = granted
    }

    // Android 13+ 알림 권한 확인 (진입 시 상태만 확인, 즉시 요청하지 않음)
    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permission = context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)
            notificationPermissionGranted = permission == android.content.pm.PackageManager.PERMISSION_GRANTED
        }
        // 화면 복귀 시 정확 알림 권한도 재확인
        viewModel.checkExactAlarmPermission()
    }

    // 앱이 포그라운드로 복귀할 때 알림 권한 상태 재확인 (iOS scenePhase .active 대응)
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    val permission = context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)
                    notificationPermissionGranted = permission == android.content.pm.PackageManager.PERMISSION_GRANTED
                }
                viewModel.checkExactAlarmPermission()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    // 알림 추가 시 권한 확인 후 네비게이션
    val navigateToAdd: () -> Unit = {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !notificationPermissionGranted) {
            showRationaleDialog = true
        } else {
            onNavigateToEdit(null)
        }
    }

    // 권한 사전 설명 다이얼로그 (UX-09)
    if (showRationaleDialog) {
        AlertDialog(
            onDismissRequest = { showRationaleDialog = false },
            icon = {
                Icon(
                    Icons.Filled.Notifications,
                    contentDescription = null,
                    tint = MaterialTheme.chanmiColors.goldAccent
                )
            },
            title = { Text("기도 시간을 알려드릴까요?") },
            text = { Text("설정한 시간에 기도 알림을 받으려면 알림 권한이 필요합니다.") },
            confirmButton = {
                TextButton(onClick = {
                    showRationaleDialog = false
                    permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    // 권한 결과와 무관하게 편집 화면으로 이동 (설정 자체는 허용)
                    onNavigateToEdit(null)
                }) {
                    Text("허용", color = MaterialTheme.chanmiColors.goldAccent)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showRationaleDialog = false
                    // 권한 없어도 설정은 가능 (나중에 권한 부여 가능)
                    onNavigateToEdit(null)
                }) {
                    Text("나중에")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("기도 알림") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로 가기")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = navigateToAdd,
                containerColor = MaterialTheme.chanmiColors.goldAccent,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.semantics { contentDescription = "알림 추가" }
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 알림 권한 거부 배너
            if (!notificationPermissionGranted) {
                NotificationDeniedCard(
                    onOpenSettings = {
                        val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                            putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                        }
                        context.startActivity(intent)
                    },
                    modifier = Modifier
                        .widthIn(max = 600.dp)
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            // 정확한 알림 권한 거부 배너 (H2: Android 12+)
            if (!canScheduleExact && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                ExactAlarmDeniedCard(
                    onOpenSettings = {
                        val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                        context.startActivity(intent)
                    },
                    modifier = Modifier
                        .widthIn(max = 600.dp)
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }

            // 3-state 분기: null(로딩), empty(빈 목록), non-empty(목록) (UX-07)
            when (val list = reminders) {
                null -> {
                    // 로딩 상태
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = MaterialTheme.chanmiColors.goldAccent)
                    }
                }
                else -> if (list.isEmpty()) {
                    EmptyReminderState(
                        onAddClick = navigateToAdd,
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f)
                    )
                } else {
                    // 대화면에서 카드가 과도하게 넓어지지 않도록 maxWidth 제한 (UX-03)
                    val lazyListState = rememberLazyListState()
                    val reorderState = rememberReorderableLazyListState(lazyListState) { from, to ->
                        viewModel.reorder(from.index, to.index)
                    }
                    LazyColumn(
                        state = lazyListState,
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier
                            .widthIn(max = 600.dp)
                            .weight(1f)
                    ) {
                        items(
                            items = list,
                            key = { it.id }
                        ) { reminder ->
                            ReorderableItem(reorderState, key = reminder.id) { isDragging ->
                                ReminderCardWithSwipe(
                                    reminder = reminder,
                                    isDragging = isDragging,
                                    dragHandleModifier = Modifier.draggableHandle(),
                                    onToggle = { viewModel.toggleEnabled(reminder) },
                                    onClick = { onNavigateToEdit(reminder.id) },
                                    onDelete = {
                                        viewModel.deleteReminder(reminder)
                                        scope.launch {
                                            val result = snackbarHostState.showSnackbar(
                                                message = "알림이 삭제되었습니다",
                                                actionLabel = "실행 취소",
                                                duration = SnackbarDuration.Long
                                            )
                                            if (result == SnackbarResult.ActionPerformed) {
                                                viewModel.undoDelete(reminder)
                                            }
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// MARK: - ReminderCard with SwipeToDismiss + Drag Handle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReminderCardWithSwipe(
    reminder: PrayerReminder,
    isDragging: Boolean,
    dragHandleModifier: Modifier = Modifier,
    onToggle: () -> Unit,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val elevation: Dp by animateDpAsState(
        targetValue = if (isDragging) 6.dp else 0.dp,
        label = "drag_elevation"
    )
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) {
                onDelete()
                true
            } else {
                false
            }
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            val color by animateColorAsState(
                targetValue = if (dismissState.targetValue == SwipeToDismissBoxValue.EndToStart)
                    MaterialTheme.colorScheme.error
                else MaterialTheme.colorScheme.surface,
                label = "dismiss_bg"
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color, RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "삭제",
                    tint = MaterialTheme.colorScheme.onError,
                    modifier = Modifier.padding(end = 20.dp)
                )
            }
        },
        enableDismissFromStartToEnd = false
    ) {
        ReminderCard(
            reminder = reminder,
            elevation = elevation,
            dragHandleModifier = dragHandleModifier,
            onToggle = onToggle,
            onClick = onClick
        )
    }
}

// MARK: - ReminderCard

@Composable
private fun ReminderCard(
    reminder: PrayerReminder,
    elevation: Dp,
    onToggle: () -> Unit,
    onClick: () -> Unit,
    dragHandleModifier: Modifier = Modifier
) {
    val accessibilityDesc = "${reminder.prayerTitle}, ${reminder.formattedTime}, ${reminder.weekdayText}"

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = elevation,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .semantics { contentDescription = accessibilityDesc }
    ) {
        Row(
            modifier = Modifier.padding(start = 4.dp, end = 14.dp, top = 14.dp, bottom = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 드래그 핸들
            Icon(
                Icons.Default.DragHandle,
                contentDescription = "순서 변경",
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                modifier = dragHandleModifier
                    .size(40.dp)
                    .padding(8.dp)
            )

            // 벨 아이콘
            Surface(
                shape = CircleShape,
                color = MaterialTheme.chanmiColors.goldAccent.copy(alpha = 0.15f),
                modifier = Modifier.size(44.dp)
            ) {
                Icon(
                    Icons.Filled.Notifications,
                    contentDescription = null,
                    tint = MaterialTheme.chanmiColors.goldAccent,
                    modifier = Modifier
                        .padding(10.dp)
                        .size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            // 기도문 이름 + 시간/요일
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = reminder.prayerTitle,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = reminder.formattedTime,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "·",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = reminder.weekdayText,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // 활성/비활성 토글 (UX-05: contentDescription 추가)
            Switch(
                checked = reminder.isEnabled,
                onCheckedChange = { onToggle() },
                colors = SwitchDefaults.colors(
                    checkedTrackColor = MaterialTheme.chanmiColors.goldAccent
                ),
                modifier = Modifier.semantics {
                    contentDescription = if (reminder.isEnabled) "알림 켜짐" else "알림 꺼짐"
                }
            )
        }
    }
}

// MARK: - 빈 상태

@Composable
private fun EmptyReminderState(
    onAddClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Outlined.NotificationsNone,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.size(16.dp))
        Text(
            text = "설정된 기도 알림이 없습니다",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.size(8.dp))
        Text(
            text = "기도 시간을 설정하면 알림을 받을 수 있습니다",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )
        Spacer(modifier = Modifier.size(24.dp))
        FilledTonalButton(onClick = onAddClick) {
            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("알림 추가")
        }
    }
}

// MARK: - 알림 권한 거부 배너

@Composable
private fun NotificationDeniedCard(
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.errorContainer,
        modifier = modifier
            .fillMaxWidth()
            .semantics { contentDescription = "알림이 꺼져 있습니다. 설정에서 알림을 허용해 주세요." }
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Filled.NotificationsOff,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "알림이 꺼져 있습니다",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "설정에서 알림을 허용해 주세요",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            FilledTonalButton(onClick = onOpenSettings) {
                Text("설정")
            }
        }
    }
}

// MARK: - 정확한 알림 권한 거부 배너 (H2)

@Composable
private fun ExactAlarmDeniedCard(
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = modifier
            .fillMaxWidth()
            .semantics { contentDescription = "정확한 알림 시간이 보장되지 않습니다. 설정에서 정확한 알림을 허용해 주세요." }
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Filled.Notifications,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "알림 시간이 부정확할 수 있습니다",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "설정에서 정확한 알림을 허용해 주세요",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            FilledTonalButton(onClick = onOpenSettings) {
                Text("설정")
            }
        }
    }
}
