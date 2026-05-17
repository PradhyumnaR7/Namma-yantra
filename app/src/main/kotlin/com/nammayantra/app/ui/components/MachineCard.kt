package com.nammayantra.app.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.nammayantra.app.data.Machine
import com.nammayantra.app.data.MachineType
import com.nammayantra.app.ui.theme.*

@Composable
fun MachineCard(
    machine: Machine,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.97f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "card_scale"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .shadow(elevation = 3.dp, shape = RoundedCornerShape(Radius.lg), spotColor = Primary.copy(alpha = 0.08f))
            .clickable {
                pressed = true
                onClick()
            },
        shape = RoundedCornerShape(Radius.lg),
        colors = CardDefaults.cardColors(containerColor = Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Machine image with type emoji fallback
            Box(
                modifier = Modifier
                    .size(88.dp)
                    .clip(RoundedCornerShape(Radius.md)),
                contentAlignment = Alignment.Center
            ) {
                if (machine.imageUrl.isNotEmpty()) {
                    AsyncImage(
                        model = machine.imageUrl,
                        contentDescription = machine.type,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.linearGradient(listOf(PrimaryContainer, SurfaceVariant))
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = MachineType.emoji(machine.type),
                            fontSize = 36.sp
                        )
                    }
                }
                // Availability dot overlay
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                        .size(10.dp)
                        .clip(RoundedCornerShape(Radius.full))
                        .background(if (machine.isAvailable) Available else Unavailable)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                // Type + name
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = machine.name.ifEmpty { machine.type },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    AvailabilityBadge(isAvailable = machine.isAvailable)
                }

                Spacer(modifier = Modifier.height(2.dp))

                // Owner
                Text(
                    text = machine.ownerName,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Price row
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "₹${machine.hourlyRate.toInt()}/hr",
                        style = MaterialTheme.typography.labelLarge,
                        color = Primary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "  •  ₹${machine.dailyRate.toInt()}/day",
                        style = MaterialTheme.typography.labelMedium,
                        color = TextSecondary
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Distance + rating row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = TextHint,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = if (machine.distanceKm > 0)
                                "${"%.1f".format(machine.distanceKm)} km"
                            else machine.village.ifEmpty { machine.district },
                            style = MaterialTheme.typography.labelSmall,
                            color = TextHint
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = Warning,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = "${"%.1f".format(machine.conditionRating)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary
                        )
                    }
                }
            }
        }
    }
}
