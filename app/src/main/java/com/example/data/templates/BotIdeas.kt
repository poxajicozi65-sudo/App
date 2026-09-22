package com.example.data.templates

import com.example.data.model.BotIdea

object BotIdeas {

    val CURATED_IDEAS: List<BotIdea> = listOf(
        BotIdea(
            title = "Fitness Streak & Hydration Coach",
            category = "Health & Habit",
            description = "A friendly Telegram bot that pings users throughout the day to drink water and log workouts, rewarding consistent streaks with badges in a vibrant Mini App.",
            keyFeatures = listOf("Automated hydration reminders", "Streak tracking with flame badges", "Workout checklist Mini App", "Telegram Stars subscription for custom AI meal plans"),
            monetization = "Freemium: free habit tracking; $3/mo Telegram Stars for AI coaching & custom nutrition plans",
            starterCommands = listOf("/start", "/log_water", "/streak", "/workout", "/stats")
        ),
        BotIdea(
            title = "Flash Deal & Group Buying Bot",
            category = "E-Commerce",
            description = "Brings group-buying dynamics (like Pinduoduo) to Telegram channels. Users pool together with friends to unlock 50% discount tiers on digital and physical products.",
            keyFeatures = listOf("Group deal countdown timers", "Instant shareable link to Telegram groups", "Cart and order tracker Mini App", "Telegram Stars & TON Pay checkout"),
            monetization = "5% platform commission on completed group purchases; sponsored merchant slots",
            starterCommands = listOf("/start", "/deals", "/my_group", "/orders", "/invite")
        ),
        BotIdea(
            title = "Anonymous Confessions & University Board",
            category = "Social & Community",
            description = "Community bot where college students or community members submit anonymous confessions or campus questions. Top voted submissions are published to the channel.",
            keyFeatures = listOf("Zero-log anonymous submission queue", "Moderator approval dashboard", "Interactive swipe voting Mini App", "Channel auto-forwarding"),
            monetization = "Tip jar (Telegram Stars) for top storytellers; pinned business ads",
            starterCommands = listOf("/start", "/confess", "/trending", "/rules", "/top")
        ),
        BotIdea(
            title = "Web3 Airdrop Quest & Verification Bot",
            category = "Crypto & Web3",
            description = "Automates community growth for crypto projects. Verifies Twitter follows, Telegram joins, and wallet connects, issuing lottery tickets for airdrops.",
            keyFeatures = listOf("Automated Telegram channel membership check", "TON Connect wallet integration", "Quest progress tracker Mini App", "Anti-sybil fraud detection"),
            monetization = "SaaS fee charged to crypto project owners ($99 per campaign)",
            starterCommands = listOf("/start", "/quests", "/wallet", "/referrals", "/airdrop")
        ),
        BotIdea(
            title = "AI Language Practice Buddy",
            category = "Education",
            description = "Voice-first language practice partner. Users send voice notes in Spanish/Japanese/French, and the bot replies with spoken audio and grammar feedback.",
            keyFeatures = listOf("Voice note speech-to-text & audio response", "Grammar correction cards", "Vocabulary flashcard Mini App", "CEFR level test"),
            monetization = "10 free voice minutes per day; unlimited speaking access for 100 Telegram Stars / month",
            starterCommands = listOf("/start", "/practice", "/vocab", "/level", "/upgrade")
        ),
        BotIdea(
            title = "Local Restaurant Table & Order Bot",
            category = "Local Business",
            description = "Allows diners to view dynamic digital menus, reserve tables, pre-order meals, and split bills directly inside Telegram without downloading separate apps.",
            keyFeatures = listOf("Interactive food menu Mini App with photos", "Table reservation calendar", "Kitchen notification webhook", "Instant bill split"),
            monetization = "Monthly subscription charged to restaurant owners + small transaction fee",
            starterCommands = listOf("/start", "/menu", "/book_table", "/my_reservation", "/support")
        )
    )

    fun getRandomIdea(category: String? = null): BotIdea {
        val filtered = if (category.isNullOrBlank() || category == "All") {
            CURATED_IDEAS
        } else {
            CURATED_IDEAS.filter { it.category.equals(category, ignoreCase = true) }
        }
        return (if (filtered.isNotEmpty()) filtered else CURATED_IDEAS).random()
    }
}
