package com.chanmi.app.ui.theme

import androidx.compose.ui.graphics.Color

// ===== iOS Asset Catalog 색상 매핑 =====

// AccentColor - 앱 강조색 (탭 선택, 버튼, 진행 표시)
val AccentColorLight = Color(0xFF3C3C2E)   // RGB(60, 60, 46)
val AccentColorDark = Color(0xFFC8B08A)    // RGB(200, 176, 138)

// AppBackground - 전체 배경색
val AppBackgroundLight = Color(0xFFF5F0E8) // RGB(245, 240, 232)
val AppBackgroundDark = Color(0xFF1C1C1C)  // RGB(28, 28, 28)

// CardBackground - 카드/셀 배경
val CardBackgroundLight = Color(0xFFFFFFFF) // RGB(255, 255, 255)
val CardBackgroundDark = Color(0xFF2C2C2C)  // RGB(44, 44, 44)

// GoldAccent - 성당 마커, 묵주 구슬, 기도문 카테고리 아이콘
val GoldAccentLight = Color(0xFFB8A472)    // RGB(184, 164, 114)
val GoldAccentDark = Color(0xFFCCB580)     // RGB(204, 181, 128) - iOS 정확 일치

// ===== 시맨틱 컬러 (iOS 시스템 색상 대응) =====

// 묵주기도 인디케이터 (달력 도트)
val RosaryIndicator = Color(0xFF9C27B0)     // Purple 계열
val RosaryIndicatorDark = Color(0xFFCE93D8)

// 선행 인디케이터 (달력 도트)
val GoodDeedIndicator = Color(0xFFE91E63)   // Pink 계열
val GoodDeedIndicatorDark = Color(0xFFF48FB1)

// 달력 - 일요일/토요일 강조
val SundayColor = Color(0xFFD32F2F)         // Red
val SundayColorDark = Color(0xFFEF9A9A)
val SaturdayColor = Color(0xFF1976D2)       // Blue
val SaturdayColorDark = Color(0xFF90CAF9)

// ===== Material 3 확장 컬러 =====

// On 컬러 (텍스트/아이콘 대비용)
val OnAccentLight = Color(0xFFFFFFFF)
val OnAccentDark = Color(0xFF1C1C1C)

// Surface 변형 (카드 내부 섹션 구분용)
val SurfaceVariantLight = Color(0xFFF0EBE3) // AppBackground보다 약간 밝은
val SurfaceVariantDark = Color(0xFF363636)  // CardBackground보다 약간 밝은

// Outline (구분선, 테두리)
val OutlineLight = Color(0xFFD5CFC5)
val OutlineDark = Color(0xFF4A4A4A)
