package com.example.util

import java.util.regex.Pattern

/**
 * Result data class for converted cloud storage links (Google Drive, Dropbox, etc.)
 */
data class ConvertedLinkResult(
    val originalUrl: String,
    val fileId: String,
    val directImageUrl: String,
    val alternativeUrl: String,
    val htmlSnippet: String,
    val markdownSnippet: String,
    val serviceType: String = "Google Drive",
    val isValid: Boolean = true,
    val message: String = "Link successfully converted to high-performance direct CDN image URL!"
)

/**
 * Utility to convert Google Drive shareable URLs into direct, embeddable CDN image links
 * that work seamlessly inside HTML <img> tags, Telegram Mini Apps, WebViews, and Coil.
 */
object DriveLinkConverter {

    // Regex patterns to identify and extract Google Drive file ID
    private val DRIVE_FILE_PATTERN = Pattern.compile(
        "drive\\.google\\.com/file/d/([a-zA-Z0-9_-]+)",
        Pattern.CASE_INSENSITIVE
    )

    private val DRIVE_OPEN_PATTERN = Pattern.compile(
        "drive\\.google\\.com/open\\?[^\"'\\s]*id=([a-zA-Z0-9_-]+)",
        Pattern.CASE_INSENSITIVE
    )

    private val DRIVE_UC_PATTERN = Pattern.compile(
        "drive\\.google\\.com/uc\\?[^\"'\\s]*id=([a-zA-Z0-9_-]+)",
        Pattern.CASE_INSENSITIVE
    )

    private val DOCS_FILE_PATTERN = Pattern.compile(
        "docs\\.google\\.com/file/d/([a-zA-Z0-9_-]+)",
        Pattern.CASE_INSENSITIVE
    )

    private val DROPBOX_PATTERN = Pattern.compile(
        "dropbox\\.com/s(cl/fi)?/[^\"'\\s]+",
        Pattern.CASE_INSENSITIVE
    )

    /**
     * Checks whether the provided URL is a Google Drive link.
     */
    fun isGoogleDriveLink(url: String): Boolean {
        val trimmed = url.trim()
        return trimmed.contains("drive.google.com") || trimmed.contains("docs.google.com/file/d")
    }

    /**
     * Extracts the Google Drive file ID from various sharing link formats.
     */
    fun extractFileId(url: String): String? {
        val trimmed = url.trim()
        if (trimmed.isBlank()) return null

        // 1. /file/d/{id} format
        val fileMatcher = DRIVE_FILE_PATTERN.matcher(trimmed)
        if (fileMatcher.find()) {
            return fileMatcher.group(1)
        }

        // 2. /open?id={id} format
        val openMatcher = DRIVE_OPEN_PATTERN.matcher(trimmed)
        if (openMatcher.find()) {
            return openMatcher.group(1)
        }

        // 3. /uc?id={id} format
        val ucMatcher = DRIVE_UC_PATTERN.matcher(trimmed)
        if (ucMatcher.find()) {
            return ucMatcher.group(1)
        }

        // 4. docs.google.com/file/d/{id} format
        val docsMatcher = DOCS_FILE_PATTERN.matcher(trimmed)
        if (docsMatcher.find()) {
            return docsMatcher.group(1)
        }

        // 5. If user just entered raw alphanumeric ID (typically 25 to 45 chars)
        if (trimmed.matches(Regex("^[a-zA-Z0-9_-]{20,50}$"))) {
            return trimmed
        }

        return null
    }

    /**
     * Converts a Google Drive sharing URL or raw ID into direct image links.
     */
    fun convert(input: String): ConvertedLinkResult? {
        val trimmed = input.trim()
        if (trimmed.isBlank()) return null

        // Check Dropbox
        if (DROPBOX_PATTERN.matcher(trimmed).find()) {
            val direct = if (trimmed.contains("?dl=0")) {
                trimmed.replace("?dl=0", "?raw=1")
            } else if (!trimmed.contains("?raw=1")) {
                if (trimmed.contains("?")) "$trimmed&raw=1" else "$trimmed?raw=1"
            } else {
                trimmed
            }
            return ConvertedLinkResult(
                originalUrl = trimmed,
                fileId = "dropbox",
                directImageUrl = direct,
                alternativeUrl = direct,
                htmlSnippet = """<img src="$direct" alt="Dropbox Asset" class="rounded-xl w-full" />""",
                markdownSnippet = "![Asset]($direct)",
                serviceType = "Dropbox",
                isValid = true,
                message = "Converted Dropbox link to direct media stream!"
            )
        }

        val fileId = extractFileId(trimmed) ?: return null

        // Primary Google usercontent CDN direct link (fastest, zero CORS issues, works in <img> and Coil)
        val directCdn = "https://lh3.googleusercontent.com/d/$fileId"

        // Secondary export direct stream
        val altDirect = "https://drive.google.com/uc?export=view&id=$fileId"

        return ConvertedLinkResult(
            originalUrl = trimmed,
            fileId = fileId,
            directImageUrl = directCdn,
            alternativeUrl = altDirect,
            htmlSnippet = """<img src="$directCdn" alt="Drive Image" class="w-full h-full object-cover rounded-xl" />""",
            markdownSnippet = "![Image]($directCdn)",
            serviceType = "Google Drive",
            isValid = true,
            message = "Converted to Google CDN direct embed URL (lh3.googleusercontent.com)!"
        )
    }

    /**
     * If the input is a Google Drive link, automatically convert it to a direct CDN link;
     * otherwise returns the original input unchanged.
     */
    fun sanitizeOrConvert(input: String): String {
        val trimmed = input.trim()
        if (isGoogleDriveLink(trimmed)) {
            val result = convert(trimmed)
            if (result != null) return result.directImageUrl
        }
        return trimmed
    }
}
