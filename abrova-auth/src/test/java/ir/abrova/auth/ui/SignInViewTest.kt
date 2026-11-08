package ir.abrova.auth.ui

import android.app.Activity
import android.app.Dialog
import android.os.Looper
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import ir.abrova.auth.AuthResult
import ir.abrova.auth.ui.FakeAuthServer.Companion.err
import ir.abrova.auth.ui.FakeAuthServer.Companion.json
import org.json.JSONObject
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowDialog
import java.util.concurrent.CountDownLatch

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], qualifiers = "w800dp-h1000dp")
class SignInViewTest {
    private val servers = mutableListOf<FakeAuthServer>()

    @After
    fun tearDown() = servers.forEach { it.shutdown() }

    private fun fake(vararg methods: String) = FakeAuthServer(methods.toList()).also { servers += it }

    private fun activity(): Activity = Robolectric.buildActivity(Activity::class.java).setup().get()

    /** Runs the main thread until [ok] (the fake server answers on other threads). */
    private fun waitFor(what: String, ok: () -> Boolean) {
        repeat(500) {
            shadowOf(Looper.getMainLooper()).idle()
            if (ok()) return
            Thread.sleep(10)
        }
        fail("timed out waiting for $what")
    }

    private fun View.find(tag: String): View? = findViewWithTag(tag)
    private fun View.edit(tag: String) = find(tag) as EditText
    private fun View.click(tag: String) {
        val v = find(tag) ?: error("no $tag")
        assertTrue("$tag is enabled", v.isEnabled)
        v.performClick()
        shadowOf(Looper.getMainLooper()).idle()
    }

    private fun texts(v: View): List<String> = when (v) {
        is ViewGroup -> (0 until v.childCount).flatMap { texts(v.getChildAt(it)) }
        is TextView -> listOf(v.text.toString())
        else -> emptyList()
    }

    @Test
    fun embeddedFormShowsTheMethodsAndSignsIn() {
        val f = fake("password", "magicLink", "telegram", "anonymous")
        f.answers["/v1/password/sign-in"] = { f.session("u1") }
        val results = mutableListOf<AuthResult>()
        val a = activity()
        val view = AbrovaSignInView(a, f.client(), AbrovaSignInOptions(appName = "My Shop")) { results += it }
        a.setContentView(view)
        waitFor("the form") { view.find("abrova-email") != null }
        assertTrue(texts(view).contains("Sign in to My Shop"))
        assertNotNull(view.find("abrova-telegram"))
        assertNull("no redirect URL", view.find("abrova-magic-link"))
        assertNull("guest is off by default", view.find("abrova-guest"))
        assertNull("embedded: no close button", view.find("abrova-close")?.takeIf { it.visibility == View.VISIBLE })

        view.click("abrova-sign-in")
        assertTrue(texts(view).contains("Required"))
        view.edit("abrova-email").setText("ali@example.com")
        view.edit("abrova-password").setText("a-good-password")
        view.click("abrova-sign-in")
        waitFor("the sign-in") { results.isNotEmpty() }
        assertEquals("u1", results.single().userId)
        assertEquals("the password is cleared", "", view.edit("abrova-password").text.toString())
    }

    @Test
    fun persianRightToLeftWithLeftToRightInputsAndErrorsFromTheCode() {
        val f = fake("password", "telegram")
        f.answers["/v1/password/sign-in"] = { err("INVALID_CREDENTIALS", 401) }
        val a = activity()
        // Like an app with android:supportsRtl="true" (the default for new projects).
        a.applicationInfo.flags = a.applicationInfo.flags or android.content.pm.ApplicationInfo.FLAG_SUPPORTS_RTL
        val auth = f.client()
        val view = AbrovaSignInView(a, auth, AbrovaSignInOptions(languageCode = "fa", appName = "تیکاشاپ"))
        a.setContentView(view)
        waitFor("the form") { view.find("abrova-email") != null }
        assertEquals(View.LAYOUT_DIRECTION_RTL, view.layoutDirection)
        assertEquals(View.LAYOUT_DIRECTION_LTR, view.edit("abrova-email").layoutDirection)
        assertEquals(View.TEXT_DIRECTION_LTR, view.edit("abrova-email").textDirection)
        assertTrue(texts(view).contains("ورود به تیکاشاپ"))
        assertEquals("fa", auth.languageCode)

        view.edit("abrova-email").setText("ali@example.com")
        view.edit("abrova-password").setText("wrong-password")
        view.click("abrova-sign-in")
        val banner = view.find("abrova-sign-in-error") as TextView
        waitFor("the error") { banner.visibility == View.VISIBLE }
        assertEquals("ایمیل یا رمز اشتباه است.", banner.text.toString())

        view.click("abrova-telegram")
        assertEquals(View.LAYOUT_DIRECTION_LTR, view.edit("abrova-phone").layoutDirection)

        // The screen goes away: the e-mail language is given back.
        a.setContentView(View(a))
        assertNull(auth.languageCode)
    }

    @Test
    fun signUpThatSentNoConfirmationSaysSoOnHomeWithThePasswordCleared() {
        val f = fake("password")
        f.answers["/v1/password/sign-up"] = { json(JSONObject().put("userId", "u9").put("verificationSent", false).put("session", JSONObject.NULL)) }
        val a = activity()
        val view = AbrovaSignInView(a, f.client())
        a.setContentView(view)
        waitFor("the form") { view.find("abrova-to-sign-up") != null }
        view.click("abrova-to-sign-up")
        view.edit("abrova-sign-up-email").setText("new@example.com")
        view.edit("abrova-sign-up-password").setText("a-good-password")
        view.click("abrova-sign-up")
        waitFor("home again") { view.find("abrova-password") != null }
        assertEquals("", view.edit("abrova-password").text.toString())
        assertTrue((view.find("abrova-sign-in-error") as TextView).text.contains("could not be sent"))
        assertNull(view.find("abrova-sent"))
    }

    @Test
    fun forgotPasswordShowsCheckYourEmail() {
        val f = fake("password")
        f.answers["/v1/password/forgot"] = { okhttp3.mockwebserver.MockResponse().setResponseCode(202) }
        val a = activity()
        val view = AbrovaSignInView(a, f.client())
        a.setContentView(view)
        waitFor("the form") { view.find("abrova-email") != null }
        view.edit("abrova-email").setText("ali@example.com")
        view.click("abrova-forgot")
        assertEquals("ali@example.com", view.edit("abrova-forgot-email").text.toString())
        view.click("abrova-forgot-send")
        waitFor("check your e-mail") { view.find("abrova-sent") != null }
        assertTrue((view.find("abrova-sent") as TextView).text.contains("ali@example.com"))
        assertFalse(f.bodies("/v1/password/forgot").single().has("redirectUrl"))
    }

    @Test
    fun wideScreenADialogThatCannotCloseWhileBusyAndAnswersTheUser() {
        val f = fake("password")
        f.answers["/v1/password/sign-in"] = { f.session("u1") }
        val gate = CountDownLatch(1)
        f.gate = gate
        val a = activity()
        val answers = mutableListOf<AuthResult?>()
        val dialog = AbrovaSignIn.show(a, f.client()) { answers += it }
        val root = dialog.window!!.decorView
        waitFor("the form") { root.find("abrova-email") != null }
        assertTrue(dialog.isShowing)
        assertTrue("a dialog, not full screen", dialog.window!!.attributes.width > 0)
        assertEquals(View.VISIBLE, root.find("abrova-close")!!.visibility)

        root.edit("abrova-email").setText("ali@example.com")
        root.edit("abrova-password").setText("a-good-password")
        root.click("abrova-sign-in")
        waitFor("busy") { !root.find("abrova-close")!!.isEnabled }
        dialog.onBackPressed()
        root.find("abrova-close")!!.performClick()
        shadowOf(Looper.getMainLooper()).idle()
        assertTrue("no closing while busy", dialog.isShowing)
        assertTrue(answers.isEmpty())

        gate.countDown()
        waitFor("the answer") { answers.isNotEmpty() }
        assertEquals(listOf("u1"), answers.map { it?.userId })
        assertFalse(dialog.isShowing)
    }

    @Test
    @Config(qualifiers = "w360dp-h740dp")
    fun phoneFullScreenClosingAnswersNull() {
        val f = fake("password")
        val a = activity()
        val answers = mutableListOf<AuthResult?>()
        val dialog: Dialog = AbrovaSignIn.show(a, f.client()) { answers += it }
        assertEquals(ShadowDialog.getLatestDialog(), dialog)
        val root = dialog.window!!.decorView
        waitFor("the form") { root.find("abrova-email") != null }
        assertEquals(ViewGroup.LayoutParams.MATCH_PARENT, dialog.window!!.attributes.width)
        assertEquals(ViewGroup.LayoutParams.MATCH_PARENT, dialog.window!!.attributes.height)
        root.click("abrova-close")
        assertFalse(dialog.isShowing)
        assertEquals(listOf<AuthResult?>(null), answers)
    }

    @Test
    fun backClosesWhenIdleAndTheActivityGoingAwayAnswersNull() {
        val f = fake("password")
        val controller = Robolectric.buildActivity(Activity::class.java).setup()
        val answers = mutableListOf<AuthResult?>()
        val dialog = AbrovaSignIn.show(controller.get(), f.client()) { answers += it }
        waitFor("the form") { dialog.window!!.decorView.find("abrova-email") != null }
        controller.pause().stop().destroy()
        shadowOf(Looper.getMainLooper()).idle()
        assertFalse(dialog.isShowing)
        assertEquals(listOf<AuthResult?>(null), answers)

        val again = mutableListOf<AuthResult?>()
        val d2 = AbrovaSignIn.show(activity(), f.client()) { again += it }
        waitFor("the form") { d2.window!!.decorView.find("abrova-email") != null }
        d2.onBackPressed()
        shadowOf(Looper.getMainLooper()).idle()
        assertFalse(d2.isShowing)
        assertEquals(listOf<AuthResult?>(null), again)
    }
}
