package ir.abrova.auth.ui

import ir.abrova.auth.AbrovaAuth
import ir.abrova.auth.AuthResult
import ir.abrova.auth.MemorySessionStorage
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Test

/**
 * The sign-in screen against a real Abrova Auth project. Skipped unless these are set:
 * `ABROVA_AUTH_LIVE_KEY` (a project with anonymous, e-mail and password without a required confirmation, and magic
 * link on), `ABROVA_AUTH_LIVE_BASE`, `ABROVA_AUTH_LIVE_EMAIL` (a new address), `ABROVA_AUTH_LIVE_PASSWORD` and
 * `ABROVA_AUTH_LIVE_MAGIC_URL` (one of the project's redirect URLs).
 */
class SignInLiveTest {
    private val key = System.getenv("ABROVA_AUTH_LIVE_KEY").orEmpty()
    private val base = System.getenv("ABROVA_AUTH_LIVE_BASE").orEmpty()
    private val email = System.getenv("ABROVA_AUTH_LIVE_EMAIL").orEmpty()
    private val password = System.getenv("ABROVA_AUTH_LIVE_PASSWORD").orEmpty()
    private val magicUrl = System.getenv("ABROVA_AUTH_LIVE_MAGIC_URL").orEmpty()

    @Test
    fun signUpWrongPasswordSignInResetMagicLinkGuest() = runBlocking {
        assumeTrue("set ABROVA_AUTH_LIVE_* to run", key.isNotEmpty() && base.isNotEmpty() && email.isNotEmpty())
        val auth = AbrovaAuth().apply { configure(key, base, OkHttpClient(), MemorySessionStorage()) }
        var result: AuthResult? = null
        // The screens' language for the run, and the texts expected in it.
        var screenLanguage = "en"
        var expectedTitle = "Sign in to Tikashop"
        var expectedWrong = "Wrong e-mail or password."
        screenLanguage = "fa"
        expectedTitle = "ورود به Tikashop"
        expectedWrong = "ایمیل یا رمز اشتباه است."

        suspend fun open(lang: String = screenLanguage): AbrovaSignInController {
            result = null
            val c = AbrovaSignInController(
                auth,
                AbrovaSignInOptions(languageCode = lang, appName = "Tikashop", allowGuest = true, magicLinkRedirectUrl = magicUrl),
                AbrovaSignInStrings.of(lang),
            ) { result = it }
            c.load()
            assertEquals(c.errorText, AbrovaSignInStep.HOME, c.step)
            return c
        }

        // Only the methods the project turned on (password, magic link, anonymous; Telegram off).
        var c = open()
        assertTrue(c.showPassword && c.showMagicLink && c.showGuest)
        assertFalse(c.showTelegram)
        assertEquals(expectedTitle, c.title)
        assertEquals(screenLanguage, auth.languageCode)

        c.go(AbrovaSignInStep.SIGN_UP)
        c.email = email
        c.password = password
        c.signUp()
        val id = result?.userId
        assertTrue("sign-up: ${c.errorText}", id != null)
        println("signed up: $id")
        c.close()
        auth.signOut()

        c = open()
        c.email = email
        c.password = "not-the-password-1"
        c.signIn()
        assertEquals("INVALID_CREDENTIALS", c.error)
        assertEquals(expectedWrong, c.errorText)
        assertNull(result)
        println("wrong password: ${c.errorText}")
        c.password = password
        c.signIn()
        assertEquals(c.errorText, id, result?.userId)
        assertEquals(id, auth.currentUserId)
        println("signed in again: same user")
        c.close()
        auth.signOut()

        c = open()
        c.email = email
        c.go(AbrovaSignInStep.FORGOT)
        c.sendReset()
        assertEquals(c.errorText, AbrovaSignInStep.SENT, c.step)
        assertEquals(AbrovaSentKind.RESET, c.sent)
        println("reset sent")
        c.close()

        c = open()
        c.go(AbrovaSignInStep.MAGIC_LINK)
        c.email = email
        c.sendMagicLink()
        assertEquals(c.errorText, AbrovaSignInStep.SENT, c.step)
        assertEquals(AbrovaSentKind.MAGIC_LINK, c.sent)
        println("magic link sent")
        c.close()

        c = open("en")
        c.continueAsGuest()
        val guest = result?.userId
        assertTrue("guest: ${c.errorText}", guest != null)
        assertNotEquals(id, guest)
        assertTrue(auth.me().isAnonymous)
        println("guest: $guest")
        c.close()
        assertNull("the e-mail language is given back", auth.languageCode)
    }
}
