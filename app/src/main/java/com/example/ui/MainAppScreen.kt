package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.WorkspacePremium
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.LeaveReviewDialog
import com.example.ui.screens.BookingFlowScreen
import com.example.ui.screens.BookingsHubScreen
import com.example.ui.screens.ExploreScreen
import com.example.ui.screens.MessagingScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.ProviderDetailScreen
import com.example.ui.screens.ProviderModeScreen
import com.example.ui.theme.ElectricTeal
import com.example.ui.viewmodel.MainTab
import com.example.ui.viewmodel.SkillNearbyViewModel
import kotlinx.coroutines.flow.collectLatest

@Composable
fun MainAppScreen(
    viewModel: SkillNearbyViewModel,
    modifier: Modifier = Modifier
) {
    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
    val selectedProvider by viewModel.selectedProvider.collectAsStateWithLifecycle()
    val checkoutState by viewModel.checkoutState.collectAsStateWithLifecycle()
    val activeChatId by viewModel.activeChatConversationId.collectAsStateWithLifecycle()
    val activeChatProvider by viewModel.activeChatProvider.collectAsStateWithLifecycle()
    val reviewBooking by viewModel.reviewDialogBooking.collectAsStateWithLifecycle()

    val filteredProviders by viewModel.filteredProviders.collectAsStateWithLifecycle()
    val searchFilter by viewModel.searchFilter.collectAsStateWithLifecycle()
    val sortOption by viewModel.sortOption.collectAsStateWithLifecycle()
    val favorites by viewModel.favorites.collectAsStateWithLifecycle()
    val bookings by viewModel.bookings.collectAsStateWithLifecycle()
    val allMessages by viewModel.allMessages.collectAsStateWithLifecycle()
    val conversations by viewModel.conversations.collectAsStateWithLifecycle()
    val subscriptionTier by viewModel.subscriptionTier.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.snackbarMessage.collectLatest { msg ->
            snackbarHostState.showSnackbar(msg)
        }
    }

    // Predictive Back Handling
    BackHandler(
        enabled = checkoutState.provider != null ||
                  selectedProvider != null ||
                  activeChatId != null ||
                  currentTab != MainTab.EXPLORE
    ) {
        when {
            checkoutState.provider != null -> viewModel.closeBookingFlow()
            selectedProvider != null -> viewModel.closeProviderDetail()
            activeChatId != null -> viewModel.closeChat()
            currentTab != MainTab.EXPLORE -> viewModel.selectTab(MainTab.EXPLORE)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            // Show bottom navigation bar only when not deep inside checkout flow
            if (checkoutState.provider == null && selectedProvider == null && activeChatId == null) {
                NavigationBar(
                    modifier = Modifier.testTag("main_bottom_nav")
                ) {
                    // Tab 1: Explore
                    NavigationBarItem(
                        selected = currentTab == MainTab.EXPLORE,
                        onClick = { viewModel.selectTab(MainTab.EXPLORE) },
                        icon = {
                            Icon(
                                imageVector = if (currentTab == MainTab.EXPLORE) Icons.Filled.Explore else Icons.Outlined.Explore,
                                contentDescription = "Explore"
                            )
                        },
                        label = { Text("Explore", fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = ElectricTeal,
                            selectedTextColor = ElectricTeal,
                            indicatorColor = ElectricTeal.copy(alpha = 0.15f)
                        ),
                        modifier = Modifier.testTag("nav_tab_explore")
                    )

                    // Tab 2: Bookings
                    val activeBookingsCount = bookings.count { it.status.name == "CONFIRMED" || it.status.name == "REQUESTED" }
                    NavigationBarItem(
                        selected = currentTab == MainTab.BOOKINGS,
                        onClick = { viewModel.selectTab(MainTab.BOOKINGS) },
                        icon = {
                            if (activeBookingsCount > 0) {
                                BadgedBox(badge = { Badge { Text("$activeBookingsCount") } }) {
                                    Icon(
                                        imageVector = if (currentTab == MainTab.BOOKINGS) Icons.Filled.CalendarMonth else Icons.Outlined.CalendarMonth,
                                        contentDescription = "Bookings"
                                    )
                                }
                            } else {
                                Icon(
                                    imageVector = if (currentTab == MainTab.BOOKINGS) Icons.Filled.CalendarMonth else Icons.Outlined.CalendarMonth,
                                    contentDescription = "Bookings"
                                )
                            }
                        },
                        label = { Text("Bookings", fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = ElectricTeal,
                            selectedTextColor = ElectricTeal,
                            indicatorColor = ElectricTeal.copy(alpha = 0.15f)
                        ),
                        modifier = Modifier.testTag("nav_tab_bookings")
                    )

                    // Tab 3: Messages
                    val unreadMessagesCount = conversations.sumOf { it.unreadCount }
                    NavigationBarItem(
                        selected = currentTab == MainTab.MESSAGES,
                        onClick = { viewModel.selectTab(MainTab.MESSAGES) },
                        icon = {
                            if (unreadMessagesCount > 0) {
                                BadgedBox(badge = { Badge { Text("$unreadMessagesCount") } }) {
                                    Icon(
                                        imageVector = if (currentTab == MainTab.MESSAGES) Icons.Filled.ChatBubbleOutline else Icons.Outlined.ChatBubbleOutline,
                                        contentDescription = "Messages"
                                    )
                                }
                            } else {
                                Icon(
                                    imageVector = if (currentTab == MainTab.MESSAGES) Icons.Filled.ChatBubbleOutline else Icons.Outlined.ChatBubbleOutline,
                                    contentDescription = "Messages"
                                )
                            }
                        },
                        label = { Text("Messages", fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = ElectricTeal,
                            selectedTextColor = ElectricTeal,
                            indicatorColor = ElectricTeal.copy(alpha = 0.15f)
                        ),
                        modifier = Modifier.testTag("nav_tab_messages")
                    )

                    // Tab 4: Provider Hub (Monetization & Subscriptions)
                    NavigationBarItem(
                        selected = currentTab == MainTab.PROVIDER_HUB,
                        onClick = { viewModel.selectTab(MainTab.PROVIDER_HUB) },
                        icon = {
                            Icon(
                                imageVector = if (currentTab == MainTab.PROVIDER_HUB) Icons.Filled.WorkspacePremium else Icons.Outlined.WorkspacePremium,
                                contentDescription = "Provider Hub"
                            )
                        },
                        label = { Text("Provider Hub", fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = ElectricTeal,
                            selectedTextColor = ElectricTeal,
                            indicatorColor = ElectricTeal.copy(alpha = 0.15f)
                        ),
                        modifier = Modifier.testTag("nav_tab_provider_hub")
                    )

                    // Tab 5: Profile
                    NavigationBarItem(
                        selected = currentTab == MainTab.PROFILE,
                        onClick = { viewModel.selectTab(MainTab.PROFILE) },
                        icon = {
                            Icon(
                                imageVector = if (currentTab == MainTab.PROFILE) Icons.Filled.Person else Icons.Outlined.Person,
                                contentDescription = "Profile"
                            )
                        },
                        label = { Text("Profile", fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = ElectricTeal,
                            selectedTextColor = ElectricTeal,
                            indicatorColor = ElectricTeal.copy(alpha = 0.15f)
                        ),
                        modifier = Modifier.testTag("nav_tab_profile")
                    )
                }
            }
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            when {
                // 1. Checkout / Booking Flow Screen
                checkoutState.provider != null -> {
                    BookingFlowScreen(
                        state = checkoutState,
                        onBack = { viewModel.closeBookingFlow() },
                        onDateSelect = { viewModel.updateCheckoutDate(it) },
                        onTimeSelect = { viewModel.updateCheckoutTime(it) },
                        onAddressChange = { viewModel.updateCheckoutAddress(it) },
                        onNotesChange = { viewModel.updateCheckoutNotes(it) },
                        onPaymentMethodSelect = { viewModel.updatePaymentMethod(it) },
                        onConfirmAndPay = { viewModel.submitBookingAndPay() },
                        onViewBookings = {
                            viewModel.closeBookingFlow()
                            viewModel.selectTab(MainTab.BOOKINGS)
                        },
                        onChatWithProvider = {
                            val provider = checkoutState.provider
                            viewModel.closeBookingFlow()
                            if (provider != null) {
                                viewModel.openChatWithProvider(provider)
                            }
                        }
                    )
                }

                // 2. Provider Detail Screen
                selectedProvider != null -> {
                    val p = selectedProvider!!
                    ProviderDetailScreen(
                        provider = p,
                        isFavorite = favorites.contains(p.id),
                        onBack = { viewModel.closeProviderDetail() },
                        onToggleFavorite = { viewModel.toggleFavorite(p.id) },
                        onBookPackage = { pkg ->
                            viewModel.closeProviderDetail()
                            viewModel.startBookingFlow(p, pkg)
                        },
                        onStartChat = {
                            viewModel.closeProviderDetail()
                            viewModel.openChatWithProvider(p)
                        }
                    )
                }

                // 3. Main Tabs Navigation
                else -> {
                    when (currentTab) {
                        MainTab.EXPLORE -> {
                            ExploreScreen(
                                providers = filteredProviders,
                                searchFilter = searchFilter,
                                sortOption = sortOption,
                                favorites = favorites,
                                onSearchQueryChange = { viewModel.updateSearchQuery(it) },
                                onCategoryChange = { viewModel.updateCategory(it) },
                                onSortChange = { viewModel.setSortOption(it) },
                                onToggleVerifiedOnly = { viewModel.toggleVerifiedOnly() },
                                onToggleProOnly = { viewModel.toggleProOnly() },
                                onToggleFavorite = { viewModel.toggleFavorite(it) },
                                onSelectProvider = { viewModel.openProviderDetail(it) },
                                onBookProvider = { viewModel.startBookingFlow(it) },
                                onChatProvider = { viewModel.openChatWithProvider(it) },
                                modifier = Modifier.padding(innerPadding)
                            )
                        }

                        MainTab.BOOKINGS -> {
                            BookingsHubScreen(
                                bookings = bookings,
                                onMessageProvider = { providerId ->
                                    val provider = filteredProviders.find { it.id == providerId }
                                    if (provider != null) {
                                        viewModel.openChatWithProvider(provider)
                                    }
                                },
                                onCancelBooking = { viewModel.cancelBooking(it) },
                                onCompleteBooking = { viewModel.markBookingCompleted(it) },
                                onOpenReviewDialog = { viewModel.openReviewDialog(it) },
                                modifier = Modifier.padding(innerPadding)
                            )
                        }

                        MainTab.MESSAGES -> {
                            MessagingScreen(
                                conversations = conversations,
                                activeConversationId = activeChatId,
                                activeProvider = activeChatProvider,
                                allMessages = allMessages,
                                onSelectConversation = { viewModel.openConversation(it) },
                                onBackToList = { viewModel.closeChat() },
                                onSendMessage = { viewModel.sendChatMessage(it) },
                                modifier = Modifier.padding(innerPadding)
                            )
                        }

                        MainTab.PROVIDER_HUB -> {
                            ProviderModeScreen(
                                currentSubscription = subscriptionTier,
                                onUpgradeSubscription = { viewModel.upgradeSubscription(it) },
                                modifier = Modifier.padding(innerPadding)
                            )
                        }

                        MainTab.PROFILE -> {
                            val favProviders = filteredProviders.filter { favorites.contains(it.id) }
                            ProfileScreen(
                                favoriteProviders = favProviders,
                                bookingsCount = bookings.size,
                                onSelectProvider = { viewModel.openProviderDetail(it) },
                                modifier = Modifier.padding(innerPadding)
                            )
                        }
                    }
                }
            }

            // Review Submission Dialog
            reviewBooking?.let { b ->
                LeaveReviewDialog(
                    booking = b,
                    onDismiss = { viewModel.closeReviewDialog() },
                    onSubmit = { rating, comment ->
                        viewModel.submitReview(rating, comment)
                    }
                )
            }
        }
    }
}
