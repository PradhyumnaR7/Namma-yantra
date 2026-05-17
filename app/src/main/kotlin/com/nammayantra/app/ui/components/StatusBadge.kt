package com.nammayantra.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nammayantra.app.data.RequestStatus
import com.nammayantra.app.ui.theme.*

@Composable
fun StatusBadge(status: RequestStatus, modifier: Modifier = Modifier) {
    val (bg, text, label) = when (status) {
        RequestStatus.PENDING -> Triple(
            StatusPending.copy(alpha = 0.15f), StatusPending, "Pending"
        )
        RequestStatus.ACCEPTED -> Triple(
            StatusAccepted.copy(alpha = 0.15f), StatusAccepted, "Accepted"
        )
        RequestStatus.DECLINED -> Triple(
            StatusDeclined.copy(alpha = 0.15f), StatusDeclined, "Declined"
        )
        RequestStatus.COMPLETED -> Triple(
            StatusCompleted.copy(alpha = 0.15f), StatusCompleted, "Completed"
        )
    }
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(Radius.full))
            .background(bg)
            .padding(horizontal = 10.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = text,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.3.sp
        )
    }
}

@Composable
fun AvailabilityBadge(isAvailable: Boolean, modifier: Modifier = Modifier) {
    val bg = if (isAvailable) Available.copy(alpha = 0.15f) else Unavailable.copy(alpha = 0.15f)
    val textColor = if (isAvailable) Available else Unavailable
    val label = if (isAvailable) "Available" else "Booked"
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(Radius.full))
            .background(bg)
            .padding(horizontal = 10.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(RoundedCornerShape(Radius.full))
                    .background(textColor)
            )
            Text(
                text = label,
                color = textColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.3.sp
            )
        }
    }
}
