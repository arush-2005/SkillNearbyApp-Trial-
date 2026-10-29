package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.SkillNearbyDatabase
import com.example.data.model.BookingStatus
import com.example.data.model.ServiceCategory
import com.example.data.model.ServicePackage
import com.example.data.model.SubscriptionTier
import com.example.data.repository.SkillNearbyRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    private lateinit var db: SkillNearbyDatabase
    private lateinit var repository: SkillNearbyRepository

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, SkillNearbyDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = SkillNearbyRepository(db.dao())
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun testAppNameString() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("SkillNearby", appName)
    }

    @Test
    fun testInitialSeedAndBookingsFlow() = runTest {
        repository.seedInitialDataIfEmpty()
        val bookings = repository.bookingsFlow.first()
        assertTrue(bookings.isNotEmpty())

        val firstBooking = bookings.first()
        assertNotNull(firstBooking.id)
        assertTrue(firstBooking.totalPrice > 0.0)
        // Verify marketplace fee calculation (5% fee)
        val expectedFee = Math.round(firstBooking.basePrice * 0.05 * 100.0) / 100.0
        assertEquals(expectedFee, firstBooking.bookingFee, 0.01)
    }

    @Test
    fun testCreateNewBooking() = runTest {
        val provider = repository.catalogProviders.first()
        val pkg = provider.packages.first()

        val booking = repository.createBooking(
            provider = provider,
            pkg = pkg,
            date = "Tomorrow",
            time = "10:00 AM",
            address = "123 Main St",
            notes = "Please bring test equipment",
            paymentMethod = "Visa ending in 4242"
        )

        assertEquals(BookingStatus.CONFIRMED, booking.status)
        assertEquals(provider.id, booking.providerId)
        assertTrue(booking.totalPrice > pkg.price)

        val allBookings = repository.bookingsFlow.first()
        assertTrue(allBookings.any { it.id == booking.id })
    }

    @Test
    fun testProviderSubscriptionUpdate() = runTest {
        repository.updateSubscriptionTier(SubscriptionTier.ELITE_BUSINESS)
        val currentTier = repository.userSubscriptionFlow.first()
        assertEquals(SubscriptionTier.ELITE_BUSINESS, currentTier)
        assertEquals(0, currentTier.platformFeePercent)
    }

    @Test
    fun testFavoriteToggle() = runTest {
        val providerId = "p_mike"
        repository.toggleFavorite(providerId, isCurrentlyFav = false)
        var favs = repository.favoritesFlow.first()
        assertTrue(favs.contains(providerId))

        repository.toggleFavorite(providerId, isCurrentlyFav = true)
        favs = repository.favoritesFlow.first()
        assertTrue(!favs.contains(providerId))
    }
}
