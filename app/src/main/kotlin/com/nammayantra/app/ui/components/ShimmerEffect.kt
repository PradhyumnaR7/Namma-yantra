package com.nammayantra.app.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import com.nammayantra.app.ui.theme.Shimmer
import com.nammayantra.app.ui.theme.ShimmerHighlight

@Composable
fun shimmerBrush(): Brush {
    val shimmerColors = listOf(
        Shimmer,
        ShimmerHighlight,
        Shimmer,
    )
    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_translate"
    )
    return Brush.linearGradient(
        colors = shimmerColors,
        start = Offset.Zero,
        end = Offset(x = translateAnim, y = translateAnim)
    )
}

@Composable
fun MachineCardShimmer(modifier: Modifier = Modifier) {
    val brush = shimmerBrush()
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(androidx.compose.ui.graphics.Color.White)
            .padding(16.dp)
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(brush)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(modifier = Modifier.fillMaxWidth(0.6f).height(16.dp).clip(RoundedCornerShape(4.dp)).background(brush))
            Box(modifier = Modifier.fillMaxWidth(0.4f).height(12.dp).clip(RoundedCornerShape(4.dp)).background(brush))
            Box(modifier = Modifier.fillMaxWidth(0.5f).height(12.dp).clip(RoundedCornerShape(4.dp)).background(brush))
            Box(modifier = Modifier.fillMaxWidth(0.3f).height(20.dp).clip(RoundedCornerShape(8.dp)).background(brush))
        }
    }
}

@Composable
fun BookingCardShimmer(modifier: Modifier = Modifier) {
    val brush = shimmerBrush()
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(androidx.compose.ui.graphics.Color.White)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            Box(modifier = Modifier.fillMaxWidth(0.5f).height(16.dp).clip(RoundedCornerShape(4.dp)).background(brush))
            Box(modifier = Modifier.width(70.dp).height(24.dp).clip(RoundedCornerShape(12.dp)).background(brush))
        }
        Box(modifier = Modifier.fillMaxWidth(0.7f).height(12.dp).clip(RoundedCornerShape(4.dp)).background(brush))
        Box(modifier = Modifier.fillMaxWidth(0.4f).height(12.dp).clip(RoundedCornerShape(4.dp)).background(brush))
    }
}
