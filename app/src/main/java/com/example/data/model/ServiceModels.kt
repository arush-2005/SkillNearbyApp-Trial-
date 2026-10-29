package com.example.data.model

enum class ServiceCategory(val displayName: String, val iconName: String) {
    ALL("All Services", "all"),
    TUTORING("Academic Tutors", "school"),
    HOME_REPAIR("Repairs & Handyman", "build"),
    DESIGN("Design & Branding", "design"),
    ELECTRICAL("Electrician", "electric"),
    CLEANING("Home Cleaning", "cleaning"),
    TECH_SUPPORT("Tech & Gadgets", "computer"),
    FITNESS("Personal Training", "fitness"),
    PHOTOGRAPHY("Photography", "camera")
}

data class ServicePackage(
    val id: String,
    val title: String,
    val description: String,
    val price: Double,
    val estimatedDurationHours: Double,
    val isPopular: Boolean = false
)

data class PortfolioItem(
    val title: String,
    val category: String,
    val description: String,
    val completedDate: String
)

data class VerifiedReview(
    val id: String,
    val reviewerName: String,
    val rating: Int,
    val date: String,
    val serviceRendered: String,
    val comment: String,
    val verifiedJob: Boolean = true
)

data class ServiceProvider(
    val id: String,
    val name: String,
    val title: String,
    val category: ServiceCategory,
    val rating: Double,
    val reviewCount: Int,
    val hourlyRate: Double,
    val distanceMiles: Double,
    val responseTimeMinutes: Int,
    val isVerified: Boolean,
    val isProSubscriber: Boolean,
    val bio: String,
    val locationArea: String,
    val jobsCompleted: Int,
    val skills: List<String>,
    val packages: List<ServicePackage>,
    val portfolio: List<PortfolioItem>,
    val reviews: List<VerifiedReview>,
    val avatarBgColor: Long = 0xFF0D9488
)

enum class BookingStatus(val label: String) {
    REQUESTED("Requested"),
    CONFIRMED("Confirmed"),
    EN_ROUTE("En Route"),
    IN_PROGRESS("In Progress"),
    COMPLETED("Completed"),
    CANCELLED("Cancelled")
}

data class Booking(
    val id: String,
    val providerId: String,
    val providerName: String,
    val providerTitle: String,
    val category: ServiceCategory,
    val packageTitle: String,
    val serviceDate: String,
    val serviceTime: String,
    val serviceLocation: String,
    val notes: String,
    val basePrice: Double,
    val bookingFee: Double,
    val taxAmount: Double,
    val totalPrice: Double,
    val status: BookingStatus,
    val paymentMethod: String,
    val timestamp: Long = System.currentTimeMillis(),
    val hasReview: Boolean = false
)

data class ChatMessage(
    val id: String,
    val conversationId: String,
    val providerId: String,
    val senderName: String,
    val text: String,
    val timestamp: Long,
    val isFromUser: Boolean,
    val quotePrice: Double? = null
)

data class ConversationPreview(
    val conversationId: String,
    val providerId: String,
    val providerName: String,
    val providerTitle: String,
    val lastMessage: String,
    val timestamp: Long,
    val unreadCount: Int = 0,
    val isVerified: Boolean = true
)

enum class SubscriptionTier(
    val tierName: String,
    val priceMonthly: Double,
    val platformFeePercent: Int,
    val badgeTitle: String
) {
    FREE_STARTER("Starter Partner", 0.0, 10, "Standard Listing"),
    PRO_PARTNER("SkillNearby Pro", 19.99, 3, "Verified Pro Badge"),
    ELITE_BUSINESS("Elite Business", 49.99, 0, "0% Fee + Priority")
}

data class ProviderMetrics(
    val grossBookingsTotal: Double,
    val completedJobsCount: Int,
    val feesRetained: Double,
    val averageRating: Double,
    val profileViewsThisWeek: Int
)

data class SearchFilter(
    val query: String = "",
    val category: ServiceCategory = ServiceCategory.ALL,
    val maxDistanceMiles: Float = 25f,
    val minRating: Float = 0f,
    val verifiedOnly: Boolean = false,
    val proOnly: Boolean = false,
    val maxHourlyRate: Float = 200f
)
