package com.chanmi.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.staticCompositionLocalOf
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
                .collectAsState(initial = DevicePosture.NORMAL)

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
        locationManager.startLocationUpdates()
    }

    override fun onStop() {
        super.onStop()
        locationManager.stopLocationUpdates()
    }
}
