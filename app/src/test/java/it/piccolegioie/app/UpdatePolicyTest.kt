package it.piccolegioie.app

import org.junit.Assert.*
import org.junit.Test

class UpdatePolicyTest {
    @Test fun comparesAndroidVersionCodesInsteadOfVersionNames() {
        assertTrue(UpdatePolicy.isNewer(9, 10))
        assertFalse(UpdatePolicy.isNewer(10, 10))
        assertFalse(UpdatePolicy.isNewer(10, 9))
        assertFalse(UpdatePolicy.isNewer(10, Long.MAX_VALUE))
    }
    @Test fun acceptsOnlyAssetsFromOurRepository() {
        assertTrue(UpdatePolicy.isReleaseAsset("https://github.com/carellice/piccole-gioie/releases/download/v1.3.0/piccole-gioie.apk"))
        listOf("http://github.com/carellice/piccole-gioie/releases/download/a/b",
            "https://github.com/other/piccole-gioie/releases/download/a/b",
            "https://github.com.evil.test/carellice/piccole-gioie/releases/download/a/b",
            "https://github.com@evil.test/carellice/piccole-gioie/releases/download/a/b",
            "https://github.com:443/carellice/piccole-gioie/releases/download/a/b").forEach { assertFalse(it, UpdatePolicy.isReleaseAsset(it)) }
    }
    @Test fun redirectsStayOnGitHubOverHttps() {
        assertTrue(UpdatePolicy.isGitHubHttps("https://release-assets.githubusercontent.com/github-production-release-asset/a"))
        assertFalse(UpdatePolicy.isGitHubHttps("https://evilgithubusercontent.com/a"))
        assertFalse(UpdatePolicy.isGitHubHttps("http://github.com/a"))
        assertFalse(UpdatePolicy.isGitHubHttps("not a url"))
    }
    @Test fun requiresFullSha256() {
        assertTrue(UpdatePolicy.validHash("a0".repeat(32)))
        assertFalse(UpdatePolicy.validHash("a0".repeat(31)))
        assertFalse(UpdatePolicy.validHash("z".repeat(64)))
    }
}
