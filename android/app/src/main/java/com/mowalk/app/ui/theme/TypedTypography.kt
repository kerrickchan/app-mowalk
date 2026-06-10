package com.mowalk.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val LocalTypography = staticCompositionLocalOf<Typography> { Typography() }

val TypedTypography: Typography
    @Composable
    get() = LocalTypography.current

object TypographyTokens {
    val StepsDisplay = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 64.sp,
        lineHeight = 72.sp,
        letterSpacing = (-1.5).sp,
    )
}
