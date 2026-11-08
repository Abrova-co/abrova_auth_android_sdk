<p align="center">
  <a href="https://abrova.ir">
    <img src="https://abrova.ir/icon.svg" alt="Abrova" width="120" />
  </a>
</p>

<h3 align="center">Abrova Auth Android SDK</h3>

<p align="center">
  Sign-in for your Android app's users: anonymous, Google, Apple, e-mail and password, magic link and Telegram, with a user token every Abrova SDK accepts.
</p>

<p align="center">
  <a href="https://central.sonatype.com/artifact/ir.abrova/abrova-auth"><img src="https://img.shields.io/maven-central/v/ir.abrova/abrova-auth" alt="Maven Central" /></a>
  <img src="https://img.shields.io/badge/Kotlin-2.1-7F52FF?logo=kotlin&logoColor=white" alt="Kotlin" />
  <img src="https://img.shields.io/badge/API-23+-3DDC84?logo=android&logoColor=white" alt="API 23+" />
  <img src="https://img.shields.io/badge/License-MIT-blue.svg" alt="MIT License" />
</p>

---

## Installation

The SDK is published on [Abrova Center](https://center.abrova.ir). Add its repository in `settings.gradle.kts` (or `settings.gradle`):

```kotlin
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://center.abrova.ir/api/packages/abrova/maven") }
    }
}
```

### Gradle (Kotlin DSL)

```kotlin
dependencies {
    implementation("ir.abrova:abrova-auth:0.4.0")
}
```

### Gradle (Groovy)

```groovy
dependencies {
    implementation 'ir.abrova:abrova-auth:0.4.0'
}
```

The SDK needs the `INTERNET` permission, which it declares itself.

## Quick start

Turn Abrova Auth on for your project in the Abrova Console and enable the
sign-in methods you want. Then, in `Application.onCreate`:

```kotlin
AbrovaAuth.shared.configure(this, "ab_live_YOUR_PUBLIC_KEY")
```

A session from an earlier launch is restored automatically. All sign-in calls
are `suspend` functions:

```kotlin
lifecycleScope.launch {
    AbrovaAuth.shared.signInAnonymously()
    Log.d("Auth", "user: ${AbrovaAuth.shared.currentUserId}")
}
```

## Ready-made sign-in screen

Like FirebaseUI: Abrova's sign-in screen, with no forms to build. It shows only the methods turned on in the Console
and answers the signed-in user, or null when the user closed it.

```kotlin
import ir.abrova.auth.ui.AbrovaSignIn
import ir.abrova.auth.ui.AbrovaSignInOptions

AbrovaSignIn.show(this, options = AbrovaSignInOptions(appName = "My Shop")) { result ->
    if (result != null) Log.d("Auth", "signed in: ${result.userId}")
}

// Or in a coroutine:
val result = AbrovaSignIn.await(this)
```

It opens as a dialog on wide screens and full screen on phones. To put the form in a layout of your own, use
`AbrovaSignInView(context, onSignedIn = { result -> ... })`. From Java:
`AbrovaSignIn.show(activity, result -> { ... })`.

| Option | What it does |
|---|---|
| `appName`, `logo` | The title ("Sign in to My Shop") and a `Drawable` above it |
| `languageCode` | `en` or `fa` (right to left). Default: the app's language. The e-mails follow it unless you set `languageCode` on the client yourself |
| `allowGuest` | Shows "Continue as guest" when anonymous sign-in is on. Off by default: every guest is a new user and counts toward your plan |
| `google`, `apple` | A `suspend` function that gets an ID token for the hashed nonce it is given (for example with Credential Manager or Sign in with Apple). The button shows only when you pass it |
| `magicLinkRedirectUrl` | A link that opens your app; shows the magic link button. Finish it with `signInWithMagicLink(token)` |
| `verifyRedirectUrl`, `resetRedirectUrl` | Your own pages for the confirm and reset links. Default: Abrova's pages |
| `openUrl` | Opens the Telegram bot link (for example with `Intent.ACTION_VIEW`); without it the link is shown to copy |

```kotlin
AbrovaSignInOptions(
    google = { hashedNonce ->
        // Your Google sign-in, with GetGoogleIdOption.Builder().setNonce(hashedNonce); null when cancelled.
        getGoogleIdToken(hashedNonce)
    },
)
```

The screen is built from Android's own views (no new dependencies) and takes its colours from your theme
(`colorAccent`, `colorBackground`, `textColorPrimary`), in light and dark mode. Persian is laid out right to left; that
needs `android:supportsRtl="true"` in your manifest (the default for new projects). While a sign-in runs the screen
cannot be closed.

## Sign in

```kotlin
val auth = AbrovaAuth.shared

auth.signInAnonymously()
auth.signIn("ana@example.com", "a-long-password")
val result = auth.signUp("ana@example.com", "a-long-password", redirectUrl = "https://example.com/verify")

// Magic link: send it, then sign in with the link the app is opened with.
auth.sendMagicLink("ana@example.com", redirectUrl = "https://example.com/magic")
AbrovaAuth.tokenFromLink(intent.dataString ?: "")?.let { auth.signInWithMagicLink(it) }

// Telegram: the code arrives in the project's bot.
val code = auth.requestTelegramCode("+37491234567")
if (!code.sent) startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(code.botUrl)))
auth.verifyTelegramCode("+37491234567", "123456")
```

Show only the methods the project offers:

```kotlin
val methods = auth.getMethods()
if (methods.isEnabled("google")) { /* show the Google button */ }
```

### Sign in with Google

Use Credential Manager with your **web** client id and a hashed nonce. The
nonce is required (16-256 characters; `AbrovaNonce.generate()` makes one) and
works once. Add
`androidx.credentials:credentials`, `androidx.credentials:credentials-play-services-auth`
and `com.google.android.libraries.identity.googleid:googleid` to your app:

```kotlin
val rawNonce = AbrovaNonce.generate()
val option = GetGoogleIdOption.Builder()
    .setServerClientId(WEB_CLIENT_ID)
    .setFilterByAuthorizedAccounts(false)
    .setNonce(AbrovaNonce.sha256(rawNonce))
    .build()
val response = CredentialManager.create(context)
    .getCredential(context, GetCredentialRequest.Builder().addCredentialOption(option).build())
val idToken = GoogleIdTokenCredential.createFrom(response.credential.data).idToken

auth.signInWithGoogle(idToken, rawNonce)
```

Add the client id to the project's Google settings in the Console.

### Sign in with Apple

Run Apple's web sign-in with your Services ID and `AbrovaNonce.sha256(rawNonce)`
as the nonce, then pass the `id_token` and the `code` Apple returns:

```kotlin
auth.signInWithApple(idToken, rawNonce, authorizationCode = code)
```

## Upgrade an anonymous user

Linking keeps the user's id, so everything they created stays theirs:

```kotlin
try {
    auth.linkGoogle(idToken, rawNonce)   // or linkApple(...), linkPassword(email, password)
} catch (e: AbrovaAuthException) {
    if (e.code == AbrovaAuthErrorCode.PROVIDER_ALREADY_LINKED) {
        // This Google account already belongs to another user.
    }
}
```

## Use with other Abrova SDKs

Abrova Sync, Chat and Push trust the signed-in user through a token provider:

```kotlin
val auth = AbrovaAuth.shared

// Sync
AbrovaSyncConfig.builder(apiKey).userTokenProvider(auth.blockingTokenProvider).build()

// Push
AbrovaPushConfig(apiKey = apiKey, tokenProvider = auth.tokenProvider)

// Chat
AbrovaChatConfig(apiKey = apiKey, userId = auth.currentUserId!!, tokenProvider = auth.strictTokenProvider)
```

`tokenProvider` returns null when nobody is signed in; `strictTokenProvider`
throws instead; `blockingTokenProvider` blocks the calling thread and must not
run on the main thread. All return a token that is refreshed before it expires.

## Session

```kotlin
auth.currentUserId                 // null when signed out
val user = auth.me()               // profile: anonymous, providers, ...

auth.authState.collect { userId -> /* sign-in, sign-out, session end */ }
val registration = auth.addAuthStateListener { userId -> }
registration.remove()

auth.signOut()                     // this device
auth.signOutEverywhere()           // every device
auth.deleteAccount()
```

The refresh token is stored encrypted with a key that never leaves the
Android Keystore; the access token stays in memory. A session revoked
elsewhere signs the user out here; a network error does not.

## Custom claims

Your server sets custom claims for a user with the server secret (`setCustomUserClaims`, see
[Custom claims](https://abrova.ir/docs/auth/custom-claims)), for example `{ "plan": "pro" }`. Every token of the
user carries them, and Abrova Sync rules read them as `auth.token.plan`. In the app, read them to show things
(a "Pro" badge): access is still decided on the server.

```kotlin
val result = auth.getTokenResult()
if (result?.claims?.get("plan") == "pro") showProBadge()

// Right after your server changed the claims ("Check my purchase"):
auth.getTokenResult(forceRefresh = true)
```

## E-mail language

Confirm e-mail, password reset and magic-link e-mails go out in the language you set, like Firebase's `languageCode`.
Unset, the project's template language from the Console applies. Edit the texts in **Console › Abrova Auth › Email
templates**.

```kotlin
AbrovaAuth.shared.languageCode = "fa" // or AbrovaAuth.shared.useAppLanguage()
```

See [Email templates](https://abrova.ir/docs/auth/email-templates).

## Errors

Every error is a `AbrovaAuthException`. Branch on `code` (see
`AbrovaAuthErrorCode`: `INVALID_CREDENTIALS`, `EMAIL_TAKEN`, `WEAK_PASSWORD`,
`RATE_LIMITED` with `retryAfterSec`, `PROVIDER_ALREADY_LINKED`, ...);
`message` is an English hint for developers.

## Example app

[`example/`](example) is a small app with the ready-made sign-in screen,
anonymous sign-in, e-mail sign-up and sign-in, sign-out and account deletion. It builds the SDK from this
repository:

```bash
cd example
./gradlew :app:installDebug
```

Paste your public key (`ab_live_...`) into its API key field.

## Documentation

- [Abrova Auth Docs](https://abrova.ir/cloud/abrova-auth/docs)
- [Android SDK guide](https://abrova.ir/cloud/abrova-auth/docs/sdks-android)

## Support

- **Documentation**: [Abrova Auth Docs](https://abrova.ir/cloud/abrova-auth/docs)
- **Issues**: [GitHub Issues](https://github.com/Abrova-co/abrova_auth_android_sdk/issues)
- **Email**: support@abrova.ir

## License

MIT License — see [LICENSE](LICENSE) for details.
