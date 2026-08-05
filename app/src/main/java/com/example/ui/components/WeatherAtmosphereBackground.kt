package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.example.data.model.WeatherCondition

@Composable
fun WeatherAtmosphereBackground(
    condition: WeatherCondition?,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val defaultColors = listOf(Color(0xFF0284C7), Color(0xFF38BDF8), Color(0xFFBAE6FD))
    val targetColors = condition?.themeGradient ?: defaultColors

    val color1 by animateColorAsState(targetValue = targetColors[0], animationSpec = tween(1000), label = "bg_color1")
    val color2 by animateColorAsState(targetValue = targetColors.getOrElse(1) { targetColors[0] }, animationSpec = tween(1000), label = "bg_color2")
    val color3 by animateColorAsState(targetValue = targetColors.getOrElse(2) { targetColors[0] }, animationSpec = tween(1000), label = "bg_color3")

    val gradientBrush = Brush.verticalGradient(
        colors = listOf(color1, color2, color3)
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(brush = gradientBrush)
    ) {
        content()
    }
}
