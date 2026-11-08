# Changelog

## 0.4.0

- Ready-made sign-in screens, like FirebaseUI: `AbrovaSignIn.show(activity) { result -> ... }` or, in a coroutine,
  `val result = AbrovaSignIn.await(activity)`. It opens as a dialog on wide screens and full screen on phones, and
  answers the signed-in user (null when closed). `AbrovaSignInView` puts the same form in any layout.
  - Shows only the methods turned on in the Console: e-mail and password (sign in, create account, forgot password),
    magic link, Telegram, Google, Apple and guest.
  - English and Persian (right to left), from the app's language or `languageCode`. The e-mails Abrova sends follow
    the screen's language, unless you set `languageCode` yourself.
  - Follows your app's theme colours, in light and dark mode; `appName` and `logo` for the title.
  - Google and Apple: pass a function that gets an ID token for the given hashed nonce (for example with Credential
    Manager). Magic link: pass `magicLinkRedirectUrl` (a link that opens your app).
  - "Continue as guest" shows only with `allowGuest = true`: every guest is a new user and counts toward your plan.
  - Built from Android's own views: no new dependencies.
- `sendPasswordReset`: `redirectUrl` is optional; without it the link opens Abrova's own reset page.
- `baseUrl`: the Abrova Auth server the client talks to.

## 0.3.0

- `languageCode` and `useAppLanguage()`, like Firebase: the language of the e-mails Abrova sends this user (confirm
  e-mail, password reset, magic link), for example `AbrovaAuth.shared.languageCode = "fa"`. Unset, the project's
  template language from the Console applies.

## 0.2.0

- `getTokenResult(forceRefresh)`: the access token with its decoded fields, like Firebase's `GetTokenResult`.
  `claims` holds the custom claims your server set with `setCustomUserClaims` (for example `plan`, `role`), plus
  `sub`, `iat`, `exp`, `auth_time` and `amr`; also `expirationTimestamp`, `issuedAtTimestamp`, `authTimestamp` and
  `signInProvider`. For showing things in the app; access is still decided on the server. Pass `forceRefresh = true`
  right after your server changed the claims.

## 0.1.1

- Sends `X-Abrova-SDK: auth-android/0.1.1` with every request so Abrova can show
  which SDK versions are in use. No behaviour change.

## 0.1.0

- First release.
- Sign-in methods: anonymous, Google, Apple, e-mail and password, magic link
  and Telegram code.
- Upgrade an anonymous user with `linkGoogle`, `linkApple` and
  `linkPassword`; the user keeps its id.
- `AbrovaNonce` for Google and Apple ID tokens; `rawNonce` is required for
  Google and Apple sign-in and linking.
- The refresh token is stored encrypted with an Android Keystore key; the
  access token stays in memory and is refreshed before it expires, one
  refresh at a time.
- `tokenProvider`, `strictTokenProvider` and `blockingTokenProvider` plug
  straight into the other Abrova SDKs.
- `signOut()`, `signOutEverywhere()`, `deleteAccount()`, `authState` and auth
  state listeners.
