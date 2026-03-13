package com.chanmi.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Chanmi 타이포그래피 - iOS 시스템 폰트 스타일을 Material 3로 매핑
 *
 * iOS SwiftUI → Material 3 매핑:
 * - .title      (28pt) → displaySmall / headlineLarge
 * - .title2     (22pt) → headlineMedium
 * - .title3     (20pt) → headlineSmall
 * - .headline   (17pt bold) → titleMedium
 * - .body       (17pt) → bodyLarge
 * - .subheadline(15pt) → bodyMedium
 * - .caption    (12pt) → bodySmall
 * - .caption2   (11pt) → labelSmall
 *
 * 한국어 최적화: lineHeight를 약간 넉넉하게 설정하여 가독성 확보
 */
val ChanmiTypography = Typography(
    // iOS .title → 큰 제목 (완료 화면, 주요 타이틀)
    headlineLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        letterSpacing = 0.sp
    ),

    // iOS .title2 → 중간 제목 (월/년 헤더)
    headlineMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        lineHeight = 30.sp,
        letterSpacing = 0.sp
    ),

    // iOS .title3 → 작은 제목 (섹션 헤더)
    headlineSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),

    // iOS .headline → 강조 텍스트 (버튼 레이블, 중요 항목)
    titleMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 17.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.sp
    ),

    // 보조 타이틀
    titleSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 15.sp,
        lineHeight = 22.sp,
        letterSpacing = 0.sp
    ),

    // iOS .body → 본문 (기도문 텍스트, 일반 콘텐츠)
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 17.sp,
        lineHeight = 26.sp,  // 한국어 기도문 가독성을 위해 넉넉하게
        letterSpacing = 0.sp
    ),

    // iOS .subheadline → 부제목/보조 텍스트
    bodyMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 22.sp,
        letterSpacing = 0.sp
    ),

    // iOS .caption → 작은 텍스트 (카테고리명, 메타데이터)
    bodySmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.sp
    ),

    // 레이블 (탭 바, 작은 버튼)
    labelLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.sp
    ),

    labelMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.sp
    ),

    // iOS .caption2 → 가장 작은 텍스트
    labelSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.sp
    )
)
