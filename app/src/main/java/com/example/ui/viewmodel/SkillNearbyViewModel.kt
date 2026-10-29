package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.SkillNearbyDatabase
import com.example.data.model.Booking
import com.example.data.model.BookingStatus
import com.example.data.model.ChatMessage
import com.example.data.model.ConversationPreview
import com.example.data.model.SearchFilter
import com.example.data.model.ServiceCategory
import com.example.data.model.ServicePackage
import com.example.data.model.ServiceProvider
import com.example.data.model.SubscriptionTier
import com.example.data.repository.SkillNearbyRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class MainTab {
    EXPLORE,
    BOOKINGS,
    MESSAGES,
    PROVIDER_HUB,
    PROFILE
}

enum class SortOption(val label: String) {
    RECOMMENDED("Top Rated & Pro"),
    DISTANCE("Closest First"),
    PRICE_LOW("Lowest Price"),
    REVIEWS("Most Reviewed")
}

data class BookingCheckoutState(
    val provider: ServiceProvider? = null,
    val selectedPackage: ServicePackage? = null,
    val selectedDate: String = "Tomorrow, Oct 30",
    val selectedTime: String = "10:00 AM",
    val serviceAddress: String = "742 Evergreen Terrace, Apt 4B",
    val notes: String = "",
    val paymentMethod: String = "Visa •••• 4242",
    val isProcessingPayment: Boolean = false,
    val confirmedBooking: Booking? = null
)

class SkillNearbyViewModel(application: Application) : AndroidViewModel(application) {

    private val db = SkillNearbyDatabase.getDatabase(application)
    private val repository = SkillNearbyRepository(db.dao())

    // Navigation State
    private val _currentTab = MutableStateFlow(MainTab.EXPLORE)
    val currentTab: StateFlow<MainTab> = _currentTab.asStateFlow()

    private val _selectedProvider = MutableStateFlow<ServiceProvider?>(null)
    val selectedProvider: StateFlow<ServiceProvider?> = _selectedProvider.asStateFlow()

    private val _activeChatConversationId = MutableStateFlow<String?>(null)
    val activeChatConversationId: StateFlow<String?> = _activeChatConversationId.asStateFlow()

    private val _activeChatProvider = MutableStateFlow<ServiceProvider?>(null)
    val activeChatProvider: StateFlow<ServiceProvider?> = _activeChatProvider.asStateFlow()

    // Checkout Flow State
    private val _checkoutState = MutableStateFlow(BookingCheckoutState())
    val checkoutState: StateFlow<BookingCheckoutState> = _checkoutState.asStateFlow()

    // Leave Review Dialog State
    private val _reviewDialogBooking = MutableStateFlow<Booking?>(null)
    val reviewDialogBooking: StateFlow<Booking?> = _reviewDialogBooking.asStateFlow()

    // Search and Filters
    private val _searchFilter = MutableStateFlow(SearchFilter())
    val searchFilter: StateFlow<SearchFilter> = _searchFilter.asStateFlow()

    private val _sortOption = MutableStateFlow(SortOption.RECOMMENDED)
    val sortOption: StateFlow<SortOption> = _sortOption.asStateFlow()

    // User Feedback Snackbars
    private val _snackbarMessage = MutableSharedFlow<String>()
    val snackbarMessage: SharedFlow<String> = _snackbarMessage.asSharedFlow()

    // Favorites
    val favorites: StateFlow<Set<String>> = repository.favoritesFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptySet()
    )

    // Bookings
    val bookings: StateFlow<List<Booking>> = repository.bookingsFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Chat Messages
    val allMessages: StateFlow<List<ChatMessage>> = repository.messagesFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Provider Subscription Tier
    val subscriptionTier: StateFlow<SubscriptionTier> = repository.userSubscriptionFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SubscriptionTier.PRO_PARTNER
    )

    // Filtered and Sorted Providers
    val filteredProviders: StateFlow<List<ServiceProvider>> = combine(
        _searchFilter,
        _sortOption,
        favorites
    ) { filter, sort, favs ->
        var list = repository.catalogProviders

        // Category filter
        if (filter.category != ServiceCategory.ALL) {
            list = list.filter { it.category == filter.category }
        }

        // Query search
        if (filter.query.isNotBlank()) {
            val q = filter.query.trim().lowercase()
            list = list.filter { p ->
                p.name.lowercase().contains(q) ||
                p.title.lowercase().contains(q) ||
                p.skills.any { it.lowercase().contains(q) } ||
                p.category.displayName.lowercase().contains(q) ||
                p.bio.lowercase().contains(q)
            }
        }

        // Distance filter
        list = list.filter { it.distanceMiles <= filter.maxDistanceMiles }

        // Rating filter
        if (filter.minRating > 0f) {
            list = list.filter { it.rating >= filter.minRating }
        }

        // Verified filter
        if (filter.verifiedOnly) {
            list = list.filter { it.isVerified }
        }

        // Pro subscriber filter
        if (filter.proOnly) {
            list = list.filter { it.isProSubscriber }
        }

        // Sorting
        when (sort) {
            SortOption.RECOMMENDED -> list.sortedWith(
                compareByDescending<ServiceProvider> { it.isProSubscriber }
                    .thenByDescending { it.rating }
                    .thenBy { it.distanceMiles }
            )
            SortOption.DISTANCE -> list.sortedBy { it.distanceMiles }
            SortOption.PRICE_LOW -> list.sortedBy { it.hourlyRate }
            SortOption.REVIEWS -> list.sortedByDescending { it.reviewCount }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = repository.catalogProviders
    )

    // Conversations List Derived from repository messages flow
    val conversations: StateFlow<List<ConversationPreview>> = repository.messagesFlow.map { msgs: List<ChatMessage> ->
        val grouped = msgs.groupBy { it.conversationId }
        grouped.mapNotNull { entry ->
            val convoId = entry.key
            val convoMsgs = entry.value
            val lastMsg = convoMsgs.maxByOrNull { it.timestamp } ?: return@mapNotNull null
            val provider = repository.getProviderById(lastMsg.providerId)
            ConversationPreview(
                conversationId = convoId,
                providerId = lastMsg.providerId,
                providerName = provider?.name ?: lastMsg.senderName,
                providerTitle = provider?.title ?: "Service Specialist",
                lastMessage = lastMsg.text,
                timestamp = lastMsg.timestamp,
                unreadCount = if (!lastMsg.isFromUser) 1 else 0,
                isVerified = provider?.isVerified ?: true
            )
        }.sortedByDescending { it.timestamp }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    init {
        viewModelScope.launch {
            repository.seedInitialDataIfEmpty()
        }
    }

    // --- Navigation Actions ---
    fun selectTab(tab: MainTab) {
        _currentTab.value = tab
        // Reset sub-screens when switching primary tabs
        _selectedProvider.value = null
        _activeChatConversationId.value = null
        _activeChatProvider.value = null
    }

    fun openProviderDetail(provider: ServiceProvider) {
        _selectedProvider.value = provider
    }

    fun closeProviderDetail() {
        _selectedProvider.value = null
    }

    fun startBookingFlow(provider: ServiceProvider, pkg: ServicePackage? = null) {
        val chosenPackage = pkg ?: provider.packages.firstOrNull() ?: ServicePackage(
            id = "custom_pkg",
            title = "Hourly Direct Service",
            description = "Standard 1-hour appointment session with specialist.",
            price = provider.hourlyRate,
            estimatedDurationHours = 1.0
        )
        _checkoutState.value = BookingCheckoutState(
            provider = provider,
            selectedPackage = chosenPackage,
            confirmedBooking = null,
            isProcessingPayment = false
        )
    }

    fun closeBookingFlow() {
        _checkoutState.value = BookingCheckoutState()
    }

    fun updateCheckoutDate(date: String) {
        _checkoutState.value = _checkoutState.value.copy(selectedDate = date)
    }

    fun updateCheckoutTime(time: String) {
        _checkoutState.value = _checkoutState.value.copy(selectedTime = time)
    }

    fun updateCheckoutAddress(addr: String) {
        _checkoutState.value = _checkoutState.value.copy(serviceAddress = addr)
    }

    fun updateCheckoutNotes(notes: String) {
        _checkoutState.value = _checkoutState.value.copy(notes = notes)
    }

    fun updatePaymentMethod(method: String) {
        _checkoutState.value = _checkoutState.value.copy(paymentMethod = method)
    }

    fun submitBookingAndPay() {
        val state = _checkoutState.value
        val provider = state.provider ?: return
        val pkg = state.selectedPackage ?: return

        viewModelScope.launch {
            _checkoutState.value = state.copy(isProcessingPayment = true)
            // Realistic simulated secure 256-bit SSL gateway processing
            delay(1200)

            val booking = repository.createBooking(
                provider = provider,
                pkg = pkg,
                date = state.selectedDate,
                time = state.selectedTime,
                address = state.serviceAddress,
                notes = state.notes,
                paymentMethod = state.paymentMethod
            )

            _checkoutState.value = _checkoutState.value.copy(
                isProcessingPayment = false,
                confirmedBooking = booking
            )
            _snackbarMessage.emit("Payment processed securely! Booking #${booking.id} confirmed.")
        }
    }

    // --- Chat Actions ---
    fun openChatWithProvider(provider: ServiceProvider) {
        val convoId = "convo_${provider.id}"
        _activeChatConversationId.value = convoId
        _activeChatProvider.value = provider
        _selectedProvider.value = null // Switch to chat view
    }

    fun openConversation(preview: ConversationPreview) {
        val provider = repository.getProviderById(preview.providerId)
        _activeChatConversationId.value = preview.conversationId
        _activeChatProvider.value = provider
    }

    fun closeChat() {
        _activeChatConversationId.value = null
        _activeChatProvider.value = null
    }

    fun sendChatMessage(text: String) {
        val convoId = _activeChatConversationId.value ?: return
        val provider = _activeChatProvider.value ?: return
        if (text.isBlank()) return

        viewModelScope.launch {
            repository.sendMessage(convoId, provider.id, text.trim())
            // Simulate provider reply after brief realistic pause
            delay(1500)
            repository.simulateProviderReply(convoId, provider, text.trim())
        }
    }

    // --- Search & Filter Actions ---
    fun updateSearchQuery(query: String) {
        _searchFilter.value = _searchFilter.value.copy(query = query)
    }

    fun updateCategory(category: ServiceCategory) {
        _searchFilter.value = _searchFilter.value.copy(category = category)
    }

    fun updateMaxDistance(miles: Float) {
        _searchFilter.value = _searchFilter.value.copy(maxDistanceMiles = miles)
    }

    fun toggleVerifiedOnly() {
        _searchFilter.value = _searchFilter.value.copy(verifiedOnly = !_searchFilter.value.verifiedOnly)
    }

    fun toggleProOnly() {
        _searchFilter.value = _searchFilter.value.copy(proOnly = !_searchFilter.value.proOnly)
    }

    fun setSortOption(option: SortOption) {
        _sortOption.value = option
    }

    fun resetFilters() {
        _searchFilter.value = SearchFilter()
        _sortOption.value = SortOption.RECOMMENDED
    }

    // --- Favorites ---
    fun toggleFavorite(providerId: String) {
        viewModelScope.launch {
            val isFav = favorites.value.contains(providerId)
            repository.toggleFavorite(providerId, isFav)
            _snackbarMessage.emit(if (isFav) "Removed from saved pros" else "Saved to favorite pros")
        }
    }

    // --- Review Submission ---
    fun openReviewDialog(booking: Booking) {
        _reviewDialogBooking.value = booking
    }

    fun closeReviewDialog() {
        _reviewDialogBooking.value = null
    }

    fun submitReview(rating: Int, comment: String) {
        val booking = _reviewDialogBooking.value ?: return
        viewModelScope.launch {
            repository.submitReview(
                bookingId = booking.id,
                providerId = booking.providerId,
                rating = rating,
                comment = comment,
                serviceTitle = booking.packageTitle
            )
            _reviewDialogBooking.value = null
            _snackbarMessage.emit("Thank you! Verified review submitted.")
        }
    }

    // --- Booking Status Updates ---
    fun cancelBooking(bookingId: String) {
        viewModelScope.launch {
            repository.updateBookingStatus(bookingId, BookingStatus.CANCELLED)
            _snackbarMessage.emit("Booking cancelled. Full refund initiated.")
        }
    }

    fun markBookingCompleted(bookingId: String) {
        viewModelScope.launch {
            repository.updateBookingStatus(bookingId, BookingStatus.COMPLETED)
            _snackbarMessage.emit("Service marked as completed.")
        }
    }

    // --- Provider Subscription Management ---
    fun upgradeSubscription(tier: SubscriptionTier) {
        viewModelScope.launch {
            repository.updateSubscriptionTier(tier)
            _snackbarMessage.emit("Subscribed to ${tier.tierName}! Reduced marketplace fee active.")
        }
    }
}
