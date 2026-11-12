package com.ginogipsy.sanmartinoapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ginogipsy.sanmartinoapp.ui.theme.SanMartinoAppTheme

/**
 * Componente per il numero della cantina (ispirato al marcatore della mappa del sito web).
 */
@Composable
fun CantinaNumber(
    number: String,
    modifier: Modifier = Modifier
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(32.dp)
            .clip(CircleShape)
            // Sfondo rosso del marcatore
            .background(MaterialTheme.colorScheme.tertiary)
            // Bordino bianco per risaltare (opzionale, ma aiuta se lo sfondo è scuro)
            .border(2.dp, Color.White, CircleShape)
    ) {
        Text(
            text = number,
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Preview
@Composable
private fun CantinaNumberPreview() {
    SanMartinoAppTheme {
        CantinaNumber(number = "1")
    }
}