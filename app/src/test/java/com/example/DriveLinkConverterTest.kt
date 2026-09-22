package com.example

import com.example.data.store.AssetCategory
import com.example.data.store.AssetStoreRepository
import com.example.util.DriveLinkConverter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DriveLinkConverterTest {

    @Test
    fun testExtractFileIdFromStandardShareUrl() {
        val url = "https://drive.google.com/file/d/1BxiMVs0XRA5nFMdKvBdBZjgmUUqptlbs74OgvE2upms/view?usp=sharing"
        val fileId = DriveLinkConverter.extractFileId(url)
        assertEquals("1BxiMVs0XRA5nFMdKvBdBZjgmUUqptlbs74OgvE2upms", fileId)
    }

    @Test
    fun testExtractFileIdFromOpenUrl() {
        val url = "https://drive.google.com/open?id=1AbCdEfGhIjKlMnOpQrStUvWxYz-1234"
        val fileId = DriveLinkConverter.extractFileId(url)
        assertEquals("1AbCdEfGhIjKlMnOpQrStUvWxYz-1234", fileId)
    }

    @Test
    fun testExtractFileIdFromUcUrl() {
        val url = "https://drive.google.com/uc?export=download&id=1XyZ9876543210_abcdefghij"
        val fileId = DriveLinkConverter.extractFileId(url)
        assertEquals("1XyZ9876543210_abcdefghij", fileId)
    }

    @Test
    fun testConvertGoogleDriveUrlToDirectCdn() {
        val url = "https://drive.google.com/file/d/1X5X9r9W9q9L4P2B1Z8Y_token123/view"
        val result = DriveLinkConverter.convert(url)
        assertNotNull(result)
        assertEquals("1X5X9r9W9q9L4P2B1Z8Y_token123", result?.fileId)
        assertEquals(
            "https://lh3.googleusercontent.com/d/1X5X9r9W9q9L4P2B1Z8Y_token123",
            result?.directImageUrl
        )
        assertEquals(
            "https://drive.google.com/uc?export=view&id=1X5X9r9W9q9L4P2B1Z8Y_token123",
            result?.alternativeUrl
        )
        assertTrue(result?.htmlSnippet?.contains("https://lh3.googleusercontent.com/d/") == true)
    }

    @Test
    fun testConvertDropboxUrl() {
        val url = "https://www.dropbox.com/s/12345abcdef/my_image.png?dl=0"
        val result = DriveLinkConverter.convert(url)
        assertNotNull(result)
        assertEquals("Dropbox", result?.serviceType)
        assertEquals("https://www.dropbox.com/s/12345abcdef/my_image.png?raw=1", result?.directImageUrl)
    }

    @Test
    fun testSanitizeOrConvert() {
        // Drive link should be converted
        val driveUrl = "https://drive.google.com/file/d/MyFileId123456789012345/view?usp=sharing"
        val converted = DriveLinkConverter.sanitizeOrConvert(driveUrl)
        assertEquals("https://lh3.googleusercontent.com/d/MyFileId123456789012345", converted)

        // Normal https URL should be left untouched
        val normalUrl = "https://images.unsplash.com/photo-test?w=800"
        val untouched = DriveLinkConverter.sanitizeOrConvert(normalUrl)
        assertEquals(normalUrl, untouched)
    }

    @Test
    fun testCuratedAssetsContainKeyCategories() {
        val assets = AssetStoreRepository.CURATED_ASSETS
        assertTrue(assets.isNotEmpty())

        val cryptoAssets = assets.filter { it.category == AssetCategory.CRYPTO }
        assertTrue("Should contain TON or crypto tokens", cryptoAssets.any { it.title.contains("TON") })

        val telegramAssets = assets.filter { it.category == AssetCategory.TELEGRAM }
        assertTrue("Should contain Telegram official assets", telegramAssets.any { it.title.contains("Telegram") })

        val banners = assets.filter { it.category == AssetCategory.BANNERS }
        assertTrue("Should contain mini app banners", banners.isNotEmpty())
    }
}
