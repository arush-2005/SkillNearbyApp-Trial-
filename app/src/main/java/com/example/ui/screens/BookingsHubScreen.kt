package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Booking
import com.example.data.model.BookingStatus
import com.example.ui.components.CategoryIcon
import com.example.ui.components.EmptyStateCard
import com.example.ui.theme.AmberDark
import com.example.ui.theme.AmberStar
import com.example.ui.theme.ElectricTeal
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.RoseError

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingsHubScreen(
    bookings: List<Booking>,
    onMessageProvider: (String) -> Unit, // providerId
    onCancelBooking: (String) -> Unit,
    onCompleteBooking: (String) -> Unit,
    onOpenReviewDialog: (Booking) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var viewingReceiptBooking by remember { mutableStateOf<Booking?>(null) }

    val activeBookings = bookings.filter { it.status == BookingStatus.REQUESTED || it.status == BookingStatus.CONFIRMED || it.status == BookingStatus.EN_ROUTE || it.status == BookingStatus.IN_PROGRESS }
    val pastBookings = bookings.filter { it.status == BookingStatus.COMPLETED || it.status == BookingStatus.CANCELLED }

    val displayedList = if (selectedTab == 0) activeBookings else pastBookings

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("bookings_hub_screen")
    ) {
        // Top Header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Text(
                text = "My Service Bookings",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Track active jobs, appointments, and receipts",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(10.dp))

            SecondaryTabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Active & Upcoming (${activeBookings.size})", fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Completed & History (${pastBookings.size})", fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal) }
                )
            }
        }

        if (displayedList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                EmptyStateCard(
                    title = if (selectedTab == 0) "No Active Bookings" else "No Completed History",
                    description = if (selectedTab == 0)
                        "You don't have any appointments scheduled right now. Explore specialists and book in seconds!"
                    else "Completed services and verified invoices will be archived here.",
                    icon = Icons.Filled.CalendarMonth
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(displayedList, key = { it.id }) { booking ->
                    BookingCard(
                        booking = booking,
                        onMessage = { onMessageProvider(booking.providerId) },
                        onCancel = { onCancelBooking(booking.id) },
                        onComplete = { onCompleteBooking(booking.id) },
                        onLeaveReview = { onOpenReviewDialog(booking) },
                        onViewReceipt = { viewingReceiptBooking = booking }
                    )
                }
            }
        }
    }

    // Receipt Modal Dialog
    viewingReceiptBooking?.let { b ->
        ReceiptDialog(
            booking = b,
            onDismiss = { viewingReceiptBooking = null }
        )
    }
}

@Composable
fun BookingCard(
    booking: Booking,
    onMessage: () -> Unit,
    onCancel: () -> Unit,
    onComplete: () -> Unit,
    onLeaveReview: () -> Unit,
    onViewReceipt: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .fillMaxWidth()
            .testTag("booking_card_${booking.id}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row: Reference ID + Status Pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "#${booking.id}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = ElectricTeal
                )

                BookingStatusPill(status = booking.status)
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Specialist & Service details
            Row(verticalAlignment = Alignment.CenterVertically) {
                CategoryIcon(category = booking.category, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = booking.providerName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Text(
                        text = booking.packageTitle,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Time & Location Details
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Filled.CalendarMonth, contentDescription = null, tint = ElectricTeal, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = booking.serviceDate, fontSize = 12.sp, fontWeight = FontWeight.Medium)
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Filled.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = booking.serviceLocation, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(10.dp))

            // Total Paid & Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "Total Charged", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        text = "$${String.format("%.2f", booking.totalPrice)}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = ElectricTeal
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = onViewReceipt,
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Icon(imageVector = Icons.Filled.Receipt, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Receipt", fontSize = 11.sp)
                    }

                    if (booking.status == BookingStatus.CONFIRMED || booking.status == BookingStatus.REQUESTED) {
                        Button(
                            onClick = onMessage,
                            colors = ButtonDefaults.buttonColors(containerColor = ElectricTeal),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(imageVector = Icons.Filled.ChatBubbleOutline, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Chat", fontSize = 11.sp)
                        }
                    } else if (booking.status == BookingStatus.COMPLETED) {
                        if (!booking.hasReview) {
                            Button(
                                onClick = onLeaveReview,
                                colors = ButtonDefaults.buttonColors(containerColor = AmberDark),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Icon(imageVector = Icons.Filled.Star, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Review", fontSize = 11.sp)
                            }
                        } else {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFFD1FAE5)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(imageVector = Icons.Filled.CheckCircle, contentDescription = null, tint = EmeraldSuccess, modifier = Modifier.size(13.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Reviewed", fontSize = 11.sp, color = EmeraldSuccess, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BookingStatusPill(status: BookingStatus) {
    val (bgColor, textColor) = when (status) {
        BookingStatus.CONFIRMED -> Color(0xFFCCFBF1) to ElectricTeal
        BookingStatus.REQUESTED -> Color(0xFFFEF3C7) to AmberDark
        BookingStatus.EN_ROUTE, BookingStatus.IN_PROGRESS -> Color(0xFFE0E7FF) to Color(0xFF4338CA)
        BookingStatus.COMPLETED -> Color(0xFFD1FAE5) to EmeraldSuccess
        BookingStatus.CANCELLED -> Color(0xFFF1F5F9) to Color(0xFF64748B)
    }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = bgColor
    ) {
        Text(
            text = status.label.uppercase(),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = textColor,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        )
    }
}

@Composable
fun ReceiptDialog(
    booking: Booking,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Official Booking Invoice", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(text = "#${booking.id}", color = ElectricTeal, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(text = "Provider: ${booking.providerName}", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                Text(text = "Service: ${booking.packageTitle}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(text = "Date: ${booking.serviceDate}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(text = "Payment: ${booking.paymentMethod}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(10.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(text = "Base Service Fee", fontSize = 12.sp)
                    Text(text = "$${String.format("%.2f", booking.basePrice)}", fontSize = 12.sp)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(text = "SkillNearby Fee (5% Protection Guarantee)", fontSize = 12.sp, color = ElectricTeal)
                    Text(text = "$${String.format("%.2f", booking.bookingFee)}", fontSize = 12.sp, color = ElectricTeal)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(text = "Estimated Tax", fontSize = 12.sp)
                    Text(text = "$${String.format("%.2f", booking.taxAmount)}", fontSize = 12.sp)
                }

                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Total Charged", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text(
                        text = "$${String.format("%.2f", booking.totalPrice)}",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp,
                        color = ElectricTeal
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "✓ Includes SkillNearby \$1,000,000 property protection and guaranteed resolution coverage.",
                    fontSize = 10.sp,
                    color = EmeraldSuccess
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = ElectricTeal)
            ) {
                Text("Close Invoice")
            }
        }
    )
}
