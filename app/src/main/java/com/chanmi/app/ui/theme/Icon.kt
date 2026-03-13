package com.chanmi.app.ui.theme

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Church
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Article
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import com.chanmi.app.R

/**
 * iOS SF Symbol → Android Material Icon 매핑
 *
 * 탭 아이콘과 기도문 카테고리 아이콘을 통합 관리합니다.
 */
object ChanmiIcons {

    // ===== 탭 바 아이콘 =====
    val RosaryTab: ImageVector = Icons.Filled.AutoAwesome      // hands.and.sparkles
    val CalendarTab: ImageVector = Icons.Filled.CalendarMonth   // calendar
    val PrayersTab: ImageVector = Icons.Filled.MenuBook         // book
    val NearbyChurchTab: ImageVector = Icons.Filled.LocationOn  // mappin.and.ellipse

    // ===== 기도문 카테고리 아이콘 (prayers.json icon 필드 매핑) =====

    /**
     * prayers.json의 SF Symbol 이름을 Material Icon으로 변환합니다.
     * @Composable 컨텍스트가 필요합니다 (커스텀 벡터 리소스 로드를 위해).
     *
     * 카테고리별 매핑:
     * - sun.max          → LightMode             (일상 기도)
     * - book.closed      → Book                  (주요 기도)
     * - building.columns → Church                (미사 기도)
     * - circle.grid.cross → RadioButtonUnchecked (묵주 기도)
     * - star             → Star                  (성모 기도)
     * - cross            → ic_latin_cross (커스텀) (십자가의 길)
     * - candle           → Favorite              (연도 기도)
     * - person.3         → Groups                (성인 기도)
     */
    @Composable
    fun fromSfSymbol(sfSymbol: String): ImageVector = when (sfSymbol) {
        // 기도문 카테고리
        "sun.max" -> Icons.Filled.LightMode
        "book.closed" -> Icons.Filled.Book
        "building.columns" -> Icons.Filled.Church
        "circle.grid.cross" -> Icons.Filled.RadioButtonUnchecked
        "star" -> Icons.Filled.Star
        "cross" -> ImageVector.vectorResource(R.drawable.ic_latin_cross)
        "candle" -> Icons.Filled.Favorite
        "person.3" -> Icons.Filled.Groups

        // 탭 바 (참조용)
        "hands.and.sparkles" -> Icons.Filled.AutoAwesome
        "calendar" -> Icons.Filled.CalendarMonth
        "book" -> Icons.Filled.MenuBook
        "mappin.and.ellipse" -> Icons.Filled.LocationOn

        // 기타 앱 내 사용 아이콘
        "heart.fill" -> Icons.Filled.Favorite
        "star.fill" -> Icons.Filled.Star

        // 기본값
        else -> Icons.Outlined.Article
    }
}
