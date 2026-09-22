package com.example.data.store

import android.content.Context
import com.example.data.preferences.SecurePreferences
import org.json.JSONArray
import org.json.JSONObject

class AssetStoreRepository(private val context: Context) {

    private val securePrefs = SecurePreferences(context)

    companion object {
        private const val PREF_SAVED_ASSETS = "user_saved_drive_assets"

        val CURATED_ASSETS: List<StoreAsset> = listOf(
            // --- Telegram & Essentials ---
            StoreAsset(
                id = "asset_tg_logo",
                title = "Telegram Official",
                category = AssetCategory.TELEGRAM,
                type = AssetType.ICON,
                previewEmoji = "✈️",
                directUrl = "https://telegram.org/img/t_logo.png",
                description = "Official Telegram cloud logo in crisp resolution",
                tags = listOf("telegram", "tg", "official", "logo", "app", "blue")
            ),
            StoreAsset(
                id = "asset_tg_stars",
                title = "Telegram Stars",
                category = AssetCategory.TELEGRAM,
                type = AssetType.ICON,
                previewEmoji = "⭐",
                directUrl = "https://raw.githubusercontent.com/TelegramMessenger/Telegram-iOS/master/submodules/LegacyComponents/Resources/LegacyComponents.bundle/Star.png",
                description = "Telegram Stars in-app currency symbol",
                tags = listOf("stars", "telegram", "currency", "payment", "yellow", "gold")
            ),
            StoreAsset(
                id = "asset_verified_badge",
                title = "Verified Shield",
                category = AssetCategory.TELEGRAM,
                type = AssetType.ICON,
                previewEmoji = "🛡️",
                directUrl = "https://img.icons8.com/color/96/verified-badge.png",
                description = "Official verified account badge for bots and apps",
                tags = listOf("verified", "check", "shield", "trust", "badge")
            ),
            StoreAsset(
                id = "asset_bot_crown",
                title = "VIP Gold Crown",
                category = AssetCategory.TELEGRAM,
                type = AssetType.ICON,
                previewEmoji = "👑",
                directUrl = "https://img.icons8.com/color/96/crown.png",
                description = "Gold crown for VIP players and top leaders",
                tags = listOf("crown", "vip", "gold", "king", "rank", "leader")
            ),

            // --- Crypto & Web3 ---
            StoreAsset(
                id = "asset_ton_coin",
                title = "TON Token",
                category = AssetCategory.CRYPTO,
                type = AssetType.ICON,
                previewEmoji = "💎",
                directUrl = "https://cryptologos.cc/logos/toncoin-ton-logo.png?v=035",
                description = "The Open Network (TON) native blockchain token",
                tags = listOf("ton", "toncoin", "the open network", "crypto", "token", "diamond")
            ),
            StoreAsset(
                id = "asset_usdt_tether",
                title = "USDT (Tether)",
                category = AssetCategory.CRYPTO,
                type = AssetType.ICON,
                previewEmoji = "💵",
                directUrl = "https://cryptologos.cc/logos/tether-usdt-logo.png?v=035",
                description = "Tether USD stablecoin payout token",
                tags = listOf("usdt", "tether", "usd", "dollar", "stablecoin", "cash")
            ),
            StoreAsset(
                id = "asset_notcoin",
                title = "Notcoin (NOT)",
                category = AssetCategory.CRYPTO,
                type = AssetType.ICON,
                previewEmoji = "🪙",
                directUrl = "https://cryptologos.cc/logos/notcoin-not-logo.png?v=035",
                description = "Viral Telegram tap-to-earn cryptocurrency token",
                tags = listOf("notcoin", "not", "tap", "telegram", "crypto")
            ),
            StoreAsset(
                id = "asset_dogs_token",
                title = "Dogs Token (DOGS)",
                category = AssetCategory.CRYPTO,
                type = AssetType.ICON,
                previewEmoji = "🐶",
                directUrl = "https://img.icons8.com/color/96/dog.png",
                description = "Telegram community-driven Dogs meme coin",
                tags = listOf("dogs", "dog", "meme", "ton", "community")
            ),
            StoreAsset(
                id = "asset_hamster_token",
                title = "Hamster Kombat",
                category = AssetCategory.CRYPTO,
                type = AssetType.ICON,
                previewEmoji = "🐹",
                directUrl = "https://img.icons8.com/color/96/hamster.png",
                description = "Hamster Kombat CEO gaming asset",
                tags = listOf("hamster", "hmstr", "ceo", "tap", "kombat")
            ),
            StoreAsset(
                id = "asset_bitcoin",
                title = "Bitcoin (BTC)",
                category = AssetCategory.CRYPTO,
                type = AssetType.ICON,
                previewEmoji = "₿",
                directUrl = "https://cryptologos.cc/logos/bitcoin-btc-logo.png?v=035",
                description = "Bitcoin digital gold pioneer",
                tags = listOf("bitcoin", "btc", "crypto", "gold", "orange")
            ),
            StoreAsset(
                id = "asset_crypto_wallet",
                title = "Web3 Crypto Wallet",
                category = AssetCategory.CRYPTO,
                type = AssetType.ICON,
                previewEmoji = "👛",
                directUrl = "https://img.icons8.com/color/96/wallet--v1.png",
                description = "Connect TON/Telegram wallet icon",
                tags = listOf("wallet", "connect", "tonkeeper", "payout", "balance")
            ),
            StoreAsset(
                id = "asset_cash_bag",
                title = "Golden Coin Bag",
                category = AssetCategory.CRYPTO,
                type = AssetType.ICON,
                previewEmoji = "💰",
                directUrl = "https://img.icons8.com/color/96/money-bag.png",
                description = "Heavy coin bag for jackpot and cash rewards",
                tags = listOf("bag", "money", "cash", "jackpot", "gold", "earn")
            ),

            // --- Gaming & Arcade ---
            StoreAsset(
                id = "asset_spin_wheel",
                title = "Lucky Spin Wheel",
                category = AssetCategory.GAMING,
                type = AssetType.ICON,
                previewEmoji = "🎡",
                directUrl = "https://img.icons8.com/color/96/roulette.png",
                description = "Fortune wheel for daily spins and multiplier bonuses",
                tags = listOf("wheel", "spin", "fortune", "lucky", "roulette", "casino")
            ),
            StoreAsset(
                id = "asset_ludo_dice",
                title = "1v1 Ludo 3D Dice",
                category = AssetCategory.GAMING,
                type = AssetType.ICON,
                previewEmoji = "🎲",
                directUrl = "https://img.icons8.com/color/96/dice.png",
                description = "Red and white gaming dice for board and battle matches",
                tags = listOf("dice", "ludo", "board", "roll", "random", "play")
            ),
            StoreAsset(
                id = "asset_gamepad_retro",
                title = "Arcade Gamepad",
                category = AssetCategory.GAMING,
                type = AssetType.ICON,
                previewEmoji = "🎮",
                directUrl = "https://img.icons8.com/color/96/controller.png",
                description = "Retro console controller for HTML5 arcade games",
                tags = listOf("gamepad", "controller", "arcade", "games", "play")
            ),
            StoreAsset(
                id = "asset_mystery_chest",
                title = "Treasure Chest",
                category = AssetCategory.GAMING,
                type = AssetType.ICON,
                previewEmoji = "🎁",
                directUrl = "https://img.icons8.com/color/96/treasure-chest.png",
                description = "Mystery loot box with surprise coins and powerups",
                tags = listOf("chest", "treasure", "box", "loot", "mystery", "reward")
            ),
            StoreAsset(
                id = "asset_gold_trophy",
                title = "Champion Trophy",
                category = AssetCategory.GAMING,
                type = AssetType.ICON,
                previewEmoji = "🏆",
                directUrl = "https://img.icons8.com/color/96/trophy.png",
                description = "Tournament winner gold trophy",
                tags = listOf("trophy", "cup", "champion", "rank", "leaderboard", "first")
            ),
            StoreAsset(
                id = "asset_fire_streak",
                title = "Daily Flame Streak",
                category = AssetCategory.GAMING,
                type = AssetType.ICON,
                previewEmoji = "🔥",
                directUrl = "https://img.icons8.com/color/96/fire-element.png",
                description = "Consecutive active days multiplier flame",
                tags = listOf("fire", "flame", "streak", "daily", "hot", "bonus")
            ),

            // --- Tasks & Rewards ---
            StoreAsset(
                id = "asset_video_play",
                title = "Watch & Earn Play",
                category = AssetCategory.REWARDS,
                type = AssetType.ICON,
                previewEmoji = "▶️",
                directUrl = "https://img.icons8.com/color/96/play-button-circled.png",
                description = "Video ad watch button with glowing play icon",
                tags = listOf("video", "watch", "ads", "play", "earn", "monetize")
            ),
            StoreAsset(
                id = "asset_gift_box",
                title = "Special Gift Box",
                category = AssetCategory.REWARDS,
                type = AssetType.ICON,
                previewEmoji = "🎀",
                directUrl = "https://img.icons8.com/color/96/gift.png",
                description = "Sign-up welcome bonus & celebratory gift",
                tags = listOf("gift", "bonus", "welcome", "reward", "box", "free")
            ),
            StoreAsset(
                id = "asset_referral_users",
                title = "Invite Friends Network",
                category = AssetCategory.REWARDS,
                type = AssetType.ICON,
                previewEmoji = "👥",
                directUrl = "https://img.icons8.com/color/96/add-user-group-man-man.png",
                description = "Multi-tier referral program and partner invitations",
                tags = listOf("referral", "invite", "friends", "team", "affiliate", "network")
            ),
            StoreAsset(
                id = "asset_calendar_check",
                title = "Daily Check-in",
                category = AssetCategory.REWARDS,
                type = AssetType.ICON,
                previewEmoji = "📅",
                directUrl = "https://img.icons8.com/color/96/calendar.png",
                description = "Calendar day-streak tracker for daily login bonuses",
                tags = listOf("calendar", "checkin", "daily", "login", "date", "claim")
            ),
            StoreAsset(
                id = "asset_piggy_bank",
                title = "Savings Piggy Bank",
                category = AssetCategory.REWARDS,
                type = AssetType.ICON,
                previewEmoji = "🐷",
                directUrl = "https://img.icons8.com/color/96/piggy-bank.png",
                description = "Vault balance and coin accumulator",
                tags = listOf("piggy", "bank", "savings", "balance", "coins", "stash")
            ),

            // --- Banners & Hero Art ---
            StoreAsset(
                id = "banner_cyber_arcade",
                title = "Cyber Arcade Neon",
                category = AssetCategory.BANNERS,
                type = AssetType.BANNER,
                previewEmoji = "🕹️",
                directUrl = "https://images.unsplash.com/photo-1550745165-9bc0b252726f?w=800&auto=format&fit=crop&q=80",
                description = "High-energy neon synthwave arcade header banner",
                tags = listOf("banner", "arcade", "cyber", "neon", "retro", "synthwave"),
                width = 800,
                height = 360
            ),
            StoreAsset(
                id = "banner_gold_coins",
                title = "Gold Coins Waterfall",
                category = AssetCategory.BANNERS,
                type = AssetType.BANNER,
                previewEmoji = "🪙",
                directUrl = "https://images.unsplash.com/photo-1621416894569-0f39ed31d247?w=800&auto=format&fit=crop&q=80",
                description = "Glorious shining gold tokens and treasure cascade",
                tags = listOf("banner", "coins", "gold", "crypto", "treasure", "rich"),
                width = 800,
                height = 360
            ),
            StoreAsset(
                id = "banner_purple_future",
                title = "Futuristic Grid Quest",
                category = AssetCategory.BANNERS,
                type = AssetType.BANNER,
                previewEmoji = "🌌",
                directUrl = "https://images.unsplash.com/photo-1509198397868-475647b2a1e5?w=800&auto=format&fit=crop&q=80",
                description = "Deep violet digital matrix grid for quest games",
                tags = listOf("banner", "purple", "matrix", "grid", "quest", "space"),
                width = 800,
                height = 360
            ),
            StoreAsset(
                id = "banner_deep_space",
                title = "Cosmic Nebula Glow",
                category = AssetCategory.BANNERS,
                type = AssetType.BANNER,
                previewEmoji = "🪐",
                directUrl = "https://images.unsplash.com/photo-1506703719100-a0f3a48c0f86?w=800&auto=format&fit=crop&q=80",
                description = "Stunning interstellar stars for Telegram space mini apps",
                tags = listOf("banner", "space", "nebula", "stars", "cosmic", "blue"),
                width = 800,
                height = 360
            ),

            // --- Mascots & Avatars ---
            StoreAsset(
                id = "avatar_bot_turbo",
                title = "TurboBot Cyborg",
                category = AssetCategory.AVATARS,
                type = AssetType.AVATAR,
                previewEmoji = "🤖",
                directUrl = "https://api.dicebear.com/7.x/bottts/png?seed=TurboBot&size=200",
                description = "Friendly AI assistant mascot avatar",
                tags = listOf("avatar", "robot", "cyborg", "bot", "blue", "mascot")
            ),
            StoreAsset(
                id = "avatar_golden_shiba",
                title = "Lucky Shiba Inu",
                category = AssetCategory.AVATARS,
                type = AssetType.AVATAR,
                previewEmoji = "🐕",
                directUrl = "https://api.dicebear.com/7.x/bottts/png?seed=LuckyShiba&size=200",
                description = "Charming gold-plated Shiba crypto mascot",
                tags = listOf("avatar", "shiba", "dog", "crypto", "mascot", "fun")
            ),
            StoreAsset(
                id = "avatar_pixel_knight",
                title = "Cyber Knight",
                category = AssetCategory.AVATARS,
                type = AssetType.AVATAR,
                previewEmoji = "🛡️",
                directUrl = "https://api.dicebear.com/7.x/bottts/png?seed=CyberKnight&size=200",
                description = "Guarded protector warrior for battle mini apps",
                tags = listOf("avatar", "knight", "cyber", "warrior", "hero", "game")
            ),
            StoreAsset(
                id = "avatar_neon_cat",
                title = "Neon Cat DJ",
                category = AssetCategory.AVATARS,
                type = AssetType.AVATAR,
                previewEmoji = "🐱",
                directUrl = "https://api.dicebear.com/7.x/bottts/png?seed=NeonCat&size=200",
                description = "Fun energetic cat mascot for music and arcade mini apps",
                tags = listOf("avatar", "cat", "neon", "dj", "mascot", "cute")
            )
        )
    }

    /**
     * Filters assets based on category and search query.
     */
    fun searchAssets(query: String, category: AssetCategory): List<StoreAsset> {
        val q = query.trim().lowercase()
        return CURATED_ASSETS.filter { asset ->
            val matchesCategory = category == AssetCategory.ALL || asset.category == category
            val matchesQuery = if (q.isBlank()) {
                true
            } else {
                asset.title.lowercase().contains(q) ||
                        asset.description.lowercase().contains(q) ||
                        asset.tags.any { it.contains(q) }
            }
            matchesCategory && matchesQuery
        }
    }

    /**
     * Retrieves saved user drive assets.
     */
    fun getSavedDriveAssets(): List<SavedDriveAsset> {
        val prefs = context.getSharedPreferences("miniapp_forge_store_prefs", Context.MODE_PRIVATE)
        val jsonStr = prefs.getString(PREF_SAVED_ASSETS, "[]") ?: "[]"
        return try {
            val arr = JSONArray(jsonStr)
            val list = mutableListOf<SavedDriveAsset>()
            for (i in 0 until arr.length()) {
                list.add(SavedDriveAsset.fromJson(arr.getJSONObject(i)))
            }
            list.sortedByDescending { it.timestamp }
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Saves a converted drive asset for future reuse.
     */
    fun saveDriveAsset(title: String, originalUrl: String, directUrl: String): SavedDriveAsset {
        val current = getSavedDriveAssets().toMutableList()
        val newAsset = SavedDriveAsset(
            title = if (title.isNotBlank()) title else "Drive Asset (${current.size + 1})",
            originalDriveUrl = originalUrl,
            directImageUrl = directUrl
        )
        current.add(0, newAsset)

        val arr = JSONArray()
        current.take(50).forEach { arr.put(it.toJson()) }

        val prefs = context.getSharedPreferences("miniapp_forge_store_prefs", Context.MODE_PRIVATE)
        prefs.edit().putString(PREF_SAVED_ASSETS, arr.toString()).apply()
        return newAsset
    }

    /**
     * Deletes a saved drive asset.
     */
    fun deleteSavedDriveAsset(id: String) {
        val current = getSavedDriveAssets().filter { it.id != id }
        val arr = JSONArray()
        current.forEach { arr.put(it.toJson()) }
        val prefs = context.getSharedPreferences("miniapp_forge_store_prefs", Context.MODE_PRIVATE)
        prefs.edit().putString(PREF_SAVED_ASSETS, arr.toString()).apply()
    }
}
