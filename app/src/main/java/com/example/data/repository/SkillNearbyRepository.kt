package com.example.data.repository

import com.example.data.local.BookingEntity
import com.example.data.local.ChatMessageEntity
import com.example.data.local.FavoriteProviderEntity
import com.example.data.local.SkillNearbyDao
import com.example.data.local.UserReviewEntity
import com.example.data.local.ProviderSubscriptionEntity
import com.example.data.model.Booking
import com.example.data.model.BookingStatus
import com.example.data.model.ChatMessage
import com.example.data.model.ConversationPreview
import com.example.data.model.PortfolioItem
import com.example.data.model.ProviderMetrics
import com.example.data.model.SearchFilter
import com.example.data.model.ServiceCategory
import com.example.data.model.ServicePackage
import com.example.data.model.ServiceProvider
import com.example.data.model.SubscriptionTier
import com.example.data.model.VerifiedReview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class SkillNearbyRepository(private val dao: SkillNearbyDao) {

    // Seed providers catalog
    val catalogProviders: List<ServiceProvider> = listOf(
        ServiceProvider(
            id = "p_sarah",
            name = "Sarah Lin, M.Ed.",
            title = "AP & SAT Math / Physics Expert Tutor",
            category = ServiceCategory.TUTORING,
            rating = 4.98,
            reviewCount = 164,
            hourlyRate = 55.0,
            distanceMiles = 1.2,
            responseTimeMinutes = 12,
            isVerified = true,
            isProSubscriber = true,
            bio = "Master's in Math Education with 8+ years teaching experience. I specialize in breaking down complex calculus, physics, and algebra concepts into intuitive, confidence-building steps. 94% of my SAT students achieve 700+ on Math.",
            locationArea = "Central Heights (within 8 miles)",
            jobsCompleted = 340,
            skills = listOf("AP Calculus BC", "SAT/ACT Prep", "Physics", "Linear Algebra", "Interactive Tablet Sessions"),
            avatarBgColor = 0xFF0D9488,
            packages = listOf(
                ServicePackage(
                    id = "pkg_s1",
                    title = "Diagnostic & Goal Setting Session",
                    description = "1-hour evaluation session to identify knowledge gaps, review prior exams, and build a custom study roadmap.",
                    price = 45.0,
                    estimatedDurationHours = 1.0
                ),
                ServicePackage(
                    id = "pkg_s2",
                    title = "Standard 1-on-1 Intensive Tutoring",
                    description = "Focused deep-dive lesson with step-by-step problem solving, guided homework drills, and PDF session notes.",
                    price = 55.0,
                    estimatedDurationHours = 1.0,
                    isPopular = true
                ),
                ServicePackage(
                    id = "pkg_s3",
                    title = "Complete Exam Prep Package (4 Hours)",
                    description = "Comprehensive review of four major syllabus units, timed practice exam grading, and strategy drills.",
                    price = 195.0,
                    estimatedDurationHours = 4.0
                )
            ),
            portfolio = listOf(
                PortfolioItem("SAT Math Score Boost: +160 pts", "Standardized Testing", "Helped student advance from 610 to 770 in 6 weeks with targeted formula mastery.", "Oct 2026"),
                PortfolioItem("AP Calculus BC 5/5 Exam Results", "High School AP", "Coached 18 high school seniors to straight 5s on the spring examination.", "May 2026"),
                PortfolioItem("College Physics I & II Prep", "Higher Education", "Custom syllabus for undergraduate mechanical engineering physics coursework.", "Aug 2026")
            ),
            reviews = listOf(
                VerifiedReview("r1", "Jessica Miller", 5, "2 days ago", "Standard 1-on-1 Intensive Tutoring", "Sarah made calculus so straightforward! My son's confidence immediately turned around after 2 sessions. Truly exceptional tutor.", true),
                VerifiedReview("r2", "David K.", 5, "1 week ago", "Complete Exam Prep Package", "Patient, structured, and extremely well prepared. Sent annotated notes after every lesson. Worth every penny.", true),
                VerifiedReview("r3", "Anita Roy", 5, "2 weeks ago", "Diagnostic & Goal Setting Session", "Clear roadmap from day one. Incredibly punctual and friendly.", true)
            )
        ),
        ServiceProvider(
            id = "p_mike",
            name = "Mike Henderson",
            title = "Master Technician & General Handyman",
            category = ServiceCategory.HOME_REPAIR,
            rating = 4.96,
            reviewCount = 218,
            hourlyRate = 65.0,
            distanceMiles = 1.8,
            responseTimeMinutes = 8,
            isVerified = true,
            isProSubscriber = true,
            bio = "Certified general contractor and home repair specialist with 14 years hands-on field experience. Fully insured and bonded. Equipped with top-tier tools for carpentry, plumbing fixtures, drywall, furniture assembly, and door alignments.",
            locationArea = "Metro West & Downtown",
            jobsCompleted = 520,
            skills = listOf("Plumbing Fixtures", "Drywall Repair", "Cabinetry", "Smart Lock Install", "Tile Restoration"),
            avatarBgColor = 0xFF0284C7,
            packages = listOf(
                ServicePackage(
                    id = "pkg_m1",
                    title = "Diagnostic & Rapid Fix (Up to 1 hr)",
                    description = "Troubleshooting and fast repairs: leaky faucets, sticking doors, squeaky hinges, or small drywall patches.",
                    price = 65.0,
                    estimatedDurationHours = 1.0,
                    isPopular = true
                ),
                ServicePackage(
                    id = "pkg_m2",
                    title = "Half-Day Handyman Punch List",
                    description = "Up to 3.5 hours tackle multiple items: vanity replacement, shelf mounting, TV hanging, and weatherproofing.",
                    price = 210.0,
                    estimatedDurationHours = 3.5
                ),
                ServicePackage(
                    id = "pkg_m3",
                    title = "Smart Home Hardware Installation",
                    description = "Installation of video doorbells, electronic deadbolts, smart thermostats, and motion sensor floodlights.",
                    price = 110.0,
                    estimatedDurationHours = 1.5
                )
            ),
            portfolio = listOf(
                PortfolioItem("Master Bath Fixture Overhaul", "Plumbing & Bath", "Replaced twin vanity faucets, shower trim kit, and installed custom glass shower door.", "Sep 2026"),
                PortfolioItem("Living Room Built-in Shelving", "Custom Carpentry", "Installed heavy-duty floating oak shelves with concealed flush-mount brackets.", "Jul 2026"),
                PortfolioItem("Whole-Home Smart Lock Suite", "Security & Tech", "Integrated 3 smart keyless deadbolts with app automation bridge.", "Aug 2026")
            ),
            reviews = listOf(
                VerifiedReview("r4", "Robert Turner", 5, "3 days ago", "Diagnostic & Rapid Fix", "Mike arrived 5 minutes early, had all proper fittings in his van, and fixed the kitchen leak in under 45 minutes. Super clean!", true),
                VerifiedReview("r5", "Claire Bennett", 5, "2 weeks ago", "Half-Day Handyman Punch List", "Tackled our entire list of to-dos efficiently. Very respectful of our home and left zero dust behind.", true)
            )
        ),
        ServiceProvider(
            id = "p_anya",
            name = "Anya Patel",
            title = "Senior Brand Identity & UI/UX Designer",
            category = ServiceCategory.DESIGN,
            rating = 4.99,
            reviewCount = 134,
            hourlyRate = 70.0,
            distanceMiles = 2.8,
            responseTimeMinutes = 15,
            isVerified = true,
            isProSubscriber = true,
            bio = "Award-winning designer helping local small businesses, medical practices, and startups elevate their brand visuals. From logos and menu cards to responsive mobile websites and cohesive design systems.",
            locationArea = "Creative Arts District (In-person & Remote)",
            jobsCompleted = 190,
            skills = listOf("Brand Identity", "Figma Prototyping", "Vector Logo Systems", "Menu & Print Design", "Packaging"),
            avatarBgColor = 0xFF7C3AED,
            packages = listOf(
                ServicePackage(
                    id = "pkg_a1",
                    title = "Brand Refresh & Vector Logo Suite",
                    description = "Includes primary mark, secondary badge, typography pairing, full brand color palette, and high-res vector files.",
                    price = 280.0,
                    estimatedDurationHours = 4.0,
                    isPopular = true
                ),
                ServicePackage(
                    id = "pkg_a2",
                    title = "UI/UX Consultation & Wireframe Audit",
                    description = "2-hour interactive audit of your current app or website with actionable Figma improvement mockups.",
                    price = 140.0,
                    estimatedDurationHours = 2.0
                ),
                ServicePackage(
                    id = "pkg_a3",
                    title = "Marketing Collateral Pack",
                    description = "Business cards, flyers, social media banner templates, and uniform branding guidelines ready for print.",
                    price = 190.0,
                    estimatedDurationHours = 3.0
                )
            ),
            portfolio = listOf(
                PortfolioItem("Artisan Bakery Complete Rebrand", "Branding", "Modern typography, warm terracotta palette, packaging bags, and storefront signage.", "Oct 2026"),
                PortfolioItem("Local Dental Clinic App UI", "Mobile Design", "Clean, welcoming patient booking and intake flow with accessibility focus.", "Sep 2026"),
                PortfolioItem("Craft Brewery Can Labels", "Packaging", "Series of 4 illustrated beer cans with metallic foil accents.", "Aug 2026")
            ),
            reviews = listOf(
                VerifiedReview("r6", "Samir K.", 5, "Yesterday", "Brand Refresh & Vector Logo Suite", "Anya is a true visionary! She captured our boutique coffee brand essence flawlessly. Turnaround was prompt and communication 10/10.", true),
                VerifiedReview("r7", "Megan Fox", 5, "1 week ago", "UI/UX Consultation", "Transformed our clunky customer portal into an intuitive masterpiece.", true)
            )
        ),
        ServiceProvider(
            id = "p_david",
            name = "David Kowalski",
            title = "Licensed Master Electrician",
            category = ServiceCategory.ELECTRICAL,
            rating = 4.97,
            reviewCount = 192,
            hourlyRate = 85.0,
            distanceMiles = 1.5,
            responseTimeMinutes = 10,
            isVerified = true,
            isProSubscriber = false,
            bio = "Master Electrician license #EL-49821. 16 years specializing in residential panel upgrades, EV Level 2 chargers, recessed LED lighting, and emergency troubleshooting.",
            locationArea = "North & East Suburbs",
            jobsCompleted = 410,
            skills = listOf("EV Chargers", "200A Panel Upgrades", "Surge Protection", "Recessed Lighting", "GFCI Outlets"),
            avatarBgColor = 0xFFD97706,
            packages = listOf(
                ServicePackage(
                    id = "pkg_d1",
                    title = "Electrical Safety Inspection & Diagnostic",
                    description = "Full panel load test, circuit breaker evaluation, GFCI testing, and troubleshooting flickering lights.",
                    price = 85.0,
                    estimatedDurationHours = 1.0,
                    isPopular = true
                ),
                ServicePackage(
                    id = "pkg_d2",
                    title = "Level 2 EV Home Charger Install",
                    description = "Dedicated 50A breaker, conduit run, NEMA 14-50 receptacle or hardwire connection, and full safety permit check.",
                    price = 350.0,
                    estimatedDurationHours = 3.0
                )
            ),
            portfolio = listOf(
                PortfolioItem("Tesla & Rivian Dual Charger Hub", "EV Charging", "Installed dual 48A hardwired chargers with smart load sharing.", "Sep 2026"),
                PortfolioItem("Whole-House LED Recessed Conversion", "Lighting Design", "Replaced 24 incandescent cans with ultra-thin wafer dimmable LEDs.", "Jul 2026")
            ),
            reviews = listOf(
                VerifiedReview("r8", "Tom Bradley", 5, "4 days ago", "Level 2 EV Home Charger Install", "Top-shelf workmanship. David ran clean conduit and verified charging speeds before leaving. Recommended to all my neighbors.", true)
            )
        ),
        ServiceProvider(
            id = "p_elena",
            name = "Elena Rostova",
            title = "Eco-Friendly Deep Cleaning Specialist",
            category = ServiceCategory.CLEANING,
            rating = 4.95,
            reviewCount = 310,
            hourlyRate = 45.0,
            distanceMiles = 0.9,
            responseTimeMinutes = 6,
            isVerified = true,
            isProSubscriber = true,
            bio = "Passionate about creating sparkling, toxin-free living environments. Using certified 100% plant-based, pet-safe, and hypo-allergenic European cleaning solutions. HEPA-filtered vacuuming included.",
            locationArea = "Downtown & University District",
            jobsCompleted = 680,
            skills = listOf("Deep Clean", "Pet-Safe Products", "Move-In/Move-Out", "Oven & Fridge Detailing", "HEPA Filtration"),
            avatarBgColor = 0xFF059669,
            packages = listOf(
                ServicePackage(
                    id = "pkg_e1",
                    title = "Standard 2-Bedroom Home Reset",
                    description = "Kitchen disinfection, bathroom scrub, dusting, HEPA vacuuming, and organic hardwood mopping.",
                    price = 90.0,
                    estimatedDurationHours = 2.0,
                    isPopular = true
                ),
                ServicePackage(
                    id = "pkg_e2",
                    title = "Complete Move-In / Move-Out Deep Clean",
                    description = "Baseboards, interior cabinet wiping, appliance interior detailing (oven + fridge), and window sills.",
                    price = 180.0,
                    estimatedDurationHours = 4.0
                )
            ),
            portfolio = listOf(
                PortfolioItem("Historic Brownstone Post-Renovation Clean", "Deep Clean", "Removed fine drywall dust across 3 floors with HEPA scrubbers.", "Oct 2026")
            ),
            reviews = listOf(
                VerifiedReview("r9", "Laura Vance", 5, "5 days ago", "Standard 2-Bedroom Home Reset", "Elena is a miracle worker. The house smelled fresh and citrusy without any chemical odors. Absolutely pristine.", true)
            )
        ),
        ServiceProvider(
            id = "p_marcus",
            name = "Marcus Brody",
            title = "Home Tech, WiFi & Smart Automation Pro",
            category = ServiceCategory.TECH_SUPPORT,
            rating = 4.93,
            reviewCount = 126,
            hourlyRate = 50.0,
            distanceMiles = 2.2,
            responseTimeMinutes = 14,
            isVerified = true,
            isProSubscriber = false,
            bio = "CompTIA A+ & Network+ certified technician with 10 years experience solving frustrating WiFi dead zones, data backup issues, virus removals, and smart TV soundbar setups.",
            locationArea = "Metro South & Valley",
            jobsCompleted = 260,
            skills = listOf("Mesh WiFi Setup", "Data Migration", "Smart Home Hubs", "PC/Mac Tuneup", "Home Theater Audio"),
            avatarBgColor = 0xFF2563EB,
            packages = listOf(
                ServicePackage(
                    id = "pkg_mb1",
                    title = "WiFi Mesh Network & Dead Zone Fix",
                    description = "Optimal node placement, channel tuning, backhaul cabling check, and guest network isolation.",
                    price = 95.0,
                    estimatedDurationHours = 1.5,
                    isPopular = true
                ),
                ServicePackage(
                    id = "pkg_mb2",
                    title = "PC/Mac Tune-Up & Malware Scrub",
                    description = "Diagnostics, SSD health check, startup optimization, thermal cleaning, and local backup automation.",
                    price = 75.0,
                    estimatedDurationHours = 1.5
                )
            ),
            portfolio = listOf(
                PortfolioItem("Seamless 3-Floor Mesh Network", "Networking", "Eliminated dropped Zoom calls with 650 Mbps coverage corner to corner.", "Aug 2026")
            ),
            reviews = listOf(
                VerifiedReview("r10", "Brian Cox", 5, "1 week ago", "WiFi Mesh Network & Dead Zone Fix", "Marcus fixed an issue that Comcast spent 3 visits failing to diagnose. Super knowledgeable guy.", true)
            )
        ),
        ServiceProvider(
            id = "p_zoe",
            name = "Zoe Washington",
            title = "NASM Certified Personal Trainer & Mobility Coach",
            category = ServiceCategory.FITNESS,
            rating = 4.97,
            reviewCount = 98,
            hourlyRate = 60.0,
            distanceMiles = 1.7,
            responseTimeMinutes = 18,
            isVerified = true,
            isProSubscriber = true,
            bio = "Specializing in functional strength, posture correction, and tailored weight-management workouts at your local park, home gym, or community center. Certified Nutrition Specialist.",
            locationArea = "East Park & Bayside",
            jobsCompleted = 210,
            skills = listOf("Functional Movement", "Kettlebell Training", "Nutrition Coaching", "Post-Rehab Strength", "HIIT"),
            avatarBgColor = 0xFFEA580C,
            packages = listOf(
                ServicePackage(
                    id = "pkg_z1",
                    title = "Mobility & Movement Assessment (1 Hr)",
                    description = "Joint range of motion check, postural alignment screen, and customized warm-up prescription.",
                    price = 50.0,
                    estimatedDurationHours = 1.0
                ),
                ServicePackage(
                    id = "pkg_z2",
                    title = "1-on-1 Personalized Coaching Session",
                    description = "High-energy customized workout, form correction, equipment provided (resistance bands, kettlebells).",
                    price = 60.0,
                    estimatedDurationHours = 1.0,
                    isPopular = true
                )
            ),
            portfolio = listOf(
                PortfolioItem("Client Lower Back Recovery & 5K Finish", "Functional Rehab", "Built core stability progression enabling client to run pain-free.", "Sep 2026")
            ),
            reviews = listOf(
                VerifiedReview("r11", "Amanda Stone", 5, "3 weeks ago", "1-on-1 Personalized Coaching Session", "Zoe's energy is infectious! She pays such close attention to form so you never get hurt.", true)
            )
        ),
        ServiceProvider(
            id = "p_mateo",
            name = "Mateo Silva",
            title = "Commercial & Portrait Photographer",
            category = ServiceCategory.PHOTOGRAPHY,
            rating = 4.91,
            reviewCount = 82,
            hourlyRate = 90.0,
            distanceMiles = 3.1,
            responseTimeMinutes = 25,
            isVerified = true,
            isProSubscriber = false,
            bio = "Published photographer specializing in outdoor lifestyle portraits, business headshots, architectural interiors, and intimate private events. Natural light enthusiast with prime lenses.",
            locationArea = "Metro Greater Area",
            jobsCompleted = 145,
            skills = listOf("Portrait Headshots", "Natural Light", "Real Estate Interiors", "Color Grading", "High-Res Retouching"),
            avatarBgColor = 0xFF4F46E5,
            packages = listOf(
                ServicePackage(
                    id = "pkg_mt1",
                    title = "Professional Headshots (Outdoor/Studio)",
                    description = "45-minute shoot, 2 outfit changes, 5 fully retouched high-resolution images delivered in 48 hours.",
                    price = 120.0,
                    estimatedDurationHours = 1.0,
                    isPopular = true
                ),
                ServicePackage(
                    id = "pkg_mt2",
                    title = "Real Estate & Interior Showcase (25 Photos)",
                    description = "HDR multi-exposure interior and exterior shots, twilight optimization, delivered ready for MLS listings.",
                    price = 220.0,
                    estimatedDurationHours = 2.0
                )
            ),
            portfolio = listOf(
                PortfolioItem("Architectural Digest Feature", "Commercial Interior", "Clean editorial shoot of modern hillside timber residence.", "Sep 2026")
            ),
            reviews = listOf(
                VerifiedReview("r12", "Hannah Lee", 5, "2 weeks ago", "Professional Headshots", "Mateo made me feel so comfortable in front of the lens. The photos turned out stunning!", true)
            )
        )
    )

    // Flow of providers combining user reviews and favorite state
    val favoritesFlow: Flow<Set<String>> = dao.getAllFavorites().map { list ->
        list.map { it.providerId }.toSet()
    }

    val bookingsFlow: Flow<List<Booking>> = dao.getAllBookings().map { entities ->
        entities.map { e ->
            Booking(
                id = e.id,
                providerId = e.providerId,
                providerName = e.providerName,
                providerTitle = e.providerTitle,
                category = ServiceCategory.values().find { it.name == e.category } ?: ServiceCategory.ALL,
                packageTitle = e.packageTitle,
                serviceDate = e.serviceDate,
                serviceTime = e.serviceTime,
                serviceLocation = e.serviceLocation,
                notes = e.notes,
                basePrice = e.basePrice,
                bookingFee = e.bookingFee,
                taxAmount = e.taxAmount,
                totalPrice = e.totalPrice,
                status = runCatching { BookingStatus.valueOf(e.status) }.getOrDefault(BookingStatus.CONFIRMED),
                paymentMethod = e.paymentMethod,
                timestamp = e.timestamp,
                hasReview = e.hasReview
            )
        }
    }

    val messagesFlow: Flow<List<ChatMessage>> = dao.getAllMessages().map { entities ->
        entities.map { e ->
            ChatMessage(
                id = e.id,
                conversationId = e.conversationId,
                providerId = e.providerId,
                senderName = e.senderName,
                text = e.text,
                timestamp = e.timestamp,
                isFromUser = e.isFromUser,
                quotePrice = e.quotePrice
            )
        }
    }

    val userSubscriptionFlow: Flow<SubscriptionTier> = dao.getProviderSubscription().map { entity ->
        if (entity != null) {
            runCatching { SubscriptionTier.valueOf(entity.currentTier) }.getOrDefault(SubscriptionTier.FREE_STARTER)
        } else {
            SubscriptionTier.PRO_PARTNER // default demo tier
        }
    }

    suspend fun seedInitialDataIfEmpty() {
        // Seed default bookings if table empty
        val existingBookings = dao.getAllBookings().firstOrNull() ?: emptyList()
        if (existingBookings.isEmpty()) {
            val sampleBooking1 = BookingEntity(
                id = "BK-8291",
                providerId = "p_mike",
                providerName = "Mike Henderson",
                providerTitle = "Master Technician & General Handyman",
                category = ServiceCategory.HOME_REPAIR.name,
                packageTitle = "Diagnostic & Rapid Fix (Up to 1 hr)",
                serviceDate = "Tomorrow, 2:00 PM",
                serviceTime = "2:00 PM",
                serviceLocation = "742 Evergreen Terrace, Apt 4B",
                notes = "Kitchen sink garbage disposal humming and not spinning. Need quick diagnostic.",
                basePrice = 65.0,
                bookingFee = 3.25, // 5% marketplace booking fee
                taxAmount = 5.46,
                totalPrice = 73.71,
                status = BookingStatus.CONFIRMED.name,
                paymentMethod = "Visa ending in 4242",
                timestamp = System.currentTimeMillis() - 7200000,
                hasReview = false
            )
            val sampleBooking2 = BookingEntity(
                id = "BK-7190",
                providerId = "p_sarah",
                providerName = "Sarah Lin, M.Ed.",
                providerTitle = "AP & SAT Math / Physics Expert Tutor",
                category = ServiceCategory.TUTORING.name,
                packageTitle = "Standard 1-on-1 Intensive Tutoring",
                serviceDate = "Oct 24, 4:00 PM",
                serviceTime = "4:00 PM",
                serviceLocation = "Downtown Public Library (Study Room C)",
                notes = "AP Calculus derivatives & chain rule review.",
                basePrice = 55.0,
                bookingFee = 2.75,
                taxAmount = 4.62,
                totalPrice = 62.37,
                status = BookingStatus.COMPLETED.name,
                paymentMethod = "Google Pay",
                timestamp = System.currentTimeMillis() - 86400000 * 3,
                hasReview = true
            )
            dao.insertBooking(sampleBooking1)
            dao.insertBooking(sampleBooking2)
        }

        // Seed initial chat messages if empty
        val existingMessages = dao.getAllMessages().firstOrNull() ?: emptyList()
        if (existingMessages.isEmpty()) {
            val now = System.currentTimeMillis()
            val m1 = ChatMessageEntity(
                id = "msg_1",
                conversationId = "convo_p_mike",
                providerId = "p_mike",
                senderName = "You",
                text = "Hi Mike, I have a humming garbage disposal. Are you able to bring common replacement parts tomorrow?",
                timestamp = now - 3600000 * 2,
                isFromUser = true
            )
            val m2 = ChatMessageEntity(
                id = "msg_2",
                conversationId = "convo_p_mike",
                providerId = "p_mike",
                senderName = "Mike Henderson",
                text = "Hello! Yes, absolutely. I always carry Badger 5 and Moen parts on my truck. I'll test the impeller and unjam key first to save you money.",
                timestamp = now - 3600000 * 2 + 180000,
                isFromUser = false
            )
            val m3 = ChatMessageEntity(
                id = "msg_3",
                conversationId = "convo_p_mike",
                providerId = "p_mike",
                senderName = "Mike Henderson",
                text = "Looking forward to helping you out tomorrow at 2:00 PM!",
                timestamp = now - 3600000 * 1,
                isFromUser = false
            )

            // Sarah conversation
            val s1 = ChatMessageEntity(
                id = "msg_s1",
                conversationId = "convo_p_sarah",
                providerId = "p_sarah",
                senderName = "Sarah Lin, M.Ed.",
                text = "Great session on derivatives last Thursday! Here are the 3 practice problems to prepare for integration.",
                timestamp = now - 86400000 * 2,
                isFromUser = false
            )
            dao.insertMessage(m1)
            dao.insertMessage(m2)
            dao.insertMessage(m3)
            dao.insertMessage(s1)
        }
    }

    suspend fun createBooking(
        provider: ServiceProvider,
        pkg: ServicePackage,
        date: String,
        time: String,
        address: String,
        notes: String,
        paymentMethod: String
    ): Booking {
        val bookingId = "BK-" + (1000..9999).random()
        val basePrice = pkg.price
        val bookingFee = Math.round(basePrice * 0.05 * 100.0) / 100.0 // 5% marketplace guarantee fee
        val tax = Math.round(basePrice * 0.08 * 100.0) / 100.0
        val total = basePrice + bookingFee + tax

        val entity = BookingEntity(
            id = bookingId,
            providerId = provider.id,
            providerName = provider.name,
            providerTitle = provider.title,
            category = provider.category.name,
            packageTitle = pkg.title,
            serviceDate = "$date at $time",
            serviceTime = time,
            serviceLocation = address.ifBlank { "Client Address (Shared securely)" },
            notes = notes.ifBlank { "Standard booking request" },
            basePrice = basePrice,
            bookingFee = bookingFee,
            taxAmount = tax,
            totalPrice = total,
            status = BookingStatus.CONFIRMED.name,
            paymentMethod = paymentMethod,
            timestamp = System.currentTimeMillis(),
            hasReview = false
        )
        dao.insertBooking(entity)

        // Also add an automated confirmation message to the chat
        val convoId = "convo_${provider.id}"
        val autoMsg = ChatMessageEntity(
            id = UUID.randomUUID().toString(),
            conversationId = convoId,
            providerId = provider.id,
            senderName = "SkillNearby System",
            text = "Booking confirmed (#$bookingId)! ${pkg.title} scheduled for $date at $time.",
            timestamp = System.currentTimeMillis(),
            isFromUser = false,
            quotePrice = total
        )
        dao.insertMessage(autoMsg)

        return Booking(
            id = entity.id,
            providerId = entity.providerId,
            providerName = entity.providerName,
            providerTitle = entity.providerTitle,
            category = provider.category,
            packageTitle = entity.packageTitle,
            serviceDate = entity.serviceDate,
            serviceTime = entity.serviceTime,
            serviceLocation = entity.serviceLocation,
            notes = entity.notes,
            basePrice = entity.basePrice,
            bookingFee = entity.bookingFee,
            taxAmount = entity.taxAmount,
            totalPrice = entity.totalPrice,
            status = BookingStatus.CONFIRMED,
            paymentMethod = entity.paymentMethod,
            timestamp = entity.timestamp,
            hasReview = false
        )
    }

    suspend fun sendMessage(conversationId: String, providerId: String, text: String, quotePrice: Double? = null) {
        val userMsg = ChatMessageEntity(
            id = UUID.randomUUID().toString(),
            conversationId = conversationId,
            providerId = providerId,
            senderName = "You",
            text = text,
            timestamp = System.currentTimeMillis(),
            isFromUser = true,
            quotePrice = quotePrice
        )
        dao.insertMessage(userMsg)
    }

    suspend fun simulateProviderReply(conversationId: String, provider: ServiceProvider, userQuery: String) {
        // Smart realistic provider responses based on their specialty
        val replyText = when (provider.category) {
            ServiceCategory.TUTORING -> "Thanks for reaching out! I reviewed your note. I can definitely help with that syllabus. Does weekday afternoon or weekend morning work best for you?"
            ServiceCategory.HOME_REPAIR -> "Got it! Thanks for describing the issue. I have standard fittings and repair kits ready. I'll make sure to double-check everything before we wrap up."
            ServiceCategory.DESIGN -> "Exciting project! I'd love to collaborate on your brand design. I will prepare an initial moodboard and wireframe concepts to review."
            ServiceCategory.ELECTRICAL -> "Safety is top priority. If there's any burning smell or trip, please isolate the breaker. Otherwise, I will inspect the load and circuit on site."
            ServiceCategory.CLEANING -> "Hello! Yes, our eco-friendly non-toxic supplies are ready. We take care of pets and delicate surfaces with extra attention."
            else -> "Thanks for messaging me through SkillNearby! I am available and happy to answer any questions or confirm your requested slot."
        }
        val reply = ChatMessageEntity(
            id = UUID.randomUUID().toString(),
            conversationId = conversationId,
            providerId = provider.id,
            senderName = provider.name,
            text = replyText,
            timestamp = System.currentTimeMillis() + 800,
            isFromUser = false
        )
        dao.insertMessage(reply)
    }

    suspend fun toggleFavorite(providerId: String, isCurrentlyFav: Boolean) {
        if (isCurrentlyFav) {
            dao.deleteFavorite(providerId)
        } else {
            dao.insertFavorite(FavoriteProviderEntity(providerId = providerId))
        }
    }

    suspend fun updateBookingStatus(bookingId: String, status: BookingStatus) {
        dao.updateBookingStatus(bookingId, status.name)
    }

    suspend fun submitReview(bookingId: String, providerId: String, rating: Int, comment: String, serviceTitle: String) {
        val review = UserReviewEntity(
            id = UUID.randomUUID().toString(),
            providerId = providerId,
            rating = rating,
            comment = comment,
            serviceTitle = serviceTitle,
            date = "Today"
        )
        dao.insertReview(review)
        dao.markBookingReviewed(bookingId)
    }

    suspend fun updateSubscriptionTier(tier: SubscriptionTier) {
        dao.setProviderSubscription(ProviderSubscriptionEntity(currentTier = tier.name))
    }

    fun getProviderById(id: String): ServiceProvider? {
        return catalogProviders.find { it.id == id }
    }
}
