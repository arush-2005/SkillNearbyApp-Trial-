package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SkillNearbyDao {

    // --- Bookings ---
    @Query("SELECT * FROM bookings ORDER BY timestamp DESC")
    fun getAllBookings(): Flow<List<BookingEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBooking(booking: BookingEntity)

    @Query("UPDATE bookings SET status = :status WHERE id = :bookingId")
    suspend fun updateBookingStatus(bookingId: String, status: String)

    @Query("UPDATE bookings SET hasReview = 1 WHERE id = :bookingId")
    suspend fun markBookingReviewed(bookingId: String)

    // --- Chat Messages ---
    @Query("SELECT * FROM chat_messages ORDER BY timestamp ASC")
    fun getAllMessages(): Flow<List<ChatMessageEntity>>

    @Query("SELECT * FROM chat_messages WHERE conversationId = :convoId ORDER BY timestamp ASC")
    fun getMessagesForConversation(convoId: String): Flow<List<ChatMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessageEntity)

    // --- Favorites ---
    @Query("SELECT * FROM favorite_providers")
    fun getAllFavorites(): Flow<List<FavoriteProviderEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(fav: FavoriteProviderEntity)

    @Query("DELETE FROM favorite_providers WHERE providerId = :providerId")
    suspend fun deleteFavorite(providerId: String)

    // --- User Reviews ---
    @Query("SELECT * FROM user_submitted_reviews WHERE providerId = :providerId ORDER BY timestamp DESC")
    fun getReviewsForProvider(providerId: String): Flow<List<UserReviewEntity>>

    @Query("SELECT * FROM user_submitted_reviews ORDER BY timestamp DESC")
    fun getAllUserReviews(): Flow<List<UserReviewEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReview(review: UserReviewEntity)

    // --- Subscription ---
    @Query("SELECT * FROM provider_subscription WHERE id = 1 LIMIT 1")
    fun getProviderSubscription(): Flow<ProviderSubscriptionEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setProviderSubscription(subscription: ProviderSubscriptionEntity)
}
