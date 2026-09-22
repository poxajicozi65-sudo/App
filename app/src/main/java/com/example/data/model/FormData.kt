package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import org.json.JSONArray
import org.json.JSONObject

@Entity(tableName = "projects")
data class Project(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val formDataJson: String
)

data class FormData(
    // Step 1: Branding
    val appName: String = "TurboReward",
    val appTagline: String = "Watch videos, play mini-games & cash out rewards",
    val accentColor: String = "#0284C7",
    val logoData: String = "",
    val fontFamily: String = "Inter",
    val theme: String = "dark",

    // Step 2: Firebase config
    val apiKey: String = "",
    val authDomain: String = "",
    val projectId: String = "",
    val storageBucket: String = "",
    val messagingSenderId: String = "",
    val appId: String = "",
    val adminEmail: String = "admin@example.com",
    val fullConfigJson: String = "",

    // Step 3: Economy settings
    val signupBonus: Int = 100,
    val adWatchReward: Int = 20,
    val referralBonus: Int = 50,
    val minWithdrawal: Int = 500,
    val coinRateCoins: Int = 1000,
    val coinRateCurrency: Double = 10.0,
    val currencySymbol: String = "₹",

    // Step 4: Feature toggles
    val enableWatchEarn: Boolean = true,
    val enableVisitEarn: Boolean = true,
    val enableSocialTasks: Boolean = true,
    val enableColorGuess: Boolean = true,
    val enableSpinWheel: Boolean = true,
    val enableQuizGame: Boolean = true,
    val enableTicTacToe: Boolean = true,
    val enableReferral: Boolean = true,
    val enableWithdrawal: Boolean = true,
    val paymentMethods: List<String> = listOf("UPI", "Paytm", "PayPal", "USDT"),

    // Step 5: Ad network
    val adSdkScriptUrl: String = "https://example.com/ad-sdk.js",
    val adZoneId: String = "zone_1024",

    // Step 6: Template Source Preset
    val templateSource: String = "earnfast",

    // Step 7: AI toggle
    val fineTuneWithAi: Boolean = false
) {
    fun toJson(): String {
        val json = JSONObject()
        json.put("appName", appName)
        json.put("appTagline", appTagline)
        json.put("accentColor", accentColor)
        json.put("logoData", logoData)
        json.put("fontFamily", fontFamily)
        json.put("theme", theme)

        json.put("apiKey", apiKey)
        json.put("authDomain", authDomain)
        json.put("projectId", projectId)
        json.put("storageBucket", storageBucket)
        json.put("messagingSenderId", messagingSenderId)
        json.put("appId", appId)
        json.put("adminEmail", adminEmail)
        json.put("fullConfigJson", fullConfigJson)

        json.put("signupBonus", signupBonus)
        json.put("adWatchReward", adWatchReward)
        json.put("referralBonus", referralBonus)
        json.put("minWithdrawal", minWithdrawal)
        json.put("coinRateCoins", coinRateCoins)
        json.put("coinRateCurrency", coinRateCurrency)
        json.put("currencySymbol", currencySymbol)

        json.put("enableWatchEarn", enableWatchEarn)
        json.put("enableVisitEarn", enableVisitEarn)
        json.put("enableSocialTasks", enableSocialTasks)
        json.put("enableColorGuess", enableColorGuess)
        json.put("enableSpinWheel", enableSpinWheel)
        json.put("enableQuizGame", enableQuizGame)
        json.put("enableTicTacToe", enableTicTacToe)
        json.put("enableReferral", enableReferral)
        json.put("enableWithdrawal", enableWithdrawal)

        val methodsArr = JSONArray()
        paymentMethods.forEach { methodsArr.put(it) }
        json.put("paymentMethods", methodsArr)

        json.put("adSdkScriptUrl", adSdkScriptUrl)
        json.put("adZoneId", adZoneId)
        json.put("templateSource", templateSource)
        json.put("fineTuneWithAi", fineTuneWithAi)

        return json.toString()
    }

    companion object {
        fun fromJson(jsonStr: String): FormData {
            if (jsonStr.isBlank()) return FormData()
            return try {
                val json = JSONObject(jsonStr)
                val methodsList = mutableListOf<String>()
                val methodsArr = json.optJSONArray("paymentMethods")
                if (methodsArr != null) {
                    for (i in 0 until methodsArr.length()) {
                        methodsList.add(methodsArr.getString(i))
                    }
                } else {
                    methodsList.addAll(listOf("UPI", "Paytm", "PayPal", "USDT"))
                }

                FormData(
                    appName = json.optString("appName", "TurboReward"),
                    appTagline = json.optString("appTagline", "Watch videos, play mini-games & cash out rewards"),
                    accentColor = json.optString("accentColor", "#0284C7"),
                    logoData = json.optString("logoData", ""),
                    fontFamily = json.optString("fontFamily", "Inter"),
                    theme = json.optString("theme", "dark"),

                    apiKey = json.optString("apiKey", ""),
                    authDomain = json.optString("authDomain", ""),
                    projectId = json.optString("projectId", ""),
                    storageBucket = json.optString("storageBucket", ""),
                    messagingSenderId = json.optString("messagingSenderId", ""),
                    appId = json.optString("appId", ""),
                    adminEmail = json.optString("adminEmail", "admin@example.com"),
                    fullConfigJson = json.optString("fullConfigJson", ""),

                    signupBonus = json.optInt("signupBonus", 100),
                    adWatchReward = json.optInt("adWatchReward", 20),
                    referralBonus = json.optInt("referralBonus", 50),
                    minWithdrawal = json.optInt("minWithdrawal", 500),
                    coinRateCoins = json.optInt("coinRateCoins", 1000),
                    coinRateCurrency = json.optDouble("coinRateCurrency", 10.0),
                    currencySymbol = json.optString("currencySymbol", "₹"),

                    enableWatchEarn = json.optBoolean("enableWatchEarn", true),
                    enableVisitEarn = json.optBoolean("enableVisitEarn", true),
                    enableSocialTasks = json.optBoolean("enableSocialTasks", true),
                    enableColorGuess = json.optBoolean("enableColorGuess", true),
                    enableSpinWheel = json.optBoolean("enableSpinWheel", true),
                    enableQuizGame = json.optBoolean("enableQuizGame", true),
                    enableTicTacToe = json.optBoolean("enableTicTacToe", true),
                    enableReferral = json.optBoolean("enableReferral", true),
                    enableWithdrawal = json.optBoolean("enableWithdrawal", true),
                    paymentMethods = if (methodsList.isEmpty()) listOf("UPI", "Paytm", "PayPal", "USDT") else methodsList,

                    adSdkScriptUrl = json.optString("adSdkScriptUrl", "https://example.com/ad-sdk.js"),
                    adZoneId = json.optString("adZoneId", "zone_1024"),
                    templateSource = json.optString("templateSource", "earnfast"),
                    fineTuneWithAi = json.optBoolean("fineTuneWithAi", false)
                )
            } catch (e: Exception) {
                FormData()
            }
        }
    }
}
