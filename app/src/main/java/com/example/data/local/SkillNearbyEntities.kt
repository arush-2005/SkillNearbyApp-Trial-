package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bookings")
data class BookingEntity(
    @PrimaryKey val id: String,
    val providerId: String,
    val providerName: String,
    val providerTitle: String,
    val category: String,
    val packageTitle: String,
    val serviceDate: String,
    val serviceTime: String,
    val serviceLocation: String,
    val notes: String,
    val basePrice: Double,
    val bookingFee: Double,
    val taxAmount: Double,
    val totalPrice: Double,
    val status: String,
    val paymentMethod: String,
    val timestamp: Long,
    val hasReview: Boolean = false
)

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey val id: String,
    val conversationId: String,
    val providerId: String,
    val senderName: String,
    val text: String,
    val timestamp: Long,
    val isFromUser: Boolean,
    val quotePrice: Double? = null
)

@Entity(tableName = "favorite_providers")
data class FavoriteProviderEntity(
    @PrimaryKey val providerId: String,
    val addedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "user_submitted_reviews")
data class UserReviewEntity(
    @PrimaryKey val id: String,
    val providerId: String,
    val rating: Int,
    val comment: String,
    val serviceTitle: String,
    val date: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "provider_subscription")
data class ProviderSubscriptionEntity(
    @PrimaryKey val id: Int = 1,
    val currentTier: String,
    val updatedAt: Long = System.currentTimeMillis()
)
