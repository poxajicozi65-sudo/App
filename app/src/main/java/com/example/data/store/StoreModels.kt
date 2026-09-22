package com.example.data.store

import org.json.JSONObject
import java.util.UUID

enum class AssetCategory(val label: String, val icon: String) {
    ALL("All Assets", "✨"),
    CRYPTO("Crypto & Web3", "🪙"),
    GAMING("Gaming & Arcade", "🎮"),
    REWARDS("Tasks & Rewards", "🎁"),
    TELEGRAM("Telegram & Badges", "⭐"),
    BANNERS("Banners & Art", "🖼️"),
    AVATARS("Mascots & Avatars", "🤖")
}

enum class AssetType {
    ICON,
    IMAGE,
    BANNER,
    AVATAR,
    SVG
}

data class StoreAsset(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val category: AssetCategory,
    val type: AssetType = AssetType.ICON,
    val previewEmoji: String = "",
    val directUrl: String = "",
    val svgData: String = "",
    val description: String = "",
    val tags: List<String> = emptyList(),
    val width: Int = 120,
    val height: Int = 120
) {
    /**
     * Returns an HTML img tag or SVG string suitable for embedding into mini apps.
     */
    fun toEmbedSnippet(): String {
        return when {
            directUrl.isNotBlank() -> """<img src="$directUrl" alt="$title" class="w-full h-full object-cover rounded-xl" />"""
            svgData.isNotBlank() -> svgData
            else -> """<span class="text-3xl">$previewEmoji</span>"""
        }
    }
}

data class SavedDriveAsset(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val originalDriveUrl: String,
    val directImageUrl: String,
    val timestamp: Long = System.currentTimeMillis()
) {
    fun toJson(): JSONObject = JSONObject().apply {
        put("id", id)
        put("title", title)
        put("originalDriveUrl", originalDriveUrl)
        put("directImageUrl", directImageUrl)
        put("timestamp", timestamp)
    }

    companion object {
        fun fromJson(json: JSONObject): SavedDriveAsset = SavedDriveAsset(
            id = json.optString("id", UUID.randomUUID().toString()),
            title = json.optString("title", "Saved Asset"),
            originalDriveUrl = json.optString("originalDriveUrl", ""),
            directImageUrl = json.optString("directImageUrl", ""),
            timestamp = json.optLong("timestamp", System.currentTimeMillis())
        )
    }
}
