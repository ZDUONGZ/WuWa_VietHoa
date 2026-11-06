package com.wuwa.viethoa.installer

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class InstallerLogicTest {

    @Test
    fun testParseStatusTokensValid() {
        val tokens = listOf("com.kurogame.wutheringwaves.global", "1", "1")
        val status = GameDetector.parseStatusTokens(tokens)

        assertTrue(status.isInstalled)
        assertTrue(status.hasDownloadedData)
        assertTrue(status.isPatched)
        assertEquals("com.kurogame.wutheringwaves.global", GameDetector.activePackage)
    }

    @Test
    fun testParseStatusTokensUninstalled() {
        val tokens = listOf("none", "0", "0")
        val status = GameDetector.parseStatusTokens(tokens)

        assertEquals(false, status.isInstalled)
        assertEquals(false, status.hasDownloadedData)
        assertEquals(false, status.isPatched)
    }

    @Test
    fun testFindMatchingPakAssetDoesNotFalsePositiveOnClientOrContent() {
        val dummyAssets = listOf(
            ReleaseAsset("WuWa_Client_Patch.pak", "http://example.com/client.pak", 1000),
            ReleaseAsset("WuWaVH_EN_99_P.pak", "http://example.com/en.pak", 2000),
            ReleaseAsset("WuWaVH_HanViet_99_P.pak", "http://example.com/hanviet.pak", 3000)
        )

        val enMatch = PatchInstaller.findMatchingPakAsset(dummyAssets, NameStyle.ENGLISH)
        assertNotNull(enMatch)
        assertEquals("WuWaVH_EN_99_P.pak", enMatch?.name)

        val hvMatch = PatchInstaller.findMatchingPakAsset(dummyAssets, NameStyle.HAN_VIET)
        assertNotNull(hvMatch)
        assertEquals("WuWaVH_HanViet_99_P.pak", hvMatch?.name)
    }

    @Test
    fun testFindMatchingFontAssetDistinguishesLexendAndSegoe() {
        val dummyAssets = listOf(
            ReleaseAsset("WahuFont_Segoe_100_P.pak", "http://example.com/segoe.pak", 1000),
            ReleaseAsset("WahuFont_Lexend_100_P.pak", "http://example.com/lexend.pak", 2000)
        )

        val lexendMatch = PatchInstaller.findMatchingFontAsset(dummyAssets, FontOption.LEXEND)
        assertNotNull(lexendMatch)
        assertEquals("WahuFont_Lexend_100_P.pak", lexendMatch?.name)

        val segoeMatch = PatchInstaller.findMatchingFontAsset(dummyAssets, FontOption.SEGOE_UI)
        assertNotNull(segoeMatch)
        assertEquals("WahuFont_Segoe_100_P.pak", segoeMatch?.name)

        val noneMatch = PatchInstaller.findMatchingFontAsset(dummyAssets, FontOption.NONE)
        assertNull(noneMatch)
    }
}
