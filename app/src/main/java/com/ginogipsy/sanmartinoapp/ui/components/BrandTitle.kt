package com.ginogipsy.sanmartinoapp.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.ginogipsy.sanmartinoapp.ui.theme.EthnaFont

@Composable
fun BrandTitle(
    text: String,
    modifier: Modifier = Modifier
) {
    val goldColor = Color(0xFFF2B84B)
    val strokeColor = Color(0xFF1A110B)
    val shadowColor = Color(0xCC0F0906)

    Box(modifier = modifier) {
        // Outline text with shadow
        Text(
            text = text.uppercase(),
            fontFamily = EthnaFont,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 24.sp,
            letterSpacing = 1.2.sp, // ~ 0.05em
            color = strokeColor,
            style = TextStyle.Default.copy(
                drawStyle = Stroke(
                    miter = 10f,
                    width = 4f,
                ),
                shadow = Shadow(
                    color = shadowColor,
                    offset = Offset(0f, 4f),
                    blurRadius = 8f
                )
            )
        )
        // Fill text
        Text(
            text = text.uppercase(),
            fontFamily = EthnaFont,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 24.sp,
            letterSpacing = 1.2.sp,
            color = goldColor,
        )
    }
}
