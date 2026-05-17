package com.nammayantra.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nammayantra.app.data.BookingRequest
import com.nammayantra.app.data.MachineType
import com.nammayantra.app.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun BookingCard(
    request: BookingRequest,
    modifier: Modifier = Modifier,
    showActions: Boolean = false,
    onAccept: (() -> Unit)? = null,
    onDecline: (() -> Unit)? = null
) {
    val status = request.getStatus()
    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .shadow(elevation = 2.dp, shape = RoundedCornerShape(Radius.lg), spotColor = Primary.copy(alpha = 0.06f)),
        shape = RoundedCornerShape(Radius.lg),
        colors = CardDefaults.cardColors(containerColor = Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(Radius.sm))
                            .background(PrimaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = MachineType.emoji(request.machineType),
                            fontSize = 20.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = request.machineName.ifEmpty { request.machineType },
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                        Text(
                            text = if (showActions) "From: ${request.requesterName}" else "Owner: ${request.ownerName}",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }
                StatusBadge(status = status)
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 12.dp),
                color = Divider,
                thickness = 1.dp
            )

            // Details row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Date
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = null,
                        tint = TextHint,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = dateFormat.format(Date(request.startDate)),
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
                // Duration
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AccessTime,
                        contentDescription = null,
                        tint = TextHint,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${request.durationHours}h • ${request.getRentalType().displayName()}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
                // Price
                Text(
                    text = "₹${request.totalPrice.toInt()}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Primary
                )
            }

            // Owner action buttons
            if (showActions && status == com.nammayantra.app.data.RequestStatus.PENDING) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { onDecline?.invoke() },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Error),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Error.copy(alpha = 0.5f))
                    ) {
                        Text("Decline", style = MaterialTheme.typography.labelMedium)
                    }
                    Button(
                        onClick = { onAccept?.invoke() },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Success)
                    ) {
                        Text("Accept", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }
    }
}
