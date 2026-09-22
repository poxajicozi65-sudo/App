package com.example.generator

import android.content.Context
import com.example.data.model.FormData
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

object MiniAppGenerator {

    data class GenerationResult(
        val projectDir: File,
        val indexFile: File,
        val adminFile: File,
        val rulesFile: File,
        val setupFile: File
    )

    fun generate(context: Context, projectId: Long, formData: FormData): GenerationResult {
        val projectDir = File(context.filesDir, "projects/$projectId")
        if (!projectDir.exists()) {
            projectDir.mkdirs()
        }

        // 1. Select template assets based on templateSource
        val (indexAssetPath, adminAssetPath, rulesAssetPath) = when (formData.templateSource.lowercase()) {
            "earnfast" -> Triple("templates/sources/earnfast_app.html", "templates/sources/earnfast_admin.html", "templates/sources/firestore_rules.rules")
            "realcash" -> Triple("templates/sources/realcash_app.html", "templates/sources/realcash_admin.html", "templates/sources/firestore_rules.rules")
            "gametozone" -> Triple("templates/sources/gametozone_app.html", "templates/sources/gametozone_admin.html", "templates/sources/firestore_rules.rules")
            "cashreward" -> Triple("templates/sources/cashreward_app.html", "templates/sources/cashreward_admin.html", "templates/sources/firestore_rules.rules")
            else -> Triple("templates/index.html", "templates/admin.html", "templates/firestore.rules")
        }

        val rawIndex = try {
            readAsset(context, indexAssetPath)
        } catch (e: Exception) {
            readAsset(context, "templates/index.html")
        }
        val rawAdmin = try {
            readAsset(context, adminAssetPath)
        } catch (e: Exception) {
            readAsset(context, "templates/admin.html")
        }
        val rawRules = try {
            readAsset(context, rulesAssetPath)
        } catch (e: Exception) {
            readAsset(context, "templates/firestore.rules")
        }

        // 2. Prepare replacements
        val firebaseConfigJson = resolveFirebaseConfig(formData)
        val paymentMethodsJson = JSONArray(formData.paymentMethods).toString()
        val logoMarkup = resolveLogoMarkup(formData)
        val initial = if (formData.appName.isNotBlank()) formData.appName.take(1).uppercase() else "M"

        // 3. Process index.html
        var processedIndex = rawIndex
            .replace("{{APP_NAME}}", escapeHtml(formData.appName))
            .replace("{{APP_NAME_INITIAL}}", initial)
            .replace("{{APP_TAGLINE}}", escapeHtml(formData.appTagline))
            .replace("{{ACCENT_COLOR}}", formData.accentColor)
            .replace("{{FONT_FAMILY}}", formData.fontFamily)
            .replace("{{THEME}}", formData.theme)
            .replace("{{LOGO_DATA}}", logoMarkup)
            .replace("{{FIREBASE_CONFIG_JSON}}", firebaseConfigJson)
            .replace("{{SIGNUP_BONUS}}", formData.signupBonus.toString())
            .replace("{{AD_REWARD}}", formData.adWatchReward.toString())
            .replace("{{REFERRAL_BONUS}}", formData.referralBonus.toString())
            .replace("{{MIN_WITHDRAWAL}}", formData.minWithdrawal.toString())
            .replace("{{COIN_RATE_COINS}}", formData.coinRateCoins.toString())
            .replace("{{COIN_RATE_CURRENCY}}", formData.coinRateCurrency.toString())
            .replace("{{CURRENCY_SYMBOL}}", formData.currencySymbol)
            .replace("{{AD_SDK_URL}}", formData.adSdkScriptUrl)
            .replace("{{AD_ZONE_ID}}", formData.adZoneId)
            .replace("{{PAYMENT_METHODS_JSON}}", paymentMethodsJson)

        // Strip features if disabled
        processedIndex = stripFeature(processedIndex, "WATCH_EARN", formData.enableWatchEarn)
        processedIndex = stripFeature(processedIndex, "VISIT_EARN", formData.enableVisitEarn)
        processedIndex = stripFeature(processedIndex, "SOCIAL_TASKS", formData.enableSocialTasks)
        processedIndex = stripFeature(processedIndex, "COLOR_GUESS", formData.enableColorGuess)
        processedIndex = stripFeature(processedIndex, "SPIN_WHEEL", formData.enableSpinWheel)
        processedIndex = stripFeature(processedIndex, "QUIZ_GAME", formData.enableQuizGame)
        processedIndex = stripFeature(processedIndex, "TIC_TAC_TOE", formData.enableTicTacToe)
        processedIndex = stripFeature(processedIndex, "REFERRAL", formData.enableReferral)
        processedIndex = stripFeature(processedIndex, "WITHDRAWAL", formData.enableWithdrawal)

        // 4. Process admin.html
        val processedAdmin = rawAdmin
            .replace("{{APP_NAME}}", escapeHtml(formData.appName))
            .replace("{{APP_NAME_INITIAL}}", initial)
            .replace("{{ADMIN_EMAIL}}", formData.adminEmail)
            .replace("{{ACCENT_COLOR}}", formData.accentColor)
            .replace("{{FIREBASE_CONFIG_JSON}}", firebaseConfigJson)
            .replace("{{SIGNUP_BONUS}}", formData.signupBonus.toString())
            .replace("{{AD_REWARD}}", formData.adWatchReward.toString())
            .replace("{{REFERRAL_BONUS}}", formData.referralBonus.toString())
            .replace("{{MIN_WITHDRAWAL}}", formData.minWithdrawal.toString())
            .replace("{{COIN_RATE_COINS}}", formData.coinRateCoins.toString())
            .replace("{{COIN_RATE_CURRENCY}}", formData.coinRateCurrency.toString())
            .replace("{{CURRENCY_SYMBOL}}", formData.currencySymbol)

        // 5. Process firestore.rules
        val processedRules = rawRules
            .replace("{{ADMIN_EMAIL}}", formData.adminEmail)

        // 6. Generate dynamic SETUP.md
        val setupContent = generateSetupGuide(formData)

        // 7. Write files
        val indexFile = File(projectDir, "index.html").apply { writeText(processedIndex) }
        val adminFile = File(projectDir, "admin.html").apply { writeText(processedAdmin) }
        val rulesFile = File(projectDir, "firestore.rules").apply { writeText(processedRules) }
        val setupFile = File(projectDir, "SETUP.md").apply { writeText(setupContent) }

        return GenerationResult(
            projectDir = projectDir,
            indexFile = indexFile,
            adminFile = adminFile,
            rulesFile = rulesFile,
            setupFile = setupFile
        )
    }

    private fun stripFeature(content: String, tag: String, enabled: Boolean): String {
        return if (enabled) {
            content
        } else {
            val regex = Regex("""<!--\s*FEATURE:${tag}:START\s*-->[\s\S]*?<!--\s*FEATURE:${tag}:END\s*-->""", RegexOption.MULTILINE)
            content.replace(regex, "")
        }
    }

    private fun resolveFirebaseConfig(formData: FormData): String {
        if (formData.fullConfigJson.isNotBlank()) {
            try {
                val json = JSONObject(formData.fullConfigJson)
                return json.toString(2)
            } catch (e: Exception) {
                // Ignore parse failure, fall back to individual fields
            }
        }
        val obj = JSONObject().apply {
            put("apiKey", formData.apiKey)
            put("authDomain", formData.authDomain)
            put("projectId", formData.projectId)
            put("storageBucket", formData.storageBucket)
            put("messagingSenderId", formData.messagingSenderId)
            put("appId", formData.appId)
        }
        return obj.toString(2)
    }

    private fun resolveLogoMarkup(formData: FormData): String {
        val logo = formData.logoData.trim()
        return when {
            logo.startsWith("data:image") || logo.startsWith("http://") || logo.startsWith("https://") -> {
                """<img src="$logo" alt="Logo" class="w-full h-full object-cover">"""
            }
            logo.startsWith("<svg") -> {
                logo
            }
            else -> {
                """<svg class="w-6 h-6 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 10V3L4 14h7v7l9-11h-7z"/></svg>"""
            }
        }
    }

    private fun escapeHtml(text: String): String {
        return text.replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&#39;")
    }

    private fun readAsset(context: Context, path: String): String {
        return context.assets.open(path).bufferedReader().use { it.readText() }
    }

    private fun generateSetupGuide(formData: FormData): String {
        return """
# ${formData.appName} — Telegram Mini App Setup Guide

Generated on-device by **MiniApp Forge**.
This package contains everything needed to run and host your Watch & Earn Telegram Mini App:
- `index.html`: The client Telegram Mini App running inside Telegram
- `admin.html`: The administrative dashboard for payouts and user balance control
- `firestore.rules`: Security rules for Cloud Firestore
- `SETUP.md`: This deployment guide

---

## Step 1: Firebase Project Setup
1. Go to [https://console.firebase.google.com](https://console.firebase.google.com) and click **"Add project"**.
2. Give your project a name (e.g. `${formData.appName}`) and disable or enable Google Analytics according to your preference.
3. In Project Overview, click the Web icon `</>` to register a Web App.
4. If not done already, note down your Firebase configuration object.

## Step 2: Enable Firebase Services
1. **Authentication:**
   - Go to **Build > Authentication > Sign-in method**.
   - Enable **Email/Password**.
   - (Optional) Enable **Anonymous** for instantaneous Telegram guest logins.
2. **Cloud Firestore:**
   - Go to **Build > Firestore Database > Create Database**.
   - Select a server location closest to your users.
   - Choose **Start in production mode**.
3. **Deploy Security Rules:**
   - In Firestore, open the **Rules** tab.
   - Replace the entire content with the contents of `firestore.rules` included in this package.
   - Click **Publish**.

## Step 3: Deploying index.html (User App)
The `index.html` file is completely self-contained (zero build step needed). You can host it on any static hosting:
- **GitHub Pages:** Create a repository, upload `index.html`, and enable Pages in repository settings.
- **Cloudflare Pages / Vercel:** Drag and drop your project directory for instant global CDN HTTPS hosting.
- **Firebase Hosting:** Run `firebase deploy --only hosting` to host on Firebase.

*Note:* Telegram requires an HTTPS URL to load Mini Apps.

## Step 4: Connecting to Telegram Bot
1. Open Telegram and search for [@BotFather](https://t.me/BotFather).
2. Send `/newbot` to create your Telegram bot (e.g., `${formData.appName}Bot`).
3. Send `/newapp` to create a Telegram Mini App:
   - Select your bot.
   - Provide a title (e.g., `${formData.appName}`).
   - Provide a short description.
   - Upload the 640x360 app icon when prompted.
   - Provide your HTTPS Web App URL (the URL from Step 3 where `index.html` is hosted).
   - Choose a short name (e.g., `app`).
4. Test your Mini App: Open `https://t.me/<your_bot_username>/<short_name>` on your phone!

## Step 5: Admin Panel (admin.html)
1. Open `admin.html` in your browser or host it privately.
2. Log in using your configured administrator email:
   - **Admin Email:** `${formData.adminEmail}`
   - If this is your first time, type any secure password; the admin panel will automatically register your administrator account with Firebase.
3. Once logged in:
   - View pending user withdrawal requests.
   - Click **Approve** or **Reject** on payout items.
   - Lookup and adjust user coin balances manually.

## Step 6: Economy and Ads Configuration
- **Signup Bonus:** ${formData.signupBonus} coins
- **Ad Watch Reward:** ${formData.adWatchReward} coins
- **Referral Bonus:** ${formData.referralBonus} coins
- **Minimum Withdrawal:** ${formData.minWithdrawal} coins (${formData.currencySymbol}${((formData.minWithdrawal.toDouble() / formData.coinRateCoins) * formData.coinRateCurrency)})
- **Ad Zone ID:** `${formData.adZoneId}`
- **Payment Methods:** ${formData.paymentMethods.joinToString(", ")}

---
*Built with MiniApp Forge — 100% on-device Telegram Mini App creator.*
        """.trimIndent()
    }
}
