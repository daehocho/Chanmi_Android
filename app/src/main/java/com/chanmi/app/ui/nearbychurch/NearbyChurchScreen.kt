package com.chanmi.app.ui.nearbychurch

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.LocationOff
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.chanmi.app.data.model.ChurchCluster
import com.chanmi.app.data.model.ChurchItem
import com.chanmi.app.location.LocationManager
import com.chanmi.app.ui.theme.chanmiColors
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.MarkerComposable
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NearbyChurchScreen(
    widthSizeClass: WindowWidthSizeClass = WindowWidthSizeClass.Compact,
    viewModel: NearbyChurchViewModel = hiltViewModel(),
    locationManager: LocationManager
) {
    val churches by viewModel.churches.collectAsState()
    val clusters by viewModel.clusters.collectAsState()
    val selectedChurch by viewModel.selectedChurch.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val showSearchButton by viewModel.showSearchThisAreaButton.collectAsState()
    val isSearchingDifferentArea by viewModel.isSearchingDifferentArea.collectAsState()
    val userLocation by locationManager.userLocation.collectAsState()
    val permissionGranted by locationManager.permissionGranted.collectAsState()

    var showList by remember { mutableStateOf(false) }
    var showChurchDetail by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        locationManager.updatePermissionStatus(granted)
    }

    // 위치가 확보되면 자동 검색
    LaunchedEffect(userLocation) {
        if (userLocation != null && churches.isEmpty()) {
            viewModel.searchNearbyChurches(userLocation!!)
        }
    }

    // 성당 선택 시 상세 시트 표시
    LaunchedEffect(selectedChurch) {
        if (selectedChurch != null) showChurchDetail = true
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("주변 성당") },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (!permissionGranted) {
                PermissionView(
                    onRequest = {
                        permissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                        )
                    },
                    onOpenSettings = {
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = Uri.fromParts("package", context.packageName, null)
                        }
                        context.startActivity(intent)
                    }
                )
            } else if (widthSizeClass == WindowWidthSizeClass.Expanded) {
                // 태블릿: 지도 | 사이드 리스트
                Row(modifier = Modifier.fillMaxSize()) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    ) {
                        MapContent(
                            clusters = clusters,
                            userLocation = userLocation,
                            showSearchButton = showSearchButton,
                            isLoading = isLoading,
                            onMarkerClick = { viewModel.selectChurch(it) },
                            onClusterClick = { viewModel.zoomToCluster(it) },
                            onCameraMove = { center, distance ->
                                viewModel.updateMapCenter(center)
                                viewModel.updateClusters(distance)
                                viewModel.checkIfMapMovedSignificantly()
                                userLocation?.let { viewModel.checkIfReturnedToUserLocation(it) }
                            },
                            onSearchThisArea = { viewModel.searchChurchesInCurrentArea() },
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    HorizontalDivider(
                        modifier = Modifier
                            .fillMaxHeight()
                            .width(1.dp)
                    )

                    // 사이드 리스트 (320dp)
                    ChurchSideList(
                        churches = churches,
                        isLoading = isLoading,
                        onChurchClick = { it.openInMaps(context) },
                        modifier = Modifier
                            .width(320.dp)
                            .fillMaxHeight()
                    )
                }
            } else {
                // 폰: 전체 지도 + 하단 목록 버튼
                Box(modifier = Modifier.fillMaxSize()) {
                    MapContent(
                        clusters = clusters,
                        userLocation = userLocation,
                        showSearchButton = showSearchButton,
                        isLoading = isLoading,
                        onMarkerClick = { viewModel.selectChurch(it) },
                        onClusterClick = { viewModel.zoomToCluster(it) },
                        onCameraMove = { center, distance ->
                            viewModel.updateMapCenter(center)
                            viewModel.updateClusters(distance)
                            viewModel.checkIfMapMovedSignificantly()
                            userLocation?.let { viewModel.checkIfReturnedToUserLocation(it) }
                        },
                        onSearchThisArea = { viewModel.searchChurchesInCurrentArea() },
                        modifier = Modifier.fillMaxSize()
                    )

                    // 성당 목록 FAB
                    FloatingActionButton(
                        onClick = { showList = true },
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 20.dp)
                            .semantics { contentDescription = "성당 목록 보기, ${churches.size}개" },
                        containerColor = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(50)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.List, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "성당 목록 (${churches.size})",
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }
                }
            }
        }
    }

    // 성당 목록 시트 (폰)
    if (showList) {
        ModalBottomSheet(
            onDismissRequest = { showList = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ) {
            ChurchListSheetContent(
                churches = churches,
                isSearchingDifferentArea = isSearchingDifferentArea,
                onChurchClick = { it.openInMaps(context) },
                onDismiss = { showList = false }
            )
        }
    }

    // 성당 상세 시트
    if (showChurchDetail && selectedChurch != null) {
        ModalBottomSheet(
            onDismissRequest = {
                showChurchDetail = false
                viewModel.clearSelection()
            }
        ) {
            ChurchDetailContent(
                church = selectedChurch!!,
                context = context,
                onDismiss = {
                    showChurchDetail = false
                    viewModel.clearSelection()
                }
            )
        }
    }
}

// ===== 권한 뷰 =====

@Composable
private fun PermissionView(
    onRequest: () -> Unit,
    onOpenSettings: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.LocationOn,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(60.dp)
        )
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            "주변 성당을 찾으려면\n위치 권한이 필요합니다",
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(20.dp))
        Button(
            onClick = onRequest,
            modifier = Modifier.semantics { contentDescription = "위치 권한 허용하기" }
        ) {
            Text("위치 권한 허용")
        }
    }
}

// ===== 지도 콘텐츠 =====

@Composable
private fun MapContent(
    clusters: List<ChurchCluster>,
    userLocation: LatLng?,
    showSearchButton: Boolean,
    isLoading: Boolean,
    onMarkerClick: (ChurchItem) -> Unit,
    onClusterClick: (ChurchCluster) -> LatLng?,
    onCameraMove: (LatLng, Double) -> Unit,
    onSearchThisArea: () -> Unit,
    modifier: Modifier = Modifier
) {
    val chanmiColors = MaterialTheme.chanmiColors

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(
            userLocation ?: LatLng(37.5665, 126.9780),
            14f
        )
    }

    // 카메라 이동 감지
    LaunchedEffect(cameraPositionState.isMoving) {
        if (!cameraPositionState.isMoving) {
            val target = cameraPositionState.position.target
            val zoom = cameraPositionState.position.zoom
            val distance = 40_075_000.0 / Math.pow(2.0, zoom.toDouble())
            onCameraMove(target, distance)
        }
    }

    // 첫 위치 수신 시 카메라 이동
    LaunchedEffect(userLocation) {
        if (userLocation != null) {
            cameraPositionState.animate(
                CameraUpdateFactory.newLatLngZoom(userLocation, 14f)
            )
        }
    }

    Box(modifier = modifier) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            uiSettings = MapUiSettings(
                myLocationButtonEnabled = true,
                compassEnabled = true,
                zoomControlsEnabled = false
            ),
            properties = MapProperties(isMyLocationEnabled = true)
        ) {
            clusters.forEach { cluster ->
                if (cluster.count == 1) {
                    val church = cluster.churches[0]
                    MarkerComposable(
                        state = MarkerState(position = church.latLng),
                        title = church.name,
                        onClick = {
                            onMarkerClick(church)
                            true
                        }
                    ) {
                        Icon(
                            Icons.Filled.LocationOn,
                            contentDescription = "${church.name}, ${church.formattedDistance}",
                            tint = chanmiColors.goldAccent,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                } else {
                    MarkerComposable(
                        state = MarkerState(position = cluster.center),
                        title = cluster.name,
                        onClick = {
                            onClusterClick(cluster)
                            true
                        }
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(36.dp)
                                .background(chanmiColors.goldAccent, CircleShape)
                        ) {
                            Text(
                                "${cluster.count}",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                }
            }
        }

        // "이 지역에서 검색" 버튼 (애니메이션)
        AnimatedVisibility(
            visible = showSearchButton,
            enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 12.dp)
        ) {
            Surface(
                onClick = onSearchThisArea,
                shape = RoundedCornerShape(50),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                shadowElevation = 4.dp,
                modifier = Modifier.semantics { contentDescription = "이 지역에서 성당 검색" }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        "이 지역에서 검색",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        // 로딩 인디케이터
        if (isLoading) {
            Surface(
                modifier = Modifier.align(Alignment.Center),
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                shadowElevation = 2.dp
            ) {
                CircularProgressIndicator(modifier = Modifier.padding(16.dp).size(32.dp))
            }
        }
    }
}

// ===== 태블릿 사이드 리스트 =====

@Composable
private fun ChurchSideList(
    churches: List<ChurchItem>,
    isLoading: Boolean,
    onChurchClick: (ChurchItem) -> Unit,
    modifier: Modifier = Modifier
) {
    if (isLoading) {
        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else if (churches.isEmpty()) {
        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            Text("주변 성당이 없습니다", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    } else {
        LazyColumn(modifier = modifier) {
            items(churches, key = { it.id }) { church ->
                ChurchListItem(
                    church = church,
                    onClick = { onChurchClick(church) }
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            }
        }
    }
}

// ===== 성당 리스트 아이템 (공통) =====

@Composable
fun ChurchListItem(
    church: ChurchItem,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .semantics { contentDescription = "${church.name}, ${church.formattedDistance}" },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(church.name, style = MaterialTheme.typography.bodyLarge)
            Text(
                church.diocese,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                church.address,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (church.phone.isNotEmpty()) {
                Text(
                    church.phone,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            church.formattedDistance,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

// ===== 성당 목록 시트 (폰) =====

@Composable
fun ChurchListSheetContent(
    churches: List<ChurchItem>,
    isSearchingDifferentArea: Boolean,
    onChurchClick: (ChurchItem) -> Unit,
    onDismiss: () -> Unit
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                if (isSearchingDifferentArea) "이 지역 성당" else "주변 성당",
                style = MaterialTheme.typography.titleMedium
            )
            TextButton(onClick = onDismiss) { Text("닫기") }
        }

        HorizontalDivider()

        LazyColumn {
            items(churches, key = { it.id }) { church ->
                ChurchListItem(
                    church = church,
                    onClick = { onChurchClick(church) }
                )
            }
        }
    }
}

// ===== 성당 상세 시트 =====

@Composable
private fun ChurchDetailContent(
    church: ChurchItem,
    context: Context,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        // 헤더
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                church.name,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            IconButton(
                onClick = onDismiss,
                modifier = Modifier.semantics { contentDescription = "성당 정보 닫기" }
            ) {
                Icon(Icons.Default.Close, contentDescription = "닫기")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 상세 정보
        DetailRow(label = "교구", value = church.diocese)
        DetailRow(label = "주소", value = church.address)

        if (church.phone.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "전화번호",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                TextButton(
                    onClick = {
                        val cleaned = church.phone.replace(Regex("[^0-9]"), "")
                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$cleaned"))
                        context.startActivity(intent)
                    },
                    modifier = Modifier.semantics {
                        contentDescription = "전화번호 ${church.phone}, 탭하여 전화 걸기"
                    }
                ) {
                    Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(church.phone)
                }
            }
        }

        DetailRow(label = "거리", value = church.formattedDistance)

        Spacer(modifier = Modifier.height(20.dp))

        // 길찾기 버튼
        Button(
            onClick = { church.openInMaps(context) },
            modifier = Modifier
                .fillMaxWidth()
                .semantics { contentDescription = "지도에서 길찾기" },
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.Map, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("길찾기")
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            value,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(start = 16.dp),
            textAlign = TextAlign.End
        )
    }
}
