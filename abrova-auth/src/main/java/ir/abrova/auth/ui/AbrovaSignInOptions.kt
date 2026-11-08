package ir.abrova.auth.ui

import android.graphics.drawable.Drawable

/**
 * Gets a Google ID token for [hashedNonce] with your Google sign-in (for example Credential Manager's
 * `GetGoogleIdOption.Builder().setNonce(hashedNonce)`). Return null when the user cancels.
 */
typealias AbrovaGoogleIdToken = suspend (hashedNonce: String) -> String?

/** What Sign in with Apple returned. */
data class AbrovaAppleCredential @JvmOverloads constructor(
    val idToken: String,
    /** Lets Abrova revoke the Apple sign-in when the account is deleted. */
    val authorizationCode: String? = null,
    /** The name Apple gives on the first sign-in only. */
    val fullName: String? = null,
)

/** Runs Sign in with Apple for [hashedNonce]. Return null when the user cancels. */
typealias AbrovaAppleCredentialFor = suspend (hashedNonce: String) -> AbrovaAppleCredential?

/** Opens [url] (the Telegram bot), for example with an `Intent.ACTION_VIEW`. */
typealias AbrovaOpenUrl = (url: String) -> Unit

/**
 * What the sign-in screen offers and how it looks. Every method also has to be turned on in the Console
 * (Abrova Auth › Sign-in methods).
 */
class AbrovaSignInOptions @JvmOverloads constructor(
    /** Shown as "Sign in to [appName]". Null: "Sign in". */
    val appName: String? = null,
    /** Shown above the title, for example your app icon. */
    val logo: Drawable? = null,
    /**
     * `en` or `fa`. Null: the app's language, English when it is neither. The e-mails Abrova sends follow it too,
     * unless you set `AbrovaAuth.languageCode` yourself.
     */
    val languageCode: String? = null,
    /**
     * Shows "Continue as guest" when anonymous sign-in is on. Off by default: every guest is a new user and counts
     * toward the plan's monthly users.
     */
    val allowGuest: Boolean = false,
    /** Shows "Continue with Google" when Google sign-in is on. */
    val google: AbrovaGoogleIdToken? = null,
    /** Shows "Continue with Apple" when Apple sign-in is on. */
    val apple: AbrovaAppleCredentialFor? = null,
    /**
     * One of the project's allowed redirect URLs that opens your app. Shows the magic link button when set and magic
     * link is on. Your app finishes it with `AbrovaAuth.shared.signInWithMagicLink(token)`.
     */
    val magicLinkRedirectUrl: String? = null,
    /** Where the confirm e-mail link goes. Null: Abrova's own confirm page. */
    val verifyRedirectUrl: String? = null,
    /** Where the password reset link goes. Null: Abrova's own reset page. */
    val resetRedirectUrl: String? = null,
    /** Opens the Telegram bot link. Without it the screen shows the link for the user to copy. */
    val openUrl: AbrovaOpenUrl? = null,
) {
    /** Builds the options from Java (Kotlin can use named arguments). Google and Apple take Kotlin suspend functions. */
    class Builder {
        private var appName: String? = null
        private var logo: Drawable? = null
        private var languageCode: String? = null
        private var allowGuest = false
        private var magicLinkRedirectUrl: String? = null
        private var verifyRedirectUrl: String? = null
        private var resetRedirectUrl: String? = null
        private var openUrl: AbrovaOpenUrl? = null

        fun appName(value: String?) = apply { appName = value }
        fun logo(value: Drawable?) = apply { logo = value }
        fun languageCode(value: String?) = apply { languageCode = value }
        fun allowGuest(value: Boolean) = apply { allowGuest = value }
        fun magicLinkRedirectUrl(value: String?) = apply { magicLinkRedirectUrl = value }
        fun verifyRedirectUrl(value: String?) = apply { verifyRedirectUrl = value }
        fun resetRedirectUrl(value: String?) = apply { resetRedirectUrl = value }
        fun openUrl(value: AbrovaOpenUrl?) = apply { openUrl = value }

        fun build() = AbrovaSignInOptions(
            appName = appName,
            logo = logo,
            languageCode = languageCode,
            allowGuest = allowGuest,
            magicLinkRedirectUrl = magicLinkRedirectUrl,
            verifyRedirectUrl = verifyRedirectUrl,
            resetRedirectUrl = resetRedirectUrl,
            openUrl = openUrl,
        )
    }
}
