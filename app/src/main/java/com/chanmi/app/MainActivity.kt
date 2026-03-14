package com.chanmi.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import android.Manifest
import android.content.pm.PackageManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.State
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.window.layout.FoldingFeature
import androidx.window.layout.WindowInfoTracker
import com.chanmi.app.location.LocationManager
import com.chanmi.app.navigation.ChanmiApp
import com.chanmi.app.ui.theme.ChanmiTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * 폴더블 디바이스 자세 정보
 */
enum class DevicePosture {
    /** 일반 상태 (펼침 또는 비폴더블) */
    NORMAL,
    /** 반 접힘 (플렉스 모드) - 수평 힌지 */
    HALF_OPENED_HORIZONTAL,
    /** 반 접힘 - 수직 힌지 (북 모드) */
    HALF_OPENED_VERTICAL
}

val LocalDevicePosture = staticCompositionLocalOf { DevicePosture.NORMAL }

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var locationManager: LocationManager

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val devicePostureFlow = WindowInfoTracker.getOrCreate(this)
            .windowLayoutInfo(this)
            .map { layoutInfo ->
                val foldingFeature = layoutInfo.displayFeatures
                    .filterIsInstance<FoldingFeature>()
                    .firstOrNull()

                when {
                    foldingFeature == null -> DevicePosture.NORMAL
                    foldingFeature.state == FoldingFeature.State.HALF_OPENED &&
                            foldingFeature.orientation == FoldingFeature.Orientation.HORIZONTAL ->
                        DevicePosture.HALF_OPENED_HORIZONTAL
                    foldingFeature.state == FoldingFeature.State.HALF_OPENED &&
                            foldingFeature.orientation == FoldingFeature.Orientation.VERTICAL ->
                        DevicePosture.HALF_OPENED_VERTICAL
                    else -> DevicePosture.NORMAL
                }
            }

        setContent {
            val windowSizeClass = calculateWindowSizeClass(this)
            val devicePosture = devicePostureFlow
                .collectAsStateWithLifecycle(initialValue = DevicePosture.NORMAL)

            ChanmiTheme {
                CompositionLocalProvider(LocalDevicePosture provides devicePosture.value) {
                    ChanmiApp(
                        widthSizeClass = windowSizeClass.widthSizeClass,
                        locationManager = locationManager
                    )
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        // 앱 재시작 시 이미 부여된 위치 권한 상태를 복원
        val hasLocationPermission = ContextCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        if (hasLocationPermission) {
            locationManager.updatePermissionStatus(true)
        }
        locationManager.startLocationUpdates()
    }

    override fun onStop() {
        super.onStop()
        locationManager.stopLocationUpdates()
    }
}
